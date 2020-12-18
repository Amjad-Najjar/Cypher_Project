import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import Logic.Action;
import java.io.IOException;
import java.security.SignatureException;

public class Controller
{

    //public static int port1;
    //public static String address;

    @FXML
    public TextField namefile;
    @FXML
    public TextField port;
    @FXML
    public TextField ip;
    @FXML
    public AnchorPane con;
    @FXML
    public AnchorPane view;
    @FXML
    public AnchorPane edit;
    @FXML
    public AnchorPane viewtext;
    @FXML
    public TextField EditField;


   public GUI clientSaid;
    public Label file;
    //Cypher_Client cypher_client=new Cypher_Client();

public void Connect(ActionEvent event) throws Exception {
    con.setVisible(false);
    int Port=Integer.parseInt(port.getText().toString());
    String Ip=ip.getText().toString();
    clientSaid=new GUI(Ip,Port);
    view.setVisible(true);

}
public void view(ActionEvent event) throws IOException, SignatureException, ClassNotFoundException {
    view.setVisible(false);
    String name=namefile.getText().toString();
   clientSaid.cypher_client.start_Client(name, Action.View);
    String content=clientSaid.cypher_client.getResponse();
    file.setText(content);
    viewtext.setVisible(true);


}
public void Edit(ActionEvent event) throws IOException, SignatureException, ClassNotFoundException {
    view.setVisible(false);
    String name=namefile.getText().toString();
    clientSaid.cypher_client.start_Client(name, Action.Edit);
    String content=clientSaid.cypher_client.getResponse();
    EditField.setText(content);
    edit.setVisible(true);
}
public void Save(ActionEvent event) throws IOException {
    clientSaid.cypher_client.Send_Edited_Text(EditField.getText().toString());
    System.out.println("done");
}

}
