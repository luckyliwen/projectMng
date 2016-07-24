package com.sap.csr.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

//@Embeddable

public class AttachmentPK implements Serializable {
	private String userId;
	private String projectId; 
	private String entryId;
	private String type;  
	 
	
	public AttachmentPK() {
		super();
	}
	
	
	


	/**
	 * @param userId
	 * @param projectId
	 * @param entryId
	 * @param type
	 */
	public AttachmentPK(String userId, String projectId, String entryId, String type) {
		super();
		this.userId = userId;
		this.projectId = projectId;
		this.entryId = entryId;
		this.type = type;
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


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryId == null) ? 0 : entryId.hashCode());
		result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttachmentPK other = (AttachmentPK) obj;
		if (entryId == null) {
			if (other.entryId != null)
				return false;
		} else if (!entryId.equals(other.entryId))
			return false;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
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
