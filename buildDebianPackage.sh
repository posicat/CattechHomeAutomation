cd /home/homeauto/CattechHomeAutomation

mvn clean install -DskipTests

cd /home/homeauto/CattechHomeAutomation/Deploy/

rm -rf /home/homeauto/debianContents
mkdir /home/homeauto/debianContents
cd /home/homeauto/debianContents

dpkg -x ../CattechHomeAutomation/Deploy/target/*.deb files
dpkg -e ../CattechHomeAutomation/Deploy/target/*.deb control

find .