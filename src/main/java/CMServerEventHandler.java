
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
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
            default:
                return;
        }
    }

    private  void processSessionEvent(CMEvent cme)  {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
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
                    System.out.println("["+se.getUserName()+"] login succeeded");
                    m_serverStub.replyEvent(se, 1);
                }

                break;
            case CMSessionEvent.LOGOUT:
                System.out.println("["+se.getUserName()+"] requests logout.");
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                processREPONSE_SESSION_INFO(se);
                break;
                /*need more */
            default:
                return;
        }
    }
    private void processREPONSE_SESSION_INFO(CMSessionEvent se)    {
        Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();
        System.out.format("%-60s%n", "------------------------------------------------------------");
        System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
        System.out.format("%-60s%n", "------------------------------------------------------------");
        while(iter.hasNext()) {
            CMSessionInfo tInfo = iter.next();
            System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), tInfo.getPort(), tInfo.getUserNum());
        }
    }
}
