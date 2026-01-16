package me.billbai.compiler.kwacc

import me.billbai.compiler.kwacc.asm.AsmNode
import me.billbai.compiler.kwacc.asm.Program as AsmProgram
import me.billbai.compiler.kwacc.asm.FunctionDef as AsmFunctionDef
import me.billbai.compiler.kwacc.asm.Instruction as AsmInst
import me.billbai.compiler.kwacc.asm.MovInst as AsmMoveInst
import me.billbai.compiler.kwacc.asm.RetInst as AsmRetInst
import me.billbai.compiler.kwacc.asm.ImmOperand as AsmImmOperand
import me.billbai.compiler.kwacc.asm.RegisterOperand as AsmRegisterOperand
import me.billbai.compiler.kwacc.asm.Operand as AsmOperand

class AsmGenerator: ASTVisitor<AsmNode> {
    override fun visitProgram(node: Program): AsmNode {
        if (node.items.isEmpty()) {
            return AsmProgram(null)
        }
        if (node.items.size == 1) {
            val result = node.items[0].accept(this)
            if (result is AsmFunctionDef) {
                return AsmProgram(result)
            } else {
                // TODO(billbai) log this
            }
        }

        return AsmProgram(null)
    }

    override fun visitFunctionDefinition(node: FunctionDefinition): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitIntType(node: IntType): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitVoidType(node: VoidType): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitParameter(node: Parameter): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitBlockStmt(node: BlockStmt): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(node: ReturnStmt): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitIntConstant(node: IntConstant): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitIdentifier(node: Identifier): AsmNode {
        TODO("Not yet implemented")
    }
}