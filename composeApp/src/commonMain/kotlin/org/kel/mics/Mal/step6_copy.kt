package org.kel.mics.Mal

//fun read(input: String?): MalType = read_str(input)
//
//fun eval(_ast: MalType, _env: Env): MalType {
//    var ast = _ast
//    var env = _env
//
//    while (true) {
//
//        val dbgeval = env.get("DEBUG-EVAL")
//        if (dbgeval !== null && dbgeval !== NIL && dbgeval !== FALSE) {
//            println ("EVAL: ${print(ast)}")
//        }
//
//        when (ast) {
//            is MalList -> {
//                if (ast.count() == 0) return ast
//                when ((ast.first() as? MalSymbol)?.value) {
//                    "def!" -> return env.set(ast.nth(1) as MalSymbol, eval(ast.nth(2), env))
//                    "let*" -> {
//                        val childEnv = Env(env)
//                        val bindings = ast.nth(1) as? ISeq ?: throw MalException("expected sequence as the first parameter to let*")
//
//                        val it = bindings.seq().iterator()
//                        while (it.hasNext()) {
//                            val key = it.next()
//                            if (!it.hasNext()) throw MalException("odd number of binding elements in let*")
//                            childEnv.set(key as MalSymbol, eval(it.next(), childEnv))
//                        }
//
//                        env = childEnv
//                        ast = ast.nth(2)
//                    }
//                    "fn*" -> return fn_STAR(ast, env)
//                    "do" -> {
//                        for (i in 1..ast.count() - 2) {
//                            eval(ast.nth(i), env)
//                        }
//                        ast = ast.seq().last()
//                    }
//                    "if" -> {
//                        val check = eval(ast.nth(1), env)
//
//                        if (check !== NIL && check !== FALSE) {
//                            ast = ast.nth(2)
//                        } else if (ast.count() > 3) {
//                            ast = ast.nth(3)
//                        } else return NIL
//                    }
//                    else -> {
//                        val evaluated = ast.elements.fold(MalList(), { a, b -> a.conj_BANG(eval(b, env)); a })
//                        val firstEval = evaluated.first()
//
//                        when (firstEval) {
//                            is MalFnFunction -> {
//                                ast = firstEval.ast
//                                env = Env(firstEval.env, firstEval.params, evaluated.rest().seq())
//                            }
//                            is MalFunction -> return firstEval.apply(evaluated.rest())
//                            else -> throw MalException("cannot execute non-function")
//                        }
//                    }
//                }
//            }
//            is MalSymbol -> return env.get(ast.value) ?: throw MalException("'${ast.value}' not found")
//            is MalVector -> return ast.elements.fold(MalVector(), { a, b -> a.conj_BANG(eval(b, env)); a })
//            is MalHashMap -> return ast.elements.entries.fold(MalHashMap(), { a, b -> a.assoc_BANG(b.key, eval(b.value, env)); a })
//            else -> return ast
//        }
//    }
//}
//
//private fun fn_STAR(ast: MalList, env: Env): MalType {
//    val binds = ast.nth(1) as? ISeq ?: throw MalException("fn* requires a binding list as first parameter")
//    val params = binds.seq().filterIsInstance<MalSymbol>()
//    val body = ast.nth(2)
//
//    return MalFnFunction(body, params, env, { s: ISeq -> eval(body, Env(env, params, s.seq())) })
//}
//
//fun print(result: MalType) = pr_str(result)
//
//fun rep(input: String, env: Env): String =
//    print(eval(read(input), env))
