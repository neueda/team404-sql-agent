import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Database {
    final static String jdbcURL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    final static String username = "team404";
    final static String password = "BrainNotFound";

    /*
     * Creates the 'movies' table in the database if it doesn't already exist.
     * Defines columns for film details and ensures 'Film' values are unique.
     */
    public void createTable() {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {
            System.out.println("Connection Successful");
            statement.execute(SqlQueries.CREATE_MOVIES_TABLE);
            System.out.println("Table 'movies' created.");

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    /*
     * Loads movie data from a CSV file into the 'movies' table.
     * Uses MERGE to insert new rows or update existing ones based on the 'Film' column.
     */

    public void loadCsv() {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {
            // Load films.csv from classpath
            InputStream inputStream = CommandLineApp.class.getClassLoader().getResourceAsStream("films.csv");
            if (inputStream == null) {
                throw new FileNotFoundException("films.csv not found in resources!");
            }
            // Copy it to a temporary file
            Path tempFile = Files.createTempFile("films", ".csv");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            // Convert to absolute path string (important for H2)
            String csvPath = tempFile.toAbsolutePath().toString();
            // Format the SQL query
            String insertQuery = String.format(SqlQueries.MERGE_MOVIES_FROM_CSV, csvPath);
            // Optional debug
            System.out.println("Executing MERGE from CSV path: " + csvPath);
            // Execute the SQL
            statement.execute(insertQuery);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     * Counts and prints the total number of rows in the 'movies' table.
     * Executes a SELECT Count query print result.
     */
    public void printCount() {
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(SqlQueries.COUNT_MOVIES);
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Loaded " + count + " rows.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    /*
     * Validates user input.
     * Checks if it contains "select *" (case-insensitive),
     * and does not contain ";", "<", or ">".
     */
    public static boolean validateInput(String userInput) {
        if (userInput.toLowerCase().startsWith("select * from") && !userInput.contains(";") && !userInput.contains("<") && !userInput.contains(">")) {
            System.out.println("Acceptable syntax received");
            return true;
        } else {
            System.out.println("The input is Invalid please try again");
            return false;
        }
    }

    /*
     * validate user input to make sure right table name
     */
    public static boolean validateInputTable(String userInput) {
        if (userInput.toLowerCase().contains("from movies")) {
            System.out.println("Valid table received");
            return true;
        } else {
            System.out.println("The table is Invalid please try again");
            return false;
        }
    }

    /**
     * Executes the given SQL query and calls printingResultSet().
     */
    public static Map<String, String> executeQuery(String validInput) {
        Map<String, String> results = new LinkedHashMap<>();
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(validInput);
            results = processResultset(rs);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    private static Map<String, String> processResultset(ResultSet rs) {
        Map<String, String> results = new LinkedHashMap<>();
        int i = 0;
        try {
            while (rs.next()) {
                i++;
                String film = rs.getString("Film");
                String genre = rs.getString("Genre");
                String leadStudio = rs.getString("Lead_Studio");
                int audienceScore = rs.getInt("Audience_Score_pc");
                double profitability = rs.getDouble("Profitability");
                int rottenTomatoes = rs.getInt("Rotten_Tomatoes_pc");
                double worldwideGross = rs.getDouble("Worldwide_Gross");
                int year = rs.getInt("Year");
                String filmDetails = String.format("{\"Name\":\"%s\",\"Genre\":\"%s\",\"Lead Studio\":\"%s\",\"Audience Score\":\"%d%%\",\"Profitability\":\"%.2f\",\"Rotten Tomatoes\":\"%d%%\",\"Worldwide Gross\":\"$%.2f\",\"Year\":\"%d\"}", film, genre, leadStudio, audienceScore, profitability, rottenTomatoes, worldwideGross, year);
                String index = String.format("\"%s\"", i);
                results.put(index, filmDetails);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}
