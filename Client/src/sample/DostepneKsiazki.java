package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static sample.Connection.sock;

/**
 * Glowne okno systemu, zawiera przeglad ksiazek jak i mozliwosc przejscia do reszty funkcjonalnosci systemu
 */
public class DostepneKsiazki extends Thread {
    @FXML
    private Button add;
    @FXML
    private Button sell;
    @FXML
    private Button back;
    static ObservableList<String> books = FXCollections.observableArrayList();
    static boolean isRunning;

    @FXML
    ListView book_list;

    @FXML
    TextField book_search;


    /**
     * Funkcja za pomoca ktorej wyswietlane sa ksiazki
     *
     * @throws IOException
     */
    @FXML
    public void initialize() throws IOException {
        books.clear();
        isRunning = true;
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println("Ksiazki");
        outp.flush();

        //Wyszukiwanie ksiazek
        FilteredList<String> filteredList = new FilteredList<>(books, data -> true);
        book_search.textProperty().addListener(((observable, oldValue, newValue) -> {
            filteredList.setPredicate(data -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return Boolean.parseBoolean(String.valueOf(data.contains(newValue)));
            });
        }));
        books.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                {
                    book_list.setItems(filteredList);
                    book_list.refresh();
                }
            }
        });
        //Tworzenie watku aby odczytac dane z serwera
        DostepneKsiazki thread_books = new DostepneKsiazki();
        thread_books.start();
    }


    /**
     * Funkcja za pomoca ktorej usuwamy ksiazki ktore zawieraja bledne informacje ktore zostaly przypisane przez uzytkownika
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void book_delete(ActionEvent event) throws IOException {

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

        //Odczytanie wiersza z tabeli Ksiazki aby odczytac informacje
        String row = book_list.getSelectionModel().getSelectedItem().toString();
        //Odczytywanie id ksiazki
        String book_id_string = row.split(" ")[0];
        long book_id = Long.parseLong(book_id_string);
        //Tworzenie zapytania do usuwania ksiazek do bazy danych
        String book_delete_statement = String.format("DELETE FROM Ksiazki WHERE id_ksiazki = %d",
                book_id);
        outp.println(book_delete_statement);
        outp.flush();

        //Tworzenie logow
        logger.info("Ksiazka zostala usunieta o nr: " + book_id);


        //Komunikat z informacja o poprawnie usunietej ksiazce wraz z jej id
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Książka została usunieta o nr: " + book_id);
        alert.showAndWait();
    }


    /**
     * Funkcja za pomoca ktorej przechodzimy do okna "DodajKsiazke"
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void book_add(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("dodajKsiazke.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        isRunning = false;
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej przechodzimy do okna "SprzedajKsiazke"
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void book_sell(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("sprzedajKsiazke.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        isRunning = false;
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej przechodzimy do glownego okna systemu
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void back_to_menu(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        isRunning = false;
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej odswiezamy stan ksiazek
     *
     * @throws IOException
     */
    @FXML
    public void books_refresh() throws IOException {
        books.clear();
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println("Ksiazki");
        outp.flush();
    }


    /**
     * Funkcja za pomoca ktorej tworzymy watek aby odczytywac dane z serwera
     */
    @Override
    public void run() {
        super.run();
        BufferedReader reader;
        String line;
        try {
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            while ((line = reader.readLine()) != null && isRunning) {
                line = line + "\n";
                String finalLine = line;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        books.add(finalLine);
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
