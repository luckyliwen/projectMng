package com.sap.csr.odata;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialException;

import org.apache.olingo.odata2.jpa.processor.api.OnJPAWriteContent;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import javax.sql.rowset.serial.SerialBlob;

public class OnDBWriteContent implements OnJPAWriteContent {

    public Blob getJPABlob(byte[] binaryData) throws ODataJPARuntimeException {
      try {
        return new SerialBlob(binaryData);
      } catch (SerialException e) {
        ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      } catch (SQLException e) {
        ODataJPARuntimeException.throwException(ODataJPARuntimeException.INNER_EXCEPTION, e);
      }
      return null;
    }



	public Clob getJPAClob(char[] characterData) throws ODataJPARuntimeException {
		// TODO Auto-generated method stub
		return null;
	}
}