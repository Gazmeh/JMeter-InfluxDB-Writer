package com.gazmeh.jmeter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backend listener that writes JMeter metrics to influxDB directly.
 * 
 * @author Alexander Wert
 * @author Mostafa Barmshory (mostafa.barmshory@gmail.com)
 *
 */
public class InfluxDBBackendListenerFile extends AbstractInfluxDBBackendListener {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBBackendListenerFile.class);

    /**
     * Parameter Keys.
     */
    public static final String KEY_FILE_PATH = "filePath";

    /**
     * Export File Writer.
     */
    private BufferedWriter exportFileWriter;

    /*
     * (non-Javadoc)
     * 
     * @see com.gazmeh.jmeter.AbstractInfluxDBBackendListener#writeBatchPoints(org.
     * influxdb.dto.BatchPoints)
     */
    @Override
    protected void writeBatchPoints(BatchPoints batchPoints) {
	List<Point> points = batchPoints.getPoints();
	for (Point point : points) {
	    try {
		exportFileWriter.append(point.lineProtocol());
		exportFileWriter.newLine();
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.gazmeh.jmeter.AbstractInfluxDBBackendListener#getDefaultParameters()
     */
    @Override
    public Arguments getDefaultParameters() {
	Arguments arguments = super.getDefaultParameters();
	arguments.addArgument(KEY_FILE_PATH, "influxDBExport.txt");
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

	LOGGER.info("Start influxDB filre writer ...");
	File exportFile = new File(context.getParameter(KEY_FILE_PATH, "influxDBExport.txt"));
	if (exportFile.getParentFile() != null && !exportFile.getParentFile().exists()) {
	    exportFile.getParentFile().mkdirs();
	}

	if (exportFile.exists()) {
	    exportFile.delete();
	    boolean created = exportFile.createNewFile();
	    if (!created) {
		throw new RuntimeException("Export file could not be created!");
	    }
	}

	exportFileWriter = new BufferedWriter(new FileWriter(exportFile));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.visualizers.backend.BackendListenerClient#teardownTest(org.
     * apache.jmeter.visualizers.backend.BackendListenerContext)
     */
    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
	super.teardownTest(context);
	LOGGER.info("Shutting down influxDB filre writer ...");
	exportFileWriter.close();
    }

}
