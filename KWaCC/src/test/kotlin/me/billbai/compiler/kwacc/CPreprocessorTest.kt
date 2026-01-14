package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class CPreprocessorTest {
    @Test
    fun `preprocess returns false when compiler cannot be started`() {
        // Use a definitely-nonexistent compiler path so the test doesn't depend on
        // clang/gcc being installed on the machine running the tests.
        val preprocessor = CPreprocessor(
            compiler = CPreprocessor.Compiler.CLANG,
            compilerPathOverride = "__definitely_not_a_real_compiler__"
        )

        // Paths don't matter here because process start should fail first.
        val ok = preprocessor.preprocess("input.c", "output.i")
        assertFalse(ok)
    }
}

