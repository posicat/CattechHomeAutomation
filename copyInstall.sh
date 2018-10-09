mvn clean install -Dmaven.test.skip=true

if [ $? -ne 0 ] ; then
	echo "Error"
	exit
fi

sudo service homeAutomation stop

# Deploy the web code
cp -v  	www/*				/home/homeAutomation/public_html/

# Deoploy Java applications
mkdir /usr/local/homeAutomation/bin/

rm -rf /usr/local/homeAutomation/modules/
mkdir /usr/local/homeAutomation/modules/

rm -rf /usr/local/homeAutomation/lib/
mkdir /usr/local/homeAutomation/lib/

rm -rf /usr/local/homeAutomation/log/
mkdir /usr/local/homeAutomation/log/

mkdir /etc/homeAutomation/

find /usr/local/homeAutomation

cp -v Deploy/target/*.jar 		/usr/local/homeAutomation/bin/
cp -v Deploy/target/modules/* 		/usr/local/homeAutomation/modules/
cp -v Deploy/target/lib/* 	    	/usr/local/homeAutomation/lib/
cp -v -rpd lib/* 			/usr/local/homeAutomation/lib/
cp -v Deploy/target/etc/*		/etc/homeAutomation/

# Deploy Support Applicationa
cp -v SupportApplications/* 		/usr/local/homeAutomation/bin/


# Deploy website
cp -v -rpd www/*				/home/homeauto/public_html
