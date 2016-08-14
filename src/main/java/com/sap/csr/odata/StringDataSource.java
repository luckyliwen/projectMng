package com.sap.csr.odata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
public class StringDataSource implements DataSource {
	private String name, content;
	
	public StringDataSource(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public String getContentType() {
		return "application/octet-stream";
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream( content.getBytes());
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		return "Name: " + name + " Content: " + content;
	}

}


