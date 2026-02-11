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

object AsmRetInst : AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmRetInst(this)
}

sealed class AsmOperand : AsmNode()

data class AsmImmOperand(
    val value: Int
) : AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmImmOperand(this)
}

sealed class AsmReg() : AsmNode() {}

object AsmRegAX: AsmReg() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmRegAX(this)
    }
}

object AsmRegR10: AsmReg() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmRegR10(this)
    }
}

object AsmRegR11: AsmReg() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmRegR11(this)
    }
}

object AsmRegDX: AsmReg() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmRegDX(this)
}


data class AsmRegisterOperand(
    val reg : AsmReg
) : AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmRegisterOperand(this)
}

data class AsmPseudoOperand(
    val identifier: String,
): AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmPseudoOperand(this)
    }
}

data class AsmStackOperand(
    val offset: Int,
): AsmOperand() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmStackOperand(this)
    }
}

sealed class AsmUnaryOperator() : AsmNode() {}

object AsmNotUnaryOperator : AsmUnaryOperator() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmNotUnaryOperator(this)
}

object AsmNegUnaryOperator : AsmUnaryOperator() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmNegUnaryOperator(this)
}

data class AsmUnaryInst(
    val op : AsmUnaryOperator,
    val operand: AsmOperand
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmUnaryInst(this)
}

data class AsmAllocateStackInst(
    val size: Int,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmAllocateStackInst(this)
}

sealed class AsmBinaryOperator(): AsmNode() {}

object AsmSubBinaryOperator: AsmBinaryOperator() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmSubBinaryOperator(this)
    }
}

object AsmAddBinaryOperator: AsmBinaryOperator() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmAddBinaryOperator(this)
}

object AsmMultiplyBinaryOperator: AsmBinaryOperator() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmMultiplyBinaryOperator(this)
}

data class AsmBinaryInst(
    val op: AsmBinaryOperator,
    val src: AsmOperand,
    val dst: AsmOperand,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmBinaryInst(this)
}

data class AsmIdivInst(
    val operand: AsmOperand
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmIdivInst(this)
}

object AsmCdqInst: AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmCdqInst(this)
}

data class AsmCmpInst(
    val operand1: AsmOperand,
    val operand2: AsmOperand,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmCmpInst(this)
    }
}

data class AsmJmpInst(
    val target: String,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmJmpInst(this)
    }
}

enum class AsmCondCode {
    E,
    NE,
    G,
    GE,
    L,
    LE;

    fun formatAsmString(): String {
        return when(this) {
            AsmCondCode.E -> "e"
            AsmCondCode.NE -> "ne"
            AsmCondCode.G -> "g"
            AsmCondCode.GE -> "ge"
            AsmCondCode.L -> "l"
            AsmCondCode.LE -> "le"
        }
    }
}

data class AsmJmpCCInst(
    val condCode: AsmCondCode,
    val target: String,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmJmpCCInst(this)
    }
}

data class AsmSetCCInst(
    val condCode: AsmCondCode,
    val operand: AsmOperand,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmSetCCInst(this)
    }
}

data class AsmLabelInst(
    val identifier: String,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T {
        return visitor.visitAsmLabelInst(this)
    }
}


data class AsmInstList(
    val instList: List<AsmInstruction>,
): AsmInstruction() {
    override fun <T> accept(visitor: AsmAstVisitor<T>): T = visitor.visitAsmInstList(this)
}