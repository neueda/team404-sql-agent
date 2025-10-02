import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.*;
/*
 *Controller starts an HTTP server and listens for HTTP requests
 */
public class Controller {
    private static final int PORT = 8080;

    /*
     * Creates HTTP server, creates endpoints, calls handler functions for each endpoint
     */
    public void httpStart() throws IOException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/api/hello", this::handleQuery);
            server.createContext("/", this::handleHealthCheck);
            server.setExecutor(null);
            server.start();
            System.out.println("SERVER IS RUNNING ON PORT " + PORT);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
     * Sends a response to a health check request to confirm that the server is healthy
     */
    private void handleHealthCheck(HttpExchange exchange) throws IOException {
        System.out.println("HealthCheck Received request method: " + exchange.getRequestMethod());
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }

    /*
     * Receives request from frontend, sends an appropriate response to the request method
     */
    public  void handleQuery(HttpExchange exchange) throws IOException {
        System.out.println("Query endpoint Received request method: " + exchange.getRequestMethod());
        if ("GET".equals(exchange.getRequestMethod())) {
            Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
            String userInput = params.get("query").getFirst();
            Agent agent = new Agent();
            String aiResponse =  agent.startAgent(userInput);
            System.out.println("AI RESPONSE: " + aiResponse);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type: application/json");
            //response body starts empty
            //call static validation methods
            byte[] responseBytes = null;
            if (Database.validateInput(aiResponse) && Database.validateInputTable(aiResponse)) {
                JSONArray JsonResult = Database.executeQuery(aiResponse);
                //return results in http response
                System.out.println(JsonResult);
                responseBytes = JsonResult.toString().getBytes();
            }

            exchange.sendResponseHeaders(200, responseBytes.length); //200 OK measures output length
            OutputStream output = exchange.getResponseBody();
            output.write(responseBytes);
            output.flush();
        } else {
            exchange.sendResponseHeaders(405, -1); //405 Method Not Allowed
        }
        exchange.close();
    }

    /*
     * Receives request called query from frontend and splits it into user input
     * */
    public static Map<String, List<String>> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Pattern.compile("&").splitAsStream(query)
                .map(s -> Arrays.copyOf(s.split("="), 2))
                .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
    }

    /*
     * the userInput from the HTTP request is decoded back into a usable string (English)
     * */
    private static String decode(final String encoded) {
        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is a required encoding", e);
        }
    }
}