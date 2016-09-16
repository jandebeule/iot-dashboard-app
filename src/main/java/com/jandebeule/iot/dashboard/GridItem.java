package com.jandebeule.iot.dashboard;

public class GridItem {
	private Item[][] items;
	private int borderThickness = 0;
	public Item[][] getItems() {
		return items;
	}
	public void setItems(Item[][] items) {
		this.items = items;
	}
	public int getBorderThickness() {
		return borderThickness;
	}
	public void setBorderThickness(int borderThickness) {
		this.borderThickness = borderThickness;
	}
	
	
}
