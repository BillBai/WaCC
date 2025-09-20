package me.billbai.compiler.kwacc

sealed class ASTNode {
    abstract fun <T> accept(visitor: ASTVisitor<T>): T
}

// Top-level program structure
data class Program(val items: List<TopLevelItem>) : ASTNode() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitProgram(this)
}

// Top-level items (functions, declarations, etc.)
sealed class TopLevelItem : ASTNode()
data class FunctionDefinition(
    val returnType: Type,
    val name: String,
    val parameters: List<Parameter>,
    val body: BlockStmt
) : TopLevelItem() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitFunctionDefinition(this)
}

// Types
sealed class Type : ASTNode()
object IntType : Type() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitIntType(this)
}
object VoidType : Type() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitVoidType(this)
}

// Parameters
data class Parameter(val type: Type, val name: String) : ASTNode() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitParameter(this)
}

// Statements
sealed class Statement : ASTNode()
data class BlockStmt(val statements: List<Statement>) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitBlockStmt(this)
}
data class ReturnStmt(val expression: Expression?) : Statement() {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitReturnStmt(this)
}

// Expressions
sealed class Expression(val type: Type) : ASTNode()
data class IntConstant(val value: String) : Expression(IntType) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitIntConstant(this)
}
data class Identifier(val name: String, val identifierType: Type) : Expression(identifierType) {
    override fun <T> accept(visitor: ASTVisitor<T>): T = visitor.visitIdentifier(this)
}