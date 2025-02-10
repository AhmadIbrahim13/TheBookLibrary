package project;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BddComm {
 private static BddComm instance;
    public static BddComm getInstance() {
    if (instance == null) {
        synchronized (BddComm.class) {
            if (instance == null) {
                instance = new BddComm();
                instance.connect(); // Ensure the database connection is initialized
            }
        }
    }
    return instance;
}

    
    private Connection connection;

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/oop2_proj";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "oracle123";

    /**
     * Establish a database connection.
     */
    public void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connected successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Close the database connection.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database disconnected successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to disconnect from the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the database connection.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to get a database connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticate a user by email and password.
     */
    public boolean authenticate(String email, String password) {
        String query = "SELECT role FROM users WHERE email = ? AND password_hash = SHA2(?, 256)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Register a new user with email, name, and password.
     */
    public boolean registerUser(String email, String name, String password) {
        String query = "INSERT INTO users (email, name, password_hash) VALUES (?, ?, SHA2(?, 256))";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, name);
            stmt.setString(3, password);

            stmt.executeUpdate();
            System.out.println("User registered successfully with email: " + email);
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("User already exists: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Fetch the role of a user by their email.
     */
    public String getUserRole(String email) {
        String query = "SELECT role FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during role retrieval: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add a new book to the database.
     */
    public boolean addBook(String title, String author, int price, int quantity) {
        String query = "INSERT INTO books (title, author, price, quantity, added_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, price);
            stmt.setInt(4, quantity);

            stmt.executeUpdate();
            System.out.println("Book added successfully.");
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a book from the database.
     */
    public boolean deleteBook(int bookId) {
        String query = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bookId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Rent a book and store rental information in the database.
     */
    public boolean rentBook(int userId, int bookId, LocalDate dueDate) {
        String query = "INSERT INTO rents (user_id, book_id, due_date) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            stmt.setDate(3, Date.valueOf(dueDate));

            stmt.executeUpdate();
            System.out.println("Book rented successfully.");
            return true;

        } catch (SQLException ex) {
            System.err.println("Failed to rent the book: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    public ResultSet getRentedBooks(String email) {
    try {
        String query = "SELECT r.book_id, b.title, b.author, r.status " +
                        "FROM rents r " +
                        "JOIN books b ON r.book_id = b.book_id " +
                        "WHERE r.user_id = (SELECT user_id FROM users WHERE email = ?) AND r.status = 'Rented'";

        PreparedStatement stmt = getConnection().prepareStatement(query);
        stmt.setString(1, email);

        return stmt.executeQuery();

    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}

public boolean returnBook(int rentalId) {
    try {
        String updateQuery = "UPDATE rents SET return_date = CURRENT_DATE, status = 'Returned' WHERE rent_id = ?";
        PreparedStatement stmt = getConnection().prepareStatement(updateQuery);
        stmt.setInt(1, rentalId);

        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/**
 * Get user ID by email.
 */
public int getUserIdByEmail(String email) {
    try {
        String query = "SELECT user_id FROM users WHERE email = ?";
        PreparedStatement stmt = getConnection().prepareStatement(query);
        stmt.setString(1, email);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                System.out.println("User ID found: " + userId);
                return userId;
            }
        }

        System.out.println("No user found for email: " + email);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}


/**
 * Fetch rented books for a given user ID.
 */
public ResultSet getRentedBooks(int userId) throws SQLException {
    String query = "SELECT rb.rent_id, rb.book_id, rb.status, b.title, b.author, b.image_url " +
                   "FROM rents rb " +
                   "JOIN books b ON rb.book_id = b.book_id " +
                   "WHERE rb.user_id = ? AND rb.status != 'returned'";
    PreparedStatement stmt = connection.prepareStatement(query);
    stmt.setInt(1, userId);
    return stmt.executeQuery();
}



/**
 * Increment the quantity of a book in the database.
 */
public boolean incrementBookQuantity(int bookId) {
    try {
        String query = "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?";
        PreparedStatement stmt = getConnection().prepareStatement(query);
        stmt.setInt(1, bookId);

        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    
}
/**
 * Update user details (email, name, and password).
 */
public boolean updateUserDetails(String newEmail, String newName, String newPassword, String oldEmail) {
    try {
        String query = "UPDATE users SET email = ?, name = ?, password_hash = SHA2(?, 256) WHERE email = ?";
        PreparedStatement stmt = getConnection().prepareStatement(query);

        stmt.setString(1, newEmail);
        stmt.setString(2, newName);
        stmt.setString(3, newPassword);
        stmt.setString(4, oldEmail);

        int rowsAffected = stmt.executeUpdate();

        return rowsAffected > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


// Necessary methods added for MyAccountPage functionality
public ResultSet getUserDetails(String email) throws SQLException {
    String query = "SELECT email, name, password_hash, role FROM users WHERE email = ?";

    PreparedStatement stmt = getConnection().prepareStatement(query);
    stmt.setString(1, email);

    return stmt.executeQuery();
}

public boolean updateUserEmail(String oldEmail, String newEmail) throws SQLException {
    if (isEmailTaken(newEmail)) {
        return false;  // Email already taken
    }

    String updateQuery = "UPDATE users SET email = ? WHERE email = ?";
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

        stmt.setString(1, newEmail);
        stmt.setString(2, oldEmail);

        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
    }
}

public boolean updateUserName(String email, String newName) throws SQLException {
    String updateQuery = "UPDATE users SET name = ? WHERE email = ?";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

        stmt.setString(1, newName);
        stmt.setString(2, email);

        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
    }
}

public boolean updateUserPassword(String email, String newPassword) throws SQLException {
    String updateQuery = "UPDATE users SET password_hash = SHA2(?, 256) WHERE email = ?";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

        stmt.setString(1, newPassword);
        stmt.setString(2, email);

        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
    }
}
public List<RentedBook> getUserRentedBooksSortedByDueDate(String userEmail) {
    List<RentedBook> rentedBooks = new ArrayList<>();
    String query = "SELECT r.rent_id, b.title, r.due_date " +
                   "FROM rents r " +
                   "JOIN users u ON r.user_id = u.user_id " +
                   "JOIN books b ON r.book_id = b.book_id " +
                   "WHERE u.email = ? and r.status <> 'Returned' " +
                   "ORDER BY r.due_date ASC";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, userEmail);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("rent_id");
            String title = rs.getString("title");
            
            Date dueDateSql = rs.getDate("due_date");  // Retrieve DATE column from database
            LocalDate dueDate = dueDateSql.toLocalDate();  // Convert to LocalDate
            LocalDateTime dueDateTime = dueDate.atStartOfDay();  // Combine with default time (00:00:00)

            rentedBooks.add(new RentedBook(id, title, dueDateTime));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return rentedBooks;
}

// Check if email already exists in the database
public boolean isEmailTaken(String email) throws SQLException {
    String query = "SELECT email FROM users WHERE email = ?";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, email);

        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    }
}
public boolean addReview(int userId, int bookId, int rating, String comments) {
    String query = "INSERT INTO reviews (user_id, book_id, rating, comment, review_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    
    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setInt(1, userId);
        stmt.setInt(2, bookId);
        stmt.setInt(3, rating);
        stmt.setString(4, comments != null ? comments : null);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("Review added successfully.");
        return rowsAffected > 0;
    } catch (SQLException e) {
        System.err.println("Failed to add review: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}


}
