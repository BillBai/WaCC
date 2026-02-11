package me.billbai.compiler.kwacc

class AsmAstPrettyPrinter: AsmAstVisitor<String> {
    private var indentLevel: Int = 0
    private fun getIndent(): String = "  ".repeat(indentLevel)
    private fun withIndent(block: (indent: String) -> String): String {
        indentLevel += 1
        val result = block(getIndent())
        indentLevel -= 1
        return result
    }

    override fun visitAsmProgram(node: AsmProgram): String {
        val builder = StringBuilder()
        builder.append("AsmProgram\n")
        val result: String = withIndent { indent ->
            indent + node.functionDef?.accept(this)
        }
        return builder.append(result).toString()
    }

    override fun visitAsmFunctionDef(node: AsmFunctionDef): String {
        val builder = StringBuilder()
        builder.append("AsmFunction(name=${node.name})\n")
        val result = withIndent {
            node.instList.accept(this)
        }
        builder.append(result)
        return builder.toString()
    }

    override fun visitAsmMovInst(node: AsmMovInst): String {
        val builder = StringBuilder()
        builder.append("Mov(")
        builder.append("src=")
        builder.append("${node.src.accept(this)}, ")
        builder.append("dst=")
        builder.append(node.dst.accept(this))
        builder.append(")")

        return builder.toString()
    }

    override fun visitAsmRetInst(node: AsmRetInst): String {
        val builder = StringBuilder()
        builder.append("Ret")
        return builder.toString()
    }

    override fun visitAsmImmOperand(node: AsmImmOperand): String {
        return "Imm(${node.value})"
    }

    override fun visitAsmRegisterOperand(node: AsmRegisterOperand): String {
        return when (node.reg) {
            is AsmRegAX -> "Reg(EAX)"
            is AsmRegR10 -> "Reg(R10D)"
            is AsmRegR11 -> "Reg(R11D)"
            is AsmRegDX -> "Reg(EDX)"
        }
    }

    override fun visitAsmInstList(node: AsmInstList): String {
        val builder = StringBuilder()
        for (inst in node.instList) {
            builder.append(getIndent() + inst.accept(this))
            builder.append("\n")
        }
        return builder.toString()
    }

    override fun visitAsmNotUnaryOperator(node: AsmNotUnaryOperator): String {
        return "Not"
    }

    override fun visitAsmNegUnaryOperator(node: AsmNegUnaryOperator): String {
        return "Neg"
    }

    override fun visitAsmUnaryInst(node: AsmUnaryInst): String {
        val builder = StringBuilder()
        val opStr = when (node.op) {
            is AsmNegUnaryOperator -> "Neg"
            is AsmNotUnaryOperator -> "Not"
        }
        builder.append("Unary(op_type=${opStr}, operand=${node.operand.accept(this)})")
        return builder.toString()
    }

    override fun visitAsmAllocateStackInst(node: AsmAllocateStackInst): String {
        return "AllocateStack(${node.size})"
    }

    override fun visitAsmRegAX(node: AsmRegAX): String {
        return "AX"
    }

    override fun visitAsmRegR10(node: AsmRegR10): String {
        return "R10"
    }

    override fun visitAsmRegR11(node: AsmRegR11): String {
        return "R11"
    }

    override fun visitAsmPseudoOperand(node: AsmPseudoOperand): String {
        return "Pseudo(\"${node.identifier}\")"
    }

    override fun visitAsmStackOperand(node: AsmStackOperand): String {
        return "Stack(${node.offset})"
    }

    override fun visitAsmRegDX(node: AsmRegDX): String = "DX"

    override fun visitAsmAddBinaryOperator(node: AsmAddBinaryOperator): String = "Add"

    override fun visitAsmSubBinaryOperator(node: AsmSubBinaryOperator): String = "Sub"

    override fun visitAsmMultiplyBinaryOperator(node: AsmMultiplyBinaryOperator): String = "Mult"

    override fun visitAsmBinaryInst(node: AsmBinaryInst): String {
        val opStr = when (node.op) {
            is AsmAddBinaryOperator -> "Add"
            is AsmSubBinaryOperator -> "Sub"
            is AsmMultiplyBinaryOperator -> "Mult"
        }
        return "Binary(op=$opStr, src=${node.src.accept(this)}, dst=${node.dst.accept(this)})"
    }

    override fun visitAsmIdivInst(node: AsmIdivInst): String {
        return "Idiv(${node.operand.accept(this)})"
    }

    override fun visitAsmCdqInst(node: AsmCdqInst): String = "Cdq"

    override fun visitAsmCmpInst(node: AsmCmpInst): String {
        return "cmpl ${node.operand1.accept(this)}, ${node.operand2.accept(this)}"
    }

    override fun visitAsmJmpInst(node: AsmJmpInst): String {
        return "jmp .L${node.target}"
    }

    override fun visitAsmJmpCCInst(node: AsmJmpCCInst): String {
        return "j${node.condCode.formatAsmString()} .L${node.target}"
    }

    override fun visitAsmSetCCInst(node: AsmSetCCInst): String {
        return "set${node.condCode.formatAsmString()} ${node.operand.accept(this)}"
    }

    override fun visitAsmLabelInst(node: AsmLabelInst): String {
        return ".L${node.identifier}"
    }
}