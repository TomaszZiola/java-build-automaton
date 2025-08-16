package io.github.tomaszziola.javabuildautomaton;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class BuildService {

    public void startBuild() {
        System.out.println("Starting build...");

        try {
            File workingDir = new File("/Users/Tomasz/Documents/IdeaProjects/test");

            runCommand(workingDir, "git", "pull");

            runCommand(workingDir, "gradle", "clean", "build");

            System.out.println("Build was successful!");
        } catch (Exception e) {
            System.err.println("Error during building: " + e.getMessage());
        }
    }

    private void runCommand(File workingDir, String... command) throws IOException, InterruptedException {
        System.out.println("Executing command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        if (!process.waitFor(5, TimeUnit.MINUTES)) {
            process.destroy();
            throw new RuntimeException("Process exceeded time limit");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("Process has ended with error code: " + exitCode);
        }
    }
}
