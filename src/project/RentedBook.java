package project;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RentedBook {
    private int id;
    private String title;
    private LocalDateTime dueDate;
    private BddComm bd;

    public RentedBook(int id, String title, LocalDateTime dueDate) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    @Override
    public String toString() {
        return title + " (Due: " + dueDate + ")";
    }
}
