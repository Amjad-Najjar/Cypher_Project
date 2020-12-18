package CA;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.Base64;
import java.util.Date;


public class CertifecateAutherity {


    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public CertifecateAutherity() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException { }

    public static X509Certificate generateV1Certificate(PublicKey PK, PrivateKey PVK, String Name)
            throws InvalidKeyException, NoSuchProviderException, SignatureException {
        Security.addProvider(new BouncyCastleProvider());
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X509Principal("CN=SERVER"));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
        certGen.setSubjectDN(new X509Name("CN=" + Name));
        certGen.setPublicKey(PK);
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        return certGen.generateX509Certificate(PVK);
    }

    public static boolean verfiy(Certificate cr, PublicKey pk) {
        try {
            cr.verify(pk);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String formatCrtFileContents(final Certificate certificate) throws CertificateEncodingException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

        final byte[] rawCrtText = certificate.getEncoded();
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        final String prettified_cert = BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        return prettified_cert;
    }


    public static void main(String[] args) throws Exception {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        kg.initialize(2048);
        KeyPair kp = kg.generateKeyPair();
        PublicKey CAPK = kp.getPublic();
        PrivateKey CAPVK = kp.getPrivate();
        System.out.println(" CA Server is Running  !! ");

        ServerSocket socketCA = new ServerSocket(9632);
        while (true) {
            Socket socket = socketCA.accept();
            ObjectOutputStream OutputStream = new ObjectOutputStream(socket.getOutputStream());
            OutputStream.writeObject(CAPK);
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey Pk = (PublicKey) inputStream.readObject();
            DataInputStream inputStream1 = new DataInputStream(socket.getInputStream());
            String Name = inputStream1.readUTF();
            X509Certificate cert = generateV1Certificate(Pk, CAPVK, Name);
            ObjectOutputStream OutputStream1 = new ObjectOutputStream(socket.getOutputStream());
            OutputStream1.writeObject(cert);
            FileOutputStream fileOut = new FileOutputStream("D:\\ITE-FIFTH\\cert\\" + Name + ".crt");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(cert);
            objectOut.close();
            System.out.println(Name + " Certificate Has been Successful Created and Saved to The Path ...  \n"+cert );

        }

    }
}
