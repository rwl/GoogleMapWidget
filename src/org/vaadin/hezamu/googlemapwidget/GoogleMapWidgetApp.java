package org.vaadin.hezamu.googlemapwidget;

import java.awt.geom.Point2D;

import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class GoogleMapWidgetApp extends Application {
	private static final long serialVersionUID = -921015383668899594L;

	@Override
	public void init() {
		setMainWindow(new Window("GoogleMapWidgetApp"));

		// Create a new map instance centered on the IT Mill offices
		GoogleMap googleMap = new GoogleMap(this, new Point2D.Double(22.3,
				60.4522), 8);

		googleMap.setWidth("640px");
		googleMap.setHeight("480px");

		// Create a marker at the IT Mill offices
		googleMap.addMarker(new BasicMarker(1L, new Point2D.Double(22.3,
				60.4522), "Test marker"));

		getMainWindow().addComponent(googleMap);
	}
}
