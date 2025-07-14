package com.example.da.controller;

import com.example.da.model.Order;
import com.example.da.model.OrderItem;
import com.example.da.model.Customer;
import com.example.da.model.Users;
import com.example.da.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

public class OrderManagementController implements Initializable {
    @FXML private TableView<Order> orderTableView;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> customerNameColumn;
    @FXML private TableColumn<Order, String> employeeNameColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, Double> totalAmountColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> paymentMethodColumn;
    @FXML private TableColumn<Order, Void> actionColumn;
    @FXML private Label totalOrdersLabel;
    @FXML private Pagination pagination;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    // Xóa các khai báo nút thao tác

    // Chi tiết đơn hàng
    @FXML private Label detailOrderIdLabel;
    @FXML private Label detailCustomerNameLabel;
    @FXML private Label detailEmployeeNameLabel;
    @FXML private Label detailOrderDateLabel;
    @FXML private Label detailTotalAmountLabel;
    @FXML private Label detailPaymentMethodLabel;
    @FXML private Label detailPromotionCodeLabel;
    @FXML private TableView<OrderItem> orderItemTableView;
    @FXML private TableColumn<OrderItem, String> productNameColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, Double> unitPriceColumn;
    @FXML private TableColumn<OrderItem, Double> totalPriceColumn;
    @FXML private Label orderTotalLabel;

    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();
    private FilteredList<Order> filteredOrderList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOrderTable();
        setupOrderItemTable();
        setupFilters();
        // Bỏ setupButtons();
        loadOrders();
        
        orderTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) showOrderDetail(newSel);
        });
    }

    private void setupOrderTable() {
        orderIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        
        customerNameColumn.setCellValueFactory(cellData -> {
            Integer customerId = cellData.getValue().getCustomerId();
            if (customerId != null) {
                Customer customer = Customer.getById(customerId.longValue());
                return new javafx.beans.property.SimpleStringProperty(customer != null ? customer.getName() : "Khách lẻ");
            }
            return new javafx.beans.property.SimpleStringProperty("Khách lẻ");
        });
        
        employeeNameColumn.setCellValueFactory(cellData -> {
            Integer userId = cellData.getValue().getUserId();
            if (userId != null) {
                try {
                    Users user = Users.getEmployeeById(userId);
                    return new javafx.beans.property.SimpleStringProperty(user != null ? user.getUsername() : "N/A");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        orderDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getOrderDate() != null ? cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        ));
        
        totalAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        paymentMethodColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        
        // Action column với nút Sửa/Xóa
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox hbox = new HBox(5, btnEdit, btnDelete);
            
            {
                btnEdit.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showEditOrderDialog(order);
                });
                
                btnDelete.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(order);
                });
                
                hbox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        
        // Không set items ở đây, sẽ được set trong loadOrders()
    }

    private void setupOrderItemTable() {
        productNameColumn.setCellValueFactory(cellData -> {
            Integer productId = cellData.getValue().getProductId();
            if (productId != null) {
                Product product = Product.getById(productId);
                return new javafx.beans.property.SimpleStringProperty(product != null ? product.getName() : "N/A");
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));
        unitPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUnitPrice().doubleValue()));
        totalPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
            cellData.getValue().getUnitPrice().doubleValue() * cellData.getValue().getQuantity() - (cellData.getValue().getDiscount() != null ? cellData.getValue().getDiscount().doubleValue() : 0.0)
        ));
        
        orderItemTableView.setItems(orderItemList);
    }

    private void setupFilters() {
        // Status filter
        statusComboBox.getItems().addAll("Tất cả", "Đã thanh toán", "Chưa thanh toán", "Đã hủy");
        statusComboBox.setValue("Tất cả");
        
        // Date filters
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        toDatePicker.setValue(LocalDate.now());
        
        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    // Xóa toàn bộ hàm setupButtons() và các setOnAction liên quan đến các nút thao tác

    private void loadOrders() {
        orderList.clear();
        List<Order> orders = Order.getAll();
        orderList.addAll(orders);
        
        // Khởi tạo filteredOrderList nếu chưa có
        if (filteredOrderList == null) {
            filteredOrderList = new FilteredList<>(orderList, p -> true);
            orderTableView.setItems(filteredOrderList);
        } else {
            // Cập nhật source list cho filteredOrderList
            filteredOrderList.setAll(orderList);
        }
        
        totalOrdersLabel.setText(String.valueOf(orderList.size()));
        if (!orderList.isEmpty()) {
            orderTableView.getSelectionModel().selectFirst();
        }
    }

    private void applyFilters() {
        if (filteredOrderList == null) return;
        
        filteredOrderList.setPredicate(order -> {
            // Search filter
            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                String orderId = String.valueOf(order.getOrderId());
                if (!orderId.contains(searchText)) {
                    return false;
                }
            }
            
            // Status filter
            String selectedStatus = statusComboBox.getValue();
            if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
                if (!order.getStatus().equals(selectedStatus)) {
                    return false;
                }
            }
            
            // Date filter
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            if (fromDate != null && toDate != null && order.getOrderDate() != null) {
                LocalDate orderDate = order.getOrderDate().toLocalDate();
                if (orderDate.isBefore(fromDate) || orderDate.isAfter(toDate)) {
                    return false;
                }
            }
            
            return true;
        });
        
        totalOrdersLabel.setText(String.valueOf(filteredOrderList.size()));
    }

    private void showOrderDetail(Order order) {
        detailOrderIdLabel.setText(String.valueOf(order.getOrderId()));
        
        // Customer name
        Integer customerId = order.getCustomerId();
        if (customerId != null) {
            Customer customer = Customer.getById(customerId.longValue());
            detailCustomerNameLabel.setText(customer != null ? customer.getName() : "Khách lẻ");
        } else {
            detailCustomerNameLabel.setText("Khách lẻ");
        }
        
        // Employee name
        Integer userId = order.getUserId();
        if (userId != null) {
            try {
                Users user = Users.getEmployeeById(userId);
                detailEmployeeNameLabel.setText(user != null ? user.getUsername() : "N/A");
            } catch (Exception e) {
                detailEmployeeNameLabel.setText("N/A");
            }
        } else {
            detailEmployeeNameLabel.setText("N/A");
        }
        
        detailOrderDateLabel.setText(order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "-");
        detailTotalAmountLabel.setText(order.getTotalAmount() != null ? String.format("%,.0f ₫", order.getTotalAmount()) : "-");
        detailPaymentMethodLabel.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "-");
        // Hiển thị mã giảm giá
        if (order.getPromoId() != null && order.getPromoId() > 0) {
            try {
                com.example.da.model.Promotion promo = com.example.da.model.Promotion.getPromotionById(order.getPromoId());
                if (promo != null) {
                    detailPromotionCodeLabel.setText(promo.getCode());
                } else {
                    detailPromotionCodeLabel.setText("-");
                }
            } catch (Exception e) {
                detailPromotionCodeLabel.setText("-");
            }
        } else {
            detailPromotionCodeLabel.setText("-");
        }
        
        // Load order items
        orderItemList.clear();
        List<OrderItem> items = OrderItem.getByOrderId(order.getOrderId());
        orderItemList.addAll(items);
        
        // Tính tổng tiền đơn hàng
        double total = items.stream().mapToDouble(i -> 
            i.getUnitPrice().doubleValue() * i.getQuantity() - (i.getDiscount() != null ? i.getDiscount().doubleValue() : 0.0)
        ).sum();
        orderTotalLabel.setText(String.format("%,.0f ₫", total));
    }

    private void showAddOrderDialog() {
        showAlert("Thông báo", "Chức năng thêm đơn hàng sẽ được phát triển sau!", Alert.AlertType.INFORMATION);
    }

    private void showEditOrderDialog(Order order) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sửa đơn hàng");
        dialog.setHeaderText("Cập nhật thông tin đơn hàng #" + order.getOrderId());
        
        // Dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Đã thanh toán", "Chưa thanh toán", "Đã hủy");
        statusCombo.setValue(order.getStatus());
        
        ComboBox<String> paymentCombo = new ComboBox<>();
        paymentCombo.getItems().addAll("Tiền mặt", "ATM");
        paymentCombo.setValue(order.getPaymentMethod());
        
        content.getChildren().addAll(
            new Label("Trạng thái:"), statusCombo,
            new Label("Phương thức thanh toán:"), paymentCombo
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                order.setStatus(statusCombo.getValue());
                order.setPaymentMethod(paymentCombo.getValue());
                if (order.update()) {
                    showAlert("Thành công", "Đã cập nhật đơn hàng!", Alert.AlertType.INFORMATION);
                    loadOrders();
                    return "OK";
                } else {
                    showAlert("Lỗi", "Không thể cập nhật đơn hàng!", Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    private void showDeleteConfirmation(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Xóa đơn hàng #" + order.getOrderId());
        alert.setContentText("Bạn có chắc chắn muốn xóa đơn hàng này? Hành động này không thể hoàn tác.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (order.delete()) {
                showAlert("Thành công", "Đã xóa đơn hàng!", Alert.AlertType.INFORMATION);
                loadOrders();
            } else {
                showAlert("Lỗi", "Không thể xóa đơn hàng!", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 