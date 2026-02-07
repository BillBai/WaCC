package me.billbai.compiler.kwacc

sealed class Token {

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
    object Tilde: Token()
    object Minus: Token()
    object Decrement: Token()
    object Plus: Token() // wow plustoken hahaha
    object Asterisk: Token()
    object Slash: Token()
    object Percent: Token()

    object LessThan: Token()
    object GreaterThan: Token()
    object LessOrEqual: Token()
    object GreaterOrEqual: Token()

    object Bang: Token()
    object LogicalAnd: Token()
    object LogicalOr: Token()

    object DoubleEqual: Token()
    object NotEqual: Token()

    object EndOfFile: Token()


    override fun toString(): String {
        return when (this) {
            is Identifier -> "Identifier($value)"
            is Constant -> "Constant($value)"
            is Keyword -> "Keyword(${keywordType.name})"
            is OpenParen -> "OpenParen"
            is CloseParen -> "CloseParen"
            is OpenBrace -> "OpenBrace"
            is CloseBrace -> "CloseBrace"
            is Semicolon -> "Semicolon"
            is EndOfFile -> "EndOfFile"
            is Tilde -> "Tilde"
            is Minus -> "Minus"
            is Decrement -> "Decrement"
            is Plus -> "Plus"
            is Asterisk -> "Asterisk"
            is Slash -> "Slash"
            is Percent -> "Percent"
            is Bang -> "Bang"
            is DoubleEqual -> "DoubleEqual"
            is GreaterOrEqual -> "GreaterOrEqual"
            is GreaterThan -> "GreaterThan"
            is LessOrEqual -> "LessOrEqual"
            is LessThan -> "LessThan"
            is LogicalAnd -> "LogicalAnd"
            is LogicalOr -> "LogicalOr"
            is NotEqual -> "NotEqual"
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