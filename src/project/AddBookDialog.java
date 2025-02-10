package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AddBookDialog {
    private Stage dialogStage;
    private ManageBooksPage manageBooksPage;
    private BddComm bddComm = new BddComm();

    public AddBookDialog(Stage parentStage, ManageBooksPage manageBooksPage) {
        this.manageBooksPage = manageBooksPage;

        dialogStage = new Stage();
        dialogStage.setTitle("Add New Book");
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();
        TextField imageUrlField = new TextField();
        Label titleLabel = new Label("Title:");
        Label authorLabel = new Label("Author:");
        Label priceLabel = new Label("Price:");
        Label quantityLabel = new Label("Quantity:");
        Label imageUrlLabel = new Label("Image URL:");
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String imageUrl = imageUrlField.getText().trim();
            int price;
            int quantity;

            try {
                price = Integer.parseInt(priceField.getText().trim());
                quantity = Integer.parseInt(quantityField.getText().trim());

                boolean added = addBookToDatabase(title, author, price, quantity, imageUrl);

                if (added) {
                    manageBooksPage.loadBooks();
                    showAlert("Success", "Book added successfully.");
                } else {
                    showAlert("Failure", "Failed to add the book.");
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

        gridPane.add(titleLabel, 0, 0);
        gridPane.add(titleField, 1, 0);

        gridPane.add(authorLabel, 0, 1);
        gridPane.add(authorField, 1, 1);

        gridPane.add(priceLabel, 0, 2);
        gridPane.add(priceField, 1, 2);

        gridPane.add(quantityLabel, 0, 3);
        gridPane.add(quantityField, 1, 3);

        gridPane.add(imageUrlLabel, 0, 4);
        gridPane.add(imageUrlField, 1, 4);

        gridPane.add(saveButton, 0, 5);
        gridPane.add(cancelButton, 1, 5);

        Scene scene = new Scene(gridPane, 400, 350);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private boolean addBookToDatabase(String title, String author, int price, int quantity, String imageUrl) {
    try {
        bddComm.connect();
        var connection = bddComm.getConnection();

        String query = "INSERT INTO books (title, author, price, quantity, image_url, added_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        var stmt = connection.prepareStatement(query);

        stmt.setString(1, title);
        stmt.setString(2, author);
        stmt.setInt(3, price);
        stmt.setInt(4, quantity);
        stmt.setString(5, imageUrl);
        
        int rowsInserted = stmt.executeUpdate();

        bddComm.disconnect();

        return rowsInserted > 0;

    } catch (Exception e) {
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
