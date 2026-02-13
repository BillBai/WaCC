package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintWriter
import kotlin.test.assertEquals

class EndToEndTest {

    /**
     * Compiles a C source string through the full pipeline:
     * Lex -> Parse -> TackyGen -> TackyToAsm -> ReplacePseudo -> Fixup -> Emit -> clang -> run
     *
     * Returns the exit code of the compiled program.
     */
    private fun compileAndRun(source: String): Int {
        val sourceFileInfo = SourceFileInfo("", "")

        // Lex
        val lexer = Lexer(sourceFileInfo, source.byteInputStream())
        val tokenizeResult = lexer.tokenize()
        check(!tokenizeResult.hasErrors) { "Lexer errors: ${tokenizeResult.errors}" }

        // Parse
        val parser = Parser(tokenizeResult.tokenStream)
        val parseResult = parser.parse()
        check(parseResult.errors.isEmpty()) { "Parser errors: ${parseResult.errors}" }
        val ast = parseResult.ast!!

        // TackyGen
        val tackyGen = TackyGen()
        val tackyProgram = ast.accept(tackyGen) as TackyProgram

        // TackyToAsm
        val asmAst = TackyToAsm().convert(tackyProgram)

        // ReplacePseudo
        val (asmAfterPseudo, stackSize) = ReplacePseudo().replace(asmAst)

        // FixupInstructions
        val finalAsmAst = FixupInstructions().fixup(asmAfterPseudo, stackSize)

        // Emit to temp .S file
        val asmFile = File.createTempFile("kwacc_test_", ".S")
        val binFile = File.createTempFile("kwacc_test_", "")
        try {
            PrintWriter(asmFile).use { writer ->
                val emitter = AsmEmitter(writer)
                finalAsmAst.accept(emitter)
            }

            // Assemble and link
            val clangProcess = ProcessBuilder("clang", asmFile.absolutePath, "-o", binFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            val clangExit = clangProcess.waitFor()
            check(clangExit == 0) {
                "clang failed: ${clangProcess.inputStream.bufferedReader().readText()}"
            }

            // Run
            val runProcess = ProcessBuilder(binFile.absolutePath)
                .start()
            return runProcess.waitFor()
        } finally {
            asmFile.delete()
            binFile.delete()
        }
    }

    // --- Chapter 1: Return constants ---

    @Test
    fun `return 0`() {
        assertEquals(0, compileAndRun("int main(void) { return 0; }"))
    }

    @Test
    fun `return 42`() {
        assertEquals(42, compileAndRun("int main(void) { return 42; }"))
    }

    // --- Chapter 2: Unary operators ---

    @Test
    fun `negate`() {
        // -(-5) = 5
        assertEquals(5, compileAndRun("int main(void) { return -(-5); }"))
    }

    @Test
    fun `complement`() {
        // ~0 = -1, exit code wraps to 255
        assertEquals(255, compileAndRun("int main(void) { return ~0; }"))
    }

    @Test
    fun `nested unary`() {
        // -(~(-5)) = -(4) = -4, exit code wraps to 252
        assertEquals(252, compileAndRun("int main(void) { return -(~(-5)); }"))
    }

    // --- Chapter 3: Binary operators ---

    @Test
    fun `addition`() {
        assertEquals(7, compileAndRun("int main(void) { return 3 + 4; }"))
    }

    @Test
    fun `subtraction`() {
        assertEquals(3, compileAndRun("int main(void) { return 10 - 7; }"))
    }

    @Test
    fun `multiplication`() {
        assertEquals(42, compileAndRun("int main(void) { return 6 * 7; }"))
    }

    @Test
    fun `division`() {
        assertEquals(5, compileAndRun("int main(void) { return 15 / 3; }"))
    }

    @Test
    fun `remainder`() {
        assertEquals(2, compileAndRun("int main(void) { return 17 % 5; }"))
    }

    @Test
    fun `precedence — multiply before add`() {
        assertEquals(7, compileAndRun("int main(void) { return 1 + 2 * 3; }"))
    }

    @Test
    fun `parentheses override precedence`() {
        assertEquals(9, compileAndRun("int main(void) { return (1 + 2) * 3; }"))
    }

    // --- Chapter 4: Relational operators ---

    @Test
    fun `less than — true`() {
        assertEquals(1, compileAndRun("int main(void) { return 1 < 2; }"))
    }

    @Test
    fun `less than — false`() {
        assertEquals(0, compileAndRun("int main(void) { return 2 < 1; }"))
    }

    @Test
    fun `less than — equal is false`() {
        assertEquals(0, compileAndRun("int main(void) { return 5 < 5; }"))
    }

    @Test
    fun `greater than — true`() {
        assertEquals(1, compileAndRun("int main(void) { return 5 > 3; }"))
    }

    @Test
    fun `greater than — false`() {
        assertEquals(0, compileAndRun("int main(void) { return 3 > 5; }"))
    }

    @Test
    fun `less or equal — equal`() {
        assertEquals(1, compileAndRun("int main(void) { return 5 <= 5; }"))
    }

    @Test
    fun `less or equal — less`() {
        assertEquals(1, compileAndRun("int main(void) { return 3 <= 5; }"))
    }

    @Test
    fun `less or equal — greater is false`() {
        assertEquals(0, compileAndRun("int main(void) { return 10 <= 5; }"))
    }

    @Test
    fun `greater or equal — equal`() {
        assertEquals(1, compileAndRun("int main(void) { return 5 >= 5; }"))
    }

    @Test
    fun `greater or equal — greater`() {
        assertEquals(1, compileAndRun("int main(void) { return 10 >= 5; }"))
    }

    @Test
    fun `greater or equal — less is false`() {
        assertEquals(0, compileAndRun("int main(void) { return 3 >= 5; }"))
    }

    @Test
    fun `equal — true`() {
        assertEquals(1, compileAndRun("int main(void) { return 5 == 5; }"))
    }

    @Test
    fun `equal — false`() {
        assertEquals(0, compileAndRun("int main(void) { return 5 == 6; }"))
    }

    @Test
    fun `not equal — true`() {
        assertEquals(1, compileAndRun("int main(void) { return 5 != 6; }"))
    }

    @Test
    fun `not equal — false`() {
        assertEquals(0, compileAndRun("int main(void) { return 5 != 5; }"))
    }

    // --- Chapter 4: Logical operators ---

    @Test
    fun `logical not — true becomes false`() {
        assertEquals(0, compileAndRun("int main(void) { return !1; }"))
    }

    @Test
    fun `logical not — false becomes true`() {
        assertEquals(1, compileAndRun("int main(void) { return !0; }"))
    }

    @Test
    fun `logical not — nonzero is truthy`() {
        assertEquals(0, compileAndRun("int main(void) { return !42; }"))
    }

    @Test
    fun `logical and — both true`() {
        assertEquals(1, compileAndRun("int main(void) { return 1 && 2; }"))
    }

    @Test
    fun `logical and — left false short-circuits`() {
        assertEquals(0, compileAndRun("int main(void) { return 0 && 1; }"))
    }

    @Test
    fun `logical and — right false`() {
        assertEquals(0, compileAndRun("int main(void) { return 1 && 0; }"))
    }

    @Test
    fun `logical or — both false`() {
        assertEquals(0, compileAndRun("int main(void) { return 0 || 0; }"))
    }

    @Test
    fun `logical or — left true short-circuits`() {
        assertEquals(1, compileAndRun("int main(void) { return 1 || 0; }"))
    }

    @Test
    fun `logical or — right true`() {
        assertEquals(1, compileAndRun("int main(void) { return 0 || 1; }"))
    }

    // --- Chapter 4: Combined expressions ---

    @Test
    fun `relational with logical and`() {
        assertEquals(1, compileAndRun("int main(void) { return (5 > 3) && (10 != 11); }"))
    }

    @Test
    fun `complex expression`() {
        assertEquals(1, compileAndRun(
            "int main(void) { return (1 < 2) && (3 >= 3) && !(5 == 6) || (0 > 1); }"
        ))
    }

    @Test
    fun `arithmetic in relational`() {
        // (2 + 3) > (1 * 4) → 5 > 4 → 1
        assertEquals(1, compileAndRun("int main(void) { return (2 + 3) > (1 * 4); }"))
    }

    @Test
    fun `chained relationals`() {
        // In C: 1 < 2 < 3 is parsed as (1 < 2) < 3 → 1 < 3 → 1
        assertEquals(1, compileAndRun("int main(void) { return 1 < 2 < 3; }"))
    }

    @Test
    fun `not of relational`() {
        // !(1 > 2) → !0 → 1
        assertEquals(1, compileAndRun("int main(void) { return !(1 > 2); }"))
    }
}
