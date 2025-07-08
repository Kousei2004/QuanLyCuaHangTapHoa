package com.example.da.controller;

import com.example.da.model.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.Date;
import java.util.List;
import java.io.IOException;
import java.time.LocalDate;
import com.example.da.dialog.PromotionFormDialogController;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class PromotionController {
    @FXML
    private FlowPane promotionsContainer;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private ComboBox<String> cmbSort;
    @FXML
    private Button btnAddPromotion;
    @FXML
    private Button btnFilter;
    @FXML
    private Button btnLoadMore;
    @FXML
    private Button btnExport;
    @FXML
    private Button btnQuickAdd;
    @FXML
    private Label lblActiveCount;
    @FXML
    private Label lblUpcomingCount;
    @FXML
    private Label lblExpiredCount;
    @FXML
    private Label lblTotalUsage;

    private ObservableList<Promotion> promotionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadPromotions();
        btnAddPromotion.setOnAction(e -> openAddPromotionDialog());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> searchPromotions());
        cmbStatus.getItems().setAll("Tất cả", "active", "deactive");
        cmbStatus.setValue("Tất cả");
        cmbStatus.setOnAction(e -> searchPromotions());
        cmbSort.getItems().setAll(
            "Mặc định",
            "Ngày áp dụng mới nhất",
            "Ngày áp dụng cũ nhất",
            "Giá giảm cao nhất",
            "Giá giảm thấp nhất"
        );
        cmbSort.setValue("Mặc định");
        cmbSort.setOnAction(e -> searchPromotions());
    }

    private void loadPromotions() {
        List<Promotion> list = Promotion.getAllPromotions();
        promotionList.setAll(list);
        promotionsContainer.getChildren().clear();
        for (Promotion promo : list) {
            VBox card = createPromotionCard(promo);
            promotionsContainer.getChildren().add(card);
        }
        updateStatistics(list);
    }

    private VBox createPromotionCard(Promotion promo) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-padding: 20; -fx-pref-width: 360;");
        // Title
        Label lblCode = new Label(promo.getCode());
        lblCode.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        Label lblDesc = new Label(promo.getDescription());
        lblDesc.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");
        // Status
        Label lblStatus = new Label();
        if ("active".equals(promo.getStatus())) {
            lblStatus.setText("Đang hoạt động");
            lblStatus.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #4CAF50; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            lblStatus.setText("Tạm dừng");
            lblStatus.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #F44336; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        HBox header = new HBox(10, new VBox(lblCode, lblDesc), lblStatus);
        header.setSpacing(10);
        // Discount
        HBox discount = new HBox(10, new Label("💰"), new Label("Giảm:"), new Label(promo.getDiscount_percent() + "%"));
        // Time
        HBox time = new HBox(10, new Label("📅"), new Label("Thời gian:"), new Label(promo.getStart_date() + " - " + promo.getEnd_date()));
        // Actions
        Button btnEdit = new Button("Sửa");
        btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> openEditPromotionDialog(promo));
        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> deletePromotion(promo.getPromo_id()));
        Button btnToggle = new Button(promo.getStatus().equals("active") ? "Tạm dừng" : "Kích hoạt");
        btnToggle.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        btnToggle.setOnAction(e -> togglePromotionStatus(promo));
        HBox actions = new HBox(10, btnEdit, btnDelete, btnToggle);
        card.getChildren().addAll(header, new Separator(), discount, time, actions);
        return card;
    }

    private void openEditPromotionDialog(Promotion promo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/PromotionFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PromotionFormDialogController controller = loader.getController();
            controller.setPromotionData(
                promo.getCode(),
                promo.getDescription(),
                promo.getDiscount_percent(),
                promo.getStart_date().toLocalDate(),
                promo.getEnd_date().toLocalDate(),
                promo.getStatus(),
                promo.getPromo_id()
            );
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sửa khuyến mãi");
            Optional<ButtonType> result = dialog.showAndWait();
            ButtonType btnSave = dialogPane.getButtonTypes().stream()
                .filter(bt -> "Lưu".equals(bt.getText()))
                .findFirst().orElse(ButtonType.OK);
            if (result.isPresent() && result.get() == btnSave) {
                String error = controller.validate();
                if (error != null) {
                    Alert alert = new Alert(AlertType.ERROR, error, ButtonType.OK);
                    alert.showAndWait();
                    openEditPromotionDialog(promo);
                    return;
                }
                promo.setCode(controller.getCode());
                promo.setDescription(controller.getDescription());
                promo.setDiscount_percent(controller.getDiscountPercent());
                promo.setStart_date(Date.valueOf(controller.getStartDate()));
                promo.setEnd_date(Date.valueOf(controller.getEndDate()));
                promo.setStatus(controller.getStatus());
                updatePromotion(promo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR, "Không thể mở dialog sửa khuyến mãi!");
            alert.showAndWait();
        }
    }

    private void togglePromotionStatus(Promotion promo) {
        promo.setStatus(promo.getStatus().equals("active") ? "deactive" : "active");
        promo.updatePromotion();
        loadPromotions();
    }

    private void openAddPromotionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/PromotionFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PromotionFormDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm khuyến mãi");
            Optional<ButtonType> result = dialog.showAndWait();
            ButtonType btnSave = dialogPane.getButtonTypes().stream()
                .filter(bt -> "Lưu".equals(bt.getText()))
                .findFirst().orElse(ButtonType.OK);
            if (result.isPresent() && result.get() == btnSave) {
                String error = controller.validate();
                if (error != null) {
                    Alert alert = new Alert(AlertType.ERROR, error, ButtonType.OK);
                    alert.showAndWait();
                    openAddPromotionDialog();
                    return;
                }
                Promotion promo = new Promotion();
                promo.setCode(controller.getCode());
                promo.setDescription(controller.getDescription());
                promo.setDiscount_percent(controller.getDiscountPercent());
                promo.setStart_date(Date.valueOf(controller.getStartDate()));
                promo.setEnd_date(Date.valueOf(controller.getEndDate()));
                promo.setStatus(controller.getStatus());
                addPromotion(promo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR, "Không thể mở dialog thêm khuyến mãi!");
            alert.showAndWait();
        }
    }

    public void addPromotion(Promotion promotion) {
        if (promotion.insertPromotion()) {
            loadPromotions();
        }
    }

    public void updatePromotion(Promotion promotion) {
        if (promotion.updatePromotion()) {
            loadPromotions();
        }
    }

    public void deletePromotion(int promoId) {
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa khuyến mãi này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (Promotion.deletePromotion(promoId)) {
                loadPromotions();
            }
        }
    }

    private void searchPromotions() {
        String keyword = txtSearch.getText().toLowerCase();
        String status = cmbStatus.getValue();
        String sort = cmbSort.getValue();
        promotionsContainer.getChildren().clear();
        List<Promotion> filtered = promotionList.filtered(p ->
            (keyword.isEmpty() || p.getCode().toLowerCase().contains(keyword) || (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword))) &&
            ("Tất cả".equals(status) || p.getStatus().equals(status))
        );
        // Sắp xếp
        if (sort != null && !"Mặc định".equals(sort)) {
            switch (sort) {
                case "Ngày áp dụng mới nhất":
                    filtered.sort((a, b) -> b.getStart_date().compareTo(a.getStart_date()));
                    break;
                case "Ngày áp dụng cũ nhất":
                    filtered.sort((a, b) -> a.getStart_date().compareTo(b.getStart_date()));
                    break;
                case "Giá giảm cao nhất":
                    filtered.sort((a, b) -> Double.compare(b.getDiscount_percent(), a.getDiscount_percent()));
                    break;
                case "Giá giảm thấp nhất":
                    filtered.sort((a, b) -> Double.compare(a.getDiscount_percent(), b.getDiscount_percent()));
                    break;
            }
        }
        for (Promotion promo : filtered) {
            VBox card = createPromotionCard(promo);
            promotionsContainer.getChildren().add(card);
        }
        updateStatistics(filtered);
    }

    private void updateStatistics(List<Promotion> list) {
        int active = 0, deactive = 0;
        for (Promotion p : list) {
            if ("active".equals(p.getStatus())) active++;
            else deactive++;
        }
        lblActiveCount.setText(String.valueOf(active));
        lblExpiredCount.setText(String.valueOf(deactive));
        // lblUpcomingCount, lblTotalUsage: cập nhật nếu có logic thêm
    }

    // TODO: Implement event handlers for UI actions (add, edit, delete, filter, search, etc.)
} 