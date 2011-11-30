mvn clean;
mvn package;
rm output;
mkdir output;
cd output;
tar -xf ../simulation/target/simulation-1.0-bin.tar.gz
cd simulation-1.0/lib;
java -jar -Xdebug -Xnoagent -Dava.rmi.server.hostname=192.168.2.4  simulation-1.0.jar 

