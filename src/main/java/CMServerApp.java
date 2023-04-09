import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Vector;

public class CMServerApp {
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;
    private boolean m_bRun;

    private final int PRINTALLMENU = 0;
    private final int STARTCM = 1;
    private final int TERMINATECM = 9;
    private final int PRINTCURRENTUSERS = 3;

    public CMServerApp()    {
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub);
    }
    public CMServerStub getServerStub()   {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public void terminateCM(){
        m_serverStub.terminateCM();
        m_bRun = false;
    }

    public static void main(String[] args)  {
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        server.startCM();

        //System.out.println("Server App terminated");
    }
    public void startCM()   {
        //start CM as a default session
        boolean bRet = m_serverStub.startCM();
        if(!bRet) {
            System.err.println("CM initialization error!");
            return;
        }
        m_bRun = true;
        startMainSession();
    }
    public void startMainSession()  {
        System.out.println("server application main session starts.");
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
                /*
                case STARTCM:
                    startCM();
                    break;
                */
                case PRINTCURRENTUSERS:
                    printCurrentUsers();
                    break;
                case TERMINATECM:
                    terminateCM();
                    break;
                default:
                    break;
            }
        }
    }
    public void printAllMenus() {
        System.out.println("Print All Menu: "+PRINTALLMENU);
        System.out.println("Start CM: "+STARTCM);
        System.out.println("Print Current Users: "+PRINTCURRENTUSERS);
        System.out.println("Terminate CM: "+TERMINATECM);
    }
    public void printCurrentUsers() {
        System.out.println("[printCurrentUsers]");
        CMMember loginUsers = m_serverStub.getLoginUsers();
        if (loginUsers==null)   {
            System.err.println("Empty User List");
            return;
        }
        System.out.println("Total Users: "+loginUsers.getMemberNum());
        Vector<CMUser> vLoginUsers = loginUsers.getAllMembers();
        Iterator<CMUser> iter = vLoginUsers.iterator();
        int uCnt = 1;
        while(iter.hasNext())   {
            CMUser uTemp = iter.next();
            System.out.println("Member#"+(uCnt++)+": "+uTemp.getName());
        }
    }

}