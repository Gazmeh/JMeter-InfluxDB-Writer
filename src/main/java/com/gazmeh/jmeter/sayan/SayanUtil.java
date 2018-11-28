package com.gazmeh.jmeter.sayan;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.crypto.Data;

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
	Date date = null;
	int index = 0x0;
	while (date == null && index < dateTimeFormat.length) {
	    convertDate(dateTimeFormat[index], localTimeStr);
	    index++;
	}
	if (date == null) {
	    date = new Date();
	}

	// Message type
	String msgType = JsonPath.read(document, "$.Body.MsgType");

	// System track number
	String systemTraceNo = JsonPath.read(document, "$.Body.SystemTraceNo");

	// ProcessingCode
	String processingCode = JsonPath.read(document, "$.Body.ProcessingCode");

	// ProcessingCode
	String terminalId = JsonPath.read(document, "$.Body.TerminalId");

	return pointBuilder//
		.tag("MsgType", msgType)//
		.tag("TerminalId", terminalId)//
		.tag("ProcessingCode", processingCode)//
		.addField("SystemTraceNo", systemTraceNo)//
		.addField("DateTime", date.getTime());
    }

    private static Date convertDate(SimpleDateFormat format, String localTimeStr) {
	try {
	    return format.parse(localTimeStr);
	} catch (Exception e) {
	    return null;
	}
    }

}
