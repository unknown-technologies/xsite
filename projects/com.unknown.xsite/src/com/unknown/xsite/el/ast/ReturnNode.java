package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.ReturnException;
import com.unknown.xsite.el.Value;

public class ReturnNode extends Node {
	private final Node node;

	public ReturnNode(Node node) {
		this.node = node;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value value = node.execute(ctx);
		throw new ReturnException(value);
	}
}
