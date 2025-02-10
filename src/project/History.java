package project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class History {
    private Stage stage;
    private BddComm bddComm;

    public History(Stage stage) {
        this.stage = stage;
        this.bddComm = new BddComm();
        this.bddComm.connect(); // Establish database connection
    }

    public void show(Stage stage1) {
        // Table to display renting information
        TableView<RentInfo> table = new TableView<>();

        // Define columns
        TableColumn<RentInfo, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<RentInfo, String> bookNameCol = new TableColumn<>("Book Name");
        bookNameCol.setCellValueFactory(new PropertyValueFactory<>("bookName"));

        TableColumn<RentInfo, LocalDate> rentedDateCol = new TableColumn<>("Rented Date");
        rentedDateCol.setCellValueFactory(new PropertyValueFactory<>("rentedDate"));

        TableColumn<RentInfo, LocalDate> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<RentInfo, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        table.getColumns().addAll(usernameCol, bookNameCol, rentedDateCol, dueDateCol, statusCol);

        // Fetch data from the database and set it in the table
        ObservableList<RentInfo> rentInfoList = fetchRentData();
        table.setItems(rentInfoList);

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            new AdminMenu(stage).show(stage);
            bddComm.disconnect(); // Close connection when navigating back
        });

        // Print button
        Button printButton = new Button("Print All");
        printButton.setOnAction(event -> {
            generateAllRentalsTextFile(table);
        });

        // Layout for buttons
        HBox buttonBox = new HBox(10, backButton, printButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Root layout
        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(10));

        // Scene and stage setup
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/admin.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Fetch rental data from the database.
     *
     * @return ObservableList of RentInfo
     */
    private ObservableList<RentInfo> fetchRentData() {
        ObservableList<RentInfo> rentInfoList = FXCollections.observableArrayList();

        String query = """
            SELECT u.name AS username, b.title AS bookName, r.rented_date, r.due_date, r.status
            FROM rents r
            INNER JOIN users u ON r.user_id = u.user_id
            INNER JOIN books b ON r.book_id = b.book_id;
        """;

        try (ResultSet resultSet = bddComm.getConnection().createStatement().executeQuery(query)) {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String bookName = resultSet.getString("bookName");
                LocalDate rentedDate = resultSet.getTimestamp("rented_date").toLocalDateTime().toLocalDate();
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                String status = resultSet.getString("status");

                RentInfo rentInfo = new RentInfo(username, bookName, rentedDate, dueDate, status);
                rentInfoList.add(rentInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rentInfoList;
    }

    /**
     * Generate a text file containing all rental information from the table.
     *
     * @param table TableView containing rental information
     */
    private void generateAllRentalsTextFile(TableView<RentInfo> table) {
        try {
            // Get the user's home directory and the Downloads folder path
            String userHome = System.getProperty("user.home");
            String downloadsFolder = userHome + "/Downloads/";

            // Generate a unique filename
            String filename = downloadsFolder + "All_Rentals_" + System.currentTimeMillis() + ".txt";

            // Use FileWriter to create the text file
            try (FileWriter writer = new FileWriter(filename)) {
                // Write header
                writer.write("ALL RENTAL INFORMATION\n");
                writer.write("=====================\n\n");

                // Write column headers
                writer.write(String.format("%-20s %-30s %-15s %-15s %-10s\n", "Username", "Book Name", "Rented Date", "Due Date", "Status"));
                writer.write("------------------------------------------------------------------------\n");

                // Write each rental record to the file
                for (RentInfo rentInfo : table.getItems()) {
                    writer.write(String.format(
                            "%-20s %-30s %-15s %-15s %-10s\n",
                            rentInfo.getUsername(),
                            rentInfo.getBookName(),
                            rentInfo.getRentedDate(),
                            rentInfo.getDueDate(),
                            rentInfo.getStatus()
                    ));
                }

                writer.write("\nThank you for using our system!");
            }

            // Show success alert with file location
            showAlert("Success", "Rental information exported successfully: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export rental information.");
        }
    }

    /**
     * Show an alert with a message.
     *
     * @param title   Alert title
     * @param message Alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
