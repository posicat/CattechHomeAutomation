package org.cattech.homeAutomation.communicationHub;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class ModuleManager {
	private static Logger log = Logger.getLogger(ModuleManager.class);

	static ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
	HomeAutomationConfiguration config;

	public ModuleManager(HomeAutomationConfiguration config) throws Exception {
		this.config = config;
		addSoftwareLibrary(config.getLibFolder());
		log.info("Classpath:"+System.getProperty("java.class.path"));
	}

	private static void addSoftwareLibrary(String file) throws Exception {
		addSoftwareLibrary(new File(file).toURI().toURL());
	}
	
	private static void addSoftwareLibrary(URL fileURL) throws Exception {
		log.info("Adding "+fileURL.toString()+" to classloader.");

		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(systemClassLoader,fileURL);
	}

	// ================================================================================
	public List<HomeAutomationModule> findLoadableModules(ChannelController controller) {
		List<HomeAutomationModule> modules = new ArrayList<HomeAutomationModule>();
		File folder = new File(config.getModulesFolder());
		File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

		if (null == listOfFiles) {
			log.info("--- Did not find any modules to load ---");
		} else {
			log.info("Enumerating " + listOfFiles.length + " modules from " + config.getModulesFolder());

			for (File jarName : listOfFiles) {
				log.info("Searching for modules in : " + jarName);

				JarFile jarFile;
				URLClassLoader classLoader;
				String rawURL = null;
				try {
					URL[] url = { new URL("jar:file:" + jarName.getAbsolutePath() + "!/") };
					classLoader = new URLClassLoader(url,systemClassLoader);
					jarFile = new JarFile(jarName);
					Enumeration<JarEntry> e = jarFile.entries();

					while (e.hasMoreElements()) {
						JarEntry je = e.nextElement();
						if (je.isDirectory() || !je.getName().endsWith(".class")) {
							continue;
						}
						// -6 because of .class
						String className = je.getName().substring(0, je.getName().length() - 6);
						className = className.replace('/', '.');
						Object clazz = null;
						try {
							clazz = classLoader.loadClass(className);
						} catch (ClassNotFoundException e1) {
							log.error("Class we found was not found - should never happen");
							e1.printStackTrace();
						}
						if (clazz != null && HomeAutomationModule.class.isAssignableFrom((Class<?>) clazz)) {
							log.info("========== Located module : " + ((Class<?>) clazz).getName() + " ==========");
							try {
								clazz = ((Class<?>) clazz).getConstructor(ChannelController.class).newInstance(controller);
								modules.add((HomeAutomationModule) clazz);
							} catch (Exception e1) {
								log.error("Could not create instance of " + clazz.getClass().getName(), e1);
								log.info("Classpath : " + System.getProperty("java.class.path"));
							}
						}
					}
				} catch (IOException e2) {
					log.error("Could not load jar : " + rawURL + "(" + e2.getMessage() + ")");
				}
			}
		}
		return modules;
	}
}
