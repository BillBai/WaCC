package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TackyGenTest {

    /** Helper: source string → resolved AST */
    private fun parseAndResolve(input: String): Program {
        val sourceFileInfo = SourceFileInfo("", "")
        val lexer = Lexer(sourceFileInfo, input.byteInputStream())
        val tokenizeResult = lexer.tokenize()
        assertTrue(tokenizeResult.tokens.isNotEmpty())

        val parser = Parser(tokenizeResult.tokenStream)
        val parseResult = parser.parse()
        assertTrue(parseResult.errors.isEmpty())
        return VariableResolver().resolveProgram(parseResult.ast!!)
    }

    @Test
    fun `test tacky gen with unary operators`() {
        val ast = parseAndResolve("int main() { return -(~(-5)); }")

        val tackyGen = TackyGen()
        val tackyProgram = ast.accept(tackyGen)
        assertTrue(tackyProgram is TackyProgram)

        val printer = TackyPrettyPrinter()
        println(printer.print(tackyProgram as TackyProgram))
    }

    @Test
    fun `test tacky gen with binary operators`() {
        val ast = parseAndResolve("int main() { return 1 + 2 * 3; }")

        val tackyGen = TackyGen()
        val tackyProgram = ast.accept(tackyGen)
        assertTrue(tackyProgram is TackyProgram)

        val printer = TackyPrettyPrinter()
        println(printer.print(tackyProgram as TackyProgram))
    }

    @Test
    fun `test tacky gen with variable declaration and assignment`() {
        val ast = parseAndResolve("int main(void) { int a = 5; int b; b = a + 1; return b; }")

        val tackyGen = TackyGen()
        val tackyProgram = ast.accept(tackyGen)
        assertTrue(tackyProgram is TackyProgram)

        val printer = TackyPrettyPrinter()
        println(printer.print(tackyProgram as TackyProgram))
    }
}
