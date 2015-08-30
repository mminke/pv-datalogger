To configure the PV Data Visualizer place the following snippet in your <TOMEE ROOT>/conf/tomee.xml file:

	<Resource id="mongoClientURI" class-name="com.mongodb.MongoClientURI"
		constructor="uri">
		uri mongodb://localhost/pvdata
	</Resource>

Make sure you have a MongoDB instance running with a database (in the example above called pvdata) which contains your PV data collected using the pvdatalogger and pvdataloader.