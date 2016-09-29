package com.sap.csr.odata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.activation.FileDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;




public class EmailMng  implements ServiceConstant, Runnable {
	
	 private static final Logger logger = LoggerFactory.getLogger(EmailMng.class);
	 private static InternetAddress  s_fromAddress;
	 static private Session mailSession = null;
	    static InternetAddress fromAddress = null;
		public static BlockingQueue<EmailMessage> blockQueue = null;
		private static boolean s_HasTryGetEmailSession  = false;
		private Transport transport = null;
		
	    static {
	    		/*InternetAddress[] address;
				try {
					//
					address = InternetAddress.parse("SAPHCPFinOps-notify@sap.com");
					fromAddress = address[0];
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/

				/*InitialContext ctx;
				try {
					ctx = new InitialContext();
					mailSession = (Session)ctx.lookup("java:comp/env/mail/EmailSession");
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
		
		    	blockQueue = new LinkedBlockingQueue<EmailMessage>();
		    	Thread thread = new Thread(new EmailMng());
		    	thread.start();
	    }
	    
	
	 
	 
	private void getLocalSession(String fileName) {
		String user = "";
		String password = "";
		
		Properties prop = new Properties();
		
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			prop.load(fis);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		user = prop.getProperty("mail.user");
		password = prop.getProperty("mail.password");
		System.out.println("user: <" + user + ">  pwd: <" + password + ">"  );
		
		final javax.mail.PasswordAuthentication auth = new javax.mail.PasswordAuthentication(user, password);
		
		mailSession = Session.getInstance(prop);
		mailSession = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
				return auth;
			}
		});

	}
	private static void getSession() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");

		    // get destination configuration for "myDestinationName", old name Session
		    DestinationConfiguration mailConfig = configuration.getConfiguration("EmailSession");
		    Map<String, String> map = mailConfig.getAllProperties();
		    Properties  prop = new Properties();
		    String user="";
			String password="";
			int smtpPort = 465; 
		    
		    for (String key :  map.keySet()) {
		    	if ( key.equals("Name") || key.equals("Type")) {
		    		continue;
		    	}
		    	
		    	String val = map.get(key);
		    	//As now mail destination can't use the upper case letter, so get the low case here		    	
//		    	if (key.equals("mail.smtp.socketfactory.port")) {
//		    		smtpPort = Integer.parseInt(val);
//		    		continue;
//		    	}
		    	if ( key.equals("mail.smtp.port")) {
		    		smtpPort = Integer.parseInt(val);
		    	}
		    	
		    	prop.put(key, val);
		    	if ( key.equals("mail.user"))
		    		user = val;
		    	else if (key.equals("mail.password")) {
		    		password = val;
		    	}
		    }
		    //Now we got user, so can set the fromAddress
		    InternetAddress[] fromAddress = InternetAddress.parse(user);
		    s_fromAddress = fromAddress[0];
		    
		    //As now destination can't add the upper case letter, so set it here
		    //??test sap mail, old "465"
		    prop.put("mail.smtp.socketFactory.port", smtpPort);
		    prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		    		
		    final javax.mail.PasswordAuthentication auth =  new javax.mail.PasswordAuthentication(
	                user, password);
		    
//		    mailSession = Session.getInstance(prop);
		    mailSession = Session.getDefaultInstance(prop, 
		    	    new javax.mail.Authenticator(){
		    	        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
		    	            return auth;
		    	        }
		    	});
		    logger.error("!!Get email session by old way ok {}", mailSession);
		} catch (NamingException e) {
			logger.error("^^Error of get sesion", e);
		} catch (AddressException e) {
			logger.error("^^Error of get sesion", e);
		}
	}
	
	public static boolean sendEmail(EmailMessage msg) {
		/*try {
			logger.error("~~~send email {}", msg.toString());
			
			if (mailSession == null  && !s_HasTryGetEmailSession) {
				s_HasTryGetEmailSession = true;
				getSession();
			}
			
			if (mailSession != null) {
				blockQueue.put(msg);
			}
				
			return true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error("Error in sendEmail", e);
			return false;
		}*/
		return true;
	}
	
	public void run() {
		EmailMessage message;
		while (true) {
			try {
				message = blockQueue.take();
				doSendEmail(message);
			} catch ( Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean doSendEmail(EmailMessage msg) throws Exception {
		Transport transport = null;
        try {
        	
        	
            // Construct message from parameters
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            //??how to get the from address
//            InternetAddress[] fromAddress = InternetAddress.parse(FROM_ADDRESS);
            InternetAddress[] toAddresses = InternetAddress.parse(msg.getTo());
            mimeMessage.setFrom(s_fromAddress);
            
            mimeMessage.setRecipients(RecipientType.TO, toAddresses);
            
            mimeMessage.setSubject( msg.getSubject(), "UTF-8");
            
            MimeMultipart multiPart = new MimeMultipart("alternative");
            MimeBodyPart part = new MimeBodyPart();
            
            //here auto add the refer and signature 
            StringBuffer sb = new StringBuffer(msg.getBody());
    
            part.setText(sb.toString(), "utf-8", "plain");
            multiPart.addBodyPart(part);
            
            //also add the attachment if have 
            DataSource attachmentDs = msg.getAttachmentDataSource();
            if (attachmentDs != null) {
            	MimeBodyPart attachment = new MimeBodyPart();
            	attachment.setDataHandler( new DataHandler(attachmentDs));
            	multiPart.addBodyPart(attachment);
            }
            
            mimeMessage.setContent(multiPart);

            // Send mail
            transport = mailSession.getTransport();
            transport.connect();
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            
            logger.error("###send email successful");
            return true;
         
        } catch (Exception e) {
            logger.error("Mail operation failed", e);
            throw new Exception(e);
        } finally {
            // Close transport layer
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    throw new Exception(e);
                }
            }
        }
	}
	
	public static void main(String[] args) {
		//StringBuffer sb = new StringBuffer();
		Formatter formatter = new Formatter(); //sb, Locale.US);
		//"Hello %s\r\nWe are sorried that your registration has been rejected because: %s \r\nBest Regards.";
//		formatter.format(MSG_REJECTED_BODY, "Lucky Li", "not provide id");
//		System.out.println(formatter.toString());
//		
//		System.out.println("==============");
//		formatter.format(MSG_APPROVED_BODY, "Lucky Li");
//		System.out.println(formatter.toString());
//		
		
		EmailMng em = new EmailMng();
		try {
			
//			em.getLocalSession("GmailDestination");
//			em.getLocalSession("QqEmailDestination");
//			em.getLocalSession("EmailDestination");
//			em.getLocalSession("SinaEmailDestination");
			
//			em.sendEmail("lucky.li01@sap.com", "csr again", " second first email " );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
