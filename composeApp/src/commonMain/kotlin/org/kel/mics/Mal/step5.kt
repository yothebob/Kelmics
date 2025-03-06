package org.kel.mics.Mal


private fun createEnv() : Env {
    var totalEnv = Env()
    ns.forEach({ it -> totalEnv.set(it.key, it.value) })
    return totalEnv
}


class Step5MALREPL : MALREPL() {

    override var env : Env = createEnv()

    override fun _eval(_para: MalType, _env: Env) : MalType {
        var para = _para
        var env = _env

        while (true) {
            when {
                para is MalList && para.count() == 0 -> return para
                para is MalList -> {
                    val first = para.first().getVal()
                    println("first: ${first}")
                    when (first) {
                        "fn*" -> return fn_STAR(para, env)
                        "if" -> para =  eval_if(para, env)
                        "do" -> para = eval_do(para, env)
                        "def!" -> { return env.set(para.nth(1) as MalSymbol, _eval(para.nth(2), env)) }
                        "let*" -> {
                            println("in let 30")
                            var child = Env(env)
                            val bindings = para.nth(1)
                            println("Bindings: ${bindings}")
                            if (bindings !is ISeq) return MalException("expected sequence as the first parameter to let*")
                            val it = bindings.seq().iterator()
                            while (it.hasNext()) {
                                val key = it.next()
                                if (!it.hasNext()) return MalException("odd number of binding elements in let*")
                                child.set(key as MalSymbol, _eval(it.next(), child))
                            }
                            env = child
                            para = para.nth(2)
                        }
                        else -> {
                            val evaluated = para.elements.fold(MalList(), { a, b ->
                                println("a,b: ${a.mal_print()} ${b.mal_print()}")
                                a.conj_BANG(_eval(b, env)); a })
                            val firstEval = evaluated.first()

                            when (firstEval) {
                                is MalFnFunction -> {
                                    para = firstEval.ast
                                    env = Env(firstEval.env, firstEval.params, evaluated.rest().seq())
                                }
                                is MalFunction -> return firstEval.apply(evaluated.rest())
                                else -> return MalException("cannot execute non-function")
                            }
                        }
                    }

                }
                para is MalSymbol -> return env.get(para.value) ?: throw MalException("'${para.value}' not found")
    //        is MalVector -> return para.elements.fold(MalVector(), { a, b -> a.conj_BANG(mal_eval2(b, env)); a })
    //        is MalHashMap -> return para.elements.entries.fold(MalHashMap(), { a, b -> a.assoc_BANG(b.key, mal_eval2(b.value, env)); a })
                else -> return para
            }
        }
    }

    override fun _read(para: String): MalType {
        return super._read(para)
    }

    override fun _print(para: MalType): String {
        return super._print(para)
    }

    override fun _rep(input: String): String {
        return super._rep(input)
    }


    private fun eval_do(ast: ISeq, env: Env): MalType {
        for (i in 1..ast.count() - 2) {
            _eval(ast.nth(i), env)
        }
        return ast.seq().last()
    }

    private fun fn_STAR(ast: MalList, env: Env): MalType {
        val binds = ast.nth(1) as? ISeq ?: return MalException("fn* requires a binding list as first parameter")
        val symbols = binds.seq().filterIsInstance<MalSymbol>()
        val body = ast.nth(2)
        return MalFnFunction(body, symbols, env, { s: ISeq -> _eval(body, Env(env, symbols, s.seq())) })
    }

    private fun eval_if(ast: ISeq, env: Env): MalType {
        if (ast.nth(1).truthy()) {
            return ast.nth(2)
        } else {
            if (ast.count() > 3) {
                return ast.nth(3)
            }
            return NIL
        }
        return NIL
    }
}
