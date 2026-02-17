package org.kel.mics.Mal.Namespaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kel.mics.ASYNC_BUFFER
import org.kel.mics.CURRENT_BUFFER
import org.kel.mics.IO.dispatchSocketCall
import org.kel.mics.IO.readFileToStr
import org.kel.mics.IO.translateNewLines
import org.kel.mics.Mal.FALSE
import org.kel.mics.Mal.ISeq
import org.kel.mics.Mal.MalAtom
import org.kel.mics.Mal.MalConstant
import org.kel.mics.Mal.MalCoreException
import org.kel.mics.Mal.MalException
import org.kel.mics.Mal.MalFunction
import org.kel.mics.Mal.MalInteger
import org.kel.mics.Mal.MalList
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalSymbol
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.MalVector
import org.kel.mics.Mal.NIL
import org.kel.mics.Mal.TRUE
import org.kel.mics.Mal.Type
import org.kel.mics.Mal.asyncOutputVal
import org.kel.mics.Mal.pr_str
import org.kel.mics.Mal.read_str
import org.kel.mics.textHook

val ns = hashMapOf<MalSymbol, MalType>(
    MalSymbol("+") to MalFunction({ a: ISeq ->
        a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) }, Args=listOf(
        Type.INT,
        Type.INT
    )),
    MalSymbol("-") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) }, Args=listOf(
        Type.INT,
        Type.INT
    )),
    MalSymbol("*") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) }, Args=listOf(
        Type.INT,
        Type.INT
    )),
    MalSymbol("/") to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) },
        Args=listOf(
            Type.INT,
            Type.INT
        )),

    MalSymbol("prn") to MalFunction({ a: ISeq ->
        println(a.seq().map { it.mal_print() }.joinToString(" "))
        NIL },
        Args=listOf(
            Type.STRING,
        )),
    MalSymbol("str") to MalFunction({ a: ISeq ->
        MalString(a.seq().map { it.mal_print() }.joinToString(" "))
    }),
    MalSymbol("pr-str") to MalFunction({ a: ISeq ->
        MalString(a.seq().map { it.mal_print() }.joinToString(""))
    }, docs="(pr-str MALSTRING+) -> MALSTRING\n  adds mal_print() values together and returns"),
    MalSymbol("string-concat") to MalFunction({ a: ISeq ->
        MalString(a.seq().map { it.mal_print() }.joinToString(""))
    }, docs="(string-concat MALSTRING+) -> MALSTRING\n  adds mal_print() values together and returns"),
    MalSymbol("string-split") to MalFunction({ a: ISeq ->
        val splitter = a.first() as? MalString ?: throw MalException("takes a str arg")
        val toSplit = a.nth(1) as? MalString ?: throw MalException("takes a str arg")
        MalList( toSplit.value.split(splitter.value).map { MalString(it) }.toMutableList())
    }, docs="(string-split SPLITTER:MALSTRING str:MALSTRING) -> MALLIST\n  split STR by SPLITTER, returning mallist."),

    MalSymbol("list") to MalFunction({ a: ISeq -> MalList(a.seq().toMutableList()) }),
    MalSymbol("list?") to MalFunction({ a: ISeq -> if (a.first() is MalList) { TRUE } else { FALSE } }),
    // MalSymbol("truthy") to MalFunction({ a: ISeq -> MalConstant(a.first().truthy().toString())})

    MalSymbol("count") to MalFunction({ a: ISeq -> if (a.first() is MalList) { MalInteger((a.first() as MalList).count().toLong()) } else { NIL } }),

    MalSymbol("=") to MalFunction({ a: ISeq -> if (a.first() == a.nth(1)) { TRUE } else { FALSE } }),
    MalSymbol("<=") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value <= (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")
    } }),
    MalSymbol(">=") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value >= (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")
    } }),
    MalSymbol(">") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value > (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")
    } }),
    MalSymbol("<") to MalFunction({ a: ISeq -> if (a.first() is MalInteger && a.nth(1) is MalInteger) {  MalConstant(((a.first() as MalInteger).value < (a.nth(1) as MalInteger).value).toString()) } else { MalException("Not a number")
    } }),

    MalSymbol("read-string") to MalFunction({ a: ISeq -> if (a.first() is MalString) {  read_str(a.first().getVal()) } else { MalException("read-string only takes string arguments, argument was ${a.first().mal_print()}")
    } }),
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
        ASYNC_BUFFER.buf.clear()
        scope.launch {
            val result = dispatchSocketCall(addrs.value, 9002, cmd.value, asyncOutputVal)
            ASYNC_BUFFER.buf.writeUtf8("${translateNewLines(result.getVal())}")
        }
        MalString(asyncOutputVal.last().value)
    }, docs="(remote-shell-command CMD:MAL_STRING ADDRS:MALSTRING) -> NIL \n  Takes a bash CMD and ip ADDRS and runs the CMD, putting result in *Remote-msg* buffer"),

    MalSymbol("translate-new-lines") to MalFunction({a : ISeq ->
        val str = a.first() as? MalString ?: throw MalException("takes a str arg")
        MalString(translateNewLines(str.value))
    }, docs="(translate-new-lines STR:MALSTRING) -> MALSTRING \n  converts remote-shell-command nl to newlines/carrige breaks"),

    MalSymbol("autocomplete-prompt") to MalFunction({ a: ISeq ->
        val autocompleteText = a.first() as? MalString ?: throw MalException("takes a str arg")
        textHook.value = autocompleteText.getVal()
        NIL
    }),

    MalSymbol("point-min") to MalFunction({a : ISeq ->
        MalInteger(0)
    }),
    MalSymbol("point-max") to MalFunction({a : ISeq ->
        MalInteger(CURRENT_BUFFER.value.buf.size)
    }),
    // Take a type, update its documentation and return it?
    MalSymbol("set-doc") to MalFunction({ a: ISeq -> a.first().documentation = a.nth(1).toString()
        a.first()
    }),
    MalSymbol("doc") to MalFunction({ a: ISeq ->
        MalString(a.seq().map { it.mal_print() }.joinToString(""))
    }),
    MalSymbol("cons") to MalFunction({ a: ISeq -> if (a.nth(1) is MalList) {
        val newlist = (a.nth(1) as MalList).seq().toMutableList()
        newlist.add(0, a.first())
        MalList(newlist)
    } else { MalException("Second argument ${a.nth(1).mal_print()} is not a list") } }),
    MalSymbol("concat") to MalFunction({ a: ISeq ->
        MalList(a.seq().flatMap { (it as ISeq).seq() }.toMutableList())
    }),
    MalSymbol("vec") to MalFunction({ a: ISeq ->
        if (a.first() is ISeq) {
            MalVector(a)
        } else {
            MalException("MalVector expects seq, got ${a.first()}")
        }
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
        MalCoreException(pr_str(throwable), throwable)
    }),
)