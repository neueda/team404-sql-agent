import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:h2:file:C:\\Zinkworks_SQL_Agent_Resources\\Sql_Agent_DB";
        String username = "team404";
        String password = "BrainNotFound";
        connection(jdbcURL,username,password);
    }
    private static void connection(String jdbcURL,String username,String password){
        /*
         *Establishes a JDBC connection to the specified database and prints a confirmation
         * message if the connection is successful.
         * This method uses a try-with-resources block to automatically close connection
         */
        try (Connection connection = DriverManager.getConnection(jdbcURL,username,password);
             Statement statement = connection.createStatement()){
            System.out.println("Connection Successful");

        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
