#mvn imstall

cd /home/homeauto/homeAutomation/deploy/

mvn clean install -P deb deb:package


rm -rf /home/homeauto/debianContents
mkdir /home/homeauto/debianContents
cd /home/homeauto/debianContents

dpkg -x ../homeAutomation/deploy/target/*.deb files
dpkg -e ../homeAutomation/deploy/target/*.deb control

find .
