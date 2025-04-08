package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class AddNode extends Node {
	private final Node left;
	private final Node right;

	public AddNode(Node left, Node right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value l = left.execute(ctx);
		Value r = right.execute(ctx);
		if(l.isString() && r.isString()) {
			return new Value(l.sval() + r.sval());
		} else {
			return new Value(l.ival() + r.ival());
		}
	}
}
