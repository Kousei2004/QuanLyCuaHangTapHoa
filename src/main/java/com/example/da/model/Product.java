package com.example.da.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private int productId;
    private String name;
    private String barcode;
    private String description;
    private double price;
    private int quantityInStock;
    private String status;
    private int categoryId;

    public Product() {}

    public Product(int productId, String name, String barcode, String description, double price, int quantityInStock, String status, int categoryId) {
        this.productId = productId;
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.status = status;
        this.categoryId = categoryId;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public static List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id DESC";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        } catch (Exception e) {
            System.out.println("[Product.getAll] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert() {
        String sql = "INSERT INTO products (name, barcode, description, price, quantity_in_stock, status, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, barcode);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setInt(5, quantityInStock);
            ps.setString(6, status);
            if (categoryId > 0) ps.setInt(7, categoryId); else ps.setNull(7, Types.INTEGER);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.productId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[Product.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE products SET name=?, barcode=?, description=?, price=?, quantity_in_stock=?, status=?, category_id=? WHERE product_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, barcode);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setInt(5, quantityInStock);
            ps.setString(6, status);
            if (categoryId > 0) ps.setInt(7, categoryId); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, productId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Product.update] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM products WHERE product_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[Product.delete] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static Product fromResultSet(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("product_id"),
            rs.getString("name"),
            rs.getString("barcode"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getInt("quantity_in_stock"),
            rs.getString("status"),
            rs.getInt("category_id")
        );
    }
} 