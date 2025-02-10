package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddUserDialog {
    private Stage stage;
    private ManageUsersPage manageUsersPage;
    private BddComm bddComm = new BddComm();  // Create an instance of BddComm

    public AddUserDialog(Stage owner, ManageUsersPage manageUsersPage) {
        this.stage = new Stage();
        this.manageUsersPage = manageUsersPage;

        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add User");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button addButton = new Button("Add User");
        addButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert("Input Error", "Full Name, Email, and Password cannot be empty.");
                return;
            }

            try {
                String hashedPassword = hashPassword(password);
                if (addUser(name, email, hashedPassword)) {
                    showAlert("Success", "User added successfully.");
                    stage.close();
                    manageUsersPage.loadUsers();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to hash password.");
            }
        });

        layout.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, passwordLabel, passwordField, addButton);

        Scene scene = new Scene(layout, 300, 300);
        stage.setScene(scene);
        stage.show();
    }

    private boolean addUser(String name, String email, String passwordHash) {
        try (Connection connection = bddComm.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, 'member')")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to add user.");
            return false;
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void show() {
        stage.show();
    }
}
