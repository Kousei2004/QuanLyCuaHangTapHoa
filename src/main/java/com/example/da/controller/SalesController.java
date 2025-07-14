package com.example.da.controller;

import com.example.da.model.Product;
import com.example.da.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import com.example.da.service.VietQRService;
import javafx.scene.layout.VBox;
import com.example.da.model.Order;
import com.example.da.model.OrderItem;
import com.example.da.model.Customer;
import com.example.da.model.Promotion;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import com.example.da.model.WorkShift;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SalesController implements Initializable {
    
    // Header elements
    @FXML private Label lblShiftInfo;
    @FXML private Label lblEmployeeName;
    @FXML private Label lblCounterNumber;
    @FXML private TextField searchProduct;
    
    // Product grid elements
    @FXML private GridPane productGrid;
    @FXML private VBox productContainer;
    
    // Cart elements
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;
    @FXML private ComboBox<Promotion> cboDiscountCode;
    @FXML private Button btnApplyDiscount;
    @FXML private Button btnRemoveDiscount;
    @FXML private Button btnCashPayment;
    @FXML private Button btnBankPayment;
    
    // Customer elements
    @FXML private Label lblCustomerType;
    @FXML private Label lblCustomerInfo;
    @FXML private Button btnChangeCustomer;
    @FXML private Button btnAddCustomer;
    
    // Action buttons
    @FXML private Button btnPendingOrders;
    @FXML private Button btnHistory;
    @FXML private Button btnCloseShift;
    @FXML private Button btnScanBarcode;
    @FXML private Button btnAddOrder;
    
    // Category filter container (dynamic buttons will be added here)
    @FXML private HBox categoryFilterBox;
    
    // Data
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double subtotal = 0.0;
    private double discount = 0.0;
    private double total = 0.0;
    
    private Customer currentCustomer = null;
    private Promotion currentPromotion = null; // Lưu promotion hiện tại
    
    // Cart item class
    public static class CartItem {
        private Product product;
        private int quantity;
        private double price;
        
        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.price = product.getPrice();
        }
        
        // Getters and setters
        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public double getTotal() { return price * quantity; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tự động mở ca mới nếu chưa có ca làm việc đang mở
        int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUser_id() : -1;
        if (userId != -1 && com.example.da.model.WorkShift.getCurrentShiftForUser(userId) == null) {
            boolean ok = com.example.da.model.WorkShift.openNewShiftForUser(userId);
            if (ok) {
                showAlert("Thành công", "Đã mở ca làm việc mới!");
            } else {
                showAlert("Lỗi", "Không thể mở ca làm việc mới!");
            }
        }
        setupEventHandlers();
        loadProducts();
        loadCategoryFilters();
        loadPromotions();
        updateCartDisplay();
        setupKeyboardShortcuts();
        updateHeaderInfo();
        setDefaultCustomer();
        // Tự động cập nhật giờ thực mỗi giây
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateHeaderInfo())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void updateHeaderInfo() {
        // Lấy tên nhân viên
        String employeeName = "Chưa đăng nhập";
        if (Session.getCurrentUser() != null) {
            employeeName = Session.getCurrentUser().getFull_name();
        }
        lblEmployeeName.setText(employeeName);

        // Lấy ca làm việc và giờ thực
        java.time.LocalTime now = java.time.LocalTime.now();
        String shift = "";
        if (!now.isBefore(java.time.LocalTime.of(6,0)) && now.isBefore(java.time.LocalTime.of(12,0))) {
            shift = "Ca sáng";
        } else if (!now.isBefore(java.time.LocalTime.of(12,0)) && now.isBefore(java.time.LocalTime.of(18,0))) {
            shift = "Ca chiều";
        } else if (!now.isBefore(java.time.LocalTime.of(18,0)) && now.isBefore(java.time.LocalTime.of(22,0))) {
            shift = "Ca tối";
        } else {
            shift = "Ca đêm";
        }
        String timeStr = now.withSecond(0).withNano(0).toString(); // HH:mm
        lblShiftInfo.setText(shift + " - " + timeStr);
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchProduct.setOnKeyPressed(this::handleSearchKeyPress);
        
        // Category buttons are now handled dynamically in loadCategoryFilters()
        
        // Action buttons
        if (btnPendingOrders != null) btnPendingOrders.setOnAction(this::handlePendingOrders);
        if (btnHistory != null) btnHistory.setOnAction(this::handleHistory);
        if (btnCloseShift != null) btnCloseShift.setOnAction(this::handleCloseShift);
        if (btnScanBarcode != null) btnScanBarcode.setOnAction(this::handleScanBarcode);
        if (btnAddOrder != null) btnAddOrder.setOnAction(this::handleAddOrder);
        
        // Customer buttons
        if (btnChangeCustomer != null) btnChangeCustomer.setOnAction(this::handleChangeCustomer);
        if (btnAddCustomer != null) btnAddCustomer.setOnAction(this::handleAddCustomer);
        
        // Payment buttons
        if (btnCashPayment != null) btnCashPayment.setOnAction(this::handleCashPayment);
        if (btnBankPayment != null) btnBankPayment.setOnAction(this::handleBankPayment);
        if (btnRemoveDiscount != null) btnRemoveDiscount.setOnAction(this::handleRemoveDiscount);
    }
    
    private void setupKeyboardShortcuts() {
        // F2 - Scan barcode
        searchProduct.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F2) {
                handleScanBarcode(new ActionEvent());
            }
        });
        
        // F3 - Focus search
        searchProduct.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F3) {
                searchProduct.requestFocus();
            }
        });
        
        // F9 - Quick payment
        searchProduct.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F9) {
                handleQuickPayment();
            }
        });
    }
    
    private void loadProducts() {
        try {
            // Load products from database
            List<Product> productList = Product.getAll();
            
            // Filter only active products with stock > 0
            List<Product> availableProducts = productList.stream()
                .filter(p -> "active".equals(p.getStatus()) && p.getQuantityInStock() > 0)
                .collect(java.util.stream.Collectors.toList());
            
            products.clear();
            products.addAll(availableProducts);
            
            // Display products in grid
            displayProducts(products);
            
            System.out.println("Loaded " + products.size() + " products for sale");
            
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to sample products if database fails
            loadSampleProducts();
        }
    }
    
    private void loadSampleProducts() {
        products.clear();
        products.addAll(
            new Product(1, "Cà phê sữa đá", "CF001", "Cà phê sữa đá thơm ngon", 35000, 50, "active", 1),
            new Product(2, "Trà sữa trân châu", "TS001", "Trà sữa trân châu đường đen", 45000, 25, "active", 1),
            new Product(3, "Nước cam ép", "NC001", "Nước cam ép tươi", 40000, 10, "active", 1),
            new Product(4, "Bánh mì thịt nướng", "BM001", "Bánh mì thịt nướng ngon", 35000, 30, "active", 2),
            new Product(5, "Bánh tiramisu", "BT001", "Bánh tiramisu Ý", 25000, 15, "active", 3)
        );
        displayProducts(products);
    }
    
    private void displayProducts(List<Product> productList) {
        // Chỉ clear productGrid, không clear hay thay đổi HBox cha!
        productGrid.getChildren().clear();
        productGrid.getColumnConstraints().clear(); // Đảm bảo không bị cộng dồn constraints khi lọc nhiều lần

        if (productList.isEmpty()) {
            // Show "No products available" message
            Label noProductsLabel = new Label("Không có sản phẩm nào khả dụng");
            noProductsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-padding: 50;");
            productGrid.add(noProductsLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;
        int maxColumns = 5; // 5 products per row (since cards are smaller now)

        for (Product product : productList) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // Add column constraints for responsive layout
        for (int i = 0; i < maxColumns; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(100.0 / maxColumns);
            productGrid.getColumnConstraints().add(colConstraint);
        }
    }
    
    private VBox createProductCard(Product product) {
        VBox card = new VBox(8);
        card.setPrefHeight(140);
        card.setPrefWidth(230); // Tăng chiều rộng card để đủ chỗ cho tên và số lượng
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2); -fx-cursor: hand;");
        
        // Header with name and status
        HBox headerBox = new HBox();
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox nameContainer = new VBox(2);
        HBox.setHgrow(nameContainer, javafx.scene.layout.Priority.ALWAYS);
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        nameLabel.setWrapText(true); // Cho phép xuống dòng
        nameLabel.setMaxWidth(170);  // Tăng giới hạn chiều rộng để tên dài hơn
        
        Label statusLabel = new Label("Đang kinh doanh");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        nameContainer.getChildren().addAll(nameLabel, statusLabel);
        
        // Stock indicator (top right)
        String stockText = String.valueOf(product.getQuantityInStock());
        String stockColor = product.getQuantityInStock() > 10 ? "#4CAF50" : "#FF9800";
        
        Label stockLabel = new Label(stockText);
        stockLabel.setStyle("-fx-background-color: " + stockColor + "; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;");
        stockLabel.setMinWidth(32); // Đảm bảo đủ chỗ cho số lớn
        stockLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        headerBox.getChildren().addAll(nameContainer, stockLabel);
        
        // Product details
        VBox detailsContainer = new VBox(4);
        detailsContainer.setStyle("-fx-padding: 8 0;");
        
        // Barcode
        Label barcodeLabel = new Label("Mã: " + product.getBarcode());
        barcodeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        // Price
        Label priceLabel = new Label(String.format("%,.0f ₫", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold; -fx-font-size: 18px;");
        
        // Description (if available)
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            Label descLabel = new Label(product.getDescription());
            descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-weight: 500;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(170);
            detailsContainer.getChildren().add(descLabel);
        }
        
        detailsContainer.getChildren().addAll(barcodeLabel, priceLabel);
        
        // Add all components to card
        card.getChildren().addAll(headerBox, detailsContainer);
        
        // Add click event to add product to cart (click anywhere on card)
        card.setOnMouseClicked(event -> {
            addToCart(product);
            showProductAddedNotification(product.getName());
        });
        
        // Add hover effect
        card.setOnMouseEntered(event -> {
            card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 3); -fx-cursor: hand;");
        });
        
        card.setOnMouseExited(event -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2); -fx-cursor: hand;");
        });
        
        return card;
    }
    
    private void showProductAddedNotification(String productName) {
        // Create a temporary notification
        Label notification = new Label("✓ Đã thêm " + productName + " vào giỏ hàng");
        notification.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold;");
        notification.setTranslateX(400);
        notification.setTranslateY(100);
        
        // Add to scene temporarily (không ép kiểu VBox)
        if (productGrid.getScene() != null) {
            javafx.scene.Parent root = productGrid.getScene().getRoot();
            if (root instanceof Pane) {
                ((Pane) root).getChildren().add(notification);
            } else if (root instanceof BorderPane) {
                // Thêm vào top hoặc center nếu cần
                ((BorderPane) root).setTop(notification);
            }
            // Remove notification after 2 seconds
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(2), notification);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                if (productGrid.getScene() != null) {
                    if (root instanceof Pane) {
                        ((Pane) root).getChildren().remove(notification);
                    } else if (root instanceof BorderPane) {
                        ((BorderPane) root).setTop(null);
                    }
                }
            });
            fadeOut.play();
        }
    }
    
    private void loadCategoryFilters() {
        categoryFilterBox.getChildren().clear();
        
        // Nút "Tất cả"
        Button allBtn = new Button("Tất cả");
        allBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
        allBtn.setOnAction(e -> filterProductsByCategoryId(null));
        categoryFilterBox.getChildren().add(allBtn);
        
        // Lấy danh sách category từ DB
        try {
            List<com.example.da.model.Category> categories = com.example.da.model.Category.getAll();
            for (com.example.da.model.Category cat : categories) {
                Button btn = new Button(cat.getName());
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #666; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
                btn.setOnAction(e -> filterProductsByCategoryId(cat.getCategoryId()));
                categoryFilterBox.getChildren().add(btn);
            }
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }
    
    private void loadPromotions() {
        try {
            if (cboDiscountCode == null) {
                System.err.println("cboDiscountCode is null - FXML injection failed");
                return;
            }
            
            // Load danh sách promotion từ database
            List<Promotion> promotions = Promotion.getAllPromotions();
            
            // Lọc chỉ những promotion active và còn hiệu lực
            java.time.LocalDate today = java.time.LocalDate.now();
            List<Promotion> validPromotions = promotions.stream()
                .filter(p -> "active".equals(p.getStatus()))
                .filter(p -> {
                    java.time.LocalDate startDate = p.getStart_date().toLocalDate();
                    java.time.LocalDate endDate = p.getEnd_date().toLocalDate();
                    return !today.isBefore(startDate) && !today.isAfter(endDate);
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Tạo danh sách với tùy chọn "Không áp dụng"
            ObservableList<Promotion> promotionList = javafx.collections.FXCollections.observableArrayList();
            promotionList.add(null); // Tùy chọn "Không áp dụng"
            promotionList.addAll(validPromotions);
            
            // Cấu hình ComboBox với tùy chọn "Không áp dụng"
            cboDiscountCode.setItems(promotionList);
            
            // Cấu hình hiển thị
            cboDiscountCode.setCellFactory(param -> new ListCell<Promotion>() {
                @Override
                protected void updateItem(Promotion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Không áp dụng");
                    } else {
                        setText(String.format("%s - %s (%.1f%%)", 
                            item.getCode(), 
                            item.getDescription() != null ? item.getDescription() : "",
                            item.getDiscount_percent()));
                    }
                }
            });
            
            // Cấu hình hiển thị cho button
            cboDiscountCode.setButtonCell(new ListCell<Promotion>() {
                @Override
                protected void updateItem(Promotion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Chọn mã giảm giá...");
                    } else {
                        setText(String.format("%s - %s (%.1f%%)", 
                            item.getCode(), 
                            item.getDescription() != null ? item.getDescription() : "",
                            item.getDiscount_percent()));
                    }
                }
            });
            
            // Thêm event listener để tự động áp dụng khi chọn
            cboDiscountCode.setOnAction(event -> {
                Promotion selected = cboDiscountCode.getValue();
                if (selected == null) {
                    // Chọn "Không áp dụng" - xóa mã giảm giá hiện tại
                    handleRemoveDiscount(new ActionEvent());
                } else {
                    applyPromotion(selected);
                }
            });
            
            System.out.println("Loaded " + validPromotions.size() + " valid promotions");
            
        } catch (Exception e) {
            System.err.println("Error loading promotions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filterProductsByCategoryId(Integer categoryId) {
        // Reset all button styles first
        for (javafx.scene.Node node : categoryFilterBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #666; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
            }
        }
        
        // Highlight selected button and filter products
        if (categoryId == null) {
            // "All" button selected
            if (!categoryFilterBox.getChildren().isEmpty()) {
                Button allBtn = (Button) categoryFilterBox.getChildren().get(0);
                allBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
            }
            displayProducts(products); // Hiển thị tất cả
        } else {
            // Find and highlight the selected category button
            for (int i = 1; i < categoryFilterBox.getChildren().size(); i++) {
                javafx.scene.Node node = categoryFilterBox.getChildren().get(i);
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    // You might need to store categoryId with the button or use a different approach
                    // For now, we'll just highlight the first category button as an example
                    if (i == 1) { // First category button
                        btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 25; -fx-cursor: hand;");
                    }
                }
            }
            List<Product> filtered = products.filtered(p -> p.getCategoryId() == categoryId);
            displayProducts(filtered);
        }
    }
    
    // Event handlers
    @FXML
    private void handleSearchKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            searchProducts();
        }
    }
    
    private void searchProducts() {
        String searchTerm = searchProduct.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            displayProducts(products);
        } else {
            List<Product> filtered = products.filtered(p -> 
                p.getName().toLowerCase().contains(searchTerm) ||
                p.getBarcode().toLowerCase().contains(searchTerm)
            );
            displayProducts(filtered);
        }
    }
    
    @FXML
    private void handlePendingOrders(ActionEvent event) {
        // TODO: Show pending orders dialog
        System.out.println("Hiển thị đơn hàng treo");
    }
    
    @FXML
    private void handleHistory(ActionEvent event) {
        // TODO: Show sales history
        System.out.println("Hiển thị lịch sử bán hàng");
    }
    
    @FXML
    private void handleCloseShift(ActionEvent event) {
        showCloseShiftDialog();
    }
    
    private void showCloseShiftDialog() {
        try {
            int userId = Session.getCurrentUser().getUser_id();
            WorkShift currentShift = WorkShift.getCurrentShiftForUser(userId);
            if (currentShift == null) {
                showAlert("Lỗi", "Không tìm thấy ca làm việc hiện tại!");
                return;
            }
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Chốt ca làm việc");
            dialog.setHeaderText("Xác nhận chốt ca");
            VBox vbox = new VBox(10);
            vbox.setPrefWidth(400);
            String startStr = currentShift.getStartTime() != null ? currentShift.getStartTime().toLocalDateTime().format(formatter) : "";
            String endStr = java.time.LocalDateTime.now().format(formatter);
            Label lblStart = new Label("Bắt đầu: " + startStr);
            Label lblEnd = new Label("Kết thúc: " + endStr);
            double cashSales = currentShift.calculateCashSales();
            double bankSales = currentShift.calculateBankSales();
            double totalSales = cashSales + bankSales;
            Label lblCash = new Label("Tiền mặt hệ thống: " + String.format("%,.0f ₫", cashSales));
            Label lblBank = new Label("Chuyển khoản: " + String.format("%,.0f ₫", bankSales));
            Label lblTotalSales = new Label("Tổng doanh thu: " + String.format("%,.0f ₫", totalSales));
            TextField txtActualCash = new TextField();
            txtActualCash.setPromptText("Nhập tiền mặt thực tế");
            Label lblDiscrepancy = new Label("Lệch quỹ: 0 ₫");
            txtActualCash.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    double actual = newVal.isEmpty() ? 0.0 : Double.parseDouble(newVal);
                    double diff = actual - cashSales;
                    lblDiscrepancy.setText("Lệch quỹ: " + String.format("%,.0f ₫", diff));
                } catch (Exception e) {
                    lblDiscrepancy.setText("Lệch quỹ: 0 ₫");
                }
            });
            vbox.getChildren().addAll(lblStart, lblEnd, lblCash, txtActualCash, lblDiscrepancy, lblBank, lblTotalSales);
            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        double actualCash = 0.0;
                        if (!txtActualCash.getText().isEmpty()) {
                            actualCash = Double.parseDouble(txtActualCash.getText());
                        }
                        double discrepancy = actualCash - cashSales;
                        currentShift.setEndTime(new java.sql.Timestamp(System.currentTimeMillis()));
                        currentShift.setTotalSales(totalSales);
                        currentShift.setCashSales(cashSales);
                        currentShift.setBankSales(bankSales);
                        currentShift.setActualCash(actualCash);
                        currentShift.setDiscrepancy(discrepancy);
                        currentShift.setConfirmedBy(userId);
                        if (currentShift.update()) {
                            showAlert("Thành công", "Đã chốt ca thành công!");
                            logoutToLoginScreen();
                        } else {
                            showAlert("Lỗi", "Không thể cập nhật ca làm việc!");
                        }
                    } catch (Exception ex) {
                        showAlert("Lỗi", "Chốt ca thất bại: " + ex.getMessage());
                    }
                }
                return null;
            });
            dialog.showAndWait();
        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể hiển thị dialog chốt ca!");
        }
    }
    
    @FXML
    private void handleScanBarcode(ActionEvent event) {
        // TODO: Open barcode scanner
        System.out.println("Mở máy quét mã vạch");
    }
    
    @FXML
    private void handleAddOrder(ActionEvent event) {
        // TODO: Create new order
        System.out.println("Tạo đơn hàng mới");
    }
    
    private void setDefaultCustomer() {
        // Khách tạm BBB
        currentCustomer = null;
        lblCustomerType.setText("BBB");
        lblCustomerInfo.setText("Khách tạm");
    }
    
    @FXML
    private void handleChangeCustomer(ActionEvent event) {
        try {
            // Tạo dialog chọn khách hàng
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle("Chọn khách hàng");
            dialog.setHeaderText("Tìm kiếm và chọn khách hàng");
            VBox vbox = new VBox(10);
            vbox.setPrefWidth(400);
            TextField txtSearch = new TextField();
            txtSearch.setPromptText("Tìm theo tên, SĐT...");
            TableView<Customer> table = new TableView<>();
            TableColumn<Customer, String> colName = new TableColumn<>("Tên khách hàng");
            colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
            TableColumn<Customer, String> colPhone = new TableColumn<>("SĐT");
            colPhone.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));
            table.getColumns().addAll(colName, colPhone);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            // Thêm dòng BBB - Khách tạm
            Customer tempCustomer = new Customer();
            tempCustomer.setName("BBB");
            tempCustomer.setPhone("Khách tạm");
            ObservableList<Customer> allCustomers = javafx.collections.FXCollections.observableArrayList();
            allCustomers.add(tempCustomer);
            allCustomers.addAll(Customer.getAllCustomers());
            table.setItems(allCustomers);
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                String search = newVal.toLowerCase();
                ObservableList<Customer> filtered = javafx.collections.FXCollections.observableArrayList();
                filtered.add(tempCustomer); // luôn có dòng BBB đầu tiên
                filtered.addAll(Customer.getAllCustomers().stream().filter(c ->
                    c.getName().toLowerCase().contains(search) ||
                    (c.getPhone() != null && c.getPhone().toLowerCase().contains(search))
                ).toList());
                table.setItems(filtered);
            });
            vbox.getChildren().addAll(txtSearch, table);
            dialog.getDialogPane().setContent(vbox);
            ButtonType btnOK = new ButtonType("Chọn", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
            dialog.setResultConverter(bt -> bt == btnOK ? table.getSelectionModel().getSelectedItem() : null);
            dialog.showAndWait().ifPresent(selected -> {
                if (selected != null) {
                    if ("BBB".equals(selected.getName())) {
                        // Chọn lại khách tạm
                        setDefaultCustomer();
                    } else {
                        currentCustomer = selected;
                        lblCustomerType.setText(selected.getName());
                        lblCustomerInfo.setText(selected.getPhone() != null ? selected.getPhone() : "");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể chọn khách hàng!");
        }
    }
    
    @FXML
    private void handleAddCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/CustomerFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            com.example.da.dialog.CustomerFormDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm khách hàng mới");
            dialog.setResizable(false);
            dialog.setResultConverter(button -> button);
            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getText().equals("Lưu")) {
                Customer newCustomer = controller.getCustomerResult();
                if (Customer.addCustomer(newCustomer)) {
                    showAlert("Thành công", "Đã thêm khách hàng mới!");
                    // Tự động chọn khách hàng vừa thêm
                    currentCustomer = newCustomer;
                    lblCustomerType.setText(newCustomer.getName());
                    lblCustomerInfo.setText(newCustomer.getPhone() != null ? newCustomer.getPhone() : "");
                } else {
                    showAlert("Lỗi", "Không thêm được khách hàng!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể thêm khách hàng!");
        }
    }
    
    @FXML
    private void handleCashPayment(ActionEvent event) {
        processPayment("cash");
    }
    
    @FXML
    private void handleBankPayment(ActionEvent event) {
        processPayment("bank");
    }
    
    @FXML
    private void handleApplyDiscount(ActionEvent event) {
        // Kiểm tra xem có sản phẩm nào trong giỏ hàng không
        if (cartItems.isEmpty()) {
            showAlert("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi áp dụng mã giảm giá!");
            return;
        }
        
        if (cboDiscountCode == null) {
            // Fallback: sử dụng dialog chọn mã giảm giá
            showPromotionSelectionDialog();
            return;
        }
        
        // Lấy promotion được chọn từ ComboBox
        Promotion selectedPromotion = cboDiscountCode.getValue();
        if (selectedPromotion == null) {
            // Chọn "Không áp dụng" - xóa mã giảm giá hiện tại
            handleRemoveDiscount(new ActionEvent());
        } else {
            applyPromotion(selectedPromotion);
        }
    }
    
    private void showPromotionSelectionDialog() {
        // Kiểm tra xem có sản phẩm nào trong giỏ hàng không
        if (cartItems.isEmpty()) {
            showAlert("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi áp dụng mã giảm giá!");
            return;
        }
        
        try {
            // Tạo dialog chọn mã giảm giá
            Dialog<Promotion> dialog = new Dialog<>();
            dialog.setTitle("Chọn mã giảm giá");
            dialog.setHeaderText("Chọn mã giảm giá để áp dụng");
            
            VBox vbox = new VBox(10);
            vbox.setPrefWidth(500);
            
            // TextField tìm kiếm
            TextField txtSearch = new TextField();
            txtSearch.setPromptText("Tìm theo mã, mô tả...");
            
            // TableView hiển thị danh sách promotion
            TableView<Promotion> table = new TableView<>();
            
            // Các cột
            TableColumn<Promotion, String> colCode = new TableColumn<>("Mã");
            colCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCode()));
            colCode.setPrefWidth(100);
            
            TableColumn<Promotion, String> colDescription = new TableColumn<>("Mô tả");
            colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
            colDescription.setPrefWidth(200);
            
            TableColumn<Promotion, String> colDiscount = new TableColumn<>("Giảm giá");
            colDiscount.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f%%", data.getValue().getDiscount_percent())
            ));
            colDiscount.setPrefWidth(80);
            
            TableColumn<Promotion, String> colStatus = new TableColumn<>("Trạng thái");
            colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                "active".equals(data.getValue().getStatus()) ? "Hoạt động" : ("none".equals(data.getValue().getStatus()) ? "" : "Tạm dừng")
            ));
            colStatus.setPrefWidth(100);
            
            table.getColumns().addAll(colCode, colDescription, colDiscount, colStatus);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Tạo promotion đặc biệt cho "Không áp dụng"
            Promotion nonePromotion = new Promotion();
            nonePromotion.setCode("Không áp dụng");
            nonePromotion.setDescription("");
            nonePromotion.setDiscount_percent(0.0);
            nonePromotion.setStatus("none");
            
            // Load danh sách promotion
            List<Promotion> allPromotions = Promotion.getAllPromotions();
            ObservableList<Promotion> promotions = javafx.collections.FXCollections.observableArrayList();
            promotions.add(nonePromotion);
            promotions.addAll(allPromotions);
            table.setItems(promotions);
            
            // Tìm kiếm
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                String search = newVal.toLowerCase();
                ObservableList<Promotion> filtered = javafx.collections.FXCollections.observableArrayList();
                // Luôn có dòng "Không áp dụng" đầu tiên
                filtered.add(nonePromotion);
                filtered.addAll(allPromotions.stream().filter(p ->
                    p.getCode().toLowerCase().contains(search) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(search))
                ).toList());
                table.setItems(filtered);
            });
            
            vbox.getChildren().addAll(txtSearch, table);
            dialog.getDialogPane().setContent(vbox);
            
            // Buttons
            ButtonType btnOK = new ButtonType("Áp dụng", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
            
            dialog.setResultConverter(bt -> {
                Promotion selected = table.getSelectionModel().getSelectedItem();
                if (bt == btnOK && selected != null) {
                    if ("Không áp dụng".equals(selected.getCode())) {
                        handleRemoveDiscount(new ActionEvent());
                        return null; // Không áp dụng mã nào
                    } else {
                        return selected;
                    }
                }
                return null;
            });
            
            // Hiển thị dialog và xử lý kết quả
            dialog.showAndWait().ifPresent(selected -> {
                if (selected != null) {
                    applyPromotion(selected);
                }
            });
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể hiển thị danh sách mã giảm giá!");
        }
    }
    

    
    private void applyPromotion(Promotion promotion) {
        // Kiểm tra xem có sản phẩm nào trong giỏ hàng không
        if (cartItems.isEmpty()) {
            showAlert("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi áp dụng mã giảm giá!");
            return;
        }
        
        // Kiểm tra trạng thái promotion
        if (!"active".equals(promotion.getStatus())) {
            showAlert("Lỗi", "Mã giảm giá đã bị vô hiệu hóa!");
            return;
        }
        
        // Kiểm tra ngày hiệu lực
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = promotion.getStart_date().toLocalDate();
        java.time.LocalDate endDate = promotion.getEnd_date().toLocalDate();
        
        if (today.isBefore(startDate) || today.isAfter(endDate)) {
            showAlert("Lỗi", "Mã giảm giá chưa có hiệu lực hoặc đã hết hạn!");
            return;
        }
        
        // Áp dụng giảm giá
        currentPromotion = promotion;
        discount = subtotal * (promotion.getDiscount_percent() / 100.0);
        updateCartDisplay();
        
        // Hiển thị thông báo thành công
        String message = String.format("Đã áp dụng mã giảm giá: %s\nGiảm giá: %.1f%%\nSố tiền giảm: %,.0f ₫", 
            promotion.getCode(), 
            promotion.getDiscount_percent(),
            discount);
        showAlert("Thành công", message);
    }
    
    private Promotion findPromotionByCode(String code) {
        try {
            List<Promotion> promotions = Promotion.getAllPromotions();
            return promotions.stream()
                .filter(p -> code.equalsIgnoreCase(p.getCode()))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @FXML
    private void handleRemoveDiscount(ActionEvent event) {
        if (currentPromotion != null) {
            discount = 0.0;
            currentPromotion = null;
            updateCartDisplay();
            if (cboDiscountCode != null) {
                cboDiscountCode.setValue(null);
            }
            showAlert("Thông báo", "Đã xóa mã giảm giá!");
        } else {
            showAlert("Thông báo", "Không có mã giảm giá nào đang được áp dụng!");
        }
    }
    
    private void handleQuickPayment() {
        if (!cartItems.isEmpty()) {
            processPayment("cash");
        }
    }
    
    private void processPayment(String paymentMethod) {
        if (cartItems.isEmpty()) {
            showAlert("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán.");
            return;
        }
        // Lấy tên nhân viên
        String employeeName = "Chưa đăng nhập";
        Integer userId = null;
        if (com.example.da.model.Session.getCurrentUser() != null) {
            employeeName = com.example.da.model.Session.getCurrentUser().getFull_name();
            userId = com.example.da.model.Session.getCurrentUser().getUser_id();
        }
        // Hộp thoại xác nhận chi tiết
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Bạn chắc chắn muốn thanh toán đơn hàng này?");
        StringBuilder content = new StringBuilder();
        content.append("Tổng tiền: ").append(String.format("%,.0f ₫", total)).append("\n");
        content.append("Phương thức: ").append(paymentMethod.equals("cash") ? "Tiền mặt" : "Chuyển khoản").append("\n");
        content.append("Nhân viên: ").append(employeeName).append("\n");
        content.append("Thời gian: ").append(java.time.LocalDateTime.now().withSecond(0).withNano(0).toString());
        confirm.setContentText(content.toString());
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (paymentMethod.equals("bank")) {
                // Hiển thị mã QR VietQR động
                try {
                    VietQRService vietQRService = new VietQRService();
                    String maDH = "DH" + System.currentTimeMillis();
                    String note = "Thanh toan - " + maDH;
                    ImageView qrView = vietQRService.createQRImageView(total, note, 250, 250);
                    VBox vbox = new VBox(10);
                    vbox.setAlignment(javafx.geometry.Pos.CENTER);
                    Label lbl = new Label("Quét mã QR để thanh toán chuyển khoản");
                    Label lblNote = new Label(note);
                    vbox.getChildren().addAll(lbl, qrView, lblNote);
                    Alert qrDialog = new Alert(Alert.AlertType.INFORMATION);
                    qrDialog.setTitle("Mã QR chuyển khoản");
                    qrDialog.setHeaderText("Vui lòng quét mã QR để thanh toán");
                    qrDialog.getDialogPane().setContent(vbox);
                    qrDialog.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Lỗi", "Không tạo được mã QR thanh toán!");
                }
            }
            // Lưu đơn hàng vào DB
            try {
                Integer customerId = (currentCustomer != null && currentCustomer.getCustomerId() != null) ? currentCustomer.getCustomerId().intValue() : null;
                Integer promoId = (currentPromotion != null) ? currentPromotion.getPromo_id() : null; // Lấy promotion ID nếu có
                String status = "completed";
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                Order order = new Order(customerId, userId, now, total, status, promoId);
                order.setPaymentMethod(paymentMethod); // Đảm bảo luôn set đúng phương thức thanh toán
                if (order.insert()) {
                    for (CartItem item : cartItems) {
                        // Tính giảm giá chia đều theo tỷ lệ giá trị sản phẩm
                        double itemDiscount = 0.0;
                        if (currentPromotion != null && subtotal > 0) {
                            itemDiscount = (item.getTotal() / subtotal) * discount;
                        }
                        OrderItem orderItem = new OrderItem(
                            order.getOrderId(),
                            item.getProduct().getProductId(),
                            item.getQuantity(),
                            java.math.BigDecimal.valueOf(item.getPrice()),
                            java.math.BigDecimal.valueOf(itemDiscount)
                        );
                        orderItem.insert();
                        // Trừ tồn kho sản phẩm
                        Product product = item.getProduct();
                        product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
                        product.update();
                    }
                } else {
                    showAlert("Lỗi", "Không lưu được đơn hàng vào cơ sở dữ liệu!");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không lưu được đơn hàng vào cơ sở dữ liệu!");
                return;
            }
            showAlert("Thành công", "Thanh toán thành công! Đơn hàng đã được lưu.");
            cartItems.clear();
            discount = 0.0; // Reset discount
            currentPromotion = null; // Reset promotion
            updateCartDisplay();
            // Reload products để cập nhật số lượng tồn kho
            loadProducts();
        }
    }
    
    // Cart management
    public void addToCart(Product product) {
        // Check if product already in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                updateCartDisplay();
                return;
            }
        }
        
        // Add new item
        cartItems.add(new CartItem(product, 1));
        updateCartDisplay();
    }
    
    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
        updateCartDisplay();
    }
    
    public void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(item);
        } else {
            item.setQuantity(newQuantity);
            updateCartDisplay();
        }
    }
    
    private void updateCartDisplay() {
        // Tính toán tổng
        subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        total = subtotal - discount;

        // Update labels
        lblSubtotal.setText(String.format("%,.0f ₫", subtotal));
        
        // Hiển thị thông tin discount với mã giảm giá nếu có
        if (currentPromotion != null) {
            lblDiscount.setText(String.format("%,.0f ₫ (%s - %.1f%%)", 
                discount, currentPromotion.getCode(), currentPromotion.getDiscount_percent()));
        } else {
        lblDiscount.setText(String.format("%,.0f ₫", discount));
        }
        
        lblTotal.setText(String.format("%,.0f ₫", total));

        // Hiển thị cart items động
        if (cartItemsContainer != null) {
            cartItemsContainer.getChildren().clear();
            for (CartItem item : cartItems) {
                HBox row = new HBox(10);
                row.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 8;");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                ImageView img = new ImageView(new Image(getClass().getResource("/icons/Product.png").toExternalForm()));
                img.setFitHeight(50);
                img.setFitWidth(50);

                VBox info = new VBox();
                Label name = new Label(item.getProduct().getName());
                name.setStyle("-fx-font-weight: bold;");
                Label price = new Label(String.format("%,.0f ₫", item.getProduct().getPrice()));
                price.setStyle("-fx-text-fill: #2196F3;");
                info.getChildren().addAll(name, price);

                HBox qtyBox = new HBox(5);
                qtyBox.setAlignment(javafx.geometry.Pos.CENTER);
                Button btnMinus = new Button("-");
                btnMinus.setOnAction(e -> updateQuantity(item, item.getQuantity() - 1));
                Label qty = new Label(String.valueOf(item.getQuantity()));
                qty.setStyle("-fx-font-weight: bold; -fx-min-width: 30;");
                Button btnPlus = new Button("+");
                btnPlus.setOnAction(e -> updateQuantity(item, item.getQuantity() + 1));
                qtyBox.getChildren().addAll(btnMinus, qty, btnPlus);

                VBox totalBox = new VBox();
                totalBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                Label totalLbl = new Label(String.format("%,.0f ₫", item.getTotal()));
                totalLbl.setStyle("-fx-font-weight: bold;");
                Button btnRemove = new Button("Xóa");
                btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-size: 11px; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> removeFromCart(item));
                totalBox.getChildren().addAll(totalLbl, btnRemove);

                row.getChildren().addAll(img, info, qtyBox, totalBox);
                cartItemsContainer.getChildren().add(row);
            }
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void logoutToLoginScreen() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/da/view/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) productGrid.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.centerOnScreen(); // Đảm bảo cửa sổ ở giữa màn hình
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển về màn hình đăng nhập!");
        }
    }
} 