package org.yearup.models;

import java.math.BigDecimal;

public class OrderLineItem {
    private int id;
    private int orderId;
    private int productId;
    private String name;
    private BigDecimal price;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;

    public OrderLineItem() {
    }


    public OrderLineItem(int id, int orderId, int productId, String name, BigDecimal price, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }


    @Override
    public String toString() {
        return String.format("%s - Quantity: %d - Price: $%.2f", name, quantity, price);
    }
}
