package me.billbai.compiler.kwacc

class ASTPrettyPrinter : ASTVisitor<String> {
    override fun visitProgram(node: Program): String {
        val result = StringBuilder()
        for (i in node.items.indices) {
            result.append(node.items[i].accept(this))
            if (i < node.items.size - 1) {
                result.append("\n\n")
            }
        }
        return result.toString()
    }

    override fun visitFunctionDefinition(node: FunctionDefinition): String {
        val params = node.parameters.joinToString(", ") { it.accept(this) }
        return "${node.returnType.accept(this)} ${node.name}($params) ${node.body.accept(this)}"
    }

    override fun visitIntType(node: IntType): String = "int"

    override fun visitVoidType(node: VoidType): String = "void"

    override fun visitParameter(node: Parameter): String = 
        "${node.type.accept(this)} ${node.name}"

    override fun visitBlockStmt(node: BlockStmt): String {
        if (node.statements.isEmpty()) {
            return "{}"
        }
        val statements = node.statements.joinToString("\n") { "    ${it.accept(this)}" }
        return "{\n$statements\n}"
    }

    override fun visitReturnStmt(node: ReturnStmt): String =
        "return${node.expression?.let { " ${it.accept(this)}" } ?: ""};"

    override fun visitIntConstant(node: IntConstant): String = node.value

    override fun visitIdentifier(node: Identifier): String = node.name
}