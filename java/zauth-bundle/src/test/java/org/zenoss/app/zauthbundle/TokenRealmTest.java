// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.zenoss.app.zauthbundle;

import io.dropwizard.client.HttpClientBuilder;
import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.Before;
import org.junit.Test;
import org.zenoss.app.config.ProxyConfiguration;
import org.zenoss.app.security.ZenossTenant;
import org.zenoss.app.security.ZenossToken;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TokenRealmTest {


    HttpServletRequest request;
    CloseableHttpResponse response;
    TokenRealm realm;
    CloseableHttpClient mockClient;

    @Before
    public void setup() {
        this.request = mock(HttpServletRequest.class);
        this.response = mock(CloseableHttpResponse.class);
        this.mockClient = mock(CloseableHttpClient.class);
        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        when(builder.build("auth-client")).thenReturn(mockClient);
        TokenRealm.init(new ProxyConfiguration(), this.mockClient);
        this.realm = new TokenRealm();
    }

    @Test
    public void testAuthenticationTokenClassIsPresent() {
        Class<? extends AuthenticationToken> cls = realm.getAuthenticationTokenClass();
        assertEquals(cls, StringAuthenticationToken.class);
    }

    @Test
    public void testGetPostMethod() throws Exception {
        HttpPost method = realm.getPostMethod("test");
        // make sure we set our token we passed in into the params
        UrlEncodedFormEntity body = (UrlEncodedFormEntity) method.getEntity();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        body.writeTo(out);
        assertEquals("id=test", out.toString(StandardCharsets.UTF_8.name()));
        assertEquals(true, method.getURI().toString().startsWith("http://"));
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        CloseableHttpResponse response = getOkResponse();
        response.addHeader(ZenossTenant.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.ID_HTTP_HEADER, "id");
        response.addHeader(ZenossToken.EXPIRES_HTTP_HEADER, "0");
        AuthenticationInfo info = realm.handleResponse("token", response);
        assertFalse(info.getPrincipals().isEmpty());

        ZenossToken token = info.getPrincipals().oneByType(ZenossToken.class);
        assertEquals(0.0, token.expires());
        assertEquals("id", token.id());

        ZenossTenant tenant = info.getPrincipals().oneByType(ZenossTenant.class);
        assertEquals("id", tenant.id());

        //these should be the same...
        assertEquals("token", info.getCredentials());
    }

    private CloseableHttpResponse getOkResponse() {
        StatusLine status = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(status);

        when(response.getHeaders(ZenossTenant.ID_HTTP_HEADER)).
                thenReturn(new Header[]{new BasicHeader(ZenossTenant.ID_HTTP_HEADER, "id")});
        when(response.getHeaders(ZenossToken.ID_HTTP_HEADER)).
                thenReturn(new Header[]{new BasicHeader(ZenossToken.ID_HTTP_HEADER, "id")});
        when(response.getHeaders(ZenossToken.EXPIRES_HTTP_HEADER)).
                thenReturn(new Header[]{new BasicHeader(ZenossToken.EXPIRES_HTTP_HEADER, "0")});

        return response;
    }

    private CloseableHttpResponse getOkResponseNoHeaders() {
        StatusLine status = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(status);
        return response;
    }

    @Test(expected = AuthenticationException.class)
    public void testHandleBadResponse() throws Exception {
        StatusLine status = new BasicStatusLine(HttpVersion.HTTP_1_1, 401, "Unauthorized");
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(status);
        AuthenticationInfo info = realm.handleResponse("test", response);
    }

    @Test
    public void testDoGetAuthorization() throws Exception {
        CloseableHttpResponse  response = getOkResponse();

        when(this.mockClient.execute(any(HttpPost.class))).thenReturn(response);
        AuthenticationToken token = new StringAuthenticationToken("test", "");
        AuthenticationInfo results = realm.doGetAuthenticationInfo(token);
        assertEquals("test", results.getCredentials());
    }


    @Test(expected = AuthenticationException.class)
    public void testDoGetAuthorizationMissingTenantId() throws Exception {
        CloseableHttpResponse  response = getOkResponseNoHeaders();
        when(this.mockClient.execute(any(HttpPost.class))).thenReturn(response);
        AuthenticationToken token = new StringAuthenticationToken("test", "");
        realm.doGetAuthenticationInfo(token);
        fail();
    }

}
