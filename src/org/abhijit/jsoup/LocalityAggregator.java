package org.abhijit.jsoup;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class LocalityAggregator {

	public enum City {
		Mumbai
	};
	
	public static void getAllLocalities(String city) throws IOException {
		PrintWriter writer = new PrintWriter("localities.txt", "UTF-8");
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
				writer.println(locality);
			}
		}
		writer.close();
	}
	
	public static void main(String[] args) throws IOException {
		
		for(City city : City.values()) {
			getAllLocalities(city.toString());
		}
	}
}
