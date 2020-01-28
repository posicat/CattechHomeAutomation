#mvn clean install -PgatherFiles

if [ $? -ne 0 ] ; then
	echo "Error"
	exit
fi

# Create directories
mkdir /home/homeAutomation/public_html/
mkdir /usr/local/homeAutomation/bin/
mkdir /usr/local/homeAutomation/bin/modules/
mkdir /usr/local/homeAutomation/bin/lib/
mkdir /usr/local/homeAutomation/bin/log/
mkdir /etc/homeAutomation/

echo ----------

# Deoploy Java applications
cp -v deploy/gatherFiles/target/*.jar 		/usr/local/homeAutomation/bin/
cp -v deploy/gatherFiles/target/modules/* 	/usr/local/homeAutomation/bin/modules/
cp -v deploy/gatherFiles/target/lib/* 		/usr/local/homeAutomation/bin/lib/
#cp -v -rd lib/* 				/usr/local/homeAutomation/bin/lib/
cp -v deploy/gatherFiles/target/etc/*		/etc/homeAutomation/

# deploy Support Applicationa
cp -v SupportApplications/* 			/usr/local/homeAutomation/bin/


# deploy website
cp -v -rd www/*					/home/homeauto/public_html
