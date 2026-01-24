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
        builder.append("Ret(${node.operand?.accept(this)})")
        return builder.toString()
    }

    override fun visitAsmImmOperand(node: AsmImmOperand): String {
        return "Imm(${node.value})"
    }

    override fun visitAsmRegisterOperand(node: AsmRegisterOperand): String {
        // TODO(bilbai): add other regs
        return "Reg(EAX)"
    }

    override fun visitAsmInstList(node: AsmInstList): String {
        val builder = StringBuilder()
        for (inst in node.instList) {
            builder.append(getIndent() + inst.accept(this))
            builder.append("\n")
        }
        return builder.toString()
    }

    override fun visitAsmNegInst(node: AsmNegInst): String {
        return "NEG ${node.operand.accept(this)}"
    }

    override fun visitAsmNotInst(node: AsmNotInst): String {
        return "NOT ${node.operand.accept(this)}"
    }
}