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
    fun visitAsmRegDX(node: AsmRegDX): T
    fun visitAsmRegR10(node: AsmRegR10): T
    fun visitAsmRegR11(node: AsmRegR11): T
    fun visitAsmPseudoOperand(node: AsmPseudoOperand): T
    fun visitAsmStackOperand(node: AsmStackOperand): T
    fun visitAsmAddBinaryOperator(node: AsmAddBinaryOperator): T
    fun visitAsmSubBinaryOperator(node: AsmSubBinaryOperator): T
    fun visitAsmMultiplyBinaryOperator(node: AsmMultiplyBinaryOperator): T
    fun visitAsmBinaryInst(node: AsmBinaryInst): T
    fun visitAsmIdivInst(node: AsmIdivInst): T
    fun visitAsmCdqInst(node: AsmCdqInst): T
    fun visitAsmCmpInst(node: AsmCmpInst): T
    fun visitAsmJmpInst(node: AsmJmpInst): T
    fun visitAsmJmpCCInst(node: AsmJmpCCInst): T
    fun visitAsmSetCCInst(node: AsmSetCCInst): T
    fun visitAsmLabelInst(node: AsmLabelInst): T
}