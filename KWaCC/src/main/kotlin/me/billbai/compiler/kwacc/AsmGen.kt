package me.billbai.compiler.kwacc

class AsmGenerator : AstVisitor<AsmNode> {
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
        val result = node.body.accept(this)
        if (result !is AsmInstList) {
            // TODO(billbai): Not supported yet.
            return AsmFunctionDef(node.name, AsmInstList(emptyList()))
        }

        return AsmFunctionDef(node.name, result)
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
        val instList = mutableListOf<AsmInstruction>()
        for (statement in node.statements) {
            when (val result = statement.accept(this)) {
                is AsmInstList -> instList.addAll(result.instList)
                is AsmInstruction -> instList.add(result)
                else -> {
                    // TODO(billbai): Not implemented yet.
                }
            }
        }
        return AsmInstList(instList)
    }

    override fun visitReturnStmt(node: ReturnStmt): AsmNode {
        val instList = mutableListOf<AsmInstruction>()
        if (node.expression != null) {
            val result = node.expression.accept(this)
            if (result is AsmOperand) {
                val dstOperand = AsmRegisterOperand(AsmRegAX)
                val movInst = AsmMovInst(src = result, dst = dstOperand)
                instList.add(movInst)
            }
        }
        instList.add(AsmRetInst)
        return AsmInstList(instList);
    }

    override fun visitIntConstant(node: IntConstant): AsmNode {
        // TODO(billbai) check value is a valid integer.
        return AsmImmOperand(node.value.toInt())
    }

    override fun visitIdentifier(node: Identifier): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitComplementOperator(node: ComplementOperator): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitNegateOperator(node: NegateOperator): AsmNode {
        TODO("Not yet implemented")
    }

    override fun visitUnary(node: UnaryExpression): AsmNode {
        TODO("Not yet implemented")
    }
}
