package me.billbai.compiler.kwacc

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayDeque

class Lexer(inputStream: InputStream) {
    private val reader = BufferedReader(InputStreamReader(inputStream))
    private val peekBuffer = ArrayDeque<Char>()
    data class LexerError(
        val message: String,
        val line: Int,
        val column: Int,
        val character: Char? = null
    )

    data class TokenizeResult(
        val tokens: List<Token>,
        val errors: List<LexerError>
    ) {
        val hasErrors: Boolean get() = errors.isNotEmpty()
        val isSuccessful: Boolean get() = !hasErrors
    }

    private var curColumn: Int = 0
    private var curLine: Int = 1
    private var curChar: Char? = null // null for EOF

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
        return TokenResult.Success(makeIdentOrKeyword(identOrKeyword))
    }

    private fun lexNumbers(): TokenResult {
        check(curChar?.isDigit() == true) { "lexNumbers() called on non-digit: '$curChar'"}
        val beginLine = curLine
        val beginColumn = curColumn
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

        return TokenResult.Success(Token.Constant(builder.toString()))
    }

    private sealed class TokenResult {
        data class Success(val token: Token) : TokenResult()
        data class Error(val error: LexerError) : TokenResult()
    }
    private fun nextToken(): TokenResult {
        try {
            while (curChar?.isWhitespace() == true) {
                advance()
            }

            val ch = curChar ?: return TokenResult.Success(Token.EndOfFile)

            if (ch.isLetter() || ch == '_') {
                return lexIdentifierOrKeyword()
            } else if (ch.isDigit()) {
                return lexNumbers()
            } else {
                val tokenResult = when (ch) {
                    '(' ->  TokenResult.Success(Token.OpenParen)
                    ')' ->  TokenResult.Success(Token.CloseParen)
                    '{' ->  TokenResult.Success(Token.OpenBrace)
                    '}' ->  TokenResult.Success(Token.CloseBrace)
                    ';' ->  TokenResult.Success(Token.Semicolon)
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
        val errors = mutableListOf<LexerError>()

        do {
            when (val result = nextToken()) {
                is TokenResult.Success -> {
                    tokens.add(result.token)
                    if (result.token.tokenType == Token.TokenType.EOF) {
                        break
                    }
                }
                is TokenResult.Error -> {
                    errors.add(result.error)
                    // Continue tokenizing after error to collect more potential errors
                }
            }
        } while (curChar != null)

        // Ensure we always have an EOF token
        if (tokens.isEmpty() || tokens.last().tokenType != Token.TokenType.EOF) {
            tokens.add(Token.EndOfFile)
        }

        return TokenizeResult(tokens, errors)
    }
}