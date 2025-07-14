package com.example.da.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private Integer customerId;
    private Integer userId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private Integer promoId;
    private String paymentMethod;

    public Order() {}

    public Order(Integer customerId, Integer userId, LocalDateTime orderDate, Double totalAmount, String status, Integer promoId) {
        this.customerId = customerId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.promoId = promoId;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPromoId() { return promoId; }
    public void setPromoId(Integer promoId) { this.promoId = promoId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean insert() {
        String sql = "INSERT INTO orders (customer_id, user_id, order_date, total_amount, status, promo_id, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (customerId != null) ps.setInt(1, customerId); else ps.setNull(1, Types.INTEGER);
            if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
            if (orderDate != null) ps.setTimestamp(3, Timestamp.valueOf(orderDate)); else ps.setNull(3, Types.TIMESTAMP);
            if (totalAmount != null) ps.setDouble(4, totalAmount); else ps.setNull(4, Types.DECIMAL);
            ps.setString(5, status != null ? status : "open");
            if (promoId != null) ps.setInt(6, promoId); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, paymentMethod);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.orderId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[Order.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static List<Order> getOrdersForShift(int userId, java.sql.Timestamp start, java.sql.Timestamp end, String paymentMethod) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id=? AND order_date >= ? AND order_date <= ? AND status='completed' AND payment_method=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, start);
            ps.setTimestamp(3, end != null ? end : new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setString(4, paymentMethod);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setUserId(rs.getInt("user_id"));
                    o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
                    o.setTotalAmount(rs.getDouble("total_amount"));
                    o.setStatus(rs.getString("status"));
                    o.setPromoId((Integer)rs.getObject("promo_id"));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    // ... các trường khác nếu cần
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Order> getAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setCustomerId((Integer)rs.getObject("customer_id"));
                o.setUserId((Integer)rs.getObject("user_id"));
                o.setOrderDate(rs.getTimestamp("order_date") != null ? rs.getTimestamp("order_date").toLocalDateTime() : null);
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setStatus(rs.getString("status"));
                o.setPromoId((Integer)rs.getObject("promo_id"));
                o.setPaymentMethod(rs.getString("payment_method"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean update() {
        String sql = "UPDATE orders SET customer_id=?, user_id=?, order_date=?, total_amount=?, status=?, promo_id=?, payment_method=? WHERE order_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (customerId != null) ps.setInt(1, customerId); else ps.setNull(1, Types.INTEGER);
            if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
            if (orderDate != null) ps.setTimestamp(3, Timestamp.valueOf(orderDate)); else ps.setNull(3, Types.TIMESTAMP);
            if (totalAmount != null) ps.setDouble(4, totalAmount); else ps.setNull(4, Types.DECIMAL);
            ps.setString(5, status != null ? status : "open");
            if (promoId != null) ps.setInt(6, promoId); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, paymentMethod);
            ps.setInt(8, orderId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Order.update] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM orders WHERE order_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Order.delete] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
} 