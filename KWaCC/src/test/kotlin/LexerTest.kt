package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LexerTest {
    @Test
    fun `test invalid number`() {
        val input = "123ab c"
        val lexer = Lexer(input.byteInputStream())
        val result = lexer.tokenize()
        assertEquals(1, result.errors.size)
        assertEquals("Invalid number format: '123ab'", result.errors[0].message)
    }
}