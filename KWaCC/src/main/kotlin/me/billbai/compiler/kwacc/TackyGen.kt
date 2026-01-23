package me.billbai.compiler.kwacc

class TackyGen() : AstVisitor<TackyNode> {
    private var tmpCounter : Int = 0

    private fun makeTmp() : String {
        val tmp = "tmp.${tmpCounter}"
        tmpCounter += 1
        return tmp
    }

    private var currentInstList : MutableList<TackyInstruction> = mutableListOf()

    override fun visitProgram(node: Program): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitFunctionDefinition(node: FunctionDefinition): TackyNode {
        check(currentInstList.isEmpty())
        val body = node.body.accept(this)
        val tackyFunction = TackyFunction(node.name,
            currentInstList.toList())
        currentInstList.clear()
        return tackyFunction
    }

    override fun visitIntType(node: IntType): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitVoidType(node: VoidType): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitParameter(node: Parameter): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitBlockStmt(node: BlockStmt): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(node: ReturnStmt): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitIntConstant(node: IntConstant): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitIdentifier(node: Identifier): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitComplementOperator(node: ComplementOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitNegateOperator(node: NegateOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitUnary(node: UnaryExpression): TackyNode {
        val subExpVal = node.expression.accept(this)
        check(subExpVal is TackyVal)

        val op =when (node.unaryOperator) {
            is NegateOperator -> {
                TackyNegateUnaryOp
            }
            is ComplementOperator -> {
                TackyComplementUnaryOp
            }
        }

        val dst = TackyVariableVal(makeTmp())
        val inst = TackyUnaryInst(op, subExpVal, dst)
        currentInstList.add(inst)
        return dst
    }

}