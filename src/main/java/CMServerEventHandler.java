
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;

import java.util.Iterator;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    public CMServerEventHandler(CMServerStub serverStub)    {
        m_serverStub = serverStub;
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
        System.out.println("[processDummyEvent]");
        System.out.println(de.getHandlerSession()+", "+de.getHandlerGroup());
        System.out.println("Dummy msg: "+de.getDummyInfo());
    }
    private void processUserEvent(CMEvent cme)  {
        CMUserEvent ue = (CMUserEvent) cme;
        System.out.println("[processUserEvent]");
        //
        switch (ue.getID()) {
            default:
                break;
        }
    }
    private void processFileEvent(CMEvent cme)  {
        CMFileEvent fe = (CMFileEvent) cme;
        System.out.println("[processFileEvent]"+fe.getID());
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
                //
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
        System.out.println("[procesSessionEvent]");
        switch (se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("["+se.getUserName()+"] requests login.");

                //여기서 cm-server.conf에서 SESSION_SCHEME 0으로 설정했다.=> No authorization
                if (confInfo.isLoginScheme())   {
                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), m_serverStub.getCMInfo());
                    if (!ret)   {
                        System.out.println("["+se.getUserName()+"] authentication fails");
                        m_serverStub.replyEvent(se, 0);
                    }
                    else {
                        System.out.println("["+se.getUserName()+"] authentication succeeded");
                        m_serverStub.replyEvent(se, 1);
                    }
                }
                else {
                    System.out.println("SESSION_SCHEME IS 0, NO LOGIN SCHEME");
                    //System.out.println("["+se.getUserName()+"] login succeeded");
                }
                break;
            case CMSessionEvent.LOGOUT:
                System.out.println("["+se.getUserName()+"] requests logout.");
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                System.out.println("["+se.getUserName()+"] requests session Info.");
                break;
            case CMSessionEvent.REGISTER_USER:
                System.out.println("User registration requested by user["+se.getUserName()+"].");
                break;
            case CMSessionEvent.LEAVE_SESSION:
                System.out.println("["+se.getUserName()+"] leaves the "+se.getSessionName());
            case CMSessionEvent.JOIN_SESSION:
                System.out.println("["+se.getUserName()+"] joins the "+se.getSessionName());
                /*need more */
            default:
                return;
        }
    }
}
