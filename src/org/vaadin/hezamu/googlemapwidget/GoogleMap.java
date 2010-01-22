package org.vaadin.hezamu.googlemapwidget;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarkerSource;
import org.vaadin.hezamu.googlemapwidget.overlay.InfoWindowTab;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;
import org.vaadin.hezamu.googlemapwidget.overlay.MarkerSource;
import org.vaadin.hezamu.googlemapwidget.overlay.PolyOverlay;
import org.vaadin.hezamu.googlemapwidget.overlay.Polygon;
import org.vaadin.hezamu.googlemapwidget.widgetset.client.ui.VGoogleMap;

import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Server side component for the VGoogleMap widget.
 */
@ClientWidget(VGoogleMap.class)
public class GoogleMap extends AbstractComponent {
	private static final long serialVersionUID = -7237970245561106947L;

	public enum MapControl {
		SmallMapControl, HierarchicalMapTypeControl, LargeMapControl, MapTypeControl, MenuMapTypeControl, OverviewMapControl, ScaleControl, SmallZoomControl
	}

	private Point2D.Double center;

	private Point2D.Double boundsNE;

	private Point2D.Double boundsSW;

	private int zoom;

	private List<MapMoveListener> moveListeners = new ArrayList<MapMoveListener>();

	private List<MapClickListener> mapClickListeners = new ArrayList<MapClickListener>();

	private MarkerSource markerSource = null;

	private Marker clickedMarker = null;

	private boolean closeInfoWindow = false;

	private Map<Long, PolyOverlay> overlays = new HashMap<Long, PolyOverlay>();

	private boolean overlaysChanged = false;

	private boolean scrollWheelZoomEnabled = true;

	private boolean clearMapTypes = false;

	private List<MapControl> controls = new ArrayList<MapControl>();

	private List<CustomMapType> mapTypes = new ArrayList<CustomMapType>();

	private boolean mapTypesChanged = false;

	private ApplicationResource markerResource = new ApplicationResource() {
		private static final long serialVersionUID = -6926454922185543547L;

		public Application getApplication() {
			return GoogleMap.this.getApplication();
		}

		public int getBufferSize() {
			return markerSource.getMarkerJSON().length;
		}

		public long getCacheTime() {
			return -1;
		}

		public String getFilename() {
			return "markersource.txt";
		}

		public DownloadStream getStream() {
			return new DownloadStream(new ByteArrayInputStream(markerSource
					.getMarkerJSON()), getMIMEType(), getFilename());
		}

		public String getMIMEType() {
			return "text/plain";
		}
	};

	/**
	 * Construct a new instance of the map with given size.
	 * 
	 * @param application
	 * @link Application owning this instance.
	 */
	public GoogleMap(Application application) {
		application.addResource(markerResource);
	}

	/**
	 * Construct a new instance of the map with given parameters.
	 * 
	 * @param application
	 * @link Application owning this instance.
	 * @param center
	 *            center of the map as a {@link Point2D.Double}
	 * @param zoom
	 *            initial zoom level of the map
	 */
	public GoogleMap(Application application, Point2D.Double center, int zoom) {
		this(application);

		this.center = center;
		this.zoom = zoom;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addVariable(this, "center_lat", center.y);
		target.addVariable(this, "center_lng", center.x);
		target.addVariable(this, "zoom", zoom);
		target.addVariable(this, "swze", scrollWheelZoomEnabled);

		for (MapControl control : controls) {
			target.addAttribute(control.name(), true);
		}

		// TODO this feels like a kludge, but unsure how to implement correctly
		if (clickedMarker != null) {
			target.addAttribute("marker", clickedMarker.getId().toString());
			target.startTag("tabs");
			InfoWindowTab[] tabs = clickedMarker.getInfoWindowContent();
			for (int i = 0; i < tabs.length; i++) {
				target.startTag("tab");
				if (tabs.length > 1) {
					target.addAttribute("selected", tabs[i].isSelected());
					target.addAttribute("label", tabs[i].getLabel());
				}
				tabs[i].getContent().paint(target);

				target.endTag("tab");
			}
			target.endTag("tabs");

			clickedMarker = null;
		} else if (markerSource != null) {
			target.addAttribute("markerRes", markerResource);
		}

		if (closeInfoWindow) {
			target.addAttribute("closeInfoWindow", true);
			closeInfoWindow = false;
		}

		if (overlaysChanged) {
			target.startTag("overlays");

			for (PolyOverlay poly : overlays.values()) {
				target.startTag("o");
				target.addAttribute("id", poly.getId());

				// Encode polyline points as a string attribute
				StringBuilder sb = new StringBuilder();
				Point2D.Double[] points = poly.getPoints();
				for (int i = 0; i < points.length; i++) {
					if (i > 0) {
						sb.append(" ");
					}
					sb.append("" + points[i].y + "," + points[i].x);
				}
				target.addAttribute("points", sb.toString());

				target.addAttribute("color", poly.getColor());
				target.addAttribute("weight", poly.getWeight());
				target.addAttribute("opacity", poly.getOpacity());
				target.addAttribute("clickable", poly.isClickable());

				if (poly instanceof Polygon) {
					Polygon polygon = (Polygon) poly;
					target.addAttribute("fillcolor", polygon.getFillColor());
					target
							.addAttribute("fillopacity", polygon
									.getFillOpacity());
				}
				target.endTag("o");
			}

			target.endTag("overlays");

			overlaysChanged = false;
		}

		if (clearMapTypes) {
			target.addAttribute("clearMapTypes", true);
			clearMapTypes = false;
		}

		if (mapTypesChanged) {
			target.startTag("mapTypes");

			for (CustomMapType mapType : mapTypes) {
				mapType.paintContent(target);
			}

			target.endTag("mapTypes");

			mapTypesChanged = false;
		}
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey("click_pos")) {
			fireClickEvent(variables.get("click_pos"));
			requestRepaint();
		}

		boolean moveEvent = false;
		Integer intVar;
		if ((intVar = (Integer) variables.get("zoom")) != null) {
			zoom = intVar;
			moveEvent = true;
		}

		String stringVar;
		if ((stringVar = (String) variables.get("center")) != null
				&& !stringVar.trim().equals("")) {
			center = GoogleMap.strToLL(stringVar);
			moveEvent = true;
		}

		if ((stringVar = (String) variables.get("bounds_ne")) != null
				&& !stringVar.trim().equals("")) {
			boundsNE = GoogleMap.strToLL(stringVar);
			moveEvent = true;
		}

		if ((stringVar = (String) variables.get("bounds_sw")) != null
				&& !stringVar.trim().equals("")) {
			boundsSW = GoogleMap.strToLL(stringVar);
			moveEvent = true;
		}

		if (moveEvent) {
			fireMoveEvent();
		}

		if (variables.containsKey("marker")) {
			clickedMarker = markerSource.getMarker(variables.get("marker")
					.toString());
			if (clickedMarker != null
					&& clickedMarker.getInfoWindowContent() != null) {
				requestRepaint();
			}
		}
	}

	private void fireMoveEvent() {
		for (MapMoveListener listener : moveListeners) {
			listener.mapMoved(zoom, center, boundsNE, boundsSW);
		}
	}

	private void fireClickEvent(Object object) {
		Point2D.Double clickPos = GoogleMap.strToLL(object.toString());
		for (MapClickListener listener : mapClickListeners) {
			listener.mapClicked(clickPos);
		}
	}

	/**
	 * Interface for listening map move and zoom events.
	 * 
	 * @author Henri Muurimaa
	 */
	public interface MapMoveListener {
		/**
		 * Handle a MapMoveEvent.
		 * 
		 * @param newZoomLevel
		 *            New zoom level
		 * @param newCenter
		 *            New center coordinates
		 * @param boundsNE
		 *            Coordinates of the north-east corner of the map
		 * @param boundsSW
		 *            Coordinates of the south-west corner of the map
		 */
		public void mapMoved(int newZoomLevel, Point2D.Double newCenter,
				Point2D.Double boundsNE, Point2D.Double boundsSW);
	}

	/**
	 * Interface for listening map click events.
	 * 
	 * @author Henri Muurimaa
	 */
	public interface MapClickListener {
		/**
		 * Handle a MapClickEvent.
		 * 
		 * @param clickPos
		 *            coordinates of the click event.
		 * 
		 */
		public void mapClicked(Point2D.Double clickPos);
	}

	/**
	 * Register a new {@link MapClickListener}.
	 * 
	 * @param listener
	 *            new {@link MapClickListener} to register
	 */
	public void addListener(MapClickListener listener) {
		if (!mapClickListeners.contains(listener)) {
			mapClickListeners.add(listener);
		}
	}

	/**
	 * Deregister a {@link MapClickListener}.
	 * 
	 * @param listener
	 *            {@link MapClickListener} to deregister
	 */
	public void removeListener(MapClickListener listener) {
		if (mapClickListeners.contains(listener)) {
			mapClickListeners.remove(listener);
		}
	}

	/**
	 * Register a new {@link MapMoveListener}.
	 * 
	 * @param listener
	 *            new {@link MapMoveListener} to register
	 */
	public void addListener(MapMoveListener listener) {
		if (!moveListeners.contains(listener)) {
			moveListeners.add(listener);
		}
	}

	/**
	 * Deregister a {@link MapMoveListener}.
	 * 
	 * @param listener
	 *            {@link MapMoveListener} to deregister
	 */
	public void removeListener(MapMoveListener listener) {
		if (moveListeners.contains(listener)) {
			moveListeners.remove(listener);
		}
	}

	/**
	 * Get current center coordinates of the map.
	 * 
	 * @return
	 */
	public Point2D.Double getCenter() {
		return center;
	}

	/**
	 * Set the current center coordinates of the map. This method can be used to
	 * pan the map programmatically.
	 * 
	 * @param center
	 *            the new center coordinates
	 */
	public void setCenter(Point2D.Double center) {
		this.center = center;
		requestRepaint();
	}

	/**
	 * Get the current zoom level of the map.
	 * 
	 * @return the current zoom level
	 */
	public int getZoom() {
		return zoom;
	}

	/**
	 * Set the zoom level of the map. This method can be used to zoom the map
	 * programmatically.
	 * 
	 * @param zoom
	 */
	public void setZoom(int zoom) {
		this.zoom = zoom;
		requestRepaint();
	}

	/**
	 * Get the coordinates of the north-east corner of the map.
	 * 
	 * @return
	 */
	public Point2D.Double getBoundsNE() {
		return boundsNE;
	}

	/**
	 * Get the coordinates of the south-west corner of the map.
	 * 
	 * @return
	 */
	public Point2D.Double getBoundsSW() {
		return boundsSW;
	}

	/**
	 * Set the {@link MarkerSource} for the map.
	 * 
	 * @param markerSource
	 */
	public void setMarkerSource(MarkerSource markerSource) {
		this.markerSource = markerSource;
	}

	/**
	 * Close the currently open info window, if any.
	 */
	public void closeInfoWindow() {
		closeInfoWindow = true;
		requestRepaint();
	}

	/**
	 * Add a new {@link PolyOverlay} to the map. Does nothing if the overlay
	 * already exist on the map.
	 * 
	 * @param overlay
	 *            {@link PolyOverlay} to add
	 * 
	 * @return True if the overlay was added.
	 */
	public boolean addPolyOverlay(PolyOverlay overlay) {
		if (!overlays.containsKey(overlay.getId())) {
			overlays.put(overlay.getId(), overlay);
			overlaysChanged = true;
			requestRepaint();
			return true;
		}

		return false;
	}

	/**
	 * Update a {@link PolyOverlay} on the map. Does nothing if the overlay does
	 * not exist on the map.
	 * 
	 * @param overlay
	 *            {@link PolyOverlay} to update
	 * 
	 * @return True if the overlay was updated.
	 */
	public boolean updateOverlay(PolyOverlay overlay) {
		if (overlays.containsKey(overlay.getId())) {
			overlays.put(overlay.getId(), overlay);
			overlaysChanged = true;
			requestRepaint();
			return true;
		}

		return false;
	}

	/**
	 * Remove a {@link PolyOverlay} from the map. Does nothing if the overlay
	 * does not exist on the map.
	 * 
	 * @param overlay
	 *            {@link PolyOverlay} to remove
	 * 
	 * @return True if the overlay was removed.
	 */
	public boolean removeOverlay(PolyOverlay overlay) {
		if (overlays.containsKey(overlay.getId())) {
			overlays.remove(overlay.getId());
			overlaysChanged = true;
			requestRepaint();
			return true;
		}

		return false;
	}

	private static Point2D.Double strToLL(String latLngStr) {
		if (latLngStr == null) {
			return null;
		}

		String nums[] = latLngStr.split(", ");
		if (nums.length != 2) {
			return null;
		}

		double lat = Double.parseDouble(nums[0].substring(1));

		double lng = Double.parseDouble(nums[1].substring(0,
				nums[1].length() - 1));

		return new Point2D.Double(lng, lat);
	}

	/**
	 * Add a Marker to the current MarkerSource. If the map has no marker source
	 * a new {@link BasicMarkerSource} is created.
	 * 
	 * @param marker
	 *            Marker to add
	 */
	public void addMarker(Marker marker) {
		if (markerSource == null) {
			markerSource = new BasicMarkerSource();
		}

		markerSource.addMarker(marker);
	}

	public void setScrollWheelZoomEnabled(boolean isEnabled) {
		scrollWheelZoomEnabled = isEnabled;
	}

	public boolean isScrollWheelZoomEnabled() {
		return scrollWheelZoomEnabled;
	}

	public boolean addControl(MapControl control) {
		if (!controls.contains(control)) {
			controls.add(control);
			return true;
		}

		return false;
	}

	public boolean hasControl(MapControl control) {
		return controls.contains(control);
	}

	public boolean removeControl(MapControl control) {
		if (controls.contains(control)) {
			controls.remove(control);
			return true;
		}

		return false;
	}

	public void addMapType(String name, int minZoom, int maxZoom,
			String copyright, String tileUrl, boolean isPng, double opacity) {
		mapTypes.add(new CustomMapType(name, minZoom, maxZoom, copyright,
				tileUrl, isPng, opacity));

		mapTypesChanged = true;
		requestRepaint();
	}

	public void clearMapTypes() {
		mapTypes.clear();
		clearMapTypes = true;
		requestRepaint();
	}

	class CustomMapType {
		private double opacity;
		private String tileUrl;
		private boolean isPng;
		private int minZoom;
		private int maxZoom;
		private String copyright;
		private String name;

		public CustomMapType(String name, int minZoom, int maxZoom,
				String copyright, String tileUrl, boolean isPng, double opacity) {
			this.name = name;
			this.minZoom = minZoom;
			this.maxZoom = maxZoom;
			this.copyright = copyright;
			this.tileUrl = tileUrl;
			this.isPng = isPng;
			this.opacity = opacity;
		}

		public void paintContent(PaintTarget target) throws PaintException {
			target.startTag("maptype");
			target.addAttribute("name", name);
			target.addAttribute("minZoom", minZoom);
			target.addAttribute("maxZoom", maxZoom);
			target.addAttribute("copyright", copyright);
			target.addAttribute("tileUrl", tileUrl);
			target.addAttribute("isPng", isPng);
			target.addAttribute("opacity", opacity);
			target.endTag("maptype");
		}

		public double getOpacity() {
			return opacity;
		}

		public String getTileUrl() {
			return tileUrl;
		}

		public boolean isPng() {
			return isPng;
		}

		public int getMinZoom() {
			return minZoom;
		}

		public int getMaxZoom() {
			return maxZoom;
		}

		public String getCopyright() {
			return copyright;
		}
	}
}
