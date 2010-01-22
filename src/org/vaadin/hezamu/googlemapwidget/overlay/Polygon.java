package org.vaadin.hezamu.googlemapwidget.overlay;

import java.awt.geom.Point2D;

public class Polygon extends PolyOverlay {

	private String fillColor;

	private double fillOpacity;

	public Polygon(Long id, Point2D.Double[] points) {
		this(id, points, "#ffffff", 1, 1.0, "#777777", 0.2, false);
	}

	public Polygon(Long id, Point2D.Double[] points, String strokeColor,
			int strokeWeight, double strokeOpacity, String fillColor,
			double fillOpacity, boolean clickable) {
		super(id, points, strokeColor, strokeWeight, strokeOpacity, clickable);

		this.fillColor = fillColor;
		this.fillOpacity = fillOpacity;
	}

	public String getFillColor() {
		return fillColor;
	}

	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}

	public double getFillOpacity() {
		return fillOpacity;
	}

	public void setFillOpacity(double fillOpacity) {
		this.fillOpacity = fillOpacity;
	}
}
