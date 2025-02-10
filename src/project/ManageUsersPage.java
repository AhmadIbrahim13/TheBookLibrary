package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ManageUsersPage {
    private Stage stage;
    private TableView<User> userTable;
    private BddComm bddComm = new BddComm();

    public ManageUsersPage(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Title
        Label title = new Label("Manage Users");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #5e3d2f;");

        // Back button to go back to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> goBack());

        HBox topBox = new HBox(10, backButton, title);
        topBox.setAlignment(Pos.CENTER_LEFT);

        // TableView for displaying users
        userTable = new TableView<>();

        // Define columns
        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTable.getColumns().addAll(nameColumn, emailColumn, roleColumn);

        // Load users from the database
        loadUsers();

        // Add and Delete buttons
        Button addUserButton = new Button("Add User");
        Button deleteUserButton = new Button("Delete User");

        addUserButton.setOnAction(event -> new AddUserDialog(stage, this).show());
        deleteUserButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                deleteUser(selectedUser);
            } else {
                showAlert("No User Selected", "Please select a user to delete.");
            }
        });

        VBox buttonBox = new VBox(10, addUserButton, deleteUserButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.setTop(topBox);  // Place title and back button in the top layout
        layout.setCenter(userTable);
        layout.setBottom(buttonBox);

        // Create the scene
        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/admin.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private void goBack() {
        // Here, you would go back to the previous scene or controller
        // For example, it might load the Admin Dashboard Page
        AdminMenu dashboard = new AdminMenu(stage);
        dashboard.show(stage);
    }

    // Method to load users from the database
    void loadUsers() {
        try {
            bddComm.connect();
            Connection connection = bddComm.getConnection();

            String query = "SELECT user_id, name, email, role FROM users WHERE role = 'member'";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            ObservableList<User> users = FXCollections.observableArrayList();
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");

                users.add(new User(userId, name, email, role));
            }

            userTable.setItems(users);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Failed to load users.");
        } finally {
            bddComm.disconnect();
        }
    }

    // Method to delete a user from the database
    private void deleteUser(User user) {
        try {
            bddComm.connect();
            Connection connection = bddComm.getConnection();

            String query = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user.getUserId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                loadUsers();  // Refresh the table after deletion
                showAlert("User Deleted", "User " + user.getName() + " has been deleted.");
            } else {
                showAlert("Deletion Failed", "User could not be found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error", "Failed to delete user.");
        } finally {
            bddComm.disconnect();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
