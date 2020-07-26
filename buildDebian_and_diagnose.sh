mvn clean install -DskipTests

cd /home/homeauto/CattechHomeAutomation/deploy/

mvn clean install

rm -rf /home/homeauto/debianContents
mkdir /home/homeauto/debianContents
cd /home/homeauto/debianContents

dpkg -x ../CattechHomeAutomation/deploy/target/*.deb files
dpkg -e ../CattechHomeAutomation/deploy/target/*.deb control

find .
