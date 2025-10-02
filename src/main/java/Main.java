import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.httpStart();

        Database db = new Database();
        db.createTable();
        db.loadCsv();
        db.printCount();

    }
}