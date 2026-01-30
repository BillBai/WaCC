package me.billbai.compiler.kwacc

class Parser(
    private var tokenStream: TokenStream
) {
    private var currentTokenIndex: Int = 0

    private val errors = mutableListOf<ParseError>()

    data class ParseError(
        val message: String,
        val token: Token?
    )
    data class ParseResult(
        val ast: Program?,
        val errors: List<ParseError>
    )

    private fun peek(): Token {
        return if (currentTokenIndex < tokenStream.tokens.size) {
            tokenStream.tokens[currentTokenIndex]
        } else {
            Token.EndOfFile;
        }
    }

    private fun advance(): Token {
        val token = peek()
        if (!isAtEnd()) {
            currentTokenIndex += 1
        }
        return token
    }

    private fun previous(): Token? {
        return if (tokenStream.tokens.isNotEmpty() && currentTokenIndex > 0) {
            tokenStream.tokens[currentTokenIndex - 1]
        } else {
            null
        }
    }

    private fun isAtEnd(): Boolean {
        return (currentTokenIndex >= tokenStream.tokens.size) || (peek() is Token.EndOfFile)
    }

    private fun addError(message: String, token: Token? = null) {
        errors.add(ParseError(message, token ?: peek()))
    }

    private fun parseType(): Type? {
        val token = peek()
        if (token !is Token.Keyword) {
            return null
        }
        return when (token.keywordType) {
            Token.KeywordType.INT -> {
                advance()
                IntType
            }
            Token.KeywordType.VOID -> {
                advance()
                VoidType
            }
            else -> {
                null
            }
        }
    }

    private fun parseFunctionDefinition(): FunctionDefinition? {
        // Parse return type
        val type = parseType()
        if (type == null) {
            addError("Expected type")
            return null
        }

        // Parse function name (identifier)
        if (peek() !is Token.Identifier) {
            addError("Expected function name")
            return null
        }
        val nameToken = advance() as Token.Identifier

        // Parse '('
        if (peek() !is Token.OpenParen) {
            addError("Expected '(' after function name")
            return null
        }
        advance()

        // Parse optional 'void'
        val funcParamType = parseType()
        if (funcParamType != null) {
            if (funcParamType != VoidType) {
                addError("Only support 'void' type for function param type")
            }
        }

        // For now, no parameters - just parse ')'
        if (peek() !is Token.CloseParen) {
            addError("Expected ')' - parameters not supported yet")
            return null
        }
        advance()

        // Parse function body (block statement)
        val body = parseBlockStatement()
        if (body == null) {
            addError("Expected function body")
            return null
        }

        return FunctionDefinition(type, nameToken.value, emptyList(), body)
    }

    /*
    private inline fun <reified T: Token> checkNextTokenType(): Boolean {
        if (isAtEnd()) return false
        return peek() is T
    }
    */

    private fun parseBlockStatement(): BlockStmt? {
        if (peek() !is Token.OpenBrace) {
            addError("Expected '{'")
            return null
        }
        advance()

        val statements = mutableListOf<Statement>()

        while ((peek() !is Token.CloseBrace) && !isAtEnd()) {
            val stmt = parseStatement()
            if (stmt != null) {
                statements.add(stmt)
            } else {
                break
            }
        }

        if (peek() !is Token.CloseBrace) {
            addError("Expect '}'")
            return null;
        }
        advance()

        return BlockStmt(statements)
    }

    private fun parseStatement(): Statement? {
        val token = peek()
        if (token is Token.Keyword && token.keywordType == Token.KeywordType.RETURN) {
            return parseReturnStatement()
        }

        addError("Unknown statement type")
        return null
    }

    private fun parseReturnStatement(): ReturnStmt? {
        val token = peek()
        check(token is Token.Keyword && token.keywordType == Token.KeywordType.RETURN) {
            "Must start with 'return' token"
        }

        // Consume 'return'
        advance()

        var expression: Expression? = null

        // Check if there's an expression before ';'
        if (peek() !is Token.Semicolon) {
            expression = parseExpression()
        }

        // Parse ';'
        if (peek() !is Token.Semicolon) {
            addError("Expected ';' after return statement")
            return null
        }
        advance()

        return ReturnStmt(expression)
    }

    private fun parseUnaryOperator(): UnaryOperator? {
        val token = peek()
        if (token == Token.Minus) {
            advance()
            return NegateOperator
        }
        if (token == Token.Tilde) {
            advance()
            return ComplementOperator
        }
        addError("Expecting - or ~ unary operator")
        return null
    }

    private fun parseExpression(): Expression? {
        val token = peek()
        if (token is Token.Constant) {
            advance()
            return IntConstant(token.value)
        }
        if (token is Token.Identifier) {
            advance()
            // Type will be determined during semantic analysis
            return Identifier(token.value, IntType)
        }
        if (token is Token.Minus || token is Token.Tilde) {
            val operator = parseUnaryOperator()
            if (operator == null) {
                addError("Expecting unary operator")
                return null
            }
            val exp = parseExpression()
            if (exp == null) {
                addError("Expecting exp adter unary operator")
                return null
            }
            return UnaryExpression(operator, exp)
        }

        if (token is Token.OpenParen) {
            advance()
            val exp = parseExpression()
            if (exp != null) {
                val nextToken = peek()
                if (nextToken == Token.CloseParen) {
                    advance()
                    return exp
                } else {
                    addError("Expected ')'")
                    return null
                }
            } else {
                addError("Expected expression after '('")
                return null
            }
        }

        addError("Expected expression")
        return null
    }

    private fun parseProgram(): Program? {
        val functionDef = parseFunctionDefinition()
        if (!isAtEnd()) {
            addError("Extra unprocessed token")
            return null
        }
        return if (functionDef != null) {
            Program(listOf(functionDef))
        } else {
            Program(listOf())
        }
    }

    fun parse(): ParseResult {
        errors.clear() // Clear any previous errors
        
        val program = parseProgram()
        return ParseResult(program, errors.toList())
    }
}