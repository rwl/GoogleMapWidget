package com.vaadin.contrib.googlemapwidget.overlay;

import java.util.List;

import com.vaadin.contrib.googlemapwidget.GoogleMap;

public interface MarkerSource {
	public List<Marker> getMarkers();

	public boolean addMarker(Marker newMarker);

	public void registerEvents(GoogleMap map);

	public byte[] getMarkerJSON();

	public Marker getMarker(String markerId);
}
