import com.sun.net.httpserver.HttpServer;

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

public class Controller {
        public static void main(String[] args) throws Exception {
            int serverPort = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext("/api/hello", (exchange -> {
                System.out.println("Received request method: " + exchange.getRequestMethod());

                if ("GET".equals(exchange.getRequestMethod())) {
                    Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
                    String noNameText = "Anonymous";
                    String name = params.getOrDefault("name", List.of(noNameText)).stream().findFirst().orElse(noNameText);
                    String respText = String.format("Hello %s!", name); //forms body
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
                    exchange.sendResponseHeaders(200, respText.getBytes().length); //200 OK
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
            if (query == null || "".equals(query)) {
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
}
