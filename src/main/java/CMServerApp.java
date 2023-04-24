import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CMServerApp extends JFrame{
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;
    private boolean m_bRun;
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_startStopButton;
    private JList curGroupUserList;
    private DefaultListModel listModel;
    private String selectedUser = null;
    private final int PRINTALLMENU = 0;
    private final int STARTCM = 1;
    private final int TERMINATECM = 9;
    private final int PRINTCURRENTUSERS = 3;
    private final int MANAGECURRENTUSERS = 31;

    private final int SETFILEPATH = 4;
    private final int PUSHFILE = 61;
    private final int REQUESTFILE = 60;


    public CMServerApp()    {
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
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub, this);
        printAllMenus();
    }
    public CMServerStub getServerStub()   {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args)  {
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        server.startCM();

        //printMsg("Server App terminated");
    }

    //print menus
    public void printAllMenus() {
        printMsgln("Print All Menu: "+PRINTALLMENU);
        printMsgln("====About User====");
        printMsgln("Print Current Users: "+PRINTCURRENTUSERS);
        printMsgln("Manage Current Users: "+MANAGECURRENTUSERS);
        printMsgln("====About File Transfer====");
        printMsgln("Request File: "+REQUESTFILE);
        printMsgln("Push File: "+PUSHFILE);
        printMsgln("====About CM====");
        printMsgln("Terminate CM: "+TERMINATECM);
    }

    //start/terminate CM
    public void startCM()   {
        //start CM as a default session
        boolean bRet = m_serverStub.startCM();
        if(!bRet) {
            System.err.println("CM initialization error!");
            return;
        }
        m_bRun = true;
        //startMainSession();
    }
    public void terminateCM()   {
        int option  = JOptionPane.showConfirmDialog(null, "Really Want to Terminate CM?", "[TerminateCM]Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION)  {
            m_serverStub.terminateCM();
        }
    }
    /*
    public void startMainSession()  {
        printMsgln("server application main session starts.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String strInput = null;
        int nCommand = -1;
        while(m_bRun) {
            printMsgln("Type 0 for menu.");
            printMsg("> ");
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
                case SETFILEPATH:
                    setFilePath();
                    break;
                case PRINTCURRENTUSERS:
                    printCurrentUsers();
                    break;
                case MANAGECURRENTUSERS:
                    manageCurrentUsers();
                    break;
                case TERMINATECM:
                    terminateCM();
                    break;

                case REQUESTFILE:
                    requestFile();
                    break;
                case PUSHFILE:
                    pushFile();
                    break;
                default:
                    break;
            }
        }
    }*/

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
                /*
                case STARTCM:
                    startCM();
                    break;
                */
            case SETFILEPATH:
                setFilePath();
                break;
            case PRINTCURRENTUSERS:
                printCurrentUsers();
                break;
            case MANAGECURRENTUSERS:
                manageCurrentUsers();
                break;
            case TERMINATECM:
                terminateCM();
                break;
            /*
            case REQUESTFILE:
                requestFile();
                break;
            case PUSHFILE:
                pushFile();
                break;
            */
            default:
                break;
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
                processInput(strText);
                input.setText("");
                input.requestFocus();
            }
        }

        public void keyReleased(KeyEvent e){}
        public void keyTyped(KeyEvent e){}
    }
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
                    // change button to "stop CM"
                    button.setText("Stop Server CM");
                }
                // check if default server or not
                if(CMConfigurator.isDServer(m_serverStub.getCMInfo())) {
                    setTitle("CM Default Server (SERVER)");
                }
                m_inTextField.requestFocus();
            }
            else if(button.getText().equals("Stop Server CM")) {
                // stop cm
                m_serverStub.terminateCM();
                printStyledMsgln("Server CM terminates.\n", "bold");
                // change button to "start CM"
                button.setText("Start Server CM");
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