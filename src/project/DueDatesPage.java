package project;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DueDatesPage {
    private final Stage stage;
    private final String userEmail;
    private final BddComm db;
    private final VBox root;
    private final List<Thread> countdownThreads = new ArrayList<>();

    public DueDatesPage(Stage stage, String userEmail) {
        this.stage = stage;
        this.userEmail = userEmail;
        this.db = BddComm.getInstance();
        this.root = new VBox(10);
        root.setStyle("-fx-padding: 20;");
    }

    public void show() {
        Label heading = new Label("Books Sorted by Due Date");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox booksContainer = new VBox(10);
        root.getChildren().addAll(heading, booksContainer);

        Button backButton = new Button("Back to User Menu");
        backButton.setOnAction(e -> {
            stopCountdownThreads();
            UserMenu userMenu = new UserMenu(stage);
            userMenu.show(stage);
        });

        root.getChildren().add(backButton);

        try {
            List<RentedBook> rentedBooks = db.getUserRentedBooksSortedByDueDate(userEmail);

            for (RentedBook book : rentedBooks) {
                HBox bookItem = new HBox(10);
                Label titleLabel = new Label(book.getTitle());

                // Countdown Label
                Label countdownLabel = new Label();
                bookItem.getChildren().addAll(titleLabel, countdownLabel);

                booksContainer.getChildren().add(bookItem);

                // Start a thread to update the countdown for this book
                Thread countdownThread = new Thread(() -> updateCountdownThread(book.getDueDate(), countdownLabel));
                countdownThread.setDaemon(true); 
                countdownThread.start();
                countdownThreads.add(countdownThread);
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Error");
            errorAlert.setContentText("Unable to fetch books: " + e.getMessage());
            errorAlert.showAndWait();
        }

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/duedates.css").toExternalForm());

        stage.setTitle("Due Dates");
        stage.setScene(scene);
        stage.show();
    }

    private void updateCountdownThread(LocalDateTime dueDate, Label countdownLabel) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                LocalDateTime now = LocalDateTime.now();
                if (dueDate.isAfter(now)) {
                    long secondsLeft = java.time.Duration.between(now, dueDate).getSeconds();

                    long days = secondsLeft / (24 * 3600);
                    long hours = (secondsLeft % (24 * 3600)) / 3600;
                    long minutes = (secondsLeft % 3600) / 60;
                    long secs = secondsLeft % 60;

                    String countdownText = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, secs);

                    // Update the label on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        countdownLabel.setText(countdownText);

                        // Change text color to red if less than 7 days remain
                        if (days < 7) {
                            countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            countdownLabel.setStyle("-fx-text-fill: black;"); // Default color
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        countdownLabel.setText("Due Date Passed");
                        countdownLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    });
                    break;
                }
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
    }

    private void stopCountdownThreads() {
        for (Thread thread : countdownThreads) {
            thread.interrupt();
        }
        countdownThreads.clear();
    }
}
