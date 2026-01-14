package me.billbai.compiler.kwacc
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun `test parse basic program`() {
        val sourceFileInfo = SourceFileInfo("", "")
        val input = "int main() { return 42; }"
        val lexer = Lexer(sourceFileInfo, input.byteInputStream())
        val tokenizeResult = lexer.tokenize()
        assertTrue(tokenizeResult.tokens.isNotEmpty())

        val parser = Parser(tokenizeResult.tokenStream)
        val parseResult = parser.parse()
        assertTrue(parseResult.errors.isEmpty())
        val ast = parseResult.ast
        assertTrue(ast != null)

        val astPrinter = ASTPrettyPrinter()
        val astString = ast.accept(astPrinter)
        println(astString)
    }


}