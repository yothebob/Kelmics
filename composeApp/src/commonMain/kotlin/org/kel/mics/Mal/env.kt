package org.kel.mics.Mal

import androidx.compose.runtime.key

class Env(val outer: Env? = null) {
    val data = HashMap<String, MalType>()

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