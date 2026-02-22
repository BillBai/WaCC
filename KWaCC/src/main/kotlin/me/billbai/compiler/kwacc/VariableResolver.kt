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

    fun resolveStatement(statement: Statement): Statement {
        return when (statement) {
            is ReturnStmt -> ReturnStmt(resolveExpression(statement.expression))
            is BlockStmt -> {
                TODO()

            }
            is ExpressionStmt -> ExpressionStmt(resolveExpression(statement.expression)!!)
            NullStmt -> statement
        }
    }

}