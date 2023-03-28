import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

    public CMClientApp(){
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }
    public CMClientStub getClientStub() {
        return m_clientStub;
    }
    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }
    public static void main(String[] args)  {
        CMClientApp client = new CMClientApp();
        CMClientStub cmStub = new client.getClientStub();
        cmStub.setAppEventHandler(client.getClientEventHandler());
        cmStub.startCM();
    }
}
