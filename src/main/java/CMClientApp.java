import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;



    //command num
    private final int STARTCM = 1;
    private final int DEFAULT_LOGIN = 2;
    private final int SYNC_LOGIN = 21;
    private final int PRINTALLMENU = 0;


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
        boolean ret = cmStub.startCM();

        if (ret)
            System.out.println("init success");
        else {
            System.err.println("init error");
            return;
        }
        //

        System.out.println("System terminated");
    }
    public void startMainSession() {
        System.out.println("client application starts.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Scanner m_scan = new Scanner(System.in);
        String strInput = null;
        int nCommand = -1;
        boolean m_bRun = false;
        while(m_bRun) {
            System.out.println("Type \"0\" for menu.");
            System.out.print("> ");
            try {
                strInput = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            try {
                nCommand = Integer.parseInt(strInput);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect command number!");
                continue;
            }

            switch (nCommand) {
                case PRINTALLMENU:
                    printAllMenus();
                    break;
                case STARTCM:
                    startCM();
                    break;
                case DEFAULT_LOGIN:
                    defaultLogin();
                    break;
                case SYNC_LOGIN:
                    syncLogin();
                    break;
                default:
            }
        }
    }

    public void printAllMenus() {

    }
    public void startCM()   {

    }
    public void defaultLogin() {
        String userName = null;
        String userPassword = null;
        Console console = System.console();

        boolean ret = false;
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

        //default login
        ret = m_clientStub.loginCM(userName, userPassword);
        if (ret)
            System.out.println("successfully sent the login req");
        else
            System.err.println("failed to sent the login req");
        return;
    }
    public void syncLogin() {
        String userName = null;
        String userPassword = null;
        Console console = System.console();

        boolean ret = false;
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

        CMSessionEvent loginAckEvent = null;
        loginAckEvent = m_clientStub.syncLoginCM(userName, userPassword);
        if(loginAckEvent != null) {
            if(loginAckEvent.isValidUser() == 0)
                System.err.println("This client fails authentication by the default server!");
            else if(loginAckEvent.isValidUser() == -1)
                System.err.println("This client is already in the login-user list!");
            else
                System.out.println("This client successfully logs in to the default server.");
        }
        else
            System.err.println("failed the login request!");
    }
    //after login process has completed, a client app must join a session and a group of CM to finish entering the CM network

}
