
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.Iterator;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    private CMServerApp m_server;
    public CMServerEventHandler(CMServerStub serverStub, CMServerApp server)    {
        m_serverStub = serverStub;
        m_server = server;
    }
    @Override
    public void processEvent(CMEvent cme)   {
        switch (cme.getType())  {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_DUMMY_EVENT:
                processDummyEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme) {
        CMDummyEvent de = (CMDummyEvent) cme;
        printMsg("[processDummyEvent]");
        printMsg(de.getHandlerSession()+", "+de.getHandlerGroup());
        printMsg("Dummy msg: "+de.getDummyInfo());
    }
    private void processUserEvent(CMEvent cme)  {
        CMUserEvent ue = (CMUserEvent) cme;
        printMsg("[processUserEvent]");
        //
        switch (ue.getID()) {

            default:
                break;
        }
    }
    private void processFileEvent(CMEvent cme)  {
        CMFileEvent fe = (CMFileEvent) cme;
        printMsg("[processFileEvent]"+fe.getID());
        switch (fe.getID()) {
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if(fe.getReturnCode() == -1) {
                    System.err.print("["+fe.getFileName()+"] does not exist in the owner!\n");
                }
                else if(fe.getReturnCode() == 0) {
                    System.err.print("["+fe.getFileSender()+"] rejects to send file("+fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                if(fe.getReturnCode() == 0) {
                    System.err.print("[" + fe.getFileReceiver() + "] rejected the push-file request!\n");
                    System.err.print("file path(" + fe.getFilePath() + "), size(" + fe.getFileSize() + ").\n");
                }
                break;
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMsg("["+fe.getFileSender()+"] completes to send file("+fe.getFileName()+", "+fe.getFileSize()+" Bytes).");
                String strFile = fe.getFileName();
                //if(m_bDistFileProc) {
               //     processFile(fe.getFileSender(), strFile);
                //    m_bDistFileProc = false;
                //}
                break;
            case CMFileEvent.END_FILE_TRANSFER_CHAN_ACK:
                //
                break;
            default:
                break;
        }
    }
    private  void processSessionEvent(CMEvent cme)  {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        printMsg("[procesSessionEvent]");
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
