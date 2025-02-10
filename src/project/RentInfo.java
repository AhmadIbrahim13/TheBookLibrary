package project;

import java.time.LocalDate;

public class RentInfo {
    private String username;
    private String bookName;
    private LocalDate rentedDate;
    private LocalDate dueDate;
    private String status;

    public RentInfo(String username, String bookName, LocalDate rentedDate, LocalDate dueDate, String status) {
        this.username = username;
        this.bookName = bookName;
        this.rentedDate = rentedDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public LocalDate getRentedDate() {
        return rentedDate;
    }

    public void setRentedDate(LocalDate rentedDate) {
        this.rentedDate = rentedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
