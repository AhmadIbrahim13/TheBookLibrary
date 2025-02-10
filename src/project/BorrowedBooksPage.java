package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BorrowedBooksPage {
    private Stage stage;
    private BddComm db;
    private int userId;

    public BorrowedBooksPage(Stage stage) {
        this.userId = Session.getInstance().getUserId();
        this.stage = stage;
        this.db = new BddComm();
        db.connect();
    }

    public void show() {
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setHgap(20);
        layout.setVgap(20);
        layout.setAlignment(Pos.TOP_CENTER);

        Label noBooksMessage = new Label("You have not borrowed any books.");
        noBooksMessage.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");

        try {
            String loggedInEmail = Session.getInstance().getEmail();
            if (loggedInEmail == null) {
                layout.add(new Label("Error: No logged-in user found."), 0, 0);
                return;
            }

            int userId = db.getUserIdByEmail(loggedInEmail);
            if (userId == -1) {
                layout.add(new Label("User not found for email: " + loggedInEmail), 0, 0);
                return;
            }

            ResultSet rentedBooks = db.getRentedBooks(userId);

            int row = 0;
            int col = 0;
            boolean hasBooks = false;

            while (rentedBooks.next()) {
                hasBooks = true;

                int rentalId = rentedBooks.getInt("rent_id");
                int bookId = rentedBooks.getInt("book_id");
                String title = rentedBooks.getString("title");
                String author = rentedBooks.getString("author");
                String status = rentedBooks.getString("status");
                String imageUrl = rentedBooks.getString("image_url");

                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = "/resources/default-book-placeholder.jpg";
                }

                BookCard bookCard = new BookCard(title, author, bookId, rentalId, status, imageUrl, this::handleReturn);
                layout.add(bookCard.createCard(), col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }

            if (!hasBooks) {
                layout.add(noBooksMessage, 0, 0, 3, 1);
                layout.setAlignment(Pos.CENTER);
            }

            Button backButton = new Button("Back to Menu");
            backButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8px 16px;");
            backButton.setOnAction(e -> {
                UserMenu userMenu = new UserMenu(stage);
                userMenu.show(stage);
            });

            HBox buttonContainer = new HBox(backButton);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPadding(new Insets(20, 0, 0, 0));

            layout.add(buttonContainer, 0, row + 1, 3, 1);

        } catch (SQLException e) {
            layout.add(new Label("Error fetching borrowed books."), 0, 0);
            e.printStackTrace();
        } finally {
            try {
                db.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/cards.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void handleReturn(int rentalId, int bookId, String bookTitle) {
        ReviewDialog reviewDialog = new ReviewDialog(db, userId, rentalId, bookId, bookTitle, this::refresh);
        reviewDialog.show();
    }

    private void refresh() {
        show();
    }
}
