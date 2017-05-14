package org.abhijit.jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class CsvParserLatLng {

	private static BufferedReader reader;
	private static final String inFile = "Stationery_" + LocalityAggregator.getCity() + ".csv";
	private static final String outFile = "Stationery_" + LocalityAggregator.getCity() + "_out.csv";
	private static final String audioFile = "Alien_Siren.wav";

	static {
		try {
			FileUtility.checkForCsvFile_LatLng();
			if (!new File(inFile).exists()) {
				System.err.println(inFile + " is not present in the system. Please try again...");
			}
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

	public static void readDataFromCsv(String fileName) throws IOException, InterruptedException {
		reader = new BufferedReader(new FileReader(fileName));
		reader.readLine();
		String record = reader.readLine();
		if (record != null) {
			if (!record.trim().isEmpty()) {
				processRecord(record);
			}
		}
	}

	public static void processRecord(String line) throws IOException, InterruptedException {
		// String fileName = "abc_" + LocalityAggregator.getCity() + ".csv";
		reader.close();
		deleteRecord(inFile, line);
		StationeryShop shop = prepareShopData(line);
		writeToCsvFile(outFile, shop);
	}

	public static StationeryShop prepareShopData(String nextLine) throws InterruptedException {
		StationeryShop shop = new StationeryShop();
		if (nextLine != null) {
			String[] parts = nextLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			shop.setName(parts[0]);
			shop.setContact(parts[1]);
			shop.setAddress(parts[2]);
			shop.setLocality(parts[3]);
			shop.setStreetAddress(parts[4]);
			shop.setCity(parts[5]);
			shop.setRating(parts[6]);
		}

		try {
			GoogleGeoLatLng latLng = LatLongFinder.getLatLng(shop.getStreetAddress());
			shop.setLatitude(latLng.getLat());
			shop.setLongitude(latLng.getLng());
		} catch (Exception e) {
			StringBuilder addressBuilder = new StringBuilder();
			addressBuilder.append(shop.getName().replaceAll("^\"|\"$", "")).append(",").append(shop.getCity().replaceAll("^\"|\"$", ""));
			GoogleGeoLatLng latLng;
			try {
				latLng = LatLongFinder.getLatLng(addressBuilder.toString());
				shop.setLatitude(latLng.getLat());
				shop.setLongitude(latLng.getLng());
			} catch (Exception ex) {
				if(ex.getMessage().contains("You have exceeded your daily request quota for this API.")) {
					playSound("Alien_Siren.wav");
					TimeUnit.MINUTES.sleep(2);
					System.out.println("Google geocode api is blocked for further data fetching...");
					ex.printStackTrace();
					System.exit(0);
				} else {
					shop.setLatitude("");
					shop.setLongitude("");
				}
			}
		}
		return shop;
	}

	public static void playSound(String fileName) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	public static void writeToCsvFile(String outFile, StationeryShop shop) throws IOException {
		// String fileName = "Stationery_"+ LocalityAggregator.getCity()+".csv";
		FileWriter writer = new FileWriter(outFile, true);

		// CSVUtils.writeLine(writer,
		// Arrays.asList(appendDQ("Name"), appendDQ("Contact"),
		// appendDQ("Address"), appendDQ("Locality"),
		// appendDQ("Street Address"), appendDQ("City"), appendDQ("Rating"),
		// appendDQ("Latitude"),
		// appendDQ("Longitude")));

		List<String> list = new ArrayList<String>();
		list.add(shop.getName());
		list.add(shop.getContact());
		list.add(shop.getAddress());
		list.add(shop.getLocality());
		list.add(shop.getStreetAddress());
		list.add(shop.getCity());
		if (shop.getRating() != null) {
			list.add(shop.getRating());
		} else {
			list.add("0.0");
		}
		list.add(appendDQ(shop.getLatitude()));
		list.add(appendDQ(shop.getLongitude()));

		CSVUtils.writeLine(writer, list);

		writer.flush();
		writer.close();
	}

	private static String appendDQ(String str) {
		return "\"" + str + "\"";
	}

	public static void deleteRecord(String fileName, String lineToRemove) throws IOException {
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

	public static void main(String[] args) throws InterruptedException {
		try {
			// int lineCount = countLines(inFile);
			int lineCount = 2500;
			// System.out.println(lineCount);
			while (lineCount > 0) {
				readDataFromCsv(inFile);
				lineCount--;
			}
			System.out.println(inFile + " is successfully processed.");
		} catch (Throwable e) {
			e.printStackTrace();
			playSound(audioFile);
			TimeUnit.MINUTES.sleep(2);
		}
	}
}