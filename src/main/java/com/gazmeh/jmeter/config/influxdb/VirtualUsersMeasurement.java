package com.gazmeh.jmeter.config.influxdb;

/**
 * Constants (Tag, Field, Measurement) names for the virtual users measurement.
 * 
 * @author Alexander Wert
 *
 */
public interface VirtualUsersMeasurement {

    /**
     * Measurement name.
     */
    String MEASUREMENT_NAME = "virtual_users";

    /**
     * Tags.
     * 
     */
    public interface Tags extends RequestMeasurement.Tags{
    }

    /**
     * Fields.
     */
    public interface Fields {
	String ACTIVE_THREADS = "active";
	String STARTED_THREADS = "started";
	String FINISHED_THREADS = "finished";
    }
}
