package org.cattech.homeAutomation.communicationHub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class Hub {

	public static final String			HOMEAUTOMATION_HOME	= "HOMEAUTOMATION_HOME";
	private static ChannelController	controller			= null;
	private static Properties			props				= new Properties();

	public static void main(String[] args) throws IOException {
		loadConfiguration();
		controller = new ChannelController(props);

		NodeSocketConnectionManager server = new NodeSocketConnectionManager(10042, controller);
		new Thread(server, "Socket Connection Manager").start();

		String modulesFolder = System.getenv(HOMEAUTOMATION_HOME);
		if (null == modulesFolder) {
			modulesFolder = "./modules";
		} else {
			modulesFolder += "/modules/";
		}
		List<HomeAutomationModule> modules = loadModules(controller, modulesFolder);

		for (HomeAutomationModule mod : modules) {
			new Thread(mod, "Module : " + mod.getModuleChannelName()).start();
		}

		System.out.println("Listening...");
		try {
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();
	}

	//================================================================================
	private static List<HomeAutomationModule> loadModules(ChannelController controller, String modulesFolder) {
		List<HomeAutomationModule> modules = new ArrayList<HomeAutomationModule>();
		File folder = new File(modulesFolder);
		File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

		int total;
		if (null == listOfFiles) {
			total = 0;
		} else {
			total = listOfFiles.length;
		}

		System.out.println("| Loading " + total + " modules from " + modulesFolder);
		String classPath = System.getProperty("java.class.path");
		classPath += ";./lib/*";
		System.setProperty("java.class.path", classPath);
		System.out.println("| Classpath : " + classPath);

		if (null != listOfFiles) for (File jarName : listOfFiles) {
			System.out.println("| Found Jar : " + jarName);

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
					if (HomeAutomationModule.class.isAssignableFrom((Class<?>) clazz)) {
						System.out.println("	| " + ((Class<?>) clazz).getName()
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
						System.err.println("	| " + ((Class<?>) clazz).getName() + " is not a HomeAutomationModule");
					}
				}
			} catch (IOException e2) {
				System.err.println("	| Could not load jar : " + rawURL + "(" + e2.getMessage() + ")");
			}
			System.out.println(".\r\n\r\n");
		}
		return modules;
	}

	//================================================================================
	private static void loadConfiguration() throws IOException {
		String homePath = getHomePath().replace("\\", "/");
		FileInputStream input;
		try {
			input = new FileInputStream(homePath + "/etc/settings.conf");
			props.load(input);
		} catch (IOException e) {
			throw new IOException("Could not find configuration file, please set " + HOMEAUTOMATION_HOME, e);
		}
	}

	//================================================================================
	private static String getHomePath() {
		String home = System.getenv(HOMEAUTOMATION_HOME);

		if (null == home) {
			String os = System.getProperty("os.name");
			if (os.matches(".*Windows.*")) {
				home = "C:/homeAutomation/";
			}
			if (os.matches(".*Linux.*")) {
				home = "/etc/homeAutomation/";
			}
		}
		return home;
	}
	//================================================================================

	//================================================================================
	//================================================================================

}
