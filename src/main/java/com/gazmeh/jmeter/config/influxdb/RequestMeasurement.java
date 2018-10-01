package com.gazmeh.jmeter.config.influxdb;

/**
 * Constants (Tag, Field, Measurement) names for the requests measurement.
 * 
 * @author Alexander Wert
 * @author Mostafa Barmshory (mostafa.barmshory@gmail.com)
 *
 */
public interface RequestMeasurement {

    /**
     * Measurement name.
     */
    String MEASUREMENT_NAME = "samples";

    /**
     * Tags.
     * 
     */
    public interface Tags {
	String NODE_NAME = "tag_node_name";
	String REQUEST_NAME = "tag_name";
	String RUN_ID = "tag_run_id";
	String TEST_NAME = "tag_test_name";
    }

    /**
     * Fields.
     * 
     */
    public interface Fields {
	String RESPONSE_TIME = "response_time";
	String IDLE_TIME = "idle_time";
	String CONNECTION_TIME = "connection_time";
	String LOAD_TIME = "load_time";

	String BYTES = "recv_bytes";
	String SENT_BYTES = "sent_bytes";
	
	String ERROR_COUNT = "error";
	String SUCCESS_COUNT = "success";
	
	String THREAD_NAME = "thread_name";
	String NODE_NAME = "node_name";
    }
}
