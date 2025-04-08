package com.unknown.xsite.el;

import static com.unknown.xsite.el.Token.Type.ADD;
import static com.unknown.xsite.el.Token.Type.AND;
import static com.unknown.xsite.el.Token.Type.ASSIGN;
import static com.unknown.xsite.el.Token.Type.COLON;
import static com.unknown.xsite.el.Token.Type.COMMA;
import static com.unknown.xsite.el.Token.Type.DIV;
import static com.unknown.xsite.el.Token.Type.DOLLAR;
import static com.unknown.xsite.el.Token.Type.ELSE;
import static com.unknown.xsite.el.Token.Type.EQUAL;
import static com.unknown.xsite.el.Token.Type.GE;
import static com.unknown.xsite.el.Token.Type.GT;
import static com.unknown.xsite.el.Token.Type.IF;
import static com.unknown.xsite.el.Token.Type.INV;
import static com.unknown.xsite.el.Token.Type.LAND;
import static com.unknown.xsite.el.Token.Type.LBRAC;
import static com.unknown.xsite.el.Token.Type.LBRACE;
import static com.unknown.xsite.el.Token.Type.LE;
import static com.unknown.xsite.el.Token.Type.LOR;
import static com.unknown.xsite.el.Token.Type.LPAR;
import static com.unknown.xsite.el.Token.Type.LT;
import static com.unknown.xsite.el.Token.Type.MOD;
import static com.unknown.xsite.el.Token.Type.MUL;
import static com.unknown.xsite.el.Token.Type.NE;
import static com.unknown.xsite.el.Token.Type.NONE;
import static com.unknown.xsite.el.Token.Type.NOT;
import static com.unknown.xsite.el.Token.Type.NULL;
import static com.unknown.xsite.el.Token.Type.OR;
import static com.unknown.xsite.el.Token.Type.POW;
import static com.unknown.xsite.el.Token.Type.RBRAC;
import static com.unknown.xsite.el.Token.Type.RBRACE;
import static com.unknown.xsite.el.Token.Type.RETURN;
import static com.unknown.xsite.el.Token.Type.RPAR;
import static com.unknown.xsite.el.Token.Type.SAR;
import static com.unknown.xsite.el.Token.Type.SEMICOLON;
import static com.unknown.xsite.el.Token.Type.SHL;
import static com.unknown.xsite.el.Token.Type.SHR;
import static com.unknown.xsite.el.Token.Type.STR;
import static com.unknown.xsite.el.Token.Type.SUB;
import static com.unknown.xsite.el.Token.Type.VAR;
import static com.unknown.xsite.el.Token.Type.WHILE;
import static com.unknown.xsite.el.Token.Type.XOR;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

public class Scanner {
	private final Reader in;
	private int last;
	private int unread;

	private int pos;

	public Scanner(Reader in) {
		this.in = in;
		last = -1;
		unread = -1;
		pos = 0;
	}

	public int getPosition() {
		return pos;
	}

	private int read() throws ParseException {
		try {
			pos++;
			if(unread != -1) {
				int result = unread;
				unread = -1;
				return result;
			} else {
				last = in.read();
				return last;
			}
		} catch(IOException e) {
			throw new ParseException("failed to read source", pos);
		}
	}

	private void unread() {
		pos--;
		unread = last;
	}

	private static boolean isWhitespace(int c) {
		switch(c) {
		case '\t':
		case '\f':
		case ' ':
			return true;
		default:
			return false;
		}
	}

	private static boolean isEOL(int c) {
		return c == '\r' || c == '\n';
	}

	private static boolean isIdent(int c) {
		return c == '_' || c == '.' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || isNumber(c);
	}

	private static boolean isIdentFirst(int c) {
		return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private static boolean isNumber(int c) {
		return c >= '0' && c <= '9';
	}

	private static boolean isHex(int c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	private static boolean isOctal(int c) {
		return(c >= '0' && c <= '7');
	}

	public Token scan() throws ParseException {
		int c = read();

		// skip comments and whitespace
		while(isWhitespace(c)) {
			c = read();
		}

		while(c == '#') {
			// comment
			c = read();
			while(!isEOL(c)) {
				c = read();
			}
		}

		if(isEOL(c)) {
			while(isEOL(c)) {
				c = read();
			}
			unread();
			return scan();
		}

		if(isIdentFirst(c)) {
			StringBuilder buf = new StringBuilder();
			while(isIdent(c)) {
				buf.append((char) c);
				c = read();
			}

			unread();
			String s = buf.toString();
			switch(s) {
			case "if":
				return new Token(IF);
			case "else":
				return new Token(ELSE);
			case "while":
				return new Token(WHILE);
			case "return":
				return new Token(RETURN);
			case "var":
				return new Token(VAR);
			case "null":
				return new Token(NULL);
			default:
				return new Token(s);
			}
		}

		if(c == '0') {
			long num = 0;
			c = read();
			if(c == 'x' || c == 'X') {
				// hex
				c = read();
				while(isHex(c)) {
					num <<= 4;
					if(c >= '0' && c <= '9') {
						num += c - '0';
					} else if(c >= 'a' && c <= 'f') {
						num += c - 'a' + 0x0A;
					} else if(c >= 'A' && c <= 'F') {
						num += c - 'A' + 0x0A;
					}

					c = read();
				}
			} else {
				while(isOctal(c)) {
					num <<= 3;
					num |= c - '0';
					c = read();
				}
			}
			unread();
			return new Token(num);
		} else if(isNumber(c)) {
			long num = 0;
			while(isNumber(c)) {
				num *= 10;
				num += c - '0';
				c = read();
			}
			unread();
			return new Token(num);
		}

		switch(c) {
		case ':':
			return new Token(COLON);
		case ',':
			return new Token(COMMA);
		case '\'': {
			StringBuilder buf = new StringBuilder();
			c = read();
			while(c != '\'') {
				if(c == -1) {
					throw new ParseException("unexpected EOF", pos);
				}
				buf.append((char) c);
				c = read();
			}
			return new Token(STR, buf.toString());
		}
		case '"': {
			StringBuilder buf = new StringBuilder();
			c = read();
			while(c != '"') {
				if(c == -1) {
					throw new ParseException("unexpected EOF", pos);
				}
				buf.append((char) c);
				c = read();
			}
			unread();
			return new Token(STR, buf.toString());
		}
		case '.':
			c = read();
			if(!isIdent(c)) {
				throw new ParseException("expected identifier, got '" + (char) c + "'", pos);
			} else {
				StringBuilder buf = new StringBuilder();
				buf.append(".");
				while(isIdentFirst(c)) {
					buf.append((char) c);
					c = read();
				}

				unread();
				return new Token(buf.toString());
			}
		case '+':
			return new Token(ADD);
		case '-':
			return new Token(SUB);
		case '*':
			c = read();
			if(c == '*') {
				return new Token(POW);
			} else {
				unread();
			}
			return new Token(MUL);
		case '/':
			return new Token(DIV);
		case '%':
			return new Token(MOD);
		case '(':
			return new Token(LPAR);
		case ')':
			return new Token(RPAR);
		case '[':
			return new Token(LBRAC);
		case ']':
			return new Token(RBRAC);
		case '{':
			return new Token(LBRACE);
		case '}':
			return new Token(RBRACE);
		case '$':
			return new Token(DOLLAR);
		case '~':
			return new Token(INV);
		case '&':
			c = read();
			if(c == '&') {
				return new Token(LAND);
			} else {
				unread();
				return new Token(AND);
			}
		case '|':
			c = read();
			if(c == '|') {
				return new Token(LOR);
			} else {
				unread();
				return new Token(OR);
			}
		case '^':
			return new Token(XOR);
		case '=':
			c = read();
			if(c == '=') {
				return new Token(EQUAL);
			} else {
				unread();
			}
			return new Token(ASSIGN);
		case '>':
			c = read();
			if(c == '=') {
				return new Token(GE);
			} else if(c == '>') {
				c = read();
				if(c == '>') {
					return new Token(SHR);
				} else {
					unread();
					return new Token(SAR);
				}
			} else {
				unread();
			}
			return new Token(GT);
		case '<':
			c = read();
			if(c == '=') {
				return new Token(LE);
			} else if(c == '<') {
				return new Token(SHL);
			} else {
				unread();
			}
			return new Token(LT);
		case '!':
			c = read();
			if(c == '=') {
				return new Token(NE);
			} else {
				unread();
			}
			return new Token(NOT);
		case ';':
			return new Token(SEMICOLON);

		}

		return new Token(NONE);
	}
}
