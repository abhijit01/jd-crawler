package org.abhijit.jsoup;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class JDCrawler {

	private static Set<String> docIdSet = new LinkedHashSet<String>();
	private static String primaryUrl = "https://www.justdial.com/";
	private static String baseUrl = "https://www.justdial.com/Pune/Stationery-Shops/nct-1764/page-";
	private static Set<String> paginationUrlSetByCity = new LinkedHashSet<String>();
	private static Map<String, String> mainUrlMapByCity = new TreeMap<String, String>();
	private static Set<StationeryShop> shopsByCity = new LinkedHashSet<StationeryShop>();
	private static Set<String> shopsUrlSetByCity = new LinkedHashSet<String>();
	private static Map<String, Set<String>> urlMapByCity = new TreeMap<String, Set<String>>();
	private static Map<String, Set<String>> localitiesMapByCity = new TreeMap<String, Set<String>>();

	public enum City {
		Hyderabad
	};

	/*
	 * Get all the docIds for a particular URL. Doc Ids are needed to hit the JD
	 * Api to get the co-ordinates
	 */
	public static void getAllDocIDsInPage(String url) throws IOException {
		Document idDoc = Jsoup.connect(url).get();

		Elements docElements = idDoc.select("span[Docid]");
		for (Element element : docElements) {
			for (Attribute attr : element.attributes()) {
				if (attr.getKey().contentEquals("docid")) {
					docIdSet.add(attr.getValue());
					System.out.println(attr.getValue());
				}
			}
		}

	}

	// Get all the localities for a particular city
	public static void getAllLocalities(String city) throws IOException {
		StringBuilder localityUrl = new StringBuilder("http://www.propertykhazana.com/localities/");
		localityUrl.append(city.toLowerCase());
		Document locDoc = Jsoup.connect(localityUrl.toString()).get();
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

	//
	public static void prepareUrlsForCities(String primaryUrl) {
		for (City city : City.values()) {
			StringBuilder cityUrlBuilder = new StringBuilder();
			cityUrlBuilder.append(primaryUrl).append(city).append("/Stationery-Shops/nct-1764/page-");
			mainUrlMapByCity.put(city.toString(), cityUrlBuilder.toString());
		}
	}

	public static void preparePaginationUrlsCityWise(String baseUrl) {
		for (int i = 1; i <= 50; i++) {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(baseUrl).append(i);
			paginationUrlSetByCity.add(urlBuilder.toString());
		}
	}

	public static void prepareJsonUrlsForCity(String city, Set<String> docIdSet) {

		for (String doc_id : JDCrawler.docIdSet) {
			String base_url = "http://mapsearch.justdis.com/v14082601/json/justdialapidet/stationery/";
			// String city = "Pune";
			String countryCode = "IN";
			String StateCode = "NA"; // valid only for USA. In our case "NA".

			String final_url = base_url + "/" + doc_id + "/" + countryCode + "/" + city + "/" + StateCode;
			shopsUrlSetByCity.add(final_url);
		}
	}

	public static void getAllShopsInCity(Set<String> shopsUrlSetByCity, String city) throws JSONException, IOException {
		for (String url : shopsUrlSetByCity) {
			JSONObject newJsonObject = readJsonFromUrl(url);
			// System.out.println(newObject.getJSONObject("results").get("name"));
			StationeryShop newShop = new StationeryShop();
			newShop.setName(newJsonObject.getJSONObject("results").getString("name"));
			newShop.setContact(newJsonObject.getJSONObject("results").getString("contact"));
			newShop.setComplat(newJsonObject.getJSONObject("results").getString("complat"));
			newShop.setComplong(newJsonObject.getJSONObject("results").getString("complong"));
			newShop.setLandmark(newJsonObject.getJSONObject("results").getString("landmark"));
			newShop.setAddress(newJsonObject.getJSONObject("results").getString("address").replaceAll(",", ";"));
			newShop.setCity(city);

			System.out.println(newShop);

			shopsByCity.add(newShop);
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public static void writeDataToCSV() throws IOException {
		String csvFile = "..//JDCrawler//StationeryList_India.csv";
		FileWriter writer = new FileWriter(csvFile);

		CSVUtils.writeLine(writer,
				Arrays.asList("Name", "Contact", "Lattitude", "Longitude", "Landmark", "Address", "City"));

		for (StationeryShop shop : shopsByCity) {
			List<String> list = new ArrayList<String>();
			list.add(shop.getName().replaceAll(",", ";"));
			list.add(shop.getContact().replaceAll(",", ";"));
			list.add(shop.getComplat());
			list.add(shop.getComplong());
			if (shop.getLandmark() != null) {
				list.add(shop.getLandmark().replaceAll(",", ";"));
			}
			list.add(shop.getAddress().replaceAll(",", ";"));
			list.add(shop.getCity());

			CSVUtils.writeLine(writer, list);
		}

		writer.flush();
		writer.close();
	}

	public static void cleanUp() {
		docIdSet = new LinkedHashSet<String>();
		paginationUrlSetByCity = new LinkedHashSet<String>();
		shopsUrlSetByCity = new LinkedHashSet<String>();
	}

	public static void main(String[] args) throws IOException {
		// prepareUrls(baseUrl);
		// for(String url : urlSetByCity ){
		// getAllDocIDsInPage(url);
		// }
		// prepareUrlsForCity("Pune", docIdSet);
		// getAllShopsInCity(shopsUrlSetByCity);
		//
		// writeDataToCSV("Pune");
		/*
		 * prepareUrlsForCities(primaryUrl); Set<String> cities =
		 * mainUrlMapByCity.keySet(); for(String city : cities) { String
		 * urlForCity = mainUrlMapByCity.get(city);
		 * preparePaginationUrlsCityWise(urlForCity); for(String url :
		 * paginationUrlSetByCity) { getAllDocIDsInPage(url); }
		 * prepareJsonUrlsForCity(city, docIdSet);
		 * getAllShopsInCity(shopsUrlSetByCity, city); writeDataToCSV();
		 * cleanUp(); }
		 */

		getAllLocalities("Hyderabad");
	}

}
