package project;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private int price;
    private int quantity;
    private String imageUrl;
    private double averageRating;

    // Constructor without averageRating (default value)
    public Book(int bookId, String title, String author, int price, int quantity, String imageUrl) {
        this(bookId, title, author, price, quantity, imageUrl, 0.0); // Default averageRating to 0.0
    }

    // Constructor with averageRating
    public Book(int bookId, String title, String author, int price, int quantity, String imageUrl, double averageRating) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * @param bookId the bookId to set
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
