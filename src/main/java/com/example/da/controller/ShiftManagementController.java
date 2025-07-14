package com.example.da.controller;

import com.example.da.model.WorkShift;
import com.example.da.model.Users;
import com.example.da.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class ShiftManagementController implements Initializable {
    // Note: The original shiftTable and columns are not in the FXML
    // They were from a different version. The current FXML has different tables.

    // New FXML elements from ShiftManagement.fxml
    @FXML private Label lblCurrentTime;
    @FXML private Label lblCurrentDate;
    @FXML private Label lblCurrentShift;
    @FXML private Label lblEmployeesInShift;
    @FXML private Label lblShiftRevenue;
    @FXML private Label lblShiftOrders;
    @FXML private Label lblCashInDrawer;
    @FXML private Button btnStartShift;
    @FXML private Button btnCloseShift;
    @FXML private Button btnHandover;
    @FXML private TabPane shiftTabs;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblCashRevenue;
    @FXML private Label lblBankRevenue;
    @FXML private PieChart categoryRevenueChart;
    @FXML private VBox topProductsList;
    @FXML private TableView<?> transactionTable;
    @FXML private TableColumn<?, ?> colTime;
    @FXML private TableColumn<?, ?> colOrderCode;
    @FXML private TableColumn<?, ?> colCustomer;
    @FXML private TableColumn<?, ?> colProducts;
    @FXML private TableColumn<?, ?> colAmount;
    @FXML private TableColumn<?, ?> colPaymentMethod;
    @FXML private TableColumn<?, ?> colCashier;
    
    // Cash count elements
    @FXML private TextField txt500k, txt200k, txt100k, txt50k, txt20k, txt10k, txt5k, txt2k, txt1k, txt500, txt200;
    @FXML private Label lbl500kTotal, lbl200kTotal, lbl100kTotal, lbl50kTotal, lbl20kTotal, lbl10kTotal, lbl5kTotal, lbl2kTotal, lbl1kTotal, lbl500Total, lbl200Total;
    @FXML private Label lblActualCashTotal;
    @FXML private Button btnCalculateTotal;
    @FXML private Button btnClearCash;
    @FXML private Label lblSystemCash;
    @FXML private Label lblActualCash;
    @FXML private Label lblCashDifference;
    @FXML private TextArea txtCashNote;
    @FXML private Button btnSaveCashCount;
    @FXML private Button btnPrintCashReport;
    @FXML private TableView<?> cashHistoryTable;
    @FXML private TableColumn<?, ?> colCashCheckTime;
    @FXML private TableColumn<?, ?> colCashChecker;
    @FXML private TableColumn<?, ?> colSystemAmount;
    @FXML private TableColumn<?, ?> colActualAmount;
    @FXML private TableColumn<?, ?> colDifference;
    @FXML private TableColumn<?, ?> colCashNote;
    @FXML private TableColumn<?, ?> colCashStatus;
    @FXML private TableColumn<WorkShift, String> colCashDifference;
    
    // Shift history elements
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private ComboBox<String> cbShiftFilter;
    @FXML private ComboBox<String> cbManagerFilter;
    @FXML private Button btnFilterShifts;
    @FXML private Button btnExportShifts;
    @FXML private TableView<WorkShift> shiftHistoryTable;
    @FXML private TableColumn<WorkShift, String> colShiftDate;
    @FXML private TableColumn<WorkShift, String> colShiftName;
    @FXML private TableColumn<WorkShift, String> colShiftStart;
    @FXML private TableColumn<WorkShift, String> colShiftEnd;
    @FXML private TableColumn<WorkShift, String> colShiftManager;
    @FXML private TableColumn<WorkShift, Integer> colEmployeeCount;
    @FXML private TableColumn<WorkShift, String> colShiftRevenueTbl;
    @FXML private TableColumn<WorkShift, Integer> colOrderCount;
    @FXML private TableColumn<WorkShift, String> colCashAmount;
    @FXML private TableColumn<WorkShift, String> colShiftStatus;
    @FXML private TableColumn<WorkShift, Void> colShiftActions;
    @FXML private Label lblTotalShifts;
    @FXML private Label lblTotalShiftRevenue;
    @FXML private Label lblAverageRevenue;
    @FXML private Label lblTotalOrders;
    
    // Report elements
    @FXML private Label lblReportDate;
    @FXML private Button btnGenerateReport;
    @FXML private Button btnPrintReport;
    @FXML private VBox reportContent;
    @FXML private Label lblReportShiftName;
    @FXML private Label lblReportShiftTime;
    @FXML private Label lblReportManager;
    @FXML private Label lblReportEmployeeCount;
    @FXML private Label lblReportTotalRevenue;
    @FXML private Label lblReportCashRevenue;
    @FXML private Label lblReportBankRevenue;
    @FXML private Label lblReportOrderCount;
    @FXML private Label lblReportAvgOrder;
    @FXML private TableView<?> reportAttendanceTable;
    @FXML private TableColumn<?, ?> colReportEmpId;
    @FXML private TableColumn<?, ?> colReportEmpName;
    @FXML private TableColumn<?, ?> colReportCheckIn;
    @FXML private TableColumn<?, ?> colReportCheckOut;
    @FXML private TableColumn<?, ?> colReportWorkHours;
    @FXML private TableColumn<?, ?> colReportNote;
    @FXML private Label lblReportSystemCash;
    @FXML private Label lblReportActualCash;
    @FXML private Label lblReportCashDiff;
    
    // Status elements
    @FXML private Label lblStatusMessage;
    @FXML private Label lblLastUpdate;

    private ObservableList<WorkShift> closedShifts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        initializeUI();
        loadClosedShifts();
        
        // Load initial data if needed
        // Note: The original shift table is not in this FXML
        // This FXML has different tables for different purposes
    }

    private void loadShifts() {
        // TODO: Load shifts data when needed
        // This method is kept for future use if we add a shift table
        List<WorkShift> shifts = WorkShift.getAll();
        // Use the data as needed for the current UI
    }

    private void initializeUI() {
        // Initialize UI components for shift history table
        if (colShiftDate != null) {
            colShiftDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartTime() != null ?
                    cellData.getValue().getStartTime().toLocalDateTime().toLocalDate().toString() : ""
            ));
        }
        if (colShiftName != null) {
            colShiftName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getShiftName(cellData.getValue().getStartTime())
            ));
        }
        if (colShiftStart != null) {
            colShiftStart.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartTime() != null ?
                    cellData.getValue().getStartTime().toLocalDateTime().toLocalTime().toString() : ""
            ));
        }
        if (colShiftEnd != null) {
            colShiftEnd.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEndTime() != null ?
                    cellData.getValue().getEndTime().toLocalDateTime().toLocalTime().toString() : ""
            ));
        }
        if (colShiftManager != null) {
            colShiftManager.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                getManagerName(cellData.getValue().getUserId())
            ));
        }
        if (colEmployeeCount != null) {
            colEmployeeCount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(
                getEmployeeCountForShift(cellData.getValue())
            ).asObject());
        }
        if (colShiftRevenueTbl != null) {
            colShiftRevenueTbl.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                formatCurrency(cellData.getValue().getTotalSales())
            ));
        }
        if (colOrderCount != null) {
            colOrderCount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(
                getOrderCountForShift(cellData.getValue())
            ).asObject());
        }
        if (colCashAmount != null) {
            colCashAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                formatCurrency(cellData.getValue().getCashSales())
            ));
        }
        if (colCashDifference != null) {
            colCashDifference.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    formatCurrency(cellData.getValue().getDiscrepancy())
                )
            );
        }
        if (colShiftStatus != null) {
            colShiftStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEndTime() != null ? "Đã đóng" : "Đang mở"
            ));
        }
        if (colShiftActions != null) {
            colShiftActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnEdit = new Button("Sửa");
                private final Button btnDelete = new Button("Xóa");
                {
                    btnEdit.setOnAction(e -> {
                        WorkShift ws = getTableView().getItems().get(getIndex());
                        showEditShiftDialog(ws);
                    });
                    btnDelete.setOnAction(e -> {
                        WorkShift ws = getTableView().getItems().get(getIndex());
                        confirmAndDeleteShift(ws);
                    });
                    btnEdit.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
                    btnDelete.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12px;");
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Chỉ hiện với tài khoản quản lý
                        com.example.da.model.Users currentUser = com.example.da.model.Session.getCurrentUser();
                        if (currentUser != null && currentUser.getRole() == com.example.da.model.Users.Role.quanly) {
                            setGraphic(new javafx.scene.layout.HBox(5, btnEdit, btnDelete));
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    private void loadClosedShifts() {
        closedShifts.clear();
        List<WorkShift> all = WorkShift.getAll();
        for (WorkShift ws : all) {
            if (ws.getEndTime() != null) {
                closedShifts.add(ws);
            }
        }
        if (shiftHistoryTable != null) {
            shiftHistoryTable.setItems(closedShifts);
        }
    }

    // Existing methods
    @FXML
    private void handleStartShift(ActionEvent event) {
        // TODO: Xử lý mở ca mới
        System.out.println("Mở ca mới");
    }

    @FXML
    private void handleCloseShift(ActionEvent event) {
        // TODO: Xử lý chốt ca
        System.out.println("Chốt ca");
    }

    @FXML
    private void handleHandover(ActionEvent event) {
        // TODO: Xử lý bàn giao ca
        System.out.println("Bàn giao ca");
    }



    // New methods for FXML event handlers
    @FXML
    private void handleCheckOut(ActionEvent event) {
        // TODO: Xử lý check-out nhân viên
        System.out.println("Check-out nhân viên");
    }

    @FXML
    private void calculateCashTotal(KeyEvent event) {
        // TODO: Tính tổng tiền mặt khi nhập số lượng
        System.out.println("Tính tổng tiền mặt");
    }

    @FXML
    private void calculateCashTotal(ActionEvent event) {
        // TODO: Tính tổng tiền mặt khi nhấn nút
        System.out.println("Tính tổng tiền mặt (button)");
    }

    @FXML
    private void clearCashCount(ActionEvent event) {
        // TODO: Xóa tất cả số liệu kiểm kê tiền mặt
        System.out.println("Xóa kiểm kê tiền mặt");
    }

    @FXML
    private void saveCashCount(ActionEvent event) {
        // TODO: Lưu kết quả kiểm kê tiền mặt
        System.out.println("Lưu kiểm kê tiền mặt");
    }

    @FXML
    private void printCashReport(ActionEvent event) {
        // TODO: In báo cáo kiểm kê tiền mặt
        System.out.println("In báo cáo kiểm kê tiền mặt");
    }

    @FXML
    private void filterShiftHistory(ActionEvent event) {
        // TODO: Lọc lịch sử ca làm việc
        System.out.println("Lọc lịch sử ca làm việc");
    }

    @FXML
    private void exportShiftHistory(ActionEvent event) {
        // TODO: Xuất Excel lịch sử ca làm việc
        System.out.println("Xuất Excel lịch sử ca làm việc");
    }

    @FXML
    private void generateShiftReport(ActionEvent event) {
        // TODO: Tạo báo cáo ca làm việc
        System.out.println("Tạo báo cáo ca làm việc");
    }

    @FXML
    private void printShiftReport(ActionEvent event) {
        // TODO: In báo cáo ca làm việc
        System.out.println("In báo cáo ca làm việc");
    }

    // Dialog sửa ca (chỉ sửa chênh lệch)
    private void showEditShiftDialog(WorkShift ws) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(String.valueOf(ws.getDiscrepancy()));
        dialog.setTitle("Sửa ca làm việc");
        dialog.setHeaderText("Sửa chênh lệch tiền mặt cho ca #" + ws.getShiftId());
        dialog.setContentText("Chênh lệch mới:");
        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(newDiff -> {
            try {
                double diff = Double.parseDouble(newDiff);
                ws.setDiscrepancy(diff);
                ws.update(); // cập nhật DB
                loadClosedShifts(); // refresh bảng
            } catch (Exception e) {
                showAlert("Lỗi", "Giá trị không hợp lệ!");
            }
        });
    }
    // Xác nhận và xóa ca
    private void confirmAndDeleteShift(WorkShift ws) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa ca #" + ws.getShiftId() + " không?");
        alert.setContentText("Thao tác này không thể hoàn tác!");
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            ws.delete(); // xóa DB
            loadClosedShifts(); // refresh bảng
        }
    }
    // Hiện alert lỗi
    private void showAlert(String title, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Helper to format currency (double)
    private String formatCurrency(double amount) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(amount) + " ₫";
    }
    // Helper to get manager name from userId
    private String getManagerName(int userId) {
        try {
            Users user = Users.getEmployeeById(userId);
            if (user != null) return user.getFull_name();
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
    // Helper to get order count for a shift
    private int getOrderCountForShift(WorkShift shift) {
        try {
            return Order.getOrdersForShift(
                shift.getUserId(),
                shift.getStartTime(),
                shift.getEndTime(),
                null // all payment methods
            ).size();
        } catch (Exception e) {
            return 0;
        }
    }
    // Helper to get employee count for a shift (default 1 for now)
    private int getEmployeeCountForShift(WorkShift shift) {
        // If you have attendance logic, replace this
        return 1;
    }
    // Helper to get shift name from startTime
    private String getShiftName(java.sql.Timestamp startTime) {
        if (startTime == null) return "";
        int hour = startTime.toLocalDateTime().getHour();
        if (hour < 12) return "Ca sáng";
        if (hour < 18) return "Ca chiều";
        return "Ca tối";
    }
} 