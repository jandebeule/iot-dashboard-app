package com.jandebeule.iot.dashboard.layout.items;

import org.eclipse.paho.client.mqttv3.MqttClient;

import com.jandebeule.iot.dashboard.MqttClientProvider;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

// Label that shows messages from MQTT topic
public class MqttLabel extends Label {

	protected MqttClient mqttClient;
	protected String topic;
	
	public MqttLabel(String caption, String broker, String topic, String username, String password, int fontSize) {
		setCaptionAsHtml(true);
		if(!caption.isEmpty()) {
			setCaption("<b>" + caption + "</b>");
		}
		setSizeFull();
		setContentMode(ContentMode.HTML);
		MqttClient mqttClient = MqttClientProvider.getInstance().getMqttClient(broker, username, password);
		MqttClientProvider.getInstance().addMqttMessageListener(mqttClient, MqttClientProvider.getInstance().new MqttMessageListener(topic, UI.getCurrent()) {			
			@Override
			public void onMessage(String message) {
				setValue("<div style=\"font-size: " + fontSize + "px;\">" + message.replaceAll("\n", "<br/>") + "</div>");
			}
		});
			
	}
	
}
