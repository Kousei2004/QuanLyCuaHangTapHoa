package com.example.da.dialog;

import com.example.da.model.Users;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EmployeeFormDialogController {
    @FXML private TextField txtFullName, txtUsername, txtEmail, txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole, cbStatus;

    private Users employee; // null nếu là thêm mới

    public void setEmployee(Users employee) {
        this.employee = employee;
        if (employee != null) {
            txtFullName.setText(employee.getFull_name());
            txtUsername.setText(employee.getUsername());
            txtEmail.setText(employee.getEmail());
            txtPhone.setText(employee.getPhone());
            cbRole.setValue(employee.getRole().toString());
            cbStatus.setValue(employee.getStatus().toString());
            txtPassword.setText(employee.getPassword());
        }
    }

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("nhanvien", "quanly");
        cbStatus.getItems().addAll("active", "inactive");
    }

    public Users getEmployeeFromForm() {
        Users u = (employee == null) ? new Users() : employee;
        u.setFull_name(txtFullName.getText());
        u.setUsername(txtUsername.getText());
        u.setPassword(txtPassword.getText());
        u.setEmail(txtEmail.getText());
        u.setPhone(txtPhone.getText());
        u.setRole(Users.Role.valueOf(cbRole.getValue()));
        u.setStatus(Users.Status.valueOf(cbStatus.getValue()));
        return u;
    }
} 