package com.unknown.xsite;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.unknown.util.log.Levels;
import com.unknown.util.log.Trace;
import com.unknown.xml.dom.Element;
import com.unknown.xml.dom.XMLReader;
import com.unknown.xsite.el.Context;
import com.unknown.xsite.el.Program;

public class Project {
	private static final Logger log = Trace.create(Project.class);

	public static final String XMLNS_T = "https://unknown-tech.eu/xml/template";
	public static final String XMLNS_C = "https://unknown-tech.eu/xml/core";

	private static final CustomElement T_HTML;

	private Path rootPath;

	private String name;
	private Path templatePath;
	private Path pagePath;

	private boolean gzip;

	private Path outputPath;

	private Map<String, CustomElement> elements = new HashMap<>();
	private Map<String, Page> pages = new HashMap<>();
	private Map<String, String> vars = new HashMap<>();

	private final Program program = new Program();
	private final Context ctx = program.createContext();

	static {
		try {
			T_HTML = new CustomElement(XMLReader.read("<t:html xmlns:c=\"" + XMLNS_C + "\" xmlns:t=\"" +
					XMLNS_T + "\"><html><c:apply/></html></t:html>"));
		} catch(IOException | ParseException e) {
			log.log(Levels.FATAL, "Failed to construct default elements: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public Project(Path rootPath, Element xml) throws IOException {
		if(!xml.name.equals("project")) {
			throw new IOException("Not a project");
		}

		this.rootPath = rootPath;
		name = xml.getAttribute("name");

		templatePath = rootPath.resolve(xml.getAttribute("templates"));
		pagePath = rootPath.resolve(xml.getAttribute("pages"));

		for(Element e : xml.getChildren()) {
			if(e.name.equals("variable")) {
				vars.put(e.getAttribute("name"), e.getAttribute("value"));
				program.constant(e.getAttribute("name"), e.getAttribute("value"));
			}
		}

		gzip = false;
		String gzipFlag = xml.getAttribute("gzip");
		if(gzipFlag != null && Boolean.parseBoolean(gzipFlag)) {
			gzip = true;
		}
	}

	public Context getContext() {
		return ctx;
	}

	public String getVariable(String var) {
		return vars.get(var);
	}

	public String getName() {
		return name;
	}

	public boolean useGZIP() {
		return gzip;
	}

	public Path getRootPath() {
		return rootPath;
	}

	public Path getTemplatePath() {
		return templatePath;
	}

	public Path getPagePath() {
		return pagePath;
	}

	public void setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public Path getOutputPath(String filename) {
		return outputPath.resolve(filename);
	}

	public void loadTemplates() throws IOException {
		elements.clear();
		elements.put("html", T_HTML);

		try(DirectoryStream<Path> stream = Files.newDirectoryStream(templatePath)) {
			for(Path path : stream) {
				if(path.getFileName().toString().endsWith(".xml") && Files.isRegularFile(path)) {
					log.info("Loading file " + path + " ...");
					Element xml = loadXML(path);
					CustomElement element = new CustomElement(xml);
					log.info("Registering custom element \"" + element.getName() + "\"");
					elements.put(element.getName(), element);
				}
			}
		}
	}

	public void loadPages() throws IOException {
		pages.clear();
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(pagePath)) {
			for(Path path : stream) {
				if(path.getFileName().toString().endsWith(".xml") && Files.isRegularFile(path)) {
					loadPage(path, null);
				} else if(Files.isDirectory(path)) {
					// category
					loadCategory(path);
				}
			}
		}
	}

	private void loadPage(Path path, String category) throws IOException {
		String pageName = getName(path);
		String pageId;
		if(category != null) {
			pageId = category + "/" + pageName;
		} else {
			pageId = pageName;
		}
		log.info("Loading file " + path + " (" + pageName + ", category " + category + ") ...");
		Element xml = loadXML(path);
		pages.put(pageId, new Page(pageName, category, xml));
	}

	private void loadCategory(Path root) throws IOException {
		String category = root.getFileName().toString();
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
			for(Path path : stream) {
				if(path.getFileName().toString().endsWith(".xml") && Files.isRegularFile(path)) {
					loadPage(path, category);
				}
			}
		}
	}

	public Collection<Page> getPages() {
		return pages.values();
	}

	private static boolean eq(String a, String b) {
		if(a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

	public Collection<Page> getCategory(String category) {
		Set<Page> result = new HashSet<>();
		for(Page page : pages.values()) {
			if(eq(page.getCategory(), category)) {
				result.add(page);
			}
		}
		return result;
	}

	public CustomElement getElement(String element) {
		return elements.get(element);
	}

	public String processPage(Page page) throws IOException {
		Map<String, String> info = new HashMap<>();
		info.put("page.category", page.getCategory());
		info.put("page.name", page.getName());
		String path = null;
		String filename = page.getName();
		if(filename.equals("index")) {
			filename = "";
		}
		if(page.getCategory() != null) {
			path = page.getCategory() + "/" + filename;
		} else {
			path = filename;
		}
		info.put("page.path", "/" + path);
		List<Element> result = process(page.getXML(), info);
		if(result.size() != 1) {
			throw new IOException("invalid result: expected one element, got " + result.size());
		}
		Element element = result.get(0);
		return "<!DOCTYPE html>\n" + element.toRawString();
	}

	private List<Element> process(Element element, Map<String, String> info) throws IOException {
		if(element.uri.equals(XMLNS_T)) {
			CustomElement e = elements.get(element.name);
			if(e == null) {
				throw new IOException("unknown element " + element.name);
			} else {
				return e.apply(this, element, info);
			}
		} else if(element.name.equals("html")) {
			return T_HTML.apply(this, element, info);
		} else {
			throw new IOException("unexpected root element " + element.name);
		}
	}

	private static String getName(Path path) {
		String filename = path.getFileName().toString();
		int index = filename.lastIndexOf('.');
		if(index == -1) {
			return filename;
		} else {
			return filename.substring(0, index);
		}
	}

	public static Element loadXML(Path path) throws IOException {
		String data = Files.readString(path);
		try {
			return XMLReader.read(data);
		} catch(ParseException e) {
			throw new IOException("Failed to parse XML: " + e.getMessage(), e);
		}
	}
}
