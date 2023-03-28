import kr.ac.konkuk.ccslab.cm.*;

public class CMServerApp {
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;

    public CMServerApp()    {
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub);
    }
    public CMServerStub getM_serverStub()   {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }
    public static void main(String[] args)  {
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = new CMServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        cmStub.startCM();
    }
}
