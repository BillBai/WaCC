package me.billbai.compiler.kwacc

// the C preprocessor is a helper class that finds the clang or gcc
// compiler and runs it with the -E -P flag to preprocess the input file
class CPreprocessor(
    val compiler: Compiler,
    val extraArgs: List<String> = emptyList(),
    val compilerPathOverride: String? = null,
) {

    enum class Compiler {
        CLANG,
        GCC
    }

    private val compilerPath: String by lazy {
        compilerPathOverride ?: when (compiler) {
            Compiler.CLANG -> "clang"
            Compiler.GCC -> "gcc"
        }
    }

    /**
     * Preprocesses the input file using the specified compiler.
     * The preprocessor will run with the -E and -P flags to output the preprocessed code.
     *
     * @param inputFilePath The path to the input file to preprocess.
     * @param outputFilePath The path where the preprocessed output will be written.
     * @return true if preprocessing was successful, false otherwise.
     */
    fun preprocess(inputFilePath: String, outputFilePath: String): Boolean {
        val command = mutableListOf(compilerPath, "-E", "-P", inputFilePath, "-o", outputFilePath)
        command.addAll(extraArgs)

        println("Running preprocessor command: ${command.joinToString(" ")}")

        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                println("Preprocessing failed with exit code: $exitCode")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            println("Error running preprocessor: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}