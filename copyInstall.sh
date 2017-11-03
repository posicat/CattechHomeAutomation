mvn clean install -Dmaven.test.skip=true

if [ $? -ne 0] ; then
	echo "Error"
	exit
fi

# Deploy the web code
cp -v  	www/*				/home/homeAutomation/public_html/

# Deoploy Java applications
mkdir /usr/local/homeAutomation/bin/
mkdir /usr/local/homeAutomation/bin/modules/
mkdir /usr/local/homeAutomation/bin/lib/
mkdir /usr/local/homeAutomation/bin/log/
mkdir /etc/homeAutomation/

cp -v Deploy/target/*.jar 		/usr/local/homeAutomation/bin/
cp -v Deploy/target/modules/* 		/usr/local/homeAutomation/bin/modules/
cp -v Deploy/target/lib/* 		/usr/local/homeAutomation/bin/lib/
cp -v -rpd lib/* 			/usr/local/homeAutomation/bin/lib/
cp -v Deploy/target/etc/*		/etc/homeAutomation/

# Deploy Support Applicationa
cp -v SupportApplications/* 		/usr/local/homeAutomation/bin/


# Deploy website
cp -v -rpd www/*				/home/homeauto/public_html
