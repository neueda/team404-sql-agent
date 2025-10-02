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
Your job is to translate English questions into SQL queries.

Rules:

1. Only use: SELECT * FROM Movies
2. Do not include ; at the end of the query
3. You may add:
   - WHERE
   - ORDER BY
   - LIMIT
   - BETWEEN
4. Do not use any other SQL commands
5. Never use: INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, or anything that modifies the database
6. Only one query is allowed (no multiple queries)
7. Always use the exact column names from the schema
8. Output must be plain SQL only
   - Do not wrap in markdown code fences
   - Do not include ```sql or ```
   - Do not add explanations, comments, or extra text
   - Return only the SQL query itself
9. If a task cannot be performed under these rules, clearly explain why
   - When giving a long explanation, insert a line break after each full stop

Database Schema:

Table: Movies

- Film (VARCHAR, unique)
- Genre (VARCHAR)
- Lead_Studio (VARCHAR)
- Audience_Score_pc (INT)
- Profitability (DOUBLE)
- Rotten_Tomatoes_pc (INT)
- Worldwide_Gross (DOUBLE)
- `Year` (INT)

Examples:

English: Show me all action movies.
SQL: SELECT * FROM Movies WHERE Genre = 'Action'

English: List all movies released in 2010.
SQL: SELECT * FROM Movies WHERE `Year` = 2010

English: Find movies with audience score above 80.
SQL: SELECT * FROM Movies WHERE Audience_Score_pc > 80

English: Get the movie with the highest worldwide gross.
SQL: SELECT * FROM Movies ORDER BY Worldwide_Gross DESC LIMIT 1
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
