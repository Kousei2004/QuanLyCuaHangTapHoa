package com.example.da.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Promotion {
    private int promo_id;
    private String code;
    private String description;
    private double discount_percent;
    private Date start_date;
    private Date end_date;
    private String status;

    // Constructors
    public Promotion() {}
    public Promotion(int promo_id, String code, String description, double discount_percent, Date start_date, Date end_date, String status) {
        this.promo_id = promo_id;
        this.code = code;
        this.description = description;
        this.discount_percent = discount_percent;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
    }

    // Getters and Setters
    public int getPromo_id() { return promo_id; }
    public void setPromo_id(int promo_id) { this.promo_id = promo_id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getDiscount_percent() { return discount_percent; }
    public void setDiscount_percent(double discount_percent) { this.discount_percent = discount_percent; }
    public Date getStart_date() { return start_date; }
    public void setStart_date(Date start_date) { this.start_date = start_date; }
    public Date getEnd_date() { return end_date; }
    public void setEnd_date(Date end_date) { this.end_date = end_date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // CRUD Methods
    public static List<Promotion> getAllPromotions() {
        List<Promotion> promotions = new ArrayList<>();
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM promotions")) {
            while (rs.next()) {
                Promotion p = new Promotion(
                    rs.getInt("promo_id"),
                    rs.getString("code"),
                    rs.getString("description"),
                    rs.getDouble("discount_percent"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status")
                );
                promotions.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return promotions;
    }

    public static Promotion getPromotionById(int id) {
        Promotion p = null;
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM promotions WHERE promo_id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Promotion(
                    rs.getInt("promo_id"),
                    rs.getString("code"),
                    rs.getString("description"),
                    rs.getDouble("discount_percent"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public boolean insertPromotion() {
        String sql = "INSERT INTO promotions (code, description, discount_percent, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, description);
            ps.setDouble(3, discount_percent);
            ps.setDate(4, start_date);
            ps.setDate(5, end_date);
            ps.setString(6, status == null ? "active" : status);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePromotion() {
        String sql = "UPDATE promotions SET code=?, description=?, discount_percent=?, start_date=?, end_date=?, status=? WHERE promo_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, description);
            ps.setDouble(3, discount_percent);
            ps.setDate(4, start_date);
            ps.setDate(5, end_date);
            ps.setString(6, status);
            ps.setInt(7, promo_id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deletePromotion(int id) {
        String sql = "DELETE FROM promotions WHERE promo_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 