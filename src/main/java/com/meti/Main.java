package com.meti;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

public class Main {
	public static final Path COMPILE = Paths.get(".", "target", "compile");
	public static final Logger logger = Logger.getLogger("Scaffold");

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		Path manifest = COMPILE.resolve(".mf");
		if (Files.exists(manifest)) {
			logger.log(Level.INFO, String.format("Successfully found manifest file at %s", manifest));
			List<String> command = buildCommand();
			executeCommand(command);
		} else {
			createManifest(manifest);
		}
	}

	private List<String> buildCommand() {
		List<String> command = new ArrayList<>();
		try {
			command.addAll(List.of("jar", "cmf", ".mf", ".jar"));
			Files.walk(COMPILE)
					.filter(path -> path.toString().endsWith(".class"))
					.map(COMPILE::relativize)
					.map(Path::toString)
					.forEach(command::add);
		} catch (IOException e) {
			logger.log(Level.SEVERE, format("Failed to build command {0}",
					String.join(" ", command)));
		}
		return command;
	}

	private static void executeCommand(List<String> command) {
		try {
			logger.log(Level.INFO, "Executing JAR assembly.");
			Process process = new ProcessBuilder(command)
					.directory(COMPILE.toFile())
					.start();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
			process.getInputStream().transferTo(outputStream);
			process.getErrorStream().transferTo(errorStream);
			process.waitFor();
			String outputString = outputStream.toString();
			String errorString = errorStream.toString();
			if (errorString.isBlank()) {
				if (outputString.isEmpty()) {
					logger.log(Level.INFO, "Assembly completed successfully.");
				} else {
					logger.log(Level.INFO, format("Assembly completed: {0}\t{1}",
							System.lineSeparator(), outputString));
				}
			} else {
				logger.log(Level.WARNING, format("Failed to execute jar command \"{0}\":{1}\t{2}",
						String.join(" ", command), System.lineSeparator(), errorString));
			}
		} catch (IOException | InterruptedException e) {
			logger.log(Level.SEVERE, format("Unable to assemble jar using command:{0}\t\"{1}\"",
					System.lineSeparator(), String.join(" ", command)), e);
		}
	}

	private static void createManifest(Path manifest) {
		try {
			logger.log(Level.INFO, String.format("Manifest file was not present, creating it at %s", manifest));
			Files.createFile(manifest);
			Files.writeString(manifest, format("Manifest-Version: 1.0{0}Main-Class: {0}Class-Path: {0}",
					System.lineSeparator()));
		} catch (IOException e) {
			logger.log(Level.SEVERE, String.format("Failed to create manifest file at %s", manifest), e);
		}
	}
}
