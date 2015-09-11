FROM mminke/tomee-plus:1.7.2-jre8

MAINTAINER Morten Minke

COPY tomee.xml.template /opt/tomee/conf/
COPY scripts/start.sh /opt/tomee/
RUN chmod a+x /opt/tomee/start.sh
COPY mongo-java-driver.jar /opt/tomee/lib/
COPY pvdatavisualizer.war /opt/tomee/webapps/

CMD ["/opt/tomee/start.sh"]