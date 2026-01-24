package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TackyGenTest {
    @Test
    fun `test tacky gen with unary operators`() {
        val sourceFileInfo = SourceFileInfo("", "")
        val input = "int main() { return -(~(-5)); }"

        // Lex
        val lexer = Lexer(sourceFileInfo, input.byteInputStream())
        val tokenizeResult = lexer.tokenize()
        assertTrue(tokenizeResult.tokens.isNotEmpty())

        // Parse
        val parser = Parser(tokenizeResult.tokenStream)
        val parseResult = parser.parse()
        assertTrue(parseResult.errors.isEmpty())
        val ast = parseResult.ast!!

        // TackyGen
        val tackyGen = TackyGen()
        val tackyProgram = ast.accept(tackyGen)
        assertTrue(tackyProgram is TackyProgram)

        // Print it out with the pretty printer
        val printer = TackyPrettyPrinter()
        println(printer.print(tackyProgram as TackyProgram))
    }
}
