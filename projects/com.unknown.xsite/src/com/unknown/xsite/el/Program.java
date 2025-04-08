package com.unknown.xsite.el;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Program {
	private final Map<String, Function> funcs = new HashMap<>();
	private final Set<String> globals = new HashSet<>();
	private final Map<String, Value> constants = new HashMap<>();

	public void define(String name) {
		globals.add(name);
	}

	public void constant(String name, Value value) {
		constants.put(name, value);
	}

	public void constant(String name, String value) {
		constant(name, new Value(value));
	}

	public void constant(String name, long value) {
		constant(name, new Value(value));
	}

	public Context createContext() {
		return new Context(this);
	}

	public boolean addFunction(Function function) {
		String name = function.getName();
		if(funcs.containsKey(name)) {
			return false;
		}
		funcs.put(name, function);
		return true;
	}

	public Value getConstant(String name) {
		return constants.get(name);
	}

	public boolean isConstant(String name) {
		return constants.containsKey(name);
	}

	public boolean isGlobal(String name) {
		return globals.contains(name);
	}

	public Function getFunction(String name) {
		return funcs.get(name);
	}
}
