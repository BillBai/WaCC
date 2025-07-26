package me.billbai.compiler.kwacc

import java.io.InputStream

class Lexer(private val inputStream: InputStream) {
    private var curColumn: Int = 0
    private var curLine: Int = 1
    private var curCharCode: Int = 0
    private var curChar: Char = 0.toChar()

    private fun readChar() {
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

    private fun nextToken(): Token? {
        try {
            while (curCharCode != -1 && curChar.isWhitespace()) {
                readChar()
            }

            if (curCharCode == -1) {
                return Token.EndOfFile
            }

            if (curChar.isLetter() || curChar == '_') {
                val builder = StringBuilder()
                do {
                    builder.append(curChar)
                    readChar()
                } while(curCharCode != -1 && (curChar.isLetterOrDigit() || curChar == '_'))
                val identOrKeyword = builder.toString()
                return makeIdentOrKeyword(identOrKeyword)
            } else if (curChar.isDigit()) {
                val builder = StringBuilder()
                do {
                    builder.append(curChar)
                    readChar()
                } while (curCharCode != -1 && (curChar.isDigit()))
                return Token.Constant(builder.toString())
            } else {
                val token = when (curChar) {
                    '(' ->  Token.OpenParen
                    ')' ->  Token.CloseParen
                    '{' ->  Token.OpenBrace
                    '}' ->  Token.CloseBrace
                    ';' ->  Token.Semicolon
                    else -> {
                        println("Error: Invalid char $curChar ($curCharCode) for lexer")
                        null
                    }
                }
                readChar()
                return token
            }
         } catch (e: Exception) {
            println("Error reading input stream: ${e.message}")
            return null
        }
    }

    fun tokenize(): List<Token> {
        readChar()
        val tokens = mutableListOf<Token>()
        do {
            val token = nextToken()
            if (token == null) {
                println("Invalid token at position ${inputStream.available()}")
                break
            }
            tokens.add(token)
            if (token.tokenType == Token.TokenType.EOF) {
                break
            }
        } while (true)
        return tokens
    }

}