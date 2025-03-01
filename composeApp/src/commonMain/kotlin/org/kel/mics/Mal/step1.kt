package org.kel.mics.Mal

fun mal_read(para: String) : MalType {
    return read_str(para)
}

fun mal_eval(para: MalType) : MalType {
    return para
}

fun mal_print(para: MalType) : String {
    return pr_str(para)
}

fun mal_rep(input: String) : String {
    return mal_print(mal_eval(mal_read(input)))
}
