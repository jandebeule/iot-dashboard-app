package com.jandebeule.iot.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.vaadin.ui.UI;

public class MqttClientProvider {

	private HashMap<String,MqttClient> mqttClients;
	private HashMap<MqttClient, List<MqttMessageListener>> mqttMessageListenerMap;
	private HashMap<MqttClient, HashSet<String>> mqttSubscribedTopicsMap;
	private HashMap<MqttClient, HashMap<String,String>> mqttSubscribedTopicsCache;
	private static MqttClientProvider singleton;
	
	public static MqttClientProvider getInstance() {
		if(singleton == null) {
			singleton = new MqttClientProvider();
		}
		return singleton;
	}
	
	public abstract class MqttMessageListener {
		private String topic;
		private UI ui;
		public MqttMessageListener(String topic, UI ui) {
			this.topic = topic;
			this.ui = ui;
		}
		public String getTopic() {
			return this.topic;
		}
		public boolean isAlive() {
			return this.ui.isAttached() && ui.getSession() != null;
		}
		public boolean isUiInitialized() {
			return ((MyUI)this.ui).initialized;
		}
		public UI getUi() {
			return this.ui;
		}
		public abstract void onMessage(String message);
	}
	
	private MqttClientProvider() {
		mqttClients = new HashMap<String,MqttClient>();
		mqttMessageListenerMap = new HashMap<MqttClient,List<MqttMessageListener>>();
		mqttSubscribedTopicsMap = new HashMap<MqttClient,HashSet<String>>();
		mqttSubscribedTopicsCache = new HashMap<MqttClient, HashMap<String, String>>();
	}
	
	public MqttClient getMqttClient(String broker, String username, String password) {
    	if(!mqttClients.containsKey(broker)) {
    		try {
    			MemoryPersistence persistence = new MemoryPersistence();
    			System.out.println("Connecting with broker '" + broker + "', username=" + username + ", password=" + password);
    			MqttClient mqttClient = new MqttClient(broker, "MqttGUI_" + System.currentTimeMillis(), persistence);
    			MqttConnectOptions connOptions = new MqttConnectOptions();
    			connOptions.setUserName(username);
    			connOptions.setPassword(password.toCharArray());
    			mqttClient.connect(connOptions);
    			System.out.println("Connected with broker '" + broker + "', username=" + username + ", password=" + password);
    			mqttClient.setCallback(new MqttCallback() {
					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {
						String messageStr = new String(message.getPayload());
						System.out.println("MQTT message received for topic '" + topic + "': " + messageStr);
						new Thread(new Runnable() {
							@Override
							public void run() {
								if(mqttMessageListenerMap.containsKey(mqttClient)) {
									mqttSubscribedTopicsCache.get(mqttClient).put(topic, messageStr);
									List<MqttMessageListener> currentListeners = new ArrayList<MqttMessageListener>(mqttMessageListenerMap.get(mqttClient));
									for(MqttMessageListener listener : currentListeners) {
										if(listener.getTopic().equals(topic) && listener.isAlive()) {
											try {
												while(!listener.isUiInitialized()) {
													try { Thread.sleep(200); } catch (InterruptedException exxx) {}
												}
												System.out.println("onMessage '" + messageStr + "' for UI " + listener.getUi() + "...");
												listener.getUi().getSession().getLockInstance().lock();
												try {
													listener.onMessage(messageStr);
													listener.getUi().push();
												} finally {
													listener.getUi().getSession().getLockInstance().unlock();
												}
												System.out.println("onMessage '" + messageStr + "' for UI " + listener.getUi() + " finished");
											} catch (Exception ex) {
												System.out.println("Exception during invocation onMessage: " + ex.getMessage());
												ex.printStackTrace();
											}
										} else if(!listener.isAlive()) {
											System.out.println("Removing dead listener");
											removeMqttMessageListener(mqttClient, listener);
										}
									}
								}
								System.out.println("Processed MQTT message '" + messageStr + "' on topic '" + topic + "'");
							}
						}).start();
						
					}
					
					@Override
					public void deliveryComplete(IMqttDeliveryToken token) {
						
					}
					
					@Override
					public void connectionLost(Throwable cause) {
						
					}
				});
    			mqttClients.put(broker, mqttClient);
    		} catch (Exception ex) {
    			System.out.println("Unable to create MQTT Client: " + ex.getMessage());
    			ex.printStackTrace();
    		}
    	}
    	return mqttClients.get(broker);
    }
	
	public void addMqttMessageListener(MqttClient client, MqttMessageListener listener) {
		if(!mqttMessageListenerMap.containsKey(client)) {
			mqttMessageListenerMap.put(client, new ArrayList<MqttMessageListener>());
		}
		if(!mqttSubscribedTopicsMap.containsKey(client)) {
			mqttSubscribedTopicsMap.put(client, new HashSet<String>());
		}
		if(!mqttSubscribedTopicsCache.containsKey(client)) {
			mqttSubscribedTopicsCache.put(client, new HashMap<String, String>());
		}
		if(!mqttSubscribedTopicsMap.get(client).contains(listener.getTopic())) {
			while(!client.isConnected()) {
				try { Thread.sleep(100); } catch (InterruptedException exx) {}
			}
			System.out.println("Subscribing for topic '" + listener.getTopic() + "'...");
			try {
				client.subscribe(listener.getTopic());
				System.out.println("Subscribed for topic '" + listener.getTopic() + "'");
				mqttSubscribedTopicsMap.get(client).add(listener.getTopic());
			} catch (Exception ex) {
				System.out.println("Unable to subscribe to topic '" + listener.getTopic() + "': " + ex.getMessage());
				ex.printStackTrace();
			}
		} else if(mqttSubscribedTopicsCache.get(client).containsKey(listener.getTopic())) {
			String messageStr = mqttSubscribedTopicsCache.get(client).get(listener.getTopic());
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while(!listener.isUiInitialized()) {
							try { Thread.sleep(200); } catch (InterruptedException exxx) {}
						}
						System.out.println("onMessage '" + messageStr + "' for UI " + listener.getUi() + "...");
						listener.getUi().getSession().getLockInstance().lock();
						try {
							listener.onMessage(messageStr);
							listener.getUi().push();
						} finally {
							listener.getUi().getSession().getLockInstance().unlock();
						}
						System.out.println("onMessage '" + messageStr + "' for UI " + listener.getUi() + " finished");
					} catch (Exception ex) {
						System.out.println("Exception during invocation onMessage: " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}).start();
		}
		mqttMessageListenerMap.get(client).add(listener);
		System.out.println("Added message listener for UI " + listener.getUi() + " and topic " + listener.getTopic());		
	}
	
	public void removeMqttMessageListener(MqttClient client, MqttMessageListener listener) {
		mqttMessageListenerMap.get(client).remove(listener);
		System.out.println("Removed message listener for UI " + listener.getUi() + " and topic " + listener.getTopic());
	}
	
	
	
}
