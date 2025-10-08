import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class Database {
    final static String jdbcURL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    final static String username = "team404";
    final static String password = "BrainNotFound";
    public String userInput;

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
     * and does not contain ";",
     */
    public static boolean validateInput(String userInput) {
        if (userInput.toLowerCase().replaceAll("\\s", "").startsWith("select*from") && !userInput.contains(";") && !userInput.toLowerCase().contains("join") ) {
            System.out.println("validateInput: Acceptable syntax received");
            return true;
        } else {
            System.out.println("validateInput: The input is Invalid please try again");
            return false;
        }
    }

    /*
     * validate user input to make sure right table name
     */
    public static boolean validateInputTable(String userInput) {
        if (userInput.toLowerCase().replaceAll("\\s", "").contains("frommovies")) {
            System.out.println("validateInputTable: Valid table received");

            //check if the column name is in the table column list
            ArrayList<String> columns = new ArrayList<String>();
            columns.add("film");
            columns.add("genre");
            columns.add("lead_studio");
            columns.add("audience_score_pc");
            columns.add("profitability");
            columns.add("rotten_tomatoes_pc");
            columns.add("worldwide_gross");
            columns.add("year");

            //we only need to check columns for "where =" and "order by" conditions
            if (userInput.toLowerCase().contains("where") || userInput.toLowerCase().contains("order by")) {
                boolean validCol = false;
                for (String header : columns) {
                    if (userInput.toLowerCase().contains(header)){
                        System.out.println("validateInputTable: Valid column name");
                        validCol = true;
                        break;
                    }
                }
                if (!validCol) {
                    System.out.println("validateInputTable: Column name does not exist");
                    return false;
                }
            }
            return true;
        } else {
            System.out.println("validateInputTable: The table is Invalid please try again");
            return false;
        }
    }

    /**
     * Executes the given SQL query and calls printingResultSet().
     */
    public static JSONArray executeQuery(String validInput) {
        JSONArray results = null;
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


    private static JSONArray processResultset(ResultSet rs) {
        JSONArray resultToJson = new JSONArray();
        try {
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("Film", rs.getString("Film"));
                row.put("Genre", rs.getString("Genre"));
                row.put("Lead_Studio", rs.getString("Lead_Studio"));
                row.put("Audience_Score_pc", rs.getInt("Audience_Score_pc"));
                row.put("Profitability", rs.getDouble("Profitability"));
                row.put("Rotten_Tomatoes_pc", rs.getInt("Rotten_Tomatoes_pc"));
                row.put("Worldwide_Gross", rs.getDouble("Worldwide_Gross"));
                row.put("Year", rs.getInt("Year"));

                resultToJson.put(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultToJson;
    }

}
