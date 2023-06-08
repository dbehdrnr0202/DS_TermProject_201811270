import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CMServerApp extends JFrame{
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;
    private JTextPane m_outTextPane;
    private JTextPane m_fileListPane;
    private JTextPane m_userListPane;
    private JButton m_startStopButton;
    private JButton m_refreshUserListButton;
    private JButton m_refreshFileListButton;
    private JList curGroupUserList;
    private JList m_userJList;
    private JList m_fileJList;
    private String selectedUser = null;
    private final int PRINTALLMENU = 0;
    private final int TERMINATECM = 9;
    private final int PRINTCURRENTUSERS = 3;
    private final int MANAGECURRENTUSERS = 31;

    private final int SETFILEPATH = 4;
    private final int PUSHFILE = 61;
    private final int REQUESTFILE = 60;
    //파일명, timestamp pair의 hashmap

    public CMServerApp() throws IOException {
        makeUI();
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub, this);
    }
    public void makeUI(){
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
        m_refreshFileListButton = new JButton("Refresh FileList");
        m_refreshFileListButton.addActionListener(cmActionListener);
        m_refreshFileListButton.setEnabled(true);

        fileListPanel.add(m_refreshFileListButton);
        fileListPanel.add(m_fileJList);
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

        add(topButtonPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.add(fileListPanel, BorderLayout.WEST);
        panel.add(userListPanel, BorderLayout.EAST);
        panel.add(m_outTextPane, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        //pack();
        setVisible(true);

        /*
        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();
        selectedUser = null;
        setTitle("CM Server");
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

        m_startStopButton = new JButton("Stop Server CM");
        m_startStopButton.addActionListener(cmActionListener);
        m_startStopButton.setEnabled(true);
        //add(startStopButton, BorderLayout.NORTH);
        topButtonPanel.add(m_startStopButton);

        setVisible(true);
        */
    }
    public CMServerStub getServerStub()   {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args) throws IOException {
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        server.startCM();
    }
    //start/terminate CM
    public void startCM()   {
        //start CM as a default session
        boolean bRet = m_serverStub.startCM();
        if(!bRet) {
            System.err.println("CM initialization error!");
            return;
        }
    }
    public void terminateCM()   {
        int option  = JOptionPane.showConfirmDialog(null, "Really Want to Terminate CM?", "[TerminateCM]Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            m_serverStub.terminateCM();
        }
    }
    //about Current Login Users
    public void printCurrentUsers() {
        printStyledMsgln("[printCurrentUsers]", "bold");
        CMMember loginUsers = m_serverStub.getLoginUsers();
        if (loginUsers==null)   {
            System.err.println("Empty User List");
            return;
        }
        printMsgln("Total Users: "+loginUsers.getMemberNum());
        Vector<CMUser> vLoginUsers = loginUsers.getAllMembers();
        Iterator<CMUser> iter = vLoginUsers.iterator();
        int uCnt = 1;
        while(iter.hasNext())   {
            CMUser uTemp = iter.next();
            printMsgln("Member#"+(uCnt++)+": "+uTemp.getName());
        }
        printStyledMsgln("=====================================", "bold");
    }
    public void manageCurrentUsers()    {
        MyActionListener cmActionListener = new MyActionListener();
        if (!updateGroupUserList())
            return;
        JFrame jf = new JFrame("manage Current Users");
        final JPanel jl = new JPanel();

        JButton jButtonUserInfo = new JButton("User Info");
        JButton jButtonLogOut = new JButton("LogOut User");
        jButtonLogOut.addActionListener(cmActionListener);
        jButtonUserInfo.addActionListener(cmActionListener);

        curGroupUserList.addListSelectionListener(cmActionListener);
        jf.setSize(500, 500);
        jf.setLayout(new BorderLayout());
        jl.add(curGroupUserList);
        jl.add(jButtonUserInfo);
        jl.add(jButtonLogOut);
        jf.add(jl);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    public boolean updateGroupUserList()   {
        CMMember loginUsers = m_serverStub.getLoginUsers();
        if (loginUsers==null)   {
            JOptionPane.showMessageDialog(null, "There's No Users in Current Server", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Vector<CMUser> vLoginUsers = loginUsers.getAllMembers();
        Iterator<CMUser> iter = vLoginUsers.iterator();
        List<String> curGroupUserlist = new ArrayList<>();
        while(iter.hasNext())   {
            curGroupUserlist.add(iter.next().getName());
        }
        curGroupUserList = new JList(curGroupUserlist.toArray());
        return true;
    }

    //about File Transfer
    public void setFilePath() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printStyledMsgln("[setFilePath Starts]", "bold");
        String strNewFilePath = null;
        printMsg("file path: ");
        String curFilePath = (m_serverStub.getTransferedFileHome()).toString();
        JTextField newFilePathField = new JTextField();
        Object[] message = {
                "current File Path: ", curFilePath,
                "Enter new File Path: ", newFilePathField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Login Info", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            strNewFilePath = newFilePathField.getText();
            boolean ret = m_serverStub.setTransferedFileHome(Paths.get(strNewFilePath));
            if (ret)
                JOptionPane.showMessageDialog(null, "Server's File Path is now ["+strNewFilePath+"] (Before"+curFilePath+")");
            else
                JOptionPane.showMessageDialog(null, "setFilePath Failed", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
        printStyledMsgln("[setFilePath Done]", "bold");
        printStyledMsgln("=====================================", "bold");
    }
    /*
    public void requestFile()   {
        String strFileName = null;
        String strFileOwner = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printStyledMsgln("[requestFile Starts]", "bold");
        try {
            printMsg("File name: ");
            strFileName = br.readLine();
            printMsg("File owner(user name): ");
            strFileOwner = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bReturn = m_serverStub.requestFile(strFileName, strFileOwner);
        if(!bReturn)
            System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");

        printStyledMsgln("[requestFile Done]", "bold");
        printStyledMsgln("=====================================", "bold");
    }
    public void pushFile()  {
        boolean bReturn = false;
        String strFilePath = null;
        String strReceiver = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printStyledMsgln("[pushFile Starts]", "bold");

        try {
            printMsgln("File path name: ");
            strFilePath = br.readLine();
            printMsgln("File receiver (user name): ");
            strReceiver = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bReturn = m_serverStub.pushFile(strFilePath, strReceiver);
        if(!bReturn)
            JOptionPane.showMessageDialog(null, "Push file error! file("+strFilePath+"), receiver("+strReceiver+")", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        else printStyledMsgln("[pushFile Done]", "bold");
        printStyledMsgln("=====================================", "bold");
    }
    */
    //about Processing Inputs/Outputs
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
    public void refreshUserList()   {
        DefaultListModel<String> model = new DefaultListModel();
        CMMember curUserList = this.m_serverStub.getLoginUsers();
        Iterator<CMUser> iter = curUserList.getAllMembers().iterator();
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

        Path defaultFilePath = this.m_serverStub.getTransferedFileHome();
        List<Path> result;
        result = Files.walk(defaultFilePath).collect(Collectors.toList());

        for (Path path:result) {
            if (path == defaultFilePath)
                continue;
            model.addElement(path.toString());
            m_fileJList.setModel(model);
        }
    }
    //Listener classes
    public class MyActionListener implements ActionListener, ListSelectionListener {
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("Start Server CM")) {
                // start cm
                boolean bRet = m_serverStub.startCM();
                if(!bRet)
                    printStyledMsg("CM initialization error!\n", "bold");
                else {
                    printStyledMsg("Server CM starts.\n", "bold");
                    printMsg("Type 0 for menu.\n");
                    button.setText("Stop Server CM");
                }
                if(CMConfigurator.isDServer(m_serverStub.getCMInfo())) {
                    setTitle("CM Default Server (SERVER)");
                }
            }
            else if(button.getText().equals("Stop Server CM")) {
                m_serverStub.terminateCM();
                printStyledMsgln("Server CM terminates.\n", "bold");
                button.setText("Start Server CM");
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
            else if (button.getText().equals("LogOut User"))    {
                CMSessionEvent cse = new CMSessionEvent();
                cse.setID(CMSessionEvent.LOGOUT);
                cse.setType(CMInfo.CM_SESSION_EVENT);
                cse.setChannelName("Default Server");
                if (m_serverStub.send((CMEvent) cse, selectedUser)) {
                    CMDataEvent de = new CMDataEvent();
                    CMUser cmUserTemp = m_serverStub.getLoginUsers().findMember(selectedUser);
                    de.setID(CMDataEvent.REMOVE_USER);
                    de.setType(CMInfo.CM_DATA_EVENT);
                    de.setUserName(selectedUser);
                    de.setHandlerGroup(cmUserTemp.getCurrentGroup());
                    de.setHandlerSession(cmUserTemp.getCurrentSession());
                    de.setSender("ADMIN");
                    m_serverStub.getLoginUsers().removeMember(selectedUser);

                    m_serverStub.broadcast(de);
                }

                else System.out.println("SENDING FAILED MANAGING FAILED");
                updateGroupUserList();
            }
            else if (button.getText().equals("User Info"))  {
                CMUser CMTemp = m_serverStub.getLoginUsers().findMember(selectedUser);
                Object message[] = {
                        CMTemp.getName(),
                        CMTemp.getPasswd()
                };
                updateGroupUserList();
                int option = JOptionPane.showConfirmDialog(null, message, "Login Info", JOptionPane.OK_CANCEL_OPTION);
            }
            else if (button.getText().equals("Cancel")) {
                return;
            }

        }
        public void valueChanged(ListSelectionEvent e)  {
            JList list = (JList) e.getSource();
            if (list.getSelectedIndex()!=-1)    {
                selectedUser = (String) list.getSelectedValue();
            }
        }
    }

}