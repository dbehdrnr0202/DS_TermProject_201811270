import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.handler.CMEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.event.*;

import java.util.Iterator;

public class CMClientEventHandler implements CMEventHandler {
    private CMClientStub m_clientStub;
    public CMClientEventHandler(CMClientStub stub) {
        m_clientStub = stub;
    }
    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
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
            System.err.println("This client fails authentication by the default server!");
        }
        //-1: same user already logged in
        else if(se.isValidUser() == -1) {
            System.err.println("This client is already in the login-user list!");
        }
        //1: valied user
        else if (se.isValidUser()==1){
            System.out.println("This client successfully logs in to the default server.");
        }
        else {
            System.err.println("Wrong isValidUser() rtn.");
        }

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
}

