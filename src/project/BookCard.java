package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BookCard {
    private String title;
    private String author;
    private int bookId;
    private int rentalId;
    private String status;
    private String imageUrl;
    private ReturnHandler returnHandler;

    public BookCard(String title, String author, int bookId, int rentalId, String status, String imageUrl, ReturnHandler returnHandler) {
        this.title = title;
        this.author = author;
        this.bookId = bookId;
        this.rentalId = rentalId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.returnHandler = returnHandler;
    }

    public VBox createCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 260);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 2; -fx-background-radius: 10;");

        ImageView bookImageView = new ImageView();
        try {
            Image bookImage = new Image(imageUrl, 120, 150, false, true);
            bookImageView.setImage(bookImage);
        } catch (IllegalArgumentException e) {
            Image placeholderImage = new Image("/resources/book-placeholder.jpg", 120, 150, false, true);
            bookImageView.setImage(placeholderImage);
        }
        bookImageView.setFitWidth(120);
        bookImageView.setFitHeight(150);

        Label bookTitle = new Label("Title: " + title);
        bookTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label bookAuthor = new Label("Author: " + author);
        bookAuthor.setStyle("-fx-font-size: 12px;");

        Label bookStatus = new Label("Status: " + status);
        bookStatus.setStyle("-fx-font-size: 12px;");

        Button returnButton = new Button("Return Book");
        returnButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
        returnButton.setOnAction(e -> returnHandler.handleReturn(rentalId, bookId, title));

        card.getChildren().addAll(bookImageView, bookTitle, bookAuthor, bookStatus, returnButton);

        return card;
    }

    public interface ReturnHandler {
        void handleReturn(int rentalId, int bookId, String bookTitle);
    }
}
