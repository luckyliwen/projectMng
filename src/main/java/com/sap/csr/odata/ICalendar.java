package com.sap.csr.odata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.model.Date;


import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Version;

import net.fortuna.ical4j.util.UidGenerator;

public class ICalendar {
	public final Logger logger = LoggerFactory.getLogger(ICalendar.class);
	private String startDateTime, endDateTime, location, subject, title, description;
	private int  timezoneOffset;
	
	public void setDuration(String startDateTime, String endDateTime, int offset) {
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		timezoneOffset = offset;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	/*
	 BEGIN:VCALENDAR
VERSION:2.0
PRODID:-
BEGIN:VEVENT
UID:sapitcloudSGNP6173077d-shop Session_2016 SAP Labs China Mid-autumn Day
DTSTART:20160916T040000Z
DTEND:20160918T040000Z
SUMMARY:d-shop Session_2016 SAP Labs China Mid-autumn Day
DESCRIPTION:Welcome\n\n\nAdditional Location Information:\nNear to JinKe Road\n\n\n\nhttps://fiorilaunchpad.sap.com/sites#my-events&/event=6173046
LOCATION:SAP Labs China PVG03 D5.3: 1000 ChenHui Road, PuDong 201203 Shanghai China
BEGIN:VALARM
TRIGGER:-PT30M
END:VALARM
END:VEVENT
DESCRIPTION:
LOCATION:SAP Labs China PVG03 D5.3: 1000 ChenHui Road, PuDong 201203 Shanghai China
END:VCALENDAR
	 */

	public String createStringContent() {
		try {
			StringBuffer sb = new StringBuffer("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-\r\nBEGIN:VEVENT\r\n");
			sb.append("UID:");
			sb.append("SAPSimpleEventMng-" + new Date().hashCode() + "-" + title);
			sb.append("\r\nDTSTART:");
			sb.append(getTimeStringFromString(startDateTime));
			sb.append("\r\nDTEND:");
			sb.append(getTimeStringFromString(endDateTime));
			sb.append("\r\nLOCATION:");
			sb.append(location);
			sb.append("\r\nSUMMARY:");
			sb.append(subject);
			sb.append("\r\nDESCRIPTION:");
			sb.append(description);
			sb.append("\r\nEND:VEVENT\r\nEND:VCALENDAR");
			return sb.toString();
		} catch (Exception e) {
			logger.error("Error of createStringContent", e);
		}
		return "";
	}

	// later check how to change the date percision to second
	public String createStringContent_old() {
		net.fortuna.ical4j.model.Calendar cal;
		try {
			cal = buildCalendarObject();
			return cal.toString();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public DataSource createDataSource() throws Exception {
		net.fortuna.ical4j.model.Calendar cal = buildCalendarObject();
		return new StringDataSource("Event of " + title, cal.toString());
	}

	public net.fortuna.ical4j.model.Calendar buildCalendarObject() throws ParseException, SocketException {
		long startTime = getTimeFromString(startDateTime);
		long endTime = getTimeFromString(endDateTime);

		VEvent vEvent = new VEvent(new Date(startTime), new Date(endTime), subject);
		// VEvent vEvent = new VEvent(
		// new Date( startTime , Dates.PRECISION_SECOND,
		// TimeZones.getDateTimeZone()),
		// new Date(endTime, Dates.PRECISION_SECOND,
		// TimeZones.getDateTimeZone()) ,
		// subject);
		Location locationProp = new Location();
		locationProp.setValue(this.location);
		vEvent.getProperties().add(locationProp);

		Description descProp = new Description();
		descProp.setValue(description);
		vEvent.getProperties().add(descProp);

		UidGenerator ug = new UidGenerator("SAP Advance Event Management Tool-");
		vEvent.getProperties().add(ug.generateUid());

		net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar();
		cal.getComponents().add(vEvent);
		// set other property
		Version ver = new Version();
		ver.setValue("2.0");
		cal.getProperties().add(ver);

		return cal;
	}

	public String getTimeStringFromString(String dateTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm");
		java.util.Date date = sdf.parse(dateTime);
		long time = date.getTime();
		// if PM, then need add 12 hours
		int pos = dateTime.indexOf(" PM");
		if (pos != -1) {
			time += 12 * 3600 * 1000;
		}
		//and need add the timezone offset
		time +=  timezoneOffset * 60 * 1000;
		date = new java.util.Date(time);
		
		//need format as 20160916T040000Z
		sdf = new SimpleDateFormat("yyyyMMdd");
		StringBuffer sb  = new StringBuffer(sdf.format(date));
		sb.append("T");
		
		sdf = new SimpleDateFormat("HHmmss");
		sb.append( sdf.format(date));
		
		sb.append("Z");
		return sb.toString();
	}

	public long getTimeFromString(String dateTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm");
		java.util.Date date = sdf.parse(dateTime);

		long time = date.getTime();

		// if PM, then need add 12 hours
		int pos = dateTime.indexOf(" PM");
		if (pos != -1)
			time += 12 * 3600 * 1000;

		return time;
	}

	public static void main(String[] args) {
		try {
			ICalendar iCal = new ICalendar();
			iCal.setDuration("08/12/16, 09:30 AM", "08/13/16, 11:30 AM", -480);
			iCal.setLocation("Labs of China");
			iCal.setSubject("Mid@Autumn Event");
			iCal.setTitle("Mid@Autumn");
			iCal.setDescription("Detal see www.sina.com");
			String output = iCal.createStringContent();
			System.out.println("==by api \r\n" + output);

			// ==============
//			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy, hh:mm");
//			java.util.Date d = sdf.parse("8/13/16, 10:31 AM");
//
//			System.out.println("date =" + d.toString());
//
//			FileDataSource fds = new FileDataSource(new File("t.txt"));
//			System.out.println("content type " + fds.getContentType());
//
//			java.util.Calendar calendar = java.util.Calendar.getInstance();
//			calendar.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER);
//			calendar.set(java.util.Calendar.DAY_OF_MONTH, 25);
//
//			// initialise as an all-day event..
//			// VEvent christmas = new VEvent(new Date(calendar.getTime()), new
//			// Date(), "Christmas Day");
//			Dur dur = new Dur(0, 2, 0, 0);
//			VEvent christmas = new VEvent(new Date(calendar.getTime()), dur, "Christmas Day");
//
//			// Generate a UID for the event..
//			UidGenerator ug = new UidGenerator("SAP Advance Event Management Tool-");
//			christmas.getProperties().add(ug.generateUid());
//
//			net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar();
//			cal.getComponents().add(christmas);
//			// set other property
//			Version ver = new Version();
//			ver.setValue("2.0");
//			cal.getProperties().add(ver);
//
//			CalendarOutputter outputter = new CalendarOutputter();
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//			System.out.println("content is " + cal.toString());
//
//			// outputter.output(cal, bos);
//			//
//			// String content = new String(bos.toByteArray());
//			// System.out.println("content is " + content);
		} catch (Exception e) {

		}
	}
}
