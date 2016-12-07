package com.jandebeule.iot.dashboard.layout.items;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.jandebeule.iot.dashboard.MqttClientProvider;
import com.vaadin.data.Property;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.UI;

// slider that publishes its value to a MQTT topic
public class MqttSlider extends HorizontalLayout {
	
	MqttClient mqttClient;
	String topic;
	String feedbackTopic;
	String valuePrefix;
	Property.ValueChangeListener valueChangeListener;
	Button min;
	Button plus;
	Slider slider;
	
	public MqttSlider(String caption, String broker, String topic, String feedbackTopic,
			final String valuePrefix, final double valueMin, final double valueMax,
			String username, String password) {
		this.topic = topic;
		this.feedbackTopic = feedbackTopic;
		this.valuePrefix = valuePrefix;
		setCaptionAsHtml(true);
		if(!caption.isEmpty()) {
			setCaption("<b>" + caption + "</b>");
		}
		min = new Button("-", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Double value = slider.getValue() - ((valueMax-valueMin)/20.0);
					mqttClient.publish(topic, (valuePrefix + value.toString()).getBytes(), 0, false);
					System.out.println("Published '" + valuePrefix + slider.getValue() + "' to topic '" + topic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		plus = new Button("+", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Double value = slider.getValue() + ((valueMax-valueMin)/20.0);
					mqttClient.publish(topic, (valuePrefix + value.toString()).getBytes(), 0, false);
					System.out.println("Published '" + valuePrefix + slider.getValue() + "' to topic '" + topic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		min.setStyleName("tiny");
		plus.setStyleName("tiny");
		setSizeFull();
		slider = new Slider(valueMin, valueMax, 0);
		slider.setImmediate(true);
		valueChangeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				try {
					mqttClient.publish(topic, (valuePrefix + slider.getValue().toString()).getBytes(), 0, false);
					System.out.println("Published '" + valuePrefix + slider.getValue() + "' to topic '" + topic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		};
		slider.addValueChangeListener(valueChangeListener);
		mqttClient = MqttClientProvider.getInstance().getMqttClient(broker, username, password);
		if(feedbackTopic != null && !feedbackTopic.isEmpty()) {
			MqttClientProvider.getInstance().addMqttMessageListener(mqttClient, MqttClientProvider.getInstance().new MqttMessageListener(feedbackTopic, UI.getCurrent()) {
				@Override
				public void onMessage(String message) {
					slider.removeValueChangeListener(valueChangeListener);
					slider.setValue(Double.parseDouble(message.substring(message.indexOf('=') + 1)));
					// bug fix for slider growing in size (only in Chrome?) when value is set and thus pushing the '+' button out of the visible area
					// solution : rebuild the MqttSlider layout, but first temporarily push empty layout (user won't notice this)
					removeAllComponents();
					getUi().push();
					slider.addValueChangeListener(valueChangeListener);
					setSizeFull();
					addComponent(min);
					addComponent(slider);
					setExpandRatio(slider, 1f);
					addComponent(plus);
				}
			});
		}
		addComponent(min);
		addComponent(slider);
		setExpandRatio(slider, 1f);
		addComponent(plus);
	}
	
	
	protected MqttClient connect(String broker, String username, String password) {
		try {
			MemoryPersistence persistence = new MemoryPersistence();
			MqttClient mqttClient = new MqttClient(broker, "MqttGUI_Slider_" + feedbackTopic + "_" + System.currentTimeMillis(), persistence);
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
