package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class NotNode extends Node {
	private final Node node;

	public NotNode(Node node) {
		this.node = node;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		return node.execute(ctx).isFalse() ? Value.TRUE : Value.FALSE;
	}
}
