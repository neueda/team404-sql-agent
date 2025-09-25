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
}
