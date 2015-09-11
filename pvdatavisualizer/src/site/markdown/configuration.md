# Pure WAR usage

To configure the PV Data Visualizer place the following snippet in your &lt;TOMEE ROOT&gt;/conf/tomee.xml file:

	<Resource id="mongoClientURI" class-name="com.mongodb.MongoClientURI"
		constructor="uri">
		uri mongodb://localhost/pvdata
	</Resource>

Make sure you have a MongoDB instance running with a database (in the example above called pvdata) which contains your PV data collected using the pvdatalogger and pvdataloader.

# Docker container

After running mvn install, a docker image is created which can be used to run the PVData Visualizer.

To run a docker container:

	docker run -it --rm --link mongodb:mongodb -e "MONGODB_SERVER_URL=mongodb://mongodb/pvdata" -p 8888:8080 mminke/pvdatavisualizer:0.0.1-SNAPSHOT	

This statement expects a mongodb to be running (for example mminke/mongodb:latest).