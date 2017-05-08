package org.abhijit.jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtility {

	//This will create a excel file if doesn't exist
	public static void checkForExcel_Main() throws IOException {
		String fileName = "Stationery_"+ LocalityAggregator.getCity()+".xls";
		File f = new File(fileName);
		if(!f.exists()) {
			//String fileName = "Stationery_"+ LocalityAggregator.getCity()+".xls";
			//File f = new File(fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(f);
				ExcelUtility.createWorkBook().write(out);
				System.out.println("Wrote " + f.getCanonicalPath());
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} else {
			System.out.println(fileName + " is ready for processing.");
		}
	}
	 
	//This will create a text file if doesn't exists 
	public static void checkForTextFile_ShopUrl() throws IOException {
		String fileName = "shop_url_list_"+LocalityAggregator.getCity()+".properties";
		File f = new File(fileName);
		if(!f.exists()) {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			System.out.println("Wrote " + f.getCanonicalPath());
			writer.close();
		} else {
			System.out.println(fileName + " is ready for processing.");
		}
	}
	
	public static void checkForTextFile_Localities() throws IOException {
		String fileName = "localities_"+LocalityAggregator.getCity()+".txt";
		File f = new File(fileName);
		if(!f.exists()) {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			System.out.println("Wrote " + f.getCanonicalPath());
			writer.close();
		} else {
			System.out.println(fileName + " is ready for processing.");
		}
	}
	
	public static void checkForCsvFile_WOLatLng() throws IOException{
		String fileName = "Stationery_"+ LocalityAggregator.getCity()+".csv";
		File f = new File(fileName);
		if(!f.exists()) {
			FileWriter writer = new FileWriter(fileName);
			System.out.println("Wrote " + f.getCanonicalPath());
			writer.close();
		} else {
			System.out.println(fileName + " is ready for processing.");
		}
	}
	
	public static boolean checkEmptyFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		if (br.readLine() == null) {
			// System.out.println("No errors, and file empty");
			return true;
		}
		return false;
	}
	
	public static void checkFiles() throws IOException {
		checkForExcel_Main();
		checkForTextFile_ShopUrl();
		checkForTextFile_Localities();
		checkForCsvFile_WOLatLng();
	}

}
