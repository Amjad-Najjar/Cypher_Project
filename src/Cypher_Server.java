import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Cypher_Server implements Runnable {
    private static final String key = "DudeIamEncrypted";
    private static  RC4 Symmetric_Encrypt = new RC4(key.getBytes());
    static String Dir = "G:\\ITE-FIFTH\\Cypher\\Texts";
    private static Socket socket;
    private final boolean isRunning = false;

    Set<Files> MyFiles = new HashSet<Files>();


    Cypher_Server(Socket socket) {
        Cypher_Server.socket = socket;
        initMyfiles(Dir);
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

    boolean get_Conniction_Status() {
        return isRunning;
    }

    void Send_Response(int port, String SelectedName, Socket socket) throws IOException {

        System.out.println(getTextFromName(SelectedName));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(getTextFromName(SelectedName));


    }


    @Override
    public void run() {
        try {

            PublicKey C_PK;
            String Session_key;
            ObjectInputStream Client_PK = new ObjectInputStream(socket.getInputStream());
            C_PK=(PublicKey)Client_PK.readObject();
            System.out.println(C_PK);
            SecureRandom SR =new SecureRandom();
            Session_key= Base64.encodeBase64String(SR.generateSeed(40));
            ObjectOutputStream Send_Session_Key =new ObjectOutputStream(socket.getOutputStream());
            Send_Session_Key.writeObject(RSA.encrypt(Base64.decodeBase64(Session_key),C_PK));
            System.out.println(Session_key);
            Symmetric_Encrypt =new RC4(Session_key.getBytes());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            int cnt = 0;
            for (String a : getFileNames()) {
                out.println(Symmetric_Encrypt.encrypt(a));
                cnt++;
            }

            Scanner in = new Scanner(socket.getInputStream());
            String a = Symmetric_Encrypt.decrypt(in.nextLine());
            ObjectInputStream object_in =new ObjectInputStream(socket.getInputStream());
            Action action=(Action) object_in.readObject();
            DataOutputStream pr = new DataOutputStream(socket.getOutputStream());
            pr.writeUTF(Symmetric_Encrypt.encrypt(getTextFromName(a)));
//            switch (action) {
//                case View: {
//                    PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
//                    pr.println(Symmetric_Encrypt.encrypt(getTextFromName(Symmetric_Encrypt.decrypt(a))));
//                    break;
//                }
//                case Edit: {
                    String tmp="";
//                    PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
//                    pr.println(Symmetric_Encrypt.encrypt(getTextFromName(Symmetric_Encrypt.decrypt(a))));
            if(action==Action.Edit){
                    DataInputStream Data_in =new DataInputStream(socket.getInputStream());
                    tmp=Symmetric_Encrypt.decrypt(Data_in.readUTF());
                    File f =new File(Dir+"\\"+"Edited"+a);
            FileWriter myWriter = new FileWriter(f);
            myWriter.write(tmp);
            myWriter.close();
                    System.out.println("edited Sucessfully "+f.getName()+" "+tmp);}



        } catch (Exception e) {
        }
    }
}

