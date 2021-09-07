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
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static sample.Connection.sock;

/**
 * Okno pozwalajace sprzedawac ksiazki
 */
public class SprzedajKsiazke extends Thread {
    @FXML
    private Button back_sell;
    @FXML
    private TextField quantity_sell;
    @FXML
    private TextField book_search_sell;
    @FXML
    private ListView book_list_sell;

    static ObservableList<String> books = FXCollections.observableArrayList();

    static boolean isRunning;


    /**
     * Funkcja za pomoca ktorej sprzedajemy ksiazki wybrane z listy
     *
     * @param event
     * @throws IOException
     */
    public void sell_book_confirmation(ActionEvent event) throws IOException {

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


        //Odczytanie wiersza z tabeli Ksiazki aby odczytac informacje
        String row = book_list_sell.getSelectionModel().getSelectedItem().toString();
        System.out.println(row);
        //ID ksiazki
        String book_id_string = row.split(" ")[0];
        int book_id = Integer.parseInt(book_id_string);

        //Zamiana Stringow na int z ilosci i ceny
        String book_price_sell = row.substring(row.lastIndexOf(" ") + 1);
        int price = Integer.parseInt(book_price_sell.trim().replaceAll("\n ", ""));
        int quantity_sell_books = Integer.parseInt(quantity_sell.getText().trim().replaceAll("\n ", ""));
        //Obliczanie kosztu calkowitego zakupu
        int summary_book = price * quantity_sell_books;
        System.out.println(summary_book);

        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        //Tworzenie zapytania do bazy danych wraz z informacjami o rachunku za sprzedane ksiazki
        int id = generateUniqueId();
        String book_sell_statement = String.format("INSERT INTO Sprzedaz VALUES(%d, %d, %d, TO_DATE(SYSDATE,'YYYY/MM/DD HH24::MI:SS'), %d)",
                id, book_id, quantity_sell_books, summary_book);
        outp.println(book_sell_statement);
        outp.flush();

        //Tworzenie logow
        logger.info("Zostal wygenerowany rachunek " + id);


        //Komunikat z informacja o poprawnie dodanym zamowieniu, wraz z jego id oraz kosztem calkowitego zakupu
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Dodano zamowienie, nr: " + id + " oraz cena za zamowienie to: " + summary_book);
        alert.showAndWait();

    }

    /**
     * Funkcja za pomoca ktorej generowany jest unikalny identyfikator rachunku sprzedazy
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
     * Funkcja za pomoca ktorej wyswietlamy liste wraz z ksiazkami dostepnymi w sprzedazy
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
        book_search_sell.textProperty().addListener(((observable, oldValue, newValue) -> {
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
                    book_list_sell.setItems(filteredList);
                    book_list_sell.refresh();
                }
            }
        });

        //Tworzenie watku aby odczytac dane z serwera
        SprzedajKsiazke thread_books_sell = new SprzedajKsiazke();
        thread_books_sell.start();


    }

    /**
     * Funkcja za pomoca ktorej odswiezamy liste ksiazek
     *
     * @throws IOException
     */
    @FXML
    public void sell_refresh() throws IOException {
        books.clear();
        PrintWriter outp;
        outp = new PrintWriter(sock.getOutputStream());
        outp.println("Ksiazki");
        outp.flush();
    }

    /**
     * Funkcja za pomoca ktorej przechodzimy do okna "DostepneKsiazki"
     *
     * @param event
     * @throws IOException
     */
    public void back_to_book_list(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("dostepneKsiazki.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        isRunning = false;
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej przechodzimy do okna "HistoriaSprzedazy"
     *
     * @param event
     * @throws IOException
     */
    @FXML
    public void sell_history(ActionEvent event) throws IOException {
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("historiaSprzedazy.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.hide();
        app_stage.setScene(home_page_scene);
        app_stage.show();
    }

    /**
     * Funkcja za pomoca ktorej tworzymy watek aby odczytywac dane z serwera
     */
    @Override
    public void run() {
        System.out.println("dziala ");
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
                        System.out.println("dodaje: " + books);
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
