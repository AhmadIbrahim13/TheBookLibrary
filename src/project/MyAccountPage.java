package project;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MyAccountPage {
    private Stage stage;
    private String email;
    private BddComm db;
    private int userId;

    public MyAccountPage(Stage stage, String email) {
        this.stage = stage;
        this.email = email;
        this.db = BddComm.getInstance();  // Initialize the database instance
        this.userId = Session.getInstance().getUserId();
    }

    public void show() {
        GridPane accountPane = new GridPane();
        accountPane.setAlignment(Pos.CENTER);
        accountPane.setHgap(10);
        accountPane.setVgap(15);

        Label title = new Label("My Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(email);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        Label passwordLabel = new Label("New Password:");
        PasswordField passwordField = new PasswordField();

        Button updateButton = new Button("Update Info");
        Button backButton = new Button("Back to Menu"); // Back button

        // Fetch user details from the database
        try {
            ResultSet userDetails = db.getUserDetails(email);
            if (userDetails != null && userDetails.next()) {
                String name = userDetails.getString("name");
                nameField.setText(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load user details.");
        }

        accountPane.add(title, 0, 0, 2, 1);
        accountPane.add(emailLabel, 0, 1);
        accountPane.add(emailField, 1, 1);
        accountPane.add(nameLabel, 0, 2);
        accountPane.add(nameField, 1, 2);
        accountPane.add(passwordLabel, 0, 3);
        accountPane.add(passwordField, 1, 3);
        accountPane.add(updateButton, 1, 4);
        accountPane.add(backButton, 1, 5); 

        updateButton.setOnAction(event -> {
    String newEmail = emailField.getText().trim();
    String newName = nameField.getText().trim();
    String newPassword = passwordField.getText().trim();

    boolean hasUpdate = false;

    try {
        if (!newEmail.equals(email)) {
            if (db.isEmailTaken(newEmail)) {
                showError("Email Conflict", "Email already exists. Try another email.");
                return;
            }
            if (db.updateUserEmail(email, newEmail)) {
                hasUpdate = true;
                Session.getInstance().setEmail(newEmail); 
                email = newEmail;
            }
        }

        if (!newName.isEmpty() && db.updateUserName(email, newName)) {
            hasUpdate = true;
        }

        if (!newPassword.isEmpty() && db.updateUserPassword(email, newPassword)) {
            hasUpdate = true;
        }

        if (hasUpdate) {
            showInfo("Success", "Account details updated.");
        } else {
            showInfo("No Changes", "No updates were made to your account.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showError("Database Error", "Failed to update user information.");
    }
});



        backButton.setOnAction(event -> {
            UserMenu userMenu = new UserMenu(stage);
            userMenu.show(stage);
        });

        Scene scene = new Scene(accountPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/admin.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
