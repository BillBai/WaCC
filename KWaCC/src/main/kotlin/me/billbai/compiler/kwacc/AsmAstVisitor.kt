package me.billbai.compiler.kwacc

interface AsmAstVisitor<T> {
    fun visitAsmProgram(node: AsmProgram): T
    fun visitAsmFunctionDef(node: AsmFunctionDef): T
    fun visitAsmMovInst(node: AsmMovInst): T
    fun visitAsmRetInst(node: AsmRetInst): T
    fun visitAsmImmOperand(node: AsmImmOperand): T
    fun visitAsmRegisterOperand(node: AsmRegisterOperand): T
    fun visitAsmInstList(node: AsmInstList): T
    fun visitAsmNotUnaryOperator(node: AsmNotUnaryOperator): T
    fun visitAsmNegUnaryOperator(node: AsmNegUnaryOperator): T
    fun visitAsmUnaryInst(node: AsmUnaryInst): T
    fun visitAsmAllocateStackInst(node: AsmAllocateStackInst): T
    fun visitAsmRegAX(node: AsmRegAX): T
    fun visitAsmRegR10(node: AsmRegR10): T
    fun visitAsmPseudoOperand(node: AsmPseudoOperand): T
    fun visitAsmStackOperand(node: AsmStackOperand): T
}