// Shaan Jalal and Stephen Kolluri
package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import view.Controller;

public class SongLib extends Application{
    @Override
    public void start(Stage primaryStage){
        try{
            FXMLLoader Daisy = new FXMLLoader();
            Daisy.setLocation(getClass().getResource("/view/Lib.fxml"));
            AnchorPane letsgo = (AnchorPane)Daisy.load();
            Controller controller = Daisy.getController();
			controller.start(primaryStage);
			Scene scene = new Scene(letsgo);

			primaryStage.setScene(scene);
			primaryStage.show();
        } catch(Exception Peach){
            Peach.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}