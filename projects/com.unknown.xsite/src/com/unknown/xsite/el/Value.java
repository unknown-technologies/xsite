package com.unknown.xsite.el;

public class Value {
	public static final Value TRUE = new Value(1);
	public static final Value FALSE = new Value(0);

	private final long ival;
	private final String sval;
	private final boolean isNull;

	public Value(long ival) {
		this.ival = ival;
		this.sval = null;
		this.isNull = false;
	}

	public Value(String sval) {
		this.ival = 0;
		this.sval = sval;
		this.isNull = sval == null;
	}

	public long ival() throws ExecuteException {
		if(isNull) {
			throw new ExecuteException("trying to read null as ival");
		} else if(sval == null) {
			return ival;
		} else {
			throw new ExecuteException("trying to read a string as ival");
		}
	}

	public String sval() throws ExecuteException {
		if(isNull) {
			return null;
		} else if(sval != null) {
			return sval;
		} else {
			throw new ExecuteException("trying to read an integer as sval");
		}
	}

	public boolean isTrue() {
		if(isNull) {
			return false;
		} else if(sval != null) {
			return sval.length() > 0;
		} else {
			return ival != 0;
		}
	}

	public boolean isFalse() {
		return !isTrue();
	}

	public boolean isString() {
		return sval != null;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(!(o instanceof Value)) {
			return false;
		}
		Value v = (Value) o;
		if(isNull) {
			return v.isNull;
		} else if(v.sval == null) {
			return sval == null && v.ival == ival;
		} else {
			return v.sval.equals(sval);
		}
	}

	@Override
	public int hashCode() {
		if(sval != null) {
			return sval.hashCode();
		} else {
			return Long.hashCode(ival);
		}
	}

	@Override
	public String toString() {
		if(sval == null) {
			return Long.toString(ival);
		} else {
			return sval;
		}
	}
}
