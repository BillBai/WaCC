package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `test valid number`() {
        val input = "123 456"
        val lexer = Lexer(input.byteInputStream())
        val result = lexer.tokenize()
        assertTrue(result.errors.isEmpty())
        assertEquals(3, result.tokens.size)
        assertEquals(Token.Constant("123"), result.tokens[0])
        assertEquals(Token.Constant("456"), result.tokens[1])
        assertEquals(Token.EndOfFile, result.tokens[2])
    }

    @Test
    fun `test unexpected character`() {
        val input = "123 $ 456"
        val lexer = Lexer(input.byteInputStream())
        val result = lexer.tokenize()
        assertEquals(1, result.errors.size)
        assertEquals("Unexpected character '$'", result.errors[0].message)
    }
}