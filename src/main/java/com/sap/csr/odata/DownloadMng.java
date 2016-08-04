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
import java.util.Map;
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
	
	
	public void addAttachmentForOneRegistration(boolean multipleEntry, long attachmentCount, 
			String projectId, Map<String,Object>regMap ) {
		//{UserId: '', entryId, Type: '' }
		String userId = (String)regMap.get("UserId"); 
		String entryId = (String)regMap.get("EntryId");
		String type = (String) regMap.get("Type");
		String []aType = type.split(";");
		String ext = (String) regMap.get("Extension");
		String []aExt = ext.split(";");
		
		TypedQuery<Attachment>  query;
		int i=0;
		for (String fileType : aType) {
			query = em.createNamedQuery(ATTACHMENT_BY_ALL_PARAM, Attachment.class);
			query.setParameter("projectId", projectId);
			query.setParameter("userId", userId);
			query.setParameter("entryId", entryId);
			query.setParameter("type", fileType);
			
			Attachment attachment = query.getSingleResult();
			
			String fileName = userId;
			if ( multipleEntry) {
				fileName += "_" + entryId;
			}
			if ( attachmentCount >1) {
				fileName += "_" + fileType.substring( fileType.length() - 1 );
			}
			fileName += "." + aExt[ i++ ];
			
			try {
				zipOs.putNextEntry(new ZipEntry(fileName));
				zipOs.write( attachment.getContent(),0, attachment.getContent().length);
				zipOs.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	/*As we need pass a lot of parameter, so we need use Json as format
	 * FileName: zip file name
	 * MultipleEntry: true/false  if false then we can directly use userid as the file prefix 
	 * AttachmentCountPerUser:   if 1 then no need extra for the identify different file one user
	 * ProjectId:  as all share same project id, so we can just pass once
	 * Attachments: [
	 *     {UserId: '', entryId, Type: '' }  (Type if more than one, then separate it by ;   
	 * ]
	 * How to define the file name for one attachment:
	 *    1:  UserId +
	 *    2:  If MultipleEntry = false,  then no need the entryId. Otherwise, add the entryId (as we don't know it is what part)
	 *    3:  If AttachmentCountPerUser =1, then no need add the type, otherwise just get the last part(0~4) of the type
	 *    4:  last is the mine
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doInit(response);
		
		//later we need build a advance JsonUtility to get the correct value, now for simple just treat all as String, then parse it
		
//		boolean multipleEntry = Boolean.parseBoolean( (String)map.get("MultipleEntry"));
//		long attachmentCountPerUser = Long.parseLong( (String)map.get("AttachmentCountPerUser") );
//		String projectId = (String)map.get("ProjectId");
		boolean multipleEntry = Boolean.parseBoolean( request.getParameter("MultipleEntry"));
		long attachmentCountPerUser = Long.parseLong(request.getParameter("AttachmentCountPerUser"));
		String projectId = request.getParameter("ProjectId");
		String fileName = request.getParameter("FileName");
		
		List<Map<String, Object>> list = JsonUtility.readMapArrayFromString(readBodyAsString(request));
			
		response.setContentType("application/x-zip-compressed");
		response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		for (Map<String,Object> map: list) {
			addAttachmentForOneRegistration(multipleEntry, attachmentCountPerUser, projectId, map);
		}
		zipOs.close();

	}
	
	private String readBodyAsString(HttpServletRequest request) throws IOException {
		byte[] body = readBody(request.getInputStream());
		return  new String(body);
	}
	
	private byte[] readBody(InputStream is) throws IOException {
		int capacity = 4096 * 1000;
		byte[] temp = new byte[capacity];

		ByteBuffer result = ByteBuffer.allocate(capacity);
		int read = is.read(temp);
		while (read >= 0) {
			if (result.remaining() < read) {
				ByteBuffer tmpResult = ByteBuffer.allocate(result.capacity() + capacity);
				//tmpResult.put(result);
				byte []oldArray = result.array();
//				logger.error("curent old result " + String.valueOf(oldArray.length) + " " + String.valueOf(result.position()));
				tmpResult.put(oldArray, 0, result.position());
				result = tmpResult;
			}
			result.put(temp, 0, read);
			read = is.read(temp);
		}

		return Arrays.copyOf(result.array(), result.position());
	}
}