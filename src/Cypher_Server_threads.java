import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cypher_Server_threads {
    public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        try (ServerSocket listener = new ServerSocket(11111)) {
            System.out.println("The  server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Cypher_Server(listener.accept()));
            }
        }
    }
}
