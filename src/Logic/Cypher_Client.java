package Logic;

import Algos.RC4;
import Algos.RSA;
import CA.CertifecateAutherity;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class Cypher_Client {

    private static String key = "DudeIamEncrypted";
    private static RC4 Symmetric_Encrypt = new RC4(key.getBytes());
    private static String Session_key;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static String text;
    private static Signature signature;

    Cypher_Server MyServer;
    Socket socket;
    String Response = "";
    String Dir = "D:\\ITE-FIFTH\\";

    public Cypher_Client(String host, int port) throws Exception {

        socket = new Socket(host, port);
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        kg.initialize(2048);
        KeyPair gk = kg.generateKeyPair();
        publicKey = gk.getPublic();
        privateKey = gk.getPrivate();
        Socket socketCa = new Socket("127.0.0.1", 9632);
        ObjectInputStream inStream = new ObjectInputStream(socketCa.getInputStream());
        PublicKey CAPK = (PublicKey) inStream.readObject();

        ObjectOutputStream outStream = new ObjectOutputStream(socketCa.getOutputStream());
        outStream.writeObject(publicKey);
        DataOutputStream outStream1 = new DataOutputStream(socketCa.getOutputStream());
        outStream1.writeUTF("Client cert");

        ObjectInputStream inStream1 = new ObjectInputStream(socketCa.getInputStream());

        X509Certificate cert = (X509Certificate) inStream1.readObject();
        socketCa.close();
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(cert);
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        X509Certificate Server_Cert = (X509Certificate) inputStream.readObject();

        if (CertifecateAutherity.verfiy(Server_Cert, CAPK)) {
            System.out.println("The Server Certifecate Has Been Verfied From CA  \n");
        }
        ObjectOutputStream Send_PK = new ObjectOutputStream(socket.getOutputStream());
        Send_PK.writeObject(publicKey);
        ObjectInputStream S_PK = new ObjectInputStream(socket.getInputStream());
        PublicKey Server_PK = (PublicKey) S_PK.readObject();
        signature = Signature.getInstance("SHA256withDSA");
        signature.initVerify(Server_PK);
        ObjectInputStream Recive_Session_Key = new ObjectInputStream(socket.getInputStream());
        Session_key = (Base64.encodeBase64String(RSA.decrypt((byte[]) Recive_Session_Key.readObject(), privateKey)));
        // Now We Can Use Symmetric Encryption on Session Key
        Symmetric_Encrypt = new RC4(Session_key.getBytes());

        Scanner s = new Scanner(socket.getInputStream());
        String tmp = new String();
        for (File a : new File(Dir).listFiles()) {
            tmp = s.next();
            System.out.println(Symmetric_Encrypt.decrypt(tmp));
        }
    }

    public String getResponse() {
        return Response;
    }

    void setEditText(String text) {
        this.text = text;
    }

    public void start_Client(String a, Action action) throws IOException, ClassNotFoundException, SignatureException {
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        out.println(Symmetric_Encrypt.encrypt(a));

        //ObjectOutputStream object_out = new ObjectOutputStream(this.socket.getOutputStream());
        //object_out.writeObject(action);
        //System.out.println(action+" From Client ");

        DataInputStream in = new DataInputStream(this.socket.getInputStream());
        Response = Symmetric_Encrypt.decrypt(in.readUTF());

        ObjectInputStream rec_sign = new ObjectInputStream(socket.getInputStream());
        byte[] byt = (byte[]) rec_sign.readObject();
       String sign=Base64.encodeBase64String(byt);
        signature.update(Base64.decodeBase64(Response));

        if (signature.verify(byt)) {
            System.out.println("The Signature Has Been Verifed !"+sign);
        }

    }


    public void Send_Edited_Text(String Edited) throws IOException {
        DataOutputStream Dataout = new DataOutputStream(this.socket.getOutputStream());
        Dataout.writeUTF(Symmetric_Encrypt.encrypt(Edited));
    }


    void SetMyServer(Cypher_Server MyServer) {
        this.MyServer = MyServer;
    }

    Cypher_Server getMyServer() {
        return MyServer;
    }


}
