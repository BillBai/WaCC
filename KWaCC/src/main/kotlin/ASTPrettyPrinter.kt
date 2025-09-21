package me.billbai.compiler.kwacc

class ASTPrettyPrinter : ASTVisitor<String> {
    private var indentLevel: Int = 0

    private fun getIndent(): String {
        return "  ".repeat(indentLevel)
    }
    private fun withIndent(block: (indent: String) -> String): String {
        indentLevel += 1
        val indent = getIndent()
        val result = block(indent)
        indentLevel -= 1
        return result
    }

    override fun visitProgram(node: Program): String {
        val result = StringBuilder()
        result.append("Program(\n")
        result.append(withIndent { indent ->
            node.items.joinToString(",\n") { "${indent}${it.accept(this)}" }
        })
        result.append("\n)")
        return result.toString()
    }

    override fun visitFunctionDefinition(node: FunctionDefinition): String {
        val result = StringBuilder()
        result.append("FunctionDefinition(\n")
        result.append(withIndent { indent ->
            val parts = mutableListOf<String>()
            parts.add("${indent}returnType=${node.returnType.accept(this)}")
            parts.add("${indent}name=\"${node.name}\"")
            parts.add("${indent}parameters=[${node.parameters.joinToString(", ") { it.accept(this) }}]")
            parts.add("${indent}body=${node.body.accept(this)}")
            parts.joinToString(",\n")
        })
        result.append("${getIndent()})")
        return result.toString()
    }

    override fun visitIntType(node: IntType): String = "IntType"

    override fun visitVoidType(node: VoidType): String = "VoidType"

    override fun visitParameter(node: Parameter): String = 
        "Parameter(type=${node.type.accept(this)}, name=\"${node.name}\")"

    override fun visitBlockStmt(node: BlockStmt): String {
        if (node.statements.isEmpty()) {
            return "BlockStmt(statements=[])"
        }
        val result = StringBuilder()
        result.append("BlockStmt(\n")
        result.append(withIndent { indent ->
            "${indent}statements=[\n" +
                    withIndent { indent ->
                        node.statements.joinToString(",\n") { "${indent}${it.accept(this)}" }
                    } +
                    "\n${indent}]"
        })
        result.append("\n${getIndent()})\n")
        return result.toString()
    }

    override fun visitReturnStmt(node: ReturnStmt): String {
        return if (node.expression != null) {
            "ReturnStmt(expression=${node.expression.accept(this)})"
        } else {
            "ReturnStmt(expression=null)"
        }
    }

    override fun visitIntConstant(node: IntConstant): String = 
        "IntConstant(value=\"${node.value}\")"

    override fun visitIdentifier(node: Identifier): String = 
        "Identifier(name=\"${node.name}\", type=${node.type.accept(this)})"
}