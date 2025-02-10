package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageBooksPage {
    private VBox bookCardsContainer;
    private BddComm bddComm = new BddComm();

    public ManageBooksPage(Stage stage) {
        bookCardsContainer = new VBox();
        bookCardsContainer.setSpacing(10);
        bookCardsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(bookCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        Button addButton = new Button("Add Book");
        addButton.setOnAction(e -> {
            new AddBookDialog(stage, this).show();
        });

        // Back Button to navigate back to AdminPage
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new AdminMenu(stage).show(stage);  // Assuming you have an AdminPage class
        });

        VBox mainLayout = new VBox(scrollPane, addButton, backButton);
        mainLayout.setSpacing(10);
        mainLayout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(mainLayout, 600, 800);
        scene.getStylesheets().add(getClass().getResource("/resources/cards.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Manage Books");
        stage.show();

        loadBooks();
    }

    public void loadBooks() {
        bookCardsContainer.getChildren().clear();

        try {
            bddComm.connect();
            var connection = bddComm.getConnection();

            String query = "SELECT book_id, title, author, price, quantity, image_url FROM books";
            var stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("price"),
                        rs.getInt("quantity"),
                        rs.getString("image_url")
                );

                HBox bookCard = createBookCard(book);
                bookCardsContainer.getChildren().add(bookCard);
            }

            bddComm.disconnect();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createBookCard(Book book) {
        HBox card = new HBox();
        card.setPadding(new Insets(10));
        card.setSpacing(15);
        card.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
        card.setAlignment(Pos.CENTER_LEFT);

        ImageView bookImageView = new ImageView();
        try {
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                Image bookImage = new Image(book.getImageUrl(), 100, 150, false, true);
                bookImageView.setImage(bookImage);
            } else {
                Image placeholderImage = new Image("/resources/book-placeholder.jpg", 100, 150, false, true);
                bookImageView.setImage(placeholderImage);
            }
        } catch (IllegalArgumentException e) {
            Image placeholderImage = new Image("/resources/book-placeholder.jpg", 100, 150, false, true);
            bookImageView.setImage(placeholderImage);
        }
        bookImageView.setFitWidth(100);
        bookImageView.setFitHeight(150);

        VBox bookDetails = new VBox();
        bookDetails.setSpacing(5);

        Text titleText = new Text("Title: " + book.getTitle());
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Text authorText = new Text("Author: " + book.getAuthor());
        Text priceText = new Text("Price per day: $" + book.getPrice());
        Text quantityText = new Text("Quantity: " + book.getQuantity());

        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> new UpdateBookDialog(new Stage(), this, book).show());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteBook(book.getBookId()));

        HBox buttonBox = new HBox(10, updateButton, deleteButton);

        bookDetails.getChildren().addAll(titleText, authorText, priceText, quantityText, buttonBox);
        card.getChildren().addAll(bookImageView, bookDetails);

        return card;
    }

    private void deleteBook(int bookId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Book");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this book?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bddComm.connect();
                    var connection = bddComm.getConnection();

                    String query = "DELETE FROM books WHERE book_id = ?";
                    var stmt = connection.prepareStatement(query);
                    stmt.setInt(1, bookId);

                    int rowsDeleted = stmt.executeUpdate();

                    bddComm.disconnect();

                    if (rowsDeleted > 0) {
                        loadBooks();  // Refresh the book cards
                        showAlert("Delete Successful", "Book deleted successfully.");
                    } else {
                        showAlert("Delete Failed", "Book not found.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to delete the book.");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show(Stage stage) {
    stage.show();
}



}
