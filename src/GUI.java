public class GUI {

        public static int portcli;
        public static String address;
        public Cypher_Client cypher_client;
        public GUI(String address,int portcli) throws Exception
        {
            this.portcli=portcli;
            this.address=address;
            cypher_client = new Cypher_Client(address,portcli);
            System.out.println("Client is connecting");

        }




}
