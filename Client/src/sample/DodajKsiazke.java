package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.*;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static sample.Connection.sock;

/**
 * Okno pozwalajace dodawac ksiazki
 */
public class DodajKsiazke {
    @FXML
    private TextField book_name;
    @FXML
    private TextField book_author;
    @FXML
    private ChoiceBox book_type;
    @FXML
    private TextField book_price;
    @FXML
    private TextField book_quantity;
    @FXML
    private TextField book_location;


    /**
     * Funkcja za pomoca ktorej zostaly przypisane rodzaje ksiazek do ChoiceBox
     */
    @FXML
    public void initialize() {
        book_type.setItems(FXCollections.observableArrayList(
                "Fantastyka", "Kryminał", "Thriller", "Sensacja", "Romans", "Science Fiction", "Przygodowe", "Horror", "Powieść", "Historyczna", "Literatura piękna"));
        book_type.getSelectionModel().selectFirst();
    }

    /**
     * Funkcja za pomoca ktorej jest dodawana ksiazka do ksiegarni
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void book_confirm_add(ActionEvent event) throws IOException {

        //Obsluga logera
        Logger logger = Logger.getLogger("sample");
        try {
            FileInputStream properties = new FileInputStream("log.properties");
            LogManager.getLogManager().readConfiguration(properties);
            properties.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Problem z logerem" + e.getMessage());
        }


        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        //Sprawdzenie czy wszystkie pola sa wypelnione
            if (!book_name.getText().isEmpty() &&
                    !book_author.getText().isEmpty() &&
                    !book_type.getValue().equals(null) &&
                    !book_price.getText().isEmpty() &&
                    !book_quantity.getText().isEmpty() &&
                    !book_location.getText().isEmpty()) {
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Uzupelnij wszystkie pola");
                alert.showAndWait();
            }


        //Przypisywanie wartosci z pol do zmiennych wartosci dla danej ksiazki
        String book_name_value = book_name.getText();
        String book_author_value = book_author.getText();
        String book_type_value = book_type.getValue().toString();
        int book_price_value = Integer.parseInt(book_price.getText());
        int book_quantity_value = Integer.parseInt(book_quantity.getText());
        String book_location_value = book_location.getText();

        int id = generateUniqueId();
        //Tworzenie zapytania do bazy danych aby dodac ksiazke o podanych przez uzytkownika wartosciach
        String book_add_statement = String.format("INSERT INTO Ksiazki VALUES(%d,'%s', '%s', '%s', %d, %d, '%s')",
                id, book_name_value,
                book_author_value, book_type_value,
                book_price_value, book_quantity_value, book_location_value);
        outp.println(book_add_statement);
        outp.flush();

        //Tworzenie logow
        logger.info("Ksiazka zostala dodana do bazy o id: " + id);

        //Wyswietlanie komunikatu o poprawnie dodanej ksiazce wraz z jej numerem id
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Ksiązka została dodana o nr: " + id);
        alert.showAndWait();

        //Czyszczenie pol po dodaniu ksiazki
        book_name.setText("");
        book_author.setText("");
        book_price.setText("");
        book_quantity.setText("");
        book_location.setText("");
    }

    /**
     * Funkcja za pomoca ktorej generowany jest unikalny identyfikator danej ksiazki
     *
     * @return
     */
    public static int generateUniqueId() {
        UUID idOne = UUID.randomUUID();
        String str = "" + idOne;
        int uid = str.hashCode();
        String filterStr = "" + uid;
        str = filterStr.replaceAll("-", "");
        return Integer.parseInt(str);
    }

    /**
     * Funkcja za pomoca ktorej wracamy do glownego okna systemu zarzadzania ksiegarnia
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void book_add_back_to_system(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("dostepneKsiazki.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }
}

