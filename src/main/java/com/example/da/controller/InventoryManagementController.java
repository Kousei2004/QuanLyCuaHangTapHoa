package com.example.da.controller;

import com.example.da.model.PurchaseOrder;
import com.example.da.model.PurchaseOrderItem;
import com.example.da.model.Supplier;
import com.example.da.model.Users;
import com.example.da.model.Product;
import com.example.da.model.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.DialogPane;
import com.example.da.dialog.PurchaseOrderFormDialogController;
import javafx.scene.control.ButtonType;
import com.example.da.dialog.PurchaseOrderItemsDialogController;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.XYChart;

public class InventoryManagementController implements Initializable {
    // Import table columns
    @FXML private TableView<PurchaseOrder> importTable;
    @FXML private TableColumn<PurchaseOrder, String> colImportCode;
    @FXML private TableColumn<PurchaseOrder, String> colImportDate;
    @FXML private TableColumn<PurchaseOrder, String> colSupplier;
    @FXML private TableColumn<PurchaseOrder, Integer> colTotalItems;
    @FXML private TableColumn<PurchaseOrder, Integer> colTotalQuantity;
    @FXML private TableColumn<PurchaseOrder, Double> colTotalValue;
    @FXML private TableColumn<PurchaseOrder, String> colImportBy;
    @FXML private TableColumn<PurchaseOrder, String> colImportStatus;
    @FXML private TableColumn<PurchaseOrder, String> colImportNote;
    
    // Inventory table columns
    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, String> colProductCode;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, Integer> colCurrentStock;
    @FXML private TableColumn<Product, Double> colStockValue;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, String> colLastUpdate;
    
    // Controls
    @FXML private Button btnImportGoods;
    @FXML private TextField txtSearchInventory;
    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbStockStatus;
    
    // Labels for statistics
    @FXML private Label lblTotalValue;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblWarningCount;
    @FXML private Label lblTodayImports;
    @FXML private Label lblTodayExports;
    
    // Warning labels
    @FXML private Label lblOutOfStock;
    @FXML private Label lblLowStock;
    
    // Warning products list
    @FXML private VBox warningProductsList;
    
    // Chart controls
    @FXML private LineChart<String, Number> inventoryChart;
    @FXML private VBox topStockProducts;
    @FXML private VBox topMovingProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Thiết lập các cột cho bảng import
        setupImportTableColumns();
        
        // Thiết lập các cột cho bảng tồn kho
        setupInventoryTableColumns();
        
        // Thiết lập các controls
        setupControls();
        
        btnImportGoods.setOnAction(e -> openAddPurchaseOrderDialog());

        loadImportOrders();
        loadInventoryData();
        loadStatistics();
        loadWarningData();
        loadChartData();
    }

    private void setupImportTableColumns() {
        // Mã phiếu nhập
        colImportCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty("PN" + String.format("%06d", cellData.getValue().getPoId())));
        
        // Ngày nhập
        colImportDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } else {
                return new SimpleStringProperty("");
            }
        });
        
        // Nhà cung cấp
        colSupplier.setCellValueFactory(cellData -> {
            Integer supplierId = cellData.getValue().getSupplierId();
            if (supplierId != null) {
                Supplier supplier = Supplier.getById(supplierId.longValue());
                return new SimpleStringProperty(supplier != null ? supplier.getName() : "N/A");
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Tổng số mặt hàng
        colTotalItems.setCellValueFactory(cellData -> {
            List<PurchaseOrderItem> items = PurchaseOrderItem.getByPurchaseOrderId(cellData.getValue().getPoId());
            return new SimpleIntegerProperty(items.size()).asObject();
        });
        
        // Tổng số lượng
        colTotalQuantity.setCellValueFactory(cellData -> {
            List<PurchaseOrderItem> items = PurchaseOrderItem.getByPurchaseOrderId(cellData.getValue().getPoId());
            int totalQty = items.stream().mapToInt(PurchaseOrderItem::getQuantity).sum();
            return new SimpleIntegerProperty(totalQty).asObject();
        });
        
        // Tổng giá trị
        colTotalValue.setCellValueFactory(cellData -> {
            List<PurchaseOrderItem> items = PurchaseOrderItem.getByPurchaseOrderId(cellData.getValue().getPoId());
            double totalValue = items.stream().mapToDouble(item -> 
                item.getQuantity() * item.getUnitPrice().doubleValue()).sum();
            return new SimpleDoubleProperty(totalValue).asObject();
        });
        
        // Người nhập
        colImportBy.setCellValueFactory(cellData -> {
            Integer userId = cellData.getValue().getUserId();
            if (userId != null) {
                try {
                    Users user = Users.getEmployeeById(userId);
                    return new SimpleStringProperty(user != null ? user.getUsername() : "N/A");
                } catch (Exception e) {
                    return new SimpleStringProperty("N/A");
                }
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Trạng thái
        colImportStatus.setCellValueFactory(cellData -> {
            List<PurchaseOrderItem> items = PurchaseOrderItem.getByPurchaseOrderId(cellData.getValue().getPoId());
            if (items.isEmpty()) {
                return new SimpleStringProperty("Chưa nhập");
            } else {
                return new SimpleStringProperty("Đã nhập");
            }
        });
        
        // Ghi chú
        colImportNote.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNote() != null ? cellData.getValue().getNote() : ""));
    }

    private void setupInventoryTableColumns() {
        // Mã sản phẩm
        colProductCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty("SP" + String.format("%06d", cellData.getValue().getProductId())));
        
        // Tên sản phẩm
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Danh mục
        colCategory.setCellValueFactory(cellData -> {
            int categoryId = cellData.getValue().getCategoryId();
            Category category = Category.getById(categoryId);
            return new SimpleStringProperty(category != null ? category.getName() : "N/A");
        });
        
        // Đơn vị (mặc định là "cái")
        colUnit.setCellValueFactory(cellData -> new SimpleStringProperty("cái"));
        
        // Tồn kho hiện tại
        colCurrentStock.setCellValueFactory(new PropertyValueFactory<>("quantityInStock"));
        
        // Giá trị tồn
        colStockValue.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getQuantityInStock() * cellData.getValue().getPrice();
            return new SimpleDoubleProperty(value).asObject();
        });
        
        // Trạng thái
        colStatus.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getQuantityInStock();
            if (stock == 0) {
                return new SimpleStringProperty("Hết hàng");
            } else if (stock <= 10) {
                return new SimpleStringProperty("Sắp hết");
            } else {
                return new SimpleStringProperty("Còn hàng");
            }
        });
        
        // Cập nhật lần cuối (mặc định)
        colLastUpdate.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Hôm nay"));
    }

    private void setupControls() {
        // Load danh mục vào ComboBox
        List<Category> categories = Category.getAll();
        ObservableList<String> categoryNames = FXCollections.observableArrayList("Tất cả danh mục");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        cbCategory.setItems(categoryNames);
        cbCategory.setValue("Tất cả danh mục");
        
        // Load trạng thái kho vào ComboBox
        ObservableList<String> stockStatuses = FXCollections.observableArrayList(
            "Tất cả trạng thái", "Còn hàng", "Sắp hết", "Hết hàng"
        );
        cbStockStatus.setItems(stockStatuses);
        cbStockStatus.setValue("Tất cả trạng thái");
        
        // Thiết lập sự kiện tìm kiếm
        txtSearchInventory.textProperty().addListener((observable, oldValue, newValue) -> {
            filterInventoryTable();
        });
        
        // Thiết lập sự kiện lọc
        cbCategory.setOnAction(e -> filterInventoryTable());
        cbStockStatus.setOnAction(e -> filterInventoryTable());
    }

    private void loadInventoryData() {
        List<Product> products = Product.getAll();
        ObservableList<Product> data = FXCollections.observableArrayList(products);
        inventoryTable.setItems(data);
    }

    private void filterInventoryTable() {
        String searchText = txtSearchInventory.getText().toLowerCase();
        String selectedCategory = cbCategory.getValue();
        String selectedStatus = cbStockStatus.getValue();
        
        List<Product> allProducts = Product.getAll();
        ObservableList<Product> filteredData = FXCollections.observableArrayList();
        
        for (Product product : allProducts) {
            boolean matchesSearch = product.getName().toLowerCase().contains(searchText) ||
                                   ("SP" + String.format("%06d", product.getProductId())).toLowerCase().contains(searchText);
            
            boolean matchesCategory = selectedCategory.equals("Tất cả danh mục");
            if (!matchesCategory) {
                Category category = Category.getById(product.getCategoryId());
                matchesCategory = category != null && category.getName().equals(selectedCategory);
            }
            
            boolean matchesStatus = selectedStatus.equals("Tất cả trạng thái");
            if (!matchesStatus) {
                int stock = product.getQuantityInStock();
                String productStatus;
                if (stock == 0) {
                    productStatus = "Hết hàng";
                } else if (stock <= 10) {
                    productStatus = "Sắp hết";
                } else {
                    productStatus = "Còn hàng";
                }
                matchesStatus = productStatus.equals(selectedStatus);
            }
            
            if (matchesSearch && matchesCategory && matchesStatus) {
                filteredData.add(product);
            }
        }
        
        inventoryTable.setItems(filteredData);
    }

    private void loadStatistics() {
        List<Product> products = Product.getAll();
        
        // Tính tổng giá trị kho
        double totalValue = products.stream()
            .mapToDouble(p -> p.getQuantityInStock() * p.getPrice())
            .sum();
        lblTotalValue.setText(String.format("%,.0f ₫", totalValue));
        
        // Tổng số sản phẩm
        lblTotalProducts.setText(String.valueOf(products.size()));
        
        // Số sản phẩm cảnh báo
        long warningCount = products.stream()
            .filter(p -> p.getQuantityInStock() <= 10)
            .count();
        lblWarningCount.setText(String.valueOf(warningCount));
        
        // Số phiếu nhập hôm nay (mặc định)
        lblTodayImports.setText("8");
        
        // Số phiếu xuất hôm nay (mặc định)
        lblTodayExports.setText("0");
    }

    private void loadImportOrders() {
        List<PurchaseOrder> orders = PurchaseOrder.getAll();
        ObservableList<PurchaseOrder> data = FXCollections.observableArrayList(orders);
        importTable.setItems(data);
    }

    private void openAddPurchaseOrderDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/PurchaseOrderFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PurchaseOrderFormDialogController controller = loader.getController();
            
            // Setup save button handler
            controller.setupSaveButtonHandler();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm phiếu nhập kho");
            
            // Chặn đóng dialog nếu dữ liệu không hợp lệ
            Button btnSave = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE)
                    .findFirst().orElse(ButtonType.OK)
            );
            if (btnSave != null) {
                btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    var po = controller.getResult();
                    if (po == null) {
                        event.consume();
                    }
                });
            }
            
            dialog.showAndWait().ifPresent(result -> {
                // Kiểm tra buttonData thay vì so sánh trực tiếp
                if (result.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE) {
                    var po = controller.getResult();
                    if (po != null) {
                        // Kiểm tra xem đã insert chưa (vì có thể đã insert trong handleSave)
                        if (po.getPoId() > 0) {
                            loadImportOrders();
                            openAddPurchaseOrderItemsDialog(po.getPoId());
                        } else {
                            if (po.insert()) {
                                loadImportOrders();
                                openAddPurchaseOrderItemsDialog(po.getPoId());
                            } else {
                                showAlert("Không thể lưu phiếu nhập! Vui lòng kiểm tra lại dữ liệu hoặc kết nối cơ sở dữ liệu.");
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openAddPurchaseOrderItemsDialog(int poId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/PurchaseOrderItemsDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PurchaseOrderItemsDialogController controller = loader.getController();
            controller.setPurchaseOrderId(poId);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nhập chi tiết phiếu nhập");
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    controller.saveItemsToDB();
                    loadInventoryData(); // Cập nhật lại tồn kho sau khi nhập hàng
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void loadWarningData() {
        List<Product> products = Product.getAll();
        
        // Đếm số sản phẩm hết hàng (tồn = 0)
        long outOfStockCount = products.stream()
            .filter(p -> p.getQuantityInStock() == 0)
            .count();
        lblOutOfStock.setText(outOfStockCount + " sản phẩm");
        
        // Đếm số sản phẩm sắp hết hàng (tồn <= 10)
        long lowStockCount = products.stream()
            .filter(p -> p.getQuantityInStock() > 0 && p.getQuantityInStock() <= 10)
            .count();
        lblLowStock.setText(lowStockCount + " sản phẩm");
        
        // Load danh sách sản phẩm cần xử lý
        loadWarningProductsList();
    }

    private void loadWarningProductsList() {
        warningProductsList.getChildren().clear();
        List<Product> products = Product.getAll();
        
        // Lọc sản phẩm cần cảnh báo (hết hàng hoặc sắp hết)
        List<Product> warningProducts = products.stream()
            .filter(p -> p.getQuantityInStock() <= 10)
            .sorted((p1, p2) -> Integer.compare(p1.getQuantityInStock(), p2.getQuantityInStock()))
            .toList();
        
        for (Product product : warningProducts) {
            VBox productCard = createWarningProductCard(product);
            warningProductsList.getChildren().add(productCard);
        }
    }

    private VBox createWarningProductCard(Product product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " + 
                     (product.getQuantityInStock() == 0 ? "#F44336" : "#FF9800") + "; -fx-border-width: 0 0 0 4;");
        
        // Tên sản phẩm
        Label nameLabel = new Label("SP" + String.format("%06d", product.getProductId()) + " - " + product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        // Danh mục
        Category category = Category.getById(product.getCategoryId());
        Label categoryLabel = new Label("Danh mục: " + (category != null ? category.getName() : "N/A"));
        categoryLabel.setStyle("-fx-text-fill: #607D8B;");
        
        // Thông tin tồn kho
        HBox stockInfo = new HBox(20);
        Label stockLabel = new Label("Tồn kho: " + product.getQuantityInStock());
        stockLabel.setStyle("-fx-text-fill: " + (product.getQuantityInStock() == 0 ? "#F44336" : "#FF9800") + "; -fx-font-weight: bold;");
        
        Label minStockLabel = new Label("Tồn tối thiểu: 10");
        Label soldTodayLabel = new Label("Đã bán hôm nay: " + (int)(Math.random() * 50)); // Giả lập dữ liệu
        
        stockInfo.getChildren().addAll(stockLabel, minStockLabel, soldTodayLabel);
        
        // Layout chính
        HBox mainLayout = new HBox(15);
        VBox leftInfo = new VBox(5);
        leftInfo.getChildren().addAll(nameLabel, categoryLabel, stockInfo);
        HBox.setHgrow(leftInfo, javafx.scene.layout.Priority.ALWAYS);
        
        VBox rightActions = new VBox(5);
        rightActions.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Trạng thái
        Label statusLabel = new Label(product.getQuantityInStock() == 0 ? "HẾT HÀNG" : "SẮP HẾT");
        statusLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + 
                           (product.getQuantityInStock() == 0 ? "#F44336" : "#FF9800") + 
                           "; -fx-padding: 5 15; -fx-background-radius: 15; -fx-font-weight: bold;");
        
        // Nút nhập hàng
        Button importButton = new Button("Nhập hàng ngay");
        importButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        importButton.setOnAction(e -> openAddPurchaseOrderDialog());
        
        rightActions.getChildren().addAll(statusLabel, importButton);
        
        mainLayout.getChildren().addAll(leftInfo, rightActions);
        card.getChildren().add(mainLayout);
        
        return card;
    }

    private void loadChartData() {
        loadInventoryMovementChart();
        loadTopStockProducts();
        loadTopMovingProducts();
    }

    private void loadInventoryMovementChart() {
        // Tạo dữ liệu giả lập cho biểu đồ biến động tồn kho 30 ngày
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tồn kho");
        
        List<Product> products = Product.getAll();
        int totalStock = products.stream().mapToInt(Product::getQuantityInStock).sum();
        
        // Tạo dữ liệu 30 ngày gần đây
        for (int i = 29; i >= 0; i--) {
            String date = java.time.LocalDate.now().minusDays(i).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            // Giả lập biến động tồn kho
            int variation = (int)(Math.random() * 100 - 50); // -50 đến +50
            int stockValue = Math.max(0, totalStock + variation);
            series.getData().add(new XYChart.Data<>(date, stockValue));
        }
        
        inventoryChart.getData().clear();
        inventoryChart.getData().add(series);
    }

    private void loadTopStockProducts() {
        topStockProducts.getChildren().clear();
        List<Product> products = Product.getAll();
        
        // Sắp xếp theo tồn kho giảm dần và lấy top 10
        List<Product> topProducts = products.stream()
            .sorted((p1, p2) -> Integer.compare(p2.getQuantityInStock(), p1.getQuantityInStock()))
            .limit(10)
            .toList();
        
        int maxStock = topProducts.isEmpty() ? 1 : topProducts.get(0).getQuantityInStock();
        
        for (int i = 0; i < topProducts.size(); i++) {
            Product product = topProducts.get(i);
            HBox productBar = createProductBar(
                (i + 1) + ". " + product.getName(),
                (double) product.getQuantityInStock() / maxStock,
                String.valueOf(product.getQuantityInStock())
            );
            topStockProducts.getChildren().add(productBar);
        }
    }

    private void loadTopMovingProducts() {
        topMovingProducts.getChildren().clear();
        List<Product> products = Product.getAll();
        
        // Giả lập dữ liệu luân chuyển (số lượng bán/ngày)
        List<Product> movingProducts = products.stream()
            .map(p -> {
                // Giả lập số lượng bán mỗi ngày
                int salesCount = (int)(Math.random() * 300 + 50); // 50-350
                return new Object() {
                    Product product = p;
                    int dailySales = salesCount;
                };
            })
            .sorted((o1, o2) -> Integer.compare(o2.dailySales, o1.dailySales))
            .limit(10)
            .map(o -> o.product)
            .toList();
        
        int maxSales = movingProducts.isEmpty() ? 1 : 300; // Giả lập max sales
        
        for (int i = 0; i < movingProducts.size(); i++) {
            Product product = movingProducts.get(i);
            int dailySales = (int)(Math.random() * 300 + 50);
            HBox productBar = createProductBar(
                (i + 1) + ". " + product.getName(),
                (double) dailySales / maxSales,
                dailySales + "/ngày"
            );
            topMovingProducts.getChildren().add(productBar);
        }
    }

    private HBox createProductBar(String productName, double progress, String value) {
        HBox bar = new HBox(10);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(productName);
        nameLabel.setMinWidth(150);
        
        ProgressBar progressBar = new ProgressBar(progress);
        HBox.setHgrow(progressBar, javafx.scene.layout.Priority.ALWAYS);
        if (value.contains("/ngày")) {
            progressBar.setStyle("-fx-accent: #4CAF50;");
        }
        
        Label valueLabel = new Label(value);
        valueLabel.setMinWidth(60);
        valueLabel.setStyle("-fx-font-weight: bold;" + (value.contains("/ngày") ? " -fx-text-fill: #4CAF50;" : ""));
        
        bar.getChildren().addAll(nameLabel, progressBar, valueLabel);
        return bar;
    }
} 