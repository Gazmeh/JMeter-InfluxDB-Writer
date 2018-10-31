package com.gazmeh.jmeter.sayan;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.samplers.SampleResult;
import org.influxdb.dto.Point.Builder;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/**
 * Sayan util
 * 
 * @author maso
 *
 */
public class SayanUtil {

    static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * Fill point with sayan sample result
     * 
     * @param sampleResult
     * @param pointBuilder
     * @return point builder
     * @throws ParseException               if the localdatetime is not valid
     * @throws UnsupportedEncodingException
     */
    public static Builder fillPoint(SampleResult sampleResult, Builder pointBuilder)
	    throws ParseException, UnsupportedEncodingException {
	String charsetName = sampleResult.getDataEncodingWithDefault();
	byte[] buff = sampleResult.getResponseData();
	// maso, 2018: decode and fill the point
	Object document = Configuration//
		.defaultConfiguration()//
		.jsonProvider()//
		.parse(new ByteArrayInputStream(buff), charsetName);

	// local date time
	String localTimeStr = JsonPath.read(document, "$.Body.DateTime");
	Date date = dateTimeFormat.parse(localTimeStr);

	// Message type
	String msgType = JsonPath.read(document, "$.Body.MsgType");
	return pointBuilder//
		.tag("MsgType", msgType)//
		.addField("SayanDateTime", date.getTime());
    }

}
