package org.abhijit.jsoup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConnectionUtility {

	public static Set<String> getProxyList() {
		Set<String> proxySet = new HashSet<String>();
		try {
			Document proxyDoc = Jsoup.connect("http://free-proxy-list.net/anonymous-proxy.html").timeout(1000000).get();
			for (Element table : proxyDoc.select("table[id=proxylisttable]")) {
				for (Element row : table.select("tr")) {
					Elements tds = row.select("td");
					if (!tds.isEmpty()) {
						if (tds.get(1).text().equals("8080") && tds.get(6).text().equals("yes")) {
							System.out.println(tds.get(0).text() + ":" + tds.get(1).text() + ":" + tds.get(6).text());
							proxySet.add(tds.get(0).text());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return proxySet;
	}

	public static void main(String[] args) {
		getProxyList();
	}
}
