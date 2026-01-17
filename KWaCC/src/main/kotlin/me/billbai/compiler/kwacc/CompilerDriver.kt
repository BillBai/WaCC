package me.billbai.compiler.kwacc

import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter

class CompilerDriver {
    enum class Mode {
        DEFAULT,
        LEX,
        PARSE,
        CODEGEN,
    }

    private var mode: Mode = Mode.DEFAULT
    private var emitAssembly: Boolean = false
    private var inputFile: String? = null


    private fun validateFile(inputFilePath: String): Boolean {
        val file = File(inputFilePath)
        if (!file.exists()) {
            println("Error: Input file '$inputFilePath' does not exist.")
            return false
        }
        return true
    }

    private fun performLexing(inputStream: InputStream): Lexer.TokenizeResult? {
        // TODO: fill in real file path
        val fileInfo = SourceFileInfo("", "")

        val lexer = Lexer(fileInfo, inputStream)
        val result = lexer.tokenize()
        if (result.hasErrors) {
            println("Lexer encountered errors:")
            for (error in result.errors) {
                println("Error at line ${error.line}, " +
                        "column ${error.column}:" +
                        " ${error.message}" +
                        " ${error.character?.let { " (character: '$it')" } ?: ""}")
            }
            return null
        }
        return result
    }

    private fun performParsing(tokenStream: TokenStream): Parser.ParseResult? {
        val parser = Parser(tokenStream)
        val parseResult = parser.parse()
        if (parseResult.errors.isNotEmpty()) {
            println("Parser errors:")
            for (error in parseResult.errors) {
                println("${error.message} -- ${error.token?.toString()}")
            }
            return null
        }
        return parseResult
    }

    fun runLexMode(inputFilePath: String): Int {
        println("Running in Lex mode...")
        if (!validateFile(inputFilePath)) return 1

        File(inputFilePath).inputStream().use { inputStream ->
            val result = performLexing(inputStream) ?: return 1
            println("Tokens generated: ${result.tokenStream.tokens.joinToString(", ")}")
        }
        return 0
    }

    fun runParseMode(inputFilePath: String): Int {
        println("Running in Parse mode...")
        if (!validateFile(inputFilePath)) return 1

        File(inputFilePath).inputStream().use { inputStream ->
            val lexResult = performLexing(inputStream) ?: return 1
            val parseResult = performParsing(lexResult.tokenStream) ?: return 1
            
            val ast = parseResult.ast
            if (ast == null) {
                println("Error: Parser returned null AST")
                return 1
            }
            
            val astPrinter = AstPrettyPrinter()
            println(ast.accept(astPrinter))
        }
        return 0
    }

    fun runCodeGenMode(inputFilePath: String): Int {
        println("Running in Code Generation mode...")
        if (!validateFile(inputFilePath)) return 1

        File(inputFilePath).inputStream().use { inputStream ->
            val lexResult = performLexing(inputStream) ?: return 1
            val parseResult = performParsing(lexResult.tokenStream) ?: return 1

            val ast = parseResult.ast
            if (ast == null) {
                println("Error: Parser returned null AST")
                return 1
            }

            val asmGen = AsmGenerator()
            val asmAst = ast.accept(asmGen)
            val asmPrinter = AsmAstPrettyPrinter()
            val asmAstResult = asmAst.accept(asmPrinter);
            println("=== ASM AST:")
            println(asmAstResult)
            println("\n")

            val stringWriter = StringWriter()
            PrintWriter(stringWriter).use { it ->
                val asmEmitter = AsmEmitter(it)
                asmAst.accept(asmEmitter)
                println("=== ASM:")
                println(stringWriter.toString())
            }

        }
        return 0
    }

    fun runDefaultMode(
        inputFilePath: String,
        outputFilePath: String,
        assemblyOutputFilePath: String
    ): Int {
        println("Running in Default mode...")
        if (!validateFile(inputFilePath)) return 1

        return 0
    }

    private fun parseArguments(args: Array<String>) {
        // Parse command line arguments to set mode and input file
        for (arg in args) {
            when (arg) {
                "--lex" -> mode = Mode.LEX
                "--parse" -> mode = Mode.PARSE
                "--codegen" -> mode = Mode.CODEGEN
                "-S" -> emitAssembly = true
                else -> inputFile = arg // Assume any other argument is an input file
            }
        }
    }

    fun defaultPreprocessedFilePath(inputFilePath: String): String {
        return "$inputFilePath.i"
    }

    fun defaultExecutableOutputFilePath(inputFilePath: String): String {
        val lastDotIndex = inputFilePath.lastIndexOf('.')
        if (lastDotIndex != -1 && lastDotIndex > 0)  {
            return inputFilePath.substring(0, lastDotIndex)
        }
        return inputFilePath
    }

    fun defaultAssembleOutputFilePath(inputFilePath: String): String {
        val lastDotIndex = inputFilePath.lastIndexOf('.')
        val baseName = if (lastDotIndex != -1 && lastDotIndex > 0) {
            inputFilePath.substring(0, lastDotIndex)
        } else {
            inputFilePath
        }
        return "${inputFilePath}.S"
    }

    fun preprocessInputFile(inputFile: String): String? {
        val output = defaultPreprocessedFilePath(inputFile)
        val preprocessor = CPreprocessor(CPreprocessor.Compiler.CLANG)
        if (preprocessor.preprocess(inputFile, output)) {
            println("Preprocessing completed successfully. Output file: $output")
            return output
        } else {
            println("Preprocessing failed.")
            return null
        }
    }

    fun main(args: Array<String>): Int {
        println("CompilerDriver started with arguments: ${args.joinToString(", ")}")

        // Parse command line arguments
        parseArguments(args)

        // Check if the input file is valid
        if (inputFile == null) {
            println("Error: No input file specified.")
            return 1
        }

        val file = File(inputFile!!)
        if (!file.exists()) {
            println("Error: Input file '$inputFile' does not exist.")
            return 1
        }

        println("Input file: $inputFile")
        println("Mode: $mode")
        println("Emit Assembly: $emitAssembly")

        // Preprocess the input file
        val preprocessedFilePath = preprocessInputFile(inputFile!!)
        if (preprocessedFilePath == null) {
            println("Error: Preprocessing failed for input file '$inputFile'.")
            return 1
        }

        // Run the appropriate mode based on the arguments
        val retCode = when (mode) {
            Mode.LEX -> runLexMode(preprocessedFilePath)
            Mode.PARSE -> runParseMode(preprocessedFilePath)
            Mode.CODEGEN -> runCodeGenMode(preprocessedFilePath)
            else -> {
                runDefaultMode(preprocessedFilePath,
                    defaultExecutableOutputFilePath(inputFile!!),
                    defaultAssembleOutputFilePath(inputFile!!))
            }
        }

        // delete the preprocessed file if it exists
        val preprocessedFile = File(preprocessedFilePath)
        if (preprocessedFile.exists()) {
            preprocessedFile.delete()
            println("Deleted preprocessed file: $preprocessedFilePath")
        }

        return retCode
    }
}