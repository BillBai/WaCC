package me.billbai.compiler.kwacc

import me.billbai.compiler.kwacc.CompilerDriver
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val ret = CompilerDriver().main(args)
    println("CompilerDriver finished with return code: $ret")
    exitProcess(ret)
}