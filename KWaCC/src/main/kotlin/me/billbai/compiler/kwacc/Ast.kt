package me.billbai.compiler.kwacc

sealed class AstNode {
    abstract fun <T> accept(visitor: AstVisitor<T>): T
}

// Top-level program structure
data class Program(
    val items: List<TopLevelItem>,
) : AstNode() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitProgram(this)
}

// Top-level items (functions, declarations, etc.)
sealed class TopLevelItem : AstNode()

data class FunctionDefinition(
    val returnType: Type,
    val name: String,
    val parameters: List<Parameter>,
    val body: BlockStmt,
) : TopLevelItem() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitFunctionDefinition(this)
}

// Types
sealed class Type : AstNode()

object IntType : Type() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitIntType(this)
}

object VoidType : Type() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitVoidType(this)
}

// Parameters
data class Parameter(
    val type: Type,
    val name: String,
) : AstNode() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitParameter(this)
}

// Statements
sealed class Statement : AstNode()

data class BlockStmt(
    val statements: List<Statement>,
) : Statement() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitBlockStmt(this)
}

data class ReturnStmt(
    val expression: Expression?,
) : Statement() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitReturnStmt(this)
}

// Expressions
sealed class Expression(
    // At parsing stage, the type might not be available
    // So here we chose a Mutable AST Approach.
    // The type will be filled during semantic stage.
    var type: Type?,
) : AstNode()

data class IntConstant(
    val value: String,
) : Expression(IntType) {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitIntConstant(this)
}

data class Identifier(
    val name: String,
) : Expression(null) {
    constructor(name: String, type: Type?) : this(name) {
        this.type = type
    }

    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitIdentifier(this)
}

data class UnaryExpression(
    val unaryOperator: UnaryOperator,
    val expression: Expression,
) : Expression(null) {
    constructor(
        unaryOperator: UnaryOperator,
        expression: Expression,
        type: Type?
    ): this(unaryOperator, expression) {
        this.type = type
    }

    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitUnary(this)
}

sealed class UnaryOperator() : AstNode() {}

object ComplementOperator : UnaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitComplementOperator(this)
}

object NegateOperator : UnaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitNegateOperator(this)
}
