package org.kel.mics.Mal

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



)


open class MALREPL() {

    open var env: Env = Env()

    open fun _read(para: String) : MalType {
        return read_str(para)
    }

    open fun _eval(para: MalType, env: Env) : MalType {
        return para
    }

    open fun _print(para: MalType) : String {
        return pr_str(para)
    }

    open fun _rep(input: String) : String {
        return _print(_eval(_read(input), env))
    }
}
