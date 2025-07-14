package com.example.da.model;

import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {
    private int poId;
    private Integer supplierId;
    private Integer userId;
    private LocalDateTime orderDate;
    private String note;

    public PurchaseOrder() {}

    public PurchaseOrder(int poId, Integer supplierId, Integer userId, LocalDateTime orderDate, String note) {
        this.poId = poId;
        this.supplierId = supplierId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.note = note;
    }

    public int getPoId() {
        return poId;
    }

    public void setPoId(int poId) {
        this.poId = poId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static List<PurchaseOrder> getAll() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_orders ORDER BY po_id DESC";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrder.getAll] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert() {
        String sql = "INSERT INTO purchase_orders (supplier_id, user_id, order_date, note) VALUES (?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (supplierId != null) ps.setInt(1, supplierId); else ps.setNull(1, Types.INTEGER);
            if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
            if (orderDate != null) ps.setTimestamp(3, Timestamp.valueOf(orderDate)); else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, note);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.poId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrder.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE purchase_orders SET supplier_id=?, user_id=?, order_date=?, note=? WHERE po_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (supplierId != null) ps.setInt(1, supplierId); else ps.setNull(1, Types.INTEGER);
            if (userId != null) ps.setInt(2, userId); else ps.setNull(2, Types.INTEGER);
            if (orderDate != null) ps.setTimestamp(3, Timestamp.valueOf(orderDate)); else ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, note);
            ps.setInt(5, poId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[PurchaseOrder.update] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM purchase_orders WHERE po_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[PurchaseOrder.delete] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static PurchaseOrder getById(int id) {
        String sql = "SELECT * FROM purchase_orders WHERE po_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrder.getById] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static PurchaseOrder fromResultSet(ResultSet rs) throws SQLException {
        return new PurchaseOrder(
            rs.getInt("po_id"),
            (Integer)rs.getObject("supplier_id"),
            (Integer)rs.getObject("user_id"),
            rs.getTimestamp("order_date") != null ? rs.getTimestamp("order_date").toLocalDateTime() : null,
            rs.getString("note")
        );
    }
} 