package com.jandebeule.iot.dashboard.layout.items;

import java.util.Iterator;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.parser.ParseError;

import com.jandebeule.iot.dashboard.MqttClientProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

// textfield (combined with optional category dropdown) 
// that publishes its value to a MQTT topic and displays search results 
// from another MQTT topic in a dropdown that can publish its value to a third
// MQTT topic
//
// The search is published as a json-string : { "category":"blabla" , "search":"whatever" }
// The results should come back as following json-string: 
//  { "results":
//		[
//		  {"item":"dsfqsdfqsdf","caption":"Item 1"} 
//		, {"item":"wvcwvwxcvwcv","caption":"Item 2"} 
//		, {"item":"araraze","caption":"Item 3"}
//		]
//	}

public class MqttSearch extends VerticalLayout {
	
	MqttClient mqttClient;
	String searchTopic;
	String resultTopic;
	String selectTopic;
	Property.ValueChangeListener valueChangeListener;
	NativeSelect categoryField;
	TextField searchField;
	NativeSelect selectField;
	
	public MqttSearch(String caption, String broker, String searchTopic,
			String resultTopic, String selectTopic, List<String> categories,
			String username, String password) {
		this.searchTopic = searchTopic;
		this.resultTopic = resultTopic;
		this.selectTopic = selectTopic;
		setCaptionAsHtml(true);
		if(!caption.isEmpty()) {
			setCaption("<b>" + caption + "</b>");
		}
		setSizeFull();
		
		HorizontalLayout categoryAndSearchLayout = new HorizontalLayout();
		categoryAndSearchLayout.setWidth("100%");
		categoryAndSearchLayout.setSpacing(false);
		categoryField = new NativeSelect();
		for(String category : categories) {
			categoryField.addItem(category);
		}
		searchField = new TextField();
		searchField.addShortcutListener(new ShortcutListener("Search",null,ShortcutAction.KeyCode.ENTER) {
			@Override
			public void handleAction(Object sender, Object target) {
				try {
					JSONObject json = new JSONObject();
					if(categoryField.getValue() != null && !categoryField.getValue().toString().isEmpty()) {
						json.put("category", categoryField.getValue());
					}
					json.put("search", searchField.getValue());
					mqttClient.publish(searchTopic, json.toJSONString().getBytes(), 0, false);
					System.out.println("Published '" + json.toJSONString() + "' to topic '" + searchTopic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + searchTopic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		if(!categories.isEmpty()) {
			categoryAndSearchLayout.addComponent(categoryField);
			categoryAndSearchLayout.setExpandRatio(categoryField, 1f);
		}
		categoryAndSearchLayout.addComponent(searchField);
		categoryAndSearchLayout.setExpandRatio(searchField, 2f);
		
		selectField = new NativeSelect("Search Results");
		selectField.setWidth("100%");
		selectField.setImmediate(true);
		valueChangeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					mqttClient.publish(selectTopic, selectField.getValue().toString().getBytes(), 0, false);
					System.out.println("Published '" + selectField.getValue() + "' to topic '" + selectTopic + "'");
				} catch (Exception ex) {
					System.out.println("Unable to publish to '" + selectTopic + "' : " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		};
		selectField.addValueChangeListener(valueChangeListener);
		mqttClient = MqttClientProvider.getInstance().getMqttClient(broker, username, password);
		MqttClientProvider.getInstance().addMqttMessageListener(mqttClient, MqttClientProvider.getInstance().new MqttMessageListener(resultTopic, UI.getCurrent()) {
			@Override
			public void onMessage(String message) {
				selectField.removeValueChangeListener(valueChangeListener);
				try {
					JSONParser parser = new JSONParser();
					JSONObject jsonObj = (JSONObject)parser.parse(message);
					JSONArray list = (JSONArray)jsonObj.get("results");
					Iterator<JSONObject> iterator = list.iterator();
					while(iterator.hasNext()) {
						JSONObject element = iterator.next();
						Item item = selectField.addItem(element.get("item"));
						selectField.setItemCaption(element.get("item"), (String) element.get("caption"));
					}
					getUi().push();
				} catch (Exception ex) {
					System.out.println("Unable to parse results from search: " + ex.getMessage());
					ex.printStackTrace();
				}
				selectField.addValueChangeListener(valueChangeListener);
			}
		});
		
		addComponent(categoryAndSearchLayout);
		addComponent(selectField);
		
	}
	
	
}
