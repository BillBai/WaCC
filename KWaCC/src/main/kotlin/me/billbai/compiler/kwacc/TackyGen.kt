package me.billbai.compiler.kwacc

class TackyGen() : AstVisitor<TackyNode> {
    private var tmpCounter: Int = 0

    private fun makeTmp(): String {
        val tmp = "tmp.${tmpCounter}"
        tmpCounter += 1
        return tmp
    }

    private var labelCounter: Int = 0

    private fun makeLabel(prefix: String): String {
        val labelName = "${prefix}.${labelCounter}"
        labelCounter += 1
        return labelName
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

        val op = when (node.unaryOperator) {
            is NegateOperator -> {
                TackyNegateUnaryOp
            }
            is ComplementOperator -> {
                TackyComplementUnaryOp
            }

            NotOperator -> {
                TackyNotUnaryOp
            }
        }

        val dst = TackyVariableVal(makeTmp())
        val inst = TackyUnaryInst(op, subExpVal, dst)
        currentInstList.add(inst)
        return dst
    }

    private fun isShortCutBinaryOperator(op: BinaryOperator): Boolean {
        return op is AndOperator || op is OrOperator
    }

    private fun emitShortCutBinaryExpression(node: BinaryExpression): TackyNode {
        check(isShortCutBinaryOperator(node.binaryOperator))

        if (node.binaryOperator == OrOperator) {
            return emitOrBinaryExpression(node)
        }

        if (node.binaryOperator == AndOperator) {
            return emitAndBinaryExpression(node)
        }

        TODO("Unexcepted short cut binary operator")
    }

    private fun emitAndBinaryExpression(node: BinaryExpression): TackyNode {
        check(isShortCutBinaryOperator(node.binaryOperator))
        val lhsVal = node.lhs.accept(this)
        check(lhsVal is TackyVal)

        val falseLabel = makeLabel("false_label")
        val jumpInst = TackyJumpIfZeroInst(lhsVal, falseLabel)
        currentInstList.add(jumpInst)

        val rhsVal = node.rhs.accept(this)
        check(rhsVal is TackyVal)
        val jumpInst2 = TackyJumpIfZeroInst(rhsVal, falseLabel)
        currentInstList.add(jumpInst2)

        val resultVar = TackyVariableVal(makeTmp())
        val trueResultInst = TackyCopyInst(TackyConstantVal(1), resultVar)
        currentInstList.add(trueResultInst)
        val endLabel = makeLabel("end")
        val jumpToEndInst = TackyJumpInst(endLabel)
        currentInstList.add(jumpToEndInst)

        val falseLabelInst = TackyLabelInst(falseLabel)
        currentInstList.add(falseLabelInst)

        val falseResultInst = TackyCopyInst(TackyConstantVal(0), resultVar)
        currentInstList.add(falseResultInst)

        val endLabelInst = TackyLabelInst(endLabel)
        currentInstList.add(endLabelInst)
        return resultVar
    }

    private fun emitOrBinaryExpression(node: BinaryExpression): TackyNode {
        check(isShortCutBinaryOperator(node.binaryOperator))
        val lhsVal = node.lhs.accept(this)
        check(lhsVal is TackyVal)

        val trueLabel = makeLabel("true_label")
        val jumpInst = TackyJumpIfNotZeroInst(lhsVal, trueLabel)
        currentInstList.add(jumpInst)

        val rhsVal = node.rhs.accept(this)
        check(rhsVal is TackyVal)
        val jumpInst2 = TackyJumpIfNotZeroInst(rhsVal, trueLabel)
        currentInstList.add(jumpInst2)

        val resultVar = TackyVariableVal(makeTmp())
        val falseResultInst = TackyCopyInst(TackyConstantVal(0), resultVar)
        currentInstList.add(falseResultInst)
        val endLabel = makeLabel("end")
        val jumpToEndInst = TackyJumpInst(endLabel)
        currentInstList.add(jumpToEndInst)

        val trueLabelInst = TackyLabelInst(trueLabel)
        currentInstList.add(trueLabelInst)

        val trueResultInst = TackyCopyInst(TackyConstantVal(1), resultVar)
        currentInstList.add(trueResultInst)

        val endLabelInst = TackyLabelInst(endLabel)
        currentInstList.add(endLabelInst)
        return resultVar
    }

    override fun visitBinaryExpression(node: BinaryExpression): TackyNode {
        if (isShortCutBinaryOperator(node.binaryOperator)) {
            return emitShortCutBinaryExpression(node)
        }

        val lhsVal = node.lhs.accept(this)
        check(lhsVal is TackyVal)
        val rhsVal = node.rhs.accept(this)
        check(rhsVal is TackyVal)

        val binOp = when (node.binaryOperator) {
            AddOperator -> TackyAddBinaryOp
            DivideOperator -> TackyDivideBinaryOp
            MultiplyOperator -> TackyMultiplyBinaryOp
            RemainderOperator -> TackyRemainderBinaryOp
            SubOperator -> TackySubBinaryOp
            EqualOperator -> TackyEqualBinaryOp
            GreaterOperator -> TackyGreaterBinaryOp
            GreaterOrEqualOperator -> TackyGreaterOrEqualBinaryOp
            LessOperator -> TackyLessBinaryOp
            LessOrEqualOperator -> TackyLessOrEqualBinaryOp
            NotEqualOperator -> TackyNotEqualBinaryOp
            OrOperator -> TODO()
            AndOperator -> TODO()
        }

        val dst = TackyVariableVal(makeTmp())
        val inst = TackyBinaryInst(
            binOp,
            src1 = lhsVal,
            src2 = rhsVal,
            dst = dst,
        )
        currentInstList.add(inst)
        return dst
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

    override fun visitNotOperator(node: NotOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitAndOperator(node: AndOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitOrOperator(node: OrOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitEqualOperator(node: EqualOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitNotEqualOperator(node: NotEqualOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitLessThanOperator(node: LessOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitGreaterThanOperator(node: GreaterOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitLessOrEqualThanOperator(node: LessOrEqualOperator): TackyNode {
        TODO("Not yet implemented")
    }

    override fun visitGreaterOrEqualOperator(node: GreaterOrEqualOperator): TackyNode {
        TODO("Not yet implemented")
    }
}