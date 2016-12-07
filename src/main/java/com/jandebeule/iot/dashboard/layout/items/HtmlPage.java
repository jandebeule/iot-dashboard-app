package com.jandebeule.iot.dashboard.layout.items;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.UI;

// refreshable HTML page
public class HtmlPage extends BrowserFrame {

	BackgroundRunnable backgroundThread;
	String url;
	String caption;
	int updateIntervalSeconds;
	
	public HtmlPage(String caption, String url, int updateIntervalSeconds) {
		setCaptionAsHtml(true);
		setCaption("<b>" + caption + "</b>");
		setSizeUndefined();
		setHeight("100%");
		this.caption = caption;
		this.url = url;
		this.updateIntervalSeconds = updateIntervalSeconds;
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				backgroundThread = new BackgroundRunnable(caption, url, updateIntervalSeconds);
				new Thread(backgroundThread).start();
			}
		});
		addDetachListener(new DetachListener() {
			@Override
			public void detach(DetachEvent event) {
				if(backgroundThread != null) {
					backgroundThread.stop();
				}
			}
		});
	}
	
	private class BackgroundRunnable implements Runnable {
		private boolean running = true;
		private String caption;
		private String url;
		private int updateIntervalSecs;
		
		public BackgroundRunnable(String caption, String url, int updateIntervalSecs) {
			this.caption = caption;
			this.url = url;
			this.updateIntervalSecs = updateIntervalSecs;
		}
		
		public void run() {
			try { Thread.sleep(3000); } catch (Exception ex) {}
			while(running) {
				try {
					UI.getCurrent().getSession().getLockInstance().lock();
					try {
						setSource(new ExternalResource(url));
						UI.getCurrent().push();
					} finally {
						UI.getCurrent().getSession().getLockInstance().unlock();
					}
					System.out.println("Updated HtmlPage '" + caption + "', url: " + url);
				} catch (Exception ex) {
					System.out.println("Unable to update HtmlPage '" + caption + "')");
					ex.printStackTrace();
				}
				try { Thread.sleep(updateIntervalSecs*1000); } catch (Exception ex) {}
			}
		}
		
		public void stop() {
			System.out.println("Stopping thread for HtmlPage '" + caption + "'");
			running = false;
		}
	}
	
}
