package com.jandebeule.iot.dashboard.layout.items;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.vaadin.addons.d3Gauge.Gauge;
import org.vaadin.addons.d3Gauge.GaugeConfig;

import com.jandebeule.iot.dashboard.MqttClientProvider;
import com.jandebeule.iot.dashboard.MqttClientProvider.MqttMessageListener;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

// Gauge that shows numeric values from MQTT topic
public class MqttGauge extends VerticalLayout {

	protected MqttClient mqttClient;
	protected String topic;
	protected String historyUrl;
	
	public MqttGauge(String caption, String broker, String topic, String username, String password, int min, int max, String historyUrl, int size) {
		this.topic = topic;
		this.historyUrl = historyUrl;
		setSizeFull();
		GaugeConfig config = new GaugeConfig();
		config.setMin(min);
		config.setMax(max);
		Gauge gauge = new Gauge(caption, 0, size, config);
		gauge.setWidth(size + 10, Unit.PIXELS);
		gauge.setHeight(size + 10, Unit.PIXELS);
		addComponent(gauge);
		setComponentAlignment(gauge, Alignment.MIDDLE_CENTER);
		MqttClient mqttClient = MqttClientProvider.getInstance().getMqttClient(broker, username, password);
		MqttClientProvider.getInstance().addMqttMessageListener(mqttClient, MqttClientProvider.getInstance().new MqttMessageListener(topic, UI.getCurrent()) {
			@Override
			public void onMessage(String message) {
				gauge.setValue(new Double(Double.parseDouble(message)).intValue());
			}
		});
		addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
			@Override
			public void layoutClick(LayoutClickEvent event) {
				Window win = new Window("History");
				win.setWidth((int)(Page.getCurrent().getBrowserWindowWidth() * 0.75), Unit.PIXELS);
				win.setHeight((int)(Page.getCurrent().getBrowserWindowHeight() * 0.75), Unit.PIXELS);
				win.setModal(true);
				BrowserFrame html = new BrowserFrame();
				html.setSizeFull();
				html.setSource(new ExternalResource(historyUrl));
				win.setContent(html);
				UI.getCurrent().addWindow(win);
				win.center();
			}
		});
		
	}
	
}
