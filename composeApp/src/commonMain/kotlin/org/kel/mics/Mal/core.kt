package org.kel.mics.Mal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.kel.mics.ASYNC_BUFFER
import org.kel.mics.BUFFERS
import org.kel.mics.IO.dispatchSocketCall
import org.kel.mics.IO.readFileToStr

val asyncOutputVal =  mutableStateListOf<MalString>(MalString(""))

@OptIn(ExperimentalCoroutinesApi::class)
val ns = hashMapOf<MalSymbol, MalType>(
    MalSymbol("+") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) }),
    MalSymbol("-")to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) }),
    MalSymbol("*") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) }),
    MalSymbol("/") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) }),

    MalSymbol("prn") to MalFunction({ a: ISeq ->
        println(a.seq().map { it.mal_print() }.joinToString(" "))
        NIL }),
    MalSymbol("str") to MalFunction({ a: ISeq ->
        println(a.seq().map { it.mal_print() }.joinToString(" "))
        MalString(a.seq().map { it.mal_print() }.joinToString(" "))}),
    MalSymbol("pr-str") to MalFunction({ a: ISeq ->
					println(a.seq().map { it.mal_print() }.joinToString(" "))
					MalString(a.seq().map { it.mal_print() }.joinToString(" "))}),
    
//    pr-str, prn, println


    MalSymbol("list") to MalFunction({ a: ISeq -> MalList(a.seq().toMutableList()) }),
    MalSymbol("list?") to MalFunction({ a: ISeq -> if (a.first() is MalList) { TRUE } else { FALSE } }),
    // MalSymbol("truthy") to MalFunction({ a: ISeq -> MalConstant(a.first().truthy().toString())})

    MalSymbol("count") to MalFunction({ a: ISeq -> if (a.first() is MalList) { MalInteger((a.first() as MalList).count().toLong()) } else { NIL } }),

    MalSymbol("=") to MalFunction({ a: ISeq -> if (a.first() == a.nth(1)) { TRUE } else { FALSE } }),
    MalSymbol("<=") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value <= (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")} }),
    MalSymbol(">=") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value >= (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")} }),
    MalSymbol(">") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value > (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")} }),
    MalSymbol("<") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value < (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")} }),

    MalSymbol("read-string") to MalFunction({ a: ISeq -> if (a.first() is MalString) {  read_str(a.first().getVal()) } else { MalException("read-string only takes string arguments, argument was ${a.first().mal_print()}")} }),
    MalSymbol("slurp") to MalFunction({ a: ISeq ->
      val name = a.first() as? MalString ?: throw MalException("slurp requires a filename parameter")
      MalString(readFileToStr(name.value))
    }),
    MalSymbol("atom") to MalFunction({ a: ISeq ->
        MalAtom(a.first()) // maybe throw some errors, like on constants?
    }),
    MalSymbol("atom?") to MalFunction({ a: ISeq ->
        val isAtom = a.first() is MalAtom
        MalConstant(isAtom.toString())
    }),
    MalSymbol("deref") to MalFunction({ a: ISeq ->
        (a.first() as MalAtom).value
    }),
    MalSymbol("reset!") to MalFunction({ a: ISeq ->
					   val atom = a.first() as MalAtom
					   val value = a.nth(1)
					   atom.value = value
					   value
				       }),
    MalSymbol("swap!") to MalFunction({ a: ISeq ->
					  val atom = a.nth(0) as MalAtom
					  val function = a.nth(1) as MalFunction
					  
					  val params = MalList()
					  params.conj_BANG(atom.value)
					  a.seq().drop(2).forEach({ it -> params.conj_BANG(it) })
					  
					  val value = function.apply(params)
					  atom.value = value
					  value
				       }),


    MalSymbol("remote-shell-command") to MalFunction({ a: ISeq -> // NON ASYNC!
        val cmd = a.first() as? MalString ?: throw MalException("Requires a String Param")
        val addrs = a.nth(1) as? MalString ?: MalString("0.0.0.0")
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            val result = dispatchSocketCall(addrs.value, 9002, cmd.value, asyncOutputVal)
            ASYNC_BUFFER.buf.writeUtf8(result.getVal())
        }
        NIL

    }),
    MalSymbol("nth-remote") to MalFunction({ a: ISeq -> // NON ASYNC!
        val idx =  a.first() as? MalInteger ?: MalInteger(0) // TODO: make a .getOrElse Function
        val responses = ASYNC_BUFFER.buf.snapshot().utf8().split("\n")
        println(responses)
        println(idx)
        val res = responses.getOrNull(idx.value.toInt())
        if (res != null) MalString(res) else NIL
    }),

    MalSymbol("read-buffer") to MalFunction({a : ISeq ->
        val buffName = a.first() as? MalString ?: throw MalException("takes a str arg")
        val buffToRead = BUFFERS.first { buffName.value == it.name }
        MalString(buffToRead.buf.snapshot().utf8())
    }),

    // Take a type, update its documentation and return it?
    MalSymbol("set-doc") to MalFunction({ a: ISeq -> a.first().documentation = a.nth(1).toString()
        a.first()
    }),
    MalSymbol("doc") to MalFunction({ a: ISeq -> read_str(a.first().documentation) }),


    MalSymbol("cons") to MalFunction({ a: ISeq -> if (a.nth(1) is MalList) {
        val newlist = (a.nth(1) as MalList).seq().toMutableList()
        newlist.add(0, a.first())
        MalList(newlist)
    } else { MalException("Second argument ${a.nth(1).mal_print()} is not a list") } }),
    MalSymbol("concat") to MalFunction({ a: ISeq ->
        MalList(a.seq().flatMap { (it as ISeq).seq() }.toMutableList())
    }),
    MalSymbol("vec") to MalFunction({ a: ISeq ->
        if (a.first() is ISeq) {MalVector(a)} else {MalException("MalVector expects seq, got ${a.first()}")}
    }),
    MalSymbol("nth") to MalFunction({ a: ISeq -> // TODO: add vect supp
        val idx = a.first() as? MalInteger ?: throw MalException("Requires a Int Param")
        val lst = a.nth(1) as? ISeq ?: throw MalException("Requires a List Param")
        if (lst.count() > idx.value) lst.nth(idx.value.toInt()) else MalException("${idx.value} Out of list ${lst.mal_print()} range.")
    }),
    MalSymbol("first") to MalFunction({ a: ISeq -> // TODO: add vect supp
        val lst = a.first() as? ISeq ?: throw MalException("Requires a List Param")
        if (lst.count() > 0) lst.first() else NIL
    }),
    MalSymbol("rest") to MalFunction({ a: ISeq -> // TODO: add vect supp
        val lst = a.first() as? ISeq ?: throw MalException("Requires a List Param")
        MalList(lst.rest().seq().toMutableList())
    }),
    MalSymbol("throw") to MalFunction({ a: ISeq ->
        val throwable = a.nth(0)
        throw MalCoreException(pr_str(throwable), throwable)
    }),

)


open class MALREPL(open var internalenv: Env = Env()) {

 //   open var internalenv = Env()

    open fun _read(para: String) : MalType {
        return read_str(para)
    }

    open fun _eval(para: MalType, env: Env) : MalType {
        return para
    }

    open fun _print(para: MalType) : String {
        return pr_str(para)
    }

    open fun _rep(input: String, _env: Env = internalenv) : String {
        return _print(_eval(_read(input), _env))
    }
}
