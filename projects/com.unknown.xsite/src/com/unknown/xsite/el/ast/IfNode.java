package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class IfNode extends Node {
	private final Node cond;
	private final Node when;
	private final Node otherwise;

	public IfNode(Node cond, Node body) {
		this.cond = cond;
		this.when = body;
		this.otherwise = null;
	}

	public IfNode(Node cond, Node when, Node otherwise) {
		this.cond = cond;
		this.when = when;
		this.otherwise = otherwise;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value c = cond.execute(ctx);
		if(c.isTrue()) {
			return when.execute(ctx);
		} else if(otherwise != null) {
			return otherwise.execute(ctx);
		} else {
			return null;
		}
	}
}
