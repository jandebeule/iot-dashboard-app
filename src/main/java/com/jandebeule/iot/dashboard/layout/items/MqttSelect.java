package com.jandebeule.iot.dashboard.layout.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.UI;

// dropdown or button that publishes its value to a MQTT topic
public class MqttSelect extends CssLayout {
	
	Component select;
	MqttClient mqttClient;
	String topic;
	String valuePrefix;
	String feedbackTopic = "";
	
	public MqttSelect(String caption, String broker, String topic, String feedbackTopic, String valuePrefix,
			final HashMap<String, String> valuesMap, HashMap<String, List<String>> feedbackValuesMap,
			String username, String password) {
		this.topic = topic;
		this.feedbackTopic = feedbackTopic;
		this.valuePrefix = valuePrefix;
		setCaptionAsHtml(true);
		if(!caption.isEmpty()) {
			setCaption("<b>" + caption + "</b>");
		}
		setSizeUndefined();
		updateSelect(valuesMap);
		System.out.println("Connecting with broker '" + broker + "', username=" + username + ", password=" + password);
		mqttClient = connect(broker, username, password);
		mqttClient.setCallback(new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				String messageStr = new String(message.getPayload()).toUpperCase();
				System.out.println("MQTT message received for topic '" + topic + "': " + messageStr + ", updating select...");
				HashMap<String, String> newValuesMap = new HashMap<String, String>();
				System.out.println("Feedback value: " + (feedbackValuesMap.get(messageStr) == null ? "NULL":feedbackValuesMap.get(messageStr)));
				for(String key : valuesMap.keySet()) {
					try {
						if(!feedbackValuesMap.get(messageStr).contains(key)) {
							newValuesMap.put(key, valuesMap.get(key));
						} else {
							System.out.println("leaving out following element: " + key + " - " + valuesMap.get(key));
						}
					} catch (Exception exx) {
						System.out.println("Unable to process feedback value '" + key + "': " + exx.getMessage());
						exx.printStackTrace();
					}
				}
				try {
					UI.getCurrent().getSession().getLockInstance().lock();
					try {
						updateSelect(newValuesMap);
						UI.getCurrent().push();
					} finally {
						UI.getCurrent().getSession().getLockInstance().unlock();
					}
				} catch (Exception ex) {
					System.out.println("Unable to update MqttSelect '" + caption + "'");
					ex.printStackTrace();
				}
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// don't care
			}
			
			@Override
			public void connectionLost(Throwable cause) {
				System.out.println("MqttSelect: Lost connection with MQTT: " + cause.getMessage());
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
	
	protected void updateSelect(HashMap<String, String> valuesMap) {
		Component oldSelect = select;
		select = new Label("No action available");
		System.out.println("Updating select with # values : " + valuesMap.size());
		if(valuesMap.size() > 1) {
			select = new NativeSelect();
			((NativeSelect)select).setImmediate(true);
			List<Entry<String, String>> sortedValues = entriesSortedByValues(valuesMap);
			for(Entry<String, String> entry : sortedValues) {
				((NativeSelect)select).addItem(entry.getKey());
				((NativeSelect)select).setItemCaption(entry.getKey(), entry.getValue());
			}
			((NativeSelect)select).addValueChangeListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					try {
						mqttClient.publish(topic, (valuePrefix + (String)((NativeSelect)select).getValue()).getBytes(), 0, false);
						System.out.println("Published '" + valuePrefix + ((NativeSelect)select).getValue() + "' to topic '" + topic + "'");
						if(feedbackTopic == null || feedbackTopic.isEmpty()) {
							updateSelect(valuesMap);
						}
					} catch (Exception ex) {
						System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			});			
		} else if(valuesMap.size() == 1) {
			select = new Button(valuesMap.values().iterator().next(), new Button.ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						mqttClient.publish(topic, (valuePrefix + valuesMap.keySet().iterator().next()).getBytes(), 0, false);
						System.out.println("Published '" + valuePrefix + valuesMap.keySet().iterator().next() + "' to topic '" + topic + "'");
					} catch (Exception ex) {
						System.out.println("Unable to publish to '" + topic + "' : " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			});
		}
		try {
			select.setWidth("100%");
			if(oldSelect == null) {
				addComponent(select);
			} else {
				replaceComponent(oldSelect, select);
			}
		} catch (Exception ex) {
			System.out.println("Exception while updating MqttSelect (" + getCaption() + "): " + ex.getMessage());
			ex.printStackTrace();
		}
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
	
	static <K,V extends Comparable<? super V>> 
	    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {	
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
	
		Collections.sort(sortedEntries, 
		    new Comparator<Entry<K,V>>() {
		        @Override
		        public int compare(Entry<K,V> e1, Entry<K,V> e2) {
		            return e2.getValue().compareTo(e1.getValue());
		        }
		    }
		);
		
		return sortedEntries;
	}
}
