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
    fun visitComplementOperator(node: ComplementOperator): T
    fun visitNegateOperator(node: NegateOperator): T
    fun visitUnary(node: UnaryExpression): T
    fun visitBinaryExpression(node: BinaryExpression): T
    fun visitAssignmentExpression(node: AssignmentExpression): T
    fun visitAddOperator(node: AddOperator): T
    fun visitSubOperator(node: SubOperator): T
    fun visitMultiplyOperator(node: MultiplyOperator): T
    fun visitDivideOperator(node: DivideOperator): T
    fun visitRemainderOperator(node: RemainderOperator): T
    fun visitNotOperator(node: NotOperator): T
    fun visitAndOperator(node: AndOperator): T
    fun visitOrOperator(node: OrOperator): T
    fun visitEqualOperator(node: EqualOperator): T
    fun visitNotEqualOperator(node: NotEqualOperator): T
    fun visitLessThanOperator(node: LessOperator): T
    fun visitGreaterThanOperator(node: GreaterOperator): T
    fun visitLessOrEqualThanOperator(node: LessOrEqualOperator): T
    fun visitGreaterOrEqualOperator(node: GreaterOrEqualOperator): T
    fun visitExpressionStmt(node: ExpressionStmt): T
    fun visitNullStmt(node: NullStmt): T
    fun visitDeclaration(node: Declaration): T
    fun visitBlockItemStatement(node: BlockItemStatement): T
    fun visitBlockItemDeclaration(node: BlockItemDeclaration): T
}