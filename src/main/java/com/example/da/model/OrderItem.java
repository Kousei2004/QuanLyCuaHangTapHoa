package com.example.da.model;

import java.math.BigDecimal;
import java.sql.*;

public class OrderItem {
    private int itemId;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    public OrderItem() {}

    public OrderItem(Integer orderId, Integer productId, Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public boolean insert() {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (orderId != null) ps.setInt(1, orderId); else ps.setNull(1, Types.INTEGER);
            if (productId != null) ps.setInt(2, productId); else ps.setNull(2, Types.INTEGER);
            if (quantity != null) ps.setInt(3, quantity); else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, unitPrice);
            ps.setBigDecimal(5, discount != null ? discount : BigDecimal.ZERO);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.itemId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[OrderItem.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static java.util.List<OrderItem> getByOrderId(int orderId) {
        java.util.List<OrderItem> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id=?";
        try (java.sql.Connection conn = MySQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setOrderId((Integer)rs.getObject("order_id"));
                    item.setProductId((Integer)rs.getObject("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setDiscount(rs.getBigDecimal("discount"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
} 