package org.kel.mics.Mal


class Env(val outer: Env?, binds: Sequence<MalSymbol>?, exprs: Sequence<MalType>?) {
    var data = HashMap<String, MalType>()

    init {
        // if binds/exprs passed; iterate and set into env
        if (binds != null && exprs != null) {
            val itb = binds.iterator()
            val ite = exprs.iterator()
            while (itb.hasNext()) {
                val b = itb.next()
                if (b.value != "&") {
                    set(b, if (ite.hasNext()) ite.next() else NIL)
                } else {
                    if (!itb.hasNext()) throw MalException("expected a symbol name for varargs")
                    set(itb.next(), MalList(ite.asSequence().toMutableList()))
                    break
                }
            }
        }
    }

    constructor() : this(null, null, null)
    constructor(outer: Env?) : this(outer, null, null)

    fun set(key: MalSymbol, defval: MalType) : MalType {
        data[key.value] = defval
//        println("adding ${key.mal_print()} with value ${defval.mal_print()}")
//        println("30 set: ${showNamespace().mal_print()}")
        return defval
    }

    fun showNamespace() : MalType {
        var acc = ""
        val stringifiedSpace = data.entries.forEach { it -> acc += "[${it.key}] => ${it.value.mal_print()}\n"}
        return MalString(acc)
    }

    fun get(key: String) : MalType? {
        return data[key] ?: outer?.get(key)
    }
}

//
//val malEnv = hashMapOf<String, MalType>(
//    "+" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger + y as MalInteger }) }),
//    "-" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger - y as MalInteger }) }),
//    "*" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger * y as MalInteger }) }),
//    "/" to MalFunction({ a: ISeq -> a.seq().reduce({ x, y -> x as MalInteger / y as MalInteger }) }),
//)
