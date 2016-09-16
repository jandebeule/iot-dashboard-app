package com.jandebeule.iot.dashboard.layout.items;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.DetachEvent;
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
		System.out.println("Connecting with broker '" + broker + "', username=" + username + ", password=" + password);
		MqttClient mqttClient = connect(broker, username, password);
		mqttClient.setCallback(new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				String messageStr = new String(message.getPayload());
				System.out.println("MQTT message received for topic '" + topic + "': " + messageStr);
				try {
					UI.getCurrent().getSession().getLockInstance().lock();
					try {
						setValue("<div style=\"font-size: " + fontSize + "px;\">" + messageStr.replaceAll("\n", "<br/>") + "</div>");
						UI.getCurrent().push();
					} finally {
						UI.getCurrent().getSession().getLockInstance().unlock();
					}
				} catch (Exception ex) {
					System.out.println("Unable to update MqttLabel '" + caption + "'");
					ex.printStackTrace();
				}
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// don't care
			}
			
			@Override
			public void connectionLost(Throwable cause) {
				System.out.println("MqttLabel: Lost connection with MQTT: " + cause.getMessage());
				cause.printStackTrace();
			}
		});
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				try {
					mqttClient.subscribe(topic);
				} catch (Exception ex) {
					System.out.println("Unable to subscribe to topic '" + topic + "' :" + ex.getMessage());
					ex.printStackTrace();
				}
				System.out.println("Subscribed to topic '" + topic + "'");
			}
		});
		addDetachListener(new DetachListener() {
			@Override
			public void detach(DetachEvent event) {
				try {
					mqttClient.unsubscribe(topic);
				} catch (Exception ex) {
					System.out.println("Unable to unsubscribe to topic '" + topic + "' :" + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		
	}
	
	protected MqttClient connect(String broker, String username, String password) {
		try {
			MemoryPersistence persistence = new MemoryPersistence();
			MqttClient mqttClient = new MqttClient(broker, "MqttGUI_" + System.currentTimeMillis(), persistence);
			MqttConnectOptions connOptions = new MqttConnectOptions();
			connOptions.setUserName(username);
			connOptions.setPassword(password.toCharArray());
			mqttClient.connect(connOptions);
			return mqttClient;
		} catch (Exception ex) {
			System.out.println("Unable to create MQTT Client: " + ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}
}
