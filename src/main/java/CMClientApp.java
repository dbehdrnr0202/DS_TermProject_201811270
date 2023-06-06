import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

import javax.imageio.IIOException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CMClientApp extends JFrame {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;
    private JTextPane m_outTextPane;
    private JTextPane m_fileListPane;
    private JTextPane m_userListPane;
    private JTextField m_inTextField;
    private JButton m_logInOutButton;
    private JButton m_refreshUserListButton;
    private JButton m_refreshFileListButton;
    private JButton m_syncFileButton;
    private JButton m_pushFileButton;
    private JButton m_setPathButton;
    private JList m_userJList;
    private JList m_fileJList;
    private HashMap<Path, FileTime> curFileList;
    private HashMap<Path, Integer> fileLogicalClock;
    private String savedFilePath;

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
    private final int PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -1;
    private final int ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -11;
    private final int PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -2;
    private final int ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -21;

    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER = -3;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER = -31;
    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -4;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -41;
    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -5;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -51;
    private final int REQUEST_SYNC_FILE = -3;
    //private final int
    public CMClientApp(){
        makeUI();
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub, this);
        savedFilePath = m_clientStub.getTransferedFileHome().toAbsolutePath().toString();
    }
    public CMClientStub getClientStub() {
        return m_clientStub;
    }
    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }
    public String getSavedFilePath()    { return savedFilePath; }
    private void makeUI()   {
        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();

        setSize(500, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setEditable(false);
        m_outTextPane.setSize(500, 400);
        m_fileListPane = new JTextPane();
        m_fileListPane.setEditable(false);
        m_userListPane = new JTextPane();
        m_userListPane.setEditable(false);

        m_fileJList = new JList();
        m_userJList = new JList();

        StyledDocument output_doc = m_outTextPane.getStyledDocument();
        Style output_defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = output_doc.addStyle("regular", output_defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = output_doc.addStyle("bold", output_defStyle);
        StyleConstants.setBold(boldStyle, true);

        JPanel userListPanel = new JPanel();
        JPanel fileListPanel = new JPanel();
        m_setPathButton = new JButton("Set File Path");
        m_setPathButton.addActionListener(cmActionListener);
        m_setPathButton.setEnabled(true);
        m_syncFileButton = new JButton("Sync Files");
        m_syncFileButton.addActionListener(cmActionListener);
        m_syncFileButton.setEnabled(true);
        m_refreshFileListButton = new JButton("Refresh FileList");
        m_refreshFileListButton.addActionListener(cmActionListener);
        m_refreshFileListButton.setEnabled(true);
        m_pushFileButton = new JButton("Push Files");
        m_pushFileButton.addActionListener(cmActionListener);
        m_pushFileButton.setEnabled(true);

        fileListPanel.add(m_refreshFileListButton);
        fileListPanel.add(m_syncFileButton);
        fileListPanel.add(m_pushFileButton);
        fileListPanel.add(m_setPathButton);
        fileListPanel.add(m_fileJList, BorderLayout.SOUTH);
        fileListPanel.setBorder(new TitledBorder("FILE LIST"));

        m_refreshUserListButton = new JButton("Refresh UserList");
        m_refreshUserListButton.addActionListener(cmActionListener);
        m_refreshUserListButton.setEnabled(true);
        userListPanel.add(m_refreshUserListButton);
        userListPanel.add(m_userJList, BorderLayout.SOUTH);
        userListPanel.setBorder(new TitledBorder("USER LIST"));

        JScrollPane scroll = new JScrollPane(m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane user_scroll = new JScrollPane(m_fileListPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane file_scroll = new JScrollPane(m_userListPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);
        userListPanel.add(user_scroll);
        fileListPanel.add(file_scroll);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        //add(topButtonPanel, BorderLayout.NORTH);

        m_logInOutButton = new JButton("LogIn to Default CM Server");
        m_logInOutButton.addActionListener(cmActionListener);
        m_logInOutButton.setEnabled(true);
        topButtonPanel.add(m_logInOutButton);
        add(topButtonPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.add(fileListPanel, BorderLayout.WEST);
        panel.add(userListPanel, BorderLayout.EAST);
        panel.add(m_outTextPane, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        add(m_inTextField, BorderLayout.SOUTH);

        //pack();
        setVisible(true);

    }
    public static void main(String[] args) throws IOException {
        CMClientApp client = new CMClientApp();
        CMClientStub cmStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();
        cmStub.setAppEventHandler(eventHandler);
        client.startCM();

        //printMsgln("Client App terminated");
    }

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
    public void startCM() throws IOException {
        boolean bRet = m_clientStub.startCM();
        if(!bRet) {
            JOptionPane.showMessageDialog(null, "There's No CM Server to Connect", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            System.err.println("CM initialization error!");
            return;
        }
        refreshUserList();
        refreshFileList();
        printAllMenus();
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

    public void refreshUserList()   {
        DefaultListModel<String> model = new DefaultListModel();
        Vector<CMUser> curUserList = this.m_clientStub.getGroupMembers().getAllMembers();
        Iterator<CMUser> iter = curUserList.iterator();
        if (curUserList.isEmpty())  {
            model.addElement("Empty List");
            this.m_userJList.setModel(model);
            return;
        }
        while(iter.hasNext())   {
            model.addElement(iter.next().getName());
        }
        this.m_userJList.setModel(model);

    }
    public void refreshFileList() throws IOException {
        DefaultListModel<String> model = new DefaultListModel();
        HashMap<Path, FileTime> recentFileList = new HashMap<>();
        Path defaultFilePath = this.m_clientStub.getTransferedFileHome();
        this.curFileList = new HashMap<>();
        List<Path> result;
        result = Files.walk(defaultFilePath).collect(Collectors.toList());
        for (Path path:result)  {
            if (path==defaultFilePath)
                continue;
            model.addElement(path.toString());
            FileTime lastModifiedTime = (FileTime) Files.getAttribute(path, "lastModifiedTime");
            recentFileList.put(path, lastModifiedTime);
        }
        m_fileJList.setModel(model);
        curFileList = recentFileList;
    }
    public void syncFiles() throws IOException {
        CMDummyEvent cme = new CMDummyEvent();

        cme.setType(0);
        cme.setDummyInfo("sync file request");
        //m_clientStub.send(cme);
        //List<FileTime> recentLastModifiedTimeList = new ArrayList<>();
        //for (Map.Entry<Path, FileTime> entrySet : curFileList.entrySet())   {

        //}
    }
    //about File Transfer
    public void requestFile()   {
        boolean bRet = m_clientStub.requestFile("데이터아키텍처 준전문가 가이드(2020.08.29.).pdf", "SERVER");
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

                String filename = file.getName();
                String filePath = file.getPath();
                String fileSender = m_clientStub.getMyself().getName();
                CMDummyEvent request_de = new CMDummyEvent();
                request_de.setType(CMInfo.CM_DUMMY_EVENT);
                request_de.setID(PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                request_de.setDummyInfo(filename+","+filePath+","+strReceiver+","+fileSender);
                request_de.setSender(m_clientStub.getMyself().getName());
                if (strReceiver!="SERVER")
                    m_clientStub.send(request_de, "SERVER");
                else {
                    printMsgln("======START to Push File===========");
                    boolean ret = m_clientStub.pushFile(filePath, "SERVER");
                    if (ret)
                        printMsgln("User[" + m_clientStub.getMyself().getName() + "] Successed to push File[" + file.getName() + "] to [Default Server]");
                    else
                        printMsgln("User[" + m_clientStub.getMyself().getName() + "] Failed to push File[" + file.getName() + "] to [Default Server]");
                }
            }
        }
    }
    public void setSavedFilePath()   {
        JTextField filepathField = new JTextField();
        Object[] message = {
                "Current File Path(Absolute Path): ", getSavedFilePath(),
                "Enter New File Path: ", filepathField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Set FilePath", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION) {
            String sNewPath = filepathField.getText();
            Path newPath = Paths.get(filepathField.getText());
            m_clientStub.setTransferedFileHome(newPath);
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
            else if (button.getText().equals("Refresh UserList"))   {
                refreshUserList();
            }
            else if (button.getText().equals("Refresh FileList"))   {
                try {
                    refreshFileList();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if (button.getText().equals("Sync Files")) {
                try {
                    syncFiles();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if (button.getText().equals("Push Files")) {
                pushFile();
            }
            else if (button.getText().equals("Set File Path"))  {
                setSavedFilePath();
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
