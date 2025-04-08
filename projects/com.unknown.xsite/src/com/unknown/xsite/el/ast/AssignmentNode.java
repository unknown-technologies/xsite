package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class AssignmentNode extends Node {
	private final String name;
	private final Node node;

	public AssignmentNode(String name, Node node) {
		this.name = name;
		this.node = node;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value val = node.execute(ctx);
		ctx.write(name, val);
		return val;
	}

}
