mvn clean install

cp -v Deploy/target/*.jar bin
cp -v Deploy/target/modules/* bin/modules
cp -v Deploy/target/lib/* bin/lib
cp -v Deploy/target/etc/* bin/etc
