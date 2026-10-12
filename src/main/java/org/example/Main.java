package org.example;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    static void main() {
        try {
            Configurations configs = new Configurations();
            URL resourceUrl = Main.class.getClassLoader().getResource("appsettings.properties");

            if (resourceUrl == null) {
                System.err.println("Nie znaleziono pliku konfiguracyjnego appsettings.properties w zasobach.");
                return;
            }

            File propertiesFile = new File(resourceUrl.toURI());
            Configuration config = configs.properties(propertiesFile);

            String dirPathStr = config.getString("orders.directory", "orders");
            Path ordersDir = Path.of(dirPathStr);

            if (!Files.exists(ordersDir) || !Files.isDirectory(ordersDir)) {
                System.out.println("Katalog z zamówieniami nie istnieje: " + ordersDir.toAbsolutePath());
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
                System.out.println("Brak plików JSON w katalogu: " + ordersDir.toAbsolutePath());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();

            for (Path file : jsonFiles) {
                OrderRequest order = mapper.readValue(file.toFile(), OrderRequest.class);
                String orderNumber = generateDeterministicOrderNumber(order);
                System.out.println("Plik: " + file.getFileName() + " | Numer zamówienia: " + orderNumber);
            }

        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas działania programu: " + e.getMessage());
        }
    }

    private static String generateDeterministicOrderNumber(OrderRequest order) {
        try {
            StringBuilder sb = new StringBuilder();
            if (order.customer() != null) {
                sb.append(order.customer().firstName()).append(";")
                        .append(order.customer().lastName()).append(";")
                        .append(order.customer().email()).append(";")
                        .append(order.customer().phone()).append(";");
            }
            if (order.items() != null) {
                for (OrderItem item : order.items()) {
                    sb.append(item.productCode()).append(":")
                            .append(item.quantity()).append(":")
                            .append(item.unit()).append(";");
                }
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return "ORD-" + hexString.toString().substring(0, 10).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania numeru zamówienia", e);
        }
    }
}