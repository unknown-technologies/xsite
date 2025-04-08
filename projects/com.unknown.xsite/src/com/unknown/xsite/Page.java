package com.unknown.xsite;

import com.unknown.xml.dom.Element;

public class Page {
	private final String name;
	private final String category;
	private final Element xml;

	public Page(String name, String category, Element xml) {
		this.name = name;
		this.category = category;
		this.xml = xml;
	}

	public Page(String name, Element xml) {
		this.name = name;
		this.category = null;
		this.xml = xml;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getPath() {
		if(category != null) {
			return category + "/" + name;
		} else {
			return name;
		}
	}

	public Element getXML() {
		return xml;
	}
}
