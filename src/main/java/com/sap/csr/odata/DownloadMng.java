package com.sap.csr.odata;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.csr.model.Attachment;
import com.sap.csr.model.Registration;
import com.sap.csr.model.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadMng extends HttpServlet implements ServiceConstant {
	static Logger logger = LoggerFactory.getLogger(AttachmentMng.class);
	private EntityManager em = null;

	private ZipOutputStream zipOs;
	private String []aUserId;
	
	// ??need check the lifecycle later
	public void doInit(HttpServletResponse response) {
		try {
			em = JpaEntityManagerFactory.getEntityManagerFactory().createEntityManager();
			zipOs = new ZipOutputStream( response.getOutputStream());
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void addAttachmentForOneEntry(String entryId) {
		TypedQuery<Attachment>  query = em.createNamedQuery(ATTACHMENT_BY_ENTRY, Attachment.class);
		
		query.setParameter("entryId", entryId);
		List<Attachment> list = query.getResultList();
		for (Attachment att : list) {
			String fileName = att.createDownloadFileName();
			try {
				zipOs.putNextEntry(new ZipEntry(fileName));
				zipOs.write( att.getContent(),0, att.getContent().length);
				zipOs.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doInit(response);

		// get the parameter infor
		String entryIds = request.getParameter("EntryIds");
		String fileName = request.getParameter("FileName");

		response.setContentType("application/x-zip-compressed");
		response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		String[] aEntryId = entryIds.split(",");

		for (String entryId : aEntryId) {
			addAttachmentForOneEntry(entryId);
		}
		zipOs.close();

	}
}