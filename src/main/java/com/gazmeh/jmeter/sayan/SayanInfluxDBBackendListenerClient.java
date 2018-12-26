package com.gazmeh.jmeter.sayan;

import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gazmeh.jmeter.InfluxDBBackendListenerClient;
import com.gazmeh.jmeter.config.influxdb.RequestMeasurement;

/**
 * Backend listener that writes JMeter metrics to influxDB directly.
 * 
 * @author Alexander Wert
 * @author Mostafa Barmshory (mostafa.barmshory@gmail.com)
 */
public class SayanInfluxDBBackendListenerClient extends InfluxDBBackendListenerClient {
    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SayanInfluxDBBackendListenerClient.class);

    public SayanInfluxDBBackendListenerClient() {
	setVirtualUserPoint(false);
    }

    /**
     * Converts the sample result into a poin.
     * 
     * @see com.gazmeh.jmeter.AbstractInfluxDBBackendListener#convertToPoint(org.apache.jmeter.samplers.SampleResult,
     *      org.apache.jmeter.visualizers.backend.BackendListenerContext,
     *      java.lang.String)
     */
    @Override
    protected Point convertToPoint(SampleResult sampleResult, BackendListenerContext context, String measurement) {
	long time = System.currentTimeMillis() * ONE_MS_IN_NANOSECONDS + getUniqueNumberForTheSamplerThread();
	Builder pointBuilder = Point.measurement(measurement)
		// time
		.time(time, TimeUnit.NANOSECONDS)
		// tags
		.tag(RequestMeasurement.Tags.REQUEST_NAME, sampleResult.getSampleLabel())//
		// fields
		.addField(RequestMeasurement.Fields.SUCCESS_COUNT, ((sampleResult.isSuccessful()) ? 1 : 0))//
		.addField(RequestMeasurement.Fields.LOAD_TIME, sampleResult.getTime())//
		.addField(RequestMeasurement.Fields.RESPONSE_TIME, sampleResult.getTime())//
		.addField(RequestMeasurement.Fields.IDLE_TIME, sampleResult.getIdleTime())//
		.addField(RequestMeasurement.Fields.CONNECTION_TIME, sampleResult.getConnectTime())//
		.addField(RequestMeasurement.Fields.BYTES, sampleResult.getBytesAsLong())//
		.addField(RequestMeasurement.Fields.SENT_BYTES, sampleResult.getSentBytes())//
		.addField(RequestMeasurement.Fields.TIME_STAMP, sampleResult.getTimeStamp());
	Point point = super.convertToPoint(sampleResult, context, measurement);
	if (!sampleResult.isSuccessful()) {
	    logger.info("Sample {} is not successfull so it is not possible to convert to sayan sample", sampleResult);
	    return point;
	}

	// Fill point with SAYAN result
	try {
	    SayanUtil.fillPoint(sampleResult, pointBuilder);
	} catch (Exception e) {
	    logger.error("Fail to convert sample to sayan", e);
	}
	return pointBuilder.build();
    }
}
