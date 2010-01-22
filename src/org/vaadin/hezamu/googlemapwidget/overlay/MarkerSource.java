package org.vaadin.hezamu.googlemapwidget.overlay;

import java.util.List;

import org.vaadin.hezamu.googlemapwidget.GoogleMap;


public interface MarkerSource {
	public List<Marker> getMarkers();

	public boolean addMarker(Marker newMarker);

	public void registerEvents(GoogleMap map);

	public byte[] getMarkerJSON();

	public Marker getMarker(String markerId);
}
