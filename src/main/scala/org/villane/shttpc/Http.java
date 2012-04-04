package org.villane.shttpc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.helpers.MessageFormatter;

/**
 * Simplifies the use of Apache HTTP Client.
 *
 * @author erkki.lindpere
 */
public class Http {

  public static String DefaultPostEncoding = "UTF-8";
  public static String DefaultURIEncoding = "UTF-8";
  public static String Hostname;
  public final DefaultHttpClient client;
  public final Map<String, String> commonHeaders = new HashMap();

  static {
    try {
      Hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      Hostname = "Unknown";
    }

  }

  public Http() {
    this.client = new DefaultHttpClient(new ThreadSafeClientConnManager(SchemeRegistryFactory.createDefault()));
    commonHeaders.put("X-OfficeDrop-Replica", "odx-replica-" + Hostname);
    commonHeaders.put("User-Agent", "odsynx 1.1");

    //HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
    //this.client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
  }

  public Http(DefaultHttpClient client) {
    this.client = client;
  }

  public static Http newTrustingInstace() {
    return new Http(new TrustingHttpClient());
  }

  public SimpleHttpResponse get(String uri) throws ClientProtocolException, IOException, URISyntaxException {
    HttpGet get = new HttpGet(new URI(uri));
    return new SimpleHttpResponse(client.execute(get));
  }

  public SimpleHttpResponse get(String uri, Object... params) throws ClientProtocolException, IOException, URISyntaxException {
    return get(formatURI(uri, params));
  }

  public SimpleHttpResponse post(String uri, Object[] uriParams, Map<String, String> postParams)
          throws ClientProtocolException, IOException {
    return post(formatURI(uri, uriParams), postParams);
  }

  public SimpleHttpResponse post(String uri, Map<String, String> postParams) throws ClientProtocolException,
          IOException {
    return update(uri, postParams, "POST");
  }

  public SimpleHttpResponse post(String uri, Map<String, String> postParams, File file) throws ClientProtocolException,
          IOException {
    return update(uri, postParams, "POST", file);
  }

  public SimpleHttpResponse put(String uri, Map<String, String> postParams) throws ClientProtocolException,
          IOException {
    return update(uri, postParams, "PUT");
  }

  public SimpleHttpResponse put(String uri, Map<String, String> postParams, File file) throws ClientProtocolException,
          IOException {
    return update(uri, postParams, "PUT", file);
  }

  public SimpleHttpResponse update(String uri, Map<String, String> postParams, String httpMethod) throws ClientProtocolException,
          IOException {
    return update(uri, postParams, httpMethod, null);
  }

  public SimpleHttpResponse update(String uri, Map<String, String> postParams, String httpMethod, File file) throws ClientProtocolException,
          IOException {
    HttpEntityEnclosingRequestBase req = null;
    if (httpMethod.equals("POST")) {

      req = new HttpPost(uri);
      setHeaders(req);
    } else {

      req = new HttpPut(uri);
    }
    HttpEntity entity = null;
    if (file != null && file.exists()) {
      MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      ContentBody cbFile = new FileBody(file, "application/octet-stream");
      mpEntity.addPart("file", cbFile);
      entity = mpEntity;
      FormBodyPart fb = null;
      for (Map.Entry<String, String> param : postParams.entrySet()) {

        mpEntity.addPart(param.getKey(), new StringBody(param.getValue(), "text/plain", Charset.forName(DefaultPostEncoding)));

      }
    } else {
      List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
      for (Map.Entry<String, String> param : postParams.entrySet()) {
        paramsList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
      }
      entity = new UrlEncodedFormEntity(paramsList, DefaultPostEncoding);
    }
    req.setEntity(entity);
    return new SimpleHttpResponse(client.execute(req));
  }

  public SimpleHttpResponse delete(String uri) throws ClientProtocolException, IOException {
    HttpDelete del = new HttpDelete(uri);
    //del.setHeader("X-OfficeDrop-Replica", Hostname);
    setHeaders(del);
    return new SimpleHttpResponse(client.execute(del));
  }

  public SimpleHttpResponse delete(String uri, Object... params) throws ClientProtocolException, IOException {
    return delete(formatURI(uri, params));
  }

  protected static String formatURI(String uri, Object... params) {
    if (params.length > 0) {
      String[] encodedParams = new String[params.length];
      for (int i = 0; i < params.length; i++) {
        encodedParams[i] = uriEncode(params[i].toString());
      }
      return MessageFormatter.arrayFormat(uri, encodedParams).getMessage();
    }
    return uri;
  }

  @SuppressWarnings("deprecation")
  public static String uriEncode(String value) {
    try {
      return URLEncoder.encode(value, DefaultURIEncoding);
    } catch (UnsupportedEncodingException e) {
      return URLEncoder.encode(value);
    }
  }

  private void setHeaders(HttpRequestBase req) {
    Iterator it = commonHeaders.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();

      req.setHeader(pairs.getKey().toString(), pairs.getKey().toString());
      //"X-OfficeDrop-Replica"
    }
  }
}
