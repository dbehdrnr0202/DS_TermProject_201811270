import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private boolean m_bRun;


    //command num
    private final int PRINTALLMENU = 0;
    private final int STARTCM = 1;
    private final int TERMINATECM = 9;

    private final int LOGIN = 2;
    private final int LOGOUT = 22;

    private final int REQUEST_SESSION_INFO = 3;

    private final int REQUEST_FILE = 60;
    private final int PUSH_FILE = 61;
    private final int PUSH_FILES = 62;

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
        cmStub.setAppEventHandler(eventHandler);
        client.startCM();

        //System.out.println("Client App terminated");
    }
    public void startMainSession() {
        System.out.println("client application main session starts.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String strInput = null;
        int nCommand = -1;
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
                System.out.println("Incorrect command format!");
                continue;
            }

            switch (nCommand) {
                case PRINTALLMENU:
                    printAllMenus();
                    break;
                //start, terminate cm
                case STARTCM:
                    startCM();
                    break;
                case TERMINATECM:
                    terminateCM();
                    break;
                //login, logout
                case LOGIN:
                    defaultLogin();
                    break;
                case LOGOUT:
                    logout();
                    break;
                //about session information
                case REQUEST_SESSION_INFO:
                    requestSessionInfo();
                    break;
                //file transmission
                case REQUEST_FILE:
                    requestFile();
                    break;
                case PUSH_FILE:
                    pushFile();
                    break;
                case PUSH_FILES:
                    pushFiles();
                    break;
                default:
            }
        }
    }

    public void printAllMenus() {
        System.out.println("Print All Menu: "+PRINTALLMENU);
        System.out.println("====About CM===");
        System.out.println("Start CM: "+STARTCM);
        System.out.println("Terminate CM0: "+TERMINATECM);
        System.out.println("====About Log In/Out===");
        System.out.println("Log In: "+LOGIN);
        System.out.println("Log Out: "+LOGOUT);
        System.out.println("====About Session===");
        System.out.println("Request Session Info: "+REQUEST_SESSION_INFO);
        System.out.println("====About File===");
        System.out.println("Request File: "+REQUEST_FILE);
        System.out.println("Push File: "+PUSH_FILE);
        System.out.println("Push Files: "+PUSH_FILES);
    }
    public void startCM()   {
        /*
        List<String> localAddressList = CMCommManager.getLocalIPList();
        if(localAddressList == null) {
            System.err.println("Local address not found!");
            return;
        }
        String strCurrentLocalAddress = localAddressList.get(0).toString();

        // get the saved server info from the server configuration file
        String strSavedServerAddress = null;
        int nSavedServerPort = -1;
        String strNewServerAddress = null;
        String strNewServerPort = null;

        strSavedServerAddress = m_clientStub.getServerAddress();
        nSavedServerPort = m_clientStub.getServerPort();

        // ask the user if he/she would like to change the server info
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("========== start CM");
        System.out.println("my current address: "+strCurrentLocalAddress);
        System.out.println("saved server address: "+strSavedServerAddress);
        System.out.println("saved server port: "+nSavedServerPort);

        try {
            System.out.print("new server address (enter for saved value): ");
            strNewServerAddress = br.readLine().trim();
            System.out.print("new server port (enter for saved value): ");
            strNewServerPort = br.readLine().trim();

            // update the server info if the user would like to do
            if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strSavedServerAddress))
                m_clientStub.setServerAddress(strNewServerAddress);
            if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nSavedServerPort)
                m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        boolean bRet = m_clientStub.startCM();
        if(!bRet) {
            System.err.println("CM initialization error!");
            return;
        }
        m_bRun = true;
        startMainSession();
    }
    public void terminateCM()   {
        m_clientStub.terminateCM();
        m_bRun = false;
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
            System.out.println("[login] success");
        else
            System.err.println("[login] failed");
        return;
    }
    /*
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
*/
    public void logout()    {
        boolean bRet = m_clientStub.logoutCM();
        if (bRet)
            System.out.println("[logout] success");
        else
            System.err.println("[logout] failed");
    }
    public void requestSessionInfo()    {
        System.out.println("==requestSessionInfo==");
        boolean bRet = m_clientStub.requestSessionInfo();
        if (bRet)   System.out.println("[requestSessionInfo] success");
        else    System.out.println("[requestSessionInfo] failed");
        System.out.println("=====================");
        return;
    }
    public void requestFile()   {
        String strFileName = null;
        String strFileOwner = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== request a file");
        try {
            System.out.print("File name: ");
            strFileName = br.readLine();
            System.out.print("File owner: ");
            strFileOwner = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bRet = m_clientStub.requestFile(strFileName, strFileOwner);
        if (bRet)
            System.out.println("[requestFile] success");
        else
            System.err.println("[requestFile] failed");

    }
    public void pushFile()  {
        String strFilePath = null;
        String strReceiver = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== push a file");
        try {
            System.out.print("File path name: ");
            strFilePath = br.readLine();
            System.out.print("File receiver: ");
            strReceiver = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bRet = m_clientStub.pushFile(strFilePath, strReceiver);
        if (bRet)
            System.out.println("[pushFile] success");
        else
            System.err.println("[pushFile] failed");
    }
    public void pushFiles() {
        String[] strFiles = null;
        String strFileList = null;
        int nMode = -1; // 1: push, 2: pull
        int nFileNum = -1;
        String strTarget = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== pull/push multiple files");
        try {
            System.out.print("Select mode (1: push, 2: pull): ");
            nMode = Integer.parseInt(br.readLine());
            if(nMode == 1) {
                System.out.print("Input receiver name: ");
                strTarget = br.readLine();
            }
            else if(nMode == 2) {
                System.out.print("Input file owner name: ");
                strTarget = br.readLine();
            }
            else {
                System.out.println("Incorrect transmission mode!");
                return;
            }

            System.out.print("Number of files: ");
            nFileNum = Integer.parseInt(br.readLine());
            System.out.print("Input file names separated with space: ");
            strFileList = br.readLine();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        strFileList.trim();
        strFiles = strFileList.split("\\s+");
        if(strFiles.length != nFileNum) {
            System.out.println("The number of files incorrect!");
            return;
        }

        for(int i = 0; i < nFileNum; i++) {
            switch(nMode) {
                case 1: // push
                    CMFileTransferManager.pushFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
                    break;
                case 2: // pull
                    CMFileTransferManager.requestPermitForPullFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
                    break;
            }
        }

        return;
    }
}
