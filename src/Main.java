import java.sql.*;
import java.util.Scanner;
public class Main {
   final static String jdbcURL = "jdbc:h2:file:C:\\Zinkworks_SQL_Agent_Resources\\Sql_Agent_DB;AUTO_SERVER=TRUE";
   final static String username = "team404";
   final static String password = "BrainNotFound";
    public static void main(String[] args)  {

        createTable();
        loadCsv();
        printCount();
        promptUser();
    }
    /*
     *promptUser method initiates a while loop to continuously prompt user to enter a SQL query, or "exit" to exit the loop
     * and close the program.
     * This method uses a try-with-resources block for the scanner to automatically close connection
     */
    private static void promptUser() {
        String userInput;
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter your SQL query or 'exit' to exit: ");
                userInput = scan.nextLine().trim();
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("App closing, goodbye!");
                    break;
                } else {
                    System.out.println("SQL Query: " + userInput);
                    if (validateInput(userInput)) {
                        System.out.println("YOUR KEYWORDS ARE VALID");
                        executeQuery(userInput);
                    } else {
                        System.out.println("YOUR KEYWORDS ARE INVALID");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
/*
 * Creates the 'movies' table in the database if it doesn't already exist.
 * Defines columns for film details and ensures 'Film' values are unique.
 */

    private static void createTable()  {
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            System.out.println("Connection Successful");
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

        } catch (SQLException e){
            e.printStackTrace();
        }

    }

/*
 * Loads movie data from a CSV file into the 'movies' table.
 * Uses MERGE to insert new rows or update existing ones based on the 'Film' column.
 */

    private static void loadCsv(){
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            String csv_path = "C:\\Zinkworks_SQL_Agent_Resources\\films.csv";
            String insertQuery = String.format("""
            MERGE INTO movies (Film,Genre,Lead_Studio,Audience_Score_pc,Profitability,Rotten_Tomatoes_pc,Worldwide_Gross,"Year") KEY(Film)
            SELECT * FROM CSVREAD('%s')
            """, csv_path);
            statement.execute(insertQuery);
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

/*
 * Counts and prints the total number of rows in the 'movies' table.
 * Executes a SELECT Count query print result.
 */
    private static void printCount(){
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery("Select * From Movies");
            int count = 0;
            while (rs.next()) {
                count ++;
            }
            System.out.println("Loaded " + count +" rows.");
        } catch (SQLException e){
            e.printStackTrace();
        }
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

/**
 * Executes the given SQL query and calls printingResultSet().
 */
    private static void executeQuery(String validInput){
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery(validInput);
            printingResultSet(rs);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

/**
 * Prints all rows from the given ResultSet with formatted output.
 */
    private static void printingResultSet(ResultSet rs)  {
        int index = 0;
       try {
               while(rs.next()){
                   index++;
                   String film = rs.getString("Film");
                   String genre = rs.getString("Genre");
                   String leadStudio = rs.getString("Lead_Studio");
                   int audienceScore = rs.getInt("Audience_Score_pc");
                   double profitability = rs.getDouble("Profitability");
                   int rottenTomatoes = rs.getInt("Rotten_Tomatoes_pc");
                   double worldwideGross = rs.getDouble("Worldwide_Gross");
                   int year = rs.getInt("Year");
                   System.out.printf(
                           "NUMBER %d Film Details: | Name: %s | Genre: %s | Lead Studio: %s | Audience Score: %d%% | Profitability: %.2f | Rotten Tomatoes: %d%% | Worldwide Gross: $%.2f | Year: %d%n",
                           index, film, genre, leadStudio, audienceScore, profitability, rottenTomatoes, worldwideGross, year
                   );
               }
           System.out.println("FINISHED PRINTING");
       }
       catch (SQLException e){
           System.out.println(e.getMessage());
       }
    }
}


