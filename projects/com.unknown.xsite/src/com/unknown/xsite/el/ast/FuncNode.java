package com.unknown.xsite.el.ast;

import java.util.List;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class FuncNode extends Node {
	private final String name;
	private final List<Node> args;

	public FuncNode(String name, List<Node> args) {
		this.name = name;
		this.args = args;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value[] values = new Value[args.size()];
		for(int i = 0; i < values.length; i++) {
			values[i] = args.get(i).execute(ctx);
		}
		return ctx.call(name, values);
	}
}
