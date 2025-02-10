package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ReviewDialog {
    private BddComm db;
    private int userId;
    private int rentalId;
    private int bookId;
    private String bookTitle;
    private Runnable onClose;

    public ReviewDialog(BddComm db, int userId, int rentalId, int bookId, String bookTitle, Runnable onClose) {
        this.db = db;
        this.userId = userId;
        this.rentalId = rentalId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.onClose = onClose;
    }

    public void show() {
        Dialog<Void> reviewDialog = new Dialog<>();
        reviewDialog.setTitle("Leave a Review");
        reviewDialog.setHeaderText("Leave a review for " + bookTitle);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label("Rating (1-5):");
        TextField ratingField = new TextField();
        ratingField.setPromptText("Enter a rating (1-5)");

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your review here...");

        Button submitButton = new Button("Submit Review");
        submitButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        submitButton.setOnAction(e -> {
            try {
                int rating = Integer.parseInt(ratingField.getText());
                if (rating < 1 || rating > 5) throw new NumberFormatException();

                String comment = commentArea.getText();
                if (comment.isEmpty()) throw new IllegalArgumentException("Comment cannot be empty.");

                boolean reviewSuccess = db.addReview(userId, bookId, rating, comment);
                if (reviewSuccess) {
                    boolean returnSuccess = db.returnBook(rentalId);
                    if (returnSuccess) {
                        db.incrementBookQuantity(bookId);
                        System.out.println("Book returned and review submitted successfully.");
                        reviewDialog.close();
                        onClose.run(); // Refresh page
                    } else {
                        System.out.println("Failed to return the book.");
                    }
                } else {
                    System.out.println("Failed to submit the review.");
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid rating between 1 and 5.");
                alert.show();
            } catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                alert.show();
            }
        });

        content.getChildren().addAll(ratingLabel, ratingField, commentLabel, commentArea, submitButton);
        reviewDialog.getDialogPane().setContent(content);
        reviewDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        reviewDialog.showAndWait();
    }
}
