/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import javax.ws.rs.core.Response;

/**
 *
 * @author rlange
 */
public class CFException extends Exception {

    private Response.Status status;

    /**
     * Creates a new CFException with the specified HTTP return code for this request,
     * detail message and cause.
     *
     * @param status HTTP return code
     * @param message
     * @param cause
     */
    public CFException(Response.Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Creates a new CFException with the specified HTTP return code for this request.
     * and detail message.
     *
     * @param status HTTP return code
     * @param message
     */
    public CFException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    private String responseMessage() {
        String msg = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"" +
                " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
                "<html><head><title>ChannelServer - Error report</title></head>" +
                "<body><h1>HTTP Status " + this.status.getStatusCode() + " - " + this.status + "</h1><hr/>" +
                "<p><b>message</b></p><p>" + getMessage() + "</p>";
        if (this.getCause() != null) {
            msg = msg + "<p><b>caused by:</b></p><p>" + this.getCause().getMessage() + "</p>";
        }
        return msg + "</body></html>";
    }

    /**
     * Returns a HTTP Response object for this exception.
     * @return HTTP response
     */
    public Response toResponse() {
        return Response.status(status)
                    .entity(responseMessage())
                    .build();
    }

    /**
     * Returns the HTTP Response status code for this exception.
     * @return HTTP response
     */
    public Response.Status getResponseStatusString() {
        return status;
    }

    /**
     * Returns the HTTP Response status code for this exception.
     * @return HTTP response
     */
    public int getResponseStatusCode() {
        return status.getStatusCode();
    }
}
