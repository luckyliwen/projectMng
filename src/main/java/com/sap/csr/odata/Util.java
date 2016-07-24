package com.sap.csr.odata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	final static Logger logger = LoggerFactory.getLogger(Util.class);

	public static String logException(String info, Exception e) {
		StringBuffer sb = new StringBuffer(info +  " exception: " + e.getMessage());

		StackTraceElement[] stackTraces = e.getStackTrace();
		logger.error("Exception call stack:" + info);
		for (StackTraceElement stack : stackTraces) {
			logger.error(stack.toString());
			sb.append("\r\n" + stack.toString());
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void debug(String format, Object obj) {
		logger.debug(format, obj);
	}

}
