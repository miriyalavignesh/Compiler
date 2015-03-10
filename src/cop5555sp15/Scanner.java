package cop5555sp15;

import java.io.IOException;
import java.util.HashMap;

import cop5555sp15.TokenStream;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;


public class Scanner {

	private enum State {
		START, IDENT_PART, DIGITS, COMMENT, STRING_LIT
	}
	private State state;
	
	private static HashMap<String, Kind> keywords;
	//local references to TokenStream objects for convenience
	final TokenStream stream;  //set in constructor

	private int index; // points to the next char to process during scanning, or if none, past the end of the array

	private Character ch;
	private int linenumber;

	private void populateKeywords(){
		keywords = new HashMap<String,Kind>();
		keywords.put("int", KW_INT);
		keywords.put("string", KW_STRING);
		keywords.put("boolean", KW_BOOLEAN);
		keywords.put("import", KW_IMPORT);
		keywords.put("class", KW_CLASS);
		keywords.put("def", KW_DEF);
		keywords.put("while", KW_WHILE);
		keywords.put("if", KW_IF);
		keywords.put("else", KW_ELSE);
		keywords.put("return", KW_RETURN);
		keywords.put("print", KW_PRINT);
		keywords.put("size",KW_SIZE);
		keywords.put("value", KW_VALUE);
		keywords.put("key",KW_KEY);
	}
	
	private Kind getKeywordType(String identifier) {
		if(isKeyword(identifier)) {
			return keywords.get(identifier);
		}
		else if("true".equals(identifier)) {
			return BL_TRUE;
		}
		else if("false".equals(identifier)) {
			return BL_FALSE;
		}
		else if("null".equals(identifier)) {
			return NL_NULL;
		}
		return IDENT;
	}
	
	private boolean isKeyword(String key) {
		return keywords.get(key)!= null?true:false;
	}
	
	private void  getch() throws IOException {
		if(index < stream.inputChars.length) {
			ch = stream.inputChars[index];
			index++;
		}
		else {
			ch = null;
		}
		return;
	}
	public Token next() throws IOException, NumberFormatException {
		state = State.START;
		Token t = null;
		int begOffset = index;
		do {
			getch();
			switch (state) { 
				/*in each state, check the next character.
                             either create a token or change state
				 */
				case START:
					if(ch == null) {
						t = stream.new Token(EOF, begOffset, index,linenumber);
						break;
					}
					switch (ch) {
					case '=':
						getch();
						if(ch == null) {
							t = stream.new Token(ASSIGN, begOffset, index,linenumber);
							break;
						}
						if (ch == '=') {
							t = stream.new Token(EQUAL, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(ASSIGN, begOffset, index,linenumber);
						}
						break;
					case '>':
						getch();
						if(ch == null) {
							t = stream.new Token(GT, begOffset, index,linenumber);
							break;
						}
						if (ch == '=') {
							t = stream.new Token(GE, begOffset, index,linenumber);
						}
						else if (ch == '>') {
							t = stream.new Token(RSHIFT, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(GT, begOffset, index,linenumber);
						}
						break;
					case '<':
						getch();
						if(ch == null) {
							t = stream.new Token(LT, begOffset, index,linenumber);
							break;
						}
						if (ch == '=') {
							t = stream.new Token(LE, begOffset, index,linenumber);
						}
						else if (ch == '<') {
							t = stream.new Token(LSHIFT, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(LT, begOffset, index,linenumber);
						}
						break;
					case '!':
						getch();
						if(ch == null) {
							t = stream.new Token(NOT, begOffset, index,linenumber);
							break;
						}
						if (ch == '=') {
							t = stream.new Token(NOTEQUAL, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(NOT, begOffset, index,linenumber);
						}
						break;
					case '-':
						getch();
						if(ch == null) {
							t = stream.new Token(MINUS, begOffset, index,linenumber);
							break;
						}
						if (ch == '>') {
							t = stream.new Token(ARROW, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(MINUS, begOffset, index,linenumber);
						}
						break;
					case '/':
						getch();
						if(ch == null) {
							t = stream.new Token(DIV, begOffset, index,linenumber);
							break;
						}
						if (ch == '*') {
							state = State.COMMENT;
						}
						else {
							index--;
							t = stream.new Token(DIV, begOffset, index,linenumber);
						}
						break;
					case '.':
						getch();
						if(ch == null) {
							t = stream.new Token(DOT, begOffset, index,linenumber);
							break;
						}
						if (ch == '.') {
							t = stream.new Token(RANGE, begOffset, index,linenumber);
						}
						else {
							index--;
							t = stream.new Token(DOT, begOffset, index,linenumber);
						}
						break;
					case '"':
						state = State.STRING_LIT;
						break;
					case '|':
						t = stream.new Token(BAR, begOffset, index,linenumber);
						break;
					case '&':
						t = stream.new Token(AND, begOffset, index,linenumber);
						break;
					case '*':
						t = stream.new Token(TIMES, begOffset, index,linenumber);
						break;
					case '+':
						t = stream.new Token(PLUS, begOffset, index,linenumber);
						break;
					case '%':
						t = stream.new Token(MOD, begOffset, index,linenumber);
						break;
					case '@':
						t = stream.new Token(AT, begOffset, index,linenumber);
						break;
					case ';':
						t = stream.new Token(SEMICOLON, begOffset, index,linenumber);
						break;
					case ',':
						t = stream.new Token(COMMA, begOffset, index,linenumber);
						break;
					case '(':
						t = stream.new Token(LPAREN, begOffset, index,linenumber);
						break;
					case ')':
						t = stream.new Token(RPAREN, begOffset, index,linenumber);
						break;
					case '[':
						t = stream.new Token(LSQUARE, begOffset, index,linenumber);
						break;
					case ']':
						t = stream.new Token(RSQUARE, begOffset, index,linenumber);
						break;
					case '{':
						t = stream.new Token(LCURLY, begOffset, index,linenumber);
						break;
					case '}':
						t = stream.new Token(RCURLY, begOffset, index,linenumber);
						break;
					case ':':
						t = stream.new Token(COLON, begOffset, index,linenumber);
						break;
					case '?':
						t = stream.new Token(QUESTION, begOffset, index,linenumber);
						break;
					case '0':
						t = stream.new Token(INT_LIT, begOffset, index,linenumber);
						break;
					default:
						if (Character.isDigit(ch)) {
							state = State.DIGITS;
						} else if (Character.isJavaIdentifierStart(ch)) {
							state = State.IDENT_PART;
						} else if(Character.isWhitespace(ch)) {
							if(ch == '\r') {
								getch();
								if(ch != '\n')
									index--;
								linenumber++;
								//System.out.println("no."+linenumber);
							}
							else if(ch == '\n') {
								linenumber++;
								//System.out.println("no."+linenumber);
								
							}
							begOffset = index;
						}
						else {
							t = stream.new Token(ILLEGAL_CHAR, begOffset, index,linenumber);
							break;
						}
					}
					break; // end of state START
				case DIGITS:
					if(ch == null) {
						t = stream.new Token(TokenStream.Kind.INT_LIT, begOffset, index,linenumber);
						break;
					}
					if (Character.isDigit(ch)) {
						state = State.DIGITS;
					}
					else {
						index--;
						t = stream.new Token(TokenStream.Kind.INT_LIT, begOffset, index,linenumber);
					}
					break; // end of state START
				case IDENT_PART:
					if(ch == null) {
						String identifier = String.valueOf(stream.inputChars, begOffset, index - begOffset);
						t = stream.new Token(getKeywordType(identifier), begOffset, index,linenumber);
						break;
					}
					if (Character.isJavaIdentifierPart(ch)) {
						state = State.IDENT_PART;
					}
					else {
						index--;
						String identifier = String.valueOf(stream.inputChars, begOffset, index - begOffset);
						t = stream.new Token(getKeywordType(identifier), begOffset, index,linenumber);
					}
					break;
				case COMMENT:
					if(ch == null) {
						t = stream.new Token(TokenStream.Kind.UNTERMINATED_COMMENT, begOffset, index,linenumber);
						break;
					}
					if(ch == '*') {
						getch();
						if(ch == null) {
							t = stream.new Token(TokenStream.Kind.UNTERMINATED_COMMENT, begOffset, index,linenumber);
							break;
						}
						else if(ch == '/') {
							begOffset = index;//just consume the comment
							state = State.START;
							break;
						}
					}
				case STRING_LIT:
					if(ch == null) {
						t = stream.new Token(TokenStream.Kind.UNTERMINATED_STRING, begOffset, index,linenumber);
						break;
					}
					if(ch == '"') {
						t = stream.new Token(TokenStream.Kind.STRING_LIT, begOffset, index,linenumber);
						break;
					}
					else if(ch == '\\') {
						getch();
						if(ch == null) {
							t = stream.new Token(TokenStream.Kind.UNTERMINATED_STRING, begOffset, index,linenumber);
							break;
						}
					}
				default:
					assert false : "should not reach here";
			}// end of switch(state)
		}   while (t == null); // loop terminates when a token is created
		return t;
	}

	public Scanner(TokenStream stream) {
		this.stream = stream;
		this.index = 0;
		this.linenumber = 1;
		populateKeywords();
	}


	// Fills in the stream.tokens list with recognized tokens 
	//from the input
	public void scan() {
		Token t = null;
		do {
			try {
				t = next();
			//	System.out.println("token::"+t);
			} catch (NumberFormatException | IOException e) {
				System.out.println("Error in scanning");
			}
			stream.tokens.add(t);
		} while (!t.kind.equals(TokenStream.Kind.EOF));
	}

}

