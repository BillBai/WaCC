package me.billbai.compiler.kwacc

class SemanticError(message: String): Exception(message)

class VariableResolver(
    val ast: AstNode,
) {
    private val variableMap: MutableMap<String, String> = mutableMapOf()

    private fun makeUnique(name: String): String {
        val varCount = variableMap.size
        return "$name.\$var_res.$varCount"
    }

    fun resolveDeclaration(declaration: Declaration): Declaration {
        val name = declaration.name
        if (variableMap.containsKey(name)) {
            throw SemanticError("Duplicated variable declaration $name")
        }

        val uniqueName = makeUnique(name)
        variableMap[name] = uniqueName

        val resolvedInitializer = if (declaration.initializer != null) {
            resolvedExpression(declaration.initializer)
        } else {
            null
        }
        return Declaration(uniqueName, resolvedInitializer)
    }

    fun resolvedExpression(expression: Expression): Expression {
        TODO()
    }
}