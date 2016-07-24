package com.sap.csr.odata;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SecurityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SecurityServlet.class);

    /** {@inheritDoc} */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	logger.error("$$$ do Get for SecurityServlet");
    	
        try {
            // Show name of logged in user
            response.getWriter().println("<p>current user attr: " + UserMng.getCurrentUserAttributes() + "</p>");

           
        } catch (Exception e) {
            // Login operation failed
            response.getWriter().println("Protected operation failed with reason: " + e.getMessage());
            logger.error("Protected operation failed", e);
        }
    }
}