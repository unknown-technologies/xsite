package com.unknown.xsite.el.ast;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.Value;

public class ConstNode extends Node {
	private final Value value;

	public ConstNode(Value value) {
		this.value = value;
	}

	@Override
	public Value execute(Context ctx) {
		return value;
	}
}
