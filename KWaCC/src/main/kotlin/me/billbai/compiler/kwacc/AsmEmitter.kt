package me.billbai.compiler.kwacc

import java.io.PrintWriter

fun identifierName(identifier: String): String {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        "mac" in osName || "darwin" in osName -> "_${identifier}"
        else -> identifier
    }
}

class AsmEmitter(
    private val printWriter: PrintWriter
): AsmAstVisitor<Unit> {
    private fun writeNonExeStack() {
        val osName = System.getProperty("os.name").lowercase()
        if ("linux" in osName) {
            printWriter.write("\n.section .note.GNU-stack,\"\",@progbits\n")
        }
    }

    private fun formatOperand(operand: AsmOperand): String {
        // TODO(billbai) support more register
        return when (operand) {
            is AsmImmOperand -> "$${operand.value}"
            // TODO(billbai)
            is AsmPseudoOperand -> {
                ""
            }
            is AsmStackOperand -> {
                ""
            }
            is AsmRegisterOperand -> {
                when (operand.reg) {
                    is AsmRegAX -> {
                        "%eax"
                    }
                    is AsmRegR10 -> {
                        "%r10d"
                    }
                }
            }

        }
    }

    override fun visitAsmProgram(node: AsmProgram) {
        node.functionDef?.accept(this)
        writeNonExeStack()
    }

    override fun visitAsmFunctionDef(node: AsmFunctionDef) {
        val name = identifierName(node.name)
        printWriter.write("\t.global ${name} \n")
        printWriter.write("${name}: \n")
        node.instList.accept(this)
    }

    override fun visitAsmMovInst(node: AsmMovInst) {
        val src = formatOperand(node.src)
        val dst = formatOperand(node.dst)
        printWriter.write("\tmovl $src, $dst\n")
    }

    override fun visitAsmRetInst(node: AsmRetInst) {
        printWriter.write("\tret\n")
    }

    override fun visitAsmImmOperand(node: AsmImmOperand) {
        TODO("Not yet implemented")
    }

    override fun visitAsmRegisterOperand(node: AsmRegisterOperand) {
        TODO("Not yet implemented")
    }

    override fun visitAsmInstList(node: AsmInstList) {
        for (inst in node.instList) {
            inst.accept(this)
        }
    }

    override fun visitAsmNotUnaryOperator(node: AsmNotUnaryOperator) {
        TODO("Not yet implemented")
    }

    override fun visitAsmNegUnaryOperator(node: AsmNegUnaryOperator) {
        TODO("Not yet implemented")
    }

    override fun visitAsmUnaryInst(node: AsmUnaryInst) {
        when (node.op) {
            is AsmNegUnaryOperator -> {
                printWriter.println(("\t negl ${formatOperand(node.operand)}"))
            }
            is AsmNotUnaryOperator -> {
                printWriter.println(("\t notl ${formatOperand(node.operand)}"))
            }
        }
    }

    override fun visitAsmAllocateStackInst(node: AsmAllocateStackInst) {
        TODO("Not yet implemented")
    }

    override fun visitAsmRegAX(node: AsmRegAX) {
        TODO("Not yet implemented")
    }

    override fun visitAsmRegR10(node: AsmRegR10) {
        TODO("Not yet implemented")
    }

    override fun visitAsmPseudoOperand(node: AsmPseudoOperand) {
        TODO("Not yet implemented")
    }

    override fun visitAsmStackOperand(node: AsmStackOperand) {
        TODO("Not yet implemented")
    }

}