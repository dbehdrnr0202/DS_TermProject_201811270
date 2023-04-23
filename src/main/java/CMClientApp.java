import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

import javax.imageio.IIOException;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.io.File;
import java.util.List;
import java.util.Scanner;

public class CMClientApp extends JFrame {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private Scanner m_scanner;
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_logInOutButton;
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

    public void printMsg(String strText) {
        printStyledMsg(strText, null);
    }
    public void printMsgln(String strText) {
        printMsg(strText+"\n");
        return;
    }
    public void printStyledMsg(String strText, String strStyleName) {
        StyledDocument doc = m_outTextPane.getStyledDocument();
        try {
            if (strStyleName==null)
                doc.insertString(doc.getLength(), strText, null);
            else doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return;
    }
    public void printStyledMsgln(String strText, String strStyleName)   {
        printStyledMsg(strText+"\n", strStyleName);
        return;
    }
    public class MyKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e)
        {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_ENTER)
            {
                JTextField input = (JTextField)e.getSource();
                String strText = input.getText();
                printMsg(strText+"\n");
                // parse and call CM API
                processInput(strText);
                input.setText("");
                input.requestFocus();
            }
        }

        public void keyReleased(KeyEvent e){}
        public void keyTyped(KeyEvent e){}
    }
    private void processInput(String strText) {
        int nCommand = -1;
        try{
            nCommand = Integer.parseInt(strText);
        }catch (NumberFormatException e)    {
            printMsgln("Command Number Error");
            return;
        }

        switch (nCommand) {
            case PRINTALLMENU:
                printAllMenus();
                break;
            //start, terminate cm
            case TERMINATECM:
                terminateCM();
                break;
            //login, logout
            case LOGIN:
                login();
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
    public class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("Start Client CM"))
            {
                // start cm
                boolean bRet = m_clientStub.startCM();
                if(!bRet)
                {
                    printStyledMsg("CM initialization error!\n", "bold");
                }
                else
                {
                    printStyledMsg("Client CM starts.\n", "bold");
                    printMsg("Type \"0\" for menu.\n");
                    // change button to "stop CM"
                    button.setText("Stop Client CM");
                }
                m_inTextField.requestFocus();
            }
            else if(button.getText().equals("LogOut to Default CM Server"))
            {
                // stop cm
                m_clientStub.terminateCM();
                printMsg("Client CM terminates.\n");
                // change button to "start CM"
                button.setText("Start Client CM");
            }
        }
    }
    public CMClientApp(){
        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();
        setTitle("CM Client");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setEditable(false);

        StyledDocument doc = m_outTextPane.getStyledDocument();
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = doc.addStyle("bold", defStyle);
        StyleConstants.setBold(boldStyle, true);

        add(m_outTextPane, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane (m_outTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_logInOutButton = new JButton("LogIn to Default CM Server");
        m_logInOutButton.addActionListener(cmActionListener);
        m_logInOutButton.setEnabled(false);
        //add(startStopButton, BorderLayout.NORTH);
        topButtonPanel.add(m_logInOutButton);

        setVisible(true);
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub, this);
        m_scanner = new Scanner((System.in));
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

        //printMsgln("Client App terminated");
    }
    public void startMainSession() {
        printMsgln("client application main session starts.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String strInput = null;
        int nCommand = -1;
        while(m_bRun) {
            printMsgln("Type \"0\" for menu.");
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
                printMsgln("Incorrect command format!");
                continue;
            }

            switch (nCommand) {
                case PRINTALLMENU:
                    printAllMenus();
                    break;
                //start, terminate cm
                case TERMINATECM:
                    terminateCM();
                    break;
                //login, logout
                case LOGIN:
                    login();
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
        printMsgln("Print All Menu: "+PRINTALLMENU);
        printMsgln("====About CM===");
        //printMsgln("Start CM: "+STARTCM);
        printMsgln("Terminate CM: "+TERMINATECM);
        printMsgln("====About Log In/Out===");
        printMsgln("Log In: "+LOGIN);
        printMsgln("Log Out: "+LOGOUT);
        printMsgln("====About Session===");
        printMsgln("Request Session Info: "+REQUEST_SESSION_INFO);
        printMsgln("====About File===");
        printMsgln("Request File: "+REQUEST_FILE);
        printMsgln("Push File: "+PUSH_FILE);
        printMsgln("Push Files: "+PUSH_FILES);
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
        printMsgln("========== start CM");
        printMsgln("my current address: "+strCurrentLocalAddress);
        printMsgln("saved server address: "+strSavedServerAddress);
        printMsgln("saved server port: "+nSavedServerPort);

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

    public void login() {
        String userName = null;
        String userPassword = null;
        //Console console = System.console();

        boolean ret = false;
        JTextField userNameField = new JTextField();
        JPasswordField userPasswordField = new JPasswordField();
        Object[] message = {
                "Enter User Name: ", userNameField,
                "Enter Password: ", userPasswordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Login Info", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            userName = userNameField.getText();
            userPassword = new String(userPasswordField.getPassword());
            if (userName.equals("SERVER")) {
                JOptionPane.showMessageDialog(null, "UserName SERVER is only for Server App", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (userName.equals("") ||userPassword.equals("")) {
                JOptionPane.showMessageDialog(null, "User ID/PW is Empty", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ret = m_clientStub.loginCM(userName, userPassword);
            if (ret)
                JOptionPane.showMessageDialog(null, "User["+userName+"] Successed to Login to Default Server");
            else
                JOptionPane.showMessageDialog(null, "User["+userName+"] Failed to Login to Default Server", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
        /*
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
        printMsgln("user name: "+userName);
        printMsgln("password: "+userPassword);

        //default login
        ret = m_clientStub.loginCM(userName, userPassword);
        if (ret)
            printMsgln("[login] success");
        else
            System.err.println("[login] failed");
        return;
        */
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
        printMsgln("user name: "+userName);
        printMsgln("password: "+userPassword);

        CMSessionEvent loginAckEvent = null;
        loginAckEvent = m_clientStub.syncLoginCM(userName, userPassword);
        if(loginAckEvent != null) {
            if(loginAckEvent.isValidUser() == 0)
                System.err.println("This client fails authentication by the default server!");
            else if(loginAckEvent.isValidUser() == -1)
                System.err.println("This client is already in the login-user list!");
            else
                printMsgln("This client successfully logs in to the default server.");
        }
        else
            System.err.println("failed the login request!");
    }
    //after login process has completed, a client app must join a session and a group of CM to finish entering the CM network
*/
    public void logout()    {
        String userName = m_clientStub.getMyself().getName();
        int option  = JOptionPane.showConfirmDialog(null, "Really Want to LogOut From Default Server?", "[Logout]Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            boolean ret = m_clientStub.logoutCM();
            if (ret)
                JOptionPane.showMessageDialog(null, "User["+userName+"] Successed to Login to Default Server");
            else
                JOptionPane.showMessageDialog(null, "User["+userName+"] Failed to Login to Default Server", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void requestSessionInfo()    {
        printMsgln("==requestSessionInfo==");
        boolean bRet = m_clientStub.requestSessionInfo();
        if (bRet)   printMsgln("[requestSessionInfo] success");
        else    printMsgln("[requestSessionInfo] failed");
        printMsgln("=====================");
        return;
    }
    public void requestFile()   {
        String strFileName = null;
        String strFileOwner = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printMsgln("====== request a file");
        try {
            System.out.print("File name: ");
            strFileName = br.readLine();
            System.out.print("File owner(server name): ");
            strFileOwner = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bRet = m_clientStub.requestFile(strFileName, strFileOwner);
        if (bRet)
            printMsgln("[requestFile] success");
        else
            System.err.println("[requestFile] failed");

    }
    public void pushFile()  {
        String strFilePath = null;
        String strReceiver = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Path transferHome = m_clientStub.getTransferedFileHome();
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(transferHome.toFile());
        int fcRet = fc.showOpenDialog(null);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        File[] files = fc.getSelectedFiles();

        for(File file : files)
            printMsgln("selected file = " + file);
        if(files.length < 1) {
            System.err.println("No file selected!");
            return;
        }
        JTextField recvField = new JTextField();
        Object[] message = {
                "Enter Receiver's Name(null for SERVER): ", recvField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Login Info", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            strReceiver = recvField.getText();
            if (strReceiver == null)
                strReceiver = "SERVER";
            for(File file:files)    {
                String filePath = file.getPath();
                boolean ret = m_clientStub.pushFile(filePath, strReceiver);
                if (ret)
                    printMsgln("User["+m_clientStub.getMyself().getName()+"] Successed to push File["+file.getName()+"] to User["+strReceiver+"]");
                else
                    printMsgln("User["+m_clientStub.getMyself().getName()+"] Failed to push File["+file.getName()+"] to User["+strReceiver+"]");
            }

        }
        /*
        printMsgln("====== push a file");
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
            printMsgln("[pushFile] success");
        else
            System.err.println("[pushFile] failed");

         */
    }
    public void pushFiles() {
        String[] strFiles = null;
        String strFileList = null;
        int nMode = -1; // 1: push, 2: pull
        int nFileNum = -1;
        String strTarget = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printMsgln("====== pull/push multiple files");
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
                printMsgln("Incorrect transmission mode!");
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
            printMsgln("The number of files incorrect!");
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
