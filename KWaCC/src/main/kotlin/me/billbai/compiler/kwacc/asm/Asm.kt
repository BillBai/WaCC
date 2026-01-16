package me.billbai.compiler.kwacc.asm

sealed class AsmNode {
}

data class Program(
    val functionDef: FunctionDef?
) : AsmNode() {

}

data class FunctionDef(
    val name: String,
    val instructions: List<Instruction>
) : AsmNode() {}

sealed class Instruction : AsmNode() {}

data class MovInst(
    val src: Operand,
    val dst: Operand,
): Instruction() {}

object RetInst: Instruction() {}

sealed class Operand : AsmNode() {}

data class ImmOperand(
    val value: Int
): Operand() {}

// only exa for now, so make this object. will add more reg later.
object RegisterOperand: Operand() {}