package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Konfiguracja przyciskow glownego okna
 */
public class Controller {

    public Button entry;
    public Button exit;
    public BorderPane background1;

    /**
     * Funkcja za pomoca ktorej wchodzimy do systemu zarzadzania ksiegarnia
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void entry_to_system(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("dostepneKsiazki.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej wychodzimy z systemu zarzadzania ksiegarnia
     */
    @FXML
    public void exit_system() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}
