package org.abhijit.jsoup;

public class GoogleGeoGeometry {
	
	private GoogleGeoBounds bounds;
	private GoogleGeoLatLng location;
	private String location_type;
	private GoogleGeoBounds viewport;
	
	public GoogleGeoBounds getBounds() {
		return bounds;
	}
	public void setBounds(GoogleGeoBounds bounds) {
		this.bounds = bounds;
	}
	public GoogleGeoLatLng getLocation() {
		return location;
	}
	public void setLocation(GoogleGeoLatLng location) {
		this.location = location;
	}
	public String getLocation_type() {
		return location_type;
	}
	public void setLocation_type(String location_type) {
		this.location_type = location_type;
	}
	public GoogleGeoBounds getViewport() {
		return viewport;
	}
	public void setViewport(GoogleGeoBounds viewport) {
		this.viewport = viewport;
	}
	
}
