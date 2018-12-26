package com.gazmeh.jmeter.sayan;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.influxdb.dto.Point.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/**
 * Sayan util
 * 
 * @author maso
 *
 */
public class SayanUtil {

    private static final Logger logger = LoggerFactory.getLogger(SayanUtil.class);

    static SimpleDateFormat dateTimeFormat[] = { //
	    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"), //
	    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"), //
	    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"), //
	    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"), //
    };

    /**
     * Fill point with sayan sample result
     * 
     * @param sampleResult
     * @param pointBuilder
     * @return system track number
     * @throws ParseException               if the localdatetime is not valid
     * @throws UnsupportedEncodingException
     */
    public static String fillPoint(SampleResult sampleResult, Builder pointBuilder)
	    throws ParseException, UnsupportedEncodingException {
	String charsetName = sampleResult.getDataEncodingWithDefault();
	byte[] buff = sampleResult.getResponseData();
	// maso, 2018: decode and fill the point
	Object document = Configuration//
		.defaultConfiguration()//
		.jsonProvider()//
		.parse(new ByteArrayInputStream(buff), charsetName);

	// local date time
//	long time = -1;
//	String localTimeStr = getJsonPartOrNull(document, "$.Body.DateTime", null);
//	Date date = null;
//	int index = 0x0;
//	while (localTimeStr != null && date == null && index < dateTimeFormat.length) {
//	    date = convertDate(dateTimeFormat[index], localTimeStr);
//	    index++;
//	}
//	if (date != null) {
//	    time = date.getTime();
//	}

	// Message type
	String msgType = getJsonPartOrNull(document, "$.Body.MsgType", "Null");

	// System track number
	String systemTraceNo = getJsonPartOrNull(document, "$.Body.SystemTraceNo", "Null");

	// ProcessingCode
	String processingCode = getJsonPartOrNull(document, "$.Body.ProcessingCode", "Null");

	// ProcessingCode
	String terminalId = getJsonPartOrNull(document, "$.Body.TerminalId", "Null");

	// ProcessingCode
	String responseCode = getJsonPartOrNull(document, "$.Body.ResponseCode", "Null");

	pointBuilder//
		.tag("MsgType", msgType)//
		.tag("TerminalId", terminalId)//
		.tag("ProcessingCode", processingCode)//
		.tag("ResponseCode", responseCode)//
		.addField("SystemTraceNo", systemTraceNo);

	return systemTraceNo;
    }

    public static String getJsonPartOrNull(Object document, String path, String defaultVal) {
	try {
	    String value = JsonPath.read(document, path);
	    if (StringUtils.isEmpty(value)) {
		return defaultVal;
	    }
	    return value;
	} catch (Exception e) {
	    logger.info("Fail to get response code", e);
	    return defaultVal;
	}
    }

    private static Date convertDate(SimpleDateFormat format, String localTimeStr) {
	try {
	    return format.parse(localTimeStr);
	} catch (Exception e) {
	    logger.info("Fail to parse date", e);
	    return null;
	}
    }

}
