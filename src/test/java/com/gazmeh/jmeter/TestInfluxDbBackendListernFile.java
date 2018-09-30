package com.gazmeh.jmeter;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static com.gazmeh.jmeter.InfluxDBBackendListenerFile.*;

import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Test;

import com.gazmeh.jmeter.config.influxdb.RequestMeasurement;

public class TestInfluxDbBackendListernFile {

    public TestInfluxDbBackendListernFile() {
	// TODO Auto-generated constructor stub
    }

    @Test
    public void testWriteFile() throws Exception {
	BatchPoints batchPoints = BatchPoints//
		.database("Test")//
		.retentionPolicy("default")//
		// tags
		.tag(RequestMeasurement.Tags.NODE_NAME, "node")//
		.tag(RequestMeasurement.Tags.TEST_NAME, "test")//
		.tag(RequestMeasurement.Tags.RUN_ID, "id")//
		.build();

	Point point = Point.measurement(RequestMeasurement.MEASUREMENT_NAME)
		// time
		.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
		// tags
		.tag(RequestMeasurement.Tags.REQUEST_NAME, "/api/test")//
		// fields
		.addField(RequestMeasurement.Fields.ERROR_COUNT, 1)//
		.addField(RequestMeasurement.Fields.THREAD_NAME, "main")//
		.addField(RequestMeasurement.Fields.NODE_NAME, "node")//
		.addField(RequestMeasurement.Fields.RESPONSE_TIME, 12)//
		.addField(RequestMeasurement.Fields.BYTES, 1024)//
		.addField(RequestMeasurement.Fields.SENT_BYTES, 1024)//
		.build();

	batchPoints.point(point);
	
	assertNotNull(batchPoints.getPoints());

	InfluxDBBackendListenerFile influxFile = new InfluxDBBackendListenerFile();
	BackendListenerContext context = mock(BackendListenerContext.class);
	when(context.getParameter(KEY_FILE_PATH, "influxDBExport.txt")).thenReturn("influxDBExport.txt");
	when(context.getParameter(KEY_TEST_NAME, "Test")).thenReturn("Test");
	when(context.getParameter(KEY_RUN_ID, "R001")).thenReturn("R001");
	when(context.getParameter(KEY_RECORD_SUB_SAMPLES, "false")).thenReturn("false");
	when(context.getParameter(KEY_NODE_NAME, "Test-Node")).thenReturn("Test-Node");
	when(context.getParameter(KEY_SAMPLERS_LIST, "")).thenReturn("");
	when(context.getBooleanParameter(KEY_USE_REGEX_FOR_SAMPLER_LIST, false)).thenReturn(false);

	influxFile.setupTest(context);
	influxFile.writeBatchPoints(batchPoints);
	influxFile.teardownTest(context);
	
	// Check 
    }
}
