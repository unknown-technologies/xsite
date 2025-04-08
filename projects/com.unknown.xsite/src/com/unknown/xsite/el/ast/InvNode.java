package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class InvNode extends Node {
	private final Node node;

	public InvNode(Node node) {
		this.node = node;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		return new Value(~node.execute(ctx).ival());
	}
}
