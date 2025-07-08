package com.example.da.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;

public class Users {
    private int user_id;
    private String username;
    private String password;
    private String full_name;
    private String email;
    private String phone;
    private Role role = Role.nhanvien;
    private Status status = Status.active;

    public enum Role {
        nhanvien, quanly
    }

    public enum Status {
        active, inactive
    }

    public Users() {
    }

    public Users(int user_id, String username, String password, String full_name, String email, String phone, Role role, Status status) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    // Getters and Setters
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // SQL Query Methods for Employee Management
    
    // Lấy tất cả nhân viên
    public static List<Users> getAllEmployees() throws SQLException, ClassNotFoundException {
        List<Users> employees = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Users user = new Users();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setStatus(Status.valueOf(rs.getString("status")));
                employees.add(user);
            }
        }
        return employees;
    }
    
    // Tìm kiếm nhân viên
    public static List<Users> searchEmployees(String searchTerm) throws SQLException, ClassNotFoundException {
        List<Users> employees = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE full_name LIKE ? OR username LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY user_id DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Users user = new Users();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setStatus(Status.valueOf(rs.getString("status")));
                employees.add(user);
            }
        }
        return employees;
    }
    
    // Lọc nhân viên theo role
    public static List<Users> filterByRole(Role role) throws SQLException, ClassNotFoundException {
        List<Users> employees = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY user_id DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Users user = new Users();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setStatus(Status.valueOf(rs.getString("status")));
                employees.add(user);
            }
        }
        return employees;
    }
    
    // Lọc nhân viên theo status
    public static List<Users> filterByStatus(Status status) throws SQLException, ClassNotFoundException {
        List<Users> employees = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY user_id DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Users user = new Users();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setStatus(Status.valueOf(rs.getString("status")));
                employees.add(user);
            }
        }
        return employees;
    }
    
    // Thêm nhân viên mới
    public static boolean addEmployee(Users employee) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getUsername());
            String password = employee.getPassword();
            stmt.setString(2, isHashed(password) ? password : hashPassword(password));
            stmt.setString(3, employee.getFull_name());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getRole().toString());
            stmt.setString(7, employee.getStatus().toString());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Cập nhật thông tin nhân viên
    public static boolean updateEmployee(Users employee) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ?, phone = ?, role = ?, status = ? WHERE user_id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getUsername());
            String password = employee.getPassword();
            stmt.setString(2, isHashed(password) ? password : hashPassword(password));
            stmt.setString(3, employee.getFull_name());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getRole().toString());
            stmt.setString(7, employee.getStatus().toString());
            stmt.setInt(8, employee.getUser_id());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Xóa nhân viên
    public static boolean deleteEmployee(int userId) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Khóa/Mở khóa tài khoản
    public static boolean toggleAccountStatus(int userId, Status status) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.toString());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Đếm tổng số nhân viên
    public static int getTotalEmployeeCount() throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // Đếm số nhân viên đang hoạt động
    public static int getActiveEmployeeCount() throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'active'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // Đếm số tài khoản bị khóa
    public static int getLockedAccountCount() throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'inactive'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // Kiểm tra username đã tồn tại chưa
    public static boolean isUsernameExists(String username) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    // Lấy nhân viên theo ID
    public static Users getEmployeeById(int userId) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Users user = new Users();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setStatus(Status.valueOf(rs.getString("status")));
                return user;
            }
        }
        return null;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isHashed(String password) {
        return password != null && password.matches("[a-fA-F0-9]{64}");
    }
} 