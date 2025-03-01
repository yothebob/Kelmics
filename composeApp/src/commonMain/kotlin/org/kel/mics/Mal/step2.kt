package org.kel.mics.Mal

val malenv = hashMapOf<String, MalType>(
    "+" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) }),
    "-" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) }),
    "*" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) }),
    "/" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) }),
)

fun mal_read2(para: String) : MalType {
    return read_str(para)
}

fun mal_eval2(para: MalType, env: HashMap<String, MalType>) : MalType {
    return when {
        para is MalSymbol -> return env[para.value] ?: throw MalException("'${para.value}' not found")
        para is MalList && para.count() == 0 -> return para
        para is MalList -> {
            if (para.count() == 0) return para
            val evaluated = para
            //val evaluated = para.elements.fold(MalList(), { a, b -> a.conj_BANG(mal_eval2(b, env)); a })
            println(evaluated.first())
            println(evaluated.first().getVal())
            // if (evaluated.first() !is MalFunction) throw MalException("cannot execute non-function")
            return (evaluated.first() as MalFunction).apply(para.removeFirst())
        }
//        is MalVector -> return para.elements.fold(MalVector(), { a, b -> a.conj_BANG(mal_eval2(b, env)); a })
//        is MalHashMap -> return para.elements.entries.fold(MalHashMap(), { a, b -> a.assoc_BANG(b.key, mal_eval2(b.value, env)); a })
        else -> return para
    }
}

fun mal_print2(para: MalType) : String {
    println(para.mal_print())
    return pr_str(para)
}

fun mal_rep2(input: String) : String {
    return mal_print2(mal_eval2(mal_read2(input), malenv))
}
