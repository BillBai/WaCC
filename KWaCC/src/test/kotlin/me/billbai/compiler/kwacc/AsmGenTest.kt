package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import java.io.PrintWriter
import java.io.StringWriter

class AsmGenTest {
    @Test
    fun `test asm generation for return constant`() {
        val sourceFileInfo = SourceFileInfo("", "")
        val input = "int main() { return 42; }"
        val lexer = Lexer(sourceFileInfo, input.byteInputStream())
        val tokenizeResult = lexer.tokenize()

        val parser = Parser(tokenizeResult.tokenStream)
        val parseResult = parser.parse()
        assertTrue(parseResult.errors.isEmpty())
        val ast = parseResult.ast!!

        // Generate ASM AST
        val asmGenerator = AsmGenerator()
        val asmAst = ast.accept(asmGenerator)
        assertTrue(asmAst is AsmProgram)

        // Pretty print it
        val asmPrinter = AsmAstPrettyPrinter()
        val asmString = (asmAst as AsmProgram).accept(asmPrinter)
        println("=== ASM AST ===")
        println(asmString)

        // Emit assembly
        val stringWriter = StringWriter()
        PrintWriter(stringWriter).use { writer ->
            val emitter = AsmEmitter(writer)
            asmAst.accept(emitter)
        }
        println("=== EMITTED ASSEMBLY ===")
        println(stringWriter.toString())
    }
}