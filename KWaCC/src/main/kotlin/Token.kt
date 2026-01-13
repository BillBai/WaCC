package me.billbai.compiler.kwacc

sealed class Token() {

    enum class KeywordType {
        INT,
        VOID,
        RETURN,
    }

    data class Identifier(val value: String): Token()
    data class Constant(val value: String): Token()
    data class Keyword(val keywordType: KeywordType): Token()

    object OpenParen: Token()
    object CloseParen: Token()
    object OpenBrace: Token()
    object CloseBrace: Token()
    object Semicolon: Token()

    object EndOfFile: Token()

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

data class TokenStream(
    val tokens: List<Token>,
    val positions: List<SourceLocationInfo>
) {
    init {
        require(tokens.size == positions.size) {
            "tokens and positions must have same length. token.size ${tokens.size} vs positions.size ${positions.size}"
        }
    }
}