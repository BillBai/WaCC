package me.billbai.compiler.kwacc

class SemanticError(message: String): Exception(message)

class VariableResolver(
    val ast: AstNode,
) {
    private val variableMap: MutableMap<String, String> = mutableMapOf()

    private fun makeUnique(name: String): String {
        return UniqueNameGenerator.genUniqueName(name)
    }

    fun resolveDeclaration(declaration: Declaration): Declaration {
        val name = declaration.name
        if (variableMap.containsKey(name)) {
            throw SemanticError("Duplicated variable declaration $name")
        }

        val uniqueName = makeUnique(name)
        variableMap[name] = uniqueName

        val resolvedInitializer = if (declaration.initializer != null) {
            resolveExpression(declaration.initializer)
        } else {
            null
        }
        return Declaration(uniqueName, resolvedInitializer)
    }

    fun resolveExpression(expression: Expression?): Expression? {
        return when(expression) {
            is UnaryExpression -> {
                UnaryExpression(expression.unaryOperator,
                    resolveExpression(expression.expression)!!)
            }
            is AssignmentExpression -> {
                if (expression.lhs !is Var) {
                    throw SemanticError("Left hand side of an Assignment is not a Var")
                } else {
                    AssignmentExpression(
                        resolveExpression(expression.lhs)!!,
                        resolveExpression(expression.rhs)!!)
                }
            }
            is BinaryExpression -> {
                BinaryExpression(expression.binaryOperator,
                    resolveExpression(expression.lhs)!!,
                    resolveExpression(expression.rhs)!!)
            }
            is IntConstant -> expression
            is Var -> {
                if (variableMap.containsKey(expression.name)) {
                    Var(variableMap[expression.name]!!)
                } else {
                    throw SemanticError("Undefined variable ${expression.name}")
                }
            }
            null -> null
        }
    }

    fun resolveBlockStmt(blockStmt: BlockStmt): BlockStmt {
        val resolvedBlockItems = mutableListOf<BlockItem>()
        for (blockItem in blockStmt.blockItems) {
            when (blockItem) {
                is BlockItemStatement -> {
                    val resolved = resolveStatement(blockItem.statement)
                    resolvedBlockItems.add(BlockItemStatement(resolved))
                }
                is BlockItemDeclaration -> {
                    val resolved = resolveDeclaration(blockItem.declaration)
                    resolvedBlockItems.add(BlockItemDeclaration(resolved))
                }
            }
        }
        return BlockStmt(resolvedBlockItems)
    }

    fun resolveStatement(statement: Statement): Statement {
        return when (statement) {
            is ReturnStmt -> ReturnStmt(resolveExpression(statement.expression))
            is BlockStmt -> resolveBlockStmt(statement)
            is ExpressionStmt -> ExpressionStmt(resolveExpression(statement.expression)!!)
            NullStmt -> statement
        }
    }

    fun resolveProgram(program: Program): Program {
        val resolvedTopLevelItems = mutableListOf<TopLevelItem>()
        for (topLevelItem in program.items) {
            when (topLevelItem) {
                is FunctionDefinition -> {
                    resolvedTopLevelItems.add(resolveFunctionDef(topLevelItem))
                }
            }
        }

        return Program(resolvedTopLevelItems)
    }

    fun resolveFunctionDef(functionDef: FunctionDefinition): FunctionDefinition {
        val resolvedBody = resolveBlockStmt(functionDef.body)
        return functionDef.copy(
            returnType = functionDef.returnType,
            name = functionDef.name,
            parameters = functionDef.parameters,
            body = resolvedBody
        )
    }

}