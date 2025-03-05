package org.kel.mics.Mal

import androidx.compose.runtime.key

class Env(val outer: Env? = null, binds: Sequence<MalSymbol>? = null, exprs: Sequence<MalType>? = null) {
    val data = HashMap<String, MalType>()

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
                    // set(itb.next(), MalList(ite.asSequence().toCollection(LinkedList<MalType>())))
                    set(itb.next(), MalList())
                    break
                }
            }
        }
    }

    fun set(key: MalSymbol, defval: MalType) : MalType {
        data.put(key.value, defval)
        return defval
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