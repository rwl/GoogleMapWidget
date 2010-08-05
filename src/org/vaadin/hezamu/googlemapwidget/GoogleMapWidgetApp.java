package org.vaadin.hezamu.googlemapwidget;

import java.awt.geom.Point2D;
import java.util.Random;

import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class GoogleMapWidgetApp extends Application {
	private GoogleMap googleMap;

	private BasicMarker mark1;

	private BasicMarker mark2;

	private BasicMarker mark3;

	private BasicMarker mark4;

	private BasicMarker mark5;

	@Override
	public void init() {
		setMainWindow(new Window("Google Map add-on demo"));

		// Create a new map instance centered on the IT Mill offices
		googleMap = new GoogleMap(this, new Point2D.Double(22.3, 60.4522), 8);

		googleMap.setWidth("640px");
		googleMap.setHeight("480px");

		// Create a marker at the IT Mill offices
		mark1 = new BasicMarker(1L, new Point2D.Double(22.3, 60.4522),
				"Test marker 1");

		mark2 = new BasicMarker(2L, new Point2D.Double(22.4, 60.4522),
				"Test marker 2");

		mark3 = new BasicMarker(4L, new Point2D.Double(22.6, 60.4522),
				"Test marker 3");

		mark4 = new BasicMarker(5L, new Point2D.Double(22.7, 60.4522),
				"Test marker 4");

		// Marker with information window pupup
		mark5 = new BasicMarker(6L, new Point2D.Double(22.8, 60.4522),
				"Marker 5");
		mark5.setInfoWindowContent(googleMap, new Label("Hello Marker 5!"));

		Label content = new Label("Hello Marker 2!");
		content.setWidth("60px");
		(mark2).setInfoWindowContent(googleMap, content);

		googleMap.addMarker(mark1);
		googleMap.addMarker(mark2);
		googleMap.addMarker(mark3);
		googleMap.addMarker(mark4);
		googleMap.addMarker(mark5);
		getMainWindow().getContent().addComponent(googleMap);

		// Add a Marker click listener to catch marker click events.
		// Note, works only if marker has information window content
		googleMap.addListener(new GoogleMap.MarkerClickListener() {
			public void markerClicked(Marker clickedMarker) {
				getMainWindow().showNotification(
						"Marker " + clickedMarker.getTitle() + " clicked",
						Notification.TYPE_TRAY_NOTIFICATION);
			}
		});

		// Add a MarkerMovedListener to catch events when a marker is dragged to
		// a new location
		googleMap.addListener(new GoogleMap.MarkerMovedListener() {
			public void markerMoved(Marker movedMarker) {
				getMainWindow().showNotification(
						"Marker " + movedMarker.getTitle() + " moved to "
								+ movedMarker.getLatLng().toString(),
						Notification.TYPE_TRAY_NOTIFICATION);
			}
		});

		addTestButtons(); // Add buttons that trigger tests map features
	}

	private void addTestButtons() {
		GridLayout grid = new GridLayout(5, 1);
		getMainWindow().addComponent(grid);

		grid.addComponent(new Button("Toggle marker 3 draggability",
				new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						mark3.setDraggable(!mark3.isDraggable());
						googleMap.requestRepaint();
					}
				}));

		grid.addComponent(new Button("Toggle marker 4 visibility",
				new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						mark4.setVisible(!mark4.isVisible());
						googleMap.requestRepaint();
					}
				}));

		grid.addComponent(new Button("Randomize Marker 5 location",
				new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						Random r = new Random();

						mark5.setLatLng(new Point2D.Double(
								22.8 + r.nextFloat() / 10, 60.4522 + r
										.nextFloat() / 10));

						googleMap.requestRepaint();
					}
				}));

		grid.addComponent(new Button("Update marker 5 title",
				new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						String chars = new String("asd123.,#€%&öäåÖÄÅ");
						mark5.setTitle(mark5.getTitle()
								+ chars.charAt(new Random().nextInt(chars
										.length())));
						googleMap.requestRepaint();
					}
				}));

		grid.addComponent(new Button("Remove \"Test marker2\"",
				new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						googleMap.removeMarker(mark2);
					}
				}));

		grid.addComponent(new Button("Add \"Test marker2\"",
				new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						googleMap.addMarker(mark2);
					}
				}));

		grid.addComponent(new Button("Toggle marker 1 icon",
				new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						if (mark1.getIconUrl() == null) {
							mark1.setIconUrl("http://bits.ohloh.net/attachments/18966/v_med.gif");
							mark1.setIconAnchor(new Point2D.Double(20, -20));
						} else {
							mark1.setIconUrl(null);
						}

						googleMap.requestRepaint();
					}
				}));

		grid.addComponent(new Button("Toggle client logging",
				new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						if (googleMap.getClientLogLevel() == 0) {
							googleMap.setClientLogLevel(1);
							getMainWindow().showNotification(
									"Client logging enabled",
									Notification.TYPE_TRAY_NOTIFICATION);
						} else {
							googleMap.setClientLogLevel(0);
							getMainWindow().showNotification(
									"Client logging enabled",
									Notification.TYPE_TRAY_NOTIFICATION);
						}
					}
				}));

		// Popup test
		grid.addComponent(new Button("Open a map in a popup",
				new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						Application app = event.getButton().getApplication();

						GoogleMap map2 = new GoogleMap(event.getButton()
								.getApplication(), new Point2D.Double(22.3,
								60.4522), 8);

						map2.setHeight("240px");
						map2.setWidth("240px");

						Window w = new Window("popup");
						w.addComponent(map2);
						w.setHeight("300px");
						w.setWidth("300px");

						app.getMainWindow().addWindow(w);
					}
				}));

		grid.addComponent(new Button("Resize map", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				googleMap.setHeight("200px");
				googleMap.setWidth("200px");
			}
		}));
	}
}
