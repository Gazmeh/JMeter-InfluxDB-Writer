/*package com.gazmeh.jmeter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.jmeter.samplers.SampleResult;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.junit.Test;

import com.gazmeh.jmeter.sayan.SayanUtil;

public class SayanUtilTest {

    @Test
    public void testWriteFile() throws Exception {
	String dataStr = "{\"Body\":{\"SystemTraceNo\":\"SystemTraceNo\", \"DateTime\":\"2018-01-01T00:00:00+03:30\"}}";

	SampleResult sampleResult = mock(SampleResult.class);
	when(sampleResult.getDataEncodingWithDefault()).thenReturn("UTF-8");
	when(sampleResult.getResponseData()).thenReturn(dataStr.getBytes());

	Builder pointBuilder = Point.measurement("test") //
		.addField("size", 10);
	
	SayanUtil.fillPoint(sampleResult, pointBuilder);

	System.out.println(pointBuilder.build().lineProtocol());
    }
    
    @Test
    public void testWriteFile2() throws Exception {
	String dataStr = "{\"Body\":{\"SystemTraceNo\":\"SystemTraceNo\", \"DateTime\":\"2018-01-01T00:00:00.758+03:30\"}}";
	
	SampleResult sampleResult = mock(SampleResult.class);
	when(sampleResult.getDataEncodingWithDefault()).thenReturn("UTF-8");
	when(sampleResult.getResponseData()).thenReturn(dataStr.getBytes());
	
	Builder pointBuilder = Point.measurement("test") //
		.addField("size", 10);
	
	SayanUtil.fillPoint(sampleResult, pointBuilder);
	System.out.println(pointBuilder.build().lineProtocol());
    }
    
    @Test
    public void testWriteFileFail() throws Exception {
	String dataStr = "{\"Body\":{\"SystemTraceNo\":\"SystemTraceNo\"}}";
	
	SampleResult sampleResult = mock(SampleResult.class);
	when(sampleResult.getDataEncodingWithDefault()).thenReturn("UTF-8");
	when(sampleResult.getResponseData()).thenReturn(dataStr.getBytes());
	
	Builder pointBuilder = Point.measurement("test") //
		.addField("size", 10);
	
	SayanUtil.fillPoint(sampleResult, pointBuilder);
	System.out.println(pointBuilder.build().lineProtocol());
    }
}
*/