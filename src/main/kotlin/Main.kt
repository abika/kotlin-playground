package org.abika

import java.math.BigInteger

/**
 * @author Alexander Bikadorov {@literal <goto@openmailbox.org>}
 */
fun translate(rna: String?): List<String> {
    val rawMap: Map<List<String>, String> = mapOf(
        Pair(listOf("AUG"), "Methionine"),
        Pair(listOf("UUU", "UUC"), "Phenylalanine"),
        Pair(listOf("UUA", "UUG"), "Leucine"),
        Pair(listOf("UCU", "UCC", "UCA", "UCG"), "Serine"),
        Pair(listOf("UAU", "UAC"), "Tyrosine"),
        Pair(listOf("UGU", "UGC"), "Cysteine"),
        Pair(listOf("UGG"), "Tryptophan"),
        Pair(listOf("UAA", "UAG", "UGA"), "STOP"),
    )

    val map = rawMap
        .flatMap { (l, protein) -> l.map { codon -> Pair(codon, protein) } }
        .toMap()

    return if (rna == null) listOf() else
        rna.chunked(3).asSequence()
            .map { codon -> map.getOrElse(codon, { -> throw IllegalArgumentException() }) }
            .takeWhile { protein -> protein != "STOP" }
            .toList()
}

class MinesweeperBoard(val board: List<String>) {

    fun withNumbers(): List<String> {
        return board.mapIndexed { y, row ->
            row.mapIndexed { x, v ->
                if (v == '*') '*' else getNeighbours(x, y).filter { i -> i == '*' }.count()
            }.map { v -> if (v == 0) ' ' else v }.joinToString("")
        }
    }

    private fun getNeighbours(x: Int, y: Int): List<Char> {
        return listOf(
            getValue(x - 1, y - 1), getValue(x - 0, y - 1), getValue(x + 1, y - 1),
            getValue(x - 1, y - 0), getValue(x + 1, y - 0),
            getValue(x - 1, y + 1), getValue(x - 0, y + 1), getValue(x + 1, y + 1)
        )
            .filterNotNull()
    }

    private fun getValue(x: Int, y: Int): Char? {
        return board.getOrNull(y)?.getOrNull(x)
    }
}

object DiffieHellman {

    fun privateKey(prime: BigInteger): BigInteger {
        return createRandomBigInteger(prime)
    }

    private fun createRandomBigInteger(upperLimit: BigInteger): BigInteger {
        val random = java.util.Random()
        var randomNumber: BigInteger
        do {
            randomNumber = BigInteger(upperLimit.bitLength(), random);
        } while (randomNumber >= upperLimit);
        return randomNumber
    }

    fun publicKey(p: BigInteger, g: BigInteger, privKey: BigInteger): BigInteger {
        return g.modPow(privKey, p)
    }

    fun secret(prime: BigInteger, publicKey: BigInteger, privateKey: BigInteger): BigInteger {
        return publicKey.modPow(privateKey, prime)
    }
}

val greekAlphabet = listOf(
    Pair(1000, "M"),
    Pair(900, "CM"),
    Pair(500, "D"),
    Pair(400, "CD"),
    Pair(100, "C"),
    Pair(90, "XC"),
    Pair(50, "L"),
    Pair(40, "XL"),
    Pair(10, "X"),
    Pair(9, "IX"),
    Pair(5, "V"),
    Pair(4, "IV"),
    Pair(1, "I"),
)

fun decimalToGreek(n: Int): String {
    var s = ""
    var nn = n
    for ((v, l) in greekAlphabet) {
        while (nn >= v) {
            nn -= v
            s += l
        }
    }
    return s
}

fun greekToDecimal(s: String): Int {
    var n = 0
    var ss = s
    for ((v, l) in greekAlphabet) {
        while (ss.startsWith(l)) {
            ss = ss.removePrefix(l)
            n += v
        }
    }
    return n
}

fun isValid(input: String): Boolean {
    val bracketMap = mapOf('(' to ')', '{' to '}', '[' to ']')
    fun rec(s: String, stack: ArrayDeque<Char>): Boolean {
        if (s.isEmpty()) return stack.isEmpty()
        when (s[0]) {
            '(', '{', '[' -> {
                stack.addFirst(s[0])
            }

            ')', '}', ']' -> {
                if (stack.isEmpty()) return false
                val c = stack.removeFirst()
                if (s[0] != bracketMap[c]) return false
            }
        }
        return rec(s.substring(1), stack)
    }

    return rec(input, ArrayDeque())
}

fun main() {
    val rawInput = """·*·*·
··*··
··*··
·····"""
    println(rawInput)
    val input = rawInput.split('\n')
    val result = MinesweeperBoard(input).withNumbers()
    println(result.joinToString("\n"))

    println(decimalToGreek(1996))
    println(greekToDecimal("MCMXCVI"))

    val callValidate: (String) -> Pair<String, Boolean> = { s: String -> Pair(s, isValid(s)) }
    println(callValidate(""))
    println(callValidate("{}"))
    println(callValidate("{[(0987)]}"))
    println(callValidate("{[(0987])}"))
}
