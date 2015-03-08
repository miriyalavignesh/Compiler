package cop5555sp15;

import static cop5555sp15.TokenStream.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import cop5555sp15.ast.*;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;
		Kind[] expected;
		String msg;

		SyntaxException(Token t, Kind expected) {
			this.t = t;
			msg = "";
			this.expected = new Kind[1];
			this.expected[0] = expected;

		}

		public SyntaxException(Token t, String msg) {
			this.t = t;
			this.msg = msg;
		}

		public SyntaxException(Token t, Kind[] expected) {
			this.t = t;
			msg = "";
			this.expected = expected;
		}

		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(" error at token ").append(t.toString()).append(" ")
					.append(msg);
			sb.append(". Expected: ");
			for (Kind kind : expected) {
				sb.append(kind).append(" ");
			}
			return sb.toString();
		}
	}

	TokenStream tokens;
	Token t;
	ArrayList<Kind> simpleType = new ArrayList<Kind>();
	ArrayList<Kind> factor_prefix = new ArrayList<Kind>();
	ArrayList<Kind> singleTokenFactorList = new ArrayList<Kind>();
	
	Parser(TokenStream tokens) {
		this.tokens = tokens;
		t = tokens.nextToken();
		initialize();
	}

	private void initialize() {
		simpleType.addAll(Arrays.asList(KW_INT ,KW_BOOLEAN,KW_STRING));
		factor_prefix.addAll(Arrays.asList(IDENT,INT_LIT,BL_TRUE,BL_FALSE,STRING_LIT,LPAREN,NOT,MINUS,KW_SIZE,KW_VALUE,KW_KEY,AT,LCURLY));
		singleTokenFactorList.addAll(Arrays.asList(INT_LIT,BL_TRUE,BL_FALSE,STRING_LIT));
		
	}
	
	private String match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			String text = t.getText();
			consume();
			return text;
		}
		throw new SyntaxException(t, kind);
	}

	private String match(Kind... kinds) throws SyntaxException {
		if (isKind(kinds)) {
			String text = t.getText();
			consume();
			return text;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		throw new SyntaxException(t, "expected one of " + sb.toString());
	}

	private boolean isKind(Kind kind) {
		return (t.kind == kind);
	}

	private void consume() {
		if (t.kind != EOF)
			t = tokens.nextToken();
	}

	private boolean isKind(Kind... kinds) {
		for (Kind kind : kinds) {
			if (t.kind == kind)
				return true;
		}
		return false;
	}

	//This is a convenient way to represent fixed sets of
	//token kinds.  You can pass these to isKind.
	static final Kind[] REL_OPS = { BAR, AND, EQUAL, NOTEQUAL, LT, GT, LE, GE };
	static final Kind[] WEAK_OPS = { PLUS, MINUS };
	static final Kind[] STRONG_OPS = { TIMES, DIV };
	static final Kind[] VERY_STRONG_OPS = { LSHIFT, RSHIFT };


	List<SyntaxException> exceptionList = new ArrayList<SyntaxException>();
	
	public Program parse() {
		Program p = null;
		try {
			p = ParseProgram();
			if (p != null)
				match(EOF);
		} catch (SyntaxException e) {
			exceptionList.add(e);
		}
		if (exceptionList.isEmpty()) return p;
		else return null;
	}

	private Program ParseProgram() throws SyntaxException {
		Token firstToken = t;
		List<QualifiedName> importList = ImportList();
		match(KW_CLASS);
		String classname = match(IDENT);
		Block b = parseBlock();
		return new Program(firstToken, importList, classname, b);
	}

	

	private List<QualifiedName> ImportList() throws SyntaxException {
		List<QualifiedName> impList = new ArrayList<>();
		while(isKind(KW_IMPORT)){
			Token firstToken = t;
			match(KW_IMPORT);
			StringBuffer name= new StringBuffer(match(IDENT));
			while(isKind(DOT)){
					match(DOT);
					name.append("/");
					name.append(match(IDENT));
			}
			impList.add(new QualifiedName(firstToken, name.toString()));
			match(SEMICOLON);
		}
		return impList;
	}

	private Block parseBlock() throws SyntaxException {
		List<BlockElem> blockElemList = new ArrayList<>();
		Token firstToken = t;
		match(LCURLY);
		while(!isKind(RCURLY)){
			if(isKind(KW_DEF)){
				blockElemList.add(declaration());
				match(SEMICOLON);
			}
			else{
				Statement s = statement();
				if(s!=null)
					blockElemList.add(s);
				match(SEMICOLON);
			}
					}
		match(RCURLY);
		return new Block(firstToken, blockElemList);
	}

	private Declaration declaration() throws SyntaxException {
		Token firstToken = t;
		match(KW_DEF);
		Token identToken = t;
		match(IDENT); 
		if(isKind(COLON)){
			return parseVarDec(firstToken, identToken);
		}
		else if(isKind(ASSIGN)){
			return closureDec(firstToken, identToken);
		}
		else {
			return new VarDec(firstToken, identToken, new UndeclaredType(firstToken));
		}
	}
	
	private VarDec parseVarDec(Token firstToken, Token identToken) throws SyntaxException{
		match(COLON);
		Type type = parseType();
		return new VarDec(firstToken, identToken, type);
	}
	
	private Type parseType() throws SyntaxException{
		if(simpleType.contains(t.kind)){
			return simpleType();
		}
		else if(isKind(AT)){
			match(AT);
			if(isKind(AT)){
				return parseKeyvalueType();
			}
			else {
				return listType();
			}
		}
		else {
			throw new SyntaxException(t, "expected one of type");
		}
	}

	private SimpleType simpleType()  throws SyntaxException {
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			Token type = t;
			consume();
			return new SimpleType(type, type);
		}
		else{
			throw new SyntaxException(t, "expected one of simpletype");
		}
	}
	
	private KeyValueType parseKeyvalueType()  throws SyntaxException{
		Token firstToken = t;
		match(AT);
		match(LSQUARE);
		SimpleType keyType = simpleType();
		match(COLON);
		Type valueType = parseType();
		match(RSQUARE);
		return new KeyValueType(firstToken, keyType, valueType);
	}
	
	private ListType listType()  throws SyntaxException{
		Token firstToken = t;
		match(LSQUARE);
		Type type = parseType();
		match(RSQUARE);
		return new ListType(firstToken, type);
	}
	
	private ClosureDec closureDec(Token firstToken,Token identToken) throws SyntaxException {
		match(ASSIGN);
		Closure c = closure();
		return new ClosureDec(firstToken, identToken, c);
	}
	
	private Closure closure() throws SyntaxException{
		Token firstToken = t;
		match(LCURLY);
		List<VarDec> argList = formalArgList();
		match(ARROW);
		List<Statement> statementList = new ArrayList<Statement>();
		while(!isKind(RCURLY)){
			Statement s= statement();
			if(s!=null)
				statementList.add(s);
			match(SEMICOLON);
		}
		match(RCURLY);
		return new Closure(firstToken, argList, statementList);
	}
	
	private List<VarDec> formalArgList() throws SyntaxException{
		List<VarDec> argList = new ArrayList<VarDec>();
		if(isKind(IDENT)){
			Token firstToken = t;
			match(IDENT);
			argList.add(parseVarDec(firstToken, firstToken));
			while(isKind(COMMA)){
				match(COMMA);
				match(IDENT);
				argList.add(parseVarDec(firstToken, firstToken));
			}
		}
		return argList;
	}
	
	private Statement statement() throws SyntaxException{
		Token firstToken = t;
		if(isKind(IDENT)){
			LValue l = lvalue();
			match(ASSIGN);
			Expression expression = parseExpression();
			return new AssignmentStatement(firstToken, l, expression);
		}
		else if(isKind(KW_PRINT)){
			match(KW_PRINT);
			Expression expression = parseExpression();
			return new PrintStatement(firstToken, expression);
		}
		else if(isKind(KW_WHILE)){
			boolean star = false;
			boolean range = false;
			match(KW_WHILE);
			if(isKind(TIMES)){
				match(TIMES);
				star = true;
			}
			
			match(LPAREN);
			Token rangeFirst = t;
			Expression expression = parseExpression();
			RangeExpression rangeExpression = null;
			if(star && isKind(RANGE)){
				match(RANGE);
				Expression upper = parseExpression();
				range = true;
				rangeExpression = new RangeExpression(rangeFirst, expression, upper);
			}
			match(RPAREN);
			Block block = parseBlock();
			if(star && range) {
				return new WhileRangeStatement(firstToken, rangeExpression, block);
			}
			else if(star) {
				return new WhileStarStatement(firstToken, expression, block);
			}
			else {
				return new WhileStatement(firstToken, expression, block);
			}
		}
		else if(isKind(KW_IF)){
			match(KW_IF);
			match(LPAREN);
			Expression expression = parseExpression();
			match(RPAREN);
			Block ifBlock = parseBlock();
			if(isKind(KW_ELSE)){
				match(KW_ELSE);
				Block elseBlock = parseBlock();
				return new IfElseStatement(firstToken, expression, ifBlock, elseBlock);
			}
			else {
				return new IfStatement(firstToken, expression, ifBlock);
			}
		}
		else if(isKind(MOD)){
			match(MOD);
			Expression expression = parseExpression();
			return new ExpressionStatement(firstToken, expression);
		}
		else if(isKind(KW_RETURN)){
			match(KW_RETURN);
			Expression expression = parseExpression();
			return new ReturnStatement(firstToken, expression);
		}
		return null;
	}
	
	private LValue lvalue() throws SyntaxException {
		Token firstToken = t;
		match(IDENT);
		if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression expression = parseExpression();
			match(RSQUARE);
			return new ExpressionLValue(firstToken, firstToken, expression);
		}
		else {
			return new IdentLValue(firstToken, firstToken);
		}
	}
	
	private Expression parseExpression() throws SyntaxException {
		Token firstToken = t;
		Expression expression0 = term();
		Expression expression1 = null;
		Token op = null;
		while(Arrays.asList(REL_OPS).contains(t.kind)){
			op = t; 
			match(REL_OPS);
			expression1 = term();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	private Expression term() throws SyntaxException {
		Token firstToken = t;
		Expression expression0 = elem();
		Expression expression1 = null;
		Token op = null;
		while(Arrays.asList(WEAK_OPS).contains(t.kind)){
			op = t;
			match(WEAK_OPS);
			expression1 = elem();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;

	}

	private Expression elem() throws SyntaxException {
		Token firstToken = t;
		Expression expression0 = thing();
		Expression expression1 = null;
		Token op = null;
		while(Arrays.asList(STRONG_OPS).contains(t.kind)){
			op = t;
			match(STRONG_OPS);
			expression1 = thing();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	private Expression thing() throws SyntaxException {
		Token firstToken = t;
		Expression expression0 = factor();
		Expression expression1 = null;
		Token op = null;
		while(Arrays.asList(VERY_STRONG_OPS).contains(t.kind)){
			op = t;
			match(VERY_STRONG_OPS);
			expression1 = factor();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	private Expression factor() throws SyntaxException {
		Token firstToken = t;
		if(isKind(IDENT)){
			match(IDENT);
			if(isKind(LSQUARE)){
				match(LSQUARE);
				Expression expression = parseExpression();
				match(RSQUARE);
				return new ListOrMapElemExpression(firstToken, firstToken, expression);
			}
			else if(isKind(LPAREN)){
				return closureevalexpression(firstToken);
			}
			else {
				return new IdentExpression(firstToken, firstToken);
			}
		}
		else if(singleTokenFactorList.contains(t.kind)){
			Expression e = null;
			if(isKind(INT_LIT)) {
				int value = t.getIntVal();
				e = new IntLitExpression(firstToken, value);
			}
			else if(isKind(BL_TRUE) || isKind(BL_FALSE)) {
				boolean value = t.getBooleanVal(); 
				e = new BooleanLitExpression(firstToken, value);
			}
			else {
				String value = t.getText();
				e = new StringLitExpression(firstToken, value);
			}
			consume();
			return e;
		}
		else if(isKind(LPAREN)){
			match(LPAREN);
			Expression e = parseExpression();
			match(RPAREN);
			return e;
		}
		else if(isKind(NOT) || isKind(MINUS)){
			Token op = t;
			consume();
			Expression expression = factor();
			return new UnaryExpression(firstToken, op, expression);
		}
		else if(isKind(KW_SIZE)){
			consume();
			match(LPAREN);
			Expression expression = parseExpression();
			match(RPAREN);
			return new SizeExpression(firstToken, expression);
		}
		else if(isKind(KW_KEY)){
			consume();
			match(LPAREN);
			Expression expression = parseExpression();
			match(RPAREN);
			return new KeyExpression(firstToken, expression);
		}
		else if(isKind(KW_VALUE)){
			consume();
			match(LPAREN);
			Expression expression = parseExpression();
			match(RPAREN);
			return new ValueExpression(firstToken, expression);
		}
		else if(isKind(LCURLY)){
			Closure c =closure();
			return new ClosureExpression(firstToken, c);
		}
		else if(isKind(AT)){
			consume();
			if(isKind(AT)){
				return maplist(firstToken);
			}
			else{
				return list(firstToken);
			}
		}
		else{
			throw new SyntaxException(t, "expected one of factor");
		}
	}
	
	private ListExpression list(Token firstToken) throws SyntaxException{
		match(LSQUARE);
		List<Expression> expressionList = expressionlist();
		match(RSQUARE);
		return new ListExpression(firstToken, expressionList);
	}

	private List<Expression> expressionlist() throws SyntaxException {
		List<Expression> expressions = new ArrayList<>();
		if(factor_prefix.contains(t.kind)){
			expressions.add(parseExpression());
			while(isKind(COMMA)){
				match(COMMA);
				expressions.add(parseExpression());
			}
		}
		return expressions;
	}
	
	private ClosureEvalExpression closureevalexpression(Token firstToken) throws SyntaxException {
		match(LPAREN);
		List<Expression> expressionList = expressionlist();
		match(RPAREN);
		return new ClosureEvalExpression(firstToken, firstToken, expressionList);
	}
	
	private MapListExpression maplist(Token firstToken) throws SyntaxException {
		match(AT);
		match(LSQUARE);
		List<KeyValueExpression> mapList = keyvaluelist();
		match(RSQUARE);
		return new MapListExpression(firstToken, mapList);
	}
	
	private List<KeyValueExpression> keyvaluelist() throws SyntaxException{
		List<KeyValueExpression> mapList = new ArrayList<>();
		if(factor_prefix.contains(t.kind)){
			mapList.add(keyvalueexpression());
			while(isKind(COMMA)){
				match(COMMA);
				mapList.add(keyvalueexpression());
			}
		}
		return mapList;
	}
	
	private KeyValueExpression keyvalueexpression() throws SyntaxException{
		Token firstToken = t;
		Expression key = parseExpression();
		match(COLON);
		Expression value = parseExpression();
		return new KeyValueExpression(firstToken, key, value);
	}
	
}
