package com.unknown.xsite.el.ast;

import java.util.List;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Value;

public class BlockNode extends Node {
	private final List<Node> body;

	public BlockNode(List<Node> body) {
		this.body = body;
	}

	@Override
	public Value execute(Context ctx) throws ExecuteException {
		Value result = null;
		for(Node node : body) {
			result = node.execute(ctx);
		}
		return result;
	}

}
