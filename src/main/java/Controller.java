import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

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
Controller starts a HTTP server and listens for HTTP requests
*/

public class Controller {
    private static final int PORT = 8080;
    /*
     * Creates HTTP server, creates endpoints, calls handler functions for each endpoint
     * */
    public void httpStart() throws IOException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/api/hello", this::handleQuery);
            server.createContext("/", this::handleHealthCheck);
            server.setExecutor(null);
            server.start();
            System.out.println("SERVER IS RUNNING .....");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*Sends a response to a health check request to confirm that the server is healthy*/
    private void handleHealthCheck(HttpExchange exchange) throws IOException {
        System.out.println("HealthCheck Received request method: " + exchange.getRequestMethod());
        exchange.sendResponseHeaders(200,0);
        exchange.close();
    }

    /*Receives request from frontend, sends an appropriate response to the request method*/
    private void handleQuery(HttpExchange exchange) throws IOException {
        System.out.println("Query endpoint Received request method: " + exchange.getRequestMethod());
        if ("GET".equals(exchange.getRequestMethod())) {
            Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
            String query = params.get("query").getFirst();

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type: application/json");

            //String respText = String.format("Hello %s!", query); //forms body
            String respText = ConvertRS();

            exchange.sendResponseHeaders(200, respText.getBytes().length); //200 OK measures output length
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
        }
        else {
            exchange.sendResponseHeaders(405, -1); //405 Method Not Allowed
        }

        exchange.close();
    }

    /*Receives request called query from frontend and splits it into user input*/
    public static Map<String, List<String>> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Pattern.compile("&").splitAsStream(query)
                .map(s -> Arrays.copyOf(s.split("="), 2))
                .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
    }

    /*the userInput from the HTTP request is decoded back into a usable string (English)*/
    private static String decode(final String encoded) {
        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is a required encoding", e);
        }
    }

    /* returns a single line of hardcoded data present for testing and developing the dynamic table on Frontend */
    private static String ConvertRS () {
        String respText = "{";
        int index = 0;
        index++;
        String film = "film";
        String genre = "genre";
        String leadStudio = "studio";
        String audienceScore = "100";
        String profitability = "profit";
        String rottenTomatoes = "70";
        String worldwideGross = "profit";
        String year = "2030";
        respText += String.format("\"%d\": \"%s, %s, %s, %s, %s, %s, %s, %s\"",index, film, genre, leadStudio, audienceScore, profitability, rottenTomatoes, worldwideGross, year);
        respText += "}";
        return respText;
    }
}