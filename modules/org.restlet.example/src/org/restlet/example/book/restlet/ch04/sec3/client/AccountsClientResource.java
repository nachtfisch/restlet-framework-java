package org.restlet.example.book.restlet.ch04.sec3.client;

import org.restlet.example.book.restlet.ch04.sec3.common.AccountsResource;
import org.restlet.resource.ClientResource;

/**
 * Client accounts resource.
 */
public class AccountsClientResource extends ClientResource implements
        AccountsResource {

    /** Dynamic proxy converting Java calls into HTTP calls. */
    private AccountsResource proxy;

    /**
     * Constructor.
     * 
     * @param uri
     *            The target root URI.
     */
    public AccountsClientResource(String uri) {
        super(uri);

        // Creates the dynamic proxy
        this.proxy = wrap(AccountsResource.class);
    }

    public String represent() {
        // Delegates the call to the dynamic proxy
        return this.proxy.represent();
    }

    public String add(String account) {
        // Delegates the call to the dynamic proxy
        return this.proxy.add(account);
    }

}