package project;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class UserMenu {
    private Stage stage;

    public UserMenu(Stage stage) {
        this.stage = stage;
    }

    public void show(Stage stage1) {
        String userEmail = Session.getInstance().getEmail(); // Get the user's email from the session.

        // Example: Display a welcome message using the session data.
        Label welcomeLabel = new Label("Welcome, " + userEmail + "!");
        // Create a GridPane for button layout
        GridPane menu = new GridPane();
        menu.setAlignment(Pos.CENTER);
        menu.setHgap(20); // Horizontal spacing between buttons
        menu.setVgap(20); // Vertical spacing between rows

        // Title
        Label title = new Label("User Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #5e3d2f;");

        // Create buttons
        Button searchBooks = new Button("Search Books");
        Button borrowedBooks = new Button("My Borrowed Books");
        Button myAccount = new Button("My Account");
        Button dueDates = new Button("Due Dates");
        Button logout = new Button("Logout");

        // Style buttons (optional: consistent UI styling)
        String buttonStyle = "-fx-font-size: 16px; -fx-padding: 10px 20px;";
        searchBooks.setStyle(buttonStyle);
        borrowedBooks.setStyle(buttonStyle);
        myAccount.setStyle(buttonStyle);
        dueDates.setStyle(buttonStyle);
        logout.setStyle(buttonStyle);

        // Set button actions
        searchBooks.setOnAction(event -> {
            try {
                new SearchBooksPage(stage).show(stage);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Unable to open Search Books page.");
            }
        });

        borrowedBooks.setOnAction(event -> {
            try {
                new BorrowedBooksPage(stage).show();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Unable to open Borrowed Books page.");
            }
        });

        myAccount.setOnAction(event -> {
            try {
                // Retrieve the user's email from the Session instance


                // Pass the email to the MyAccountPage and open it
                new MyAccountPage(stage, userEmail).show();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Unable to open My Account page.");
            }
        });

        dueDates.setOnAction(e -> {

            new DueDatesPage(stage, userEmail).show();;
        });

        logout.setOnAction(event -> {
            try {
                new LoginPage().start(stage);
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Unable to log out.");
            }
        });

        // Add title and buttons to the grid
        menu.add(title, 0, 0, 2, 1); // Title spanning two columns
        menu.add(searchBooks, 0, 1);
        menu.add(borrowedBooks, 1, 1);
        menu.add(myAccount, 0, 2);
        menu.add(dueDates, 1, 2);

        // Add Logout button under the middle of the two rows above
        menu.add(logout, 0, 3, 2, 1); // Spanning two columns
        GridPane.setHalignment(logout, javafx.geometry.HPos.CENTER); // Center align the logout button

        // Create the scene and set it on the stage
        Scene scene = new Scene(menu, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/admin.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("User Menu");
        stage.show(); // Ensure the stage is shown
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
