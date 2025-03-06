package org.kel.mics.Mal



private fun createEnv() : Env {
    var totalEnv = Env()
    ns.forEach({ it -> totalEnv.set(it.key, it.value) })
    return totalEnv
}

val env2 = createEnv()

fun mal_read4(para: String) : MalType {
    return read_str(para)
}

private fun eval_do(ast: ISeq, env: Env): MalType {
    for (i in 1..ast.count() - 2) {
        mal_eval4(ast.nth(i), env)
    }
    return mal_eval4(ast.seq().last(), env)
}

private fun eval_fn_STAR(ast: ISeq, env: Env): MalType {
    val binds = ast.nth(1) as? ISeq ?: throw MalException("fn* requires a binding list as first parameter")
    val symbols = binds.seq().filterIsInstance<MalSymbol>()
    val body = ast.nth(2)

    return MalFunction({ s: ISeq ->
        mal_eval4(body, Env(env, symbols, s.seq()))
    })
}

private fun eval_if(ast: ISeq, env: Env): MalType {
    if (ast.nth(1).truthy()) {
        return mal_eval4(ast.nth(2), env)
    } else {
        if (ast.count() > 3) {
            return mal_eval4(ast.nth(3), env)
        }
        return NIL
    }
    return NIL
}


fun mal_eval4(para: MalType, env: Env) : MalType {
    println("21 eval3: ${para}, ${para.mal_print()}, ${para.getVal()}")
    return when {
        para is MalList && para.count() == 0 -> return para
        para is MalList -> {
            val first = para.first().getVal()
            println("first: ${first}")
            when (first) {
                "fn*" -> return eval_fn_STAR(para, env)
                "if" -> return eval_if(para, env)
                "do" -> return eval_do(para, env)
                "def!" -> { return env.set(para.nth(1) as MalSymbol, mal_eval4(para.nth(2), env)) }
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
                        val value = mal_eval4(it.next(), child)
                        child.set(key as MalSymbol, value)
                    }
                    return mal_eval4(para.nth(2), child)
                }
                else -> {
                    val evaluated = para.elements.fold(MalList(), { a, b ->
                        println("a,b: ${a.mal_print()} ${b.mal_print()}")
                        a.conj_BANG(mal_eval4(b, env)); a })
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

fun mal_print4(para: MalType) : String {
    println(para.mal_print())
    return pr_str(para)
}


fun mal_rep4(input: String) : String {

    return mal_print4(mal_eval4(mal_read4(input), env2))
}
