package com.unknown.xsite.test;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.Parser;
import com.unknown.xsite.el.Program;
import com.unknown.xsite.el.Scanner;
import com.unknown.xsite.el.Value;
import com.unknown.xsite.el.ast.Node;

public class ElTest {
	private static Parser parser(String s) throws Exception {
		Reader read = new StringReader(s);
		Scanner scan = new Scanner(read);
		return new Parser(scan);
	}

	@Test
	public void testExpression1() throws Exception {
		Parser p = parser("1 == 0");
		Node node = p.parseExpression();
		Value result = node.execute(new Context(new Program()));
		assertEquals(0, result.ival());
	}

	@Test
	public void testExpression2() throws Exception {
		Parser p = parser("42 == 42");
		Node node = p.parseExpression();
		Value result = node.execute(new Context(new Program()));
		assertEquals(1, result.ival());
	}

	@Test
	public void testExpression3() throws Exception {
		Parser p = parser("x == 'hello'");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("x", new Value("hello"));
		Value result = node.execute(ctx);
		assertEquals(1, result.ival());
	}

	@Test
	public void testExpression4() throws Exception {
		Parser p = parser("x.y == 'hello'");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("x.y", new Value("hello"));
		Value result = node.execute(ctx);
		assertEquals(1, result.ival());
	}

	@Test
	public void testExpression5() throws Exception {
		Parser p = parser("x == 'world'");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("x", new Value("hello"));
		Value result = node.execute(ctx);
		assertEquals(0, result.ival());
	}

	@Test
	public void testExpression6() throws Exception {
		Parser p = parser("0 && 1");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("entry.name", new Value("index"));
		Value result = node.execute(ctx);
		assertEquals(0, result.ival());
	}

	@Test
	public void testExpression7() throws Exception {
		Parser p = parser(
				"entry.name != 'index' && (!isset('entry.attr.hidden') || entry.attr.hidden != 'true')");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("entry.name", new Value("index"));
		Value result = node.execute(ctx);
		assertEquals(0, result.ival());
	}

	@Test
	public void testExpression8() throws Exception {
		Parser p = parser(
				"(entry.name != 'index') && (!isset('entry.attr.hidden') || entry.attr.hidden != 'true')");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		ctx.write("entry.name", new Value("index"));
		Value result = node.execute(ctx);
		assertEquals(0, result.ival());
	}

	@Test
	public void testIsset1() throws Exception {
		Parser p = parser("isset('entry.attr.hidden')");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		Value result = node.execute(ctx);
		assertEquals(0, result.ival());
	}

	@Test
	public void testIsset2() throws Exception {
		Parser p = parser("!isset('entry.attr.hidden')");
		Node node = p.parseExpression();
		Context ctx = new Context(new Program());
		Value result = node.execute(ctx);
		assertEquals(1, result.ival());
	}
}
