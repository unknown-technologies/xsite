package com.unknown.xsite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import com.unknown.util.log.Levels;
import com.unknown.util.log.Trace;

public class XSiteGenerator {
	private static final Logger log = Trace.create(XSiteGenerator.class);

	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.out.println("Usage: XSiteGenerator project.xml output-dir");
			System.exit(1);
		}

		Trace.setupConsoleApplication(Levels.INFO);

		String projectFile = args[0];
		String outputPath = args[1];

		Path projectPath = Paths.get(projectFile).toAbsolutePath();
		Path rootPath = projectPath.getParent();

		Project project = new Project(rootPath, Project.loadXML(projectPath));
		project.setOutputPath(Paths.get(outputPath).toAbsolutePath());

		log.info("Processing project \"" + project.getName() + "\" at \"" + project.getRootPath() +
				"\" with output path \"" + project.getOutputPath() + "\"");
		log.info("Template path: " + project.getTemplatePath());
		log.info("Page path: " + project.getPagePath());

		project.loadTemplates();
		project.loadPages();

		for(Page page : project.getPages()) {
			String result = project.processPage(page);
			String out;
			if(page.getCategory() != null) {
				out = page.getCategory() + "/" + page.getName();
				Path dir = project.getOutputPath(page.getCategory());
				if(!Files.exists(dir)) {
					Files.createDirectory(dir);
				}
			} else {
				out = page.getName();
			}
			Path output = project.getOutputPath(out);
			log.info("Writing page " + output + " ...");
			byte[] data = result.getBytes(StandardCharsets.UTF_8);
			Files.write(output.resolveSibling(output.getFileName() + ".html"), data);
			if(project.useGZIP()) {
				byte[] gzip = gzip(data);
				if(gzip != null) {
					Files.write(output.resolveSibling(output.getFileName() + ".html.gz"), gzip);
				}
			}
		}
	}

	private static class GZOutputStream extends GZIPOutputStream {
		public GZOutputStream(OutputStream out) throws IOException {
			super(out);
			def.setLevel(Deflater.BEST_COMPRESSION);
		}
	}

	private static byte[] gzip(byte[] data) {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZOutputStream gzip = new GZOutputStream(out)) {
			gzip.write(data);
			gzip.flush();
			gzip.close();
			out.flush();
			return out.toByteArray();
		} catch(IOException e) {
			return null;
		}
	}
}
