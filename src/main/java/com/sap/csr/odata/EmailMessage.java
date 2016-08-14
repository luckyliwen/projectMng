package com.sap.csr.odata;
import javax.activation.DataSource;
public class EmailMessage {
	private String from, to, subject, body;
	private String attachmentName, attachmentContent;
	
	public String toString() {
		StringBuffer sb = new StringBuffer(100);
		sb.append("to:");  sb.append(to);
		sb.append(" subject:");  sb.append(subject);
		sb.append(" body:" + body);
		if ( attachmentName != null) {
			sb.append(" \r\nattachmentName:");  sb.append(attachmentName);
			sb.append(" \r\nattachmentContent:\r\n");  sb.append(attachmentContent);
		}
		return sb.toString();
	}
	
	public EmailMessage( String to, String subject, String body) {
		this.to = to;
		this.subject = subject;
		this.body = body;
	}
	
	public DataSource getAttachmentDataSource() {
		if ( attachmentName != null) {
			return new StringDataSource(attachmentName, attachmentContent);
		} else {
			return null;
		}
	}
	
	public void setAttachment(String name, String content) {
		attachmentName = name;
		attachmentContent = content;
	}

	/**
	 * @return the from
	 */
	public final String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public final void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public final String getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public final void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the subjecj
	 */
	public final String getSubject() {
		return subject;
	}

	/**
	 * @param subjecj the subjecj to set
	 */
	public final void setSubjecj(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the body
	 */
	public final String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public final void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the attachmentName
	 */
	public final String getAttachmentName() {
		return attachmentName;
	}

	/**
	 * @return the attachmentContent
	 */
	public final String getAttachmentContent() {
		return attachmentContent;
	}
	
	
}
