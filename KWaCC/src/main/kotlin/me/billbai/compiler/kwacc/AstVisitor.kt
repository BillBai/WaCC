package me.billbai.compiler.kwacc

interface AstVisitor<T> {
    fun visitProgram(node: Program): T
    fun visitFunctionDefinition(node: FunctionDefinition): T
    fun visitIntType(node: IntType): T
    fun visitVoidType(node: VoidType): T
    fun visitParameter(node: Parameter): T
    fun visitBlockStmt(node: BlockStmt): T
    fun visitReturnStmt(node: ReturnStmt): T
    fun visitIntConstant(node: IntConstant): T
    fun visitIdentifier(node: Identifier): T
}