package learning.jakarta.ai.util;

import org.apache.maven.cli.MavenCli;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class MavenUtility {

    public static void invokeMavenArchetype(String archetypeGroupId, String archetypeArtifactId,
            String archetypeVersion, Properties properties, File workingDirectory) {
        
        // Ensure working directory exists
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new RuntimeException("Failed to create working directory: " + workingDirectory.getAbsolutePath());
        }

        // Set system properties for Maven
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.getAbsolutePath());
        System.setProperty("maven.repo.local", System.getProperty("user.home") + "/.m2/repository");
        System.setProperty("maven.home", System.getProperty("maven.home", System.getProperty("user.home") + "/.m2"));
        
        // Disable Maven transfer progress to reduce log noise
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss");
        
        // Set up Maven environment
        if (System.getProperty("java.home") == null) {
            System.setProperty("java.home", System.getProperty("java.home", "/usr/lib/jvm/default"));
        }
        
        List<String> options = new LinkedList<>();
        options.addAll(Arrays.asList(
            "archetype:generate", 
            "-DinteractiveMode=false",
            "-DaskForDefaultPropertyValues=false", 
            "-DarchetypeGroupId=" + archetypeGroupId,
            "-DarchetypeArtifactId=" + archetypeArtifactId, 
            "-DarchetypeVersion=" + archetypeVersion
        ));
        
        // Add properties
        properties.forEach((k, v) -> options.add("-D" + k + "=" + v));

        // Capture Maven output
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        try (PrintStream outStream = new PrintStream(baosOut);
             PrintStream errStream = new PrintStream(baosErr)) {
            
            log.info("Executing Maven command with options: {}", options);
            
            MavenCli cli = new MavenCli();
            int result = cli.doMain(
                options.toArray(new String[0]), 
                workingDirectory.getAbsolutePath(),
                outStream, 
                errStream
            );

            String output = baosOut.toString();
            String error = baosErr.toString();
            
            log.debug("Maven output: {}", output);
            if (!error.isEmpty()) {
                log.warn("Maven errors: {}", error);
            }

            if (result != 0) {
                StringBuilder mavenCommand = new StringBuilder("mvn");
                options.forEach(o -> mavenCommand.append(" ").append(o));
                
                throw new RuntimeException(String.format(
                    "Failed to invoke Maven Archetype command: %s. Exit code: %d. Error: %s", 
                    mavenCommand.toString(), result, error
                ));
            }
            
            log.info("Maven archetype generation completed successfully");
            
        } catch (Exception e) {
            log.error("Error during Maven execution", e);
            throw new RuntimeException("Failed to execute Maven archetype generation", e);
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
