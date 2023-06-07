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
import java.awt.desktop.AboutHandler;
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
    private JButton m_logInOutButton;
    private JButton m_refreshUserListButton;
    private JButton m_refreshFileListButton;
    private JButton m_syncFileButton;
    private JButton m_pushFileButton;
    private JButton m_setPathButton;
    private JList m_userJList;
    private JList m_fileJList;
    public HashMap<String, FileTimeInfo> fileLogicalClock;
    public HashMap<String, Integer> fileLogicalClock2;
    Set<String> filesToDeleteMap;
    Set<String> filesToSendMap;
    private String savedFilePath;
    public static class FileTimeInfo{
        public long lastModifiedTime;
        public int logicalTime;
        public FileTimeInfo(long modifiedTime, int logicalTime) {
            this.lastModifiedTime = modifiedTime;
            this.logicalTime = logicalTime;
        }
    }
    private final int PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -1;
    private final int SEND_TIME_INFO = -9;
    private final int REQUEST_TIME_INFO = -10;
    private final int REQUEST_DELETE_FILE = -55;


    //private final int
    public CMClientApp(){
        makeUI();
        fileLogicalClock = new HashMap<>();
        fileLogicalClock2 = new HashMap<>();
        filesToDeleteMap = new HashSet<>();
        filesToSendMap = new HashSet<>();
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
        MyActionListener cmActionListener = new MyActionListener();

        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setSize(600, 400);
        m_outTextPane.setEditable(false);
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
        fileListPanel.setSize(100, 200);
        fileListPanel.setSize(500, 200);
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

        //JScrollPane scroll = new JScrollPane(m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane scroll = new JScrollPane(m_outTextPane);
        JScrollPane user_scroll = new JScrollPane(m_fileListPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane file_scroll = new JScrollPane(m_userListPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);
        userListPanel.add(user_scroll);
        fileListPanel.add(file_scroll);

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


    //start/terminate CM
    public void startCM() throws IOException {
        boolean bRet = m_clientStub.startCM();
        if(!bRet) {
            JOptionPane.showMessageDialog(null, "There's No CM Server to Connect", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            System.err.println("CM initialization error!");
            return;
        }
        //refreshUserList();
        //refreshFileList();
    }
    //about Login, Logout
    public boolean login() throws IOException {
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
                refreshUserList();
                refreshFileList();
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

        Set<String> tempMap = new HashSet<>();
        Path defaultFilePath = this.m_clientStub.getTransferedFileHome();
        List<Path> result;
        result = Files.walk(defaultFilePath).collect(Collectors.toList());

        for (Path path:result)  {
            if (path==defaultFilePath)
                continue;
            model.addElement(path.toString());
            tempMap.add(path.getFileName().toString());

            FileTime lastModifiedTime = (FileTime) Files.getAttribute(path, "lastModifiedTime");
            FileTimeInfo timeInfo = new FileTimeInfo(lastModifiedTime.toMillis(), 0);
            //recentFileList.put(path, lastModifiedTime);
            String filename = path.getFileName().toString();
            if (!fileLogicalClock.isEmpty()) {
                //프로그램을 실행한 이후
                if (fileLogicalClock.get(filename)!=null)  {
                    //파일이 로컬에서 수정되었을 경우
                    long l1 = fileLogicalClock.get(filename).lastModifiedTime;
                    long l2 = lastModifiedTime.toMillis();
                    if (l1<l2) {
                        FileTimeInfo savedValue = fileLogicalClock.get(filename);
                        savedValue.logicalTime +=1;
                        savedValue.lastModifiedTime = lastModifiedTime.toMillis();
                        fileLogicalClock.put(filename, savedValue);
                        fileLogicalClock2.put(filename, savedValue.logicalTime);
                        filesToSendMap.add(filename);
                    }
                }
                else {
                    //파일이 새로 생성되었을 경우
                    fileLogicalClock.put(filename, timeInfo);
                    fileLogicalClock2.put(filename, timeInfo.logicalTime);
                    filesToSendMap.add(filename);
                }
            }
            else {
                //처음 실행할 경우
                fileLogicalClock.put(path.getFileName().toString(), timeInfo);
                fileLogicalClock2.put(path.getFileName().toString(), timeInfo.logicalTime);
                filesToSendMap.add(filename);
            }
        }
        Iterator<String> setIter = fileLogicalClock.keySet().iterator();
        while(setIter.hasNext())    {
            String currentfile = setIter.next();
            if(!tempMap.contains(currentfile))   {
                filesToDeleteMap.add(currentfile);
            }
        }
        Iterator<String> delIter = filesToDeleteMap.iterator();
        while(delIter.hasNext())    {
            String name = delIter.next();
            fileLogicalClock.remove(name);
            fileLogicalClock2.remove(name);
            filesToSendMap.remove(name);
        }
        m_fileJList.setModel(model);
    }
    public static boolean isFileExists(File file) {
        return file.exists() && !file.isDirectory();
    }

    public void requestDelFile(String delFileName)  {
        requestDelFile(delFileName, "SERVER");
    }
    public void requestDelFile(String delFileName, String recevier) {
        CMDummyEvent de = new CMDummyEvent();
        de.setSender(m_clientStub.getMyself().getName());
        de.setReceiver(recevier);
        de.setID(REQUEST_DELETE_FILE);
        de.setDummyInfo(delFileName);
        m_clientStub.send(de, recevier);
    }
    public void syncFiles() throws IOException {
        Object[] message = {
                "Really Sync Files?"
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Sync Files Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION) {
            //전송할 파일들 먼저 시작하기
            Iterator<String> sendIter = filesToSendMap.iterator();
            while(sendIter.hasNext())   {
                String sendFileName = sendIter.next();
                sendTimeInfo("SERVER", sendFileName, fileLogicalClock.get(sendFileName).logicalTime);
            }
            Object[] confirmMessage = {
                    "This process will remove server's files\n confirm?"
            };
            option = JOptionPane.showConfirmDialog(null, confirmMessage, "Remove Server's Files Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (option==JOptionPane.OK_OPTION)  {
                Iterator<String> delIter = filesToDeleteMap.iterator();
                while(delIter.hasNext())   {
                    String delFileName = delIter.next();
                    requestDelFile(delFileName);
                }
            }
        }
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
        String strReceiver = null;
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
                long modifiedTime = file.lastModified();
                int logicalTime = 0;
                CMDummyEvent request_de = new CMDummyEvent();
                request_de.setType(CMInfo.CM_DUMMY_EVENT);
                request_de.setID(PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                request_de.setDummyInfo(filename+","+filePath+","+strReceiver+","+fileSender+","+modifiedTime+","+logicalTime);
                request_de.setSender(m_clientStub.getMyself().getName());
                FileTimeInfo timeInfo = new FileTimeInfo(modifiedTime, logicalTime);
                fileLogicalClock.put(filename, timeInfo);
                fileLogicalClock2.put(filename, timeInfo.logicalTime);
                //실 수신자에게 시간 정보 전달
                //sendTimeInfo(strReceiver, filename, modifiedTime, logicalTime);
                if (strReceiver!="SERVER") {
                    //SERVER가 아닐 경우(서버를 경유해서 파일 전송)
                    //sendTimeInfo("SERVER", filename, modifiedTime, logicalTime);
                    m_clientStub.send(request_de, "SERVER");
                }
                else {//strReceiver=="SERVER"(서버에 파일 전송)
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
    public int requestTimeInfo(String filename, String receiver)  {
        CMDummyEvent de = new CMDummyEvent();
        de.setDummyInfo(filename);
        de.setID(REQUEST_TIME_INFO);
        de.setSender(m_clientStub.getMyself().getName());
        m_clientStub.send(de, receiver);
        return 0;
    }
    public boolean sendTimeInfo(String receiver, String filename, int logicalTime)  {
        CMDummyEvent de = new CMDummyEvent();
        de.setDummyInfo(filename+","+logicalTime);
        de.setID(SEND_TIME_INFO);
        de.setSender(m_clientStub.getMyself().getName());
        de.setReceiver(receiver);
        boolean rtn = m_clientStub.send(de, receiver);
        return rtn;
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
    public class MyActionListener implements ActionListener, ListSelectionListener {
        public void actionPerformed(ActionEvent e)  {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("LogIn to Default CM Server")) {
                try {
                    if (login()) {
                        printStyledMsg("Client CM starts.\n", "bold");
                        button.setText("LogOut from Default CM Server");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
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
