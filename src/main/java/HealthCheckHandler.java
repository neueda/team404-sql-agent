import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
public class HealthCheckHandler {
    public void handler(HttpExchange exchange) throws IOException{
        System.out.println("HealthCheck Received request method:" + exchange.getRequestMethod());
        exchange.sendResponseHeaders(200,0);
        exchange.close();

    }
}
