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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static sample.Connection.sock;

/**
 * Okno zawierajace informacje o historii zakupow
 */
public class HistoriaSprzedazy extends Thread {

    @FXML
    private Button save_history;
    @FXML
    private TextField history_search;
    @FXML
    private ListView sell_history_list;
    static ObservableList<String> history = FXCollections.observableArrayList();
    static boolean isRunning;


    /**
     * Funkcja za pomoca ktorej zapisujemy konkretny rachunek do pliku
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void save_history_to_file(ActionEvent event) throws IOException {

        Logger logger = Logger.getLogger("sample");
        try {
            FileInputStream properties = new FileInputStream("log.properties");
            LogManager.getLogManager().readConfiguration(properties);
            properties.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Problem z logerem" + e.getMessage());
        }

        //ID rachunku
        String row = sell_history_list.getSelectionModel().getSelectedItem().toString();
        String sell_id_string = row.split(" ")[0];

        //Zapis rachunku do pliku
        String plik = String.format("Rachunek%s.txt", sell_id_string);
        FileWriter text_file = new FileWriter(plik);
        text_file.write(row);
        text_file.close();

        //Tworzenie logow
        logger.info("Zostal zapisany rachunek do pliku o numerze" + sell_id_string);

        //Komunikat z informacja o poprawnie zapisanym rachunku do pliku
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Zapisano rachunek, nr: " + sell_id_string);
        alert.showAndWait();
    }

    /**
     * Funkcja za pomoca ktorej wyswietlamy historie rachunkow
     *
     * @throws IOException
     */
    @FXML
    public void initialize() throws IOException {
        history.clear();
        isRunning = true;
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println("Sprzedaz");
        outp.flush();

        //Wyszukiwanie ksiazek
        FilteredList<String> filteredList = new FilteredList<>(history, data -> true);
        history_search.textProperty().addListener(((observable, oldValue, newValue) -> {
            filteredList.setPredicate(data -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return Boolean.parseBoolean(String.valueOf(data.contains(newValue)));
            });
        }));
        history.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                {
                    sell_history_list.setItems(filteredList);
                    sell_history_list.refresh();
                }
            }

        });
        //Tworzenie watku aby odczytac dane z serwera
        HistoriaSprzedazy thread_history = new HistoriaSprzedazy();
        thread_history.start();
    }

    /**
     * Funkcja za pomoca ktorej odswiezamy liste rachunkow
     *
     * @throws IOException
     */
    @FXML
    public void history_refresh() throws IOException {
        history.clear();
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println("Sprzedaz");
        outp.flush();
    }

    /**
     * Funkcja za pomoca ktorej przechodzimy do okna "DostepneKsiazki"
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void back_to_sell(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("dostepneKsiazki.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        isRunning = false;
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej tworzymy watek aby odczytac dane z serwera
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
                        history.add(finalLine);
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
