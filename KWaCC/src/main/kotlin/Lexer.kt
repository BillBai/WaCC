package me.billbai.compiler.kwacc

import java.io.InputStream

class Lexer(private val inputStream: InputStream) {
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
    private var curCharCode: Int = 0
    private var curChar: Char = 0.toChar()

    private fun advance() {
        curCharCode = inputStream.read()
        curChar = curCharCode.toChar()
        if (curCharCode == -1) {
            return
        }
        this.curColumn += 1
        if (curCharCode == '\n'.code) {
            this.curLine += 1
            this.curColumn = 0
        }
    }

    private fun makeIdentOrKeyword(identOrKeyword: String): Token {
        return when (identOrKeyword) {
            "int" -> Token.Keyword(Token.KeywordType.INT)
            "void" -> Token.Keyword(Token.KeywordType.VOID)
            "return" -> Token.Keyword(Token.KeywordType.RETURN)
            else -> Token.Identifier(identOrKeyword)
        }
    }

    private sealed class TokenResult {
        data class Success(val token: Token) : TokenResult()
        data class Error(val error: LexerError) : TokenResult()
    }
    private fun nextToken(): TokenResult {
        try {
            while (curCharCode != -1 && curChar.isWhitespace()) {
                advance()
            }

            if (curCharCode == -1) {
                return TokenResult.Success(Token.EndOfFile)
            }

            if (curChar.isLetter() || curChar == '_') {
                val builder = StringBuilder()
                do {
                    builder.append(curChar)
                    advance()
                } while(curCharCode != -1 && (curChar.isLetterOrDigit() || curChar == '_'))
                val identOrKeyword = builder.toString()
                return TokenResult.Success(makeIdentOrKeyword(identOrKeyword))
            } else if (curChar.isDigit()) {
                val builder = StringBuilder()
                do {
                    builder.append(curChar)
                    advance()
                } while (curCharCode != -1 && curChar.isDigit())

                // Check for invalid number format (number followed by letter)
                if (curCharCode != -1 && curChar.isLetter()) {
                    val invalidBuilder = StringBuilder(builder.toString())
                    do {
                        invalidBuilder.append(curChar)
                        advance()
                    } while (curCharCode != -1 && curChar.isLetterOrDigit())

                    return TokenResult.Error(
                        LexerError(
                            "Invalid number format: '${invalidBuilder}'",
                            curLine,
                            curColumn
                        )
                    )
                }

                return TokenResult.Success(Token.Constant(builder.toString()))
            } else {
                val tokenResult = when (curChar) {
                    '(' ->  TokenResult.Success(Token.OpenParen)
                    ')' ->  TokenResult.Success(Token.CloseParen)
                    '{' ->  TokenResult.Success(Token.OpenBrace)
                    '}' ->  TokenResult.Success(Token.CloseBrace)
                    ';' ->  TokenResult.Success(Token.Semicolon)
                    else -> {
                        TokenResult.Error(
                            LexerError(
                                "Unexpected character '$curChar'",
                                curLine,
                                curColumn,
                                curChar
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
        } while (curCharCode != -1)

        // Ensure we always have an EOF token
        if (tokens.isEmpty() || tokens.last().tokenType != Token.TokenType.EOF) {
            tokens.add(Token.EndOfFile)
        }

        return TokenizeResult(tokens, errors)
    }
}