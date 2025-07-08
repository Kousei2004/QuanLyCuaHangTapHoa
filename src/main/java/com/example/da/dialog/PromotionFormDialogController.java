package com.example.da.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class PromotionFormDialogController {
    @FXML private DialogPane dialogPane;
    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;
    @FXML private TextField txtDiscountPercent;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cmbStatus;

    private boolean isEdit = false;
    private int promoId = -1;

    public void setPromotionData(String code, String description, double discountPercent, LocalDate startDate, LocalDate endDate, String status, int promoId) {
        txtCode.setText(code);
        txtDescription.setText(description);
        txtDiscountPercent.setText(String.valueOf(discountPercent));
        dpStartDate.setValue(startDate);
        dpEndDate.setValue(endDate);
        cmbStatus.setValue(status);
        this.isEdit = true;
        this.promoId = promoId;
    }

    public String getCode() { return txtCode.getText(); }
    public String getDescription() { return txtDescription.getText(); }
    public double getDiscountPercent() { return Double.parseDouble(txtDiscountPercent.getText()); }
    public LocalDate getStartDate() { return dpStartDate.getValue(); }
    public LocalDate getEndDate() { return dpEndDate.getValue(); }
    public String getStatus() { return cmbStatus.getValue(); }
    public boolean isEditMode() { return isEdit; }
    public int getPromoId() { return promoId; }

    @FXML
    private void initialize() {
        cmbStatus.getItems().setAll("active", "deactive");
        if (cmbStatus.getValue() == null) cmbStatus.setValue("active");
    }

    public String validate() {
        if (getCode() == null || getCode().trim().isEmpty()) return "Mã khuyến mãi không được để trống!";
        try { Double.parseDouble(txtDiscountPercent.getText()); } catch (Exception e) { return "Phần trăm giảm phải là số!"; }
        if (getStartDate() == null || getEndDate() == null) return "Vui lòng chọn ngày bắt đầu và kết thúc!";
        if (getEndDate().isBefore(getStartDate())) return "Ngày kết thúc phải sau ngày bắt đầu!";
        return null;
    }
} 