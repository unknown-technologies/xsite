package com.unknown.xsite.el;

import java.util.List;

import com.unknown.xsite.el.ast.Node;

public class Function {
	private final String name;
	private final List<String> args;
	private final List<Node> body;

	public Function(String name, List<String> args, List<Node> body) {
		this.name = name;
		this.args = args;
		this.body = body;
	}

	public String getName() {
		return name;
	}

	public Value execute(Context ctx, Value[] arguments) throws ExecuteException {
		if(arguments.length != args.size()) {
			throw new ArityException(args.size(), arguments.length);
		}

		Context c = new Context(ctx);
		for(int i = 0; i < args.size(); i++) {
			c.write(args.get(i), arguments[i]);
		}

		try {
			Value result = null;
			for(Node node : body) {
				result = node.execute(c);
			}
			return result;
		} catch(ReturnException e) {
			return e.getValue();
		}
	}
}
