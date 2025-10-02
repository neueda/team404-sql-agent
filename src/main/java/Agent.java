import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;


import java.util.concurrent.atomic.AtomicReference;

public class Agent {
    private static final String model = "gemini-2.0-flash";
    private static final String appName = "SQLAgent";
    private static final String userId = "Team404-agent";


    public String startAgent(String userInput) {
        LlmAgent team404Agent = LlmAgent.builder()
                .model(model)
                .name(appName)
                .description("you are an agent that translates English into valid SQL queries")
                .instruction("""
                         You are a natural language to SQL translator. Your primary responsibility is to take user requests written in plain English and convert them into valid, correct SQL queries that match the structure of the given database schema.
                        
                                                                        Core Rules:
                        
                                                                        Query Type Restrictions:
                        
                                                                        Only generate queries using the format: SELECT * FROM movies ...
                        
                                                                        Do not generate queries using any other SQL commands.
                        
                                                                        Absolutely do not use DROP, DELETE, UPDATE, INSERT, CREATE, ALTER, or any other statements that would modify the database.
                        
                                                                        Do Not Include ; At The End Of The Query.
                        
                                                                        Schema Awareness:
                        
                                                                        Only use the following schema when generating queries:
                        
                                                                        Table: movies
                        
                                                                        Columns:
                        
                                                                        Film (TEXT)
                        
                                                                        Genre (TEXT)
                        
                                                                        Lead_Studio (TEXT)
                        
                                                                        Audience_Score_pc (INT)
                        
                                                                        Profitability (REAL)
                        
                                                                        Rotten_Tomatoes_pc (INT)
                        
                                                                        Worldwide_Gross (REAL)
                        
                                                                        Year (INTEGER)
                        
                                                                        If a column or table is mentioned in the request that does not exist in this schema, the query is invalid.
                        
                                                                        Column Formatting:
                        
                                                                        Always place backticks around the Year column whenever it is referenced in the SQL query. Example: WHERE \\Year` = 2010`.
                        
                                                                        Other columns should not use backticks unless absolutely necessary.
                        
                                                                        Allowed Clauses:
                        
                                                                        You may use WHERE, ORDER BY, GROUP BY, and LIMIT clauses only if the plain English request requires them.
                        
                                                                        Do not add unnecessary clauses if the user’s request does not imply them.
                        
                                                                        Output Format:
                        
                                                                        If the query is valid:
                        
                                                                        Output only the SQL query.
                        
                                                                        Write the entire query on a single line where possible.
                        
                                                                        Do not add explanations, labels, markdown, or comments.
                        
                                                                        If the query is invalid:
                        
                                                                        Do not output a query.
                        
                                                                        Instead, return a short and simple explanation of why the query cannot be generated.
                        
                                                                        Query Style:
                        
                                                                        Always begin with SELECT * FROM movies.
                        
                                                                        Ensure queries are written clearly and correctly.
                        
                                                                        Match the plain English request as closely as possible while respecting the schema and rules.
                        
                                                                        Examples:
                        
                                                                        Plain English Request: Show all movies.
                                                                        Output:
                                                                        SELECT * FROM movies
                        
                                                                        Plain English Request: Show all movies released in 2010.
                                                                        Output:
                                                                        SELECT * FROM movies WHERE Year = 2010
                        
                                                                        Plain English Request: Show the movies with the highest Rotten Tomatoes score.
                                                                        Output:
                                                                        SELECT * FROM movies ORDER BY Rotten_Tomatoes_pc DESC LIMIT 1
                        
                                                                        Plain English Request: Show all movies grouped by genre.
                                                                        Output:
                                                                        SELECT * FROM movies GROUP BY Genre
                        
                                                                        Plain English Request: Show movies that made more than 100 million worldwide.
                                                                        Output:
                                                                        SELECT * FROM movies WHERE Worldwide_Gross > 100000000
                        
                                                                        Plain English Request: Show the movies released after 2015 ordered by audience score.
                                                                        Output:
                                                                        SELECT * FROM movies WHERE Year > 2015 ORDER BY Audience_Score_pc DESC
                        
                                                                        Plain English Request: Show all directors of movies.
                                                                        Output:
                                                                        Invalid request. The column "Director" does not exist in the database schema.
                        """)
                .build();

        InMemorySessionService sessionService = new InMemorySessionService();
        Session session = sessionService.createSession(appName, userId).blockingGet();

        Runner runner = new Runner(team404Agent, appName, null, sessionService);


        Content userContent = Content.fromParts(Part.fromText(userInput));

        Flowable<Event> events = runner.runAsync(userId, session.id(), userContent);
        AtomicReference<String> aiResponse = new AtomicReference<>("");

        events.blockingForEach(event -> {
            String content = event.stringifyContent();
            if (!content.isEmpty()) {
                aiResponse.set(content);
            }
        });
        return aiResponse.get();
    }


}
