package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import javax.xml.transform.Source
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LexerTest {

    private fun tokenize(input: String): Lexer.TokenizeResult {
        val sourceFileInfo = SourceFileInfo("<inline-test-case>", "")
        return Lexer(sourceFileInfo, input.byteInputStream()).tokenize()
    }

    @Nested
    inner class NumberTests {
        @Test
        fun `valid numbers separated by whitespace`() {
            val result = tokenize("123 456")
            assertTrue(result.isSuccessful)
            val tokens = result.tokenStream.tokens
            assertEquals(3, tokens.size)
            assertEquals(Token.Constant("123"), tokens[0])
            assertEquals(Token.Constant("456"), tokens[1])
            assertEquals(Token.EndOfFile, tokens[2])
        }

        @Test
        fun `number followed by semicolon is valid`() {
            val result = tokenize("123;")
            val tokens = result.tokenStream.tokens
            assertTrue(result.isSuccessful)
            assertEquals(3, tokens.size)
            assertEquals(Token.Constant("123"), tokens[0])
            assertEquals(Token.Semicolon, tokens[1])
        }

        @Test
        fun `number followed by parenthesis is valid`() {
            val result = tokenize("123)")
            val tokens = result.tokenStream.tokens
            assertTrue(result.isSuccessful)
            assertEquals(Token.Constant("123"), result.tokens[0])
            assertEquals(Token.CloseParen, result.tokens[1])
        }

        @Test
        fun `number followed by single letter is invalid`() {
            val result = tokenize("123a")
            assertTrue(result.hasErrors)
            assertEquals(1, result.errors.size)
            assertEquals("Invalid number format: '123a'", result.errors[0].message)
        }

        @Test
        fun `number followed by multiple letters is invalid`() {
            val result = tokenize("123abc")
            assertTrue(result.hasErrors)
            assertEquals("Invalid number format: '123abc'", result.errors[0].message)
        }

        @Test
        fun `number followed by underscore is invalid`() {
            val result = tokenize("123_foo")
            assertTrue(result.hasErrors)
            assertEquals("Invalid number format: '123_foo'", result.errors[0].message)
        }

        @Test
        fun `multiple invalid numbers in sequence`() {
            val result = tokenize("123a 456bc 789d")
            assertEquals(3, result.errors.size)
            assertEquals("Invalid number format: '123a'", result.errors[0].message)
            assertEquals("Invalid number format: '456bc'", result.errors[1].message)
            assertEquals("Invalid number format: '789d'", result.errors[2].message)
        }

        @Test
        fun `invalid number followed by semicolon preserves semicolon`() {
            val result = tokenize("123abc;")
            assertEquals(1, result.errors.size)
            assertEquals("Invalid number format: '123abc'", result.errors[0].message)
            // Semicolon should still be tokenized
            assertTrue(result.tokens.contains(Token.Semicolon))
        }
    }

    @Nested
    inner class KeywordTests {
        @Test
        fun `recognizes int keyword`() {
            val result = tokenize("int")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Keyword(Token.KeywordType.INT), result.tokens[0])
        }

        @Test
        fun `recognizes void keyword`() {
            val result = tokenize("void")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Keyword(Token.KeywordType.VOID), result.tokens[0])
        }

        @Test
        fun `recognizes return keyword`() {
            val result = tokenize("return")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Keyword(Token.KeywordType.RETURN), result.tokens[0])
        }

        @Test
        fun `keyword prefix is identifier`() {
            val result = tokenize("integer")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("integer"), result.tokens[0])
        }

        @Test
        fun `keyword with suffix is identifier`() {
            val result = tokenize("return_value")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("return_value"), result.tokens[0])
        }
    }

    @Nested
    inner class IdentifierTests {
        @Test
        fun `simple identifier`() {
            val result = tokenize("foo")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("foo"), result.tokens[0])
        }

        @Test
        fun `identifier starting with underscore`() {
            val result = tokenize("_private")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("_private"), result.tokens[0])
        }

        @Test
        fun `identifier with numbers`() {
            val result = tokenize("var123")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("var123"), result.tokens[0])
        }

        @Test
        fun `identifier with underscores`() {
            val result = tokenize("my_var_name")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Identifier("my_var_name"), result.tokens[0])
        }
    }

    @Nested
    inner class PunctuationTests {
        @Test
        fun `all punctuation tokens`() {
            val result = tokenize("(){};")
            assertTrue(result.isSuccessful)
            assertEquals(Token.OpenParen, result.tokens[0])
            assertEquals(Token.CloseParen, result.tokens[1])
            assertEquals(Token.OpenBrace, result.tokens[2])
            assertEquals(Token.CloseBrace, result.tokens[3])
            assertEquals(Token.Semicolon, result.tokens[4])
        }
    }

    @Nested
    inner class ErrorTests {
        @Test
        fun `unexpected character reports error`() {
            val result = tokenize("123 $ 456")
            assertEquals(1, result.errors.size)
            assertEquals("Unexpected character '\$'", result.errors[0].message)
        }

        @Test
        fun `continues after error`() {
            val result = tokenize("123 $ 456")
            // Should still get the valid tokens
            assertTrue(result.tokens.any { it == Token.Constant("123") })
            assertTrue(result.tokens.any { it == Token.Constant("456") })
        }
    }

    @Nested
    inner class FullProgramTests {
        @Test
        fun `minimal main function`() {
            val result = tokenize("int main() { return 0; }")
            assertTrue(result.isSuccessful)

            val expected = listOf(
                Token.Keyword(Token.KeywordType.INT),
                Token.Identifier("main"),
                Token.OpenParen,
                Token.CloseParen,
                Token.OpenBrace,
                Token.Keyword(Token.KeywordType.RETURN),
                Token.Constant("0"),
                Token.Semicolon,
                Token.CloseBrace,
                Token.EndOfFile
            )
            assertEquals(expected, result.tokens)
        }

        @Test
        fun `void function`() {
            val result = tokenize("void foo() {}")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Keyword(Token.KeywordType.VOID), result.tokens[0])
            assertEquals(Token.Identifier("foo"), result.tokens[1])
        }
    }

    @Nested
    inner class EdgeCaseTests {
        @Test
        fun `empty input`() {
            val result = tokenize("")
            assertTrue(result.isSuccessful)
            assertEquals(1, result.tokens.size)
            assertEquals(Token.EndOfFile, result.tokens[0])
        }

        @Test
        fun `whitespace only`() {
            val result = tokenize("   \n\t  ")
            assertTrue(result.isSuccessful)
            assertEquals(1, result.tokens.size)
            assertEquals(Token.EndOfFile, result.tokens[0])
        }

        @Test
        fun `newlines between tokens`() {
            val result = tokenize("int\nmain")
            assertTrue(result.isSuccessful)
            assertEquals(Token.Keyword(Token.KeywordType.INT), result.tokens[0])
            assertEquals(Token.Identifier("main"), result.tokens[1])
        }
    }
}