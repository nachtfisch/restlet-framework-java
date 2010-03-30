/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.test.jaxrs.server;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.AllowAllAccess;
import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.util.Util;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan Koops
 */
public class RestletServerWrapper implements ServerWrapper {

    private AccessControl accessControl;

    private Component component;

    public RestletServerWrapper() {
    }

    /**
     * @return the accessControl
     */
    public AccessControl getAccessControl() {
        return accessControl;
    }

    /**
     * @param accessControl
     *                the accessControl to set. May be null to not require
     *                authentication.
     * @throws IllegalArgumentException
     */
    public void setAccessControl(AccessControl accessControl)
            throws IllegalArgumentException {
        this.accessControl = accessControl;
    }

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param appConfig
     * @return Returns the started component. Should be stopped with
     *         {@link #stopServer(Component)}
     * @throws Exception
     */
    public void startServer(final ApplicationConfig appConfig,
            Protocol protocol, final ChallengeScheme challengeScheme,
            Parameter contextParameter) throws Exception {
        Component comp = new Component();
        if (contextParameter != null)
            comp.getContext().getParameters().add(contextParameter);
        comp.getServers().add(protocol, 0);

        // Create an application
        Application application = new Application(comp.getContext()) {
            @Override
            public Restlet createRoot() {
                if (accessControl == null) {
                    return new JaxRsRouter(getContext(), appConfig,
                            AllowAllAccess.getInstance());
                }
                Guard guard = new Guard(getContext(), challengeScheme, "");
                guard.getSecrets().put("admin", "adminPW".toCharArray());
                guard.getSecrets().put("alice", "alicesSecret".toCharArray());
                guard.getSecrets().put("bob", "bobsSecret".toCharArray());
                JaxRsRouter router = new JaxRsRouter(getContext(), appConfig,
                        accessControl);
                guard.setNext(router);
                return guard;
            }
        };

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();
        this.component = comp;
        System.out.println("listening on port " + getPort());
    }

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception {
        if (this.component != null)
            this.component.stop();
    }

    public int getPort() {
        if (this.component == null)
            throw new IllegalStateException("the server is not started yet.");
        Server server = Util.getOnlyElement(this.component.getServers());
        int port = server.getPort();
        if (port > 0)
            return port;
        port = server.getEphemeralPort();
        if (port > 0)
            return port;
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // 
            }
            port = server.getEphemeralPort();
            if (port > 0)
                return port;
        }
        throw new IllegalStateException("Sorry, the port is not available");
    }
}