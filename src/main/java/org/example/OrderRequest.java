package org.example;

import java.util.List;

public record OrderRequest(Customer customer, List<OrderItem> items) {}