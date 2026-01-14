package me.billbai.compiler.kwacc

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayDeque

class Lexer(sourceFileInfo: SourceFileInfo, inputStream: InputStream) {
    private val sourceFileInfo = sourceFileInfo
    private val reader = BufferedReader(InputStreamReader(inputStream))
    private val peekBuffer = ArrayDeque<Char>()
    data class LexerError(
        val message: String,
        val line: Int,
        val column: Int,
        val character: Char? = null
    )

    data class TokenizeResult(
        val tokenStream: TokenStream,
        val errors: List<LexerError>
    ) {
        val tokens: List<Token> get() = tokenStream.tokens
        val hasErrors: Boolean get() = errors.isNotEmpty()
        val isSuccessful: Boolean get() = !hasErrors
    }

    private var curColumn: Int = 0
    private var curLine: Int = 1
    private var curChar: Char? = null // null for EOF

    private fun makeCurrentSourceLocInfo(): SourceLocationInfo {
        return SourceLocationInfo(sourceFileInfo, curLine, curColumn)
    }

    private fun advance() {
        if (peekBuffer.isNotEmpty()) {
            val ch = peekBuffer.removeFirst()
            curChar = ch
        } else {
            val code = reader.read()
            if (code == -1) {
                curChar = null
                return
            }
            curChar = code.toChar()
        }

        this.curColumn += 1
        if (curChar == '\n') {
            this.curLine += 1
            this.curColumn = 0
        }
    }

    private fun peek(): Char? {
        if (peekBuffer.isEmpty()) {
            val nextCharCode = reader.read()
            if (nextCharCode == -1) {
                return null
            }
            peekBuffer.addLast(nextCharCode.toChar())
        }
        return peekBuffer.first()
    }

    private fun peekN(count: UInt): List<Char> {
        val result = mutableListOf<Char>()

        while (peekBuffer.size < count.toInt()) {
            val nextCharCode = reader.read()
            if (nextCharCode == -1) {
                break
            }
            peekBuffer.addLast(nextCharCode.toChar())
        }

        for (i in 0 until count.toInt()) {
            if (i < peekBuffer.size) {
                result.add(peekBuffer.elementAt(i))
            } else {
                break
            }
        }
        return result
    }

    private fun makeIdentOrKeyword(identOrKeyword: String): Token {
        return when (identOrKeyword) {
            "int" -> Token.Keyword(Token.KeywordType.INT)
            "void" -> Token.Keyword(Token.KeywordType.VOID)
            "return" -> Token.Keyword(Token.KeywordType.RETURN)
            else -> Token.Identifier(identOrKeyword)
        }
    }

    private fun lexIdentifierOrKeyword(): TokenResult {
        check(((curChar?.isLetter() == true || curChar == '_'))) {
            "lexIdentifierOrKeyword called on invalid input: '$curChar'"
        }

        val builder = StringBuilder()
        do {
            builder.append(curChar)
            advance()
        } while((curChar?.isLetterOrDigit() == true) || curChar == '_')
        val identOrKeyword = builder.toString()
        val curLoc = makeCurrentSourceLocInfo()
        return TokenResult.Success(makeIdentOrKeyword(identOrKeyword),
            curLoc)
    }

    private fun lexNumbers(): TokenResult {
        check(curChar?.isDigit() == true) { "lexNumbers() called on non-digit: '$curChar'"}
        val beginLine = curLine
        val beginColumn = curColumn
        val curLoc = makeCurrentSourceLocInfo()
        val builder = StringBuilder()
        do {
            builder.append(curChar)
            advance()
        } while (curChar?.isDigit() == true)

        // Check for invalid number format (number followed by letter)
        if (curChar?.isLetter() == true || curChar == '_') {
            val invalidNumBuilder = StringBuilder(builder.toString())
            do {
                invalidNumBuilder.append(curChar)
                advance()
            } while (curChar?.isLetterOrDigit() == true || curChar == '_')

            return TokenResult.Error(
                LexerError(
                    "Invalid number format: '$invalidNumBuilder'",
                    beginLine, beginColumn
                )
            )
        }

        return TokenResult.Success(Token.Constant(builder.toString()), curLoc)
    }

    private sealed class TokenResult{
        data class Success(val token: Token, val locationInfo: SourceLocationInfo) : TokenResult()
        data class Error(val error: LexerError) : TokenResult()
    }
    private fun nextToken(): TokenResult {
        try {
            while (curChar?.isWhitespace() == true) {
                advance()
            }

            val curLoc = makeCurrentSourceLocInfo()
            val ch = curChar ?: return TokenResult.Success(Token.EndOfFile, curLoc)

            if (ch.isLetter() || ch == '_') {
                return lexIdentifierOrKeyword()
            } else if (ch.isDigit()) {
                return lexNumbers()
            } else {
                val tokenResult = when (ch) {
                    '(' ->  TokenResult.Success(Token.OpenParen, curLoc)
                    ')' ->  TokenResult.Success(Token.CloseParen, curLoc)
                    '{' ->  TokenResult.Success(Token.OpenBrace, curLoc)
                    '}' ->  TokenResult.Success(Token.CloseBrace, curLoc)
                    ';' ->  TokenResult.Success(Token.Semicolon, curLoc)
                    else -> {
                        TokenResult.Error(
                            LexerError(
                                "Unexpected character '$ch'",
                                curLine,
                                curColumn,
                                ch
                            )
                        )
                    }
                }
                advance()
                return tokenResult
            }
         } catch (e: Exception) {
            return TokenResult.Error(
                LexerError(
                    "IO error while reading input: ${e.message}",
                    curLine,
                    curColumn
                )
            )
        }
    }

    fun tokenize(): TokenizeResult {
        advance()
        val tokens = mutableListOf<Token>()
        val positions = mutableListOf<SourceLocationInfo>()
        val errors = mutableListOf<LexerError>()

        // On lexing errors, the loop records the error and continues to find more tokens.
        // At EOF, nextToken() returns Success(EndOfFile) and we break out of the loop.
        // The code below is purely defensive.
        do {
            when (val result = nextToken()) {
                is TokenResult.Success -> {
                    tokens.add(result.token)
                    positions.add(result.locationInfo)
                    if (result.token is Token.EndOfFile) {
                        break
                    }
                }
                is TokenResult.Error -> {
                    errors.add(result.error)
                    // Continue tokenizing after error to collect more potential errors
                }
            }
        } while (true)

        // Ensure we always have an EOF token
        if (tokens.isEmpty() || (tokens.last() !is Token.EndOfFile)) {
            tokens.add(Token.EndOfFile)
            positions.add(makeCurrentSourceLocInfo())
        }

        return TokenizeResult(TokenStream(tokens, positions), errors)
    }
}