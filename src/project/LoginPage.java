package project;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginPage extends Application {
    private BddComm db = new BddComm();
    private VBox dynamicFormArea = new VBox(10);
    private Label toggleText = new Label("Don't have an account? Sign up now.");
    private boolean showingLogin = true;

    @Override
    public void start(Stage primaryStage) {

        ImageView imageView = new ImageView(new Image(getClass().getResource("/resources/library2.jpg").toExternalForm()));
        imageView.setFitWidth(400);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(true);
        VBox imagePane = new VBox(imageView);
        imagePane.setAlignment(Pos.CENTER);

        toggleText.setStyle("-fx-text-fill: #8d6e4e; -fx-underline: true;");
        toggleText.setOnMouseClicked(event -> {
            if (showingLogin) {
                showSignUpForm(primaryStage);
                toggleText.setText("Already have an account? Log in now.");
            } else {
                showLoginForm(primaryStage);
                toggleText.setText("Don't have an account? Sign up now.");
            }
            showingLogin = !showingLogin;
        });

        VBox rightPane = new VBox(20);
        rightPane.setAlignment(Pos.CENTER);
        rightPane.getChildren().addAll(dynamicFormArea, toggleText);
        rightPane.setStyle("-fx-padding: 20;");

        HBox root = new HBox(imagePane, rightPane);
        root.setStyle("-fx-padding: 0; -fx-background-color: #f3e5dc;");

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());

        primaryStage.setTitle("Library System");
        primaryStage.setScene(scene);
        primaryStage.show();

        showLoginForm(primaryStage);
    }

    private void showLoginForm(Stage stage) {
        VBox loginForm = new VBox(10);
        loginForm.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Welcome to the Library");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #5e3d2f;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Login");
        Label messageLabel = new Label();

        loginButton.setOnAction(event -> {
            try {
                db.connect();
                String email = emailField.getText().trim();
                String password = passwordField.getText().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("Email and password cannot be empty.");
                    return;
                }

                if (db.authenticate(email, password)) {
                    String role = db.getUserRole(email);
                    Session.getInstance().setEmail(email);
                    Session.getInstance().setUserId(db.getUserIdByEmail(email));

                    if ("admin".equalsIgnoreCase(role)) {
                        Session.getInstance().setRole("admin");
                        new AdminMenu(stage).show(stage);
                    } else if ("member".equalsIgnoreCase(role)) {
                        Session.getInstance().setRole("member");
                        new UserMenu(stage).show(stage);
                    } else {
                        messageLabel.setText("Unknown role. Contact the administrator.");
                    }
                } else {
                    messageLabel.setText("Invalid email or password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("An error occurred. Please try again.");
            } finally {
                try {
                    db.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        loginForm.getChildren().addAll(title, new Label("Email:"), emailField, new Label("Password:"), passwordField, loginButton, messageLabel);
        dynamicFormArea.getChildren().setAll(loginForm);
    }

    private void showSignUpForm(Stage stage) {
        VBox signupForm = new VBox(10);
        signupForm.setAlignment(Pos.CENTER_LEFT);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button signupButton = new Button("Sign Up");
        Label messageLabel = new Label();

        signupButton.setOnAction(event -> {
            try {
                db.connect();
                String email = emailField.getText().trim();
                String name = nameField.getText().trim();
                String password = passwordField.getText().trim();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("All fields are required.");
                    return;
                }

                if (db.registerUser(email, name, password)) {
                    Session.getInstance().setEmail(email);
                    new UserMenu(stage).show(stage);
                    messageLabel.setText("Sign-up successful!");
                    messageLabel.setStyle("-fx-text-fill: green;");
                } else {
                    messageLabel.setText("User already exists. Try logging in.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("An error occurred. Please try again.");
            } finally {
                try {
                    db.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        signupForm.getChildren().addAll(new Label("Email:"), emailField, new Label("Name:"), nameField, new Label("Password:"), passwordField, signupButton, messageLabel);
        dynamicFormArea.getChildren().setAll(signupForm);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
