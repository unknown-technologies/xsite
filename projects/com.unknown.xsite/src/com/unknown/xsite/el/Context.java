package com.unknown.xsite.el;

import java.util.HashMap;
import java.util.Map;

public class Context {
	private final Map<String, Value> variables = new HashMap<>();
	private final Context parent;
	private final Program prog;
	private final java.util.function.Function<String, Value> varget;

	public Context(Program prog) {
		this.parent = null;
		this.prog = prog;
		this.varget = null;
	}

	public Context(Context parent) {
		this.parent = parent;
		this.prog = parent.prog;
		this.varget = null;
	}

	public Context(Context parent, java.util.function.Function<String, Value> varget) {
		this.parent = parent;
		this.prog = parent.prog;
		this.varget = varget;
	}

	public void write(String name, Value value) throws ExecuteException {
		if(prog.isConstant(name)) {
			throw new ExecuteException("trying to redefine constant " + name);
		}
		if(parent != null && prog.isGlobal(name)) {
			parent.write(name, value);
		} else {
			variables.put(name, value);
		}
	}

	public Value read(String name) throws UndefinedVariableException {
		if(varget != null) {
			Value val = varget.apply(name);
			if(val != null) {
				return val;
			}
		}

		Value con = prog.getConstant(name);
		if(con != null) {
			return con;
		}

		Value var = variables.get(name);
		if(var == null) {
			if(parent == null) {
				throw new UndefinedVariableException(name);
			} else {
				return parent.read(name);
			}
		} else {
			return var;
		}
	}

	public Value call(String name, Value[] args) throws ExecuteException {
		switch(name) {
		case "when":
			if(args.length != 2 && args.length != 3) {
				throw new ArityException(2, args.length);
			}
			if(args.length == 2) {
				boolean cond = args[0].isTrue();
				if(cond) {
					return args[1];
				} else {
					return null;
				}
			} else {
				boolean cond = args[0].isTrue();
				if(cond) {
					return args[1];
				} else {
					return args[2];
				}
			}
		case "isset":
			if(args.length != 1) {
				throw new ArityException(1, args.length);
			}
			try {
				read(args[0].sval());
				return Value.TRUE;
			} catch(UndefinedVariableException e) {
				return Value.FALSE;
			}
		case "get":
			if(args.length != 2) {
				throw new ArityException(2, args.length);
			}
			try {
				return read(args[0].sval());
			} catch(UndefinedVariableException e) {
				return args[1];
			}
		default:
			throw new ExecuteException("undefined function " + name);
		}
	}
}
