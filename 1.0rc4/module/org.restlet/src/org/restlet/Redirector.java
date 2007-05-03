/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.util.logging.Level;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.util.Template;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * 
 * @see org.restlet.util.Template
 * @see <a href="http://www.restlet.org/tutorial#part10">Tutorial: URI rewriting
 *      and redirection</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Redirector extends Restlet {
    /**
     * In this mode, the client is permanently redirected to the URI generated
     * from the target URI pattern.<br/> See
     * org.restlet.data.Status.REDIRECTION_PERMANENT.
     */
    public static final int MODE_CLIENT_PERMANENT = 1;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern.<br/> See
     * org.restlet.data.Status.REDIRECTION_FOUND.
     */
    public static final int MODE_CLIENT_FOUND = 2;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern.<br/> See
     * org.restlet.data.Status.REDIRECTION_SEE_OTHER.
     */
    public static final int MODE_CLIENT_SEE_OTHER = 3;

    /**
     * In this mode, the client is temporarily redirected to the URI generated
     * from the target URI pattern.<br/> See
     * org.restlet.data.Status.REDIRECTION_TEMPORARY.
     */
    public static final int MODE_CLIENT_TEMPORARY = 4;

    /**
     * In this mode, the call is sent to the context's dispatcher. Once the
     * selected client connector has completed the request handling, the
     * response is normally returned to the client. In this case, you can view
     * the Redirector as acting as a transparent proxy Restlet.<br>
     * <br>
     * Remember to add the required connectors to the parent Component and to
     * declare them in the list of required connectors on the
     * Application.connectorService property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed.
     */
    public static final int MODE_DISPATCHER = 5;

    /**
     * @deprecated Use the MODE_DISPATCHER constant instead.
     */
    @Deprecated
    public static final int MODE_CONNECTOR = 5;

    /** The target URI pattern. */
    protected String targetTemplate;

    /** The redirection mode. */
    protected int mode;

    /**
     * Constructor for the connector mode.
     * 
     * @param context
     *            The context.
     * @param targetTemplate
     *            The template to build the target URI.
     * @see org.restlet.util.Template
     */
    public Redirector(Context context, String targetTemplate) {
        this(context, targetTemplate, MODE_CONNECTOR);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetPattern
     *            The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param mode
     *            The redirection mode.
     */
    public Redirector(Context context, String targetPattern, int mode) {
        super(context);
        this.targetTemplate = targetPattern;
        this.mode = mode;
    }

    /**
     * Handles a call to a resource or a set of resources.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        // Generate the target reference
        Reference targetRef = getTargetRef(request, response);

        switch (this.mode) {
        case MODE_CLIENT_PERMANENT:
            getLogger().log(Level.INFO,
                    "Permanently redirecting client to: " + targetRef);
            response.redirectPermanent(targetRef);
            break;

        case MODE_CLIENT_FOUND:
            getLogger().log(Level.INFO,
                    "Redirecting client to found location: " + targetRef);
            response.setRedirectRef(targetRef);
            response.setStatus(Status.REDIRECTION_FOUND);
            break;

        case MODE_CLIENT_TEMPORARY:
            getLogger().log(Level.INFO,
                    "Temporarily redirecting client to: " + targetRef);
            response.redirectTemporary(targetRef);
            break;

        case MODE_DISPATCHER:
            getLogger().log(Level.INFO,
                    "Redirecting via client connector to: " + targetRef);
            redirectDispatcher(targetRef, request, response);
            break;
        }
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     * 
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void redirectDispatcher(Reference targetRef, Request request,
            Response response) {
        request.setResourceRef(targetRef);
        request.getAttributes().remove("org.restlet.http.headers");
        getContext().getDispatcher().handle(request, response);
        response.setEntity(rewrite(response.getEntity()));
        response.getAttributes().remove("org.restlet.http.headers");
    }

    /**
     * Returns the target reference to redirect to.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target reference to redirect to.
     */
    protected Reference getTargetRef(Request request, Response response) {
        // Create the template
        Template rt = new Template(getLogger(), this.targetTemplate);

        // Return the formatted target URI
        return new Reference(rt.format(request, response));
    }

    /**
     * Optionnaly rewrites the response entity returned in the MODE_CONNECTOR
     * mode. By default, it just returns the initial entity without any
     * modification.
     * 
     * @param initialEntity
     *            The initial entity returned.
     * @return The rewritten entity.
     */
    protected Representation rewrite(Representation initialEntity) {
        return initialEntity;
    }

}
