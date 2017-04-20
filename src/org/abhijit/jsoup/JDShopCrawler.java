package org.abhijit.jsoup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JDShopCrawler {
	
	private static String primaryUrl = "https://www.justdial.com/" ;
	private static Set<String> paginationUrlSetByLocality = new LinkedHashSet<String>();

	//This function will read all the localities from loclaities.txt file 
	public static void readLocalities(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = in.readLine()) != null) {
				if(!str.trim().isEmpty()) {
					System.out.println(str);
					prepareMainUrlForLocality(primaryUrl, "Mumbai", str);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//prepare main url for a particular locality
	public static String prepareMainUrlForLocality(String primaryUrl, String city , String locality ) {
		StringBuilder localityUrlBuilder = new StringBuilder();
		localityUrlBuilder.append(primaryUrl).append(city).append("/Stationery-Shops-in-");
		localityUrlBuilder.append(locality.replaceAll(" ", "-"));
		localityUrlBuilder.append("/nct-10453443/page-");
		return localityUrlBuilder.toString();
	}
	
	//prepare pagination urls for a particular locality 
	public static void preparePaginationUrlsForLocality(String localityUrl) {
		for (int i = 1; i <= 50; i++) {
			StringBuilder pagionationUrlBuilder = new StringBuilder();
			pagionationUrlBuilder.append(localityUrl).append(i);
			paginationUrlSetByLocality.add(pagionationUrlBuilder.toString());
			System.out.println(pagionationUrlBuilder);
		}
	}
	
	public static void prepareShopUrls(String url) throws IOException, InterruptedException {
		PrintWriter writer = new PrintWriter("shop_url.txt", "UTF-8");
		//for(String url : paginationUrlSetByLocality) {
			//System.setProperty("http.proxyHost", "125.67.236.195");
			//System.setProperty("http.proxyPort", "8080");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("207.99.118.74", 8080));
			Document shopUrlData = Jsoup.connect(url)
						.header("Accept-Encoding", "gzip, deflate")
						.maxBodySize(0)
						.userAgent("Chrome")
						.timeout(600000000).get();
			Thread.sleep(10000000);
			Elements shopUrlElements = shopUrlData.select("span[class = jcn]");
			for(Element element : shopUrlElements) {
				System.out.println(element.child(0).attr("href"));		
				writer.println(element.child(0).attr("href"));
			}
	//	}
		writer.close();
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		//readLocalities("localities.txt");
		prepareMainUrlForLocality(primaryUrl, "Mumbai", "Marine Lines");
		preparePaginationUrlsForLocality("https://www.justdial.com/Mumbai/Stationery-Shops-in-Marine-Lines/nct-10453443/page-");
		prepareShopUrls("https://www.justdial.com/Mumbai/Stationery-Shops-in-Marine-Lines/nct-10453443/page-40");
	}
}
