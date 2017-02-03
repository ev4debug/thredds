/*
 * Copyright 1998-2015 University Corporation for Atmospheric Research/Unidata
 *  See the LICENSE file for more information.
 */

package dap4.test;


import dap4.core.util.DapUtil;
import dap4.servlet.DapController;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import thredds.server.dap4.Dap4Controller;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPMethod;
import ucar.httpservices.HTTPSession;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Provide a version of HTTPMethod that uses Spring servlet mocking.
 * This allows e.g. dap client code to avoid using an actual server
 * when testing.
 */

public class MockExecutor extends HTTPMethod.Executor
{
    //////////////////////////////////////////////////
    // Instance variables

    protected MockMvc mockMvc = null;
    protected String resourcepath = null; // absolute path to resource directory

    public MockExecutor(String path)
            throws HTTPException
    {
        if(path == null)
            throw new HTTPException("Null resourcepath");
        this.resourcepath = path;
        this.resourcepath = DapUtil.canonicalpath(this.resourcepath);
    }

    @Override
    public HttpResponse
    execute(HttpRequestBase rq, HttpHost targethost, HttpClient httpclient, HTTPSession session)
            throws HTTPException
    {
        // Extract stuff from the arguments
        URI uri = rq.getURI();
        String path = uri.getPath();
        path = DapUtil.canonicalpath(path);
        assert path.startsWith("/");
        String[] pieces = path.split("[/]");
        assert pieces.length >= 2;
        // Path is absolute, so pieces[0] will be empty
        // so pieces[1] should determine the controller
        DapController controller;
        if("d4ts".equals(pieces[1])) {
            controller = new D4TSController();
        } else if("thredds".equals(pieces[1])) {
            controller = new Dap4Controller();
        } else
            throw new HTTPException("Unknown controller type " + pieces[1]);
        StandaloneMockMvcBuilder mvcbuilder =
                MockMvcBuilders.standaloneSetup(controller);
        mvcbuilder.setValidator(new TestServlet.NullValidator());
        MockMvc mockMvc = mvcbuilder.build();

        MockHttpServletRequestBuilder mockrb = MockMvcRequestBuilders.get(uri);

        // We need to use only the path part
        mockrb.servletPath(uri.getPath());

        // Move any headers from rq to mockrb
        Header[] headers = rq.getAllHeaders();
        for(int i = 0; i < headers.length; i++) {
            Header h = headers[i];
            mockrb.header(h.getName(), h.getValue());
        }

        // Since the url we gave to get above has the query parameters,
        // they will be parsed and added to the rb parameters.

        // Finally set the resource dir
        mockrb.requestAttr("RESOURCEDIR", this.resourcepath);

        // Now invoke the servlet
        MvcResult result;
        try {
            result = mockMvc.perform(mockrb).andReturn();
        } catch (Exception e) {
            throw new HTTPException(e);
        }

        // Collect the output
        MockHttpServletResponse res = result.getResponse();
        byte[] byteresult = res.getContentAsByteArray();

        // Convert to HttpResponse
        this.response = new BasicHttpResponse(HttpVersion.HTTP_1_1, res.getStatus(), "");
        if(this.response == null)
            throw new HTTPException("HTTPMethod.execute: Response was null");

        Collection<String> keys = res.getHeaderNames();
        for(String key : keys) {
            List<String> values = res.getHeaders(key);
            for(String v : values) {
                this.response.addHeader(key, v);
            }
        }
        ByteArrayEntity entity = new ByteArrayEntity(byteresult);
        String sct = res.getContentType();
        entity.setContentType(sct);
        this.response.setEntity(entity);
        return this.response;
    }
}

