import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.*;


public class Controller {
        public static void main(String[] args) throws Exception {
            int serverPort = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext("/api/hello", (exchange -> {
                System.out.println("Received request method: " + exchange.getRequestMethod());

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
            }));
            server.setExecutor(null); //creates a default executor
            server.start();
        }

        public static Map<String, List<String>> splitQuery(String query) {
            if (query == null || query.isEmpty()) {
                return Collections.emptyMap();
            }
            return Pattern.compile("&").splitAsStream(query)
                    .map(s -> Arrays.copyOf(s.split("="), 2))
                    .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
        }

        private static String decode(final String encoded) {
            try {
                return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 is a required encoding", e);
            }
        }

        private static String ConvertRS () {
            //will receive a resultset, and loop through it assigning a key and value to a JSON shaped string for each row
            //single line of hardcoded data present for testing and developing the dynamic table
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
