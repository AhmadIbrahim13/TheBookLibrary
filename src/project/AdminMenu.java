package project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminMenu {
    private Stage stage;

    public AdminMenu(Stage stage) {
        this.stage = stage;
    }

    public void show(Stage stage1) {
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(20); 
        buttonGrid.setVgap(20); 
        buttonGrid.setAlignment(Pos.CENTER);
        Button manageUsers = new Button("Manage Users");
        Button manageBooks = new Button("Manage Books");
        Button printInvoices = new Button("Print Invoices");
        Button history = new Button("History");
        Button logout = new Button("Logout");
        buttonGrid.add(manageUsers, 0, 0); 
        buttonGrid.add(manageBooks, 1, 0); 
        buttonGrid.add(printInvoices, 0, 1);  
        buttonGrid.add(history, 1, 1); 

        manageUsers.setOnAction(event -> new ManageUsersPage(stage).show());
        manageBooks.setOnAction(event -> new ManageBooksPage(stage).show(stage));
        printInvoices.setOnAction(event -> new PrintInvoices(stage).show(stage));
        history.setOnAction(event -> new History(stage).show(stage));

        LogoutHandler logoutHandler = new LogoutHandler(stage);
        logout.setOnAction(event -> logoutHandler.logout());

        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #5e3d2f;");

        VBox root = new VBox(20, title, buttonGrid, logout);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/resources/admin.css").toExternalForm());
        stage.setScene(scene);

        stage.show();
    }
}