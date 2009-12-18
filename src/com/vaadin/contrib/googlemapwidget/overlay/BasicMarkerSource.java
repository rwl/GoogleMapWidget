package com.vaadin.contrib.googlemapwidget.overlay;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.contrib.googlemapwidget.GoogleMap;

public class BasicMarkerSource implements MarkerSource {

	private List<Marker> markers = new ArrayList<Marker>();

	public List<Marker> getMarkers() {
		return markers;
	}

	public boolean addMarker(Marker newMarker) {
		if (markers.contains(newMarker)) {
			return false;
		}

		markers.add(newMarker);

		return true;
	}

	public byte[] getMarkerJSON() {
		// 1000 markers, using String concatenation: 8100ms
		// using StringBuilder: 17ms :)
		StringBuilder markerJSON = new StringBuilder();

		for (int i = 0; i < markers.size(); i++) {
			Marker marker = markers.get(i);

			markerJSON.append("{\"mid\":\"" + marker.getId());
			markerJSON.append("\",\"lat\":" + marker.getLatLng().y);
			markerJSON.append(",\"lng\":" + marker.getLatLng().x);
			markerJSON.append(",\"title\":\"" + marker.getTitle());
			markerJSON.append("\",\"visible\":" + marker.isVisible());
			markerJSON.append(",\"info\":"
					+ (marker.getInfoWindowContent() != null));
			markerJSON.append(",\"draggable\":" + marker.isDraggable());
			if (marker.getIconUrl() != null) {
				markerJSON.append(",\"icon\":\"" + marker.getIconUrl() + "\"");
			}

			markerJSON.append("}");
			if (i != markers.size() - 1) {
				markerJSON.append(",");
			}
		}

		return ("[" + markerJSON + "]").getBytes();
	}

	public void registerEvents(GoogleMap map) {
		// This marker source implementation is not interested in map events
	}

	public Marker getMarker(String markerId) {
		// TODO This is pretty silly, the marker collection should be a map
		for (Marker marker : markers) {
			if (marker.getId().toString().equals(markerId)) {
				return marker;
			}
		}

		return null;
	}
}
