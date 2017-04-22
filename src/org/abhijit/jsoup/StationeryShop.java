package org.abhijit.jsoup;

public class StationeryShop {

	private String name;

    private String contact;
    
    private String address;
    
    private String city ;
    
    private String locality ;
    
    private String rating ;

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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
    
    public String toString() {
    	return "Stationery Shop :  [Name : " + name + "\nContact : " + contact  
    			+ "\nAddress : " + address + "\nCity : " + city + "\nRating : " + rating + " ]";
    }
}
