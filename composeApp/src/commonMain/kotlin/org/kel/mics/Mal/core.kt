package org.kel.mics.Mal

import okio.FileSystem
import okio.Path.Companion.toPath
import org.kel.mics.IO.readFileToStr

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
