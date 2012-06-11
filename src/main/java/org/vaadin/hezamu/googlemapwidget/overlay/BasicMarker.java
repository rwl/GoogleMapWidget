package org.vaadin.hezamu.googlemapwidget.overlay;

import java.awt.geom.Point2D;

import com.vaadin.ui.Component;

public class BasicMarker implements Marker {

	private Long id;

	private boolean visible = true;

	private Point2D.Double latLng;

	private String iconUrl = null;

	private Point2D.Double iconAnchor;

	private String title = null;

	private InfoWindowTab[] infoWindowContent = null;

	private boolean draggable = true;

	public BasicMarker(Long id, Point2D.Double latLng, String title) {
		this.id = id;
		this.latLng = latLng;
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Point2D.Double getLatLng() {
		return latLng;
	}

	public void setLatLng(Point2D.Double latLng) {
		this.latLng = latLng;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String imageUrl) {
		iconUrl = imageUrl;
	}

	public Point2D.Double getIconAnchor() {
		return iconAnchor;
	}

	public void setIconAnchor(Point2D.Double iconAnchor) {
		this.iconAnchor = iconAnchor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public InfoWindowTab[] getInfoWindowContent() {
		return infoWindowContent;
	}

	public void setInfoWindowContent(InfoWindowTab[] tabs) {
		infoWindowContent = tabs;
	}

	public void setInfoWindowContent(Component parent, Component component) {
		infoWindowContent = new InfoWindowTab[] { new InfoWindowTab(parent,
				component) };
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

}
