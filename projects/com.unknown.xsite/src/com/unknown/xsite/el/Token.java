package com.unknown.xsite.el;

public class Token {
	public Type type;
	public String str;
	public long val;

	public Token(Type type) {
		this.type = type;
	}

	public Token(String str) {
		this.type = Type.IDENT;
		this.str = str;
	}

	public Token(long number) {
		this.type = Type.NUMBER;
		this.val = number;
	}

	public Token(Type type, String str) {
		this.type = type;
		this.str = str;
	}

	@Override
	public String toString() {
		switch(type) {
		case IDENT:
			return "Token[IDENT:\"" + str + "\"]";
		case NUMBER:
			return "Token[NUMBER:" + val + "]";
		default:
			return "Token[" + type + "]";
		}
	}

	public static enum Type {
		IDENT, NUMBER, COLON, COMMA, EQUAL, ASSIGN, ADD, SUB, MUL, DIV, MOD, POW, GT, GE, LT, LE, NOT, NE, SHL, SHR, SAR, AND, OR, XOR, INV, LAND, LOR, IF, ELSE, WHILE, RETURN, VAR, LPAR, RPAR, LBRAC, RBRAC, LBRACE, RBRACE, DOLLAR, SEMICOLON, STR, NULL, NONE;
	}
}
