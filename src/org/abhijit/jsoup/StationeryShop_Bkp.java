package org.abhijit.jsoup;


public class StationeryShop_Bkp {

		private String name;

	    private String contact;
	    
	    private String complat;

	    private String complong;

	    private String landmark;

	    private String address;
	    
	    private String city ;

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

		public String getComplat() {
			return complat;
		}

		public void setComplat(String complat) {
			this.complat = complat;
		}

		public String getComplong() {
			return complong;
		}

		public void setComplong(String complong) {
			this.complong = complong;
		}

		public String getLandmark() {
			return landmark;
		}

		public void setLandmark(String landmark) {
			this.landmark = landmark;
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

		@Override
	    public String toString()
	    {
	        return "Stationery Shop :  [Name : " + name + "\nContact : " + contact + "\nLattitude : " + complat 
	        		+"\nLongitude : "+ complong + "\nLandmark : " + landmark + "\nAddress : " + address + "\nCity : " + city +" ]";
	    }
			
}
