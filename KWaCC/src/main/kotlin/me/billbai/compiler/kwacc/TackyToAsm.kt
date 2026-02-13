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

    private fun convertTackyBinaryOpToAsmUnaryOp(tackyBinaryOp: TackyBinaryOp): AsmBinaryOperator {
        return when (tackyBinaryOp) {
            TackyAddBinaryOp -> AsmAddBinaryOperator
            TackySubBinaryOp -> AsmSubBinaryOperator
            TackyMultiplyBinaryOp -> AsmMultiplyBinaryOperator
            else -> throw IllegalArgumentException("Not a simple binary op")
        }
    }

    private fun emitUnaryInst(inst: TackyUnaryInst): List<AsmInstruction> {
        if (inst.op == TackyNegateUnaryOp || inst.op == TackyComplementUnaryOp) {
            val insts = mutableListOf<AsmInstruction>()
            val movSrcOperand = convertTackyValueToAsmOperand(inst.src)
            val movDstOperand = convertTackyValueToAsmOperand(inst.dst)
            val movInst = AsmMovInst(movSrcOperand, movDstOperand)
            val unaryOp = when (inst.op) {
                TackyComplementUnaryOp -> AsmNotUnaryOperator
                TackyNegateUnaryOp -> AsmNegUnaryOperator
                TackyNotUnaryOp -> throw IllegalStateException("unexcepted TackyNotUnaryOp op")
            }
            val unaryInst = AsmUnaryInst(unaryOp, movDstOperand)
            insts.add(movInst)
            insts.add(unaryInst)
            return insts
        }

        if (inst.op == TackyNotUnaryOp) {
            val insts = mutableListOf<AsmInstruction>()
            val src = convertTackyValueToAsmOperand(inst.src)
            val dst = convertTackyValueToAsmOperand(inst.dst)
            val cmpInst = AsmCmpInst(AsmImmOperand(0), src)
            val movInst = AsmMovInst(AsmImmOperand(0), dst)
            val setCCInst = AsmSetCCInst(AsmCondCode.E, dst)
            insts.add(cmpInst)
            insts.add(movInst)
            insts.add(setCCInst)
            return insts
        }

        throw IllegalStateException("Unexpected unary op: ${inst.op}")
    }

    private fun emitReturnInst(inst: TackyReturnInst): List<AsmInstruction> {
        val insts = mutableListOf<AsmInstruction>()
        if (inst.value != null) {
            val movOperand = convertTackyValueToAsmOperand(inst.value)
            val movInst = AsmMovInst(movOperand, AsmRegisterOperand(AsmRegAX))
            insts.add(movInst)
        }
        val retInst = AsmRetInst
        insts.add(retInst)
        return insts
    }

    private fun isBinaryRelationInst(inst: TackyBinaryInst): Boolean {
        return when (inst.op) {
            TackyEqualBinaryOp, TackyNotEqualBinaryOp,
            TackyGreaterBinaryOp, TackyGreaterOrEqualBinaryOp,
            TackyLessBinaryOp, TackyLessOrEqualBinaryOp
                -> true
            else -> false
        }
    }

    private fun condCodeForBinaryRelationOp(op: TackyBinaryOp): AsmCondCode {
        return when (op) {
            TackyGreaterBinaryOp -> AsmCondCode.G
            TackyGreaterOrEqualBinaryOp -> AsmCondCode.GE
            TackyLessBinaryOp -> AsmCondCode.L
            TackyLessOrEqualBinaryOp -> AsmCondCode.LE
            TackyNotEqualBinaryOp -> AsmCondCode.NE
            TackyEqualBinaryOp -> AsmCondCode.E
            else -> {
                throw IllegalArgumentException("Not a binary relation op $op")
            }
        }
    }
    private fun emitBinaryRelationInst(inst: TackyBinaryInst): List<AsmInstruction> {
        check(isBinaryRelationInst(inst))
        val src1 = convertTackyValueToAsmOperand(inst.src1)
        val src2 = convertTackyValueToAsmOperand(inst.src2)
        // we use AT&T syntax so swap the src2 and src1 order here
        val cmpInst = AsmCmpInst(src2, src1)
        val dst = convertTackyValueToAsmOperand(inst.dst)
        val clearInst = AsmMovInst(AsmImmOperand(0), dst)
        val setCCInst = AsmSetCCInst(condCodeForBinaryRelationOp(inst.op), dst)
        return listOf(cmpInst, clearInst, setCCInst)
    }

    private fun emitBinaryInst(inst: TackyBinaryInst): List<AsmInstruction> {
        if (isBinaryRelationInst(inst)) {
            return emitBinaryRelationInst(inst)
        }

        val insts = mutableListOf<AsmInstruction>()

        val src1 = convertTackyValueToAsmOperand(inst.src1)
        val src2 = convertTackyValueToAsmOperand(inst.src2)
        val dst = convertTackyValueToAsmOperand(inst.dst)
        when (inst.op) {
            is TackyAddBinaryOp, is TackySubBinaryOp, is TackyMultiplyBinaryOp -> {
                val asmBinOp = convertTackyBinaryOpToAsmUnaryOp(inst.op)
                val moveInst = AsmMovInst(src1, dst)
                val binOpInst = AsmBinaryInst(asmBinOp, src2, dst)
                insts.add(moveInst)
                insts.add(binOpInst)
            }
            is TackyDivideBinaryOp -> {
                val moveInst = AsmMovInst(src1, AsmRegisterOperand(AsmRegAX))
                val cdqInst = AsmCdqInst
                val idivInst = AsmIdivInst(src2)
                val moveResultInst = AsmMovInst(AsmRegisterOperand(AsmRegAX), dst)
                insts.add(moveInst)
                insts.add(cdqInst)
                insts.add(idivInst)
                insts.add(moveResultInst)
            }
            is TackyRemainderBinaryOp -> {
                val moveInst = AsmMovInst(src1, AsmRegisterOperand(AsmRegAX))
                val cdqInst = AsmCdqInst
                val idivInst = AsmIdivInst(src2)
                val moveResultInst = AsmMovInst(AsmRegisterOperand(AsmRegDX), dst)
                insts.add(moveInst)
                insts.add(cdqInst)
                insts.add(idivInst)
                insts.add(moveResultInst)
            }

            // And/Or binary ops are generated with shortcut logic and not use here
            TackyAndBinaryOp,
            TackyOrBinaryOp,
                // binary relation ops are handled elsewhere.
            TackyEqualBinaryOp,
            TackyGreaterBinaryOp,
            TackyGreaterOrEqualBinaryOp,
            TackyLessBinaryOp,
            TackyLessOrEqualBinaryOp,
            TackyNotEqualBinaryOp -> {
                throw IllegalStateException("Should Not Reach Here!")
            }
        }

        return insts
    }

    private fun emitCopyInst(inst: TackyCopyInst): List<AsmInstruction> {
        val insts = mutableListOf<AsmInstruction>()
        val src = convertTackyValueToAsmOperand(inst.src)
        val dst = convertTackyValueToAsmOperand(inst.dst)
        val moveInst = AsmMovInst(src, dst)
        insts.add(moveInst)
        return insts
    }

    private fun emitJumpIfNotZeroInst(inst: TackyJumpIfNotZeroInst): List<AsmInstruction> {
        val insts = mutableListOf<AsmInstruction>()
        val cond = convertTackyValueToAsmOperand(inst.condition)
        val cmpInst = AsmCmpInst(AsmImmOperand(0), cond)
        insts.add(cmpInst)
        val jmpInst = AsmJmpCCInst(AsmCondCode.NE, inst.target)
        insts.add(jmpInst)
        return insts
    }

    private fun emitJumpIfZeroInst(inst: TackyJumpIfZeroInst): List<AsmInstruction> {
        val insts = mutableListOf<AsmInstruction>()
        val cond = convertTackyValueToAsmOperand(inst.condition)
        val cmpInst = AsmCmpInst(AsmImmOperand(0), cond)
        insts.add(cmpInst)
        val jmpInst = AsmJmpCCInst(AsmCondCode.E, inst.target)
        insts.add(jmpInst)
        return insts
    }

    private fun emitJumpInst(inst: TackyJumpInst): List<AsmInstruction> {
        return listOf(AsmJmpInst(inst.target))
    }

    private fun emitLabelInst(inst: TackyLabelInst): List<AsmInstruction> {
        return listOf(AsmLabelInst(inst.identifier))
    }

    private fun convertInstruction(inst: TackyInstruction): List<AsmInstruction> {
        when (inst) {
            is TackyReturnInst -> {
               return emitReturnInst(inst)
            }
            is TackyUnaryInst -> {
                return emitUnaryInst(inst)
            }
            is TackyBinaryInst -> {
                return emitBinaryInst(inst)
            }
            is TackyCopyInst -> {
                return emitCopyInst(inst)
            }
            is TackyJumpIfNotZeroInst -> {
                return emitJumpIfNotZeroInst(inst)
            }
            is TackyJumpIfZeroInst -> {
                return emitJumpIfZeroInst(inst)
            }
            is TackyJumpInst -> {
                return emitJumpInst(inst)
            }
            is TackyLabelInst -> {
                return emitLabelInst(inst)
            }
        }
    }
}
