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

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SearchBooksPage {
    private VBox bookCardsContainer;
    private BddComm bddComm = new BddComm();
    private Stage stage;
    private int userId;

    public SearchBooksPage(Stage stage) {
        this.stage = stage;
        this.userId = Session.getInstance().getUserId();

        // Search bar setup
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a book...");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchBooks(searchField.getText()));

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setPadding(new Insets(10));

        // Book cards container
        bookCardsContainer = new VBox();
        bookCardsContainer.setSpacing(10);
        bookCardsContainer.setPadding(new Insets(10));

        // Scroll pane for book cards
        ScrollPane scrollPane = new ScrollPane(bookCardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        // Back button
        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> goBack());

        // Main layout
        VBox mainLayout = new VBox(searchBar, scrollPane, backButton);
        mainLayout.setSpacing(10);
        mainLayout.setAlignment(Pos.CENTER);

        // Scene setup
        Scene scene = new Scene(mainLayout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/cards.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Search Books");
        stage.show();

        // Load all books initially
        loadBooks();
    }

    private void loadBooks() {
        bookCardsContainer.getChildren().clear();
        String query = """
                SELECT b.book_id, b.title, b.author, b.price, b.quantity, b.image_url, 
                       IFNULL(AVG(r.rating), 0) AS average_rating
                FROM books b
                LEFT JOIN reviews r ON b.book_id = r.book_id
                GROUP BY b.book_id, b.title, b.author, b.price, b.quantity, b.image_url;
                """;

        try {
            bddComm.connect();
            var connection = bddComm.getConnection();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                displayBooks(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            bddComm.disconnect();
        }
    }

    private void searchBooks(String searchText) {
        bookCardsContainer.getChildren().clear();
        String query = """
                SELECT b.book_id, b.title, b.author, b.price, b.quantity, b.image_url, 
                       IFNULL(AVG(r.rating), 0) AS average_rating
                FROM books b
                LEFT JOIN reviews r ON b.book_id = r.book_id
                WHERE b.title LIKE ?
                GROUP BY b.book_id, b.title, b.author, b.price, b.quantity, b.image_url;
                """;

        try {
            bddComm.connect();
            var connection = bddComm.getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, "%" + searchText + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.isBeforeFirst()) {
                        Label noResults = new Label("No books found.");
                        noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
                        bookCardsContainer.getChildren().add(noResults);
                    } else {
                        displayBooks(rs);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            bddComm.disconnect();
        }
    }

    private void displayBooks(ResultSet rs) throws SQLException {
        HBox rowContainer = new HBox(10);
        rowContainer.setAlignment(Pos.CENTER);

        int count = 0;

        while (rs.next()) {
            Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("price"),
                    rs.getInt("quantity"),
                    rs.getString("image_url"),
                    rs.getDouble("average_rating")
            );

            VBox bookCard = createBookCard(book);
            rowContainer.getChildren().add(bookCard);
            count++;

            if (count == 3) {
                bookCardsContainer.getChildren().add(rowContainer);
                rowContainer = new HBox(10);
                rowContainer.setAlignment(Pos.CENTER);
                count = 0;
            }
        }

        if (!rowContainer.getChildren().isEmpty()) {
            bookCardsContainer.getChildren().add(rowContainer);
        }
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 300);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 2; -fx-background-radius: 10;");

        ImageView bookImageView = new ImageView();
        try {
            if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                bookImageView.setImage(new Image(book.getImageUrl(), 120, 150, false, true));
            } else {
                bookImageView.setImage(new Image("/resources/book-placeholder.jpg", 120, 150, false, true));
            }
        } catch (IllegalArgumentException e) {
            bookImageView.setImage(new Image("/resources/book-placeholder.jpg", 120, 150, false, true));
        }
        bookImageView.setFitWidth(120);
        bookImageView.setFitHeight(150);

        Text titleText = new Text("Title: " + book.getTitle());
        Text authorText = new Text("Author: " + book.getAuthor());
        Text priceText = new Text("Price per day: $" + book.getPrice());
        Text quantityText = new Text("Quantity: " + book.getQuantity());
        Text ratingText = new Text(String.format("Rating: %.1f ★", book.getAverageRating()));

        Button rentButton = new Button("Rent Book");
        rentButton.setOnAction(e -> rentBook(book));
        if (book.getQuantity() <= 0) {
            rentButton.setDisable(true);
            rentButton.setText("Out of Stock");
            rentButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        }

        Button reviewButton = new Button("Check Reviews");
        reviewButton.setOnAction(e -> checkReviews(book.getBookId()));

        card.getChildren().addAll(bookImageView, titleText, authorText, priceText, quantityText, ratingText, rentButton, reviewButton);
        return card;
    }

    private void checkReviews(int bookId) {
        Dialog<Void> reviewsDialog = new Dialog<>();
        reviewsDialog.setTitle("Book Reviews");
        reviewsDialog.setHeaderText("Reviews for Book ID: " + bookId);

        VBox reviewsContainer = new VBox(10);
        reviewsContainer.setPadding(new Insets(10));
        reviewsContainer.setAlignment(Pos.CENTER);

        String query = """
                SELECT u.name AS user_name, r.comment, r.rating
                FROM reviews r
                JOIN users u ON r.user_id = u.user_id
                WHERE r.book_id = ?;
                """;

        try {
            bddComm.connect();
            var connection = bddComm.getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, bookId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.isBeforeFirst()) {
                        reviewsContainer.getChildren().add(new Label("No reviews found for this book."));
                    } else {
                        while (rs.next()) {
                            String userName = rs.getString("user_name");
                            String comment = rs.getString("comment");
                            int rating = rs.getInt("rating");

                            Label reviewLabel = new Label(String.format("%s: %s (%d★)", userName, comment, rating));
                            reviewLabel.setWrapText(true);
                            reviewsContainer.getChildren().add(reviewLabel);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            reviewsContainer.getChildren().add(new Label("Error fetching reviews."));
            e.printStackTrace();
        } finally {
            bddComm.disconnect();
        }

        reviewsDialog.getDialogPane().setContent(new ScrollPane(reviewsContainer));
        reviewsDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        reviewsDialog.showAndWait();
    }

    private void goBack() {
        UserMenu userMenu = new UserMenu(stage);
        userMenu.show(stage);
    }

    private void rentBook(Book book) {
    int currentUserId = Session.getInstance().getUserId();

    if (currentUserId == 0) {
        showAlert("Session Error", "User is not logged in. Please log in again.");
        return;
    }

    Dialog<Void> rentDialog = new Dialog<>();
    rentDialog.setTitle("Rent Book");
    rentDialog.setHeaderText("Enter Rental Details");

    Label dueDateLabel = new Label("Due Date:");
    DatePicker dueDatePicker = new DatePicker();
    dueDatePicker.setValue(LocalDate.now().plusDays(1)); // Default due date to one day after today

    // Disable past and current dates
    dueDatePicker.setDayCellFactory(datePicker -> new DateCell() {
        @Override
        public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);
            // Disable past and current dates
            if (date.isBefore(LocalDate.now().plusDays(1))) {
                setDisable(true);
                setStyle("-fx-background-color: #ffc0cb;"); 
            }
        }
    });

    Label totalCostLabel = new Label("Total Cost: $0.00");

    dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue != null && newValue.isAfter(LocalDate.now())) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), newValue);
            double totalCost = days * book.getPrice();
            totalCostLabel.setText(String.format("Total Cost: $%.2f", totalCost));
        }
    });

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));

    grid.add(dueDateLabel, 0, 0);
    grid.add(dueDatePicker, 1, 0);
    grid.add(totalCostLabel, 0, 1, 2, 1);

    rentDialog.getDialogPane().setContent(grid);

    ButtonType rentButtonType = new ButtonType("Rent", ButtonBar.ButtonData.OK_DONE);
    rentDialog.getDialogPane().getButtonTypes().addAll(rentButtonType, ButtonType.CANCEL);

    rentDialog.setResultConverter(dialogButton -> {
        if (dialogButton == rentButtonType) {
            LocalDate dueDate = dueDatePicker.getValue();

            if (dueDate != null) {
                try {
                    bddComm.connect();
                    var connection = bddComm.getConnection();

                    // Insert rental record
                    String insertQuery = "INSERT INTO rents (user_id, book_id, due_date, total_price) VALUES (?, ?, ?, ?)";
                    var stmtInsert = connection.prepareStatement(insertQuery);
                    long rentalDays = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
                    double totalPrice = rentalDays * book.getPrice();

                    stmtInsert.setInt(1, currentUserId);
                    stmtInsert.setInt(2, book.getBookId());
                    stmtInsert.setDate(3, Date.valueOf(dueDate));
                    stmtInsert.setDouble(4, totalPrice);
                    stmtInsert.executeUpdate();

                    String updateQuantityQuery = "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?";
                    var stmtUpdateQuantity = connection.prepareStatement(updateQuantityQuery);
                    stmtUpdateQuantity.setInt(1, book.getBookId());

                    int rowsUpdated = stmtUpdateQuantity.executeUpdate();

                    if (rowsUpdated > 0) {
                        showAlert("Rent Successful", String.format("Book rented successfully! Total cost: $%.2f", totalPrice));
                        loadBooks(); // Refresh book list
                    } else {
                        showAlert("Error", "Failed to update book quantity.");
                    }

                    bddComm.disconnect();

                } catch (SQLException ex) {
                    showAlert("Database Error", "Failed to rent the book or update quantity.");
                    ex.printStackTrace();
                }
            } else {
                showAlert("Input Error", "Please select a due date.");
            }
        }
        return null;
    });

    rentDialog.showAndWait();
}


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void show(Stage stage) {
        stage.show();
    }
}
