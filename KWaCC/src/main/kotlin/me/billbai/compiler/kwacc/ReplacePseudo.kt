package me.billbai.compiler.kwacc

class ReplacePseudo {
    private var currentOffset: Int = 0
    private val pseudoMap: MutableMap<String, Int> = mutableMapOf()

    // returns new program + total stack size
    fun replace(program: AsmProgram): Pair<AsmProgram, Int> {
        if (program.functionDef == null) {
            return Pair(program, 0)
        }

        val newFunctionDef = replaceFunction(program.functionDef)
        val newProgram = AsmProgram(newFunctionDef)
        val stackSize = -currentOffset
        return Pair(newProgram, stackSize)
    }

    private fun replaceFunction(functionDef: AsmFunctionDef): AsmFunctionDef {
        val newInstList = mutableListOf<AsmInstruction>()
        for (inst in functionDef.instList.instList) {
           val newInst = replaceInstruction(inst)
            newInstList.add(newInst)
        }
        val newFunctionDef = AsmFunctionDef(functionDef.name, AsmInstList(newInstList))
        return newFunctionDef
    }

    private fun replaceInstruction(inst: AsmInstruction): AsmInstruction {
        if (inst is AsmMovInst) {
            return inst.copy(
                src = replaceOperand(inst.src),
                dst = replaceOperand(inst.dst)
            )
        }
        if (inst is AsmUnaryInst) {
            return inst.copy(
                op = inst.op,
                operand = replaceOperand(inst.operand))

        }
        return inst
    }

    private fun replaceOperand(operand: AsmOperand): AsmOperand {
        if (operand is AsmPseudoOperand) {
            var offset = pseudoMap[operand.identifier]
            if (offset == null) {
                currentOffset -= 4
                pseudoMap[operand.identifier] = currentOffset
                offset = currentOffset
            }
            return AsmStackOperand(offset)
        }
        return operand
    }


}