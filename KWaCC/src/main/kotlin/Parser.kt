package me.billbai.compiler.kwacc

class Parser(
    private var tokens: List<Token>
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
        if (currentTokenIndex < tokens.size) {
            return tokens[currentTokenIndex]
        } else {
            return Token.EndOfFile;
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
        return if (tokens.isNotEmpty() && currentTokenIndex > 0) {
            tokens[currentTokenIndex - 1]
        } else {
            null
        }
    }

    private fun isAtEnd(): Boolean {
        return currentTokenIndex >= tokens.size || peek().tokenType == Token.TokenType.EOF
    }

    private fun addError(message: String, token: Token? = null) {
        errors.add(ParseError(message, token ?: peek()))
    }

    private fun parseType(): Type? {
        val token = peek()
        if (token is Token.Keyword) {
            when (token.keywordType) {
                Token.KeywordType.INT -> {
                    advance()
                    return IntType
                }
                Token.KeywordType.VOID -> {
                    advance()
                    return VoidType
                }
                else -> {
                    return null
                }
            }
        }
        return null
    }

    private fun parseFunctionDefinition(): FunctionDefinition? {
        // Parse return type
        val type = parseType()
        if (type == null) {
            addError("Expected type")
            return null
        }

        // Parse function name (identifier)
        if (!checkTokenType(Token.TokenType.IDENTIFIER)) {
            addError("Expected function name")
            return null
        }
        val nameToken = advance() as Token.Identifier

        // Parse '('
        if (!checkTokenType(Token.TokenType.OPEN_PAREN)) {
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
        if (!checkTokenType(Token.TokenType.CLOSE_PAREN)) {
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

    private fun checkTokenType(tokenType: Token.TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().tokenType == tokenType
    }

    private fun parseBlockStatement(): BlockStmt? {
        if (!checkTokenType(Token.TokenType.OPEN_BRACE)) {
            addError("Expected '{'")
            return null
        }
        advance()

        val statements = mutableListOf<Statement>()

        while (!checkTokenType(Token.TokenType.CLOSE_BRACE) && !isAtEnd()) {
            val stmt = parseStatement()
            if (stmt != null) {
                statements.add(stmt)
            } else {
                break
            }
        }

        if (!checkTokenType(Token.TokenType.CLOSE_BRACE)) {
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
        if (!checkTokenType(Token.TokenType.SEMICOLON)) {
            expression = parseExpression()
        }

        // Parse ';'
        if (!checkTokenType(Token.TokenType.SEMICOLON)) {
            addError("Expected ';' after return statement")
            return null
        }
        advance()

        return ReturnStmt(expression)
    }

    private fun parseExpression(): Expression? {
        val token = peek()
        when (token.tokenType) {
            Token.TokenType.CONSTANT -> {
                advance()
                return IntConstant((token as Token.Constant).value)
            }
            Token.TokenType.IDENTIFIER -> {
                advance()
                // Type will be determined during semantic analysis
                return Identifier((token as Token.Identifier).value, IntType)
            }
            else -> {
                addError("Expected expression")
                return null
            }
        }
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