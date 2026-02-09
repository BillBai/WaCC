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
    val value: TackyVal?
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

sealed class TackyBinaryOp() : TackyNode() {}

object TackyAddBinaryOp: TackyBinaryOp() {}

object TackySubBinaryOp: TackyBinaryOp() {}

object TackyMultiplyBinaryOp: TackyBinaryOp() {}

object TackyDivideBinaryOp: TackyBinaryOp() {}

object TackyRemainderBinaryOp: TackyBinaryOp() {}

data class TackyBinaryInst(
    val op: TackyBinaryOp,
    val src1: TackyVal,
    val src2: TackyVal,
    val dst: TackyVal
): TackyInstruction() {}

object TackyNotUnaryOp: TackyUnaryOp() {}

object TackyEqualBinaryOp: TackyBinaryOp() {}

object TackyNotEqualBinaryOp: TackyBinaryOp() {}

object TackyLessOrEqualBinaryOp: TackyBinaryOp() {}

object TackyLessBinaryOp: TackyBinaryOp() {}

object TackyGreaterBinaryOp: TackyBinaryOp() {}

object TackyGreaterOrEqualBinaryOp: TackyBinaryOp() {}

object TackyAndBinaryOp: TackyBinaryOp() {}

object TackyOrBinaryOp: TackyBinaryOp() {}


data class TackyCopyInst(
    val src: TackyVal,
    val dst: TackyVal,
): TackyInstruction() {}

data class TackyJumpInst(
    val target: String,
): TackyInstruction() {}

data class TackyJumpIfZeroInst(
    val condition: TackyVal,
    val target: String,
): TackyInstruction() {}

data class TackyJumpIfNotZeroInst(
    val condition: TackyVal,
    val target: String,
): TackyInstruction() {}

data class TackyLabelInst(
    val identifier: String,
): TackyInstruction() {}




