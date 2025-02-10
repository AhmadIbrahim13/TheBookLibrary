package project;

import javafx.beans.property.*;
import java.time.LocalDate;

public class InvoiceItem {
    private final StringProperty title;
    private final DoubleProperty price;
    private final ObjectProperty<LocalDate> rentedDate;
    private final ObjectProperty<LocalDate> dueDate;

    // Constructor with all fields
    public InvoiceItem(String title, double price, LocalDate rentedDate, LocalDate dueDate) {
        this.title = new SimpleStringProperty(title);
        this.price = new SimpleDoubleProperty(price);
        this.rentedDate = new SimpleObjectProperty<>(rentedDate);
        this.dueDate = new SimpleObjectProperty<>(dueDate); // Properly initialize dueDate
    }

    // Property methods for binding in TableView
    public StringProperty titleProperty() {
        return title;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public ObjectProperty<LocalDate> rentedDateProperty() {
        return rentedDate;
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }

    // Getter methods for accessing values
    public String getTitle() {
        return title.get();
    }

    public double getPrice() {
        return price.get();
    }

    public LocalDate getRentedDate() {
        return rentedDate.get();
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    // Setter methods for updating values
    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public void setRentedDate(LocalDate rentedDate) {
        this.rentedDate.set(rentedDate);
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }
}
