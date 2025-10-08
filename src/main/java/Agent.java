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

    /*
     * startAgent uses the builder method to create an AI Agent, using the supplied model name, app name and userId.
     * The agent processes a user input string and returns feedback or a formatted SQL query
     * */
    public String startAgent(String userInput) {
        //LlmAgent chosen to create our agent
        LlmAgent team404Agent = LlmAgent.builder()
                .model(model)
                .name(appName)
                .description("you are an agent that translates English into valid SQL queries")
                .instruction("""
                        Your job is to translate English questions into SQL queries.
                        
                        Rules:
                        
                        Only use: SELECT * FROM Movies
                        
                        Do not include ; at the end of the query
                        
                        You may add:
                        
                        WHERE
                        
                        ORDER BY
                        
                        LIMIT
                        
                        BETWEEN
                        
                        OFFSET
                        
                        Do not use any other SQL commands
                        
                        Never use: INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, or anything that modifies the database
                        
                        Only one query is allowed (no multiple queries)
                        
                        Always use the exact column names from the schema
                        
                        Output must be plain SQL only
                        
                        Do not wrap in markdown code fences
                        
                        Do not include sql or
                        
                        Do not add explanations, comments, or extra text
                        
                        Return only the SQL query itself
                        
                        If a task cannot be performed under these rules, clearly explain why
                        
                        When giving a long explanation, insert a line break after each full stop
                        
                        When a user asks for "revenue," they mean Worldwide_Gross
                        
                        Always order by highest (DESC) unless the user specifies otherwise
                        
                        Database Schema:
                        
                        Table: Movies
                        
                        Film (VARCHAR, unique)
                        
                        Genre (VARCHAR)
                        
                        Lead_Studio (VARCHAR)
                        
                        Audience_Score_pc (INT)
                        
                        Profitability (DOUBLE)
                        
                        Rotten_Tomatoes_pc (INT)
                        
                        Worldwide_Gross (DOUBLE)
                        
                        `Year` (INT)
                        
                        Examples:
                        
                        English: Show me all action movies.
                        SQL: SELECT * FROM Movies WHERE Genre = 'Action'
                        
                        English: List all movies released in 2010.
                        SQL: SELECT * FROM Movies WHERE `Year` = 2010
                        
                        English: Find movies with audience score above 80.
                        SQL: SELECT * FROM Movies WHERE Audience_Score_pc > 80
                        
                        English: Get the movie with the highest worldwide gross.
                        SQL: SELECT * FROM Movies ORDER BY Worldwide_Gross DESC LIMIT 1
                        
                        English: Get all movies with a name between A and D
                        SQL: SELECT * FROM Movies WHERE Film BETWEEN 'A' AND 'D'
                        
                        English: show me movies with a lead studio starting with A through F
                        SQL: SELECT * FROM Movies WHERE Lead_Studio BETWEEN 'A' AND 'F'
                        
                        English: rank movies alphabetically from B to G
                        SQL: SELECT * FROM Movies WHERE Film BETWEEN 'B' AND 'G' ORDER BY Film
                        """)
                .build();

        //create a session memory
        InMemorySessionService sessionService = new InMemorySessionService();
        //stores the context of the exchange with the input and agent responses (Conversation)
        Session session = sessionService.createSession(appName, userId).blockingGet();
        //runner makes the agent perform its tasks in the environment
        Runner runner = new Runner(team404Agent, appName, null, sessionService);
        //userContent text contains the user message to the agent
        Content userContent = Content.fromParts(Part.fromText(userInput));
        //events is the stream of data (events) coming from the agent runner - Async
        Flowable<Event> events = runner.runAsync(userId, session.id(), userContent);
        //we want to pull the agent response out of the exchange and store it
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
