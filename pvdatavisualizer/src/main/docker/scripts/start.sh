#!/bin/bash

# Create config file using environment variables
sed -e s,MONGODB_SERVER_URL,$MONGODB_SERVER_URL, $CATALINA_HOME/conf/tomee.xml.template >$CATALINA_HOME/conf/tomee.xml

echo ====================================================================
echo Using environment:
echo --------------------------------------------------------------------
echo MONGODB_SERVER_URL=$MONGODB_SERVER_URL
echo CATALINA_HOME=$CATALINA_HOME
echo ====================================================================
echo Using tomee.xml:
echo --------------------------------------------------------------------
cat $CATALINA_HOME/conf/tomee.xml
echo ====================================================================

# Start tomee
$CATALINA_HOME/bin/catalina.sh run
