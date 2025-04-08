package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class VarNode extends Node {
	private final String name;

	public VarNode(String name) {
		this.name = name;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		return ctx.read(name);
	}
}
