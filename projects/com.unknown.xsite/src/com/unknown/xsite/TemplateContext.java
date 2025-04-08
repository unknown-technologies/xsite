package com.unknown.xsite;

import com.unknown.xml.dom.Element;

public class TemplateContext {
	private final TemplateContext parent;
	private final Element data;

	public TemplateContext(Element data) {
		this.parent = null;
		this.data = data;
	}

	public TemplateContext(TemplateContext parent, Element data) {
		this.parent = parent;
		this.data = data;
	}

	public TemplateContext getParent() {
		return parent;
	}

	public Element getData() {
		return data;
	}
}
