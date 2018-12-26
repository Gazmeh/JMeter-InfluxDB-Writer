package com.gazmeh.jmeter.config.influxdb;

/**
 * Constants (Tag, Field, Measurement) names for the measurement that denotes
 * start and end points of a load test.
 * 
 * @author Alexander Wert
 *
 */
public interface TestStartEndMeasurement {

    /**
     * Measurement name.
     */
    String MEASUREMENT_NAME = "testStartEnd";

    /**
     * Tags.
     * 
     * @author Alexander Wert
     *
     */
    public interface Tags {
	String TYPE = "type";
	String NODE_NAME = "nodeName";
	String RUN_ID = "runId";
	String TEST_NAME = "testName";
    }

    /**
     * Fields.
     * 
     * @author Alexander Wert
     *
     */
    public interface Fields {
	/**
	 * Test name field.
	 */
	String PLACEHOLDER = "placeholder";
    }

    /**
     * Values.
     * 
     * @author Alexander Wert
     *
     */
    public interface Values {
	/**
	 * Finished.
	 */
	String FINISHED = "finished";
	/**
	 * Started.
	 */
	String STARTED = "started";
    }
}
