package cop5555sp15.ast;

import org.objectweb.asm.*;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.TIMES;
import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.MOD;

import cop5555sp15.TypeConstants;

public class CodeGenVisitor implements ASTVisitor, Opcodes, TypeConstants {

	ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	// Because we used the COMPUTE_FRAMES flag, we do not need to
	// insert the mv.visitFrame calls that you will see in some of the
	// asmifier examples. ASM will insert those for us.
	// FYI, the purpose of those instructions is to provide information
	// about what is on the stack just before each branch target in order
	// to speed up class verification.
	FieldVisitor fv;
	String className;
	String classDescriptor;

	// This class holds all attributes that need to be passed downwards as the
	// AST is traversed. Initially, it only holds the current MethodVisitor.
	// Later, we may add more attributes.
	static class InheritedAttributes {
		public InheritedAttributes(MethodVisitor mv) {
			super();
			this.mv = mv;
		}

		MethodVisitor mv;
	}

	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		Label l1 = new Label();
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 0);
		
		if(assignmentStatement.lvalue instanceof ExpressionLValue) {
			mv.visitFieldInsn(GETFIELD, className, assignmentStatement.lvalue.firstToken.getText(), "Ljava/util/ArrayList;");
			ExpressionLValue expressionLValue = (ExpressionLValue)assignmentStatement.lvalue;
			expressionLValue.expression.visit(this, arg);
		}
			assignmentStatement.expression.visit(this, arg);
		//}
		//assignmentStatement.lvalue.visit(this, arg); // called only for IdentValue
		if(assignmentStatement.lvalue instanceof ExpressionLValue) {
			if(assignmentStatement.lvalue.type.equals(intType))
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			else if(assignmentStatement.lvalue.type.equals(booleanType)) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			}
			
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(ILjava/lang/Object;)V", false);
			//mv.visitInsn(POP);
			//mv.visitFieldInsn(PUTFIELD, className, assignmentStatement.lvalue.firstToken.getText(), "Ljava/util/ArrayList;");
			
		}else {
			if(assignmentStatement.expression.expressionType.contains(emptyList))
				mv.visitTypeInsn(CHECKCAST, "java/util/ArrayList");
			mv.visitFieldInsn(PUTFIELD, className, assignmentStatement.lvalue.firstToken.getText(), assignmentStatement.lvalue.type);
		}
		return null;
	}

	public void compareBinaryExpression(BinaryExpression binaryExpression,
			Object arg,MethodVisitor mv,int opCode) throws Exception{
		binaryExpression.expression0.visit(this, arg);
		Label l1 = new Label();
		binaryExpression.expression1.visit(this,arg);
		mv.visitJumpInsn(opCode, l1);
		mv.visitInsn(ICONST_1);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		mv.visitInsn(ICONST_0);
		mv.visitLabel(l2);
		
	}
	
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		if(binaryExpression.expressionType.equals(intType)) {
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			if(binaryExpression.op.kind.equals(PLUS)) {
				mv.visitInsn(IADD);
			}
			else if(binaryExpression.op.kind.equals(MINUS)){
				mv.visitInsn(ISUB);
			}
			else if(binaryExpression.op.kind.equals(TIMES)){
				mv.visitInsn(IMUL);
			}
			else if(binaryExpression.op.kind.equals(DIV)){
				mv.visitInsn(IDIV);
			}
			else if(binaryExpression.op.kind.equals(MOD)){
				mv.visitInsn(IREM);
			}
		}
		else if(binaryExpression.expressionType.equals(stringType)) {
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			binaryExpression.expression0.visit(this, arg);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			binaryExpression.expression1.visit(this, arg);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
		}
		else if(binaryExpression.expressionType.equals(booleanType)) {
			// int (== | != | < | <= | >= | >) int     -> boolean
			// *      string (== | !=) string          -> boolean
		/*	BAR, // |
			AND, // &
			EQUAL, // ==
			NOTEQUAL, // !=
			LT, // <
			GT, // >
			LE, // <=
			GE, // >=
			PLUS, // +
			MINUS, // -
			TIMES, // *
			DIV, // /
			MOD, // %
			NOT, // !
			LSHIFT, // <<
			RSHIFT, // >>
			ARROW, // ->
		*/
			//Kind op = binaryExpression.op.kind;
			switch (binaryExpression.op.kind) {
				case AND: {
					binaryExpression.expression0.visit(this, arg);
					Label l1 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					binaryExpression.expression1.visit(this,arg);
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitInsn(ICONST_1);
					Label l2 = new Label();
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
					break;
				}
				case BAR: {
					binaryExpression.expression0.visit(this, arg);
					Label l1 = new Label();
					mv.visitJumpInsn(IFNE, l1);
					binaryExpression.expression1.visit(this,arg);
					mv.visitJumpInsn(IFNE, l1);
					mv.visitInsn(ICONST_0);
					Label l2 = new Label();
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
					break;
				}
				case EQUAL: {
					if(binaryExpression.expression0.expressionType.equals(stringType)) {
						compareBinaryExpression(binaryExpression, arg, mv, IF_ACMPNE);
					}
					else {
						compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPNE);
					}
					break;
				}
				case NOTEQUAL: {
					if(binaryExpression.expression0.expressionType.equals(stringType)) {
						compareBinaryExpression(binaryExpression, arg, mv, IF_ACMPEQ);
					}
					else {
						compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPEQ);
					}
					break;
				}
				case LT: {
					compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPGE);
					break;
				}
				case GT: {
					compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPLE);
					break;
				}
				case LE: {
					compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPGT);
					break;
				}
				case GE: {
					compareBinaryExpression(binaryExpression, arg, mv, IF_ICMPLT);
					break;
				}
			}
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
		// first statement
		// of all visit
		// methods that
		// generate
		// instructions
		mv.visitLdcInsn(booleanLitExpression.value);
		return null;
	}

	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureDec(ClosureDec closureDeclaration, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitVarInsn(ALOAD, 0);
		//System.out.println(className+"--"+identExpression.identToken.getText()+identExpression.expressionType);
		mv.visitFieldInsn(GETFIELD, className, identExpression.identToken.getText(), identExpression.expressionType);
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg)
			throws Exception {
		 MethodVisitor mv = ((InheritedAttributes) arg).mv;
		 mv.visitFieldInsn(PUTFIELD, className, identLValue.firstToken.getText(), identLValue.type);
		 return null;
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		ifElseStatement.expression.visit(this, arg);
		Label l3 = new Label();
		mv.visitJumpInsn(IFEQ, l3);
		ifElseStatement.ifBlock.visit(this, arg);
		Label l6 = new Label();
		mv.visitJumpInsn(GOTO, l6);
		mv.visitLabel(l3);
		ifElseStatement.elseBlock.visit(this, arg);
		mv.visitLabel(l6);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		ifStatement.expression.visit(this, arg);
		Label l3 = new Label();
		mv.visitJumpInsn(IFEQ, l3);
		//Label l4 = new Label();
		//mv.visitLabel(l4);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(l3);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitTypeInsn(NEW, "java/util/ArrayList");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
		
		//mv.visitFieldInsn(PUTFIELD, className, assignmentStatement.lvalue.firstToken.getText(), assignmentStatement.expression.expressionType);
		int length = listExpression.expressionList.size();
		for(int i=0;i<length;i++) {
			mv.visitInsn(DUP);
			Expression exp = listExpression.expressionList.get(i);
			exp.visit(this, arg);
			if(exp.expressionType.equals(intType)) {
				//mv.visitVarInsn(ALOAD, 1);
			
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
			    mv.visitInsn(POP);
			   // if(i != length-1)
			    //	mv.visitInsn(DUP);
			}
			else if(exp.expressionType.equals(booleanType)) {
				//mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
			    mv.visitInsn(POP);
			   // if(i != length-1)
			   // mv.visitInsn(DUP);
			}
			else {
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
			    mv.visitInsn(POP);
				//System.out.println("already an object");
			}
		}
		return null;
	}

	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, listOrMapElemExpression.identToken.getText(), "Ljava/util/ArrayList;");
		listOrMapElemExpression.expression.visit(this, arg);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "get", "(I)Ljava/lang/Object;", false);
		
		return null;
	}

	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(printStatement.firstToken.getLineNumber(), l0);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		printStatement.expression.visit(this, arg); // adds code to leave value
													// of expression on top of
													// stack.
													// Unless there is a good
													// reason to do otherwise,
													// pass arg down the tree
		String etype = printStatement.expression.getType();
		if (etype.equals("I") || etype.equals("Z")
				|| etype.equals("Ljava/lang/String;") || etype.equals("Ljava/lang/Object;")) {
			String desc = "(" + etype + ")V";
			//String desc = "(Ljava/lang/Object;)V";
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					desc, false);
		} else
			throw new UnsupportedOperationException(
					"printing list or map not yet implemented");
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		className = program.JVMName;
		classDescriptor = 'L' + className + ';';
		cw.visit(52, // version
				ACC_PUBLIC + ACC_SUPER, // access codes
				className, // fully qualified classname
				null, // signature
				"java/lang/Object", // superclass
				new String[] { "cop5555sp15/Codelet" } // implemented interfaces
		);
		cw.visitSource(null, null); // maybe replace first argument with source
									// file name

		// create init method
		{
			MethodVisitor mv;
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			//mv.visitLineNumber(3, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		// generate the execute method
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "execute", // name of top
																	// level
																	// method
				"()V", // descriptor: this method is parameterless with no
						// return value
				null, // signature.  This is null for us, it has to do with generic types
				null // array of strings containing exceptions
				);
		mv.visitCode();
		Label lbeg = new Label();
		mv.visitLabel(lbeg);
		mv.visitLineNumber(program.firstToken.lineNumber, lbeg);
		program.block.visit(this, new InheritedAttributes(mv));
		mv.visitInsn(RETURN);
		Label lend = new Label();
		mv.visitLabel(lend);
		mv.visitLocalVariable("this", classDescriptor, null, lbeg, lend, 0);
		mv.visitMaxs(0, 0);  //this is required just before the end of a method. 
		                     //It causes asm to calculate information about the
		                     //stack usage of this method.
		mv.visitEnd();

		
		cw.visitEnd();
		return cw.toByteArray();
	}

	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		sizeExpression.expression.visit(this, arg);
		if(sizeExpression.expression.expressionType.contains(emptyList)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "size", "()I", false);
		}
		return null;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
		// first statement
		// of all visit
		// methods that
		// generate
		// instructions
		mv.visitLdcInsn(stringLitExpression.value);
		return null;
	}

	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		unaryExpression.expression.visit(this, arg);
		if(unaryExpression.expressionType.equals(intType)) {
			mv.visitInsn(INEG);
		}
		else if(unaryExpression.expressionType.equals(booleanType)) {
			Label l5 = new Label();
			mv.visitJumpInsn(IFEQ, l5);
			mv.visitInsn(ICONST_0);
			Label l6 = new Label();
			mv.visitJumpInsn(GOTO, l6);
			mv.visitLabel(l5);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l6);
		}
		return null;
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		 if(varDec.type instanceof SimpleType){
			 fv = cw.visitField(ACC_PUBLIC, varDec.identToken.getText(), varDec.type.getJVMType(), null, null);
		 }
		 else if(varDec.type instanceof ListType) {
			 fv = cw.visitField(ACC_PUBLIC, varDec.identToken.getText(), "Ljava/util/ArrayList;", null, null);
		 }
		 fv.visitEnd();
		 return null;
	}

	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitWhileStarStatement(WhileStarStatement whileStarStatment,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		/***
		 * Label l3 = new Label();
mv.visitJumpInsn(GOTO, l3);
Label l4 = new Label();
mv.visitLabel(l4);
blockvisit
mv.visitLabel(l3);
mv.visitLineNumber(14, l3);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ILOAD, 2);
mv.visitJumpInsn(IFEQ, l4);
		 */
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		Label l4 = new Label();
		mv.visitLabel(l4);
		whileStatement.block.visit(this, arg);
		mv.visitLabel(l3);
		whileStatement.expression.visit(this, arg);
		mv.visitJumpInsn(IFNE, l4);
		return null;
	}

	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

}
