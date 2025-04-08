package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class PowNode extends Node {
	private final Node left;
	private final Node right;

	public PowNode(Node left, Node right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		return new Value(Math.round(Math.pow(left.execute(ctx).ival(), right.execute(ctx).ival())));
	}
}
