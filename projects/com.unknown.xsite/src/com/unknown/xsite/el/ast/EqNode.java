package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class EqNode extends Node {
	private final Node left;
	private final Node right;

	public EqNode(Node left, Node right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		return left.execute(ctx).equals(right.execute(ctx)) ? Value.TRUE : Value.FALSE;
	}
}
