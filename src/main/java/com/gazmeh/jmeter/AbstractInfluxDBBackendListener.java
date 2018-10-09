package com.gazmeh.jmeter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gazmeh.jmeter.config.influxdb.InfluxDBConfig;
import com.gazmeh.jmeter.config.influxdb.RequestMeasurement;
import com.gazmeh.jmeter.config.influxdb.VirtualUsersMeasurement;

/**
 * Backend listener which convert samples into a batch point
 * 
 * @author Mostafa Barmshory (mostafa.barmshory@gmail.com)
 * @sine 1.4.0
 */
public abstract class AbstractInfluxDBBackendListener implements BackendListenerClient {
    /*
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInfluxDBBackendListener.class);

    /**
     * Parameter Keys.
     */
    public static final String KEY_USE_REGEX_FOR_SAMPLER_LIST = "useRegexForSamplerList";
    public static final String KEY_TEST_NAME = "testName";
    public static final String KEY_RUN_ID = "runId";
    public static final String KEY_NODE_NAME = "nodeName";
    public static final String KEY_SAMPLERS_LIST = "samplersList";
    public static final String KEY_RECORD_SUB_SAMPLES = "recordSubSamples";
    public static final String KEY_MEASUREMENT_NAME = "measurementName";

    /**
     * Constants.
     */
    public static final String SEPARATOR = ";";
    public static final int ONE_MS_IN_NANOSECONDS = 1000000;

    /**
     * Name of the test.
     */
    private String testName;

    /**
     * A unique identifier for a single execution (aka 'run') of a load test. In a
     * CI/CD automated performance test, a Jenkins or Bamboo build id would be a
     * good value for this.
     */
    private String runId;

    /**
     * Name of the name
     */
    private String nodeName;

    /**
     * List of samplers to record.
     */
    private String samplersList = "";

    /**
     * Regex if samplers are defined through regular expression.
     */
    private String regexForSamplerList;

    /**
     * Set of samplers to record.
     */
    private Set<String> samplersToFilter;

    /**
     * InfluxDB configuration.
     */
    InfluxDBConfig influxDBConfig;

    /**
     * Random number generator
     */
    private Random randomNumberGenerator;

    /**
     * Indicates whether to record Subsamples
     */
    private boolean recordSubSamples;

    private boolean useRegexToFilter;

    private String measurementName;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.visualizers.backend.BackendListenerClient#setupTest(org.
     * apache.jmeter.visualizers.backend.BackendListenerContext)
     */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
	LOGGER.info("Influch backend listener is started..");
	influxDBConfig = new InfluxDBConfig(context);
	testName = context.getParameter(KEY_TEST_NAME, "Test");
	runId = context.getParameter(KEY_RUN_ID, "R001");
	recordSubSamples = Boolean.parseBoolean(context.getParameter(KEY_RECORD_SUB_SAMPLES, "false"));
	measurementName = context.getParameter(KEY_MEASUREMENT_NAME, RequestMeasurement.MEASUREMENT_NAME);
	/*
	 * Will be used to compare performance of R001, R002, etc of 'Test'
	 */
	randomNumberGenerator = new Random();
	nodeName = context.getParameter(KEY_NODE_NAME, "Test-Node");
	parseSamplers(context);
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
	// TODO: maso, 2018: write stop sample
    }

    /**
     * Parses list of samplers.
     *
     * @param context {@link BackendListenerContext}.
     */
    private void parseSamplers(BackendListenerContext context) {
	samplersList = context.getParameter(KEY_SAMPLERS_LIST, "");
	samplersToFilter = new HashSet<String>();
	useRegexToFilter = context.getBooleanParameter(KEY_USE_REGEX_FOR_SAMPLER_LIST, false);
	if (useRegexToFilter) {
	    regexForSamplerList = samplersList;
	} else {
	    regexForSamplerList = null;
	    String[] samplers = samplersList.split(SEPARATOR);
	    samplersToFilter = new HashSet<String>();
	    for (String samplerName : samplers) {
		samplersToFilter.add(samplerName);
	    }
	}
    }

    /*
     * Creates list of samples
     */
    private List<SampleResult> createSampleList(List<SampleResult> sampleResults) {
	// list
	List<SampleResult> allSampleResults = new ArrayList<>();
	for (SampleResult sampleResult : sampleResults) {
	    addSamplesToList(allSampleResults, sampleResult, recordSubSamples);
	}

	// filter
	List<SampleResult> result = new ArrayList<>();
	for (SampleResult sampleResult : allSampleResults) {
	    if ((null != regexForSamplerList && sampleResult.getSampleLabel().matches(regexForSamplerList))
		    || samplersToFilter.contains(sampleResult.getSampleLabel())) {
		result.add(sampleResult);
	    }
	}

	return result;
    }

    private void addSamplesToList(List<SampleResult> allSampleResults, SampleResult sampleResult,
	    boolean recordSubSamples2) {
	allSampleResults.add(sampleResult);

	if (recordSubSamples) {
	    for (SampleResult subResult : sampleResult.getSubResults()) {
		addSamplesToList(allSampleResults, subResult, recordSubSamples);
	    }
	}
    }

    /**
     * Try to get a unique number for the sampler thread
     */
    private int getUniqueNumberForTheSamplerThread() {
	return randomNumberGenerator.nextInt(ONE_MS_IN_NANOSECONDS);
    }

    /*
     * Converts sample into a point
     */
    private Point convertToPoint(SampleResult sampleResult, BackendListenerContext context, String measurement) {
	long time = System.currentTimeMillis() * ONE_MS_IN_NANOSECONDS + getUniqueNumberForTheSamplerThread();
	Point point = Point.measurement(measurement)
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
		.addField(RequestMeasurement.Fields.TIME_STAMP, sampleResult.getTimeStamp())//
		.build();
	return point;
    }

    private BatchPoints createBatchPoints(BackendListenerContext context) {
	BatchPoints batchPoints = BatchPoints//
		.database(influxDBConfig.getInfluxDatabase())//
		.retentionPolicy(influxDBConfig.getInfluxRetentionPolicy())//
		// tags
		.tag(RequestMeasurement.Tags.NODE_NAME, nodeName)//
		.tag(RequestMeasurement.Tags.TEST_NAME, testName)//
		.tag(RequestMeasurement.Tags.RUN_ID, runId)//
		.build();
	return batchPoints;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.visualizers.backend.BackendListenerClient#
     * handleSampleResults(java.util.List,
     * org.apache.jmeter.visualizers.backend.BackendListenerContext)
     */
    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
	// Gather all the listeners
	List<SampleResult> allSampleResults = createSampleList(sampleResults);
	BatchPoints batchPoints = createBatchPoints(context);
	String measurement = getMeasurementName();
	for (SampleResult sampleResult : allSampleResults) {
	    Point point = convertToPoint(sampleResult, context, measurement);
	    batchPoints.point(point);
	}
	batchPoints.point(createThreadsPoint(context));
	writeBatchPoints(batchPoints);
    }

    /**
     * Fetch measurement name
     * 
     * @return
     */
    private String getMeasurementName() {
	if (measurementName != null && !measurementName.isEmpty()) {
	    return measurementName;
	}
	return RequestMeasurement.MEASUREMENT_NAME;
    }

    /*
     * Creates a point to show VU counts
     */
    private Point createThreadsPoint(BackendListenerContext context) {
	ThreadCounts th = JMeterContextService.getThreadCounts();
	Point point = Point.measurement(VirtualUsersMeasurement.MEASUREMENT_NAME)//
		// time
		.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)//
		// fields
		.addField(VirtualUsersMeasurement.Fields.ACTIVE_THREADS, th.activeThreads)//
		.addField(VirtualUsersMeasurement.Fields.STARTED_THREADS, th.startedThreads)//
		.addField(VirtualUsersMeasurement.Fields.FINISHED_THREADS, th.finishedThreads)//
		.build();
	return point;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.visualizers.backend.BackendListenerClient#
     * getDefaultParameters()
     */
    @Override
    public Arguments getDefaultParameters() {
	Arguments arguments = new Arguments();
	arguments.addArgument(KEY_TEST_NAME, "Test");
	arguments.addArgument(KEY_NODE_NAME, "Test-Node");
	arguments.addArgument(KEY_RUN_ID, "R001");
	arguments.addArgument(KEY_SAMPLERS_LIST, ".*");
	arguments.addArgument(KEY_USE_REGEX_FOR_SAMPLER_LIST, "true");
	arguments.addArgument(KEY_RECORD_SUB_SAMPLES, "true");
	arguments.addArgument(KEY_MEASUREMENT_NAME, RequestMeasurement.MEASUREMENT_NAME);

	arguments.addArgument(InfluxDBConfig.KEY_INFLUX_DB_DATABASE, InfluxDBConfig.DEFAULT_DATABASE);
	arguments.addArgument(InfluxDBConfig.KEY_RETENTION_POLICY, InfluxDBConfig.DEFAULT_RETENTION_POLICY);
	return arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.visualizers.backend.BackendListenerClient#
     * SampleResult(org.apache.jmeter.visualizers.backend. BackendListenerContext,
     * org.apache.jmeter.samplers.SampleResult)
     */
    @Override
    public SampleResult createSampleResult(BackendListenerContext context, SampleResult result) {
	return result;
    }

    abstract protected void writeBatchPoints(BatchPoints batchPoints);
}
