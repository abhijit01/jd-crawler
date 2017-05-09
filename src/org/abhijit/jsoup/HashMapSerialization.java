package org.abhijit.jsoup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HashMapSerialization {

	public static void writedata() throws FileNotFoundException, IOException {
		Map<String, String> ldapContent = new HashMap<String, String>();
		//String fileName = "shop_url_list_"+LocalityAggregator.getCity()+".txt";
		ldapContent.put("a", "abhijit");
		ldapContent.put("b", "bikram");
		ldapContent.put("c", "chandu");
		Properties properties = new Properties();

//		for (Map.Entry<String,String> entry : ldapContent.entrySet()) {
//		    properties.put(entry.getKey(), entry.getValue());
//		}
		
		properties.putAll(ldapContent);

		properties.store(new FileOutputStream("data.properties", true), null);
		System.out.println("Stored Data ");
	}
	
	public static void readData() throws FileNotFoundException, IOException {
		Map<String, String> ldapContent = new HashMap<String, String>();
		Properties properties = new Properties();
		properties.load(new FileInputStream("data.properties"));

		for (String key : properties.stringPropertyNames()) {
		   ldapContent.put(key, properties.get(key).toString());
		}
		System.out.println("Reading complete ");
		for(String str : ldapContent.values()) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//writedata();
		readData();
	}
		
}
