package org.cattech.homeAutomation.communicationHub;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicURLClassLoader extends URLClassLoader {

	// Found over here :
	// https://stackoverflow.com/questions/1010919/adding-files-to-java-classpath-at-runtime

	public DynamicURLClassLoader(URLClassLoader classLoader) {
		super(classLoader.getURLs());
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
	public void addFolderOfJars(String folderPath) throws MalformedURLException {
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));
		for (File file : listOfFiles) {
			addURL(file.toURI().toURL());
		}
	}

	public String getURLsAsString() {
		URL[] urls = getURLs();
		String urlStr = "";
		for (URL url : urls) {
			urlStr += ":" + url;
		}
		return urlStr;
	}
}