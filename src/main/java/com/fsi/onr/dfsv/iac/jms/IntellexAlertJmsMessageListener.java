package com.fsi.onr.dfsv.iac.jms;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.json.XML;

import com.fsi.onr.dfsv.iac.AlertProcessingeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public abstract class IntellexAlertJmsMessageListener implements MessageListener {
	
	private static final Logger logger = Logger.getLogger(IntellexAlertJmsMessageListener.class.getName());
	
	protected abstract void handleAlertJson(String alertJson) throws AlertProcessingeException;
	
	@Override
	public void onMessage(Message message) {
		String alertAsFormattedJsonString = null;
		logger.info("Received Alert");
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage)message;
			try {
				String text = textMessage.getText();
				logger.info("Alert text: " + text);
				
				String alertAsJsonString = XML.toJSONObject(text).toString();
				JsonObject alertAsJsonObject = new Gson().fromJson(alertAsJsonString, JsonObject.class);
				alertAsFormattedJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(alertAsJsonObject);
				
			} catch (JMSException e) {
				logger.log(Level.SEVERE, "Failed to process Alert", e);
			}
		} else {
			logger.warning("Expected message of type " + TextMessage.class.getName() + ". Received " + message.getClass().getName());
			return;
		}
		
		try {
			handleAlertJson(alertAsFormattedJsonString);
		} catch (AlertProcessingeException e) {
			logger.log(Level.SEVERE, "Failed to process alert", e);
		}
	}

}
