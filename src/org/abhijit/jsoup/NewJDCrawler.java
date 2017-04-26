package org.abhijit.jsoup;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class NewJDCrawler {

	private static Map<String, Set<String>> localitiesMapByCity = new TreeMap<String, Set<String>>();
	private static Map<String, Set<String>> localityUrlMapByCity = new TreeMap<String, Set<String>>();
	private static Set<String> paginationUrlSetByLocality = new LinkedHashSet<String>();
	private static Set<StationeryShop> shopsByCity = new TreeSet<StationeryShop>();
	private static String primaryUrl = "https://www.justdial.com/" ;
	private static Set<String> shopsUrlSetByCity = new LinkedHashSet<String>();
	
	public enum City {
		Mumbai
	};

	// Get all the localities for a particular city
	public static void getAllLocalities(String city) throws IOException {
		StringBuilder localityUrl = new StringBuilder("http://www.propertykhazana.com/localities/");
		localityUrl.append(city.toLowerCase());
		Document locDoc = Jsoup.connect(localityUrl.toString()).ignoreHttpErrors(true)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
				.timeout(600000000).get();
		Elements localityElements = locDoc.getElementsByAttributeValue("id", "localitylist");
		Set<String> localitiesSet = new TreeSet<String>();
		for (Element element : localityElements) {
			for (Node data : element.childNodes()) {
				String locality = data.toString().replaceAll("\\<.*?>", "").trim();
				if (!locality.isEmpty() && !"".equals(locality))
					localitiesSet.add(locality);
			}
		}
		System.out.println(localitiesSet);
		localitiesMapByCity.put(city, localitiesSet);
	}
	
	//prepare main urls for each locality
	public static void prepareUrlsForLocalities(String primaryUrl) {
		for (City city : City.values()) {
			Set<String> localityUrlSet = new TreeSet<String>();
			Set<String> localities = localitiesMapByCity.get(city.toString());
			for (String locality : localities) {
				StringBuilder localityUrlBuilder = new StringBuilder();
				localityUrlBuilder.append(primaryUrl).append(city).append("/Stationery-Shops-in-");
				localityUrlBuilder.append(locality.replaceAll(" ", "-"));
				localityUrlBuilder.append("/nct-10453443/page-");
				localityUrlSet.add(localityUrlBuilder.toString());
			}
			System.out.println(localityUrlSet);
			localityUrlMapByCity.put(city.toString(), localityUrlSet);
		}
	}
	
	//prepare pagination url's locality wise 
	public static void preparePaginationUrlsLocalityWise(String city) {
		Set<String> localitiesUrlSet = localityUrlMapByCity.get(city.toString());
		for (String localityUrl : localitiesUrlSet) {
			for (int i = 1; i <= 50; i++) {
				StringBuilder pagionationUrlBuilder = new StringBuilder();
				pagionationUrlBuilder.append(localityUrl).append(i);
				paginationUrlSetByLocality.add(pagionationUrlBuilder.toString());
			}
		}
		System.out.println(paginationUrlSetByLocality);
	}
	
	//prepare single shop urls 
	public static void prepareShopUrls(Set<String> paginationUrlSetByLocality) throws IOException {
		PrintWriter writer = new PrintWriter("shop_url.txt", "UTF-8");
		for(String url : paginationUrlSetByLocality) {
		//	System.setProperty("https.proxyHost", "107.17.92.18");
		//	System.setProperty("https.proxyPort", "8080");
			Document shopUrlData = Jsoup.connect(url).ignoreHttpErrors(true)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
						.timeout(600000000).get();
			
			Elements shopUrlElements = shopUrlData.select("span[class = jcn]");
			for(Element element : shopUrlElements) {
				System.out.println(element.child(0).attr("href"));
				shopsUrlSetByCity.add(element.child(0).attr("href"));		
				writer.println(element.child(0).attr("href"));
			}
		}
		System.out.println(shopsUrlSetByCity);
		writer.close();
	}

	//Get all the shops in the city 
	public static void getAllShopsInCity(Set<String> shopsUrlSetByCity) throws IOException {
		for (String url : shopsUrlSetByCity) {
			Document shopData = Jsoup.connect(url).ignoreHttpErrors(true)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.timeout(600000000).get();

			StationeryShop newShop = new StationeryShop();

			// Get the name of the shop
			Elements nameElements = shopData.select("span[class=fn]");
			for (Element element : nameElements) {
				newShop.setName(element.ownText());
				// System.out.println(" Name : " + element.ownText());
			}

			// Get all the contacts for a particular shop
			Set<String> contacts = new HashSet<String>();
			Elements telephoneNums = shopData.select("a[class=tel]");
			for (Element telph : telephoneNums) {
				// System.out.println(telph.ownText());
				if (!telph.ownText().trim().isEmpty())
					contacts.add(telph.ownText().replaceAll("\\<.*?>", "").trim());
			}
			StringBuilder contactBuilder = new StringBuilder();
			for (String str : contacts)
				contactBuilder.append(str).append(" ; ");
			newShop.setContact(contactBuilder.toString());

			// Get the address for a particular shop
			Elements addressElements = shopData.select("span[class=jaddt]");
			for (Element element : addressElements) {
				newShop.setAddress(element.ownText());
				// System.out.println(element.ownText());
			}

			// Get the rating for a particular shop
			Elements ratingElements = shopData.select("span[class=value-titles]");
			for (Element element : ratingElements) {
				// System.out.println(element.ownText());
				newShop.setRating(element.ownText());
			}

			// Get the city
			Element city = shopData.getElementById("city");
			// System.out.println(city.val());
			newShop.setCity(city.val());

			System.out.println(newShop);
			shopsByCity.add(newShop);
		}
	}
	
	//Write data into a csv file 
	public static void writeDataToCSV() throws IOException {
		String csvFile = "..//JDCrawler//StationeryList_Mum.csv";
		FileWriter writer = new FileWriter(csvFile);

		CSVUtils.writeLine(writer, Arrays.asList("Name", "Contact", "Address", "City", "Rating"));

		for (StationeryShop shop : shopsByCity) {
			List<String> list = new ArrayList<String>();
			list.add(shop.getName().replaceAll(",", ";"));
			list.add(shop.getContact().replaceAll(",", ";"));
			list.add(shop.getAddress().replaceAll(",", ";"));
			list.add(shop.getCity());
			list.add(shop.getRating());

			CSVUtils.writeLine(writer, list);
		}

		writer.flush();
		writer.close();
	}

	public void cleanUp() {
		localitiesMapByCity = new TreeMap<String, Set<String>>();
		localityUrlMapByCity = new TreeMap<String, Set<String>>();
		paginationUrlSetByLocality = new LinkedHashSet<String>();
		shopsByCity = new TreeSet<StationeryShop>();
		shopsUrlSetByCity = new LinkedHashSet<String>();
	}

	public static void main(String[] args) throws IOException {
		for(City city : City.values()) {
			getAllLocalities(city.toString());
			prepareUrlsForLocalities(primaryUrl);
			preparePaginationUrlsLocalityWise(city.toString());
			prepareShopUrls(paginationUrlSetByLocality);
			getAllShopsInCity(shopsUrlSetByCity);
		}
		
		//prepareShopUrls("https://www.justdial.com/Mumbai/Stationery-Shops-in-90-Feet-Road/nct-10453443/page-1");
	}
}
