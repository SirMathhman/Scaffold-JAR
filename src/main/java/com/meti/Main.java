package com.meti;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.MessageFormat.*;

public class Main {
	public static final Path JAVA = Paths.get(".", "src", "main", "java");
	public static final Path COMPILE =Paths.get(".", "target", "compile");

	public static void main(String[] args) {
		new Main().run();
	}

	private void run(){
		Path manifest = COMPILE.resolve(".mf");
		if (Files.exists(manifest)) {
			List<String> command = new ArrayList<>();
			try {
				command.addAll(List.of("jar", "cmf", ".mf", ".jar"));
				Files.walk(COMPILE)
						.filter(path -> path.toString().endsWith(".class"))
						.map(COMPILE::relativize)
						.map(Path::toString)
						.forEach(command::add);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Process process = new ProcessBuilder(command)
						.directory(COMPILE.toFile())
						.start();
				process.getInputStream().transferTo(System.out);
				process.getErrorStream().transferTo(System.err);
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Files.createFile(manifest);
				Files.writeString(manifest, format("Manifest-Version: 1.0{0}Main-Class: {0}", System.lineSeparator()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
