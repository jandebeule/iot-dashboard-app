package com.jandebeule.iot.dashboard.layout;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class ExpandableTabsheet extends TabSheet {

	public ExpandableTabsheet() {
		setSizeFull();
		
		// for the moment we don't support adding new tabs on the fly
		
//		addTab(new Label(), "+");
//		addSelectedTabChangeListener(new SelectedTabChangeListener() {
//			@Override
//			public void selectedTabChange(SelectedTabChangeEvent event) {
//				if(getTab(getSelectedTab()).getCaption().equals("+")) {
//					final Window popup = new Window("Tab caption");
//					popup.setWidth("350px");
//					popup.setHeight("200px");
//					VerticalLayout layout = new VerticalLayout();
//					layout.setSizeFull();
//					layout.setSpacing(true);
//					layout.setMargin(true);
//					TextField caption = new TextField("Caption for the new tab:");
//					caption.setWidth("100%");
//					layout.addComponent(caption);
//					Button ok = new Button("OK", new Button.ClickListener() {
//						@Override
//						public void buttonClick(ClickEvent event) {
//							Tab tab = addTab(caption.getValue());
//							setSelectedTab(tab);
//							UI.getCurrent().removeWindow(popup);
//						}
//					});
//					layout.addComponent(ok);
//					popup.setContent(layout);
//					popup.setModal(true);
//					caption.focus();
//					UI.getCurrent().addWindow(popup);
//					popup.center();
//				}
//			}
//		});
	}
	
	public Tab addTab(String location, String caption) {
		//removeTab(getTab(getComponentCount() - 1));
		Tab tab = addTab(new Grid(location, caption), caption);
		//addTab(new Label(), "+");
		return tab;
	}
	
	
}
