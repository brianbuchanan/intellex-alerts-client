package com.fsi.onr.dfsv.iac.jms;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NamingException;

import com.fsi.onr.dfsv.DfsvProperties;

public class IntellexAlertJmsSubscriber {

	private static final Logger logger = Logger.getLogger(IntellexAlertJmsSubscriber.class.getName());
	
	private static final String SUBSCRIBER_NAME = "fsi-intellex-alerts-subscriber";
	
	private Session session = null;
	private Connection connection = null;
	private Context context = null;
	
	private IntellexAlertJmsMessageListener intellexAlertMessageListener = null;
	
	public IntellexAlertJmsSubscriber(IntellexAlertJmsMessageListener intellexAlertMessageListener) {
		this.intellexAlertMessageListener = intellexAlertMessageListener;
	}
	
	
	public void subscribe() {
		logger.info("Loading application context");
		context = DfsvProperties.getAmqpContext();
		if (context == null) {
			throw new ExceptionInInitializerError("Failed to set up initial context");
		}
		ConnectionFactory connectionFactory;
		try {
			connectionFactory = (ConnectionFactory) context.lookup("qpidConnectionFactory");
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			Topic topic = (Topic) context.lookup("intellex-alerts");
			MessageConsumer messageConsumer = session.createConsumer(topic);
			messageConsumer.setMessageListener(intellexAlertMessageListener);
		} catch (NamingException | JMSException e) {
			logger.info("Failed to set up connection and session");
			throw new ExceptionInInitializerError(e);
		}
	}
	
	@PreDestroy
	public void closeConnection() {
		try {
			if (session != null) {
				session.unsubscribe(SUBSCRIBER_NAME);
			}
			if (connection != null) {
				connection.close();
			}
			if (context != null) {
				context.close();
			}
		} catch (JMSException e) {
			logger.log(Level.SEVERE, "Failed to unsubscribe or close connection.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Failed to close Naming Context", e);
		}
	}
}
