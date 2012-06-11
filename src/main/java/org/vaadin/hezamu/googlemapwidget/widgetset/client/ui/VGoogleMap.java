package org.vaadin.hezamu.googlemapwidget.widgetset.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.maps.client.Copyright;
import com.google.gwt.maps.client.CopyrightCollection;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.TileLayer;
import com.google.gwt.maps.client.control.Control;
import com.google.gwt.maps.client.control.HierarchicalMapTypeControl;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.OverviewMapControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.control.SmallZoomControl;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.MercatorProjection;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.maps.client.overlay.PolygonOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VGoogleMap extends Composite implements Paintable,
		MapClickHandler, MapMoveEndHandler, MarkerDragEndHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-googlemap";

	public static final String CLICK_EVENT_IDENTIFIER = "click";

	private static Boolean loadingApi = false;

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private MapWidget map = null;

	private final Map<String, Marker> knownMarkers = new HashMap<String, Marker>();

	private final Map<Integer, Overlay> knownPolygons = new HashMap<Integer, Overlay>();

	private boolean ignoreVariableChanges = true;

	private long markerRequestSentAt;

	private final List<MapControl> controls = new ArrayList<MapControl>();

	private ArrayList<UIDL> uidl = new ArrayList<UIDL>();

	private final SimplePanel wrapperPanel;

	private String apiKey = "";

	private int logLevel = 0;

	private Timer apiLoadWaitTimer = null;

	public enum MapControl {
		SmallMapControl, HierarchicalMapTypeControl, LargeMapControl, MapTypeControl, MenuMapTypeControl, OverviewMapControl, ScaleControl, SmallZoomControl
	}

	class CustomTileLayer extends TileLayer {
		private final String tileUrl;
		private final boolean isPng;
		private final double opacity;

		public CustomTileLayer(CopyrightCollection copyColl, int minZoom,
				int maxZoom, String tileUrl, boolean isPng, double opacity) {
			super(copyColl, minZoom, maxZoom);

			this.tileUrl = tileUrl;
			this.isPng = isPng;
			this.opacity = opacity;
		}

		@Override
		public boolean isPng() {
			return isPng;
		}

		@Override
		public double getOpacity() {
			return opacity;
		}

		@Override
		public String getTileURL(Point tile, int zoomLevel) {
			String url = tileUrl.replace("{X}", "" + tile.getX())
					.replace("{Y}", "" + tile.getY())
					.replace("{ZOOM}", "" + zoomLevel);

			return url;
		}
	}

	/**
	 * Load the GoogleMaps API asynchronously with the provided key.
	 * 
	 * @param apiKey
	 *            - the API-key to be used.
	 */
	private static native void loadScript(String apiKey)
	/*-{
		var script = $doc.createElement('script');
		script.lang = "javascript";
		script.src = "http://maps.google.com/maps?gwt=1&file=api&key="+apiKey+"&v=2.x&async=2";
		$wnd.document.body.appendChild(script);	
	}-*/;

	/**
	 * Check for the existence of the GoogleMaps API, if not found the API is
	 * loaded.
	 * 
	 * @param apiKey
	 *            - the key to be used if API not loaded
	 * @return true - if API loaded <br />
	 *         false - if no API found
	 */
	private static boolean checkForGoogleMapApi(String apiKey) {
		if (com.google.gwt.maps.client.Maps.isLoaded()) {
			synchronized (loadingApi) {
				if (loadingApi) {
					loadingApi = false;
				}
			}
			return true;
		} else {
			synchronized (loadingApi) {
				if (!loadingApi) {
					loadingApi = true;
					loadScript(apiKey);
				}
			}

			return false;
		}
	}

	/**
	 * Once the API has been loaded, the MapWidget can be initialized. This
	 * method will initialize the MapWidget and place it inside the wrapper from
	 * the composite root.
	 */
	private void loadMap() {
		map = new MapWidget();
		wrapperPanel.add(map);

		// This method call of the Paintable interface sets the component
		// style name in DOM tree
		setStyleName(CLASSNAME);

		map.addMapMoveEndHandler(this);

		map.addMapClickHandler(this);

		// Update all the uidl requests that have been made
		if (uidl != null && uidl.size() > 0) {
			for (UIDL uidl : this.uidl) {
				updateFromUIDL(uidl, client);
			}
		}

		uidl = null;
	}

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VGoogleMap() {
		wrapperPanel = new SimplePanel();

		initWidget(wrapperPanel); // All Composites need to call initWidget()
	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

		String width = uidl.getStringAttribute("width");
		String height = uidl.getStringAttribute("height");
		logLevel = uidl.getIntAttribute("loglevel");

		if (height != null) {
			wrapperPanel.setHeight(height);
		}

		if (width != null) {
			wrapperPanel.setWidth(width);
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		if (map == null) {
			apiKey = uidl.getStringAttribute("apikey");

			if (checkForGoogleMapApi(apiKey)) {
				loadMap();
			} else {
				if (apiLoadWaitTimer == null) {
					apiLoadWaitTimer = new Timer() {

						@Override
						public void run() {

							if (checkForGoogleMapApi(apiKey)) {
								loadMap();

							} else {
								schedule(100);
							}

						}
					};

					apiLoadWaitTimer.schedule(100);
				}

				if (this.uidl == null) {
					ApplicationConnection
							.getConsole()
							.error("The ArrayList holding UIDL updates was NULL, this should never happen!");
					this.uidl = new ArrayList<UIDL>();
				}

				this.uidl.add(uidl);

				return;
			}
		}

		if (apiLoadWaitTimer != null) {
			apiLoadWaitTimer.cancel();
			apiLoadWaitTimer = null;
		}

		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();

		if (uidl.hasAttribute("cached") && uidl.getBooleanAttribute("cached")) {
			return;
		}

		long start = System.currentTimeMillis();

		// Do not send any variable changes while changing the map
		ignoreVariableChanges = true;

		int newZoom = uidl.getIntVariable("zoom");
		if (map.getZoomLevel() != newZoom) {
			map.setZoomLevel(newZoom);
		}

		LatLng newCenter = LatLng.newInstance(
				uidl.getDoubleVariable("center_lat"),
				uidl.getDoubleVariable("center_lng"));

		boolean scrollWheelZoomEnabled = uidl.getBooleanVariable("swze");
		if (map.isScrollWheelZoomEnabled() != scrollWheelZoomEnabled) {
			map.setScrollWheelZoomEnabled(scrollWheelZoomEnabled);
		}

		if (map.getCenter().getLatitude() != newCenter.getLatitude()
				|| map.getCenter().getLongitude() != newCenter.getLongitude()) {
			map.setCenter(newCenter);
		}

		for (MapControl control : MapControl.values()) {
			if (uidl.hasAttribute(control.name())) {
				if (!controls.contains(control)) {
					map.addControl(newControl(control));
					controls.add(control);
				}
			} else if (controls.contains(control)) {
				map.removeControl(newControl(control));
				controls.add(control);
			}
		}

		if (uidl.hasAttribute("markerRes")) {
			String markerUrl = client.translateVaadinUri(uidl
					.getStringAttribute("markerRes"));
			if (markerUrl != null) {
				DeferredCommand
						.addCommand(new MarkerRetrieveCommand(markerUrl));
			}
		}

		if (uidl.hasAttribute("marker")) {
			// When adding the markers we get the ID from JSONString.toString()
			// which includes quotation marks around the ID.
			String markerId = "\"" + uidl.getStringAttribute("marker") + "\"";

			Marker marker = knownMarkers.get(markerId);

			for (final Iterator<Object> it = uidl.getChildIterator(); it
					.hasNext();) {
				final UIDL u = (UIDL) it.next();
				if (!u.getTag().equals("tabs")) {
					continue;
				}

				if (u.getChildCount() == 0) {
					log(1, "No contents for info window");
				} else if (u.getChildCount() == 1) {
					// Only one component in the info window -> no tabbing
					UIDL paintableUIDL = u.getChildUIDL(0).getChildUIDL(0);
					Paintable paintable = client.getPaintable(paintableUIDL);

					map.getInfoWindow().open(marker.getLatLng(),
							new InfoWindowContent((Widget) paintable));

					// Update components in the info window after adding them to
					// DOM so that size calculations can succeed
					paintable.updateFromUIDL(paintableUIDL, client);
				} else {
					int tabs = u.getChildCount();
					// More than one component, show them in info window tabs
					InfoWindowContent.InfoWindowTab[] infoTabs = new InfoWindowContent.InfoWindowTab[tabs];

					Paintable[] paintables = new Paintable[tabs];
					UIDL[] uidls = new UIDL[tabs];

					int selectedId = 0;
					for (int i = 0; i < u.getChildCount(); i++) {
						UIDL childUIDL = u.getChildUIDL(i);
						if (selectedId == 0
								&& childUIDL.getBooleanAttribute("selected")) {
							selectedId = i;
						}

						String label = childUIDL.getStringAttribute("label");

						UIDL paintableUIDL = childUIDL.getChildUIDL(0);
						Paintable paintable = client
								.getPaintable(paintableUIDL);

						paintables[i] = paintable;
						uidls[i] = paintableUIDL;

						infoTabs[i] = new InfoWindowContent.InfoWindowTab(
								label, (Widget) paintable);
					}

					map.getInfoWindow().open(marker.getLatLng(),
							new InfoWindowContent(infoTabs, selectedId));

					// Update paintables after adding them to DOM so that
					// size calculations can succeed
					for (int i = 0; i < paintables.length; i++) {
						paintables[i].updateFromUIDL(uidls[i], client);
					}
				}
			}
		}

		if (uidl.hasAttribute("clearMapTypes")) {
			for (MapType type : map.getMapTypes()) {
				map.removeMapType(type);
			}
		}

		Map<Integer, Overlay> newPolys = new HashMap<Integer, Overlay>();

		// Process polygon/polyline overlays and map types
		for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
			final UIDL u = (UIDL) it.next();
			if (u.getTag().equals("overlays")) {

				long nodeStart = System.currentTimeMillis();

				for (final Iterator<Object> iter = u.getChildIterator(); iter
						.hasNext();) {
					final UIDL polyUIDL = (UIDL) iter.next();

					Overlay poly = null;
					if (polyUIDL.hasAttribute("fillcolor")) {
						poly = polygonFromUIDL(polyUIDL);
					} else {
						poly = polylineFromUIDL(polyUIDL);
					}

					if (poly != null) {
						newPolys.put(polyUIDL.getIntAttribute("id"), poly);
					}
				}

				log(1,
						"Polygon overlays processed in "
								+ (System.currentTimeMillis() - nodeStart)
								+ "ms");
			} else if (u.getTag().equals("mapTypes")) {
				long nodeStart = System.currentTimeMillis();

				for (final Iterator<Object> iter = u.getChildIterator(); iter
						.hasNext();) {
					map.addMapType(mapTypeFromUIDL((UIDL) iter.next()));
				}

				log(1, "Map types processed in "
						+ (System.currentTimeMillis() - nodeStart) + "ms");
			}
		}

		// Remove deleted overlays from the map
		List<Integer> removedPolyIds = new ArrayList<Integer>();
		for (Entry<Integer, Overlay> entry : knownPolygons.entrySet()) {
			if (!newPolys.containsKey(entry.getKey())) {
				map.removeOverlay(entry.getValue());
				removedPolyIds.add(entry.getKey());
			}
		}

		// ... and from the map. Can't remove them while iterating the
		// collection
		for (Integer id : removedPolyIds)
			knownPolygons.remove(id);

		// Add new overlays
		for (Entry<Integer, Overlay> entry : newPolys.entrySet()) {
			if (!knownPolygons.containsKey(entry.getKey())) {
				knownPolygons.put(entry.getKey(), entry.getValue());
				map.addOverlay(entry.getValue());
			}
		}

		if (uidl.hasAttribute("closeInfoWindow")) {
			map.getInfoWindow().close();
		}

		ignoreVariableChanges = false;

		if (uidl.hasAttribute("reportBounds")
				&& uidl.getBooleanAttribute("reportBounds") == true) {
			reportMapBounds();
		}

		log(1,
				"IGoogleMap.updateFromUIDL() took "
						+ (System.currentTimeMillis() - start) + "ms");
	}

	private MapType mapTypeFromUIDL(UIDL maptypeUIDL) {
		int minZoom = maptypeUIDL.getIntAttribute("minZoom");
		int maxZoom = maptypeUIDL.getIntAttribute("maxZoom");
		String copyright = maptypeUIDL.getStringAttribute("copyright");
		String name = maptypeUIDL.getStringAttribute("name");
		String tileUrl = maptypeUIDL.getStringAttribute("tileUrl");
		boolean isPng = maptypeUIDL.getBooleanAttribute("isPng");
		double opacity = maptypeUIDL.getDoubleAttribute("opacity");

		CopyrightCollection myCopyright = new CopyrightCollection();

		myCopyright.addCopyright(new Copyright(1, LatLngBounds.newInstance(
				LatLng.newInstance(-90, -180), LatLng.newInstance(90, 180)),
				minZoom, copyright));

		return new MapType(new TileLayer[] { new CustomTileLayer(myCopyright,
				minZoom, maxZoom, tileUrl, isPng, opacity) },
				new MercatorProjection(maxZoom - minZoom + 1), name);
	}

	private Control newControl(MapControl control) {
		if (control.equals(MapControl.SmallMapControl)) {
			return new SmallMapControl();
		}
		if (control.equals(MapControl.HierarchicalMapTypeControl)) {
			return new HierarchicalMapTypeControl();
		}
		if (control.equals(MapControl.LargeMapControl)) {
			return new LargeMapControl();
		}
		if (control.equals(MapControl.MapTypeControl)) {
			return new MapTypeControl();
		}
		if (control.equals(MapControl.MenuMapTypeControl)) {
			return new MenuMapTypeControl();
		}
		if (control.equals(MapControl.OverviewMapControl)) {
			return new OverviewMapControl();
		}
		if (control.equals(MapControl.ScaleControl)) {
			return new ScaleControl();
		}
		if (control.equals(MapControl.SmallZoomControl)) {
			return new SmallZoomControl();
		}

		log(1, "Unknown control: " + control);

		return null;
	}

	private Polyline polylineFromUIDL(UIDL polyUIDL) {
		String[] encodedPoints = polyUIDL.getStringAttribute("points").split(
				" ");
		LatLng[] points = new LatLng[encodedPoints.length];
		for (int i = 0; i < encodedPoints.length; i++) {
			String[] p = encodedPoints[i].split(",");
			double lat = Double.parseDouble(p[0]);
			double lng = Double.parseDouble(p[1]);
			points[i] = LatLng.newInstance(lat, lng);
		}

		String color = polyUIDL.getStringAttribute("color");
		int weight = polyUIDL.getIntAttribute("weight");
		double opacity = polyUIDL.getDoubleAttribute("opacity");
		boolean clickable = polyUIDL.getBooleanAttribute("clickable");

		return new Polyline(points, color, weight, opacity,
				PolylineOptions.newInstance(clickable, false));
	}

	private Polygon polygonFromUIDL(UIDL polyUIDL) {
		String[] encodedPoints = polyUIDL.getStringAttribute("points").split(
				" ");
		LatLng[] points = new LatLng[encodedPoints.length];
		for (int i = 0; i < encodedPoints.length; i++) {
			String[] p = encodedPoints[i].split(",");
			double lat = Double.parseDouble(p[0]);
			double lng = Double.parseDouble(p[1]);
			points[i] = LatLng.newInstance(lat, lng);
		}

		String color = polyUIDL.getStringAttribute("color");
		int weight = polyUIDL.getIntAttribute("weight");
		double opacity = polyUIDL.getDoubleAttribute("opacity");
		String fillColor = polyUIDL.getStringAttribute("fillcolor");
		double fillOpacity = polyUIDL.getDoubleAttribute("fillopacity");
		boolean clickable = polyUIDL.getBooleanAttribute("clickable");

		return new Polygon(points, color, weight, opacity, fillColor,
				fillOpacity, PolygonOptions.newInstance(clickable));
	}

	private Marker createMarker(JSONNumber jsLat, JSONNumber jsLng,
			JSONString jsTitle, JSONBoolean jsVisible, JSONString jsIcon,
			int iconAnchorX, int iconAnchorY, JSONBoolean jsDraggable) {

		Icon icon = null;
		if (jsIcon != null) {
			icon = Icon.newInstance(jsIcon.stringValue());
			icon.setIconAnchor(Point.newInstance(iconAnchorX, iconAnchorY));

			log(1, "Icon URL '" + jsIcon.stringValue() + "' at anchor point ("
					+ iconAnchorX + "," + iconAnchorY + ")");
		}

		MarkerOptions mopts;
		if (icon != null) {
			mopts = MarkerOptions.newInstance(icon);
		} else {
			mopts = MarkerOptions.newInstance();
		}

		mopts.setTitle(jsTitle.stringValue());
		mopts.setDraggable(jsDraggable.booleanValue());

		final double lat = jsLat.doubleValue();
		final double lng = jsLng.doubleValue();

		if (lat < -90 || lat > 90) {
			log(1, "Invalid latitude for marker: " + lat);
			return null;
		}

		if (lng < -180 || lng > 180) {
			log(1, "Invalid latitude for marker: " + lat);
			return null;
		}

		Marker result = new Marker(LatLng.newInstance(lat, lng), mopts);
		result.setVisible(jsVisible.booleanValue());

		return result;
	}

	private String getMarkerIconURL(Marker marker) {
		if (marker.getIcon() == null)
			return null;

		return marker.getIcon().getImageURL();
	}

	public void onClick(MapClickEvent event) {
		if (ignoreVariableChanges) {
			return;
		}

		if (event.getOverlay() != null) {
			return;
		}

		client.updateVariable(paintableId, "click_pos", event.getLatLng()
				.toString(), true);
	}

	public void onMoveEnd(MapMoveEndEvent event) {
		if (ignoreVariableChanges) {
			return;
		}

		reportMapBounds();
	}

	private void reportMapBounds() {
		client.updateVariable(paintableId, "zoom", map.getZoomLevel(), false);
		client.updateVariable(paintableId, "bounds_ne", map.getBounds()
				.getNorthEast().toString(), false);
		client.updateVariable(paintableId, "bounds_sw", map.getBounds()
				.getSouthWest().toString(), false);
		client.updateVariable(paintableId, "center",
				map.getCenter().toString(), true);
	}

	public void onDragEnd(MarkerDragEndEvent event) {
		Marker marker = (Marker) event.getSource();

		Set<String> keys = knownMarkers.keySet();
		for (String key : keys) {

			// Find the key for the moved marker
			if (knownMarkers.get(key).equals(marker)) {
				client.updateVariable(paintableId, "markerMovedId", key, false);
				client.updateVariable(paintableId, "markerMovedLat", marker
						.getLatLng().getLatitude(), false);
				client.updateVariable(paintableId, "markerMovedLong", marker
						.getLatLng().getLongitude(), true);
				break;
			}
		}
	}

	protected void markerClicked(String mId) {
		client.updateVariable(paintableId, "marker", mId, true);
	}

	private void log(int level, String message) {
		if (level <= logLevel) {
			// Show message in GWT console
			System.out.println(message);

			// And also in Vaadin debug window
			ApplicationConnection.getConsole().log(message);
		}
	}

	class InfoWindowOpener implements MarkerClickHandler {
		private final String markerId;

		InfoWindowOpener(String markerId) {
			super();
			this.markerId = markerId;
		}

		public void onClick(MarkerClickEvent event) {
			markerClicked(markerId);
		}
	}

	class MarkerRetrieveCommand implements Command {
		private final String markerUrl;

		MarkerRetrieveCommand(String markerUrl) {
			super();
			this.markerUrl = markerUrl;
		}

		public void execute() {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
					markerUrl);

			try {
				builder.setTimeoutMillis(2000);

				markerRequestSentAt = System.currentTimeMillis();

				builder.sendRequest(null, new RequestCallback() {
					public void onError(Request request, Throwable e) {
						if (e instanceof RequestTimeoutException) {
							log(1,
									"Timeout fetching marker data: "
											+ e.getMessage());
						} else {
							log(1,
									"Error fetching marker data: "
											+ e.getMessage());
						}
					}

					public void onResponseReceived(Request request,
							Response response) {
						String markerJSON = response.getText();

						System.out.println(""
								+ markerJSON.length()
								+ " bytes of marker response got in "
								+ (System.currentTimeMillis() - markerRequestSentAt)
								+ "ms");

						JSONArray array = null;
						try {
							long start = System.currentTimeMillis();
							JSONValue json = JSONParser.parse(markerJSON);
							array = json.isArray();
							log(1,
									"JSON parsed in "
											+ (System.currentTimeMillis() - start)
											+ "ms");
							if (array == null) {
								System.out
										.println("Marker JSON was not an array.");
								return;
							}

							handleMarkerJSON(array);
						} catch (Exception e) {
							log(1, "Error parsing json: " + e.getMessage());
						}
					}
				});
			} catch (RequestException e) {
				log(1, "Failed to send the request: " + e.getMessage());
			}
		}

		private void handleMarkerJSON(JSONArray array) {
			synchronized (knownMarkers) {

				JSONValue value;
				long startTime = System.currentTimeMillis();
				int initSize = knownMarkers.size();
				List<String> markersFromThisUpdate = new ArrayList<String>();

				for (int i = 0; i < array.size(); i++) {
					JSONObject jsMarker;
					JSONString jsMID, jsTitle, jsIcon;
					JSONNumber jsLat, jsLng;
					JSONBoolean jsVisible, jsHasInfo, jsDraggable;
					Marker marker = null;
					boolean isOldMarker = false;
					boolean replaceMarker = false;

					if ((jsMarker = array.get(i).isObject()) == null) {
						continue;
					}

					// Read marker id
					if ((value = jsMarker.get("mid")) == null) {
						continue;
					}
					if ((jsMID = value.isString()) == null) {
						continue;
					}

					if ((value = jsMarker.get("draggable")) == null) {
						continue;
					} else {
						if (knownMarkers.containsKey(jsMID.toString())) {
							marker = knownMarkers.get(jsMID.toString());
							marker.setDraggingEnabled((((JSONBoolean) jsMarker
									.get("draggable")).booleanValue()));
							isOldMarker = true;
						}
					}

					// Add maker to list of markers in this update
					markersFromThisUpdate.add(jsMID.toString());

					// Read marker latitude
					if ((value = jsMarker.get("lat")) == null) {
						if (!isOldMarker)
							continue;
					}
					if ((jsLat = value.isNumber()) == null) {
						if (!isOldMarker)
							continue;
					}

					// Read marker longitude
					if ((value = jsMarker.get("lng")) == null) {
						if (!isOldMarker)
							continue;
					}
					if ((jsLng = value.isNumber()) == null) {
						if (!isOldMarker)
							continue;
					} else {
						// marker.setLatLng(jsLng.doubleValue());
					}

					// Read marker title
					if ((value = jsMarker.get("title")) == null) {
						if (!isOldMarker)
							continue;
					}
					if ((jsTitle = value.isString()) == null) {
						if (!isOldMarker)
							continue;
					} else {
						if (isOldMarker && marker != null) {
							String title = marker.getTitle();

							// if title is changed
							if (!jsTitle.stringValue().equals(title)) {
								replaceMarker = true;
								log(1, "Title changed: " + marker.getTitle());
							}
						}
					}

					// Read marker visibility
					if ((value = jsMarker.get("visible")) == null) {
						if (!isOldMarker)
							continue;
					}
					if ((jsVisible = value.isBoolean()) == null) {
						if (!isOldMarker)
							continue;
					} else {
						if (marker != null) {
							boolean old = marker.isVisible();

							marker.setVisible(jsVisible.booleanValue());

							if (old != marker.isVisible()) {
								log(1,
										"Toggled marker '" + marker.getTitle()
												+ "' visibility to "
												+ jsVisible.booleanValue());
							}
						}
					}

					// Read marker draggability (is that a word? :)
					if ((value = jsMarker.get("draggable")) == null) {
						if (!isOldMarker)
							continue;
					}

					if ((jsDraggable = value.isBoolean()) == null) {
						if (!isOldMarker)
							continue;
					}

					// Change position, if changed
					if (marker != null && jsLat != null && jsLng != null
							&& marker.getLatLng() != null) {
						LatLng llang = marker.getLatLng();

						LatLng llang2 = LatLng.newInstance(jsLat.doubleValue(),
								jsLng.doubleValue());
						if (!llang.isEquals(llang2)) {
							marker.setLatLng(llang2);
						}
					}

					// Read marker icon
					if ((value = jsMarker.get("icon")) == null) {
						jsIcon = null;
						if (marker != null) {
							String currentURL = getMarkerIconURL(marker);
							if (!currentURL
									.startsWith("http://maps.gstatic.com")
									&& currentURL != null && currentURL != "") {
								replaceMarker = true;
								log(1, "Icon url changed " + marker.getTitle()
										+ " from '" + currentURL + "'");
							}
						}
					} else if ((jsIcon = value.isString()) == null) {
						if (!isOldMarker)
							continue;
					} else {
						if (marker != null
								&& getMarkerIconURL(marker) != jsIcon
										.toString()) {
							replaceMarker = true;
							log(1, "Icon url changed 2 " + marker.getTitle());
						}
					}

					int iconAnchorX = 0;
					if ((value = jsMarker.get("iconAnchorX")) != null) {
						JSONNumber jsAnchorX;
						if ((jsAnchorX = value.isNumber()) != null) {
							log(1, "Anchor X: " + jsAnchorX.toString());
							iconAnchorX = (int) Math.round(jsAnchorX
									.doubleValue());
						} else {
							log(1, "Anchor X NaN");
						}
					}

					int iconAnchorY = 0;
					if ((value = jsMarker.get("iconAnchorY")) != null) {
						JSONNumber jsAnchorY;
						if ((jsAnchorY = value.isNumber()) != null) {
							iconAnchorY = (int) Math.round(jsAnchorY
									.doubleValue());
						}
					}

					// do not create new one if old found (only if we want to
					// replace it)
					if (isOldMarker && !replaceMarker)
						continue;

					if (!isOldMarker)
						replaceMarker = false; // Never replace a marker if
												// there is no previous one

					if (replaceMarker) {
						log(1, "Replacing marker " + marker.getTitle());
						map.removeOverlay(marker);
						markersFromThisUpdate.remove(marker);
					}

					marker = createMarker(jsLat, jsLng, jsTitle, jsVisible,
							jsIcon, iconAnchorX, iconAnchorY, jsDraggable);

					if (marker != null) {
						map.addOverlay(marker);

						// Add dragEnd handlers to marker
						marker.addMarkerDragEndHandler(VGoogleMap.this);

						// Read boolean telling if marker has a info window
						if ((value = jsMarker.get("info")) != null) {
							if ((jsHasInfo = value.isBoolean()) != null
									&& jsHasInfo.booleanValue()) {
								marker.addMarkerClickHandler(new InfoWindowOpener(
										jsMID.stringValue()));

							}
						}

						knownMarkers.put(jsMID.toString(), marker);
					}
				}

				int newMarkers = knownMarkers.size() - initSize;

				long dur = System.currentTimeMillis() - startTime;

				if (newMarkers == 0) {
					log(1, "No new markers added in " + dur + "ms.");
				} else {
					log(1, "" + newMarkers + " markers added in " + dur
							+ "ms: " + dur / newMarkers + "ms per marker");
				}

				// Remove markers that wasn't in the update (i.e. removed on
				// server side)
				List<String> removedMarkers = new ArrayList<String>();
				for (String mID : knownMarkers.keySet()) {
					if (!markersFromThisUpdate.contains(mID)) {
						map.removeOverlay(knownMarkers.get(mID));
						removedMarkers.add(mID);
					}
				}

				for (String mID : removedMarkers) {
					knownMarkers.remove(mID);
				}
			}
		}
	}

	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		wrapperPanel.setHeight(height);

		if (map != null) {
			map.setHeight(height);
		} else {
			ApplicationConnection.getConsole().error(
					"Set height attempted before map initialized");
		}
	}

	@Override
	public void setWidth(String width) {
		super.setWidth(width);
		wrapperPanel.setWidth(width);

		if (map != null) {
			map.setWidth(width);
		} else {
			ApplicationConnection.getConsole().error(
					"Set width attempted before map initialized");
		}
	}
}
