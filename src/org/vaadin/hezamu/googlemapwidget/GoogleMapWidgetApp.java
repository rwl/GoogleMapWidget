package org.vaadin.hezamu.googlemapwidget;

import java.awt.geom.Point2D;

import org.vaadin.hezamu.googlemapwidget.overlay.BasicMarker;
import org.vaadin.hezamu.googlemapwidget.overlay.Marker;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class GoogleMapWidgetApp extends Application {
	private static final long serialVersionUID = -921015383668899594L;
	  Marker mark1;
	    Marker mark2;
	    GoogleMap googleMap;
		private BasicMarker mark3;
		private BasicMarker mark4;
		private BasicMarker mark5;

	    @Override
	    public void init() {
	        setMainWindow(new Window("GoogleMapWidgetApp"));

	        // Create a new map instance centered on the IT Mill offices
	        googleMap = new GoogleMap(this, new Point2D.Double(22.3, 60.4522), 8);

	        googleMap.setWidth("640px");
	        googleMap.setHeight("480px");

	        // Create a marker at the IT Mill offices
	        mark1 = new BasicMarker(1L, new Point2D.Double(22.3, 60.4522),
	                "Test marker");
	        
	        mark2 = new BasicMarker(2L, new Point2D.Double(22.4, 60.4522),
	                "Test marker2");
	        
	        mark3 = new BasicMarker(4L, new Point2D.Double(22.6, 60.4522),
            "Test marker3");
	        
	        mark4 = new BasicMarker(5L, new Point2D.Double(22.7, 60.4522),
            "Test marker4");
	      
	      
	        //Marker with information window pupup
	        mark5 = new BasicMarker(6L, new Point2D.Double(22.8, 60.4522),
            "Test marker5");
	        mark5.setInfoWindowContent(googleMap, new Label("Hello")); 
	        
	        
	        Label content =  new Label("Hello"); 
	        content.setWidth("60px"); 
	        ((BasicMarker)mark2).setInfoWindowContent(googleMap,content); 
	        	
	        googleMap.addMarker(mark1);
	        googleMap.addMarker(mark2);
	        googleMap.addMarker(mark3);
	        googleMap.addMarker(mark4);
	        googleMap.addMarker(mark5);
	        googleMap.addMarker(mark5);
	        getMainWindow().addComponent(googleMap);

	        extendedFeatures();
	       
	}
	    
	/**
	 * Demo of some of the extended features of the google maps widget
	 */
	private void extendedFeatures(){
		
		//Remove a marker from the map
		 Button removeMarker = new Button("Remove \"Test marker2\"", new Button.ClickListener() {

	            public void buttonClick(ClickEvent event) {
//	                googleMap.addMarker(new BasicMarker(4L, new Point2D.Double(22.2, 60.4522),
//	                "Test marker3"));
	                googleMap.removeMarker(mark2);
//	                googleMap.removeAllMarkers(); 
	            }
	        });

	        getMainWindow().addComponent(removeMarker);
	        
	        
	        //Add a Marker click listener to catch marker click events. 
	        //Note, works only if marker has information window content
	        googleMap.addListener(new GoogleMap.MarkerClickListener() {
				
				public void markerClicked(Marker clickedMarker) {
					System.out.println("Marker:" + clickedMarker.getTitle() +" clicked"); 
					
				}
			}); 
	        
	        
	        //Add a MarkerMovedListener to catch events when a marker is dragged to 
	        //a new location
	        googleMap.addListener(new GoogleMap.MarkerMovedListener() {
				
				public void markerMoved(Marker movedMarker) {
					System.out.println("Evetn propagated! Marker: " + movedMarker.getTitle() + " moved. New loc: " + movedMarker.getLatLng().toString());
				}
			});
		
	}
}
