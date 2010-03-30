/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream based on a source stream that must only be partially read.
 */
public class InputEntityStream extends InputStream {

    /** The source stream. */
    private volatile InputStream source;

    /** The total size that should be read from the source stream. */
    private volatile long availableSize;

    /**
     * Constructor.
     * 
     * @param source
     *            The source stream.
     * @param size
     *            The total size that should be read from the source stream.
     */
    public InputEntityStream(InputStream source, long size) {
        this.source = source;
        this.availableSize = size;
    }

    /**
     * Reads a byte from the underlying stream.
     * 
     * @return The byte read, or -1 if the end of the stream has been reached.
     */
    @Override
    public int read() throws IOException {
        int result = -1;

        if (this.availableSize > 0) {
            result = this.source.read();

            if (result > 0) {
                this.availableSize = this.availableSize - 1;
            }
        }

        return result;
    }

}