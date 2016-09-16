package com.jandebeule.iot.dashboard.wizards;

import java.util.ArrayList;
import java.util.List;

import com.jandebeule.iot.dashboard.Item;
import com.jandebeule.iot.dashboard.Item.Type;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MqttItemWizard extends Window {

	public interface WizardCompletedListener {
		public void wizardCompleted(Item item);
	}
	
	private List<WizardCompletedListener> listeners = new ArrayList<WizardCompletedListener>();
	
	public MqttItemWizard() {
		setHeight("500px");
		setWidth("600px");
		setModal(true);
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setMargin(true);
		final TextField topic = new TextField("Topic to subscribe:");
		topic.setWidth("100%");
		layout.addComponent(topic);
		Button ok = new Button("OK", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Item item = new Item();
				item.setType(Type.MQTT_SUBSCRIBE_LABEL);
				item.setBroker("tcp://192.168.0.219:1883");
				item.setUsername("xxx");
				item.setPassword("********");
				item.setTopic(topic.getValue());
				for(WizardCompletedListener listener : listeners) {
					listener.wizardCompleted(item);
				}
				UI.getCurrent().removeWindow(MqttItemWizard.this);
			}
		});
		layout.addComponent(ok);
		setContent(layout);
		center();
	}
	
	public void addWizardCompletedListener(WizardCompletedListener listener) {
		listeners.add(listener);
	}
	
	public void removeWizardCompletedListener(WizardCompletedListener listener) {
		listeners.remove(listener);
	}
	
	
}
