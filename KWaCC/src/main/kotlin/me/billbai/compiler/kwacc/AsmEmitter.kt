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
        return when (operand) {
            is AsmImmOperand -> "$${operand.value}"
            is AsmPseudoOperand -> {
                // TODO(billbai) should error
                ""
            }
            is AsmStackOperand -> {
                "${operand.offset}(%rbp)"
            }
            is AsmRegisterOperand -> {
                when (operand.reg) {
                    is AsmRegAX -> "%eax"
                    is AsmRegR10 -> "%r10d"
                    is AsmRegDX -> "%edx"
                }
            }

        }
    }

    override fun visitAsmProgram(node: AsmProgram) {
        node.functionDef?.accept(this)
        writeNonExeStack()
    }

    private fun emitFunctionPrologue() {
        printWriter.write("\tpushq %rbp\n")
        printWriter.write("\tmovq %rsp, %rbp\n")
    }

    private fun emitFunctionEpilogue() {
        printWriter.write("\tmovq %rbp, %rsp\n")
        printWriter.write("\tpopq %rbp\n")
    }

    override fun visitAsmFunctionDef(node: AsmFunctionDef) {
        val name = identifierName(node.name)
        printWriter.write("\t.global ${name} \n")
        printWriter.write("${name}: \n")
        emitFunctionPrologue()
        node.instList.accept(this)
    }

    override fun visitAsmMovInst(node: AsmMovInst) {
        val src = formatOperand(node.src)
        val dst = formatOperand(node.dst)
        printWriter.write("\tmovl $src, $dst\n")
    }

    override fun visitAsmRetInst(node: AsmRetInst) {
        emitFunctionEpilogue()
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
        printWriter.write("\tsubq \$${node.size}, %rsp\n")
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

    override fun visitAsmRegDX(node: AsmRegDX) {
        TODO("Not yet implemented")
    }

    override fun visitAsmAddBinaryOperator(node: AsmAddBinaryOperator) {
        TODO("Not yet implemented")
    }

    override fun visitAsmSubBinaryOperator(node: AsmSubBinaryOperator) {
        TODO("Not yet implemented")
    }

    override fun visitAsmMultiplyBinaryOperator(node: AsmMultiplyBinaryOperator) {
        TODO("Not yet implemented")
    }

    override fun visitAsmBinaryInst(node: AsmBinaryInst) {
        val opName = when (node.op) {
            is AsmAddBinaryOperator -> "addl"
            is AsmSubBinaryOperator -> "subl"
            is AsmMultiplyBinaryOperator -> "imull"
        }
        printWriter.println("\t$opName ${formatOperand(node.src)}, ${formatOperand(node.dst)}")
    }

    override fun visitAsmIdivInst(node: AsmIdivInst) {
        printWriter.println("\tidivl ${formatOperand(node.operand)}")
    }

    override fun visitAsmCdqInst(node: AsmCdqInst) {
        printWriter.println("\tcdq")
    }
}