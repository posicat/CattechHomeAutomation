#Cattech Home Automation



# Running/Debugging in Eclipse

## Importing Maven project into Eclipse

- File / Import / Existing Maven Projects
- Select CattechHomeAutomation folder
- Click Finsh

## Configuring Project to run in Eclipse

- Create a test configuration folder outside of the CattechHomeAutomation folder 
- Copy CattechHomeAutomation/Deploy/src/main/resources/usr/share/doc/CattechHomeAutomation/settings.conf.orig into this folder, 
- Rename your copy of the file settings.config

> - **Edit this file with any configuration changes you may want to make for your testing**
> - You may want to create a database, assign different ports, etc for testing.
> - Details of what can be set will exist in the .orig file, and can be found in the code of the relevant modules. (I'll get around to documenting this better later)
> - HINT : I personally keep this file in sync with my /etc/CattechHomeAutomation/settings.config using meld (diff would work too) to view differences.

- In Project Explorer Navigate to CattechHomeAutomation / Hub / Src/main/java / org.cattech.homeAutomation / commandLine / HomeAutomationHub.java
- Right click on HomeAutomationHub.java and Run As / Run Configurations
- Click Java Application
- Click New Launch Configuration (icon at top)
- On the Classpath tab

> - Click User Entries
> - Click Add Projects.....
> - Click Select All
> - Click OK

- On the Environment tab add

> - HOMEAUTOMATION_CONFIG
> - HOMEAUTOMATION_LOG
> - HOMEAUTOMATION_HOME - set these 3 to the path to your test configuration folder (created above)
> - HOMEAUTOMATION_MODULES - set to ../Deploy/target/root/usr/bin/CattechHomeAutomation/modules/
> - HOMEAUTOMATION_LIB - set to ../Deploy/target/root/usr/lib/CattechHomeAutomation/

>
> ** Other override environment variables can be referenced in org.cattech.homeAutomation.configuration.HomeAutomationConfiguration.initialize()

- Click Close and save