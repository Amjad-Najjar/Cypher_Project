import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Scanner;

public class Console_Main {
    static private Robot robot;

    private static void insert(String text) {
        if (robot == null) {
            // Lazily initialise the robot
            try {
                robot = new Robot();
                robot.setAutoDelay(5);
                robot.setAutoWaitForIdle(true);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
        char[] chars = text.toCharArray();
        for (char c : chars) {
            int code = KeyEvent.getExtendedKeyCodeForChar(c);
            robot.keyPress(code);
            robot.keyRelease(code);
        }
    }

    public static void main(String[] args) throws Exception {
        ////System.out.println("Client is running !!\nPlease Enter the Host ip and The Server Port");
        //// Scanner in =new Scanner(System.in);
        ////String host=in.nextLine();
        ////int Port =in.nextInt();
        Cypher_Client c = new Cypher_Client("127.0.0.1", 11111);
        System.out.println("Enter The name of file you Want to open then Enter if you want to 1 to edit or 2 view !!\n");
        Scanner in = new Scanner(System.in);
        String FileName = in.nextLine();
        in = new Scanner(System.in);
        int act = in.nextInt();

        Action action;
        System.out.println(act);
        if (act == 1) {
            action = Action.Edit;
        } else {
            action = Action.View;
        }
        c.start_Client(FileName, action);
        if (action == Action.View) {
            System.out.println(c.getResponse());
        }
        in = new Scanner(System.in);

        if (action == Action.Edit) {
            System.out.println("Please Edit and press Enter !!\n ");
            insert(c.getResponse());
            c.Send_Edited_Text(in.nextLine());
        }


    }
}
