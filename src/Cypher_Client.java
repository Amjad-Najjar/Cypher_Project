import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Scanner;

public class Cypher_Client
{

    Cypher_Server MyServer ;
    Socket socket;
    String Response="";
    String Dir="G:\\ITE-FIFTH\\Cypher\\Texts";
    private static String key="DudeIamEncrypted";
    private static RC4 Symmetric_Encrypt=new RC4(key.getBytes());
    private static String Session_key;
    private static GenerateKeys gk;
    private static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static String text;
    private static Signature signature;
    Cypher_Client(String host,int port) throws Exception
    {

        socket = new Socket(host,port);
        gk=new GenerateKeys(1024);
        publicKey=gk.getPublicKey();
        privateKey=gk.getPrivateKey();
        ObjectOutputStream Send_PK =new ObjectOutputStream(socket.getOutputStream());
        Send_PK.writeObject(publicKey);
        ObjectInputStream S_PK  =new ObjectInputStream(socket.getInputStream());
        PublicKey Server_PK=(PublicKey) S_PK.readObject();
        signature=Signature.getInstance("SHA256withDSA");
        signature.initVerify(Server_PK);
        ObjectInputStream Recive_Session_Key =new ObjectInputStream(socket.getInputStream());
        Session_key=(Base64.encodeBase64String(RSA.decrypt((byte[])Recive_Session_Key.readObject(),privateKey)));
        System.out.println(Session_key);
        // Now We Can Use Symmetric Encryption on Session Key
        Symmetric_Encrypt =new RC4(Session_key.getBytes());
        Scanner s =new Scanner(socket.getInputStream());
        String tmp=new String();
       for (File a :new File(Dir).listFiles())
       {
            tmp=s.next();
            System.out.println(Symmetric_Encrypt.decrypt(tmp));
        }
    }
    public String getResponse() {return  Response;}
    void setEditText(String text){this.text=text;}
    public void start_Client(String a,Action action) throws IOException, ClassNotFoundException, SignatureException {
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
                out.println(Symmetric_Encrypt.encrypt(a));

        ObjectOutputStream object_out =new ObjectOutputStream(this.socket.getOutputStream());
                object_out.writeObject(action);

        DataInputStream in = new DataInputStream(this.socket.getInputStream());
                Response = Symmetric_Encrypt.decrypt(in.readUTF());

        ObjectInputStream rec_sign =new ObjectInputStream(socket.getInputStream());
        byte[] byt=(byte[])rec_sign.readObject();
        System.out.println(Base64.encodeBase64String(byt));
        signature.update(Base64.decodeBase64(Response));

        if(signature.verify(byt))
        {
            System.out.println("Yessssssssss!!");
        }

//        System.out.println("\nSomthing Wrong Happened\n");

    }


public void Send_Edited_Text(String Edited) throws IOException {
    DataOutputStream Dataout = new DataOutputStream(this.socket.getOutputStream());
    Dataout.writeUTF(Symmetric_Encrypt.encrypt(Edited));
}




    void SetMyServer(Cypher_Server MyServer)
    {
        this.MyServer=MyServer;
    }
    Cypher_Server getMyServer (){return MyServer;}


}
