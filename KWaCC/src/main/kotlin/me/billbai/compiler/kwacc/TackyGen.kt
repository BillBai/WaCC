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
        // TODO(billbai) a program may have more than 1 function. deal with that too
        if (node.items.isEmpty()) {
            return TackyFunction("null", emptyList<TackyInstruction>())
        }
        if (node.items.size > 1) {
            println("WARNING: only support one function for now!")
        }
        val function = node.items[0].accept(this)
        check(function is TackyFunction)
        val program = TackyProgram(function)
        return program
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
        // this is dummy value.
        // no one cares about statement's return value
        // just like no one cares about me. :(
        var last : TackyNode = TackyConstantVal(0)
        for (statement in node.statements) {
            last = statement.accept(this)
        }
        return last
    }

    override fun visitReturnStmt(node: ReturnStmt): TackyNode {
        val value = node.expression?.accept(this)
        check(value is TackyVal)
        val returnInst = TackyReturnInst(value)
        currentInstList.add(returnInst)
        return returnInst
    }

    override fun visitIntConstant(node: IntConstant): TackyNode {
        // TODO(billbai): check node.value is a valid Integer
        return TackyConstantVal(node.value.toInt())
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

    override fun visitBinaryExpression(node: BinaryExpression): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitAddOperator(node: AddOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitSubOperator(node: SubOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitMultiplyOperator(node: MultiplyOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitDivideOperator(node: DivideOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitRemainderOperator(node: RemainderOperator): TackyNode {
        TODO("Not yet implemented")
    }
}