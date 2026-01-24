package me.billbai.compiler.kwacc

sealed class AsmNode {
    abstract fun <T> accept(visitor: AsmAstVisitor<T>): T
}

data class AsmProgram(
    val functionDef: AsmFunctionDef?
) : AsmNode() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmProgram(this)
}

data class AsmFunctionDef(
    val name: String,
    val instList: AsmInstList
) : AsmNode() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmFunctionDef(this)
}

sealed class AsmInstruction : AsmNode()

data class AsmMovInst(
    val src: AsmOperand,
    val dst: AsmOperand,
) : AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmMovInst(this)
}

data class AsmRetInst(
    val operand: AsmOperand?
) : AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmRetInst(this)
}

sealed class AsmOperand : AsmNode()

data class AsmImmOperand(
    val value: Int
) : AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmImmOperand(this)
}

// Only EAX for now, so make this object. Will add more registers later.
object AsmRegisterOperand : AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmRegisterOperand(this)
}

data class AsmNegInst(
    val operand: AsmOperand
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmNegInst(this)
}

data class AsmNotInst(
    val operand: AsmOperand
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmNotInst(this)
}


data class AsmInstList(
    val instList: List<AsmInstruction>,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmInstList(this)
}