package com.jandebeule.iot.dashboard.layout.items;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.vaadin.data.Property;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.UI;

// slider that publishes its value to a MQTT topic
public class MqttSlider extends Slider {
	
	MqttClient mqttClient;
	String topic;
	String valuePrefix;
	Property.ValueChangeListener valueChangeListener;
	
	public MqttSlider(String caption, String broker, String topic, String feedbackTopic,
			final String valuePrefix, final double valueMin, final double valueMax,
			String username, String password) {
		this.topic = topic;
		this.valuePrefix = valuePrefix;
		setCaptionAsHtml(true);
		setCaption("<b>" + caption + "</b>");
		setWidth("100%");
		setMin(valueMin);
		setMax(valueMax);
		setImmediate(true);
		valueChangeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				try {
					mqttClient.publish(topic, (valuePrefix + getValue().toString()).getBytes(), 0, false);
					System.out.println("Published '" + valuePrefix + getValue() + "' to topic '" + topic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		};
		addValueChangeListener(valueChangeListener);
		System.out.println("Connecting with broker '" + broker + "', username=" + username + ", password=" + password);
		mqttClient = connect(broker, username, password);
		mqttClient.setCallback(new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				String messageStr = new String(message.getPayload());
				System.out.println("MQTT message received for topic '" + topic + "': " + messageStr + ", updating slider");
				try {
					UI.getCurrent().getSession().getLockInstance().lock();
					try {
						removeValueChangeListener(valueChangeListener);
						setValue(Double.parseDouble(messageStr.substring(messageStr.indexOf('=') + 1)));
						UI.getCurrent().push();
						addValueChangeListener(valueChangeListener);
						UI.getCurrent().push();
					} finally {
						UI.getCurrent().getSession().getLockInstance().unlock();
					}
				} catch (Exception ex) {
					System.out.println("Unable to update MqttSlider '" + caption + "'");
					ex.printStackTrace();
				}
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// don't care
			}
			
			@Override
			public void connectionLost(Throwable cause) {
				System.out.println("MqttSlider: Lost connection with MQTT: " + cause.getMessage());
				cause.printStackTrace();
			}
		});
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(feedbackTopic != null && !feedbackTopic.isEmpty()) {
							try {
								mqttClient.subscribe(feedbackTopic);
							} catch (Exception ex) {
								System.out.println("Unable to subscribe to topic '" + feedbackTopic + "' :" + ex.getMessage());
								ex.printStackTrace();
							}
							System.out.println("Subscribed to topic '" + feedbackTopic + "'");
						}
					}
				}).start();
			}
		});
		
		addDetachListener(new DetachListener() {
			@Override
			public void detach(DetachEvent event) {
				if(feedbackTopic != null && !feedbackTopic.isEmpty()) {
					try {
						mqttClient.unsubscribe(feedbackTopic);
					} catch (Exception ex) {
						System.out.println("Unable to unsubscribe to topic '" + feedbackTopic + "' :" + ex.getMessage());
						ex.printStackTrace();
					}
					System.out.println("Unsubscribed to topic '" + feedbackTopic + "'");
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
