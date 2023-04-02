import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

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
        CMSessionEvent se = (CMSessionEvent) cme;
        switch (se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("["+se.getUserName()+"] requests login.");
                break;
            case CMSessionEvent.LOGOUT:
                System.out.println("["+se.getUserName()+"] requests logout.");
                break;
                /*need more */
            default:
                return;
        }
    }
}
