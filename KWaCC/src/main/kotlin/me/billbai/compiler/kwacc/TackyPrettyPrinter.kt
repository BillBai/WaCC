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
            is TackyCopyInst -> "Copy src=${printVal(inst.src)} dst=${printVal(inst.dst)}"
            is TackyJumpIfNotZeroInst -> "JumpIfNotZero ${inst.condition} ${inst.target}"
            is TackyJumpIfZeroInst -> "JumpIfZero ${inst.condition} ${inst.target}"
            is TackyJumpInst -> "Jump ${inst.target}"
            is TackyLabelInst -> "Label ${inst.identifier}"
        }
    }

    private fun printUnaryOp(op: TackyUnaryOp): String {
        return when (op) {
            is TackyNegateUnaryOp -> "Negate"
            is TackyComplementUnaryOp -> "Complement"
            is TackyNotUnaryOp -> "Not"
        }
    }

    private fun printBinaryOp(op: TackyBinaryOp): String {
        return when (op) {
            is TackyAddBinaryOp -> "Add"
            is TackyDivideBinaryOp -> "Divide"
            is TackyMultiplyBinaryOp -> "Multiply"
            is TackyRemainderBinaryOp -> "Remainder"
            is TackySubBinaryOp -> "Subtract"
            is TackyAndBinaryOp -> "And"
            is TackyEqualBinaryOp -> "Equal"
            is TackyGreaterBinaryOp -> "Greater"
            is TackyGreaterOrEqualBinaryOp -> "GreaterOrEqual"
            is TackyLessBinaryOp -> "Less"
            is TackyLessOrEqualBinaryOp -> "LessOrEqual"
            is TackyNotEqualBinaryOp -> "NotEqual"
            is TackyOrBinaryOp -> "Or"
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