
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import org.apache.commons.math3.util.Pair;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    private CMServerApp m_server;
    private final int PUSH_FILE_TO_CLIENT_VIA_SERVER_1 = -1;
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
    private final HashMap<PushEvent, Boolean> pushEventMap;
    private class PushEvent{
        public String filename;
        public String receiver;
        public String sender;
        public PushEvent(String filename, String receiver, String sender)  {
            this.filename = filename;
            this.receiver = receiver;
            this.sender = sender;
        }
    }
    public CMServerEventHandler(CMServerStub serverStub, CMServerApp server)    {
        m_serverStub = serverStub;
        m_server = server;
        pushEventMap = new HashMap<>();
    }
    @Override
    public void processEvent(CMEvent cme)   {
        switch (cme.getType())  {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_DUMMY_EVENT:
                try {
                    processDummyEvent(cme);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            /*
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            */
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme) throws InterruptedException {
        CMDummyEvent de = (CMDummyEvent) cme;
        CMDummyEvent send_de = new CMDummyEvent();
        send_de.setType(CMInfo.CM_DUMMY_EVENT);
        System.out.println("[processDummyEvent]");
        String filename = de.getDummyInfo().split(",")[0];
        String receiver = de.getDummyInfo().split(",")[2];
        String fileSender =de.getDummyInfo().split(",")[3];
        String filePath = m_serverStub.getTransferedFileHome().toString()+"\\"+fileSender+"\\"+filename;

        String sender = de.getSender();
        switch (de.getID()) {
            case PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                PushEvent pe = new PushEvent(filename, receiver, fileSender);
                pushEventMap.put(pe, false);
                printMsg("PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                send_de.setID(ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                send_de.setDummyInfo(de.getDummyInfo());
                m_serverStub.send(send_de, sender);
                break;
            case START_PUSH_FILE_TO_CLIENT_VIA_SERVER_2:
                printMsg("PUSH_FILE_TO_CLIENT_VIA_SERVER_2");
                send_de.setID(PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                send_de.setDummyInfo(filename+","+filePath+","+receiver+","+sender);
                m_serverStub.send(send_de, receiver);
                break;
            case ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_2:
                printMsg("ACK_PUSH_FILE_TO_CLIENT_VIA_SERVER_2");
                boolean ret = m_serverStub.pushFile("C:\\Users\\Hi\\IdeaProjects\\DistributedSystem\\CM_Maven\\CMApp\\server-file-path\\t2\\데이터아키텍처 준전문가 가이드(2020.08.29.).pdf", "t1");
                printMsg("server is pushing file to user result: "+ret);
                send_de.setID(END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                m_serverStub.send(send_de, receiver);
                break;
            case END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                printMsg("END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                send_de.setID(ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1);
                send_de.setDummyInfo(de.getDummyInfo());
                m_serverStub.send(send_de, sender);
                break;
           /*
            case ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1:
                printMsg("ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_1");
                send_de.setID(PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                m_serverStub.send(send_de, receiver);
                //printMsg("========START to send FILE"+filePath+filename);
                //send_de.setID(END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2);
                //m_serverStub.send(send_de, receiver);
                //m_serverStub.pushFile(filePath, receiver);
                break;
            */
            case ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2:
                printMsg("ACK_END_PUSH_FILE_TO_CLIENT_VIA_SERVER_2");
                send_de.setID(END_PUSH_FILE_TO_CLIENT_VIA_SERVER);
                send_de.setDummyInfo(de.getDummyInfo());
                m_serverStub.send(send_de, receiver);
                m_serverStub.send(send_de, fileSender);
                break;
        }
        //printMsg(de.getHandlerSession()+", "+de.getHandlerGroup());
        //printMsg("Dummy Sender: "+de.getSender());
        printMsg("Dummy msg: "+de.getDummyInfo());
    }
    /*
    private void processUserEvent(CMEvent cme)  {
        CMUserEvent ue = (CMUserEvent) cme;
        System.out.println("[processUserEvent]");
        //
        switch (ue.getID()) {

            default:
                break;
        }
    }
    */
    private void processFileEvent(CMEvent cme)  {
        CMFileEvent fe = (CMFileEvent) cme;
        System.out.println("[processFileEvent]"+fe.getID());
        boolean rtn;
        switch (fe.getID()) {
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                printMsg("REQUEST_PERMIT_PUSH_FILE");
                //printMsg("User["+fe.getFileSender()+"] Requests to Permit Push File["+fe.getFileName()+"]");
                rtn = m_serverStub.replyEvent(fe, 1);
                if (rtn) {
                    System.out.println("Server Accepted to Permit PUSH FILE");
                }
                else
                    System.out.println("Server Accepted to Permit PUSH FILE but replyEvent Failed");
                break;
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                printMsg("REPLY_PERMIT_PULL_FILE");
                if(fe.getReturnCode() == -1) {
                    System.err.print("["+fe.getFileName()+"] does not exist in the owner!\n");
                }
                else if(fe.getReturnCode() == 0) {
                    System.err.print("["+fe.getFileSender()+"] rejects to send file("+fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                printMsg("REPLY_PERMIT_PUSH_FILE");
                if(fe.getReturnCode() == 0) {
                    System.err.print("[" + fe.getFileReceiver() + "] rejected the push-file request!\n");
                    System.err.print("file path(" + fe.getFilePath() + "), size(" + fe.getFileSize() + ").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            //case CMFileEvent.START_FILE_TRANSFER_CHAN:
                printMsg("START_FILE_TRANSFER");
                rtn = m_serverStub.replyEvent(fe, 1);
                System.out.println(rtn);
                //printMsg("[FILE_EVENT]"+fe.getFileSender()+" is about to send file("+fe.getFileName()+").");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            //case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMsg("END_FILE_TRANSFER");
                rtn = m_serverStub.replyEvent(fe, 1);
                System.out.println(rtn);
                String filename = fe.getFileName();
                String receiver = fe.getFileReceiver();
                String sender = fe.getFileSender();
                //printMsg("[FILE_EVENT]"+fe.getFileSender()+" completes to send file(" +fe.getFileName()+", "+fe.getFileSize()+" Bytes) to "+fe.getFileReceiver());
                break;
            case CMFileEvent.END_FILE_TRANSFER_ACK:
                printMsg("END_FILE_TRANSFER_ACK");
            //case CMFileEvent.END_FILE_TRANSFER_CHAN_ACK:
                //printMsg("[FILE_EVENT]"+fe.getFileReceiver()+" completes to receive file(" +fe.getFileName()+", "+fe.getFileSize()+" Bytes) from "+fe.getFileSender());

            default:
                break;
        }
    }
    private  void processSessionEvent(CMEvent cme)  {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        System.out.println("[procesSessionEvent]ID: "+se.getID());
        switch (se.getID()) {
            case CMSessionEvent.LOGIN:
                printMsg("["+se.getUserName()+"] requests login.");

                //여기서 cm-server.conf에서 SESSION_SCHEME 0으로 설정했다.=> No authorization
                if (confInfo.isLoginScheme())   {
                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), m_serverStub.getCMInfo());
                    if (!ret)   {
                        printMsg("["+se.getUserName()+"] authentication fails");
                        m_serverStub.replyEvent(se, 0);
                    }
                    else {
                        printMsg("["+se.getUserName()+"] authentication succeeded");
                        m_serverStub.replyEvent(se, 1);
                    }
                }
                else {
                    System.out.println("SESSION_SCHEME IS 0, NO LOGIN SCHEME");
                    //printMsg("["+se.getUserName()+"] login succeeded");
                }
                break;
            case CMSessionEvent.LOGOUT:
                printMsg("["+se.getUserName()+"] requests logout.");
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                printMsg("["+se.getUserName()+"] requests session Info.");
                break;
            case CMSessionEvent.REGISTER_USER:
                printMsg("User registration requested by user["+se.getUserName()+"].");
                break;
            case CMSessionEvent.LEAVE_SESSION:
                printMsg("["+se.getUserName()+"] leaves the "+se.getSessionName());
            case CMSessionEvent.JOIN_SESSION:
                printMsg("["+se.getUserName()+"] joins the "+se.getSessionName());
                /*need more */
            default:
                return;
        }
    }
    private void printMsg(String strText)   {
        m_server.printStyledMsgln(strText, "bold");
        return;
    }
}
