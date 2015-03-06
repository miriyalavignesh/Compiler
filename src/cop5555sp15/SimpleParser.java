package cop5555sp15;

import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.ARROW;
import static cop5555sp15.TokenStream.Kind.ASSIGN;
import static cop5555sp15.TokenStream.Kind.AT;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.BL_FALSE;
import static cop5555sp15.TokenStream.Kind.BL_TRUE;
import static cop5555sp15.TokenStream.Kind.COLON;
import static cop5555sp15.TokenStream.Kind.COMMA;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.DOT;
import static cop5555sp15.TokenStream.Kind.EOF;
import static cop5555sp15.TokenStream.Kind.EQUAL;
import static cop5555sp15.TokenStream.Kind.GE;
import static cop5555sp15.TokenStream.Kind.GT;
import static cop5555sp15.TokenStream.Kind.IDENT;
import static cop5555sp15.TokenStream.Kind.INT_LIT;
import static cop5555sp15.TokenStream.Kind.KW_BOOLEAN;
import static cop5555sp15.TokenStream.Kind.KW_CLASS;
import static cop5555sp15.TokenStream.Kind.KW_DEF;
import static cop5555sp15.TokenStream.Kind.KW_ELSE;
import static cop5555sp15.TokenStream.Kind.KW_IF;
import static cop5555sp15.TokenStream.Kind.KW_IMPORT;
import static cop5555sp15.TokenStream.Kind.KW_INT;
import static cop5555sp15.TokenStream.Kind.KW_KEY;
import static cop5555sp15.TokenStream.Kind.KW_PRINT;
import static cop5555sp15.TokenStream.Kind.KW_RETURN;
import static cop5555sp15.TokenStream.Kind.KW_SIZE;
import static cop5555sp15.TokenStream.Kind.KW_STRING;
import static cop5555sp15.TokenStream.Kind.KW_VALUE;
import static cop5555sp15.TokenStream.Kind.KW_WHILE;
import static cop5555sp15.TokenStream.Kind.LCURLY;
import static cop5555sp15.TokenStream.Kind.LE;
import static cop5555sp15.TokenStream.Kind.LPAREN;
import static cop5555sp15.TokenStream.Kind.LSHIFT;
import static cop5555sp15.TokenStream.Kind.LSQUARE;
import static cop5555sp15.TokenStream.Kind.LT;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.MOD;
import static cop5555sp15.TokenStream.Kind.NOT;
import static cop5555sp15.TokenStream.Kind.NOTEQUAL;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.RANGE;
import static cop5555sp15.TokenStream.Kind.RCURLY;
import static cop5555sp15.TokenStream.Kind.RPAREN;
import static cop5555sp15.TokenStream.Kind.RSHIFT;
import static cop5555sp15.TokenStream.Kind.RSQUARE;
import static cop5555sp15.TokenStream.Kind.SEMICOLON;
import static cop5555sp15.TokenStream.Kind.STRING_LIT;
import static cop5555sp15.TokenStream.Kind.TIMES;

import java.util.ArrayList;
import java.util.Arrays;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;

public class SimpleParser {

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
	

	SimpleParser(TokenStream tokens) {
		this.tokens = tokens;
		t = tokens.nextToken();
		initialize();
	}

	private void initialize() {
		simpleType.addAll(Arrays.asList(KW_INT ,KW_BOOLEAN,KW_STRING));
		factor_prefix.addAll(Arrays.asList(IDENT,INT_LIT,BL_TRUE,BL_FALSE,STRING_LIT,LPAREN,NOT,MINUS,KW_SIZE,KW_VALUE,KW_KEY,AT,LCURLY));
		singleTokenFactorList.addAll(Arrays.asList(INT_LIT,BL_TRUE,BL_FALSE,STRING_LIT));
		
	}
	
	private Kind match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			consume();
			return kind;
		}
		throw new SyntaxException(t, kind);
	}

	private Kind match(Kind... kinds) throws SyntaxException {
		Kind kind = t.kind;
		if (isKind(kinds)) {
			consume();
			return kind;
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


	public void parse() throws SyntaxException {
		Program();
		match(EOF);
	}

	private void Program() throws SyntaxException {
		ImportList();
		match(KW_CLASS);
		match(IDENT);
		Block();
	}

	private void ImportList() throws SyntaxException {
		while(isKind(KW_IMPORT)){
			match(KW_IMPORT);
			match(IDENT);
			while(isKind(DOT)){
					match(DOT);
					match(IDENT);
			}
			match(SEMICOLON);
		}
	}

	private void Block() throws SyntaxException {
		match(LCURLY);
		while(!isKind(RCURLY)){
			if(isKind(KW_DEF)){
				declaration();
				match(SEMICOLON);
			}
			else{
				statement();
				match(SEMICOLON);
			}
		}
		match(RCURLY);
	}

	private void declaration() throws SyntaxException {
		match(KW_DEF);
		match(IDENT); 
		if(isKind(COLON)){
			varDec();
		}
		else if(isKind(ASSIGN)){
			closuredec();
		}
	}
	
	private void varDec() throws SyntaxException{
		match(COLON);
		type();
	}
	
	private void type() throws SyntaxException{
		if(simpleType.contains(t.kind)){
			simpleType();
		}
		else if(isKind(AT)){
			match(AT);
			if(isKind(AT)){
				keyvalueType();
			}
			else {
				listType();
			}
		}
		else {
			throw new SyntaxException(t, "expected one of type");
		}		
	}

	private void simpleType()  throws SyntaxException {
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			consume();
		}
		else{
			throw new SyntaxException(t, "expected one of simpletype");
		}
	}
	
	private void keyvalueType()  throws SyntaxException{
		match(AT);
		match(LSQUARE);
		simpleType();
		match(COLON);
		type();
		match(RSQUARE);
	}
	
	private void listType()  throws SyntaxException{
		match(LSQUARE);
		type();
		match(RSQUARE);
	}
	
	private void closuredec() throws SyntaxException {
		match(ASSIGN);
		closure();
	}
	
	private void closure() throws SyntaxException{
		match(LCURLY);
		formalArgList();
		match(ARROW);
		while(!isKind(RCURLY)){
			statement();
			match(SEMICOLON);
		}
		match(RCURLY);
	}
	
	private void formalArgList() throws SyntaxException{
		if(isKind(IDENT)){
			match(IDENT);
			varDec();
			while(isKind(COMMA)){
				match(COMMA);
				match(IDENT);
				varDec();	
			}
		}
	}
	
	private void statement() throws SyntaxException{
		if(isKind(KW_PRINT)){
			match(KW_PRINT);
			expression();
		}
		else if(isKind(KW_WHILE)){
			match(KW_WHILE);
			if(isKind(TIMES)){
				match(TIMES);
			}
			match(LPAREN);
			expression();
			if(isKind(RANGE)){
				match(RANGE);
				expression();
			}
			// merged all while loop conditions
			// removed range expression production as its used in only one place
			match(RPAREN);
			Block();
		}
		else if(isKind(KW_IF)){
			match(KW_IF);
			match(LPAREN);
			expression();
			match(RPAREN);
			Block();
			if(isKind(KW_ELSE)){
				match(KW_ELSE);
				Block();
			}
		}
		else if(isKind(MOD)){
			match(MOD);
			expression();
		}
		else if(isKind(KW_RETURN)){
			match(KW_RETURN);
			expression();
		}
		else if(isKind(IDENT)){
			lvalue();
			match(ASSIGN);
			expression();
		}
		
	}
	
	private void lvalue() throws SyntaxException {
		match(IDENT);
		if(isKind(LSQUARE)){
			match(LSQUARE);
			expression();
			match(RSQUARE);
		}
		
	}
	
	private void expression() throws SyntaxException {
		term();
		while(Arrays.asList(REL_OPS).contains(t.kind)){
			match(REL_OPS);
			term();
		}
		
	}
	
	private void term() throws SyntaxException {
		elem();
		while(Arrays.asList(WEAK_OPS).contains(t.kind)){
			match(WEAK_OPS);
			elem();
		}		
	}

	private void elem() throws SyntaxException {
		thing();
		while(Arrays.asList(STRONG_OPS).contains(t.kind)){
			match(STRONG_OPS);
			thing();
		}
		
	}
	
	private void thing() throws SyntaxException {
		factor();
		while(Arrays.asList(VERY_STRONG_OPS).contains(t.kind)){
			match(VERY_STRONG_OPS);
			factor();
		}
		
	}
	
	private void factor() throws SyntaxException {
		if(isKind(IDENT)){
			match(IDENT);
			if(isKind(LSQUARE)){
				match(LSQUARE);
				expression();
				match(RSQUARE);
			}
			else if(isKind(LPAREN)){
				closureevalexpression();
			}
		}
		else if(singleTokenFactorList.contains(t.kind)){
			consume();
		}
		else if(isKind(LPAREN)){
			match(LPAREN);
			expression();
			match(RPAREN);
		}
		else if(isKind(NOT) || isKind(MINUS)){
			consume();
			factor();
		}
		else if(isKind(KW_SIZE) || isKind(KW_KEY) | isKind(KW_VALUE)){
			consume();
			match(LPAREN);
			expression();
			match(RPAREN);
		}
		else if(isKind(LCURLY)){
			closure();
		}
		else if(isKind(AT)){
			consume();
			if(isKind(AT)){
				maplist();
			}
			else{
				list();
			}
		}
		else{
			throw new SyntaxException(t, "expected one of factor");
		}	
	}
	
	private void list() throws SyntaxException{
		match(LSQUARE);
		expressionlist();
		match(RSQUARE);
	}

	private void expressionlist() throws SyntaxException {
		if(factor_prefix.contains(t.kind)){
			expression();
			while(isKind(COMMA)){
				match(COMMA);
				expression();
			}
		}
		
	}
	
	private void closureevalexpression() throws SyntaxException {
		match(LPAREN);
		expressionlist();
		match(RPAREN);
		
	}
	
	private void maplist() throws SyntaxException {
		match(AT);
		match(LSQUARE);
		keyvaluelist();
		match(RSQUARE);
		
	}
	
	private void keyvaluelist() throws SyntaxException{
		if(factor_prefix.contains(t.kind)){
			keyvalueexpression();
			while(isKind(COMMA)){
				match(COMMA);
				keyvalueexpression();
			}
		}
	}
	
	private void keyvalueexpression() throws SyntaxException{
		expression();
		match(COLON);
		expression();
	}
	
}
