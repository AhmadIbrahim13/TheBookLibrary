package project;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrintInvoices {
    private Stage stage;
    private BddComm bddComm;

    public PrintInvoices(Stage stage) {
        this.stage = stage;
        this.bddComm = new BddComm();
    }

    public void show(Stage stage1) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Label userLabel = new Label("Select User:");
        ComboBox<String> userComboBox = new ComboBox<>();
        loadUsers(userComboBox);

        Label dateRangeLabel = new Label("Select Date Range:");
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER);
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        dateRangeBox.getChildren().addAll(new Label("From:"), startDatePicker, new Label("To:"), endDatePicker);

        Button generateButton = new Button("Generate Invoice");
        Button printButton = new Button("Print Out");
        printButton.setDisable(true); // Initially disabled until the invoice is generated

        // Configure the TableView
        TableView<InvoiceItem> invoiceTable = new TableView<>();
        configureTable(invoiceTable);

        Label totalPriceLabel = new Label("Total Price: $0.00");

        generateButton.setOnAction(e -> {
            String selectedUser = userComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (selectedUser == null || startDate == null || endDate == null) {
                showAlert("Error", "Please select a user and specify the date range.");
            } else {
                generateInvoice(selectedUser, startDate, endDate, invoiceTable, totalPriceLabel);
                printButton.setDisable(false); // Enable the print button after generating the invoice
            }
        });

        printButton.setOnAction(e -> {
    String selectedUser = userComboBox.getValue();
            try {
                generateInvoiceTextFile(selectedUser, invoiceTable, totalPriceLabel.getText());
            } catch (IOException ex) {
                Logger.getLogger(PrintInvoices.class.getName()).log(Level.SEVERE, null, ex);
            }
});


        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> {
            AdminMenu adminMenu = new AdminMenu(stage);
            adminMenu.show(stage);
        });

        layout.getChildren().addAll(userLabel, userComboBox, dateRangeLabel, dateRangeBox, generateButton, printButton, invoiceTable, totalPriceLabel, backButton);

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/invoices.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Print Invoices");
        stage.show();
    }

    private void loadUsers(ComboBox<String> userComboBox) {
        try {
            bddComm.connect();
            Connection connection = bddComm.getConnection();
            String query = "SELECT email FROM users";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                userComboBox.getItems().add(rs.getString("email"));
            }

            bddComm.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configureTable(TableView<InvoiceItem> invoiceTable) {
        TableColumn<InvoiceItem, String> titleColumn = new TableColumn<>("Book Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<InvoiceItem, Double> priceColumn = new TableColumn<>("Total Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<InvoiceItem, LocalDate> rentedDateColumn = new TableColumn<>("Rented Date");
        rentedDateColumn.setCellValueFactory(new PropertyValueFactory<>("rentedDate"));

        TableColumn<InvoiceItem, LocalDate> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        invoiceTable.getColumns().addAll(titleColumn, priceColumn, rentedDateColumn, dueDateColumn);
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void generateInvoice(String userEmail, LocalDate startDate, LocalDate endDate, TableView<InvoiceItem> invoiceTable, Label totalPriceLabel) {
        try {
            bddComm.connect();
            Connection connection = bddComm.getConnection();

            String query = """
                SELECT books.title, books.price, rents.rented_date, rents.due_date
                FROM rents
                JOIN books ON rents.book_id = books.book_id
                JOIN users ON rents.user_id = users.user_id
                WHERE users.email = ? AND rents.rented_date BETWEEN ? AND ?
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            List<InvoiceItem> invoiceItems = new ArrayList<>();
            double totalPrice = 0;

            while (rs.next()) {
                String title = rs.getString("title");
                double pricePerDay = rs.getDouble("price");
                LocalDate rentedDate = rs.getDate("rented_date").toLocalDate();
                LocalDate dueDate = rs.getDate("due_date").toLocalDate(); // Ensure dueDate is extracted correctly

                long daysRented = ChronoUnit.DAYS.between(rentedDate, dueDate);
                double totalItemPrice = pricePerDay * daysRented;

                     // Pass the correct dueDate to the InvoiceItem
                invoiceItems.add(new InvoiceItem(title, totalItemPrice, rentedDate, dueDate));
                totalPrice += totalItemPrice;
}


            if (invoiceItems.isEmpty()) {
                showAlert("No Rentals Found", "No books were rented within the specified dates.");
            } else {
                invoiceTable.getItems().setAll(invoiceItems);
                totalPriceLabel.setText(String.format("Total Price: $%.2f", totalPrice));
            }

            bddComm.disconnect();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to generate the invoice.");
        }
    }

   private void generateInvoiceTextFile(String userEmail, TableView<InvoiceItem> invoiceTable, String totalPrice) throws IOException {
       // Get the user's home directory and the Downloads folder path
       String userHome = System.getProperty("user.home");
       String downloadsFolder = userHome + "/Downloads/";
       // Create a unique filename for the invoice
       String filename = downloadsFolder + "Invoice_" + userEmail.replace("@", "_") + "_" + System.currentTimeMillis() + ".txt";
       // Use FileWriter to create the invoice text file
       try (FileWriter writer = new FileWriter(filename)) {
           // Write the invoice header
           writer.write("INVOICE\n");
           writer.write("====================\n\n");
           
           // Write user details
           writer.write("User: " + userEmail + "\n");
           writer.write("Total Price: " + totalPrice + "\n\n");
           
           // Write table header
           writer.write(String.format("%-30s %-15s %-15s %-15s\n", "Book Title", "Total Price", "Rented Date", "Due Date"));
           writer.write("---------------------------------------------------------------\n");
           
           // Write the details of each item in the invoice
           for (InvoiceItem item : invoiceTable.getItems()) {
               writer.write(String.format("%-30s %-15.2f %-15s %-15s\n",
                       item.getTitle(),
                       item.getPrice(),
                       item.getRentedDate(),
                       item.getDueDate()));
           }
           
           // Add a footer
           writer.write("\nThank you for your business!\n");
       }
       // Show success alert with file location
       showAlert("Success", "Invoice generated successfully: " + filename);
}


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
