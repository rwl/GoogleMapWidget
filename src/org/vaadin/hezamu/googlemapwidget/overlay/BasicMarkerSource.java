package org.vaadin.hezamu.googlemapwidget.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.hezamu.googlemapwidget.GoogleMap;

public class BasicMarkerSource implements MarkerSource, Serializable {
	private static final long serialVersionUID = -803448463650898130L;

	private final List<Marker> markers = new ArrayList<Marker>();

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

			markerJSON.append("{\"mid\":\"").append(marker.getId());
			markerJSON.append("\",\"lat\":").append(marker.getLatLng().y);
			markerJSON.append(",\"lng\":").append(marker.getLatLng().x);

			// Escape single and double quotes
			markerJSON.append(",\"title\":\"").append(
					marker.getTitle().replaceAll("'", "\'")
							.replaceAll("\"", "\\\\\""));

			markerJSON.append("\",\"visible\":").append(marker.isVisible());
			markerJSON.append(",\"info\":").append(
					marker.getInfoWindowContent() != null);
			markerJSON.append(",\"draggable\":").append(marker.isDraggable());

			if (marker.getIconUrl() != null) {
				markerJSON.append(",\"icon\":\"").append(
						marker.getIconUrl() + "\"");
				if (marker.getIconAnchor() != null) {
					markerJSON.append(",\"iconAnchorX\":").append(
							marker.getIconAnchor().x);
					markerJSON.append(",\"iconAnchorY\":").append(
							marker.getIconAnchor().y);
				} else {
					markerJSON.append(",\"iconAnchorX\":").append(
							marker.getLatLng().x);
					markerJSON.append(",\"iconAnchorY\":").append(
							marker.getLatLng().y);
				}
			}

			markerJSON.append("}");
			if (i != markers.size() - 1) {
				markerJSON.append(",");
			}
		}

		try {
			return ("[" + markerJSON + "]").getBytes("UTF-8");
		} catch (Exception e) {
			return ("[" + markerJSON + "]").getBytes();
		}
	}

	public void registerEvents(GoogleMap map) {
		// This marker source implementation is not interested in map events
	}

	public Marker getMarker(String markerId) {
		// TODO The marker collection should be a map...
		for (Marker marker : markers) {
			if (marker.getId().toString().equals(markerId)) {
				return marker;
			}
		}

		return null;
	}
}
