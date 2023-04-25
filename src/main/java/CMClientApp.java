import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

import javax.imageio.IIOException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.util.*;
import java.util.List;

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
    private final int REGISTER_USER = 1;
    private final int DEREGISTER_USER = 11;
    private final int TERMINATECM = 9;

    private final int LOGIN = 2;
    private final int LOGOUT = 22;

    private final int REQUEST_SESSION_INFO = 3;
    private final int REQUEST_CURRENT_GROUP_MEMEBERS =31;
    private final int REQUEST_MY_INFO = 32;

    private final int REQUEST_FILE = 60;
    private final int PUSH_FILE = 61;


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
        JScrollPane scroll = new JScrollPane (m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_logInOutButton = new JButton("LogIn to Default CM Server");
        m_logInOutButton.addActionListener(cmActionListener);
        m_logInOutButton.setEnabled(true);
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
    /*
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
                case REQUEST_CURRENT_GROUP_MEMEBERS:
                    requestCurrentGroupMembers();
                    break;
                case REQUEST_MY_INFO:
                    requestMyInfo();
                    break;
                //file transmission
                case REQUEST_FILE:
                    requestFile();
                    break;
                case PUSH_FILE:
                    pushFile();
                    break;
                default:
            }
        }
    }
    */
    //print menus
    public void printAllMenus() {
        printMsgln("Print All Menu: "+PRINTALLMENU);
        printMsgln("====About User Reg====");
        printMsgln("Register User: "+REGISTER_USER);
        printMsgln("Deregister User: "+DEREGISTER_USER);
        printMsgln("====About CM====");
        printMsgln("Terminate CM: "+TERMINATECM);
        printMsgln("====About Log In/Ou=t===");
        printMsgln("Log In: "+LOGIN);
        printMsgln("Log Out: "+LOGOUT);
        printMsgln("====About Session====");
        printMsgln("Request Session Info: "+REQUEST_SESSION_INFO);
        printMsgln("Request Current Group Members: "+REQUEST_CURRENT_GROUP_MEMEBERS);
        printMsgln("Request My Info: "+REQUEST_MY_INFO);
        printMsgln("====About File Transfer====");
        printMsgln("Request File: "+REQUEST_FILE);
        printMsgln("Push File: "+PUSH_FILE);
    }

    //start/terminate CM
    public void startCM()   {
        boolean bRet = m_clientStub.startCM();
        if(!bRet) {
            JOptionPane.showMessageDialog(null, "There's No CM Server to Connect", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            System.err.println("CM initialization error!");
            return;
        }
        printAllMenus();
        m_bRun = true;
        //startMainSession();
    }
    public void terminateCM()   {
        int option  = JOptionPane.showConfirmDialog(null, "Really Want to Terminate CM?", "[TerminateCM]Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            m_clientStub.terminateCM();
        }
    }

    //register/deregister user (But don't need now)
    // need when cm-server.conf/LOGIN_SCHEME is 1(now 0)
    // LOGIN_SCHEME 0: Don't need Authorization
    //              1: Need Authorization
    public void registerUser()  {
        String userName = null;
        String userPassword = null;

        boolean ret = false;
        JTextField userNameField = new JTextField();
        JPasswordField userPasswordField = new JPasswordField();
        Object[] message = {
                "Enter User Name: ", userNameField,
                "Enter Password: ", userPasswordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Register User Info", JOptionPane.OK_CANCEL_OPTION);
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
            m_clientStub.registerUser(userName, userPassword);
            JOptionPane.showMessageDialog(null, "User["+userName+"] Registered to Default Server");
        }
    }
    public void deregisterUser()  {
        String userName = null;
        String userPassword = null;

        boolean ret = false;
        JTextField userNameField = new JTextField();
        JPasswordField userPasswordField = new JPasswordField();
        Object[] message = {
                "Enter User Name: ", userNameField,
                "Enter Password: ", userPasswordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Deregister User Info", JOptionPane.OK_CANCEL_OPTION);
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
            m_clientStub.registerUser(userName, userPassword);
            JOptionPane.showMessageDialog(null, "User["+userName+"] Registered to Default Server");
        }
    }

    //about Login, Logout
    public boolean login() {
        String userName = null;
        String userPassword = null;

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
                return false;
            }
            if (userName.equals("") ||userPassword.equals("")) {
                JOptionPane.showMessageDialog(null, "User ID/PW is Empty", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            ret = m_clientStub.loginCM(userName, userPassword);
            if (ret) {
                JOptionPane.showMessageDialog(null, "User[" + userName + "] Successed to Login to Default Server");
                m_logInOutButton.setText("LogOut from Default CM Server");
                return true;
            }
            else
                JOptionPane.showMessageDialog(null, "User["+userName+"] Failed to Login to Default Server", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    public void logout()    {
        String userName = m_clientStub.getMyself().getName();
        int option  = JOptionPane.showConfirmDialog(null, "Really Want to LogOut From Default Server?", "[Logout]Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            boolean ret = m_clientStub.logoutCM();
            if (ret) {
                JOptionPane.showMessageDialog(null, "User[" + userName + "] Successed to Login to Default Server");
                m_logInOutButton.setText("LogIn to Default CM Server");
            }
            else
                JOptionPane.showMessageDialog(null, "User["+userName+"] Failed to Login to Default Server", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
    }

    //about request Information
    /*
    public void requestSessionInfo()    {
        printMsgln("==requestSessionInfo==");
        boolean ret = m_clientStub.requestSessionInfo();
        if (ret)   {
            printMsgln("[requestSessionInfo] success");
        }
        else    printMsgln("[requestSessionInfo] failed");
        printMsgln("=====================");
        return;
    }
    */
    public void requestMyInfo() {
        String userName = m_clientStub.getMyself().getName();
        String userPassword = m_clientStub.getMyself().getPasswd();
        printMsgln("");
        Object[] message = {
                "My User Name: ", userName,
                "My Password: ", userPassword
        };
        int option = JOptionPane.showConfirmDialog(null, message, "My User Info", JOptionPane.OK_OPTION);
    }
    public JList getCurGroupUserJList() {
        CMMember curGroupUsers = m_clientStub.getGroupMembers();
        Vector<CMUser> vGroupUsers = curGroupUsers.getAllMembers();
        Iterator<CMUser> iter = vGroupUsers.iterator();
        JList curGroupUserList;
        List<String> curGroupUserlist = new ArrayList<>();
        while(iter.hasNext())   {
            curGroupUserlist.add(iter.next().getName());
        }
        curGroupUserList = new JList(curGroupUserlist.toArray());
        return curGroupUserList;
    }
    public void requestCurrentGroupMembers()    {
        setVisible(true);
        JList curGroupUserList = getCurGroupUserJList();
        MyActionListener cmListSelectionListener = new MyActionListener();
        curGroupUserList.addListSelectionListener(cmListSelectionListener);
        Object[] message = {
                "Client Users In Current Group(Except Me)",
                curGroupUserList,
        };
        JOptionPane.showConfirmDialog(null, message, "Showing Current Group Members", JOptionPane.CLOSED_OPTION);
    }


    //about File Transfer
    public void requestFile()   {
        CMUser cmUserMySelf = m_clientStub.getMyself();
        if (cmUserMySelf.getName()=="?") {
            JOptionPane.showMessageDialog(null, "You Have To Log In to Use this Service", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        CMUser cmUserMySelf = m_clientStub.getMyself();
        if (cmUserMySelf.getName()=="?") {
            JOptionPane.showMessageDialog(null, "You Have To Log In to Use this Service", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        String fileInfos = "";
        for(File file : files)  {
            printMsgln("selected file = " + file);
            fileInfos+=file.getName()+" ["+file.getPath()+"]\n";
        }
        if(files.length < 1) {
            System.err.println("No file selected!");
            return;
        }
        JList curGroupUserList = getCurGroupUserJList();
        curGroupUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        MyActionListener cmListSelectionListener = new MyActionListener();
        curGroupUserList.addListSelectionListener(cmListSelectionListener);
        JTextField recvField = new JTextField();
        Object[] message = {
                "Files To Send", fileInfos,
                "Users In Current Group",
                curGroupUserList,
                "Enter Receiver's Name(Just Enter for SERVER): ", recvField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Confirm PushFile Receiver", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            strReceiver = recvField.getText();
            if (strReceiver.equals(""))
                strReceiver = "SERVER";
            for(File file:files)    {
                String filePath = file.getPath();
                boolean ret = m_clientStub.pushFile(filePath, strReceiver);
                if (ret) {
                    if (strReceiver.equals("SERVER"))
                        printMsgln("User[" + m_clientStub.getMyself().getName() + "] Successed to push File[" + file.getName() + "] to [Default Server]");
                    else
                        printMsgln("User[" + m_clientStub.getMyself().getName() + "] Successed to push File[" + file.getName() + "] to User["+strReceiver+"]");
                }
                else    {
                    if (strReceiver.equals("SERVER"))
                        printMsgln("User["+m_clientStub.getMyself().getName()+"] Failed to push File["+file.getName()+"] to [Default Server]");
                    else
                        printMsgln("User["+m_clientStub.getMyself().getName()+"] Failed to push File["+file.getName()+"] to User["+strReceiver+"]");
                }

            }

        }
    }

    //about Processing Inputs/Outputs
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
            case REGISTER_USER:
                registerUser();
                break;
            case DEREGISTER_USER:
                deregisterUser();
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
            /*
            case REQUEST_SESSION_INFO:
                requestSessionInfo();
                break;
            */
            case REQUEST_CURRENT_GROUP_MEMEBERS:
                requestCurrentGroupMembers();
                break;
            case REQUEST_MY_INFO:
                requestMyInfo();
                break;
            //file transmission
            case REQUEST_FILE:
                requestFile();
                break;
            case PUSH_FILE:
                pushFile();
                break;
            default:
        }
    }
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

    //Listener classes
    public class MyKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_ENTER) {
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
    public class MyActionListener implements ActionListener, ListSelectionListener {
        public void actionPerformed(ActionEvent e)  {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("LogIn to Default CM Server")) {
                if (login()) {
                    printStyledMsg("Client CM starts.\n", "bold");
                    printMsg("Type 0 for menu.\n");
                    button.setText("LogOut from Default CM Server");
                    m_inTextField.requestFocus();
                }
            }
            else if(button.getText().equals("LogOut from Default CM Server")) {
                String userName = m_clientStub.getMyself().getName();
                logout();
                printMsg("User["+userName+"] LogOuted From Default CM Server.\n");
                button.setText("LogIn to Default CM Server");
            }
        }
        public void valueChanged(ListSelectionEvent e)  {
            JList list = (JList) e.getSource();
            if (list.getSelectedIndex()!=-1)    {
                String strToCopy = (String)list.getSelectedValue();
                StringSelection str = new StringSelection(strToCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(str, null);
            }
        }
    }
}
