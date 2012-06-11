package org.vaadin.hezamu.googlemapwidget.overlay;

import com.vaadin.ui.Component;

public class InfoWindowTab {

	private Component content;

	private String label;

	private boolean selected;

	public InfoWindowTab(Component parent, Component content) {
		this(parent, content, null, false);
	}

	public InfoWindowTab(Component parent, Component content, String label) {
		this(parent, content, label, false);
	}

	public InfoWindowTab(Component parent, Component content, String label,
			boolean selected) {
		this.content = content;
		this.content.setParent(parent);

		this.label = label;
		this.selected = selected;
	}

	public Component getContent() {
		return content;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
