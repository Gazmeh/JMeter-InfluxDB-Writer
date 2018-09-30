package com.gazmeh.jmeter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gazmeh.jmeter.config.influxdb.InfluxDBConfig;

/**
 * Backend listener that writes JMeter metrics to influxDB directly.
 * 
 * @author Alexander Wert
 * @author Mostafa Barmshory (mostafa.barmshory@gmail.com)
 */
public class InfluxDBBackendListenerClient extends AbstractInfluxDBBackendListener {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBBackendListenerClient.class);

    /**
     * influxDB client.
     */
    private InfluxDB influxDB;

    @Override
    protected void writeBatchPoints(BatchPoints batchPoints) {
	influxDB.write(batchPoints);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gazmeh.jmeter.AbstractInfluxDBBackendListener#getDefaultParameters()
     */
    @Override
    public Arguments getDefaultParameters() {
	Arguments arguments = super.getDefaultParameters();
	arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_HOST, "localhost");
	arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_PORT, Integer.toString(InfluxDBConfig.DEFAULT_PORT));
	arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_USER, "");
	arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_PASSWORD, "");
	return arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gazmeh.jmeter.AbstractInfluxDBBackendListener#setupTest(org.apache.jmeter
     * .visualizers.backend.BackendListenerContext)
     */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
	super.setupTest(context);
	setupInfluxClient(context);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
	super.teardownTest(context);
	LOGGER.info("Shutting down influxDB client ...");
	influxDB.disableBatch();
	influxDB.close();
    }

    /*
     * Setup influxDB client.
     * 
     */
    private void setupInfluxClient(BackendListenerContext context) {
	influxDBConfig = new InfluxDBConfig(context);
	influxDB = InfluxDBFactory.connect(influxDBConfig.getInfluxDBURL(), influxDBConfig.getInfluxUser(),
		influxDBConfig.getInfluxPassword());
	influxDB.enableBatch(100, 5, TimeUnit.SECONDS);
	createDatabaseIfNotExistent();
    }

    /*
     * Creates the configured database in influx if it does not exist yet.
     */
    private void createDatabaseIfNotExistent() {
	// check db
	Query query = new Query("SHOW DATABASES", null);
	QueryResult result = influxDB.query(query);
	List<List<Object>> databaseNames = result.getResults().get(0).getSeries().get(0).getValues();
	List<String> databases = new ArrayList<>();
	if (databaseNames != null) {
	    for (List<Object> database : databaseNames) {
		databases.add(database.get(0).toString());
	    }
	}
	// create query
	if (!databases.contains(influxDBConfig.getInfluxDatabase())) {
	    String name = influxDBConfig.getInfluxDatabase();
	    Preconditions.checkNonEmptyString(name, "name");
	    String createDatabaseQueryString = String.format("CREATE DATABASE \"%s\"", name);
	    query = new Query(createDatabaseQueryString, null);
	    influxDB.query(query);
	}
    }
}
