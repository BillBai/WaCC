package me.billbai.compiler.kwacc

class FixupInstructions {

    fun fixup(program: AsmProgram, stackSize: Int): AsmProgram {
        if (program.functionDef == null) {
            return program
        }
        val newFunctionDef = fixupFunction(program.functionDef, stackSize)
        return AsmProgram(newFunctionDef)
    }

    private fun fixupFunction(functionDef: AsmFunctionDef, stackSize: Int): AsmFunctionDef {
        val newInstList = mutableListOf<AsmInstruction>()

        val allocateStackInst = AsmAllocateStackInst(stackSize)
        newInstList.add(allocateStackInst)

        for (inst in functionDef.instList.instList) {
            fixupInstruction(inst, newInstList)
        }

        return AsmFunctionDef(functionDef.name, AsmInstList(newInstList))
    }

    private fun fixupInstruction(inst: AsmInstruction, output: MutableList<AsmInstruction>) {
        if (inst is AsmMovInst) {
            val src = inst.src
            val dst = inst.dst
            if ((src is AsmStackOperand) && (dst is AsmStackOperand)) {
                val tmpRegOperand = AsmRegisterOperand(AsmRegR10)
                val newSrcInst = AsmMovInst(src=src, dst=tmpRegOperand)
                val newDstInst = AsmMovInst(src=tmpRegOperand, dst=dst)
                output.add(newSrcInst)
                output.add(newDstInst)
                return
            }
        }
        if (inst is AsmCmpInst) {
            val op1 = inst.operand1
            val op2 = inst.operand2
            if (op1 is AsmStackOperand && op2 is AsmStackOperand) {
                val tmpRegOperand = AsmRegisterOperand(AsmRegR10)
                val movOp1 = AsmMovInst(src=op1, dst=tmpRegOperand)
                val newCmpInst = AsmCmpInst(tmpRegOperand, op2)
                output.add(movOp1)
                output.add(newCmpInst)
                return
            }
            if (op2 is AsmImmOperand) {
                val tmpRegOperand = AsmRegisterOperand(AsmRegR11)
                val movOp2 = AsmMovInst(op2, tmpRegOperand)
                val newCmpInst = AsmCmpInst(op1, tmpRegOperand)
                output.add(movOp2)
                output.add(newCmpInst)
                return
            }
        }
        if (inst is AsmBinaryInst) {
            if (inst.op == AsmAddBinaryOperator || inst.op == AsmSubBinaryOperator) {
                if (inst.src is AsmStackOperand && inst.dst is AsmStackOperand) {
                    val tmpRegOperand = AsmRegisterOperand(AsmRegR10)
                    val movSrcInst = AsmMovInst(src=inst.src, dst=tmpRegOperand)
                    val newBinOpInst = AsmBinaryInst(inst.op, src=tmpRegOperand, dst=inst.dst)
                    output.add(movSrcInst)
                    output.add(newBinOpInst)
                    return
                }
            }
            if (inst.op == AsmMultiplyBinaryOperator) {
                if (inst.dst is AsmStackOperand) {
                    val tmpRegOperand = AsmRegisterOperand(AsmRegR11)
                    val movInst = AsmMovInst(src=inst.dst, tmpRegOperand)
                    val newMulInst = AsmBinaryInst(AsmMultiplyBinaryOperator, src=inst.src, dst=tmpRegOperand)
                    val movResultInst = AsmMovInst(src=tmpRegOperand, dst=inst.dst)

                    output.add(movInst)
                    output.add(newMulInst)
                    output.add(movResultInst)
                    return
                }
            }
        }
        if (inst is AsmIdivInst) {
            if (inst.operand is AsmImmOperand) {
                val tmpRegOperand = AsmRegisterOperand(AsmRegR10)
                val movInst = AsmMovInst(src=inst.operand, dst=tmpRegOperand)
                val newIdivInst = AsmIdivInst(tmpRegOperand)
                output.add(movInst)
                output.add(newIdivInst)
                return
            }
        }

        output.add(inst)
    }
}
