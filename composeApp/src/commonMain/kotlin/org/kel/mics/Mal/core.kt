package org.kel.mics.Mal

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

val asyncOutputVal = mutableStateListOf<MalString>(MalString(""))

fun malArgTypes(argumentList: List<Type?>, suppliedArgs: ISeq) : MalType {
    argumentList.forEachIndexed { i, it ->
        if (it != null) {
            if (it.MakeMalType(suppliedArgs.nth(i).getVal()) == suppliedArgs.nth(i)) {
                return MalException("Argument ${i}: ${it.MakeMalType(suppliedArgs.nth(i).getVal())} could not be cast into ${it} type.")
            }
        }
    }
    return NIL
}

open class MALREPL(open var internalenv: Env = Env()) {

 //   open var internalenv = Env()

    open fun _read(para: String) : MalType {
        return read_str(para)
    }

    open fun _eval(para: MalType, env: Env) : MalType {
        return para
    }

    open fun _print(para: MalType) : String {
        return pr_str(para)
    }

    open fun _rep(input: String, _env: Env = internalenv) : String {
        return _print(_eval(_read(input), _env))
    }
}
