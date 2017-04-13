package org.abhijit.jsoup;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"@type"})
public class Address {
	
	private String type;

	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	private String streetAddress;

	public String getStreetAddress() {
		return this.streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	private String addressLocality;

	public String getAddressLocality() {
		return this.addressLocality;
	}

	public void setAddressLocality(String addressLocality) {
		this.addressLocality = addressLocality;
	}

	private String postalCode;

	public String getPostalCode() {
		return this.postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	private String addressCountry;

	public String getAddressCountry() {
		return this.addressCountry;
	}

	public void setAddressCountry(String addressCountry) {
		this.addressCountry = addressCountry;
	}
	
	public String toString() {
		return "StreetAddress : " + streetAddress 
			  +"\nAddressLocality : " + addressLocality
			  +"\nPostalCode : " + postalCode
			  +"\nAddressCountry : " + addressCountry;
	}
}
