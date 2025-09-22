package learning.jakarta.ai.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MavenProcessUtility {

    public static void invokeMavenArchetype(String archetypeGroupId, String archetypeArtifactId,
            String archetypeVersion, Properties properties, File workingDirectory) {

        // Ensure working directory exists
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new RuntimeException("Failed to create working directory: " + workingDirectory.getAbsolutePath());
        }

        // Build Maven command
        List<String> command = new ArrayList<>();

        // Determine Maven executable
        String mvnCommand = findMavenExecutable();
        command.add(mvnCommand);

        // Add Maven arguments
        command.add("archetype:generate");
        command.add("-DinteractiveMode=false");
        command.add("-DaskForDefaultPropertyValues=false");
        command.add("-DarchetypeGroupId=" + archetypeGroupId);
        command.add("-DarchetypeArtifactId=" + archetypeArtifactId);
        command.add("-DarchetypeVersion=" + archetypeVersion);

        // Add properties
        properties.forEach((k, v) -> command.add("-D" + k + "=" + v));

        log.info("Executing Maven command: {}", String.join(" ", command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDirectory);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug(line);
                }
            }

            // Wait for process to complete with timeout
            boolean finished = process.waitFor(120, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Maven archetype generation timed out after 120 seconds");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new RuntimeException(String.format(
                    "Failed to invoke Maven Archetype command. Exit code: %d. Output: %s",
                    exitCode, output.toString()
                ));
            }

            log.info("Maven archetype generation completed successfully");

        } catch (IOException | InterruptedException e) {
            log.error("Error executing Maven process", e);
            throw new RuntimeException("Failed to execute Maven archetype generation", e);
        }
    }

    private static String findMavenExecutable() {
        // Check if mvn is available in PATH
        String os = System.getProperty("os.name").toLowerCase();
        String mvnCommand = os.contains("win") ? "mvn.cmd" : "mvn";

        // Try to find Maven home
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }

        if (mavenHome != null) {
            File mvnExecutable = new File(mavenHome, "bin/" + mvnCommand);
            if (mvnExecutable.exists() && mvnExecutable.canExecute()) {
                return mvnExecutable.getAbsolutePath();
            }
        }

        // Check if mvn is in PATH
        try {
            Process process = Runtime.getRuntime().exec(new String[]{mvnCommand, "--version"});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return mvnCommand;
            }
        } catch (IOException | InterruptedException e) {
            log.debug("mvn not found in PATH", e);
        }

        // Try common Maven installation locations
        String[] commonLocations = {
            "/usr/local/bin/mvn",
            "/usr/bin/mvn",
            "/opt/maven/bin/mvn",
            System.getProperty("user.home") + "/.m2/wrapper/mvn"
        };

        for (String location : commonLocations) {
            File mvnExecutable = new File(location);
            if (mvnExecutable.exists() && mvnExecutable.canExecute()) {
                return mvnExecutable.getAbsolutePath();
            }
        }

        // Fallback to mvn and hope it's in PATH
        log.warn("Could not find Maven executable, falling back to 'mvn' command");
        return mvnCommand;
    }
}