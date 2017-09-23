package communicationHub;

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

import moduleBase.HomeAutomationModule;

public class Hub {

	private static ChannelController controller = null;

	public static void main(String[] args) throws IOException {
		controller = new ChannelController();
		NodeSocketConnectionManager server = new NodeSocketConnectionManager(10042, controller);
		new Thread(server, "Connection Manager").start();

		String modulesFolder = System.getenv("HOMEAUTOMODULES");
		if (null == modulesFolder) {
			modulesFolder = "./modules";
		}
		List<HomeAutomationModule> modules = loadModules(controller, modulesFolder);
		
		for (HomeAutomationModule mod : modules) {
			new Thread(mod,"Module : "+mod.getModuleChannelName()).start();
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

		if (null!=listOfFiles)
			for (File jarName : listOfFiles) {
				System.out.println("Jar : " + jarName);
	
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
						try {
							Object clazz = classLoader.loadClass(className).getConstructor(ChannelController.class).newInstance(controller);
							if (clazz instanceof HomeAutomationModule) {
								System.out.println("Loading module : " + clazz.getClass().getName());
								modules.add((HomeAutomationModule) clazz);
							}
						} catch (ClassNotFoundException
								|NoSuchMethodException
								|SecurityException
								|InstantiationException
								|IllegalAccessException
								|IllegalArgumentException
								|InvocationTargetException e1) {
							System.out.println("Could not load class : "+ className );
							e1.printStackTrace();
						}
					}
				} catch (IOException e2) {
					System.out.println("Could not load jar : "+ rawURL+ "("+e2.getMessage()+")");
				}
			}
		return modules;
	}
	//================================================================================
	//================================================================================

}
