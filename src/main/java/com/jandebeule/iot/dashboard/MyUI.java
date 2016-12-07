package com.jandebeule.iot.dashboard;

import java.io.File;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.jandebeule.iot.dashboard.layout.ExpandableTabsheet;
import com.jandebeule.iot.dashboard.layout.Grid;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Push(value=PushMode.MANUAL)
public class MyUI extends UI {
	
	public boolean initialized = false;
	
    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	vaadinRequest.getService().setSystemMessagesProvider(SystemMessageProvider.get());
    	// get the extension to the URL (without the first '/')
    	String pathInfo = vaadinRequest.getPathInfo().substring(1);
        if("".equals(pathInfo)) {
        	setContent(new Label("Please append a <i>/[location]</i> to your URL where <i>[location]</i> is the parameter used in the name of your <b>items_<i>[location]</i>.xml</b> file that defines the dashboard."
        			+ "<br></br>The xml files need to be placed in the following directory: <i>" + new File("").getAbsolutePath() + "</i>", ContentMode.HTML));
        } else {
	    	List<String> tabs = ItemsPersistence.getTabs(pathInfo);
	    	if(tabs.size() == 1) {
	    		setContent(new Grid(pathInfo, tabs.get(0)));
	    	} else {
	        	final ExpandableTabsheet layout = new ExpandableTabsheet();
		        for(String tab : ItemsPersistence.getTabs(pathInfo)) {
		        	layout.addTab(pathInfo, tab);
		        }
		        setContent(layout);
	    	}
        }
        initialized = true;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    
    
}
