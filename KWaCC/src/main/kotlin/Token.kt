package me.billbai.compiler.kwacc

sealed class Token(val tokenType: TokenType) {
    enum class TokenType {
        IDENTIFIER,
        CONSTANT,

        KEYWORD,

        OPEN_PAREN,
        CLOSE_PAREN,
        OPEN_BRACE,
        CLOSE_BRACE,
        SEMICOLON,

        EOF,
    }

    enum class KeywordType {
        INT,
        VOID,
        RETURN,
    }

    data class Identifier(val value: String) : Token(TokenType.IDENTIFIER)
    data class Constant(val value: String) : Token(TokenType.CONSTANT)
    data class Keyword(val keywordType: KeywordType) : Token(TokenType.KEYWORD)

    object OpenParen : Token(TokenType.OPEN_PAREN)
    object CloseParen : Token(TokenType.CLOSE_PAREN)
    object OpenBrace : Token(TokenType.OPEN_BRACE)
    object CloseBrace : Token(TokenType.CLOSE_BRACE)
    object Semicolon : Token(TokenType.SEMICOLON)

    object EndOfFile : Token(TokenType.EOF)

    override fun toString(): String {
        return when (this) {
            is Identifier -> "Identifier($value)"
            is Constant -> "Constant($value)"
            is Keyword -> "Keyword(${keywordType.name})"
            OpenParen -> "OpenParen"
            CloseParen -> "CloseParen"
            OpenBrace -> "OpenBrace"
            CloseBrace -> "CloseBrace"
            Semicolon -> "Semicolon"
            EndOfFile -> "EndOfFile"
        }
    }
}
