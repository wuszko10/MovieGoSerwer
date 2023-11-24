package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import connection.Connect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.*;
import java.util.Base64;
import java.util.List;

@SpringBootApplication
@RestController
public class MyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApiApplication.class, args);
    }

    @GetMapping("/movies")
    public ResponseEntity<String> getMovies() {
        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        if (connection != null) {
            try {
                // Create SQL query
                String query = "SELECT f.id_filmu, f.tytul, f.czas_trwania, f.ocena, f.opis, f.okladka, f.cena, g.nazwa_gatunku, s.data, s.pora_emisji, s.id_seansu " +
                        "FROM film f " +
                        "INNER JOIN gatunek g ON f.id_gatunku = g.id_gatunku " +
                        "INNER JOIN seanse s ON f.id_filmu = s.id_filmu";

                // Execute the query
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                // Process query results
                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode moviesArray = objectMapper.createArrayNode();
                while (resultSet.next()) {
                    // Get values from query result columns
                    int id_filmu = resultSet.getInt("id_filmu");
                    String tytul = resultSet.getString("tytul");
                    int czas_trwania = resultSet.getInt("czas_trwania");
                    double ocena = resultSet.getDouble("ocena");
                    String opis = resultSet.getString("opis");
                    Blob okladka = resultSet.getBlob("okladka");
                    double cena = resultSet.getDouble("cena");

                    String nazwa_gatunku = resultSet.getString("nazwa_gatunku");

                    String data = resultSet.getString("data");
                    String pora_emisji = resultSet.getString("pora_emisji");
                    int id_seansu = resultSet.getInt("id_seansu");


                    byte[] imageBytes = okladka.getBytes(1, (int) okladka.length()); // Read the Blob data as a byte array
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes); // Convert the byte array to a Base64 encoded string

                    // Append movie details to the response string
                    ObjectNode movieObject = objectMapper.createObjectNode();
                    movieObject.put("id_filmu", id_filmu);
                    movieObject.put("tytul", tytul);
                    movieObject.put("czas_trwania", czas_trwania);
                    movieObject.put("ocena", ocena);
                    movieObject.put("opis", opis);
                    movieObject.put("okladka", base64Image);
                    movieObject.put("cena", cena);

                    movieObject.put("nazwa_gatunku", nazwa_gatunku);

                    movieObject.put("data", data);
                    movieObject.put("pora_emisji", pora_emisji);
                    movieObject.put("id_seansu", id_seansu);

                    moviesArray.add(movieObject);
                }

                // Close ResultSet and Statement objects
                resultSet.close();
                statement.close();

                // Convert the moviesArray to a JSON string
                String moviesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moviesArray);

                // Set the response headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Return the response with the JSON string and headers
                return new ResponseEntity<>(moviesJson, headers, HttpStatus.OK);

            } catch (SQLException | IOException e) {
                e.printStackTrace();
            } finally {
                connect.close(); // Close the connection
            }
        }

        // Return a response indicating no movies found
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/tickets")
    public String getTickets(@RequestParam(name = "id_uzyt") int id_uzyt) {
        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        String moviesJson = "No movies found";
        if (connection != null) {
            try {
                // Create SQL query
                String query = "SELECT rezerwacje.nr_rezerwacji, film.tytul, uzytkownicy.login, COUNT(bilet.id_rezer) AS ilosc_biletow, GROUP_CONCAT(CONCAT(miejsca.rzad, ':', miejsca.fotel) SEPARATOR ' | ') AS miejsca, SUM(bilet.cena) AS cena, CONCAT(seanse.data,' ', seanse.pora_emisji) AS data FROM bilet " +
                        "INNER JOIN rezerwacje ON bilet.id_rezer = rezerwacje.id_rezer " +
                        "INNER JOIN uzytkownicy ON rezerwacje.id_uzyt = uzytkownicy.id_uzyt " +
                        "INNER JOIN miejsca ON bilet.id_miejsca = miejsca.id_miejsca " +
                        "INNER JOIN seanse ON bilet.id_seansu = seanse.id_seansu " +
                        "INNER JOIN film ON seanse.id_filmu = film.id_filmu " +
                        "WHERE uzytkownicy.id_uzyt = ? " +
                        "GROUP BY bilet.id_rezer";

                // Execute the query
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, id_uzyt);
                ResultSet resultSet = statement.executeQuery();

                // Process query results
                moviesJson = "";

                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode moviesArray = objectMapper.createArrayNode();
                while (resultSet.next()) {
                    // Get values from query result columns
                    String reservationNumber = resultSet.getString("nr_rezerwacji");
                    String movieTitle = resultSet.getString("tytul");
                    String userLogin = resultSet.getString("login");
                    int reservationId = resultSet.getInt("ilosc_biletow");
                    String seatDescription = resultSet.getString("miejsca");
                    double orderValue = resultSet.getDouble("cena");
                    String dateReservation = resultSet.getString("data");

                    // Append movie details to the response string
                    ObjectNode movieObject = objectMapper.createObjectNode();
                    movieObject.put("nr_rezerwacji", reservationNumber);
                    movieObject.put("tytul", movieTitle);
                    movieObject.put("login", userLogin);
                    movieObject.put("ilosc_biletow", reservationId);
                    movieObject.put("miejsca", seatDescription);
                    movieObject.put("cena", orderValue);
                    movieObject.put("data", dateReservation);

                    moviesArray.add(movieObject);
                }

                moviesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moviesArray);

                // Close ResultSet and Statement objects
                resultSet.close();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                connect.close(); // Close the connection
            }
        }
        return moviesJson;
    }

    @GetMapping("/seats")
    public String getSeats(){
        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        String seatsJson = "No movies found";
        if (connection != null) {
            try {
                // Create SQL query
                String query = "SELECT id_miejsca, rzad, fotel FROM miejsca";

                // Execute the query
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                // Process query results
                seatsJson = "";

                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode seatsArray = objectMapper.createArrayNode();
                while (resultSet.next()) {
                    // Get values from query result columns
                    int seatId = resultSet.getInt("id_miejsca");
                    int row = resultSet.getInt("rzad");
                    int armchair = resultSet.getInt("fotel");

                    // Append movie details to the response string
                    ObjectNode movieObject = objectMapper.createObjectNode();
                    movieObject.put("id_miejsca", seatId);
                    movieObject.put("rzad", row);
                    movieObject.put("fotel", armchair);

                    seatsArray.add(movieObject);
                }

                seatsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(seatsArray);

                // Close ResultSet and Statement objects
                resultSet.close();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                connect.close(); // Close the connection
            }
        }
        return seatsJson;
    }

    @GetMapping("/seats/reserved")
    public String getReservedSeats(@RequestParam(name = "id_seansu") int id_seansu){

        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        String seatsJson = "No seats found";
        if (connection != null) {
            try {

                String query = "SELECT miejsca.rzad, miejsca.fotel FROM bilet INNER JOIN miejsca ON bilet.id_miejsca=miejsca.id_miejsca WHERE bilet.id_seansu = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, id_seansu);
                ResultSet resultSet = statement.executeQuery();

                seatsJson = "";

                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode seatsArray = objectMapper.createArrayNode();
                while (resultSet.next()) {
                    int row = resultSet.getInt("rzad");
                    int col = resultSet.getInt("fotel");

                    ObjectNode movieObject = objectMapper.createObjectNode();
                    movieObject.put("rzad", row);
                    movieObject.put("fotel", col);

                    seatsArray.add(movieObject);
                }

                seatsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(seatsArray);

                // Close ResultSet and Statement objects
                resultSet.close();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                connect.close(); // Close the connection
            }
        }
        return seatsJson;
    }



    @PostMapping("/book")
    public ResponseEntity<BookResponse> bookTickets(@RequestBody BookResponse bookResponse) {
        try {
            int userId = bookResponse.getUserId();
            double price = bookResponse.getPrice();
            List<Ticket> ticketList = bookResponse.getTicketList();

            Connect connect = new Connect();
            Connection connection = connect.getConnection();
            if (connection != null) {
                try {
                    connection.setAutoCommit(false); // Enable manual transaction management

                    String lastOrderQuery = "SELECT nr_rezerwacji FROM rezerwacje ORDER BY nr_rezerwacji DESC LIMIT 1";
                    PreparedStatement lastOrderStatement = connection.prepareStatement(lastOrderQuery);
                    ResultSet lastOrderResult = lastOrderStatement.executeQuery();
                    String orderNumber;

                    if (lastOrderResult.next()) {
                        String lastOrderNumber = lastOrderResult.getString("nr_rezerwacji");
                        int orderIndex = Integer.parseInt(lastOrderNumber.substring(1)) + 1;
                        orderNumber = "K" + String.format("%04d", orderIndex);
                    } else {
                        orderNumber = "K0001";
                    }

                    String insertOrderQuery = "INSERT INTO rezerwacje (nr_rezerwacji, id_uzyt, kwota_rezer) VALUES (?, ?, ?)";
                    PreparedStatement insertOrderStatement = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
                    insertOrderStatement.setString(1, orderNumber);
                    insertOrderStatement.setInt(2, userId);
                    insertOrderStatement.setDouble(3, price);
                    insertOrderStatement.executeUpdate();
                    ResultSet generatedKeys = insertOrderStatement.getGeneratedKeys();
                    int orderId;

                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);

                        // Insert tickets
                        for (Ticket ticket : ticketList) {
                            String insertTicketQuery = "INSERT INTO bilet (id_biletu, id_rezer, id_seansu, id_miejsca, cena) VALUES (NULL, ?, ?, ?, ?)";
                            PreparedStatement insertTicketStatement = connection.prepareStatement(insertTicketQuery);
                            insertTicketStatement.setInt(1, orderId);
                            insertTicketStatement.setInt(2, ticket.getId_seansu());
                            insertTicketStatement.setInt(3, ticket.getId_miejsca());
                            insertTicketStatement.setDouble(4, ticket.getCena());
                            insertTicketStatement.executeUpdate();
                            insertTicketStatement.close();
                        }

                        connection.commit(); // Commit the transaction if the update and insertion were successful
                        System.out.println("Tickets booked successfully. Order ID: " + orderId);
                        return ResponseEntity.ok(bookResponse);
                    } else {
                        connection.rollback(); // Rollback the transaction if the insertion failed
                        System.out.println("Failed to book tickets");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    connection.rollback(); // Rollback the transaction in case of SQL error
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } finally {
                    connect.close(); // Close the connection to the database
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping("/login")
    public String checkLogin(@RequestParam(name = "login") String login, @RequestParam(name = "password") String password){
        String logSucces = "false";
        int count = 0;
        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        if (connection != null) {
            try {
                // Tworzenie zapytania SQL
                String query = "SELECT id_uzyt FROM uzytkownicy WHERE login = ? AND haslo = ?";

                PreparedStatement statement = connection.prepareStatement(query);

                // Ustawienie wartości parametrów
                statement.setString(1, login);
                statement.setString(2, password);

                // Wykonanie zapytania
                ResultSet resultSet = statement.executeQuery();


                // Przetwarzanie wyników zapytania
                while(resultSet.next()){
                    count++;
                }
                if(count>0){
                    logSucces="true";
                }
                // Zamknięcie obiektów ResultSet i Statement
                resultSet.close();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connect.close(); // Zamknięcie połączenia
            }

        }
        return logSucces;
    }

    @GetMapping("/users/{login}")
    public ResponseEntity<String> getUserByUsername(@PathVariable("login") String login) {
        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        if (connection != null) {
            try {
                // Create SQL query
                String query = "SELECT * FROM uzytkownicy WHERE login = ?";

                // Execute the query
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, login);
                ResultSet resultSet = statement.executeQuery();

                // Process query result
                if (resultSet.next()) {
                    // Get values from query result columns
                    int id = resultSet.getInt("id_uzyt");
                    String name = resultSet.getString("imie");
                    String surname = resultSet.getString("nazwisko");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("haslo");
                    String address = resultSet.getString("adres");
                    String birthdate = resultSet.getString("data_ur");
                    int number = resultSet.getInt("numer_tel");

                    // Create a JSON object for the user
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode userObject = objectMapper.createObjectNode();
                    userObject.put("id_uzyt", id);
                    userObject.put("login", login);
                    userObject.put("imie", name);
                    userObject.put("nazwisko", surname);
                    userObject.put("email", email);
                    userObject.put("haslo", password);
                    userObject.put("adres", address);
                    userObject.put("data_ur", birthdate);
                    userObject.put("numer_tel", number);

                    // Convert the user object to a JSON string
                    String userJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userObject);

                    // Set the response headers
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    // Return the response with the JSON string and headers
                    return new ResponseEntity<>(userJson, headers, HttpStatus.OK);
                }

                // Close ResultSet and Statement objects
                resultSet.close();
                statement.close();
            } catch (SQLException | JsonProcessingException e) {
                e.printStackTrace();
            } finally {
                connect.close(); // Close the connection
            }
        }

        // Return a response indicating no user found
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> addUser(@RequestBody RegistrationResponse registrationResponse) {

        String login = registrationResponse.getLogin();
        String password = registrationResponse.getPassword();
        String email = registrationResponse.getEmail();

        Connect connect = new Connect();
        Connection connection = connect.getConnection();
        if (connection != null) {
            try {
                connection.setAutoCommit(false); // enable manual transaction management

                // Create SQL query
                String query = "SELECT id_uzyt FROM uzytkownicy WHERE login = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, login);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return ResponseEntity.ok(new RegistrationResponse("User with the given login already exists"));
                } else {
                    // Create SQL query
                    query = "INSERT INTO uzytkownicy (login, haslo, email) VALUES (?, ?, ?)";

                    // Prepare SQL statement with parameters
                    PreparedStatement statement2 = connection.prepareStatement(query);
                    statement2.setString(1, login);
                    statement2.setString(2, password);
                    statement2.setString(3, email);

                    // Execute SQL statement
                    int rowsAffected = statement2.executeUpdate();
                    if (rowsAffected == 1) { // If exactly one row was inserted
                        connection.commit(); // commit the transaction
                        return ResponseEntity.ok(new RegistrationResponse("User registered successfully"));
                    } else {
                        connection.rollback(); // rollback the transaction
                        return ResponseEntity.ok(new RegistrationResponse("Failed to register user"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    connection.rollback(); // rollback the transaction in case of SQL error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return ResponseEntity.ok(new RegistrationResponse("An error occurred during registration"));
            } finally {
                connect.close(); // close the database connection
            }
        }
        return null;
    }



}
