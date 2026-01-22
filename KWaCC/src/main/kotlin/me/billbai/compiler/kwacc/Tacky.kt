package me.billbai.compiler.kwacc

sealed class TackyNode() {}

data class TackyProgram(
    val functionDefinition: TackyFunction
) : TackyNode() {}

data class TackyFunction(
    val identifier: String,
    val instructions: List<TackyInstruction>,
): TackyNode() {}

sealed class TackyInstruction() : TackyNode() {}

data class TackyReturnInst(
    val valued: TackyVal
) : TackyInstruction() {}

data class TackyUnaryInst(
    val op : TackyUnaryOp,
    val src : TackyVal,
    val dst : TackyVal,
) : TackyInstruction() {}

sealed class TackyVal() : TackyNode() {}

data class TackyConstantVal(
    val value: Int
) : TackyVal() {}

data class TackyVariableVal(
    val identifier: String,
) : TackyVal() {}

sealed class TackyUnaryOp() : TackyNode() {}

object TackyComplementUnaryOp : TackyUnaryOp() {}

object TackyNegateUnaryOp : TackyUnaryOp() {}