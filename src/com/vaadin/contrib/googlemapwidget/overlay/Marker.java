package com.vaadin.contrib.googlemapwidget.overlay;

import java.awt.geom.Point2D;

/**
 * @author Henri Muurimaa
 */
public interface Marker {
	public Long getId();

	public boolean isVisible();

	public Point2D.Double getLatLng();

	public String getIconUrl();

	public String getTitle();

	public InfoWindowTab[] getInfoWindowContent();

	public boolean isDraggable();
}
