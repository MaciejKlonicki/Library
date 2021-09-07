package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Konfiguracja glownego okna
 */
public class Main extends Application {

    /**
     * Funkcja za pomoca ktora deklaruje nam wyglad ekranu startowego
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Księgarnia");
        primaryStage.setScene(new Scene(root, 720, 480));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icons8-storytelling-48.png")));
        primaryStage.show();
    }

    /**
     * Funkcja za pomoca ktorej laczymy sie z serwerem
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Connection.connect();
        launch(args);
    }
}
