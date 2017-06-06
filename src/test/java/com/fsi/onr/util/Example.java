package com.fsi.onr.util;

import com.fsi.onr.dfsv.iac.AlertProcessingeException;
import com.fsi.onr.dfsv.iac.jms.IntellexAlertJmsMessageListener;
import com.fsi.onr.dfsv.iac.jms.IntellexAlertJmsSubscriber;

public class Example {

	public static void main(String[] args) throws InterruptedException {
		MyIntellexAlertListener myIntellexAlertListener = new MyIntellexAlertListener();
		IntellexAlertJmsSubscriber intellexAlertJmsSubscriber = new IntellexAlertJmsSubscriber(myIntellexAlertListener);
		intellexAlertJmsSubscriber.subscribe();
		Thread.sleep(10000);
		intellexAlertJmsSubscriber.closeConnection();
	}
	
	private static class MyIntellexAlertListener extends IntellexAlertJmsMessageListener {

		@Override
		protected void handleAlertJson(String alertJson)
				throws AlertProcessingeException {
			System.out.println(alertJson);
		}
		
	}
}
