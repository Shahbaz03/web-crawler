package com.webcrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawler Starter App
 * 
 * @author Shahbaz.Alam
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
	    System.out.print("Search : ");
	    String keyword = scanner.nextLine();
		keyword = keyword.replace(" ", "+");
		String url = "https://www.google.co.in/search?q=" + keyword + "&num=" + String.valueOf(10);

		//Connect to the url and obain HTML response
		Document searchResults = getDocuments(url);
		if(searchResults == null){
			System.exit(1);
		}

		List<String> pageUrls = parseDocument(searchResults);
		List<String> javasriptLibraries = new ArrayList<String>();
		
		pageUrls.stream()
			.filter(pageUrl -> pageUrl.contains("http"))
			.map(App::getDocuments)
			.filter(document -> document != null)
			.map(App::getJavascriptLibraries)
			.forEach(list -> javasriptLibraries.addAll(list));
		

		System.out.println("Top 5 Javascript Libraries-------");
		javasriptLibraries.parallelStream().
            collect(Collectors.toConcurrentMap(
                w -> w, w -> 1, Integer::sum))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new)).forEach((k,v) -> System.out.println(k + ":" + v));
		
		
		scanner.close();
	}
	
	private static List<String> getJavascriptLibraries(Document document) {
		return document.select("script[src]").stream()
				.map(element -> element.attr("src"))
				.filter(js -> js.contains("http"))
				.collect(Collectors.toList());
	}

	private static List<String> parseDocument(Document document) {
		List<String> hrefLinks = new ArrayList<String>();
		//parsing HTML after examining DOM
		Elements elements  = document.select("h3.r > a");
		System.out.println("Top Pages Found::");
		for(Element element : elements) {
			String linkHref = element.attr("href");
			String pageUrl = linkHref.substring(7, linkHref.indexOf("&"));
			if(pageUrl.contains("%"))
				pageUrl = pageUrl.substring(0, pageUrl.indexOf("%"));
			
			System.out.println(pageUrl);
			hrefLinks.add(pageUrl);
		}
		return hrefLinks;
	}

	private static Document getDocuments(String url) {
		Document document = null;
		try {
			document = Jsoup
					.connect(url)
					.userAgent("Mozilla")
					.timeout(5000).get();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return document;
	}
}
