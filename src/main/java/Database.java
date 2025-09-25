import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    final static String jdbcURL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    final static String username = "team404";
    final static String password = "BrainNotFound";

    /*
     * Creates the 'movies' table in the database if it doesn't already exist.
     * Defines columns for film details and ensures 'Film' values are unique.
     */
    public void createTable()  {
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            System.out.println("Connection Successful");
            statement.execute(SqlQueries.CREATE_MOVIES_TABLE);
            System.out.println("Table 'movies' created.");

        } catch (SQLException e){
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

}
