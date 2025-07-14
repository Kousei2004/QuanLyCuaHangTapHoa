package com.example.da.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderItem {
    private int itemId;
    private Integer poId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public PurchaseOrderItem() {}

    public PurchaseOrderItem(int itemId, Integer poId, Integer productId, Integer quantity, BigDecimal unitPrice) {
        this.itemId = itemId;
        this.poId = poId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Integer getPoId() {
        return poId;
    }

    public void setPoId(Integer poId) {
        this.poId = poId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public static List<PurchaseOrderItem> getAll() {
        List<PurchaseOrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_order_items ORDER BY item_id DESC";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.getAll] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static List<PurchaseOrderItem> getByPurchaseOrderId(int poId) {
        List<PurchaseOrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM purchase_order_items WHERE po_id=? ORDER BY item_id DESC";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, poId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromResultSet(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.getByPurchaseOrderId] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert() {
        String sql = "INSERT INTO purchase_order_items (po_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (poId != null) ps.setInt(1, poId); else ps.setNull(1, Types.INTEGER);
            if (productId != null) ps.setInt(2, productId); else ps.setNull(2, Types.INTEGER);
            if (quantity != null) ps.setInt(3, quantity); else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, unitPrice);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.itemId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.insert] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String sql = "UPDATE purchase_order_items SET po_id=?, product_id=?, quantity=?, unit_price=? WHERE item_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (poId != null) ps.setInt(1, poId); else ps.setNull(1, Types.INTEGER);
            if (productId != null) ps.setInt(2, productId); else ps.setNull(2, Types.INTEGER);
            if (quantity != null) ps.setInt(3, quantity); else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, unitPrice);
            ps.setInt(5, itemId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.update] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String sql = "DELETE FROM purchase_order_items WHERE item_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.delete] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static PurchaseOrderItem getById(int id) {
        String sql = "SELECT * FROM purchase_order_items WHERE item_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        } catch (Exception e) {
            System.out.println("[PurchaseOrderItem.getById] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static PurchaseOrderItem fromResultSet(ResultSet rs) throws SQLException {
        return new PurchaseOrderItem(
            rs.getInt("item_id"),
            (Integer)rs.getObject("po_id"),
            (Integer)rs.getObject("product_id"),
            (Integer)rs.getObject("quantity"),
            rs.getBigDecimal("unit_price")
        );
    }
} 