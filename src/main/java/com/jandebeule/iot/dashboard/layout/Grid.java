package com.jandebeule.iot.dashboard.layout;

import com.jandebeule.iot.dashboard.Item;
import com.jandebeule.iot.dashboard.Item.Type;
import com.jandebeule.iot.dashboard.ItemsPersistence;
import com.jandebeule.iot.dashboard.layout.items.HtmlPage;
import com.jandebeule.iot.dashboard.layout.items.MqttGauge;
import com.jandebeule.iot.dashboard.layout.items.MqttLabel;
import com.jandebeule.iot.dashboard.layout.items.MqttSearch;
import com.jandebeule.iot.dashboard.layout.items.MqttSelect;
import com.jandebeule.iot.dashboard.layout.items.MqttSlider;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;


// Layout component that uses pure HTML (table) to create a grid where items can be placed in
// check the Item.java class to get an overview of possible item types and their variables
public class Grid extends CustomLayout {
	
	final static int DEFAULT_ROWS = 4;
	final static int DEFAULT_COLS = 2;
	
	protected String location;
	protected Item items[][];
	
	public Grid(String location, com.jandebeule.iot.dashboard.GridItem gridItem) {
		this.location = location;
		this.items = gridItem.getItems();
		int borderThickness = gridItem.getBorderThickness();
		setSizeFull();
		String template = "<table style=\"border-collapse: collapse; border: " + borderThickness + "px solid white; width: 100%; height: 100%;\">";
		for(int row = 0 ; row < items.length ; row++) {
			template += "<tr style=\"height: " + (int)Math.round(100.0/items.length) + "%; border: " + borderThickness + "px solid white;\">";
			for(int col = 0 ; col < items[row].length; col++) {
				template += "<td style=\"padding: 0px 5px 0px 5px; width: " + (int)Math.round(100.0/items[row].length) + "%; border: " + borderThickness + "px solid white;\"><div style=\"width: 100%; height: 100%;\" location=\"element_" + row + "_" + col + "\"></div></td>";
			}
			template += "</tr>";
		}
		template += "</table>";
		setTemplateContents(template);
		fillGrid();
	}
	
	public Grid(String location, String caption, com.jandebeule.iot.dashboard.GridItem gridItem) {
		this(location, gridItem);
		if(!caption.isEmpty()) {
			setCaptionAsHtml(true);
			setCaption("<b>" + caption + "</b>");
		}
	}
	
	public Grid(String location, String tab) {
		this(location, ItemsPersistence.getTabItems(location, tab));
	}
	
	public void fillGrid() {
		for(int row=0 ; row<items.length ; row++) {
			for(int col=0 ; col<items[row].length ; col++) {
				Component component = null;
				if(items[row][col] == null) {
					component = createEmptyItem(row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_SUBSCRIBE_LABEL)) {
					component = createMqttLabel(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_SUBSCRIBE_GAUGE)) {
					component = createMqttGauge(items[row][col], row, col, items.length, items[row].length);
				} else if(items[row][col].getType().equals(Type.MQTT_PUBLISH_SELECT)) {
					component = createMqttSelect(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_PUBLISH_SLIDER)) {
					component = createMqttSlider(items[row][col], row, col);
				} else if(items[row][col].getType().equals(Type.MQTT_SEARCH)) {
					component = createMqttSearch(items[row][col], row, col);
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
	
	public Component createEmptyItem(final int row, final int col) {
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
		Component label = new Label();
		return label;
	}
	
	public MqttLabel createMqttLabel(final Item item, final int row, final int col) {
		MqttLabel label = new MqttLabel(item.getCaption(), item.getBroker(), item.getTopic(), item.getUsername(), item.getPassword(), item.getFontSize());
		return label;
	}
	
	public MqttGauge createMqttGauge(final Item item, final int row, final int col, final int rows, final int cols) {
		int rowheight = (int)((Page.getCurrent().getBrowserWindowHeight() - 50) / (double)rows);
		int colWidth = (int)((Page.getCurrent().getBrowserWindowWidth()) / (double)cols);
		MqttGauge gauge = new MqttGauge(item.getCaption(), item.getBroker(), item.getTopic(), item.getUsername(), item.getPassword(), item.getGaugeMin(), item.getGaugeMax(), item.getGaugeHistoryUrl(), Math.min(rowheight, colWidth));
		return gauge;
	}
	
	public MqttSelect createMqttSelect(final Item item, final int row, final int col) {
		MqttSelect select = new MqttSelect(item.getCaption(), item.getBroker(), item.getTopic(), item.getFeedbackTopic(), item.getPublishValuePrefix(), item.getPublishValueMap(), item.getFeedbackToPublishValueMap(), item.getUsername(), item.getPassword());
		return select;
	}
	
	public MqttSlider createMqttSlider(final Item item, final int row, final int col) {
		MqttSlider slider = new MqttSlider(item.getCaption(), item.getBroker(), item.getTopic(), item.getFeedbackTopic(), item.getPublishValuePrefix(), item.getPublishValueMin(), item.getPublishValueMax(), item.getUsername(), item.getPassword());
		return slider;
	}
	
	public MqttSearch createMqttSearch(final Item item, final int row, final int col) {
		MqttSearch search = new MqttSearch(item.getCaption(), item.getBroker(), item.getSearchTopic(), item.getResultTopic(), item.getSelectTopic(), item.getCategories(), item.getUsername(), item.getPassword());
		return search;
	}
	
	public HtmlPage createHtmlPage(final Item item, final int row, final int col) {
		HtmlPage page = new HtmlPage(item.getCaption(), item.getUrl(), item.getUpdateIntervalSeconds());
		return page;
	}
	
	public Grid createGrid(final Item item, final int row, final int col) {
		com.jandebeule.iot.dashboard.GridItem gridItem = new com.jandebeule.iot.dashboard.GridItem();
		gridItem.setItems(item.getGrid());
		gridItem.setBorderThickness(item.getBorderThickness());
		Grid grid = new Grid(location, item.getCaption(), gridItem);
		return grid;
	}
}
