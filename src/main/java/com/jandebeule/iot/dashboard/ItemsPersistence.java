package com.jandebeule.iot.dashboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jandebeule.iot.dashboard.Item.Type;

// helper class to read xml configuration file that defines tabs and their content
//(filename is "items.xml", for instance at /var/lib/tomcat8/items.xml if you are using tomcat8) 
//
//
///////////////
// example : //
///////////////
//
//<?xml version="1.0" encoding="UTF-8" standalone="no"?>
//<tabs>
//	<tab>
//		<name>First Tab</name>
//		<rows>2</rows>
//		<columns>1</columns>
//		<border-thickness>1</border-thickness>
//		<item>
//			<row>0</row>
//			<col>0</col>
//			<caption>Grid caption 1</caption> 
//			<type>GRID</type>
//			<grid>
//				<rows>1</rows>
//				<columns>2</columns>
//				<border-thickness>2</border-thickness>
//				<item>
//					<row>0</row>
//					<col>0</col>
//					<caption>Caption for item at 0,0</caption> 
//					<type>MQTT_SUBSCRIBE_LABEL</type>
//					<broker>tcp://10.1.1.2:1883</broker>
//					<username>user</username>
//					<password>*****</password>
//					<topic>/topic/first</topic>
//					<font_size>20</font_size>
//				</item>
//				<item>
//					<row>0</row>
//					<col>1</col>
//					<caption>Caption for item at 0,1</caption> 
//					<type>MQTT_SUBSCRIBE_LABEL</type>
//					<broker>tcp://10.1.1.2:1883</broker>
//					<username>user</username>
//					<password>*****</password>
//					<topic>/topic/second</topic>
//					<font_size>40</font_size>
//				</item>
//			</grid>
//		</item>
//		<item>
//			<row>1</row>
//			<col>0</col>
//			<caption>Grid caption 2</caption> 
//			<type>GRID</type>
//			<grid>
//				<rows>1</rows>
//				<columns>2</columns>
//				<border-thickness>0</border-thickness>
//				<item>
//					<row>1</row>
//					<col>1</col>
//					<caption>Caption for item at 1,0</caption> 
//					<type>MQTT_PUBLISH_SELECT</type>
//					<broker>tcp://10.1.1.2:1883</broker>
//					<username>user</username>
//					<password>******</password>
//					<topic>/topic/action</topic>
//					<values>
//						<value name="VALUE_1">First Value</value>
//						<value name="VALUE_2">Second Value</value>
//						<value name="VALUE_3">Third Value</value>
//					</values>
//					<feedback_topic>/topic/feedback_state</feedback_topic>
//					<feedback_values>
//						<FEEDBACK_VALUE_1>
//							<item_invisible>VALUE_1</item_invisible>
//						</FEEDBACK_VALUE_1>
//						<FEEDBACK_VALUE_2>
//							<item_invisible>VALUE_2</item_invisible>
//						</FEEDBACK_VALUE_2>
//						<FEEDBACK_VALUE_X>
//							<item_invisible>VALUE_1</item_invisible>
//							<item_invisible>VALUE_3</item_invisible>
//						</FEEDBACK_VALUE_X>
//					</feedback_values>
//				</item>
//				<item>
//					<row>1</row>
//					<col>1</col>
//					<caption>Caption for item at 1,1</caption> 
//					<type>MQTT_PUBLISH_SLIDER</type>
//					<broker>tcp://10.1.1.2:1883</broker>
//					<username>user</username>
//					<password>******</password>
//					<topic>/topic/slider/action</topic>
//					<feedback_topic>/topic/slider/state</feedback_topic>
//					<value_prefix>value=</value_prefix>
//					<value_min>0</value_min>
//					<value_max>100</value_max>
//				</item>
//			</grid>
//		</item>
//	</tab>
//	<tab>
//		<name>Tab 2</name>
//		<rows>2</rows>
//		<columns>2</columns>
//		<item>
//			<row>0</row>
//			<col>0</col>
//			<caption>Buienradar</caption> 
//			<type>HTTP_SUBSCRIBE</type>
//			<url>http://gadgets.buienradar.nl/gadget/zoommap/?lat=51.05&amp;lng=3.71667&amp;overname=2&amp;zoom=6&amp;naam=Gent&amp;size=2&amp;voor=1</url>
//			<update_interval_secs>300</update_interval_secs>
//		</item>
//		<item>
//			<row>1</row>
//			<col>0</col>
//			<caption>School</caption> 
//			<type>HTTP_SUBSCRIBE</type>
//			<url>https://www.google.com/maps/embed/v1/directions?origin=place_id:ChIJBW2USktxw0cRcfs00lKSPo8&amp;destination=place_id:ChIJzYrU5zhyw0cRkJDliwBl0g0&amp;key=AIzaSyDVlCcCEIuYEvKKdQcNsOAQ6J2DfbEHphA</url>
//			<update_interval_secs>300</update_interval_secs>
//		</item>
//		<item>
//			<row>1</row>
//			<col>1</col>
//			<caption>Nevele - Wi-Fi Huis</caption> 
//			<type>HTTP_SUBSCRIBE</type>
//			<url>https://www.google.com/maps/embed/v1/directions?origin=place_id:ChIJBW2USktxw0cRcfs00lKSPo8&amp;destination=place_id:EiNEcmllc3N0cmFhdCA0LCA5ODUwIE5ldmVsZSwgQmVsZ2nDqw&amp;key=AIzaSyDVlCcCEIuYEvKKdQcNsOAQ6J2DfbEHphA</url>
//			<update_interval_secs>300</update_interval_secs>
//		</item>
//	</tab>
//</tabs>
public class ItemsPersistence {

	private static final String XML_FILE = "items.xml";
	
	// fetch all "tab" elements from configuration file
	public static List<String> getTabs() {
		System.out.println("Fetching tabs from config file: " + new File(XML_FILE).getAbsolutePath());
		List<String> tabs = new ArrayList<String>();
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	// use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(XML_FILE);
            Element doc = dom.getDocumentElement();
            NodeList nl = doc.getElementsByTagName("tab");
            System.out.println("" + nl.getLength() + " tabs found in configuration");
            for(int n=0 ; n<nl.getLength() ; n++) {
            	Node node = nl.item(n);
            	NodeList nlTab = node.getChildNodes();
            	for(int k=0 ; k<nlTab.getLength() ; k++) {
//            		System.out.println("Tab child node found with name '" + nlTab.item(k).getNodeName() 
//            				+ "' and value '" + nlTab.item(k).getTextContent());
            		if(nlTab.item(k).getNodeName() == "name") {
            			tabs.add(nlTab.item(k).getTextContent());
            		}
            	}
            }            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return tabs;
		
	}
	
	// get the content from a "tab" element described in the configuration file
	public static GridItem getTabItems(String tabName) {
		System.out.println("Fetching tab items for tab '" + tabName + "' from config file: " + new File(XML_FILE).getAbsolutePath());
		int rows = -1;
		int cols = -1;
		Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(XML_FILE);
            Element doc = dom.getDocumentElement();
            NodeList nl = doc.getElementsByTagName("tab");
            System.out.println("" + nl.getLength() + " tabs found in configuration");
            for(int n=0 ; n<nl.getLength() ; n++) {
            	Node node = nl.item(n);
            	NodeList nlTab = node.getChildNodes();
            	boolean tabFound = false;
            	for(int k=0 ; k<nlTab.getLength() ; k++) {
//            		System.out.println("Tab child node found with name '" + nlTab.item(k).getNodeName() 
//           				+ "' and value '" + nlTab.item(k).getTextContent() + "'");
            		if(nlTab.item(k).getNodeName().equals("name")) {
            			if(nlTab.item(k).getTextContent().equals(tabName)) {
            				tabFound = true;
            			}
            		}
            		if(nlTab.item(k).getNodeName().equals("rows")) {
            			rows = Integer.valueOf(nlTab.item(k).getTextContent());
            		}
            		if(nlTab.item(k).getNodeName().equals("columns")) {
            			cols = Integer.valueOf(nlTab.item(k).getTextContent());
            		}
            	}
            	System.out.println("Requested tab '" + tabName + "' found: " + tabFound + ", rows=" + rows + ", columns=" + cols);
            	if(tabFound) {
            		GridItem grid = new GridItem();
            		grid.setItems(getGridItems(node));
            		grid.setBorderThickness(getGridBorderThickness(node));
            		return grid;
            	}
            }            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return null;
    }
	
	public static int getGridBorderThickness(Node node) {
		NodeList nl = node.getChildNodes();
    	int thickness = 0;
    	for(int k=0 ; k<nl.getLength() ; k++) {
    		if(nl.item(k).getNodeName().equals("border-thickness")) {
    			thickness = Integer.valueOf(nl.item(k).getTextContent());
    		}
    	}
    	return thickness;
	}
	
	public static Item[][] getGridItems(Node node) {
		NodeList nl = node.getChildNodes();
    	int rows = -1;
    	int cols = -1;
		for(int k=0 ; k<nl.getLength() ; k++) {
    		if(nl.item(k).getNodeName().equals("rows")) {
    			rows = Integer.valueOf(nl.item(k).getTextContent());
    		}
    		if(nl.item(k).getNodeName().equals("columns")) {
    			cols = Integer.valueOf(nl.item(k).getTextContent());
    		}
    	}
    	if(rows > 0 && cols > 0) {
    		System.out.println("Creating item array " + rows + "x" + cols);
			Item items[][] = new Item[rows][cols];
			for(int k=0 ; k<nl.getLength() ; k++) {
				if(nl.item(k).getNodeName().equals("item")) {
					Item item = new Item();		
					int row = -1;
					int col = -1;
					NodeList nlItem = nl.item(k).getChildNodes();
					for(int l=0 ; l<nlItem.getLength() ; l++) {
//						System.out.println("Item child node found with name '" + nlItem.item(l).getNodeName() 
//	            				+ "' and value '" + nlItem.item(l).getTextContent() + "'");
						switch(nlItem.item(l).getNodeName()) {
						case "grid":
							item.setGrid(getGridItems(nlItem.item(l)));
							item.setBorderThickness(getGridBorderThickness(nlItem.item(l)));
							break;
						case "type":
							item.setType(Type.valueOf(nlItem.item(l).getTextContent()));
							break;
						case "broker":
							item.setBroker(nlItem.item(l).getTextContent());
							break;
						case "username":
							item.setUsername(nlItem.item(l).getTextContent());
							break;
						case "password":
							item.setPassword(nlItem.item(l).getTextContent());
							break;
						case "topic":
							item.setTopic(nlItem.item(l).getTextContent());
							break;
						case "feedback_topic":
							item.setFeedbackTopic(nlItem.item(l).getTextContent());
							break;
						case "row":
							row = Integer.valueOf(nlItem.item(l).getTextContent());
							break;
						case "col":
							col = Integer.valueOf(nlItem.item(l).getTextContent());
							break;
						case "caption":
							item.setCaption(nlItem.item(l).getTextContent());
							break;
						case "font_size":
							item.setFontSize(Integer.parseInt(nlItem.item(l).getTextContent()));
							break;
						case "value_prefix":
							item.setPublishValuePrefix(nlItem.item(l).getTextContent());
							break;
						case "value_min":
							item.setPublishValueMin(Double.parseDouble(nlItem.item(l).getTextContent()));
							break;
						case "value_max":
							item.setPublishValueMax(Double.parseDouble(nlItem.item(l).getTextContent()));
							break;
						case "values":
							NodeList valuesItem = nlItem.item(l).getChildNodes();
	        				for(int v=0 ; v<valuesItem.getLength() ; v++) {
	        					if(valuesItem.item(v).getNodeType() == 1) {
	        						item.getPublishValueMap().put(valuesItem.item(v).getAttributes().getNamedItem("name").getTextContent(), valuesItem.item(v).getTextContent());
	        					}
	        				}
							break;
						case "feedback_values":
							NodeList feedbackvaluesItem = nlItem.item(l).getChildNodes();
	        				for(int v=0 ; v<feedbackvaluesItem.getLength() ; v++) {
	        					if(feedbackvaluesItem.item(v).getNodeType() == 1) {
	        						List<String> feedBackValues = new ArrayList<String>();
	        						NodeList feedbackNl = feedbackvaluesItem.item(v).getChildNodes();
	        						for(int w=0 ; w<feedbackNl.getLength() ; w++) {
	    	        					if(feedbackNl.item(w).getNodeName() == "item_invisible") {
	    	        						feedBackValues.add(feedbackNl.item(w).getTextContent());
	    	        					}
	        						}
	        						item.getFeedbackToPublishValueMap().put(feedbackvaluesItem.item(v).getNodeName().toUpperCase(), feedBackValues);
	        					}
	        				}
							break;
						case "url":
							item.setUrl(nlItem.item(l).getTextContent());
							break;
						case "update_interval_secs":
							item.setUpdateIntervalSeconds(Integer.parseInt(nlItem.item(l).getTextContent()));
							break;
						}
						
					}
					items[row][col] = item;
				}
			}
			return items;
    	} else {
    		System.out.println("Rows or columns < 1");
    		return null;
    	}
	}
}
