package Logic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cypher_Server_threads {
    public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, ClassNotFoundException {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("DSA");
        kg.initialize(2048);
        KeyPair kp = kg.generateKeyPair();
        Socket CASocket = new Socket("127.0.0.1", 9632);
        ObjectInputStream inStream = new ObjectInputStream(CASocket.getInputStream());
        PublicKey CAPK = (PublicKey) inStream.readObject();

        ObjectOutputStream outStream = new ObjectOutputStream(CASocket.getOutputStream());
        outStream.writeObject(kp.getPublic());

        DataOutputStream outStream1 = new DataOutputStream(CASocket.getOutputStream());
        outStream1.writeUTF("Server cert");

        ObjectInputStream inStream1 = new ObjectInputStream(CASocket.getInputStream());

        X509Certificate cert = (X509Certificate) inStream1.readObject();
        CASocket.close();

        try (ServerSocket listener = new ServerSocket(11111)) {
            System.out.println("The Main server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Cypher_Server(listener.accept(), kp, cert, CAPK));
            }
        }
    }
}
