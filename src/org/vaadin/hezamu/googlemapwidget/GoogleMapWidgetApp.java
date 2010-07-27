package org.vaadin.hezamu.googlemapwidget;

import java.awt.geom.Point2D;
import java.util.Random;

import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class GoogleMapWidgetApp extends Application {
	private static final long serialVersionUID = -921015383668899594L;
	private Marker mark1;
	private Marker mark2;
	private GoogleMap googleMap;
	private BasicMarker mark3;
	private BasicMarker mark4;
	private BasicMarker mark5;
	private final int i = 0;

	@Override
	public void init() {
		setMainWindow(new Window("GoogleMapWidgetApp"));

		// Create a new map instance centered on the IT Mill offices
		googleMap = new GoogleMap(this, new Point2D.Double(22.3, 60.4522), 8);

		googleMap.setWidth("640px");
		googleMap.setHeight("480px");

		// Create a marker at the IT Mill offices
		mark1 = new BasicMarker(1L, new Point2D.Double(22.3, 60.4522),
				"Test marker1");

		mark2 = new BasicMarker(2L, new Point2D.Double(22.4, 60.4522),
				"Test marker2 ");

		mark3 = new BasicMarker(4L, new Point2D.Double(22.6, 60.4522),
				"Test marker3 ");

		mark4 = new BasicMarker(5L, new Point2D.Double(22.7, 60.4522),
				"Test marker4");

		// Marker with information window pupup
		mark5 = new BasicMarker(6L, new Point2D.Double(22.8, 60.4522),
				"Marker5");
		mark5.setInfoWindowContent(googleMap, new Label("Hello"));

		Label content = new Label("Hello");
		content.setWidth("60px");
		((BasicMarker) mark2).setInfoWindowContent(googleMap, content);

		googleMap.addMarker(mark1);
		googleMap.addMarker(mark2);
		googleMap.addMarker(mark3);
		googleMap.addMarker(mark4);
		googleMap.addMarker(mark5);
		getMainWindow().getContent().addComponent(googleMap);

		final Button b = new Button("Marker 3 is draggable: "
				+ mark3.isDraggable());
		b.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -9120699719811785224L;

			@Override
			public void buttonClick(ClickEvent event) {
				mark3.setDraggable(!mark3.isDraggable());
				b.setCaption("Marker 3 is draggable: " + mark3.isDraggable());
				googleMap.requestRepaint();
			}

		});

		final Button b2 = new Button("Marker 4 is visible: "
				+ mark4.isVisible());
		b2.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 3281713274525655809L;

			@Override
			public void buttonClick(ClickEvent event) {
				mark4.setVisible(!mark4.isVisible());
				b2.setCaption("Marker 4 is visible: " + mark4.isVisible());
				googleMap.requestRepaint();
			}
		});

		final Button b3 = new Button("Randomize Marker 5 location ");
		b3.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 8603447515298318466L;

			@Override
			public void buttonClick(ClickEvent event) {
				mark5.setTitle(mark5.getTitle() + i);
				Random r = new Random();

				int val1 = r.nextInt(10);
				int val2 = r.nextInt(10);
				mark5.setLatLng(new Point2D.Double(
						22.8 + ((double) val1) / 100L,
						60.4522 + ((double) val2) / 100L));
				googleMap.requestRepaint();
			}
		});

		final Button b4 = new Button("Set this title to marker 5: "
				+ mark5.getTitle() + i);
		b4.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				mark5.setTitle(mark5.getTitle() + i);
				b4.setCaption("Set this title to marker 5: " + mark5.getTitle()
						+ i);
				googleMap.requestRepaint();

			}

		});

		HorizontalLayout hl = new HorizontalLayout();
		Label l = new Label("These didn't work before: ");
		l.setSizeUndefined();
		hl.setSpacing(true);
		hl.addComponent(l);
		hl.addComponent(b);
		hl.addComponent(b2);
		hl.addComponent(b3);
		hl.addComponent(b4);

		getMainWindow().getContent().addComponent(hl);

		extendedFeatures();

		popupTest();

		Button resize = new Button("resize", new Button.ClickListener() {
			private static final long serialVersionUID = 3616458938424224832L;

			public void buttonClick(ClickEvent event) {
				googleMap.setHeight("200px");
				googleMap.setWidth("200px");

			}

		});

		getMainWindow().addComponent(resize);
	}

	private void popupTest() {
		Button popupB = new Button("popup", new Button.ClickListener() {
			private static final long serialVersionUID = -6394624551407250559L;

			public void buttonClick(ClickEvent event) {
				Application app = event.getButton().getApplication();

				GoogleMap map2 = new GoogleMap(event.getButton()
						.getApplication(), new Point2D.Double(22.3, 60.4522), 8);

				// map2.setHeight("240px");
				// map2.setWidth("300px");

				map2.setHeight("240px");
				map2.setWidth("240px");

				Window w = new Window("popup");
				w.addComponent(map2);
				w.setHeight("300px");
				w.setWidth("300px");

				app.getMainWindow().addWindow(w);

			}
		});

		getMainWindow().addComponent(popupB);
	}

	/**
	 * Demo of some of the extended features of the google maps widget
	 */
	private void extendedFeatures() {

		// Remove a marker from the map
		Button removeMarker = new Button("Remove \"Test marker2\"",
				new Button.ClickListener() {
					private static final long serialVersionUID = -8593284561770538538L;

					public void buttonClick(ClickEvent event) {
						// googleMap.addMarker(new BasicMarker(4L, new
						// Point2D.Double(22.2, 60.4522),
						// "Test marker3"));
						googleMap.removeMarker(mark2);
						// googleMap.removeAllMarkers();
					}
				});

		getMainWindow().addComponent(removeMarker);

		// Remove a marker from the map
		Button addMarker = new Button("Add \"Test marker2\"",
				new Button.ClickListener() {
					private static final long serialVersionUID = 6973907146984242699L;

					public void buttonClick(ClickEvent event) {
						// googleMap.addMarker(new BasicMarker(4L, new
						// Point2D.Double(22.2, 60.4522),
						// "Test marker3"));
						googleMap.addMarker(mark2);
						// googleMap.removeAllMarkers();
					}
				});

		getMainWindow().addComponent(addMarker);

		// Add a Marker click listener to catch marker click events.
		// Note, works only if marker has information window content
		googleMap.addListener(new GoogleMap.MarkerClickListener() {

			public void markerClicked(Marker clickedMarker) {
				System.out.println("Marker:" + clickedMarker.getTitle()
						+ " clicked");

			}
		});

		// Add a MarkerMovedListener to catch events when a marker is dragged to
		// a new location
		googleMap.addListener(new GoogleMap.MarkerMovedListener() {
			public void markerMoved(Marker movedMarker) {
				System.out.println("Evetn propagated! Marker: "
						+ movedMarker.getTitle() + " moved. New loc: "
						+ movedMarker.getLatLng().toString());
			}
		});
	}
}
