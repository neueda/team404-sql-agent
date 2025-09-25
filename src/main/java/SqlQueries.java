public class SqlQueries {
    /**
     * Contains SQL queries for managing the 'movies' table.
     * Includes table creation, merging data from a CSV file, and counting rows.
     */

    public static final String CREATE_MOVIES_TABLE = """
        CREATE TABLE IF NOT EXISTS movies (
            Film VARCHAR(100) UNIQUE,
            Genre VARCHAR(50),
            Lead_Studio VARCHAR(50),
            Audience_Score_pc INT,
            Profitability DOUBLE,
            Rotten_Tomatoes_pc INT,
            Worldwide_Gross DOUBLE,
            `Year` INT
        )
    """;

    public static final String MERGE_MOVIES_FROM_CSV = """
        MERGE INTO movies (Film, Genre, Lead_Studio, Audience_Score_pc, Profitability, Rotten_Tomatoes_pc, Worldwide_Gross, `Year`) KEY(Film)
        SELECT * FROM CSVREAD('%s');
    """;

    public static final String COUNT_MOVIES = "SELECT COUNT(*) FROM movies";

}

