package com.example.csvpointbypoint.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SafeCsvPathValidator {
    private final Path allowedRoot;

    public SafeCsvPathValidator(@Value("${app.csv.allowed-root}") String allowedRoot) {
        this.allowedRoot = Path.of(allowedRoot);
    }

    public Path validate(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("CSV path must not be blank");
        }

        Path requested = Path.of(filePath);
        if (!requested.isAbsolute()) {
            throw new IllegalArgumentException("CSV path must be absolute: " + filePath);
        }

        Path rootReal = allowedRoot.toAbsolutePath().normalize().toRealPath();
        Path fileReal = requested.normalize().toRealPath();

        if (!fileReal.startsWith(rootReal)) {
            throw new IllegalArgumentException("CSV path is outside the allowed root: " + filePath);
        }
        if (!Files.isRegularFile(fileReal) || !Files.isReadable(fileReal)) {
            throw new IllegalArgumentException("CSV path must reference a readable regular file: " + filePath);
        }
        if (!fileReal.getFileName().toString().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are allowed: " + filePath);
        }

        return fileReal;
    }
}
