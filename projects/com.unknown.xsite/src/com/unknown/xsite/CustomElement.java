package com.unknown.xsite;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.unknown.syntax.CHighlighter;
import com.unknown.syntax.GlslHighlighter;
import com.unknown.syntax.Highlighter;
import com.unknown.syntax.JavaHighlighter;
import com.unknown.syntax.MipsAsmHighlighter;
import com.unknown.syntax.PhpHighlighter;
import com.unknown.syntax.ShHighlighter;
import com.unknown.syntax.XmlHighlighter;
import com.unknown.xml.dom.Attribute;
import com.unknown.xml.dom.Element;
import com.unknown.xml.dom.Text;
import com.unknown.xml.dom.XMLReader;
import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.ExecuteException;
import com.unknown.xsite.el.Parser;
import com.unknown.xsite.el.Scanner;
import com.unknown.xsite.el.Value;
import com.unknown.xsite.el.ast.Node;

public class CustomElement {
	private static final Set<String> NO_SELF_CLOSING = Set.of("span", "div", "ul", "ol", "li", "a", "td", "script");
	private static final Set<String> ALWAYS_TRIM = Set.of("h1", "h2", "h3", "p");
	private static final List<String> META_ATTR_ORDER = List.of("name", "property", "content");

	private final Element xml;

	private final String name;

	public CustomElement(Element xml) throws IOException {
		this.xml = xml;

		if(!xml.uri.equals(Project.XMLNS_T)) {
			throw new IOException("Invalid URI: " + xml.uri);
		}

		name = xml.name;
	}

	public Element getXML() {
		return xml;
	}

	public String getName() {
		return name;
	}

	public List<Element> apply(Project project, Element data, Map<String, String> info) throws IOException {
		assert data.name.equals(name) : String.format("expected %s, got %s", name, data.name);

		List<Element> result = new ArrayList<>();
		applyRoot(result, project, new TemplateContext(data), new HashMap<>(info));
		return result;
	}

	// apply root template element
	private void applyRoot(List<Element> result, Project project, TemplateContext ctx,
			Map<String, String> attributes) throws IOException {
		Map<String, String> attrs = new HashMap<>();
		attrs.putAll(attributes);

		Element element = ctx.getData();
		for(Attribute attrib : element.getAttributes()) {
			String val = processAttribute(project, attributes, attrib.value);
			attrs.put("attr." + attrib.name, val);
		}

		attrs.put("value", element.value);

		for(Element e : xml.getChildren(true)) {
			apply(result, project, attrs, e, ctx);
		}
	}

	// apply one template element with given data
	private static void apply(List<Element> result, Project project, Map<String, String> attrs, Element element,
			TemplateContext ctx) throws IOException {
		if(element.uri.equals(Project.XMLNS_T)) {
			CustomElement e = project.getElement(element.name);
			if(e != null) {
				e.applyRoot(result, project, new TemplateContext(ctx, element), attrs);
			} else {
				throw new IOException("unknown element " + element.name);
			}
		} else if(element.uri.equals(Project.XMLNS_C)) {
			// control flow
			processC(result, project, attrs, element, ctx);
		} else if(element instanceof Text) {
			element.value = processQuotes(element.value);
			result.add(element);
		} else {
			// recurse
			recurse(result, project, attrs, element, ctx);
		}
	}

	private static void recurse(List<Element> result, Project project, Map<String, String> attrs, Element element,
			TemplateContext ctx) throws IOException {
		Element el = new Element(element.uri, element.name, element.qName, element.value);

		setSelfClosing(el);
		boolean trim = false;
		String attrorder = null;

		if(element.uri.length() == 0 && ALWAYS_TRIM.contains(element.name)) {
			trim = true;
		}

		Map<String, String> newattr = new HashMap<>();
		for(Attribute attr : element.getAttributes()) {
			String value = processAttribute(project, attrs, attr.value);
			if(attr.uri.equals(Project.XMLNS_C)) {
				switch(attr.name) {
				case "trim":
					trim = bool(value);
					break;
				case "attrorder":
					attrorder = value;
					break;
				}
			} else {
				newattr.put(attr.name, value);
			}
		}

		for(Entry<String, String> attr : newattr.entrySet()) {
			String name = attr.getKey();
			String value = attr.getValue();

			if(trim) {
				value = value.trim();
			}

			el.addAttribute(name, value);
		}

		List<Element> children = new ArrayList<>();
		for(Element c : element.getChildren(true)) {
			apply(children, project, attrs, c, ctx);
		}
		for(Element c : children) {
			el.addChild(c);
		}

		if(trim) {
			el.compress();
			if(el.value != null) {
				el.value = el.value.trim();
			}
		}

		if(attrorder != null) {
			el.setAttributeOrder(List.of(attrorder.split(" ")));
		} else if(element.uri.length() == 0 && element.name.equals("meta")) {
			el.setAttributeOrder(META_ATTR_ORDER);
		}

		result.add(el);
	}

	private static void processC(List<Element> result, Project project, Map<String, String> attrs, Element command,
			TemplateContext ctx) throws IOException {
		switch(command.name) {
		case "out": {
			String value = processAttribute(project, attrs, command.getAttribute("value"));
			result.add(new Text(processQuotes(value)));
			break;
		}
		case "apply": {
			Element data = ctx.getData();
			if(command.getAttribute("select") != null) {
				String select = command.getAttribute("select").strip();
				for(Element c : data.getChildren(true)) {
					if(c.name.equals(select)) {
						apply(result, project, attrs, c, ctx.getParent());
					}
				}
			} else {
				for(Element c : data.getChildren(true)) {
					apply(result, project, attrs, c, ctx.getParent());
				}
			}
			break;
		}
		case "if": {
			String value = processAttribute(project, attrs, command.getAttribute("test"));
			if(bool(value)) {
				for(Element c : command.getChildren(true)) {
					apply(result, project, attrs, c, ctx);
				}
			}
			break;
		}
		case "ifnt": {
			String value = processAttribute(project, attrs, command.getAttribute("test"));
			if(!bool(value)) {
				for(Element c : command.getChildren(true)) {
					apply(result, project, attrs, c, ctx);
				}
			}
			break;
		}
		case "choose": {
			loop: for(Element e : command.getChildren(true)) {
				if(!e.uri.equals(Project.XMLNS_C)) {
					throw new IOException("unexpected element: " + e.name);
				}
				switch(e.name) {
				case "when": {
					String value = processAttribute(project, attrs, e.getAttribute("test"));
					if(bool(value)) {
						for(Element c : e.getChildren(true)) {
							apply(result, project, attrs, c, ctx);
						}
						break loop;
					}
					break;
				}
				case "otherwise": {
					for(Element c : e.getChildren(true)) {
						apply(result, project, attrs, c, ctx);
					}
					break;
				}
				}
			}
			break;
		}

		case "foreach": {
			String category = processAttribute(project, attrs, command.getAttribute("category"));
			String sort = processAttribute(project, attrs, command.getAttribute("sort"));
			String key = processAttribute(project, attrs, command.getAttribute("key"));
			String limit = processAttribute(project, attrs, command.getAttribute("limit"));

			boolean asc = sort == null || sort.equals("ascending");

			HashMap<String, String> newattrs = new HashMap<>(attrs);
			List<Page> pages = new ArrayList<>(project.getCategory(category));

			if(key != null) {
				Collections.sort(pages, (a, b) -> {
					String x = a.getXML().getAttribute(key);
					String y = b.getXML().getAttribute(key);
					if(x == null && y == null) {
						return 0;
					} else if(x == null) {
						return 1;
					} else if(y == null) {
						return -1;
					}
					int cmp = x.compareTo(y);
					return asc ? cmp : -cmp;
				});
			} else if(asc) {
				Collections.sort(pages, (a, b) -> a.getName().compareTo(b.getName()));
			} else {
				Collections.sort(pages, (a, b) -> -a.getName().compareTo(b.getName()));
			}

			int lim = 0;
			if(limit != null) {
				lim = Integer.parseInt(limit);
			}

			int i = 0;
			for(Page page : pages) {
				if(lim != 0 && i++ > lim) {
					break;
				}

				newattrs.put("entry.name", page.getName());
				newattrs.put("entry.category", page.getCategory());
				newattrs.put("entry.path", page.getPath());

				for(Attribute attr : page.getXML().getAttributes()) {
					newattrs.put("entry.attr." + attr.name, attr.value);
				}

				for(Element c : command.getChildren(true)) {
					apply(result, project, newattrs, c, ctx);
				}

				for(Attribute attr : page.getXML().getAttributes()) {
					newattrs.remove("entry.attr." + attr.name);
				}
			}
			break;
		}
		case "code": {
			String language = processAttribute(project, attrs, command.getAttribute("language"));
			if(language == null) {
				break;
			}

			String code = ctx.getData().value;
			if(code == null) {
				throw new IOException("empty code block");
			}

			Highlighter highlighter = null;
			switch(language) {
			case "c":
			case "cpp":
				highlighter = new CHighlighter();
				break;
			case "glsl":
				highlighter = new GlslHighlighter();
				break;
			case "java":
				highlighter = new JavaHighlighter();
				break;
			case "php":
				highlighter = new PhpHighlighter();
				break;
			case "mips":
				highlighter = new MipsAsmHighlighter();
				break;
			case "sh":
				highlighter = new ShHighlighter();
				break;
			case "xml":
				highlighter = new XmlHighlighter();
				break;
			default:
				throw new IOException("unknown language " + language);
			}

			String html = null;
			try(StringWriter out = new StringWriter(); Reader in = new StringReader(code)) {
				highlighter.format(in, out);
				html = out.toString().trim();
			}

			if(html != null) {
				try {
					Element xml = XMLReader.read("<pre>" + html + "</pre>", false);
					result.addAll(Arrays.asList(xml.getChildren(true)));
				} catch(ParseException e) {
					throw new IOException("parse error: " + e.getMessage());
				}
			}
			break;
		}
		default:
			throw new IOException("unknown instruction " + command.name);
		}
	}

	private static String processAttribute(Project project, Map<String, String> attrs, String value)
			throws IOException {
		int state = 0;
		StringBuilder result = new StringBuilder();
		StringBuilder tmp = null;
		for(char c : value.toCharArray()) {
			switch(state) {
			case 0: // normal
				if(c == '$') {
					state = 1;
				} else if(c == '\\') {
					state = 3;
				} else {
					result.append(c);
				}
				break;
			case 1: // $
				if(c == '{') {
					state = 2;
					tmp = new StringBuilder();
				} else if(c == '$') {
					result.append('$');
				} else {
					result.append('$');
					result.append(c);
					state = 0;
				}
				break;
			case 2: // ${
				if(c == '}') {
					state = 0;
					String var = tmp.toString();
					result.append(eval(project, attrs, var));
				} else {
					tmp.append(c);
				}
				break;
			case 3: // '\\'
				if(c == '$') {
					result.append('$');
				} else if(c == '\\') {
					result.append('\\');
				} else {
					result.append('\\');
					result.append(c);
				}
				state = 0;
			}
		}
		return result.toString();
	}

	private static boolean bool(String s) {
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equals("1");
	}

	private static void setSelfClosing(Element e) {
		e.preventSelfClosing = NO_SELF_CLOSING.contains(e.name);
	}

	private static String eval(Project project, Map<String, String> attrs, String expr) throws IOException {
		try(Reader in = new StringReader(expr)) {
			Parser p = new Parser(new Scanner(in));
			Node root = p.parseExpression();

			Context ctx = new Context(project.getContext());
			for(Entry<String, String> attr : attrs.entrySet()) {
				ctx.write(attr.getKey(), new Value(attr.getValue()));
			}

			Value result = root.execute(ctx);
			if(result == null) {
				return "";
			} else {
				return result.toString();
			}
		} catch(ParseException | ExecuteException e) {
			throw new IOException("Expression failed to execute: " + e.getMessage(), e);
		}
	}

	private static String processQuotes(String text) {
		return text.replaceAll("\"(\\w)", "“$1")
				.replaceAll("(\\w)\"", "$1”")
				.replaceAll("(\\w)'(\\w)", "$1’$2");
	}
}
