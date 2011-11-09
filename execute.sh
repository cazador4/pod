mvn clean;
mvn package;
rm output;
mkdir output;
cd output;
tar -xf ../simulation/target/simulation-1.0-bin.tar.gz
cd simulation-1.0/lib;
java -jar -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n  simulation-1.0.jar 127.0.0.1 1099 id

