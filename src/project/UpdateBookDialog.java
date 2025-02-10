package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UpdateBookDialog {
    private Stage dialogStage;
    private ManageBooksPage manageBooksPage;
    private Book book;
    private BddComm bddComm = new BddComm();

    public UpdateBookDialog(Stage parentStage, ManageBooksPage manageBooksPage, Book book) {
        this.manageBooksPage = manageBooksPage;
        this.book = book;

        dialogStage = new Stage();
        dialogStage.setTitle("Update Book Information");

        // Input fields
        TextField titleField = new TextField(book.getTitle());
        TextField authorField = new TextField(book.getAuthor());
        TextField priceField = new TextField(String.valueOf(book.getPrice()));
        TextField quantityField = new TextField(String.valueOf(book.getQuantity()));
        TextField imageUrlField = new TextField(book.getImageUrl());

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            String newTitle = titleField.getText().trim();
            String newAuthor = authorField.getText().trim();
            String newImageUrl = imageUrlField.getText().trim();
            int newPrice;
            int newQuantity;

            try {
                newPrice = Integer.parseInt(priceField.getText().trim());
                newQuantity = Integer.parseInt(quantityField.getText().trim());

                if (updateBookInDatabase(newTitle, newAuthor, newPrice, newQuantity, newImageUrl)) {
                    manageBooksPage.loadBooks(); // Refresh the book table
                    showAlert("Update Successful", "Book updated successfully.");
                } else {
                    showAlert("Update Failed", "Failed to update book information.");
                }

                dialogStage.close();

            } catch (NumberFormatException e) {
                showAlert("Input Error", "Price and Quantity must be valid integers.");
            }
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(new Label("Title:"), 0, 0);
        gridPane.add(titleField, 1, 0);

        gridPane.add(new Label("Author:"), 0, 1);
        gridPane.add(authorField, 1, 1);

        gridPane.add(new Label("Price:"), 0, 2);
        gridPane.add(priceField, 1, 2);

        gridPane.add(new Label("Quantity:"), 0, 3);
        gridPane.add(quantityField, 1, 3);

        gridPane.add(new Label("Image URL:"), 0, 4);
        gridPane.add(imageUrlField, 1, 4);

        gridPane.add(saveButton, 0, 5);
        gridPane.add(cancelButton, 1, 5);

        Scene scene = new Scene(gridPane, 400, 350);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private boolean updateBookInDatabase(String title, String author, int price, int quantity, String imageUrl) {
        try {
            bddComm.connect();
            var connection = bddComm.getConnection();

            String query = "UPDATE books SET title = ?, author = ?, price = ?, quantity = ?, image_url = ? WHERE book_id = ?";
            var stmt = connection.prepareStatement(query);

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, price);
            stmt.setInt(4, quantity);
            stmt.setString(5, imageUrl);
            stmt.setInt(6, book.getBookId());

            int rowsUpdated = stmt.executeUpdate();

            bddComm.disconnect();

            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void show() {
        dialogStage.show();
    }
}
