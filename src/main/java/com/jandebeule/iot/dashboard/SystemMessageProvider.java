package com.jandebeule.iot.dashboard;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;

class SystemMessageProvider implements SystemMessagesProvider {

	private static SystemMessageProvider instance;
	private SystemMessages messages;
	
	public static SystemMessageProvider get() {
		if(instance == null) {
			instance = new SystemMessageProvider();
		}
		return instance;
	}
 	
	private SystemMessageProvider() {
		messages = new CustomizedSystemMessages();
		((CustomizedSystemMessages)messages).setSessionExpiredNotificationEnabled(false);
		((CustomizedSystemMessages)messages).setSessionExpiredURL(null);
	}
	
	@Override
	public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
		return messages;
	}

}
