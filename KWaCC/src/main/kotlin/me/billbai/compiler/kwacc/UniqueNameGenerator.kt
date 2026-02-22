package me.billbai.compiler.kwacc

object UniqueNameGenerator {
    private var counter = 0

    fun genUniqueName(prefix: String): String = "$prefix.${counter++}"
}