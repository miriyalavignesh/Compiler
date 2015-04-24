package cop5555sp15.ast;

import static cop5555sp15.TokenStream.Kind.*;

import java.util.Arrays;

import cop5555sp15.TypeConstants;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.symbolTable.SymbolTable;

public class TypeCheckVisitor implements ASTVisitor, TypeConstants {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		ASTNode node;

		public TypeCheckException(String message, ASTNode node) {
			super(node.firstToken.lineNumber + ":" + message);
			this.node = node;
		}
	}

	SymbolTable symbolTable;

	public TypeCheckVisitor(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	boolean check(boolean condition, String message, ASTNode node)
			throws TypeCheckException {
		if (condition)
			return true;
		throw new TypeCheckException(message, node);
	}

	/**
	 * Ensure that types on left and right hand side are compatible.
	 */
	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		assignmentStatement.expression.visit(this, arg);
		assignmentStatement.lvalue.visit(this, arg);
	    if(assignmentStatement.expression.expressionType.equals(assignmentStatement.lvalue.type)) {
			assignmentStatement.lvalue.type = assignmentStatement.expression.expressionType;
		}
		else {
			throw new TypeCheckException("Incompatible Assignment Statement Lvalue and expression are different types", assignmentStatement);
		}
		
		return null;
	}

	/**
	 * Ensure that both types are the same, save and return the result type
	 */
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		binaryExpression.expression0.visit(this, arg);
		binaryExpression.expression1.visit(this, arg);/*
		int (+ | - | * | /) int 				-> int
		 *      string + string         				-> string
		 *      int (== | != | < | <= | >= | >) int     -> boolean
		 *      string (== | !=) string       			-> boolean*/
		final Kind[] ARIT_OPS = { PLUS, MINUS, TIMES, DIV , MOD};
		final Kind[] CMP_OPS = {EQUAL,NOTEQUAL, LT, LE, GE ,GT};
		final Kind[] BOOL_OPS = {BAR,AND,EQUAL,NOTEQUAL};
		//whitelisting only valid operations
		if(binaryExpression.expression0.expressionType.equals(binaryExpression.expression1.expressionType)) {
			if(binaryExpression.expression1.expressionType.equals(intType)) {
				if(Arrays.asList(ARIT_OPS).contains(binaryExpression.op.kind)) {
					binaryExpression.expressionType = binaryExpression.expression1.expressionType;
				}
				else if(Arrays.asList(CMP_OPS).contains(binaryExpression.op.kind)){
					binaryExpression.expressionType = booleanType;
				}
				else {
					throw new TypeCheckException("Operator not supported for int", binaryExpression);
				}
			}
			else if(binaryExpression.expression1.expressionType.equals(stringType)) {
				if(binaryExpression.op.kind.equals(PLUS))
					binaryExpression.expressionType = binaryExpression.expression1.expressionType;
				else if(binaryExpression.op.kind.equals(EQUAL) || binaryExpression.op.kind.equals(NOTEQUAL)) {
					binaryExpression.expressionType = booleanType;
				}
				else {
					throw new TypeCheckException("Operator not supported for string", binaryExpression);
				}
			}
			else {
				if(Arrays.asList(BOOL_OPS).contains(binaryExpression.op.kind)){
					binaryExpression.expressionType = booleanType;
				}
				else {
					throw new TypeCheckException("Operator not supported for boolean exp", binaryExpression);
				}
			}
		}
		else {
			throw new TypeCheckException("binaryExpression with different types",
					binaryExpression);
		}
		return null;
	}

	/**
	 * Blocks define scopes. Check that the scope nesting level is the same at
	 * the end as at the beginning of block
	 */
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		int numScopes = symbolTable.enterScope();
		// visit children
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		int numScopesExit = symbolTable.leaveScope();
		check(numScopesExit > 0 && numScopesExit == numScopes,
				"unbalanced scopes", block);
		return null;
	}

	/**
	 * Sets the expressionType to booleanType and returns it
	 * 
	 * @param booleanLitExpression
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		booleanLitExpression.setType(booleanType);
		return booleanType;
	}

	/**
	 * A closure defines a new scope Visit all the declarations in the
	 * formalArgList, and all the statements in the statementList construct and
	 * set the JVMType, the argType array, and the result type
	 * 
	 * @param closure
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Make sure that the name has not already been declared and insert in
	 * symbol table. Visit the closure
	 */
	@Override
	public Object visitClosureDec(ClosureDec closureDec, Object arg) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that the given name is declared as a closure Check the argument
	 * types The type is the return type of the closure
	 */
	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		String ident = expressionLValue.firstToken.getText();
		if(symbolTable.lookup(ident)!= null) {
			expressionLValue.expression.visit(this, arg);
			VarDec varDec = (VarDec)symbolTable.lookup(ident);
			if(varDec.type instanceof ListType){
					ListType lType = (ListType) varDec.type;
					expressionLValue.type = lType.type.getJVMType();
			}
			
		}
		return null;
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that name has been declared in scope Get its type from the
	 * declaration.
	 * 
	 */
	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		//System.out.println(identExpression.identToken.getText()+"  ---");
		if(symbolTable.lookup(identExpression.identToken.getText()) == null) {
			throw new TypeCheckException("Identifier not declared in current scope", identExpression);
		}
		else {
			VarDec varDec = (VarDec)symbolTable.lookup(identExpression.identToken.getText());
			if(varDec.type instanceof ListType) {
				identExpression.expressionType = "Ljava/util/ArrayList;";
			}
			else {
				identExpression.expressionType = varDec.type.getJVMType();
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg)
			throws Exception {
		String ident = identLValue.firstToken.getText();
		if(symbolTable.lookup(ident)!= null) {
			VarDec varDec = (VarDec)symbolTable.lookup(ident);
			identLValue.type = (varDec.type instanceof ListType) ? "Ljava/util/ArrayList;" : varDec.type.getJVMType();
		}
		else {
			throw new TypeCheckException("Ident Lvalue not defined", identLValue);
		}
		return null;
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		ifElseStatement.expression.visit(this, arg);
		if(!ifElseStatement.expression.expressionType.equals(booleanType)) {
			throw new TypeCheckException("Expression inside If is not boolean", ifElseStatement);
		}
		ifElseStatement.ifBlock.visit(this, arg);
		ifElseStatement.elseBlock.visit(this, arg);
		return null;
	}

	/**
	 * expression type is boolean
	 */
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		ifStatement.expression.visit(this, arg);
		if(!ifStatement.expression.expressionType.equals(booleanType)) {
			throw new TypeCheckException("Expression inside If is not boolean", ifStatement);
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	/**
	 * expression type is int
	 */
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		intLitExpression.setType(intType);
		return intType;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	// visit the expressions (children) and ensure they are the same type
	// the return type is "Ljava/util/ArrayList<"+type0+">;" where type0 is the
	// type of elements in the list
	// this should handle lists of lists, and empty list. An empty list is
	// indicated by "Ljava/util/ArrayList;".
	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		String listType = "";
		for(Expression exp:listExpression.expressionList ) {
			exp.visit(this, arg);
			if(listType.isEmpty()) listType = exp.expressionType;
			if(!exp.expressionType.equals(listType)) {
				throw new TypeCheckException("expressions in list are of different types",listExpression);
			}
		}
		//listExpression.expressionType = listType.isEmpty()? "Ljava/util/ArrayList;" : "Ljava/util/ArrayList<"+listType+">;";
		listExpression.expressionType = "Ljava/util/ArrayList;";
		//System.out.println(listExpression.expressionType);
		return null;
	}

	/** gets the type from the enclosed expression */
	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		Declaration dec = symbolTable.lookup(listOrMapElemExpression.identToken.getText());
		if(dec == null)
			 throw new TypeCheckException("Undefined list reference", listOrMapElemExpression);
		VarDec varDec = (VarDec) dec;
		if(varDec.type instanceof ListType) {
			listOrMapElemExpression.expression.visit(this, arg);
			if(!listOrMapElemExpression.expression.expressionType.equals(intType)) {
				throw new TypeCheckException("List Index is not of type int", listOrMapElemExpression);
			}
			
			
					ListType lType = (ListType) varDec.type;
			if(lType.type.getJVMType().contains("Ljava/util/List")){
				listOrMapElemExpression.expressionType =  "Ljava/util/ArrayList;";
			}else {
				listOrMapElemExpression.expressionType = "Ljava/lang/Object;";
			}
			
		}
		return null;
	}

	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		printStatement.expression.visit(this, null);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		if (arg == null) {
			program.JVMName = program.name;
		} else {
			program.JVMName = arg + "/" + program.name;
		}
		// ignore the import statement
		if (!symbolTable.insert(program.name, null)) {
			throw new TypeCheckException("name already in symbol table",
					program);
		}
		program.block.visit(this, true);
		return null;
	}

	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks that both expressions have type int.
	 * 
	 * Note that in spite of the name, this is not in the Expression type
	 * hierarchy.
	 */
	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	// nothing to do here
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		sizeExpression.expression.visit(this, arg);
		if(!sizeExpression.expression.expressionType.contains(emptyList) && !sizeExpression.expression.expressionType.contains(emptyMap)) {
			//System.out.println(sizeExpression.expression.expressionType);
			throw new TypeCheckException("Expected expression list or map", sizeExpression);
		}
		sizeExpression.expressionType = intType;
		return null;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		stringLitExpression.setType(stringType);
		return stringType; 
	}

	/**
	 * if ! and boolean, then boolean else if - and int, then int else error
	 */
	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {
		unaryExpression.expression.visit(this, arg);
		//System.out.println(unaryExpression.op.getText());
		if(unaryExpression.op.kind.equals(MINUS)) {
			if(unaryExpression.expression.expressionType.equals(intType)) {
			unaryExpression.expressionType = unaryExpression.expression.expressionType; 
			}
		}
		else if(unaryExpression.op.kind.equals(NOT)) {
			if(unaryExpression.expression.expressionType.equals(booleanType)) {
				unaryExpression.expressionType = unaryExpression.expression.expressionType; 
			}
		}
		else {
			//System.out.println(unaryExpression.op);
			throw new TypeCheckException("not an Unary Operator invalid expression", unaryExpression);
		}
		return null;
	}

	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"undeclared types not supported");
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * check that this variable has not already been declared in the same scope.
	 */
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		if (!symbolTable.insert(varDec.identToken.getText(), varDec)) {
			throw new TypeCheckException("name already in symbol table",
					varDec);
		}
		return null;
	}

	/**
	 * All checking will be done in the children since grammar ensures that the
	 * rangeExpression is a rangeExpression.
	 */
	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");

	}

	@Override
	public Object visitWhileStarStatement(
			WhileStarStatement whileStarStatement, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		whileStatement.expression.visit(this, arg);
		if(!whileStatement.expression.expressionType.equals(booleanType)) {
			throw new TypeCheckException("Expression inside while guard is not boolean", whileStatement);
		}
		whileStatement.block.visit(this, arg);
		return null;
	}

}
