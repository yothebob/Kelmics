package org.kel.mics.Mal



private fun createEnv() : Env {
    var totalEnv = Env()
    totalEnv.set(MalSymbol("+"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) }))
    totalEnv.set(MalSymbol("-"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) }))
    totalEnv.set(MalSymbol("*"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) }))
    totalEnv.set(MalSymbol("/"), MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) }))
    // totalEnv.set( MalSymbol("prn"), MalFunction({ a: ISeq -> a.seq().reduce({ acc, mt ->  acc = acc.toString() + mt.mal_print().toString()})}))
    return totalEnv
}

val env = createEnv()

fun mal_read3(para: String) : MalType {
    return read_str(para)
}

fun mal_eval3(para: MalType, env: Env) : MalType {
    println("21 eval3: ${para}, ${para.mal_print()}, ${para.getVal()}")
    return when {
        para is MalList && para.count() == 0 -> return para
        para is MalList -> {
            val first = para.first().getVal()
            println("first: ${first}")
            when (first) {
                "def!" -> { return env.set(para.nth(1) as MalSymbol, mal_eval3(para.nth(2), env)) }
                "let*" -> {
                    println("in let 30")
                    val child = Env(env)
                    val bindings = para.nth(1)
                    println("Bindings: ${bindings}")
                    if (bindings !is ISeq) throw MalException("expected sequence as the first parameter to let*")
                    val it = bindings.seq().iterator()
                    while (it.hasNext()) {
                        val key = it.next()
                        if (!it.hasNext()) throw MalException("odd number of binding elements in let*")
                        val value = mal_eval3(it.next(), child)
                        child.set(key as MalSymbol, value)
                    }
                    return mal_eval3(para.nth(2), child)
                }
                else -> {
                    val evaluated = para.elements.fold(MalList(), { a, b ->
                        println("a,b: ${a.mal_print()} ${b.mal_print()}")
                        a.conj_BANG(mal_eval3(b, env)); a })
                    if (evaluated.first() !is MalFunction) {
                        println("throw error, not a function")
                        return para
                    }
                    return (evaluated.first() as MalFunction).apply(evaluated.rest())
                }
            }

        }
        para is MalSymbol -> return env.get(para.value) ?: throw MalException("'${para.value}' not found")
//        is MalVector -> return para.elements.fold(MalVector(), { a, b -> a.conj_BANG(mal_eval2(b, env)); a })
//        is MalHashMap -> return para.elements.entries.fold(MalHashMap(), { a, b -> a.assoc_BANG(b.key, mal_eval2(b.value, env)); a })
        else -> return para
    }
}

fun mal_print3(para: MalType) : String {
    println(para.mal_print())
    return pr_str(para)
}


fun mal_rep3(input: String) : String {

    return mal_print3(mal_eval3(mal_read3(input), env))
}
