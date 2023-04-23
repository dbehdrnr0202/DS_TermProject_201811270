import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.event.*;

import java.util.Iterator;

public class CMClientEventHandler implements CMAppEventHandler {
    private CMClientStub m_clientStub;
    private CMClientApp m_client;
    public CMClientEventHandler(CMClientStub stub, CMClientApp client) {
        m_clientStub = stub;
        m_client = client;
    }
    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_DATA_EVENT:
                processDataEvent(cme);
                break;
            case CMInfo.CM_DUMMY_EVENT:
                processDummyEvent(cme);
                break;
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                return;
        }
    }
    private void processSessionEvent(CMEvent cme) {
        CMSessionEvent se = (CMSessionEvent)cme;
        switch(se.getID()) {
            case CMSessionEvent.LOGIN_ACK:
                processLOGIN_ACK(se);
                break;
            case CMSessionEvent.LOGOUT:
                processLOGOUT_ACK(se);
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                processRESPONSE_SESSION_INFO(se);
                break;
            default:
                return;
        }
    }
    private  void processLOGIN_ACK(CMSessionEvent se)   {
        //0: user authentication failed
        if(se.isValidUser() == 0)   {
            System.err.println("[SESSION_EVENT]This client fails authentication by the default server!");
        }
        //-1: same user already logged in
        else if(se.isValidUser() == -1) {
            System.err.println("[SESSION_EVENT]This client is already in the login-user list!");
        }
        //1: valid user
        else if (se.isValidUser()==1){
            printMsg("[SESSION_EVENT]This client successfully logs in to the default server.");
        }
        else {
            System.err.println("[SESSION_EVENT]Wrong isValidUser() rtn.");
        }
    }
    private void processLOGOUT_ACK(CMSessionEvent se)   {
        String serverName = se.getSender();
        printMsg("[SESSION_EVENT] removed From Server["+serverName+"] by Admin");
        m_clientStub.disconnectFromServer();
    }
    //Receiving session info
    private void processRESPONSE_SESSION_INFO(CMSessionEvent se)    {
        Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();
        System.out.format("%-60s%n", "------------------------------------------------------------");
        System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
        System.out.format("%-60s%n", "------------------------------------------------------------");
        while(iter.hasNext())   {
            CMSessionInfo tInfo = iter.next();
            System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(), tInfo.getUserNum());
        }
    }
    private void processDataEvent(CMEvent se)    {
        CMDataEvent de = (CMDataEvent)se;
        switch (de.getID()) {
            case CMDataEvent.NEW_USER:
                printMsg("[DATA_EVENT]New_User "+de.getUserName()+" joins the group "+de.getHandlerGroup()+" in "+de.getHandlerSession());
                break;
            case CMDataEvent.REMOVE_USER:
                printMsg("[DATA_EVENT]User "+de.getUserName()+" left the group "+de.getHandlerGroup()+" in "+de.getHandlerSession());
                break;
            default:
                return;
        }
    }
    private void processDummyEvent(CMEvent cme) {
        CMDummyEvent de = (CMDummyEvent)cme;
        printMsg("[DUMMY_EVENT]Dummy msg "+de.getDummyInfo()+" from user"+de.getSender());
        return;
    }

    private void processUserEvent(CMEvent cme)  {
        CMUserEvent ue = (CMUserEvent) cme;
        switch (ue.getStringID())   {
            case "userInfo":
                System.out.println("[USER_EVENT]ID: "+ue.getStringID());
                String name = ue.getEventField(CMInfo.CM_STR, "name");
                int age = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "age"));
                double weight = Double.parseDouble(ue.getEventField(CMInfo.CM_DOUBLE, "weight"));
                System.out.println("Field value: name: "+name);
                System.out.println("Field value: age: "+age);
                System.out.println("Field value: weight: "+weight);
                break;
            default:
                System.err.println("[USER_EVENT]unknown CMUserEvent ID: "+ue.getStringID());
        }
    }
    private void processFileEvent(CMEvent cme)  {
        CMFileEvent fe = (CMFileEvent) cme;
        System.out.println("[processFileEvent]"+fe.getID());
        switch (fe.getID()) {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                String strReq = "["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+ ").";
                printMsg(strReq);
                m_clientStub.replyEvent(fe, 1);
                break;
            //Checking out the result of the file transfer request
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if(fe.getReturnCode() == -1) {
                    System.err.println("[FILE_EVENT]"+fe.getFileName()+" does not exist in the owner!");
                }
                else if(fe.getReturnCode() == 0) {
                    System.err.println("[FILE_EVENT]"+fe.getFileSender()+" rejects to send file("+fe.getFileName()+").");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                printMsg("[FILE_EVENT]"+fe.getFileSender()+" is about to send file("+fe.getFileName()+").");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMsg("[FILE_EVENT]"+fe.getFileSender()+" completes to send file(" +fe.getFileName()+", "+fe.getFileSize()+" Bytes) to "+fe.getFileReceiver());
                break;
            default:
                break;
        }
    }
    private void printMsg(String strText)   {
        m_client.printStyledMsgln(strText, "bold");
        return;
    }
}

