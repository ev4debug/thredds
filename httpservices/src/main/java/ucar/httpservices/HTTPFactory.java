/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.httpservices;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.message.BasicHttpResponse;

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * HTTPFactory creates method instance.
 * This code was originally in HttpMethod.
 */

public class HTTPFactory
{
    // In order to test client side code that mocks
    // HTTPMethod, provide a static global
    // than can be set by a test program.

    static public java.lang.Class MOCKMETHODCLASS = null;

    //////////////////////////////////////////////////////////////////////////
    // Static factory methods for creating HTTPSession instances

    static public HTTPSession newSession(String host, int port) throws HTTPException
    {
        return new HTTPSession(host, port);
    }

    static public HTTPSession newSession(String url) throws HTTPException
    {
        return new HTTPSession(url);
    }

    static public HTTPSession newSession(HttpHost target) throws HTTPException
    {
        return new HTTPSession(target);
    }

    @Deprecated
    static public HTTPSession newSession(AuthScope scope) throws HTTPException
    {
        HttpHost hh = new HttpHost(scope.getHost(), scope.getPort(), null);
        return new HTTPSession(hh);
    }

    //////////////////////////////////////////////////////////////////////////
    // Static factory methods for creating HTTPMethod instances

    static public HTTPMethod Get(HTTPSession session, String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Get, session, legalurl);
    }

    static public HTTPMethod Head(HTTPSession session, String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Head, session, legalurl);
    }

    static public HTTPMethod Put(HTTPSession session, String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Put, session, legalurl);
    }

    static public HTTPMethod Post(HTTPSession session, String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Post, session, legalurl);
    }

    static public HTTPMethod Options(HTTPSession session, String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Options, session, legalurl);
    }

    static public HTTPMethod Get(HTTPSession session) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Get, session, null);
    }

    static public HTTPMethod Head(HTTPSession session) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Head, session, null);
    }

    static public HTTPMethod Put(HTTPSession session) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Put, session, null);
    }

    static public HTTPMethod Post(HTTPSession session) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Post, session, null);
    }

    static public HTTPMethod Options(HTTPSession session) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Options, session, null);
    }

    static public HTTPMethod Get(String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Get, null, legalurl);
    }

    static public HTTPMethod Head(String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Head, null, legalurl);
    }

    static public HTTPMethod Put(String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Put, null, legalurl);
    }

    static public HTTPMethod Post(String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Post, null, legalurl);
    }

    static public HTTPMethod Options(String legalurl) throws HTTPException
    {
        return makemethod(HTTPSession.Methods.Options, null, legalurl);
    }

    /**
     * Common method creation code so we can isolate mocking
     *
     * @param session
     * @return
     * @throws HTTPException
     */
    static protected HTTPMethod makemethod(HTTPSession.Methods m, HTTPSession session, String url)
            throws HTTPException
    {
        java.lang.Class methodcl = HTTPMethod.class;
        if(MOCKMETHODCLASS != null)
            methodcl = MOCKMETHODCLASS;
        Constructor<HTTPMethod> cons = null;
        try {
            cons = methodcl.getConstructor(HTTPSession.Methods.class, HTTPSession.class, String.class);
        } catch (Exception e) {
            throw new HTTPException("HTTPFactory: no proper HTTPMethod constructor available", e);
        }
        HTTPMethod meth = null;
        try {
            meth = cons.newInstance(m, session, url);
        } catch (Exception e) {
            throw new HTTPException("HTTPFactory: HTTPMethod constructor failed", e);
        }
        return meth;
    }


    static public Set<String> getAllowedMethods()
    {
        HttpResponse rs = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 0, "");
        Set<String> set = new HttpOptions().getAllowedMethods(rs);
        return set;
    }

}
