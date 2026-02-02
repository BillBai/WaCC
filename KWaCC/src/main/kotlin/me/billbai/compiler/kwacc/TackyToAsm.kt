package me.billbai.compiler.kwacc

class TackyToAsm {
    fun convert(program: TackyProgram): AsmProgram {
        val fn = convertFunction(program.functionDefinition)
        return AsmProgram(fn)
    }

    private fun convertFunction(fn: TackyFunction): AsmFunctionDef {
        val instructions = mutableListOf<AsmInstruction>()
        for (inst in fn.instructions) {
            instructions.addAll(convertInstruction(inst))
        }
        return AsmFunctionDef(fn.identifier, AsmInstList(instructions))
    }

    private fun convertTackyValueToAsmOperand(tackyValue: TackyVal): AsmOperand {
        return when (tackyValue) {
            is TackyConstantVal -> {
                AsmImmOperand(tackyValue.value)
            }
            is TackyVariableVal -> {
                AsmPseudoOperand(tackyValue.identifier)
            }
        }
    }

    private fun convertTackyUnaryOpToAsmUnaryOp(tackyUnaryOp: TackyUnaryOp): AsmUnaryOperator {
        return when (tackyUnaryOp) {
            TackyComplementUnaryOp -> AsmNotUnaryOperator
            TackyNegateUnaryOp -> AsmNegUnaryOperator
        }
    }

    private fun convertInstruction(inst: TackyInstruction): List<AsmInstruction> {
        val insts = mutableListOf<AsmInstruction>()
        when (inst) {
            is TackyReturnInst -> {
                if (inst.value != null) {
                    val movOperand = convertTackyValueToAsmOperand(inst.value)
                    val movInst = AsmMovInst(movOperand, AsmRegisterOperand(AsmRegAX))
                    insts.add(movInst)
                }
                val retInst = AsmRetInst
                insts.add(retInst)
            }
            is TackyUnaryInst -> {
                val movSrcOperand = convertTackyValueToAsmOperand(inst.src)
                val movDstOperand = convertTackyValueToAsmOperand(inst.dst)
                val movInst = AsmMovInst(movSrcOperand, movDstOperand)
                val unaryOp = convertTackyUnaryOpToAsmUnaryOp(inst.op)
                val unaryInst = AsmUnaryInst(unaryOp, movDstOperand)
                insts.add(movInst)
                insts.add(unaryInst)
            }
            is TackyBinaryInst -> {
                TODO()
            }
        }
        return insts
    }
}
