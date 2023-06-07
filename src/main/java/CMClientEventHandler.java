import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.nio.file.Path;
import java.util.Iterator;

public class CMClientEventHandler implements CMAppEventHandler {
    private final int ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -11;
    private final int PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -2;
    private final int ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -21;
    private final int START_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -22;
    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER = -3;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER = -31;
    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -4;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -41;
    private final int END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -5;
    private final int ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2 = -51;
    private final int SEND_TIME_INFO = -9;
    private final int SEND_TIME_INFO_MODIFIED = -91;
    private final int SEND_TIME_INFO_NOT_MODIFIED = -92;
    private final int REQUEST_TIME_INFO = -10;
    private final int REQUEST_DELETE_FILE = -55;
    private final int REQUEST_DELETE_FILE_ACK = -551;
    private boolean isProccessingFile;

    private String processingFileInfo;
    private CMClientStub m_clientStub;
    private CMClientApp m_client;
    public class FileTimeInfo{
        public long lastModifiedTime;
        public int logicalTime;
        public FileTimeInfo(long modifiedTime, int logicalTime) {
            this.lastModifiedTime = modifiedTime;
            this.logicalTime = logicalTime;
        }
    }

    public CMClientEventHandler(CMClientStub stub, CMClientApp client) {
        m_clientStub = stub;
        m_client = client;
        this.isProccessingFile = false;
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
            /*
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            */
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme) {
        CMDummyEvent de = (CMDummyEvent) cme;
        int eventId = de.getID();
        switch (eventId)    {
            case SEND_TIME_INFO_MODIFIED:
                printMsg("SEND_TIME_INFO_MODIFIED");
                printMsg("서버가 내 파일의 정보로 갱신함");
                String filepath = m_clientStub.getTransferedFileHome()+"\\"+de.getDummyInfo().split(",")[0];
                m_clientStub.pushFile(filepath, "SERVER");
                return;
            case SEND_TIME_INFO_NOT_MODIFIED:
                printMsg("SEND_TIME_INFO_NOT_MODIFIED");
                printMsg("서버의 파일의 정보이 더 최신이라 client의 clock을 갱신함");
                String recvFileName = de.getDummyInfo().split(",")[0];
                int recvLogicalClock = Integer.parseInt(de.getDummyInfo().split(",")[1]);
                m_clientStub.requestFile(recvFileName, "SERVER");
                this.m_client.fileLogicalClock2.put(recvFileName, recvLogicalClock);
                return;
            case REQUEST_DELETE_FILE_ACK:
                printMsg("REQUEST_DELETE_FILE_ACK");
                printMsg("SERVER deleted file: "+de.getDummyInfo());
                m_client.filesToDeleteMap.remove(de.getDummyInfo());
                return;
            default://file transfer via server event
                break;
        }
        CMDummyEvent send_de = new CMDummyEvent();
        send_de.setType(CMInfo.CM_DUMMY_EVENT);
        System.out.println("[processDummyEvent]");
        send_de.setDummyInfo(de.getDummyInfo());
        String filename = de.getDummyInfo().split(",")[0];
        String filePath = de.getDummyInfo().split(",")[1];
        String receiver = de.getDummyInfo().split(",")[2];
        String fileSender =de.getDummyInfo().split(",")[3];
        String sender = de.getSender();
        switch (de.getID()) {
            case ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                this.isProccessingFile = true;
                this.processingFileInfo = de.getDummyInfo();
                printMsg("ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                printMsg("start to send file: "+filename);
                m_clientStub.pushFile(filePath, "SERVER");
                break;
            case ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                this.isProccessingFile = false;
                printMsg("ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                send_de.setID(START_PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                m_clientStub.send(send_de, "SERVER");
                break;
            case PUSH_FILE_TO_CLIENT_VIA_SERVER_2:
                this.isProccessingFile = true;
                printMsg("PUSH_FILE_TO_CLIENT_VIA_SERVER_2");
                send_de.setID(ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                m_clientStub.send(send_de, "SERVER");
                int recvLogicalTime = Integer.parseInt(de.getDummyInfo().split(",")[4]);
                CMClientApp.FileTimeInfo info = new CMClientApp.FileTimeInfo(0, recvLogicalTime);
                m_client.fileLogicalClock.put(filename, info);
                this.processingFileInfo = de.getDummyInfo();
                break;
            case END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                printMsg("END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                send_de.setID(ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                m_clientStub.send(send_de, "SERVER");
                break;
            case END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2:
                this.isProccessingFile = false;
                printMsg("END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2");
                send_de.setID(ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                m_clientStub.send(send_de, "SERVER");
                break;
            case END_PUSH_FILE_TO_CLIENT_VIA_SERVER:
                printMsg("END_PUSH_FILE_TO_CLIENT_VIA_SERVER");
                printMsg("===PUSH_FILE_TO_CLIENT_VIA_SERVER is Done===");
                printMsg("============================================");
                send_de.setID(ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER);
                m_clientStub.send(send_de, "SERVER");
                break;
            default:
                break;
        }
        //printMsg(de.getHandlerSession()+", "+de.getHandlerGroup());

        //printMsg("Dummy Sender: "+de.getSender());
        //printMsg("Dummy msg: "+de.getDummyInfo());
    }
    /*
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
    */
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
                printMsg("REPLY_PERMIT_PULL_FILE");
                if(fe.getReturnCode() == -1) {
                    System.err.println("[FILE_EVENT]"+fe.getFileName()+" does not exist in the owner!");
                }
                else if(fe.getReturnCode() == 0) {
                    System.err.println("[FILE_EVENT]"+fe.getFileSender()+" rejects to send file("+fe.getFileName()+").");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                printMsg("REQUEST_PERMIT_PUSH_FILE");
                if (m_clientStub.replyEvent(fe, 1))
                    printMsg("User["+fe.getFileReceiver()+"] Accepted to Permit PUSH FILE");
                else
                    printMsg("User["+fe.getFileReceiver()+"] Accepted to Permit PUSH FILE but replyEvent Failed");
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                printMsg("REPLY_PERMIT_PUSH_FILE");
                if (fe.getReturnCode()==1) {
                    printMsg("[FILE_EVENT]"+fe.getFileReceiver()+" Accepted to receive File["+fe.getFileName()+"]");
                }
                else if (fe.getReturnCode()==0)    {
                    printMsg("[FILE_EVENT]"+fe.getFileReceiver()+" Rejected to receive File["+fe.getFileName()+"]");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            //case CMFileEvent.START_FILE_TRANSFER_CHAN:
                printMsg("START_FILE_TRANSFER");
                m_clientStub.replyEvent(fe, 1);
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            //case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMsg("END_FILE_TRANSFER");
                m_clientStub.replyEvent(fe, 1);
                break;
            case CMFileEvent.CONTINUE_FILE_TRANSFER:
                String info = fe.getFileName();
                break;
            case CMFileEvent.END_FILE_TRANSFER_ACK:
                printMsg("END_FILE_TRANSFER_ACK");
            //case CMFileEvent.END_FILE_TRANSFER_CHAN_ACK:
                if (isProccessingFile==true)    {
                    CMDummyEvent de = new CMDummyEvent();
                    de.setType(CMInfo.CM_DUMMY_EVENT);
                    de.setID(END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                    de.setDummyInfo(this.processingFileInfo);
                    de.setSender(m_clientStub.getMyself().getName());
                    m_clientStub.send(de, "SERVER");
                    m_client.printMsgln("First. User[" + m_clientStub.getMyself().getName() + "] Successed to push File[" + processingFileInfo.split(",")[0] + "] to [Default Server]");
                }
                printMsg("[FILE_EVENT]"+fe.getFileReceiver()+" completes to receive file(" +fe.getFileName()+", "+fe.getFileSize()+" Bytes) from "+fe.getFileSender());
                printMsg("========START to send END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                break;
            default:
                break;
        }
    }

    private void processSessionEvent(CMEvent cme) {
        CMSessionEvent se = (CMSessionEvent)cme;
        System.out.println("[procesSessionEvent]ID: "+se.getID());
        switch(se.getID()) {
            case CMSessionEvent.LOGIN_ACK:
                processLOGIN_ACK(se);
                break;
            case CMSessionEvent.LOGOUT:
                processLOGOUT_ACK(se);
                break;
            default:
                return;
        }
    }

    private void processLOGIN_ACK(CMSessionEvent se)   {
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

    private void printMsg(String strText)   {
        m_client.printStyledMsgln(strText, "bold");
        return;
    }
}

