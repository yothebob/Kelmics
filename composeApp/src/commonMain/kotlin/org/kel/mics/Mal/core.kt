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
)

