package com.fsi.onr.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;

public class TestNotifierReadFolder {

    private static final Logger gLogger = Logger.getLogger(TestNotifier.class.getName());
    private static final String ERROR_MSG = "Failed to publish test alert";
    
    public static void main(String[] args) throws NamingException, IOException {
        
        String qpidConnectionString = null;
        String folderAlertPath = null;
        
        if (args.length > 1) {
            qpidConnectionString = args[0];
        } else {
            gLogger.severe("Error reading Qpid Connection String");
        }
        
        if(args.length == 2){
            folderAlertPath = args[1];
        } else {
            gLogger.severe("Error reading class path.");
        }
        
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial", "org.apache.qpid.jndi.PropertiesFileInitialContextFactory");
        properties.setProperty("connectionfactory.qpidConnectionFactory", qpidConnectionString);
        properties.setProperty("topic.intellex-alerts", "intellex-alerts");
        
        Context context = new InitialContext(properties);
        
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("qpidConnectionFactory");
        Topic topic = (Topic) context.lookup("intellex-alerts");
        Connection connection = null;
        Session session;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer messageProducer = session.createProducer(topic);
            
            sendAlertsFromFolder(folderAlertPath, session, messageProducer);
            
            session.commit();
            connection.close();
            gLogger.info("Published notification on topic '" + topic.getTopicName() + "'");
        } catch (JMSException e) {
            gLogger.log(Level.SEVERE, ERROR_MSG, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    gLogger.log(Level.SEVERE, "Failed to close connection", e);
                }
            }
        }
    }
    
    private static void sendAlertsFromFolder(String folderAlertPath, Session session, MessageProducer messageProducer) throws IOException, JMSException{
        
        String[] extensions = {"txt"};
        File directory = new File(folderAlertPath);
        
        Iterator<File> alertFileIterator = FileUtils.iterateFiles(directory, extensions, false);
        
        while(alertFileIterator.hasNext()){
            File curFile = alertFileIterator.next();
            String alertText = FileUtils.readFileToString(curFile, "UTF-8");
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(alertText);
            messageProducer.send(textMessage);
        }
    }
}
