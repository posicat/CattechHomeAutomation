package org.cattech.homeAutomation.communicationHub;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.configuration.homeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class Hub {
	private static Logger log = Logger.getLogger(Hub.class);
	
	private static ChannelController	controller			= null;
	private static homeAutomationConfiguration config;
	
	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		config = new homeAutomationConfiguration();
		config.initialize();
		controller = new ChannelController(config);

		NodeSocketConnectionManager server = new NodeSocketConnectionManager(10042, controller);
		new Thread(server, "Socket Connection Manager").start();
		
		
		List<HomeAutomationModule> modules = loadModules();

		for (HomeAutomationModule mod : modules) {
			new Thread(mod, "Module : " + mod.getModuleChannelName()).start();
		}

		log.info("Listening...");
		try {
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Stopping Server");
		server.stop();
	}

	//================================================================================
	private static List<HomeAutomationModule> loadModules() {
		List<HomeAutomationModule> modules = new ArrayList<HomeAutomationModule>();
		File folder = new File(config.getModulesFolder());
		File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

		int total;
		if (null == listOfFiles) {
			total = 0;
		} else {
			total = listOfFiles.length;
		}

		log.info("| Loading " + total + " modules from " + config.getModulesFolder());
		String classPath = System.getProperty("java.class.path");
		classPath += config.getLibFolder();
		System.setProperty("java.class.path", classPath);
		log.info("| Classpath : " + classPath);

		if (null != listOfFiles) for (File jarName : listOfFiles) {
			log.info("| Found Jar : " + jarName);

			JarFile jarFile;
			URLClassLoader classLoader;
			String rawURL = null;
			try {
				rawURL = "jar:file:" + jarName.getAbsolutePath() + "!/";
				URL[] url = { new URL(rawURL) };
				classLoader = URLClassLoader.newInstance(url);
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
						System.err.println("	| Class we found was not found - should never happen");
						e1.printStackTrace();
					}
					if (clazz!=null && HomeAutomationModule.class.isAssignableFrom((Class<?>) clazz)) {
						log.info("	| " + ((Class<?>) clazz).getName()
								+ " is a HomeAutomationModule, Loading : " + ((Class<?>) clazz).getName());
						try {
							clazz = ((Class<?>) clazz).getConstructor(ChannelController.class).newInstance(controller);
							modules.add((HomeAutomationModule) clazz);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
							System.err.println("	| Could not create instance of " + clazz.getClass().getName());
							e1.printStackTrace();
						}
					} else {
						if (clazz != null) {
							System.err.println("	| " + ((Class<?>) clazz).getName() + " is not a HomeAutomationModule");
						}
					}
				}
			} catch (IOException e2) {
				System.err.println("	| Could not load jar : " + rawURL + "(" + e2.getMessage() + ")");
			}
			log.info(".\r\n\r\n");
		}
		return modules;
	}

	//================================================================================
	//================================================================================

}
