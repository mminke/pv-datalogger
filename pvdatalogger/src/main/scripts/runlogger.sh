export INVERTER_IPADDRESS=192.168.0.2

java -jar pvdatalogger.jar -a $INVERTER_IPADDRESS 1>> pvdata.log 2>> pvdata.error.log &