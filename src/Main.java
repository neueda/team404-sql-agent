import java.sql.*;
import java.util.Scanner;
//helooooooodasdsa
public class Main {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:h2:file:C:\\Zinkworks_SQL_Agent_Resources\\Sql_Agent_DB;AUTO_SERVER=TRUE";
        String username = "team404";
        String password = "BrainNotFound";
        connection(jdbcURL, username, password);
        promptUser();
    }
    /*
     *promptUser method initiates a while loop to continuously prompt user to enter a SQL query, or "exit" to exit the loop
     * and close the program.
     * This method uses a try-with-resources block for the scanner to automatically close connection
     */
    private static void promptUser(){
        String userInput;
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter your SQL query or 'exit' to exit: ");
                userInput = scan.nextLine().trim();

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("App closing, goodbye!");
                    break;
                }
                else {
                    System.out.println("SQL Query: " + userInput);
                    if (validateInput(userInput)){
                        System.out.println("YOUR KEYWORDS ARE VALID");
                    }else {
                        System.out.println("YOUR KEYWORDS ARE INVALID");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     *Establishes a JDBC connection to the specified database and prints a confirmation
     * message if the connection is successful.
     * This method uses a try-with-resources block to automatically close connection
     */

    private static void connection(String jdbcURL,String username,String password){

        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            System.out.println("Connection Successful");

            createTable(statement);
            loadCsv(statement);
            printCount(statement);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

/*
 * Creates the 'movies' table in the database if it doesn't already exist.
 * Defines columns for film details and ensures 'Film' values are unique.
 */

    private static void createTable(Statement statement) throws SQLException {

        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS movies (
                Film VARCHAR(100) UNIQUE,
                Genre VARCHAR(50),
                Lead_Studio VARCHAR(50),
                Audience_Score_pc INT,
                Profitability DOUBLE,
                Rotten_Tomatoes_pc INT,
                Worldwide_Gross DOUBLE,
                "Year" INT
            )
            """;
        statement.execute(createTableQuery);
        System.out.println("Table 'movies' created.");
    }

/*
 * Loads movie data from a CSV file into the 'movies' table.
 * Uses MERGE to insert new rows or update existing ones based on the 'Film' column.
 */

    private static void loadCsv(Statement statement) throws SQLException {
        String csv_path = "C:\\Zinkworks_SQL_Agent_Resources\\films.csv";
        String insertQuery = String.format("""
            MERGE INTO movies (Film,Genre,Lead_Studio,Audience_Score_pc,Profitability,Rotten_Tomatoes_pc,Worldwide_Gross,"Year") KEY(Film)
            SELECT * FROM CSVREAD('%s')
            """, csv_path);

        statement.execute(insertQuery);
    }

/*
 * Counts and prints the total number of rows in the 'movies' table.
 * Executes a SELECT query and iterates through the result set to determine the count.
 */
    private static void printCount(Statement statement) throws SQLException {
        ResultSet rs = statement.executeQuery("Select * From Movies");
        int count = 0;
        while (rs.next()) {
            count ++;
        }
        System.out.println("Loaded " + count +" rows.");
    }

/*
 * Validates user input.
 * Checks if it contains "select *" (case-insensitive),
 * and does not contain ";", "<", or ">".
 */
    private static boolean validateInput(String userInput){

   if(userInput.toLowerCase().contains("select * from") && !userInput.contains(";") && !userInput.contains("<") && !userInput.contains(">")){
       return true;
   }else {
       return false;
   }
    }
}
