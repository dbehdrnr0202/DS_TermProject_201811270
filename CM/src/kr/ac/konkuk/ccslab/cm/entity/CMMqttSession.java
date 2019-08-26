package kr.ac.konkuk.ccslab.cm.entity;

import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;

/**
 * The CMMqttSession class represents a MQTT session.
 * @author CCSLab, Konkuk University
 *
 */
public class CMMqttSession {
	// to store will message
	private CMMqttWill m_mqttWill;
	// list of determined subscriptions
	private CMList<CMMqttTopicQoS> m_subscriptionList;
	// list of requested subscriptions (4 client)
	private CMList<CMMqttTopicQoS> m_reqSubscriptionList;
	// list of sent QoS 1 and QoS 2 PUBLISH events that have not been completely acknowledged 
	// (4 client: client->server, 4 server: server->client)
	private CMList<CMMqttEventPUBLISH> m_sentUnAckPublishList;
	// list of received QoS 2 PUBLISH events that have not been completely acknowledged 
	// (4 client: client<-server, 4 server: server<-client)
	private CMList<CMMqttEventPUBLISH> m_recvUnAckPublishList;
	// list of received QoS 2 PUBREC events that have not been completely acknowledged 
	// (4 client: client<-server, 4 server: server<-client)
	private CMList<CMMqttEventPUBREC> m_recvUnAckPubrecList; 
	// list of pending QoS 1 and QoS 2 PUBLISH events of transmission to the client (4 server)
	private CMList<CMMqttEventPUBLISH> m_pendingTransPublishList;
	
	public CMMqttSession()
	{
		m_mqttWill = null;
		m_subscriptionList = new CMList<CMMqttTopicQoS>();
		m_reqSubscriptionList = null;
		m_sentUnAckPublishList = new CMList<CMMqttEventPUBLISH>();
		m_recvUnAckPublishList = new CMList<CMMqttEventPUBLISH>();
		m_recvUnAckPubrecList = new CMList<CMMqttEventPUBREC>();
		m_pendingTransPublishList = new CMList<CMMqttEventPUBLISH>();
	}
	
	//////////////////////// setter/getter
	
	public void setMqttWill(CMMqttWill mqttWill)
	{
		m_mqttWill = mqttWill;
	}
	
	public CMMqttWill getMqttWill()
	{
		return m_mqttWill;
	}
	
	// subscription list
	public void setSubscriptionList(CMList<CMMqttTopicQoS> subscriptionList)
	{
		m_subscriptionList = subscriptionList;
	}
	
	public CMList<CMMqttTopicQoS> getSubscriptionList()
	{
		return m_subscriptionList;
	}
	
	public void setReqSubscriptionList(CMList<CMMqttTopicQoS> reqSubscriptionList)
	{
		m_reqSubscriptionList = reqSubscriptionList;
	}
	
	public CMList<CMMqttTopicQoS> getReqSubscriptionList()
	{
		return m_reqSubscriptionList;
	}
	
	// sent-unack-publish list
	public void setSentUnAckPublishList(CMList<CMMqttEventPUBLISH> pubList)
	{
		m_sentUnAckPublishList = pubList;
	}
	
	public CMList<CMMqttEventPUBLISH> getSentUnAckPublishList()
	{
		return m_sentUnAckPublishList;
	}
	
	// recv-unack-publish list
	public void setRecvUnAckPublishList(CMList<CMMqttEventPUBLISH> pubList)
	{
		m_recvUnAckPublishList = pubList;
	}
	
	public CMList<CMMqttEventPUBLISH> getRecvUnAckPublishList()
	{
		return m_recvUnAckPublishList;
	}
	
	// recv-unack-pubrec list
	public void setRecvUnAckPubrecList(CMList<CMMqttEventPUBREC> recList)
	{
		m_recvUnAckPubrecList = recList;
	}
	
	public CMList<CMMqttEventPUBREC> getRecvUnAckPubrecList()
	{
		return m_recvUnAckPubrecList;
	}
	
	// pending-trans-publish list
	public void setPendingTransPublishList(CMList<CMMqttEventPUBLISH> eventList)
	{
		m_pendingTransPublishList = eventList;
	}
	
	public CMList<CMMqttEventPUBLISH> getPendingTransPublishList()
	{
		return m_pendingTransPublishList;
	}
	
	//////////////////////// subscription list
	
	public boolean addSubscription(CMMqttTopicQoS topicQoS)
	{
		return m_subscriptionList.addElement(topicQoS);
	}
	
	public CMMqttTopicQoS findSubscription(String strTopic)
	{
		for(CMMqttTopicQoS topicQoS : m_subscriptionList.getList())
		{
			if(topicQoS.getTopic().equals(strTopic))
				return topicQoS;
		}
		
		return null;
	}
	
	public boolean removeSubscription(String strTopic)
	{
		CMMqttTopicQoS topicQoS = findSubscription(strTopic);
		if(topicQoS == null)
			return false;
		
		return m_subscriptionList.removeElement(topicQoS);
	}
	
	public void removeAllSubscription()
	{
		m_subscriptionList.removeAllElements();
		return;
	}
	
	//////////////////////// sent-unack-publish list
	
	public boolean addSentUnAckPublish(CMMqttEventPUBLISH pubEvent)
	{
		int nID = pubEvent.getPacketID();
		CMMqttEventPUBLISH mqttEvent = findSentUnAckPublish(nID);
		if(mqttEvent != null)
		{
			System.err.println("CMMqttSession.addSentUnAckPublish(), the same packet ID ("
					+nID+") already exists!");
			System.err.println(mqttEvent.toString());
			return false;			
		}
		
		return m_sentUnAckPublishList.addElement(pubEvent);
	}
	
	public CMMqttEventPUBLISH findSentUnAckPublish(int nPacketID)
	{
		int nID = -1;
		for(CMMqttEventPUBLISH unackEvent : m_sentUnAckPublishList.getList())
		{
			nID = unackEvent.getPacketID();
			if(nID == nPacketID)
				return unackEvent;
		}
		
		return null;
	}
	
	public boolean removeSentUnAckPublish(int nPacketID)
	{
		CMMqttEventPUBLISH unackEvent = findSentUnAckPublish(nPacketID);
		if(unackEvent == null)
			return false;
		
		return m_sentUnAckPublishList.removeElement(unackEvent);
	}
	
	public void removeAllSentUnAckPublish()
	{
		m_sentUnAckPublishList.removeAllElements();
		return;
	}
	
	//////////////////////// recv-unack-publish list

	public boolean addRecvUnAckPublish(CMMqttEventPUBLISH pubEvent)
	{
		int nID = pubEvent.getPacketID(); 
		CMMqttEventPUBLISH unackEvent = findRecvUnAckPublish(nID);
		if(unackEvent != null)
		{
			System.err.println("CMMqttSession.addRecvUnAckPublish(), the same packet ID ("
					+nID+") already exists!");
			System.err.println(unackEvent.toString());
			
			return false;
		}
		
		return m_recvUnAckPublishList.addElement(pubEvent);
	}
	
	public CMMqttEventPUBLISH findRecvUnAckPublish(int nPacketID)
	{
		int nID = -1;
		for(CMMqttEventPUBLISH unackEvent : m_recvUnAckPublishList.getList())
		{
			nID = unackEvent.getPacketID();
			if(nID == nPacketID)
				return unackEvent;
		}
		
		return null;
	}
	
	public boolean removeRecvUnAckPublish(int nPacketID)
	{
		CMMqttEventPUBLISH unackEvent = findRecvUnAckPublish(nPacketID);
		if(unackEvent == null)
			return false;
		
		return m_recvUnAckPublishList.removeElement(unackEvent);
	}
	
	public void removeAllRecvUnAckPublish()
	{
		m_recvUnAckPublishList.removeAllElements();
		return;
	}
	
	//////////////////////// recv-unack-pubrec list

	public boolean addRecvUnAckPubrec(CMMqttEventPUBREC recEvent)
	{
		int nID = recEvent.getPacketID(); 
		CMMqttEventPUBREC unackEvent = findRecvUnAckPubrec(nID);
		if(unackEvent != null)
		{
			System.err.println("CMMqttSession.addRecvUnAckPubrec(), the same packet ID ("
					+nID+") already exists!");
			System.err.println(unackEvent.toString());
			
			return false;
		}
		
		return m_recvUnAckPubrecList.addElement(recEvent);
	}
	
	public CMMqttEventPUBREC findRecvUnAckPubrec(int nPacketID)
	{
		int nID = -1;
		for(CMMqttEventPUBREC unackEvent : m_recvUnAckPubrecList.getList())
		{
			nID = unackEvent.getPacketID();
			if(nID == nPacketID)
				return unackEvent;
		}
		
		return null;
	}
	
	public boolean removeRecvUnAckPubrec(int nPacketID)
	{
		CMMqttEventPUBREC unackEvent = findRecvUnAckPubrec(nPacketID);
		if(unackEvent == null)
			return false;
		
		return m_recvUnAckPubrecList.removeElement(unackEvent);
	}
	
	public void removeAllRecvUnAckPubrec()
	{
		m_recvUnAckPubrecList.removeAllElements();
		return;
	}
	

	//////////////////////// pending-trans-event list
	
	public boolean addPendingTransPublish(CMMqttEventPUBLISH pubEvent)
	{
		int nID = pubEvent.getPacketID();
		CMMqttEventPUBLISH pendingEvent = findPendingTransPublish(nID);
		if(pendingEvent != null)
		{
			System.err.println("CMMqttSession.addPendingTransPublish(), the same packet ID ("
					+nID+") already exists!");
			System.err.println(pendingEvent.toString());
			return false;			
		}
		
		return m_pendingTransPublishList.addElement(pubEvent);
	}
	
	public CMMqttEventPUBLISH findPendingTransPublish(int nPacketID)
	{
		int nID = -1;
		for(CMMqttEventPUBLISH pendingEvent : m_pendingTransPublishList.getList())
		{
			nID = pendingEvent.getPacketID();
			if(nID == nPacketID)
				return pendingEvent;
		}
		
		return null;
	}
	
	public boolean removePendingTransPublish(int nPacketID)
	{
		CMMqttEventPUBLISH pendingEvent = findPendingTransPublish(nPacketID);
		if(pendingEvent == null)
			return false;
		
		return m_pendingTransPublishList.removeElement(pendingEvent);
	}
	
	public void removeAllPendingTransPublish()
	{
		m_pendingTransPublishList.removeAllElements();
		return;
	}

	//////////////////////////////////// Overridden methods
	
	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("CMMqttSession {\n");
		if(m_mqttWill == null)
			strBuf.append("Mqtt-Will is null!\n");
		else
			strBuf.append(m_mqttWill.toString()+"\n");
		
		strBuf.append("Subscription List: "+m_subscriptionList.toString()+"\n");
		strBuf.append("}\n");
		
		return strBuf.toString();
	}
	
	///////////////////////////////////// private methods

	/*
	private int getMQTTPacketID(CMMqttEvent mqttEvent)
	{
		int nID = -1;
		if(mqttEvent instanceof CMMqttEventPUBLISH)
			nID = ((CMMqttEventPUBLISH)mqttEvent).getPacketID();
		else if(mqttEvent instanceof CMMqttEventPUBACK)
			nID = ((CMMqttEventPUBACK)mqttEvent).getPacketID();
		else if(mqttEvent instanceof CMMqttEventPUBREC)
			nID = ((CMMqttEventPUBREC)mqttEvent).getPacketID();
		else if(mqttEvent instanceof CMMqttEventPUBREL)
			nID = ((CMMqttEventPUBREL)mqttEvent).getPacketID();
		else if(mqttEvent instanceof CMMqttEventPUBCOMP)
			nID = ((CMMqttEventPUBCOMP)mqttEvent).getPacketID();
		
		return nID;
	}
	*/
}
