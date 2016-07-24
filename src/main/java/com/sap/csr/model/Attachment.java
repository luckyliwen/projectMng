package com.sap.csr.model;

import java.beans.Transient;
import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

import com.sap.csr.odata.ServiceConstant;

@IdClass(AttachmentPK.class)
@Entity(name="Attachment")
@NamedQueries({ 
    @NamedQuery(name=ServiceConstant.ATTACHMENT_BY_ENTRY_AND_TYPE, query="select a from Attachment a where a.entryId= :entryId and a.type= :type"),
    //just need the entry can get all the attachment for one project, one user
    @NamedQuery(name=ServiceConstant.ATTACHMENT_BY_ENTRY, query="select a from Attachment a where a.userId = :userId  and a.entryId= :entryId")
}) 

public class Attachment implements Serializable {

	private static long serialVersionUID = 1L;
	
	
	
    @Id
    @Column(name = "ATT_USERID", length = 10, nullable = false)
    private String userId;
    
    @Id
    private String type;  //short description description of the file attachment type
    
    @Id
    private String projectId;  
    
    @Id
    private String entryId;  //the entry id for same user and projectId
    
    
//    private String fileName;  //file name of the file upload by user
    private String mimeType;
    
    //@Transient
//    private String theUserId;
    
   private byte [] content;
  
  
    public Attachment() {
    	super();
    	//attachmentPK = new AttachmentPK();
    }
    
    public Attachment(String userId, String projectId, String entryId,   String type){
    	super();
    	
    	this.userId = userId;
		this.projectId = projectId;
		this.entryId = entryId;
		this.type = type;
    }
    
	
	
	/**
	 * @return the mimeType
	 */
	public final String getMimeType() {
		return mimeType;
	}
	/**
	 * @param mimeType the mimeType to set
	 */
	public final void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	/**
	 * @return the content
	 */
	public final byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public final void setContent(byte[] content) {
		this.content = content;
	}

	//the key will not change
	public void updateProperty(final Attachment newAttachment) {
//		setFileName(newAttachment.getFileName());
		setMimeType(newAttachment.getMimeType());
		setContent(newAttachment.getContent());
	}
	

	/**
	 * @return the userId
	 */
	public final String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public final void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the type
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public final void setType(String type) {
		this.type = type;
	}

	public String createDownloadFileName() {
		String name = userId +"_" + type + ".";
		name += mimeType;
		return name;
	}

	/**
	 * @return the projectId
	 */
	public final String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public final void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the entryId
	 */
	public final String getEntryId() {
		return entryId;
	}

	/**
	 * @param entryId the entryId to set
	 */
	public final void setEntryId(String entryId) {
		this.entryId = entryId;
	}



	
}