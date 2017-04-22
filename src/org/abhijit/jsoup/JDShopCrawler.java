package org.abhijit.jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JDShopCrawler {

	private static String primaryUrl = "https://www.justdial.com/";
	private static Set<String> paginationUrlSetByLocality = new HashSet<String>();
	private static Set<String> shopsUrlSetByLocality = new HashSet<String>();
	private static Set<StationeryShop> shopsByLocality = new HashSet<StationeryShop>();
	private static ExcelUtility excelUtility;
	private static Workbook workBook;
	private static BufferedReader in ;
	/*static {
		try {
			excelUtility = new ExcelUtility();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	// This function will read all the localities from loclaities.txt file
	public static void readLocality(String fileName) throws InterruptedException, InvalidFormatException {
		try {
			in = new BufferedReader(new FileReader(fileName));
			String locality;
			while ((locality = in.readLine()) != null) {
				if (!locality.trim().isEmpty()) {
					System.out.println(locality);
					processLocality(locality);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void processLocality(String locality) throws IOException, InterruptedException, InvalidFormatException {
		String localityUrl = prepareMainUrlForLocality(primaryUrl, "Mumbai", locality);
		preparePaginationUrlsForLocality(localityUrl);
		prepareShopUrls(paginationUrlSetByLocality);
		getAllShopsInLocality(shopsUrlSetByLocality, locality);
		writeDataToCSV();
		populateData();
		cleanUp();
		deleteLocality("localities.txt", locality);
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

	public static void prepareShopUrls(Set<String> paginationUrlSetByLocality)
			throws IOException, InterruptedException {
		PrintWriter writer = new PrintWriter("shop_url.txt", "UTF-8");
		for (String url : paginationUrlSetByLocality) {
			// System.setProperty("http.proxyHost", "125.67.236.195");
			// System.setProperty("http.proxyPort", "8080");
			// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			// InetSocketAddress("207.99.118.74", 8080));
			// Thread.sleep(5000);
			Document shopUrlData = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate").maxBodySize(0)
					.userAgent("Chrome").timeout(600000000).get();
			Elements shopUrlElements = shopUrlData.select("span[class = jcn]");
			for (Element element : shopUrlElements) {
				System.out.println(element.child(0).attr("href"));
				shopsUrlSetByLocality.add(element.child(0).attr("href"));
				writer.println(element.child(0).attr("href"));
			}
		}
		writer.close();
	}

	// Get all the shops in the city
	public static void getAllShopsInLocality(Set<String> shopUrlSetByLocality, String locality)
			throws IOException, InterruptedException {
		for (String url : shopUrlSetByLocality) {
			// Thread.sleep(5000);
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

			newShop.setLocality(locality);

			System.out.println(newShop);
			shopsByLocality.add(newShop);
		}
	}

	public static void deleteLocality(String fileName, String lineToRemove) {
		try {
			
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
				in.close();
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				in.close();
				System.out.println("Could not rename file");

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// Write data into a csv file
	public static void writeDataToCSV() throws IOException {
		String csvFile = "..//JDCrawler//StationeryList_Mum.csv";
		FileWriter writer = new FileWriter(csvFile);

		CSVUtils.writeLine(writer, Arrays.asList("Name", "Contact", "Address", "Locality", "City", "Rating"));

		for (StationeryShop shop : shopsByLocality) {
			List<String> list = new ArrayList<String>();
			list.add(shop.getName().replaceAll(",", ";"));
			list.add(shop.getContact().replaceAll(",", ";"));
			list.add(shop.getAddress().replaceAll(",", ";"));
			list.add(shop.getLocality());
			list.add(shop.getCity());
			if (shop.getRating() != null) {
				list.add(shop.getRating());
			} else {
				list.add("0.0");
			}
			CSVUtils.writeLine(writer, list);
		}

		writer.flush();
		writer.close();
	}

	public static void populateData() throws IOException, InvalidFormatException {
		String excelFilePath = "Stationery_Mum.xls";
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
	    workBook = WorkbookFactory.create(inputStream);
	    Sheet mySheet = workBook.getSheetAt(0);
 
		for (StationeryShop shop : shopsByLocality) {
			Row row = mySheet.createRow(mySheet.getLastRowNum() + 1);
			row.createCell(0).setCellValue(shop.getName().replaceAll(",", ";"));
			row.createCell(1).setCellValue(shop.getContact().replaceAll(",", ";"));
			row.createCell(2).setCellValue(shop.getAddress().replaceAll(",", ";"));
			row.createCell(3).setCellValue(shop.getLocality());
			row.createCell(4).setCellValue(shop.getCity());
			if (shop.getRating() != null) {
				row.createCell(5).setCellValue(shop.getRating());
			} else {
				row.createCell(5).setCellValue("0.0");
			}
		}
	    
	    inputStream.close();
	    
        FileOutputStream outputStream = new FileOutputStream("Stationery_Mum.xls");
        workBook.write(outputStream);
        outputStream.close();
	}

	public static void cleanUp() {
		paginationUrlSetByLocality = new HashSet<String>();
		shopsUrlSetByLocality = new HashSet<String>();
		shopsByLocality = new HashSet<StationeryShop>();
	}

	public static void main(String[] args) throws IOException, InterruptedException, InvalidFormatException {
		// readLocality("localities.txt");
		// prepareMainUrlForLocality(primaryUrl, "Mumbai", "Marine Lines");
		// preparePaginationUrlsForLocality("https://www.justdial.com/Mumbai/Stationery-Shops-in-Marine-Lines/nct-10453443/page-");
		// prepareShopUrls(paginationUrlSetByLocality);
		// getAllShopsInLocality("https://www.justdial.com/Mumbai/Stationery-Point-Opposite-Bharatiya-Misthan-Fort/022P1224784784C5Q6Y7_BZDET?xid=TXVtYmFpIFN0YXRpb25lcnkgU2hvcHMgTWFyaW5lIExpbmVz");
		// deleteLocality("localities.txt", "Chembur");

		readLocality("localities.txt");
		//populateData();
	}
}
