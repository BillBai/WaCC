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
    val blockItems: List<BlockItem>,
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

data class Var(
    val name: String,
) : Expression(null) {
    constructor(name: String, type: Type?) : this(name) {
        this.type = type
    }

    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitVar(this)
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

object NotOperator: UnaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitNotOperator(this)
}

data class BinaryExpression(
    val binaryOperator: BinaryOperator,
    val lhs: Expression,
    val rhs: Expression,
) : Expression(null) {
    constructor(
        binaryOperator: BinaryOperator,
        lhs: Expression,
        rhs: Expression,
        type: Type?
    ): this(binaryOperator, lhs, rhs) {
        this.type = type
    }

    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitBinaryExpression(this)
}

data class AssignmentExpression(
    val lhs: Expression,
    val rhs: Expression,
): Expression(null) {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitAssignmentExpression(this)
}

data class ExpressionStmt(
    val expression: Expression
): Statement() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitExpressionStmt(this)
    }
}

object NullStmt: Statement() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitNullStmt(this)
    }

}

data class Declaration(val name: String, val initializer: Expression?): AstNode() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitDeclaration(this)
    }
}

sealed class BlockItem : AstNode() {}

data class BlockItemStatement(
    val statement: Statement
): BlockItem() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitBlockItemStatement(this)
    }
}


data class BlockItemDeclaration(
    val declaration: Declaration
): BlockItem() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitBlockItemDeclaration(this)
    }
}


sealed class BinaryOperator() : AstNode() {}

object AddOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitAddOperator(this)
    }
}

object SubOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitSubOperator(this)
    }
}

object MultiplyOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitMultiplyOperator(this)
    }
}

object DivideOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitDivideOperator(this)
    }
}

object RemainderOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitRemainderOperator(this)
    }
}

object AndOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitAndOperator(this)
}

object OrOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitOrOperator(this)
}

object EqualOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitEqualOperator(this)
}

object NotEqualOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitNotEqualOperator(this)
}

object LessOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T = visitor.visitLessThanOperator(this)
}

object GreaterOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitGreaterThanOperator(this)
    }
}

object LessOrEqualOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitLessOrEqualThanOperator(this)
    }
}

object GreaterOrEqualOperator: BinaryOperator() {
    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visitGreaterOrEqualOperator(this)
    }
}