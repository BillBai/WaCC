package me.billbai.compiler.kwacc

class TackyPrettyPrinter {
    fun print(program: TackyProgram): String {
        val fn = program.functionDefinition
        val sb = StringBuilder()
        sb.appendLine("Function:${fn.identifier}")
        for (inst in fn.instructions) {
            sb.appendLine("  ${printInstruction(inst)}")
        }
        return sb.toString()
    }

    fun printInstruction(inst: TackyInstruction): String {
        return when (inst) {
            is TackyUnaryInst -> "${printVal(inst.dst)} = ${printUnaryOp(inst.op)} ${printVal(inst.src)}"
            is TackyReturnInst -> "Return ${inst.value?.let { printVal(it) } ?: "void"}"
            is TackyBinaryInst -> "${printVal(inst.dst)} = ${printVal(inst.src1)} ${printBinaryOp(inst.op)} ${printVal(inst.src2)}"
        }
    }

    private fun printUnaryOp(op: TackyUnaryOp): String {
        return when (op) {
            is TackyNegateUnaryOp -> "Negate"
            is TackyComplementUnaryOp -> "Complement"
        }
    }

    private fun printBinaryOp(op: TackyBinaryOp): String {
        return when (op) {
            is TackyAddBinaryOp -> "Add"
            TackyDivideBinaryOp -> "Divide"
            TackyMultiplyBinaryOp -> "Multiply"
            TackyRemainderBinaryOp -> "Remainder"
            TackySubBinaryOp -> "Subtract"
        }
    }

    private fun printVal(value: TackyVal): String {
        return when (value) {
            is TackyConstantVal -> {
                value.value.toString()
            }
            is TackyVariableVal -> {
                value.identifier
            }
        }
    }
}