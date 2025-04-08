package com.unknown.xsite.el;

@SuppressWarnings("serial")
public class UndefinedVariableException extends ExecuteException {
	public UndefinedVariableException(String name) {
		super("undefined variable " + name);
	}
}
