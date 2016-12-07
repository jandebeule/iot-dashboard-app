package com.jandebeule.iot.dashboard.layout.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.MqttClient;

import com.jandebeule.iot.dashboard.MqttClientProvider;
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
		mqttClient = MqttClientProvider.getInstance().getMqttClient(broker, username, password);
		if(feedbackTopic != null && !feedbackTopic.isEmpty()) {
			MqttClientProvider.getInstance().addMqttMessageListener(mqttClient, MqttClientProvider.getInstance().new MqttMessageListener(feedbackTopic, UI.getCurrent()) {
				@Override
				public void onMessage(String message) {
					message = message.toUpperCase();
					HashMap<String, String> newValuesMap = new HashMap<String, String>();
					System.out.println("Feedback value: " + (feedbackValuesMap.get(message) == null ? "NULL":feedbackValuesMap.get(message)));
					for(String key : valuesMap.keySet()) {
						try {
							if(!feedbackValuesMap.get(message).contains(key)) {
								newValuesMap.put(key, valuesMap.get(key));
							} else {
								System.out.println("leaving out following element: " + key + " - " + valuesMap.get(key));
							}
						} catch (Exception exx) {
							System.out.println("Unable to process feedback value '" + key + "': " + exx.getMessage());
							exx.printStackTrace();
						}
					}
					updateSelect(newValuesMap);
				}
			});
		}
			
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
