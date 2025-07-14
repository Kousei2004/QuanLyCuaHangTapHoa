package com.example.da.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private int categoryId;
    private String name;
    private int displayOrder;
    private String description;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Category() {}

    public Category(int categoryId, String name, int displayOrder, String description, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.displayOrder = displayOrder;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public static List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY display_order, name";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        } catch (Exception e) {
            System.out.println("[Category.getAll] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert() {
        String sql = "INSERT INTO categories (name, display_order, description, status) VALUES (?, ?, ?, ?)";
        System.out.println("[Category.insert] Params: name=" + name + ", displayOrder=" + displayOrder + ", description=" + description + ", status=" + status);
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, displayOrder);
            ps.setString(3, description);
            ps.setString(4, status);
            int affected = ps.executeUpdate();
            System.out.println("[Category.insert] Rows affected: " + affected);
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.categoryId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[Category.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE categories SET name=?, display_order=?, description=?, status=? WHERE category_id=?";
        System.out.println("[Category.update] Params: name=" + name + ", displayOrder=" + displayOrder + ", description=" + description + ", status=" + status + ", categoryId=" + categoryId);
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, displayOrder);
            ps.setString(3, description);
            ps.setString(4, status);
            ps.setInt(5, categoryId);
            int affected = ps.executeUpdate();
            System.out.println("[Category.update] Rows affected: " + affected);
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Category.update] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM categories WHERE category_id=?";
        System.out.println("[Category.delete] Params: categoryId=" + categoryId);
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            int affected = ps.executeUpdate();
            System.out.println("[Category.delete] Rows affected: " + affected);
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Category.delete] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static Category fromResultSet(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("category_id"),
            rs.getString("name"),
            rs.getInt("display_order"),
            rs.getString("description"),
            rs.getString("status"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }

    public static Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return fromResultSet(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("[Category.getById] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
} 