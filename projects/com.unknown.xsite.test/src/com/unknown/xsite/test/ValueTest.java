package com.unknown.xsite.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.unknown.xsite.el.Value;

public class ValueTest {
	@Test
	public void testIvalEquals1() {
		Value v1 = new Value(7);
		Value v2 = new Value(7);
		assertTrue(v1.equals(v2));
	}

	@Test
	public void testIvalEquals2() {
		Value v1 = new Value(7);
		Value v2 = new Value(8);
		assertFalse(v1.equals(v2));
	}

	@Test
	public void testSvalEquals1() {
		Value v1 = new Value("hello");
		Value v2 = new Value("hello");
		assertTrue(v1.equals(v2));
	}

	@Test
	public void testSvalEquals2() {
		Value v1 = new Value("hello");
		Value v2 = new Value("world");
		assertFalse(v1.equals(v2));
	}

	@Test
	public void testIsTrue1() {
		Value v = new Value(0);
		assertFalse(v.isTrue());
	}

	@Test
	public void testIsTrue2() {
		Value v = new Value(7);
		assertTrue(v.isTrue());
	}

	@Test
	public void testIsTrue3() {
		Value v = new Value("");
		assertFalse(v.isTrue());
	}

	@Test
	public void testIsTrue4() {
		Value v = new Value("noodle");
		assertTrue(v.isTrue());
	}
}
