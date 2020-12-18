package Logic;

import Algos.RC4;
import Algos.RSA;
import CA.CertifecateAutherity;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Cypher_Server implements Runnable {
    private static final String key = "DudeIamEncrypted";
    static String Dir = "D:\\ITE-FIFTH\\";
    private static RC4 Symmetric_Encrypt = new RC4(key.getBytes());
    private static Socket socket;
    private static Signature sign;
    private static PrivateKey pr_k;
    private static PublicKey pu_k;
    private static PublicKey CAPK;
    private static X509Certificate Server_cert;
    private final boolean isRunning = false;
    Set<Files> MyFiles = new HashSet<Files>();


    Cypher_Server(Socket socket, KeyPair key_pair, X509Certificate cert, PublicKey CAPK) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException {

        Cypher_Server.socket = socket;
        Cypher_Server.Server_cert = cert;
        Cypher_Server.CAPK = CAPK;
        initMyfiles(Dir);
        pr_k = key_pair.getPrivate();
        pu_k = key_pair.getPublic();
        sign = Signature.getInstance("SHA256withDSA");
        sign.initSign(pr_k);

    }

    void initMyfiles(String dir) {
        File f = new File(dir);
        File[] fs = f.listFiles();
        for (File a : fs) {
            MyFiles.add(new Files(a.getName(), a.getAbsolutePath()));
        }

    }

    String[] getFileNames() {
        String[] tmp = new String[MyFiles.size()];
        int cnt = 0;
        for (Files s : MyFiles) {
            tmp[cnt] = s.File_Name;
            cnt++;
        }

        return tmp;
    }

    String getTextFromDir(String Dir) {
        String out = "Somthing Wrong Happened";
        File tmp = new File(Dir);
        try {
            BufferedReader br = new BufferedReader(new FileReader(tmp));
            out = br.lines().collect(Collectors.joining("\n"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    String getTextFromName(String Name) {
        return getTextFromDir(Dir + "\\" + Name);

    }

    @Override
    public void run() {
        try {

            PublicKey C_PK;
            String Session_key;
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            X509Certificate Client_Cert = (X509Certificate) inputStream.readObject();
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(Server_cert);
            if (CertifecateAutherity.verfiy(Client_Cert, CAPK)) {
                System.out.println("The Client Certifecate Has Been Verfied From CA  \n");
            }
            ObjectInputStream Client_PK = new ObjectInputStream(socket.getInputStream());
            C_PK = (PublicKey) Client_PK.readObject();
            ObjectOutputStream Send_PK = new ObjectOutputStream(socket.getOutputStream());
            Send_PK.writeObject(pu_k);
            SecureRandom SR = new SecureRandom();
            Session_key = Base64.encodeBase64String(SR.generateSeed(40));
            ObjectOutputStream Send_Session_Key = new ObjectOutputStream(socket.getOutputStream());
            Send_Session_Key.writeObject(RSA.encrypt(Base64.decodeBase64(Session_key), C_PK));
            System.out.println("Session Key Has Been Made and agreed  :  " + Session_key);
            Symmetric_Encrypt = new RC4(Session_key.getBytes());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            int cnt = 0;
            for (String a : getFileNames()) {
                out.println(Symmetric_Encrypt.encrypt(a));
                cnt++;
            }
            Scanner in = new Scanner(socket.getInputStream());
            String a = Symmetric_Encrypt.decrypt(in.nextLine());
            System.out.println(a+" From server");

            //ObjectInputStream object_in = new ObjectInputStream(socket.getInputStream());
            //Action action = (Action) object_in.readObject();
            Action action=Action.View;

            DataOutputStream pr = new DataOutputStream(socket.getOutputStream());
            pr.writeUTF(Symmetric_Encrypt.encrypt(getTextFromName(a)));
            System.out.println(" From server error");

            sign.update(Base64.decodeBase64(getTextFromName(a)));
            System.out.println("Server Signature :  " + Base64.encodeBase64String(sign.sign()));
            ObjectOutputStream send_sign = new ObjectOutputStream(socket.getOutputStream());
            send_sign.writeObject(sign.sign());
            String tmp = "";
            if (action == Action.Edit) {
                DataInputStream Data_in = new DataInputStream(socket.getInputStream());
                tmp = Symmetric_Encrypt.decrypt(Data_in.readUTF());
                File f = new File(Dir + "\\" + "Edited" + a);
                FileWriter myWriter = new FileWriter(f);
                myWriter.write(tmp);
                System.out.println("Edited Sucessfully " + f.getName() + " " + tmp);
                myWriter.close();
            }


        } catch (Exception e) {
        }
    }
}

