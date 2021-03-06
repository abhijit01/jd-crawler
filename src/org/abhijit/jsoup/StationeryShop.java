package org.abhijit.jsoup;

public class StationeryShop {

	private String name;

	private String contact;

	private String address;

	private String locality;

	private String streetAddress;

	private String city;

	private String rating;

	private String latitude;

	private String longitude;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAdress) {
		this.streetAddress = streetAdress;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String toString() {
		return "Stationery Shop :  [Name : " + name + "\nContact : " + contact + "\nAddress : " + address + "\nCity : "
				+ city + "\nRating : " + rating + "]";
	}
}
