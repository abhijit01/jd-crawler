package org.abhijit.jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JDShopCrawler {

	private static Map<String, String> shopUrlMapByDocID = new HashMap<String, String>();
	private static String primaryUrl = "https://www.justdial.com/";
	private static Set<String> paginationUrlSetByLocality = new HashSet<String>();
	private static Set<String> shopsUrlSetByLocality = new HashSet<String>();
	private static Set<StationeryShop> shopsByLocality = new HashSet<StationeryShop>();
	private static String audioFile = "Alien_Siren.wav";
	private static Workbook workBook;
	private static BufferedReader in;

	static {
		String fileName = "shop_url_list_" + LocalityAggregator.getCity() + ".properties";
		File file = new File(fileName);
		try {
			if (file.exists()) {
				readFileToMap(fileName);
			}
			FileUtility.checkFiles();
		} catch (IOException e) {
			playSound(audioFile);
			e.printStackTrace();
			try {
				TimeUnit.MINUTES.sleep(2);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static String getDocID(String url) {
		String result = "";
		Pattern regex = Pattern.compile("[^/]+(?=\\?[^?]+$)");
		Matcher regexMatcher = regex.matcher(url);
		if (regexMatcher.find()) {
			result = regexMatcher.group();
		}
		return result;
		//System.out.println(result);
	}

	public static void readFileToMap(String fileName) throws FileNotFoundException, IOException {
		// try (BufferedReader br = new BufferedReader(new FileReader(new
		// File(fileName)))) {
		// String line;
		// while ((line = br.readLine()) != null) {
		// shopUrlSetByCity.add(line);
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		Properties properties = new Properties();
		properties.load(new FileInputStream(fileName));

		for (String key : properties.stringPropertyNames()) {
			shopUrlMapByDocID.put(key, properties.get(key).toString());
		}
		System.out.println("Reading " + fileName + " is now completed.");
	}

	// Read the no of lines in a .txt file
	public static int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

	// This function will read all the localities from loclaities.txt file
	public static void readLocality(String fileName, String city)
			throws InterruptedException, InvalidFormatException, IOException {
		in = new BufferedReader(new FileReader(fileName));
		String locality = in.readLine();
		if (locality != null) {
			if (!locality.trim().isEmpty()) {
				System.out.println(locality);
				processLocality(locality, city);
			}
		}
		// in.close();
	}

	// This will process a single locality
	public static void processLocality(String locality, String city)
			throws IOException, InterruptedException, InvalidFormatException {
		String fileName = "localities_" + LocalityAggregator.getCity() + ".txt";
		in.close();
		String localityUrl = prepareMainUrlForLocality(primaryUrl, city, locality);
		preparePaginationUrlsForLocality(localityUrl);
		prepareShopUrls(paginationUrlSetByLocality);
		getAllShopsInLocality(shopsUrlSetByLocality, locality);
		writeDataToCSV();
		populateData();
		deleteLocality(fileName, locality);
		cleanUp();
	}

	// prepare main url for a particular locality
	public static String prepareMainUrlForLocality(String primaryUrl, String city, String locality) {
		StringBuilder localityUrlBuilder = new StringBuilder();
		localityUrlBuilder.append(primaryUrl).append(city).append("/Stationery-Shops-in-");
		localityUrlBuilder.append(locality.replaceAll(" ", "-"));
		localityUrlBuilder.append("/nct-10453443/page-");
		return localityUrlBuilder.toString();
	}

	// prepare pagination urls for a particular locality
	public static void preparePaginationUrlsForLocality(String localityUrl) {
		for (int i = 1; i <= 50; i++) {
			StringBuilder pagionationUrlBuilder = new StringBuilder();
			pagionationUrlBuilder.append(localityUrl).append(i);
			paginationUrlSetByLocality.add(pagionationUrlBuilder.toString());
			System.out.println(pagionationUrlBuilder);
		}
	}

	// fetch all the shop urls locality wise
	public static void prepareShopUrls(Set<String> paginationUrlSetByLocality)
			throws InterruptedException, FileNotFoundException, IOException {
		try {
			for (String url : paginationUrlSetByLocality) {
				// System.setProperty("https.proxyHost", "107.18.134.122");
				// System.setProperty("https.proxyPort", "8080");
				// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
				// InetSocketAddress("207.99.118.74", 8080));
				// Thread.sleep(5000);
				Document shopUrlData = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").maxBodySize(0)
						.userAgent("Chrome").timeout(10 * 1000).get();
				Elements shopUrlElements = shopUrlData.select("span[class = jcn]");
				for (Element element : shopUrlElements) {
					String shopUrl = element.child(0).attr("href");
					System.out.println(shopUrl);
					String docID = getDocID(shopUrl);
					if (!shopUrlMapByDocID.keySet().contains(docID)) {
						shopUrlMapByDocID.put(docID, shopUrl);
						shopsUrlSetByLocality.add(shopUrl);
						// writer.println(element.child(0).attr("href"));
					}
				}
			}
			// writer.close();
		} catch (IOException ie) {
			writeShopUrlDataToFile();
			playSound(audioFile);
			TimeUnit.MINUTES.sleep(2);
			ie.printStackTrace();
		} finally {
			writeShopUrlDataToFile();
		}
	}

	public static void writeShopUrlDataToFile() throws FileNotFoundException, IOException {
		String fileName = "shop_url_list_" + LocalityAggregator.getCity() + ".properties";
		// FileWriter fstream = new FileWriter(fileName, true);
		// BufferedWriter out = new BufferedWriter(fstream);
		Properties properties = new Properties();
		properties.putAll(shopUrlMapByDocID);
		properties.store(new FileOutputStream(fileName, true), null);
		System.out.println("Wrote all the available shop url data to " + fileName);
		// out.close();
	}

	public static void stripDuplicatesFromFile(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		Set<String> lines = new HashSet<String>(10000); // maybe should be
														// bigger
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		reader.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (String unique : lines) {
			writer.write(unique);
			writer.newLine();
		}
		writer.close();
	}

	// Get all the shops in the city
	public static void getAllShopsInLocality(Set<String> shopUrlSetByLocality, String locality)
			throws IOException, InterruptedException {
		// Response response = null;
		for (String url : shopUrlSetByLocality) {
			// Thread.sleep(5000);
			// Run this function using different proxies
			// System.setProperty("https.proxyHost", "107.18.134.122");
			// System.setProperty("https.proxyPort", "8080");
			/*
			 * Set<String> proxySet = ConnectionUtility.getProxyList(); String
			 * randomIP = getRandomIP(proxySet);
			 * System.setProperty("https.proxyHost", randomIP);
			 * System.setProperty("https.proxyPort", "8080");
			 * System.out.println("Using proxy : " + randomIP);
			 * 
			 * response =
			 * Jsoup.connect(url).followRedirects(false).timeout(10*1000).
			 * execute(); System.out.println(response.statusCode() + " : " +
			 * response.url());
			 * 
			 * if (200 == response.statusCode()) {
			 */

			Document shopData = Jsoup.connect(url).ignoreHttpErrors(true)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.timeout(10 * 1000).get();

			StringBuilder addressBuilder = new StringBuilder();
			StationeryShop newShop = new StationeryShop();

			// Get the name of the shop
			Elements nameElements = shopData.select("span[class=fn]");
			for (Element element : nameElements) {
				newShop.setName(element.ownText());
				addressBuilder.append(element.ownText());
				addressBuilder.append(" , ");
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

			newShop.setLocality(locality);

			addressBuilder.append(getStreetAddress(shopData));

			newShop.setStreetAddress(addressBuilder.toString());

			// try {
			// GoogleGeoLatLng latLng =
			// LatLongFinder.getLatLng(addressBuilder.toString());
			// newShop.setLatitude(latLng.getLat());
			// newShop.setLongitude(latLng.getLng());
			// } catch (Exception e) {
			// GoogleGeoLatLng latLng;
			// try {
			// latLng = LatLongFinder.getLatLng(locality);
			// newShop.setLatitude(latLng.getLat());
			// newShop.setLongitude(latLng.getLng());
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// }
			// }

			System.out.println(newShop);
			shopsByLocality.add(newShop);
		}
		// }
	}

	// Delete processed locality from .txt file
	public static void deleteLocality(String fileName, String lineToRemove) throws IOException {
		File inFile = new File(fileName);

		if (!inFile.isFile()) {
			System.out.println("Parameter is not an existing file");
			return;
		}

		// Construct the new file that will later be renamed to the original
		// filename.
		File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

		String line = null;

		// Read from the original file and write to the new
		// unless content matches data to be removed.
		while ((line = br.readLine()) != null) {
			if (!line.trim().equals(lineToRemove)) {
				pw.println(line);
				pw.flush();
			}
		}
		pw.close();
		br.close();

		// Delete the original file
		if (!inFile.delete()) {
			System.out.println("Could not delete file");
			return;
		}

		// Rename the new file to the filename the original file had.
		if (!tempFile.renameTo(inFile))
			System.out.println("Could not rename file");
	}

	// Write data into a csv file
	public static void writeDataToCSV() throws IOException {
		// String csvFile = "..//JDCrawler//StationeryList_Hyd.csv";
		String fileName = "Stationery_" + LocalityAggregator.getCity() + ".csv";
		FileWriter writer = new FileWriter(fileName, true);

		CSVUtils.writeLine(writer, Arrays.asList(appendDQ("Name"), appendDQ("Contact"), appendDQ("Address"),
				appendDQ("Locality"), appendDQ("Street Address"), appendDQ("City"), appendDQ("Rating")));

		for (StationeryShop shop : shopsByLocality) {
			List<String> list = new ArrayList<String>();
			list.add(appendDQ(shop.getName()));
			list.add(appendDQ(shop.getContact()));
			list.add(appendDQ(shop.getAddress()));
			list.add(appendDQ(shop.getLocality()));
			list.add(appendDQ(shop.getStreetAddress()));
			list.add(appendDQ(shop.getCity()));
			if (shop.getRating() != null) {
				list.add(appendDQ(shop.getRating()));
			} else {
				list.add(appendDQ("0.0"));
			}
			CSVUtils.writeLine(writer, list);
		}

		writer.flush();
		writer.close();
	}

	private static String appendDQ(String str) {
		return "\"" + str + "\"";
	}

	// This method will write data into excel file
	public static void populateData() throws IOException, InvalidFormatException {
		String fileName = "Stationery_" + LocalityAggregator.getCity() + ".xls";
		FileInputStream inputStream = new FileInputStream(new File(fileName));
		workBook = WorkbookFactory.create(inputStream);
		Sheet mySheet = workBook.getSheetAt(0);

		for (StationeryShop shop : shopsByLocality) {
			Row row = mySheet.createRow(mySheet.getLastRowNum() + 1);
			row.createCell(0).setCellValue(shop.getName());
			row.createCell(1).setCellValue(shop.getContact());
			row.createCell(2).setCellValue(shop.getAddress());
			row.createCell(3).setCellValue(shop.getLocality());
			row.createCell(4).setCellValue(shop.getStreetAddress());
			row.createCell(5).setCellValue(shop.getCity());
			if (shop.getRating() != null) {
				row.createCell(6).setCellValue(shop.getRating());
			} else {
				row.createCell(6).setCellValue("0.0");
			}
			// row.createCell(6).setCellValue(shop.getLatitude());
			// row.createCell(7).setCellValue(shop.getLongitude());
		}

		inputStream.close();

		FileOutputStream outputStream = new FileOutputStream(fileName);
		workBook.write(outputStream);
		outputStream.close();
	}

	// This method will extract the street address from webpage source
	public static String getStreetAddress(Document shopDoc) throws IOException {
		Element addrElement = shopDoc.getElementsByTag("script").get(1);
		String jsonString = addrElement.data();
		JSONObject jObject = new JSONObject(jsonString);
		// System.out.println(jObject.getJSONObject("address").getString("streetAddress"));
		return jObject.getJSONObject("address").getString("streetAddress");
	}

	// This method will cleanup the sets
	public static void cleanUp() {
		paginationUrlSetByLocality = new HashSet<String>();
		shopsUrlSetByLocality = new HashSet<String>();
		shopsByLocality = new HashSet<StationeryShop>();
	}

	public static void playSound(String fileName) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
			clip.loop(5);
		} catch (Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	// Select a random IP from the list
	// private static String getRandomIP(Set<String> proxySet) {
	// List<String> asList = new ArrayList<String>(proxySet);
	// Collections.shuffle(asList);
	// return asList.get(0);
	// }
	//
	// public static void getIP() throws SocketException {
	// Enumeration e = NetworkInterface.getNetworkInterfaces();
	// while (e.hasMoreElements()) {
	// NetworkInterface n = (NetworkInterface) e.nextElement();
	// Enumeration ee = n.getInetAddresses();
	// while (ee.hasMoreElements()) {
	// InetAddress i = (InetAddress) ee.nextElement();
	// System.out.println(i.getCanonicalHostName());
	// }
	// }
	// }

	public static void main(String[] args) throws InterruptedException {
		// readLocality("localities.txt");
		// prepareMainUrlForLocality(primaryUrl, "Mumbai", "Marine Lines");
		// preparePaginationUrlsForLocality("https://www.justdial.com/Mumbai/Stationery-Shops-in-Marine-Lines/nct-10453443/page-");
		// prepareShopUrls(paginationUrlSetByLocality);
		// getAllShopsInLocality("https://www.justdial.com/Mumbai/Stationery-Point-Opposite-Bharatiya-Misthan-Fort/022P1224784784C5Q6Y7_BZDET?xid=TXVtYmFpIFN0YXRpb25lcnkgU2hvcHMgTWFyaW5lIExpbmVz");
		// deleteLocality("localities.txt", "Chembur");
		// populateData();
		// getDocID("https://www.justdial.com/Hyderabad/S-K-R-Co-Near-Sai-baba-temple-Uppal/040PXX40-XX40-160506121243-H9N4_BZDET?xid=SHlkZXJhYmFkIFN0YXRpb25lcnkgU2hvcHMgSGFic2lndWRh");
		// stripDuplicatesFromFile(fileName);
		// System.out.println(appendDQ("abhijit"));
		// String fileName =
		// "shop_url_list_"+LocalityAggregator.getCity()+".txt";

		String city = LocalityAggregator.getCity();
		String fileName = "localities_" + city + ".txt";
		String outFile = "Stationery_" + city + ".csv";
		try {

			if (FileUtility.checkEmptyFile(fileName) && FileUtility.checkEmptyFile(outFile)) {
				LocalityAggregator.getAllLocalities(city.toString());
			}
			int lineCount = countLines(fileName);
			while (lineCount > 0) {
				readLocality(fileName, city);
				lineCount--;
			}
		} catch (Throwable ex) {
			playSound(audioFile);
			TimeUnit.MINUTES.sleep(2);
			ex.printStackTrace();
		}

	}
}
