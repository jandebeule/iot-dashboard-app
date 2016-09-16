package com.jandebeule.iot.dashboard;

import java.util.HashMap;
import java.util.List;

// item that describes a single element in the GUI and that is described in
// the configuration file (see documentation ItemsPersistence.java for an example)
public class Item {

	public enum Type {
		HTTP_SUBSCRIBE, 		// a HTML page that is refreshed at a fixed interval
		HTTP_PUBLISH,   		// TO BE IMPLEMENTED (REST call?)
		MQTT_SUBSCRIBE_LABEL,	// A Label showing the content from a MQTT topic
		MQTT_PUBLISH_SELECT,	// A dropdown/button which value is published to a MQTT topic
		MQTT_PUBLISH_SLIDER,	// A slider which value is published to a MQTT topic
		GRID 					// a nested grid that contains additional Items
	}
	
	private String caption = "";
	private Type type;
	private String broker;
	private String username;
	private String password;
	private String topic;
	
	private int fontSize = 16;  // for a Label
	
	private String publishValuePrefix = "";  // a fixed prefix that is used for values published to a MQTT topic
	private double publishValueMin;			 // minimum value of a slider
	private double publishValueMax;			 // maximum value of a slider
	private HashMap<String, String> publishValueMap = new HashMap<String, String>(); // possible values for MQTT publish (using dropdown) and the mapping to their representation in the GUI (i.e. the values displayed in the dropdown)
	private String feedbackTopic = "";			 // MQTT topic that can be used by an Item that publishes to an (other) MQTT topic to reflect the current state 
	private HashMap<String, List<String>> feedbackToPublishValueMap = new HashMap<String, List<String>>();  // mapping from received values on feedback MQTT topic to which values should not be displayed in the dropdown
	
	private String url;			// url for HTTP items
	private int updateIntervalSeconds;	// update interval for HTTP_SUBSCRIBE
	
	private int borderThickness = 0;	// border thickness for a nested Grid
	
	private Item[][] grid;			// items for a nested Grid
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getBroker() {
		return broker;
	}
	public void setBroker(String broker) {
		this.broker = broker;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getPublishValuePrefix() {
		return publishValuePrefix;
	}
	public void setPublishValuePrefix(String publishValuePrefix) {
		this.publishValuePrefix = publishValuePrefix;
	}
	public double getPublishValueMin() {
		return publishValueMin;
	}
	public void setPublishValueMin(double publishValueMin) {
		this.publishValueMin = publishValueMin;
	}
	public double getPublishValueMax() {
		return publishValueMax;
	}
	public void setPublishValueMax(double publishValueMax) {
		this.publishValueMax = publishValueMax;
	}
	public HashMap<String, String> getPublishValueMap() {
		return publishValueMap;
	}
	public void setPublishValueMap(HashMap<String, String> publishValueMap) {
		this.publishValueMap = publishValueMap;
	}
	public String getFeedbackTopic() {
		return feedbackTopic;
	}
	public void setFeedbackTopic(String feedbackTopic) {
		this.feedbackTopic = feedbackTopic;
	}
	public HashMap<String, List<String>> getFeedbackToPublishValueMap() {
		return feedbackToPublishValueMap;
	}
	public void setFeedbackToPublishValueMap(HashMap<String, List<String>> feedbackToPublishValueMap) {
		this.feedbackToPublishValueMap = feedbackToPublishValueMap;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getUpdateIntervalSeconds() {
		return updateIntervalSeconds;
	}
	public void setUpdateIntervalSeconds(int updateIntervalSeconds) {
		this.updateIntervalSeconds = updateIntervalSeconds;
	}
	public Item[][] getGrid() {
		return grid;
	}
	public void setGrid(Item[][] grid) {
		this.grid = grid;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getBorderThickness() {
		return borderThickness;
	}
	public void setBorderThickness(int borderThickness) {
		this.borderThickness = borderThickness;
	}
	
	
	
}
