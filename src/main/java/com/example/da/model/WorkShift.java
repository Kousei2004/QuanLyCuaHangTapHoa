package com.example.da.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkShift {
    private int shiftId;
    private int userId;
    private Timestamp startTime;
    private Timestamp endTime;
    private double totalSales;
    private double discrepancy;
    private Integer confirmedBy;
    private double cashSales;
    private double bankSales;
    private double actualCash;

    // Getters & Setters
    public int getShiftId() { return shiftId; }
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }
    public double getDiscrepancy() { return discrepancy; }
    public void setDiscrepancy(double discrepancy) { this.discrepancy = discrepancy; }
    public Integer getConfirmedBy() { return confirmedBy; }
    public void setConfirmedBy(Integer confirmedBy) { this.confirmedBy = confirmedBy; }
    public double getCashSales() { return cashSales; }
    public void setCashSales(double cashSales) { this.cashSales = cashSales; }
    public double getBankSales() { return bankSales; }
    public void setBankSales(double bankSales) { this.bankSales = bankSales; }
    public double getActualCash() { return actualCash; }
    public void setActualCash(double actualCash) { this.actualCash = actualCash; }

    // Lấy tất cả ca làm việc
    public static List<WorkShift> getAll() {
        List<WorkShift> list = new ArrayList<>();
        String sql = "SELECT * FROM work_shifts ORDER BY start_time DESC";
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Thêm ca làm việc mới
    public boolean insert() {
        String sql = "INSERT INTO work_shifts (user_id, start_time, end_time, total_sales, discrepancy, confirmed_by, cash_sales, bank_sales, actual_cash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, startTime);
            ps.setTimestamp(3, endTime);
            ps.setDouble(4, totalSales);
            ps.setDouble(5, discrepancy);
            if (confirmedBy != null) ps.setInt(6, confirmedBy); else ps.setNull(6, Types.INTEGER);
            ps.setDouble(7, cashSales);
            ps.setDouble(8, bankSales);
            ps.setDouble(9, actualCash);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.shiftId = rs.getInt(1);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật ca làm việc
    public boolean update() {
        String sql = "UPDATE work_shifts SET user_id=?, start_time=?, end_time=?, total_sales=?, discrepancy=?, confirmed_by=?, cash_sales=?, bank_sales=?, actual_cash=? WHERE shift_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, startTime);
            ps.setTimestamp(3, endTime);
            ps.setDouble(4, totalSales);
            ps.setDouble(5, discrepancy);
            if (confirmedBy != null) ps.setInt(6, confirmedBy); else ps.setNull(6, Types.INTEGER);
            ps.setDouble(7, cashSales);
            ps.setDouble(8, bankSales);
            ps.setDouble(9, actualCash);
            ps.setInt(10, shiftId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa ca làm việc
    public boolean delete() {
        String sql = "DELETE FROM work_shifts WHERE shift_id=?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy ca làm việc hiện tại của user (chưa kết thúc)
    public static WorkShift getCurrentShiftForUser(int userId) {
        String sql = "SELECT * FROM work_shifts WHERE user_id=? AND end_time IS NULL ORDER BY start_time DESC LIMIT 1";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return fromResultSet(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tính tổng doanh thu của ca làm việc này
    public double calculateTotalSales() {
        double total = 0.0;
        String sql = "SELECT SUM(total_amount) FROM orders WHERE user_id=? AND order_date >= ? AND (order_date <= ? OR ? IS NULL) AND status='completed'";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, startTime);
            if (endTime != null) {
                ps.setTimestamp(3, endTime);
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Tính tổng tiền mặt của ca làm việc này
    public double calculateCashSales() {
        double total = 0.0;
        String sql = "SELECT SUM(total_amount) FROM orders WHERE user_id=? AND order_date >= ? AND (order_date <= ? OR ? IS NULL) AND status='completed' AND payment_method='cash'";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, startTime);
            if (endTime != null) {
                ps.setTimestamp(3, endTime);
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Tính tổng chuyển khoản của ca làm việc này
    public double calculateBankSales() {
        double total = 0.0;
        String sql = "SELECT SUM(total_amount) FROM orders WHERE user_id=? AND order_date >= ? AND (order_date <= ? OR ? IS NULL) AND status='completed' AND payment_method='bank'";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, startTime);
            if (endTime != null) {
                ps.setTimestamp(3, endTime);
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Mở ca mới cho user, start_time lấy từ end_time của ca trước nếu có
    public static boolean openNewShiftForUser(int userId) {
        java.sql.Timestamp startTime = new java.sql.Timestamp(System.currentTimeMillis());
        // Tìm ca gần nhất đã kết thúc
        String sql = "SELECT end_time FROM work_shifts WHERE user_id=? AND end_time IS NOT NULL ORDER BY end_time DESC LIMIT 1";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp lastEnd = rs.getTimestamp(1);
                    if (lastEnd != null) startTime = lastEnd;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Tạo ca mới
        WorkShift shift = new WorkShift();
        shift.setUserId(userId);
        shift.setStartTime(startTime);
        shift.setEndTime(null);
        shift.setTotalSales(0.0);
        shift.setDiscrepancy(0.0);
        shift.setConfirmedBy(null);
        return shift.insert();
    }

    // Helper: chuyển ResultSet thành WorkShift
    private static WorkShift fromResultSet(ResultSet rs) throws SQLException {
        WorkShift ws = new WorkShift();
        ws.shiftId = rs.getInt("shift_id");
        ws.userId = rs.getInt("user_id");
        ws.startTime = rs.getTimestamp("start_time");
        ws.endTime = rs.getTimestamp("end_time");
        ws.totalSales = rs.getDouble("total_sales");
        ws.discrepancy = rs.getDouble("discrepancy");
        ws.confirmedBy = (Integer)rs.getObject("confirmed_by");
        try { ws.cashSales = rs.getDouble("cash_sales"); } catch(Exception e) { ws.cashSales = 0.0; }
        try { ws.bankSales = rs.getDouble("bank_sales"); } catch(Exception e) { ws.bankSales = 0.0; }
        try { ws.actualCash = rs.getDouble("actual_cash"); } catch(Exception e) { ws.actualCash = 0.0; }
        return ws;
    }
} 