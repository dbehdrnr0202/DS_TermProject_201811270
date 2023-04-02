import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

    public CMClientApp(){
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }
    public CMClientStub getClientStub() {
        return m_clientStub;
    }
    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }
    public static void main(String[] args)  {
        CMClientApp client = new CMClientApp();
        CMClientStub cmStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();
        cmStub.setAppEventHandler(eventHandler);//여기가 왜?
        /*
        * Provided: CMClientEventHandler
        * required:  CMAppEventHandler
        */
        String userName = null;
        String userPassword = null;
        Scanner scanner = new Scanner(System.in);

        boolean ret = false;
        ret = cmStub.startCM();

        if (ret)
            System.out.println("init success");
        else {
            System.err.println("init error");
            return;
        }
        //
        System.out.print("Enter user name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            userName = br.readLine();
            if (console==null)  {
                System.out.print("Enter password: ");
                userPassword = br.readLine();
            }
            else
                userPassword = new String(console.readPassword("password: "));
        }
        catch (IOException e)  {
            e.printStackTrace();
        }
        //
        System.out.println("user name: "+userName);
        System.out.println("password: "+userPassword);
        ret = m_clientStub.loginCM(userName, userPassword);
        if (ret)
            System.out.println("successfully sent the login req");
        else {
            System.err.println("failed to sent the login req");
            return;
        }

        System.out.println("Press enter to execute next API");
        scanner.nextLine();

    }
}
