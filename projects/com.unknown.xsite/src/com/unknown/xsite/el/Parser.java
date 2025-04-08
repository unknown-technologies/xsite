package com.unknown.xsite.el;

import static com.unknown.xsite.el.Token.Type.ADD;
import static com.unknown.xsite.el.Token.Type.AND;
import static com.unknown.xsite.el.Token.Type.ASSIGN;
import static com.unknown.xsite.el.Token.Type.COMMA;
import static com.unknown.xsite.el.Token.Type.DIV;
import static com.unknown.xsite.el.Token.Type.ELSE;
import static com.unknown.xsite.el.Token.Type.EQUAL;
import static com.unknown.xsite.el.Token.Type.GE;
import static com.unknown.xsite.el.Token.Type.GT;
import static com.unknown.xsite.el.Token.Type.IDENT;
import static com.unknown.xsite.el.Token.Type.IF;
import static com.unknown.xsite.el.Token.Type.LAND;
import static com.unknown.xsite.el.Token.Type.LBRACE;
import static com.unknown.xsite.el.Token.Type.LE;
import static com.unknown.xsite.el.Token.Type.LOR;
import static com.unknown.xsite.el.Token.Type.LPAR;
import static com.unknown.xsite.el.Token.Type.LT;
import static com.unknown.xsite.el.Token.Type.MOD;
import static com.unknown.xsite.el.Token.Type.MUL;
import static com.unknown.xsite.el.Token.Type.NE;
import static com.unknown.xsite.el.Token.Type.NONE;
import static com.unknown.xsite.el.Token.Type.NOT;
import static com.unknown.xsite.el.Token.Type.NULL;
import static com.unknown.xsite.el.Token.Type.NUMBER;
import static com.unknown.xsite.el.Token.Type.OR;
import static com.unknown.xsite.el.Token.Type.POW;
import static com.unknown.xsite.el.Token.Type.RBRACE;
import static com.unknown.xsite.el.Token.Type.RETURN;
import static com.unknown.xsite.el.Token.Type.RPAR;
import static com.unknown.xsite.el.Token.Type.SAR;
import static com.unknown.xsite.el.Token.Type.SEMICOLON;
import static com.unknown.xsite.el.Token.Type.SHL;
import static com.unknown.xsite.el.Token.Type.SHR;
import static com.unknown.xsite.el.Token.Type.STR;
import static com.unknown.xsite.el.Token.Type.SUB;
import static com.unknown.xsite.el.Token.Type.VAR;
import static com.unknown.xsite.el.Token.Type.XOR;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.unknown.xsite.el.Token.Type;
import com.unknown.xsite.el.ast.AddNode;
import com.unknown.xsite.el.ast.AndNode;
import com.unknown.xsite.el.ast.AssignmentNode;
import com.unknown.xsite.el.ast.BlockNode;
import com.unknown.xsite.el.ast.ConstNode;
import com.unknown.xsite.el.ast.DivNode;
import com.unknown.xsite.el.ast.EqNode;
import com.unknown.xsite.el.ast.FuncNode;
import com.unknown.xsite.el.ast.GeNode;
import com.unknown.xsite.el.ast.GtNode;
import com.unknown.xsite.el.ast.IfNode;
import com.unknown.xsite.el.ast.LAndNode;
import com.unknown.xsite.el.ast.LOrNode;
import com.unknown.xsite.el.ast.LeNode;
import com.unknown.xsite.el.ast.LtNode;
import com.unknown.xsite.el.ast.ModNode;
import com.unknown.xsite.el.ast.MulNode;
import com.unknown.xsite.el.ast.NeNode;
import com.unknown.xsite.el.ast.Node;
import com.unknown.xsite.el.ast.NotNode;
import com.unknown.xsite.el.ast.OrNode;
import com.unknown.xsite.el.ast.PowNode;
import com.unknown.xsite.el.ast.ReturnNode;
import com.unknown.xsite.el.ast.SarNode;
import com.unknown.xsite.el.ast.ShlNode;
import com.unknown.xsite.el.ast.ShrNode;
import com.unknown.xsite.el.ast.SubNode;
import com.unknown.xsite.el.ast.VarNode;
import com.unknown.xsite.el.ast.XorNode;

public class Parser {
	private final Scanner scanner;

	private Token t;
	private Token la;
	private Type sym;

	public Parser(Scanner scanner) {
		this.scanner = scanner;
	}

	private void scan() throws ParseException {
		t = la;
		la = scanner.scan();
		sym = la.type;
	}

	private void check(Type type) throws ParseException {
		if(sym != type) {
			error("invalid token: expected " + type + ", got " + sym);
		}
		scan();
	}

	private void error(String msg) throws ParseException {
		throw new ParseException(msg, scanner.getPosition());
	}

	public Program parse() throws ParseException {
		scan();
		Program prog = new Program();
		while(sym == IDENT || sym == VAR) {
			if(sym == IDENT) {
				Function func = func();
				if(!prog.addFunction(func)) {
					error("Function already defined: " + func.getName());
				}
			} else {
				scan();
				check(IDENT);
				String name = t.str;
				check(SEMICOLON);
				prog.define(name);
			}
		}
		check(NONE);
		return prog;
	}

	public Node parseExpression() throws ParseException {
		scan();
		Node expr = expr();
		check(NONE);
		return expr;
	}

	private Function func() throws ParseException {
		List<String> args = new ArrayList<>();
		List<Node> body = new ArrayList<>();
		check(IDENT);
		String name = t.str;
		check(LPAR);
		if(sym == IDENT) {
			scan();
			args.add(t.str);
			while(sym == COMMA) {
				scan();
				check(IDENT);
				args.add(t.str);
			}
		}
		check(RPAR);
		check(LBRACE);
		while(sym != RBRACE && sym != NONE) {
			body.add(statement());
		}
		check(RBRACE);
		return new Function(name, args, body);
	}

	private Node statement() throws ParseException {
		if(sym == IF) {
			scan();
			check(LPAR);
			Node cond = expr();
			check(RPAR);
			Node body = statement();
			if(sym == ELSE) {
				scan();
				Node otherwise = statement();
				return new IfNode(cond, body, otherwise);
			} else {
				return new IfNode(cond, body);
			}
		} else if(sym == LBRACE) {
			scan();
			List<Node> body = new ArrayList<>();
			while(sym != NONE && sym != RBRACE) {
				body.add(statement());
			}
			check(RBRACE);
			return new BlockNode(body);
		} else if(sym == RETURN) {
			scan();
			Node expr = expr();
			check(SEMICOLON);
			return new ReturnNode(expr);
		} else {
			Node node = expr();
			check(SEMICOLON);
			return node;
		}
	}

	private Node expr() throws ParseException {
		Node result = lor();
		while(sym == LOR) {
			scan();
			result = new LOrNode(result, lor());
		}
		return result;
	}

	private Node lor() throws ParseException {
		Node result = land();
		while(sym == LAND) {
			scan();
			result = new LAndNode(result, land());
		}
		return result;
	}

	private Node land() throws ParseException {
		Node result = cmp();
		while(sym == GT || sym == GE || sym == LT || sym == LE || sym == EQUAL || sym == NE) {
			scan();
			if(t.type == GT) {
				result = new GtNode(result, cmp());
			} else if(t.type == GE) {
				result = new GeNode(result, cmp());
			} else if(t.type == LT) {
				result = new LtNode(result, cmp());
			} else if(t.type == LE) {
				result = new LeNode(result, cmp());
			} else if(t.type == EQUAL) {
				result = new EqNode(result, cmp());
			} else if(t.type == NE) {
				result = new NeNode(result, cmp());
			} else {
				error("unreachable");
			}
		}
		return result;
	}

	private Node cmp() throws ParseException {
		Node result = sum();
		while(sym == ADD || sym == SUB || sym == OR) {
			scan();
			if(t.type == ADD) {
				result = new AddNode(result, sum());
			} else if(t.type == SUB) {
				result = new SubNode(result, sum());
			} else {
				result = new OrNode(result, sum());
			}
		}
		return result;
	}

	private Node sum() throws ParseException {
		Node result = factor();
		while(sym == MUL || sym == DIV || sym == MOD || sym == POW || sym == AND || sym == XOR) {
			scan();
			if(t.type == MUL) {
				result = new MulNode(result, factor());
			} else if(t.type == DIV) {
				result = new DivNode(result, factor());
			} else if(t.type == MOD) {
				result = new ModNode(result, factor());
			} else if(t.type == POW) {
				result = new PowNode(result, factor());
			} else if(t.type == AND) {
				result = new AndNode(result, factor());
			} else if(t.type == XOR) {
				result = new XorNode(result, factor());
			}
		}
		return result;
	}

	private Node factor() throws ParseException {
		Node result = term();
		while(sym == SHL || sym == SHR || sym == SAR) {
			scan();
			if(t.type == SHL) {
				result = new ShlNode(result, term());
			} else if(t.type == SHR) {
				result = new ShrNode(result, term());
			} else {
				result = new SarNode(result, term());
			}
		}
		return result;
	}

	private Node term() throws ParseException {
		if(sym == LPAR) {
			scan();
			Node expr = expr();
			check(RPAR);
			return expr;
		} else if(sym == IDENT) {
			scan();
			String name = t.str;
			if(sym == LPAR) {
				scan();
				if(sym == RPAR) {
					scan();
					return new FuncNode(name, Collections.emptyList());
				}
				List<Node> args = new ArrayList<>();
				args.add(expr());
				while(sym == COMMA) {
					scan();
					args.add(expr());
				}
				check(RPAR);
				return new FuncNode(name, args);
			} else if(sym == ASSIGN) {
				scan();
				Node value = expr();
				return new AssignmentNode(name, value);
			} else {
				return new VarNode(name);
			}
		} else if(sym == SUB || sym == NUMBER) {
			return number();
		} else if(sym == STR) {
			return str();
		} else if(sym == NULL) {
			scan();
			return new ConstNode(new Value(null));
		} else if(sym == NOT) {
			scan();
			Node term = term();
			return new NotNode(term);
		} else {
			error("expected LPAR|IDENT|SUB|NOT|NUMBER|STR|NULL, got " + sym);
			return null; // unreachable
		}
	}

	private Node number() throws ParseException {
		if(sym == SUB) {
			scan();
			check(NUMBER);
			return new ConstNode(new Value(-t.val));
		} else {
			check(NUMBER);
			return new ConstNode(new Value(t.val));
		}
	}

	private Node str() throws ParseException {
		check(STR);
		return new ConstNode(new Value(t.str));
	}
}
