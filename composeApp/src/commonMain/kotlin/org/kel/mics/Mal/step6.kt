package org.kel.mics.Mal


fun step6createEnv() : Env {
    var totalEnv = Env()
    ns.forEach({ it -> totalEnv.set(it.key, it.value) })
    totalEnv.set(MalSymbol("shownamespace"), MalFunction({ a: ISeq ->
        println(totalEnv.showNamespace())
        totalEnv.showNamespace()
    }))
    return totalEnv
}


class Step6MALREPL : MALREPL() {

    override var internalenv = Env()

    init {
        internalenv = step6createEnv()
    }

    override fun _eval(_para: MalType, _env: Env) : MalType {

        var cenv = _env
        var ast = _para

        while (true) {

            val dbgeval = cenv.get("DEBUG-EVAL")
            if (dbgeval !== null && dbgeval !== NIL && dbgeval !== FALSE) {
                println ("EVAL: ${print(ast)}")
            }

            when (ast) {
                is MalList -> {
                    if (ast.count() == 0) return ast
                    when ((ast.first() as? MalSymbol)?.value) {
                        "def!" -> {
                            println("updating cenv with def ${ast.nth(1).mal_print()} to ${ast.nth(2).mal_print()}")
                            return cenv.set(ast.nth(1) as MalSymbol, _eval(ast.nth(2), env))
                        }
                        "let*" -> {
                            val childEnv = Env(cenv)
                            val bindings = ast.nth(1) as? ISeq ?: return MalException("expected sequence as the first parameter to let*")

                            val it = bindings.seq().iterator()
                            while (it.hasNext()) {
                                val key = it.next()
                                if (!it.hasNext()) return MalException("odd number of binding elements in let*")
                                childEnv.set(key as MalSymbol, _eval(it.next(), childEnv))
                            }

                            cenv = childEnv
                            ast = ast.nth(2)
                        }
                        "fn*" -> {
                            val binds = ast.nth(1) as? ISeq ?: return MalException("fn* requires a binding list as first parameter")
                            val symbols = binds.seq().filterIsInstance<MalSymbol>()
                            val body = ast.nth(2)
                            return MalFnFunction(body, symbols, cenv, { s: ISeq -> this._eval(body, Env(cenv, symbols, s.seq())) })

                        }
                        "do" -> {
                            for (i in 1..ast.count() - 2) {
                                this._eval(ast.nth(i), cenv)
                            }
                            ast = ast.seq().last()
                        }
                        "if" -> {
                            val check = this._eval(ast.nth(1), cenv)

                            if (check !== NIL && check !== FALSE) {
                                ast = ast.nth(2)
                            } else if (ast.count() > 3) {
                                ast = ast.nth(3)
                            } else return NIL
                        }
                        else -> {
                            val evaluated = ast.elements.fold(MalList(), { a, b -> a.conj_BANG(this._eval(b, cenv)); a })
                            val firstEval = evaluated.first()

                            when (firstEval) {
                                is MalFnFunction -> {
                                    ast = firstEval.ast
                                    cenv = Env(firstEval.env, firstEval.params, evaluated.rest().seq())
                                }
                                is MalFunction -> return firstEval.apply(evaluated.rest())
                                else -> return MalException("cannot execute non-function")
                            }
                        }
                    }
                }
                is MalSymbol -> return cenv.get(ast.value) ?: return MalException("'${ast.value}' not found")
                is MalVector -> return ast.elements.fold(MalVector(), { a, b -> a.conj_BANG(_eval(b, cenv)); a })
                is MalHashMap -> return ast.elements.entries.fold(MalHashMap(), { a, b -> a.assoc_BANG(b.key, _eval(b.value, cenv)); a })
                else -> return ast
            }
        }
    }

    override fun _read(para: String): MalType {
        return super._read(para)
    }

    override fun _print(para: MalType): String {
        return super._print(para)
    }

    override fun _rep(input: String, _env: Env): String {
        return super._rep(input, _env)
    }
}
