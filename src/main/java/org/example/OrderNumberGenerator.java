package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class OrderNumberGenerator {

    public static String generate(OrderRequest order) {
        try {
            StringBuilder sb = new StringBuilder();

            if (order.getCustomer() != null) {
                Customer c = order.getCustomer();
                sb.append(c.getFirstName()).append(";")
                        .append(c.getLastName()).append(";")
                        .append(c.getEmail()).append(";")
                        .append(c.getPhone()).append(";");
            }

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    sb.append(item.getProductCode()).append(":")
                            .append(item.getQuantity()).append(":")
                            .append(item.getUnit()).append(";");
                }
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            String hex = HexFormat.of().withUpperCase().formatHex(hashBytes);
            return "ORD-" + hex.substring(0, 10);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm SHA-256 not available", e);
        }
    }
}