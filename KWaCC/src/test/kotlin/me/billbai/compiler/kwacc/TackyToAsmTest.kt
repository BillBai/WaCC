package me.billbai.compiler.kwacc

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TackyToAsmTest {

    private fun convertInstructions(vararg instructions: TackyInstruction): List<AsmInstruction> {
        val program = TackyProgram(
            TackyFunction("test", instructions.toList())
        )
        val result = TackyToAsm().convert(program)
        // Unwrap: AsmProgram -> AsmFunctionDef -> AsmInstList -> instList
        return result.functionDef!!.instList.instList
    }

    @Test
    fun `Not unary emits Cmp, Mov zero, SetCC E`() {
        val insts = convertInstructions(
            TackyUnaryInst(
                TackyNotUnaryOp,
                TackyVariableVal("src"),
                TackyVariableVal("dst")
            )
        )

        assertEquals(3, insts.size)

        val cmp = insts[0]
        assertIs<AsmCmpInst>(cmp)
        assertIs<AsmImmOperand>(cmp.operand1)
        assertEquals(0, (cmp.operand1 as AsmImmOperand).value)
        assertIs<AsmPseudoOperand>(cmp.operand2)
        assertEquals("src", (cmp.operand2 as AsmPseudoOperand).identifier)

        val mov = insts[1]
        assertIs<AsmMovInst>(mov)
        assertIs<AsmImmOperand>(mov.src)
        assertEquals(0, (mov.src as AsmImmOperand).value)

        val setCC = insts[2]
        assertIs<AsmSetCCInst>(setCC)
        assertEquals(AsmCondCode.E, setCC.condCode)
    }

    @Test
    fun `Negate unary emits Mov then Neg`() {
        val insts = convertInstructions(
            TackyUnaryInst(
                TackyNegateUnaryOp,
                TackyConstantVal(5),
                TackyVariableVal("dst")
            )
        )

        assertEquals(2, insts.size)
        assertIs<AsmMovInst>(insts[0])
        val unary = insts[1]
        assertIs<AsmUnaryInst>(unary)
        assertEquals(AsmNegUnaryOperator, unary.op)
    }

    @Test
    fun `Complement unary emits Mov then Not`() {
        val insts = convertInstructions(
            TackyUnaryInst(
                TackyComplementUnaryOp,
                TackyConstantVal(5),
                TackyVariableVal("dst")
            )
        )

        assertEquals(2, insts.size)
        assertIs<AsmMovInst>(insts[0])
        val unary = insts[1]
        assertIs<AsmUnaryInst>(unary)
        assertEquals(AsmNotUnaryOperator, unary.op)
    }

    @Test
    fun `Less relational emits Cmp, Mov zero, SetCC L`() {
        val insts = convertInstructions(
            TackyBinaryInst(
                TackyLessBinaryOp,
                TackyVariableVal("a"),
                TackyVariableVal("b"),
                TackyVariableVal("dst")
            )
        )

        assertEquals(3, insts.size)

        // cmp should have src2, src1 (reversed for AT&T)
        val cmp = insts[0]
        assertIs<AsmCmpInst>(cmp)
        assertEquals("b", (cmp.operand1 as AsmPseudoOperand).identifier)
        assertEquals("a", (cmp.operand2 as AsmPseudoOperand).identifier)

        val mov = insts[1]
        assertIs<AsmMovInst>(mov)
        assertEquals(0, (mov.src as AsmImmOperand).value)

        val setCC = insts[2]
        assertIs<AsmSetCCInst>(setCC)
        assertEquals(AsmCondCode.L, setCC.condCode)
    }

    @Test
    fun `all relational ops produce correct condition codes`() {
        data class RelTestCase(val op: TackyBinaryOp, val expectedCC: AsmCondCode)

        val cases = listOf(
            RelTestCase(TackyEqualBinaryOp, AsmCondCode.E),
            RelTestCase(TackyNotEqualBinaryOp, AsmCondCode.NE),
            RelTestCase(TackyLessBinaryOp, AsmCondCode.L),
            RelTestCase(TackyLessOrEqualBinaryOp, AsmCondCode.LE),
            RelTestCase(TackyGreaterBinaryOp, AsmCondCode.G),
            RelTestCase(TackyGreaterOrEqualBinaryOp, AsmCondCode.GE),
        )

        for (case in cases) {
            val insts = convertInstructions(
                TackyBinaryInst(case.op, TackyConstantVal(1), TackyConstantVal(2), TackyVariableVal("dst"))
            )
            val setCC = insts[2]
            assertIs<AsmSetCCInst>(setCC)
            assertEquals(case.expectedCC, setCC.condCode, "Failed for ${case.op}")
        }
    }

    @Test
    fun `Copy emits single Mov`() {
        val insts = convertInstructions(
            TackyCopyInst(TackyConstantVal(42), TackyVariableVal("dst"))
        )

        assertEquals(1, insts.size)
        val mov = insts[0]
        assertIs<AsmMovInst>(mov)
        assertEquals(42, (mov.src as AsmImmOperand).value)
        assertEquals("dst", (mov.dst as AsmPseudoOperand).identifier)
    }

    @Test
    fun `JumpIfZero emits Cmp zero then JmpCC E`() {
        val insts = convertInstructions(
            TackyJumpIfZeroInst(TackyVariableVal("cond"), "target_label")
        )

        assertEquals(2, insts.size)

        val cmp = insts[0]
        assertIs<AsmCmpInst>(cmp)
        assertEquals(0, (cmp.operand1 as AsmImmOperand).value)
        assertEquals("cond", (cmp.operand2 as AsmPseudoOperand).identifier)

        val jmp = insts[1]
        assertIs<AsmJmpCCInst>(jmp)
        assertEquals(AsmCondCode.E, jmp.condCode)
        assertEquals("target_label", jmp.target)
    }

    @Test
    fun `JumpIfNotZero emits Cmp zero then JmpCC NE`() {
        val insts = convertInstructions(
            TackyJumpIfNotZeroInst(TackyVariableVal("cond"), "target_label")
        )

        assertEquals(2, insts.size)

        val cmp = insts[0]
        assertIs<AsmCmpInst>(cmp)
        assertEquals(0, (cmp.operand1 as AsmImmOperand).value)

        val jmp = insts[1]
        assertIs<AsmJmpCCInst>(jmp)
        assertEquals(AsmCondCode.NE, jmp.condCode)
        assertEquals("target_label", jmp.target)
    }

    @Test
    fun `Jump emits Jmp`() {
        val insts = convertInstructions(
            TackyJumpInst("my_label")
        )

        assertEquals(1, insts.size)
        val jmp = insts[0]
        assertIs<AsmJmpInst>(jmp)
        assertEquals("my_label", jmp.target)
    }

    @Test
    fun `Label emits Label`() {
        val insts = convertInstructions(
            TackyLabelInst("my_label")
        )

        assertEquals(1, insts.size)
        val label = insts[0]
        assertIs<AsmLabelInst>(label)
        assertEquals("my_label", label.identifier)
    }
}
