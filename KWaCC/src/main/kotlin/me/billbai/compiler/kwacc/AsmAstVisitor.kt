package me.billbai.compiler.kwacc

interface AsmAstVisitor<T> {
    fun visitAsmProgram(node: AsmProgram): T
    fun visitAsmFunctionDef(node: AsmFunctionDef): T
    fun visitAsmMovInst(node: AsmMovInst): T
    fun visitAsmRetInst(node: AsmRetInst): T
    fun visitAsmImmOperand(node: AsmImmOperand): T
    fun visitAsmRegisterOperand(node: AsmRegisterOperand): T
    fun visitAsmInstList(node: AsmInstList): T
    fun visitAsmNegInst(node: AsmNegInst): T
    fun visitAsmNotInst(node: AsmNotInst): T
}