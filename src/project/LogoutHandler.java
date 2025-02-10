package project;

import javafx.stage.Stage;

public class LogoutHandler {

    private final Stage stage;

    public LogoutHandler(Stage stage) {
        this.stage = stage;
    }

    public void logout() {
        System.out.println("User has been logged out.");
        
        // Redirect to the login page
        new LoginPage().start(stage);
    }
}
