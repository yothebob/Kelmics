package org.kel.mics.Mal
import kotlin.text.Regex

val TOKEN_REGEX = Regex("[\\s,]*(~@|[\\[\\]{}()'`~^@]|\"(?:\\\\.|[^\\\\\"])*\"?|;.*|[^\\s\\[\\]{}('\"`,;)]*)")
val ATOM_REGEX = Regex("(^-?[0-9]+$)|(^nil$)|(^true$)|(^false$)|^\"((?:\\\\.|[^\\\\\"])*)\"$|^\"(.*)$|:(.*)|(^[^\"]*$)")

class Reader(sequence: Sequence<String>) {
    val tokens = sequence.iterator()
    var current = advance()

    fun next() : String? { // """Set current to next token."""
        var result = current
        current = advance()
        return result
    }

    fun peek(): String? = current // look at the current token

    private fun advance(): String? = if (tokens.hasNext()) tokens.next() else null //get next token or null

}

fun read_str(input: String?) : MalType {
    val seq = tokenizer(input!!)
    if (seq == null) return NIL // return NIL
    val reader = Reader(sequence = seq)
    return read_form(reader)
}

fun tokenizer(input: String?) : Sequence<String>? {
    if (input.isNullOrBlank()) return null
    return TOKEN_REGEX.findAll(input)
        .map({ it -> it.groups[1]?.value as String })
        .filter({ it != "" && !it.startsWith(";")})
}

fun read_form(r : Reader) : MalType {
    val nxt = r.peek()
    return when (nxt) {
        null -> throw MalContinue()
        "("  -> read_list(r)
        ")"  -> throw MalReaderException("expected form, got ')'")
        "["  -> read_vector(r)
        "]"  -> throw MalReaderException("expected form, got ']'")
        "{"  -> read_hashmap(r)
        "}"  -> throw MalReaderException("expected form, got '}'")
        "'"  -> read_shorthand(r, "quote")
        "`"  -> read_shorthand(r, "quasiquote")
        "~"  -> read_shorthand(r, "unquote")
        "~@" -> read_shorthand(r, "splice-unquote")
        "^"  -> read_with_meta(r)
        "@"  -> read_shorthand(r, "deref")
        else -> read_atom(r)
    }
}

fun read_vector(reader: Reader): MalType = read_sequence(reader, MalVector(), "]")

fun read_list(r : Reader) : MalType {
    println("47: ${r}")
    return read_sequence(r, MalList(), ")")
}

private fun read_sequence(reader: Reader, sequence: IMutableSeq, end: String): MalType {
//    println("51: ${sequence.mal_print()} ${sequence}")
    reader.next()
//    println("form after next: ${reader.peek()}")

    do {
        val form = when (reader.peek()) {
            null -> println("throw error")//throw MalReaderException("expected '$end', got EOF")
            end  -> { reader.next(); null }
            else -> read_form(reader)
        }

        if (form != null) {
            sequence.conj_BANG(form as MalType)
        }
    } while (form != null)

    return sequence
}

fun read_atom(r: Reader) : MalType {
    //TODO: impliment myself
    val next = r.next() ?: throw MalReaderException("Unexpected null token")
    val groups = ATOM_REGEX.find(next)?.groups ?: throw MalReaderException("Unrecognized token: " + next)

    return if (groups[1]?.value != null) {
        MalInteger(groups[1]?.value?.toLong() ?: throw MalReaderException("Error parsing number: " + next))
    } else if (groups[2]?.value != null) {
        NIL
    } else if (groups[3]?.value != null) {
        TRUE
    } else if (groups[4]?.value != null) {
        FALSE
    } else if (groups[5]?.value != null) {
        MalString((groups[5]?.value as String).replace(Regex("""\\(.)"""))
        { m: MatchResult ->
            if (m.groups[1]?.value == "n") "\n"
            else m.groups[1]?.value.toString()
        })
    } else if (groups[6]?.value != null) {
        throw MalReaderException("expected '\"', got EOF")
    } else if (groups[7]?.value != null) {
        MalKeyword(groups[7]?.value as String)
    } else if (groups[8]?.value != null) {
        MalSymbol(groups[8]?.value as String)
    } else {
        throw MalReaderException("Unrecognized token: " + next)
    }
}

fun read_shorthand(reader: Reader, symbol: String): MalType {
    reader.next()

    val list = MalList()
    list.conj_BANG(MalSymbol(symbol))
    list.conj_BANG(read_form(reader))

    return list
}

fun read_with_meta(reader: Reader): MalType {
    reader.next()

    val meta = read_form(reader)
    val obj = read_form(reader)

    val list = MalList()
    list.conj_BANG(MalSymbol("with-meta"))
    list.conj_BANG(obj)
    list.conj_BANG(meta)

    return list
}

fun read_hashmap(reader: Reader): MalType {
    reader.next()
    val hashMap = MalHashMap()

    do {
        var value : MalType? = null;
        val key = when (reader.peek()) {
            null -> throw MalReaderException("expected '}', got EOF")
            "}"  -> { reader.next(); null }
            else -> {
                var key = read_form(reader)
                if (key !is MalString) {
                    throw MalReaderException("hash-map keys must be strings or keywords")
                }
                value = when (reader.peek()) {
                    null -> throw MalReaderException("expected form, got EOF")
                    else -> read_form(reader)
                }
                key
            }
        }

        if (key != null) {
            hashMap.assoc_BANG(key, value as MalType)
        }
    } while (key != null)

    return hashMap
}

