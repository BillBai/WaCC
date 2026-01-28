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

        output.add(inst)
    }
}
