package com.jandebeule.iot.dashboard.layout;

import com.jandebeule.iot.dashboard.Item;
import com.jandebeule.iot.dashboard.ItemsPersistence;
import com.jandebeule.iot.dashboard.Item.Type;
import com.jandebeule.iot.dashboard.layout.items.HtmlPage;
import com.jandebeule.iot.dashboard.layout.items.MqttLabel;
import com.jandebeule.iot.dashboard.layout.items.MqttSelect;
import com.jandebeule.iot.dashboard.layout.items.MqttSlider;
import com.jandebeule.iot.dashboard.wizards.MqttItemWizard;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.UI;


// Layout component that uses pure HTML (table) to create a grid where items can be placed in
// check the Item.java class to get an overview of possible item types and their variables
public class Grid extends CustomLayout {
	
	final static int DEFAULT_ROWS = 4;
	final static int DEFAULT_COLS = 2;
	
	protected Item items[][];
	
	public Grid(com.jandebeule.iot.dashboard.GridItem gridItem) {
		this.items = gridItem.getItems();
		int borderThickness = gridItem.getBorderThickness();
		setSizeFull();
		String template = "<table style=\"border-collapse: collapse; border: " + borderThickness + "px solid black; width: 100%; height: 100%;\">";
		for(int row = 0 ; row < items.length ; row++) {
			template += "<tr style=\"height: " + (int)Math.round(100.0/items.length) + "%; border: " + borderThickness + "px solid black;\">";
			for(int col = 0 ; col < items[row].length; col++) {
				template += "<td style=\"width: " + (int)Math.round(100.0/items[row].length) + "%; border: " + borderThickness + "px solid black;\"><div style=\"width: 100%; height: 100%;\" location=\"element_" + row + "_" + col + "\"></div></td>";
			}
			template += "</tr>";
		}
		template += "</table>";
		setTemplateContents(template);
		fillGrid();
	}
	
	public Grid(String caption, com.jandebeule.iot.dashboard.GridItem gridItem) {
		this(gridItem);
		if(!caption.isEmpty()) {
			setCaptionAsHtml(true);
			setCaption("<b>" + caption + "</b>");
		}
	}
	
	public Grid(String tab) {
		this(ItemsPersistence.getTabItems(tab));
	}
	
	public void fillGrid() {
		for(int row=0 ; row<items.length ; row++) {
			for(int col=0 ; col<items[row].length ; col++) {
				Component component = null;
				if(items[row][col] == null) {
					component = createEmptyItem(row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_SUBSCRIBE_LABEL)) {
					component = createMqttLabel(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_PUBLISH_SELECT)) {
					component = createMqttSelect(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_PUBLISH_SLIDER)) {
					component = createMqttSlider(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.HTTP_SUBSCRIBE)) {
					component = createHtmlPage(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.GRID)) {
					component = createGrid(items[row][col], row, col);
				} 
				if(component != null) {
					addComponent(component, "element_" + row + "_" + col);
				}
			}
		}
	}
	
	public Button createEmptyItem(final int row, final int col) {
//		Button button = new Button("+", new Button.ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				MqttItemWizard wizard = new MqttItemWizard();
//				UI.getCurrent().addWindow(wizard);
//				wizard.addWizardCompletedListener(new MqttItemWizard.WizardCompletedListener() {
//					@Override
//					public void wizardCompleted(Item item) {
//						items[row][col] = item;
//						fillGrid();
//					}
//				});
//			}
//		});
		// for the moment the wizard to create a new Item on the fly is not implemented yet
		Button button = new Button("");
		button.setSizeFull();
		return button;
	}
	
	public MqttLabel createMqttLabel(final Item item, final int row, final int col) {
		MqttLabel label = new MqttLabel(item.getCaption(), item.getBroker(), item.getTopic(), item.getUsername(), item.getPassword(), item.getFontSize());
		return label;
	}
	
	public MqttSelect createMqttSelect(final Item item, final int row, final int col) {
		MqttSelect select = new MqttSelect(item.getCaption(), item.getBroker(), item.getTopic(), item.getFeedbackTopic(), item.getPublishValuePrefix(), item.getPublishValueMap(), item.getFeedbackToPublishValueMap(), item.getUsername(), item.getPassword());
		return select;
	}
	
	public MqttSlider createMqttSlider(final Item item, final int row, final int col) {
		MqttSlider slider = new MqttSlider(item.getCaption(), item.getBroker(), item.getTopic(), item.getFeedbackTopic(), item.getPublishValuePrefix(), item.getPublishValueMin(), item.getPublishValueMax(), item.getUsername(), item.getPassword());
		return slider;
	}
	
	public HtmlPage createHtmlPage(final Item item, final int row, final int col) {
		HtmlPage page = new HtmlPage(item.getCaption(), item.getUrl(), item.getUpdateIntervalSeconds());
		return page;
	}
	
	public Grid createGrid(final Item item, final int row, final int col) {
		com.jandebeule.iot.dashboard.GridItem gridItem = new com.jandebeule.iot.dashboard.GridItem();
		gridItem.setItems(item.getGrid());
		gridItem.setBorderThickness(item.getBorderThickness());
		Grid grid = new Grid(item.getCaption(), gridItem);
		return grid;
	}
}
