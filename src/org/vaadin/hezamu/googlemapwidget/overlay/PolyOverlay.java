package org.vaadin.hezamu.googlemapwidget.overlay;

import java.awt.geom.Point2D;

public class PolyOverlay {
	private Long id;

	private Point2D.Double[] points;

	private String color;

	private int weight;

	private double opacity;

	private boolean clickable;

	public PolyOverlay(Long id, Point2D.Double[] points) {
		this(id, points, "#ffffff", 1, 1.0, false);
	}

	public PolyOverlay(Long id, Point2D.Double[] points, String color,
			int weight, double opacity, boolean clickable) {
		this.id = id;
		this.points = points;
		this.color = color;
		this.weight = weight;
		this.opacity = opacity;
		this.clickable = clickable;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Point2D.Double[] getPoints() {
		return points;
	}

	public void setPoints(Point2D.Double[] points) {
		this.points = points;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public double getOpacity() {
		return opacity;
	}

	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
}
