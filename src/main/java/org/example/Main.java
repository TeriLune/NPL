package org.example;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        try {
            Configurations configs = new Configurations();
            URL resourceUrl = Main.class.getClassLoader().getResource("appsettings.properties");

            if (resourceUrl == null) {
                System.err.println("File appsettings.properties not found.");
                return;
            }

            File propertiesFile = new File(resourceUrl.toURI());
            Configuration config = configs.properties(propertiesFile);

            String dirPathStr = config.getString("orders.directory", "./orders");
            Path ordersDir = Path.of(dirPathStr);

            if (!Files.exists(ordersDir) || !Files.isDirectory(ordersDir)) {
                System.out.println("Orders directory does not exist: " + ordersDir.toAbsolutePath());
                return;
            }

            List<Path> jsonFiles;
            try (Stream<Path> stream = Files.list(ordersDir)) {
                jsonFiles = stream
                        .filter(file -> !Files.isDirectory(file))
                        .filter(file -> file.getFileName().toString().toLowerCase().endsWith(".json"))
                        .toList();
            }

            if (jsonFiles.isEmpty()) {
                System.out.println("No JSON files found in: " + ordersDir.toAbsolutePath());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();

            for (Path file : jsonFiles) {
                OrderRequest order = mapper.readValue(file.toFile(), OrderRequest.class);

                String generatedNumber = OrderNumberGenerator.generate(order);
                order.setOrderNumber(generatedNumber);

                System.out.println("File: " + file.getFileName() + " | Order number: " + order.getOrderNumber());
            }

        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
        }
    }
}