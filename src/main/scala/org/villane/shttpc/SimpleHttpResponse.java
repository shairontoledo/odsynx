package org.villane.shttpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SimpleHttpResponse {
  public static final String DefaultEncoding = "UTF-8";
  public final HttpResponse response;
  private MappingJsonFactory jsonFactory = null;

  public SimpleHttpResponse(HttpResponse response) {
    this.response = response;
  }

  public Header[] headers() {
    return response.getAllHeaders();
  }

  public Header[] headers(String name) {
    return response.getHeaders(name);
  }

  public int statusCode() {
    return response.getStatusLine().getStatusCode();
  }

  public String reasonPhrase() {
    return response.getStatusLine().getReasonPhrase();
  }

  public InputStream asInputStream() throws IOException {
    return response.getEntity().getContent();
  }

  public byte[] asBytes() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    response.getEntity().writeTo(buf);
    EntityUtils.consume(response.getEntity());
    return buf.toByteArray();
  }

  public String asText(String encoding) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    response.getEntity().writeTo(buf);
    EntityUtils.consume(response.getEntity());
    return buf.toString(encoding);
  }

  public String asText() throws IOException {
    String enc = declaredCharSet();
    return enc != null ? asText(enc) : asText(DefaultEncoding);
  }

  public Document asDomXml() throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(response.getEntity().getContent());
    EntityUtils.consume(response.getEntity());
    return doc;
  }

  public Document asDomHtml() throws IOException, ParserConfigurationException, SAXException {
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(response.getEntity().getContent()));
    EntityUtils.consume(response.getEntity());
    return parser.getDocument();
  }

  public JsonNode asJackson() throws JsonParseException, IOException {
    if (jsonFactory == null) {
      jsonFactory = new MappingJsonFactory();
    }
    JsonParser jsonParser = jsonFactory.createJsonParser(asInputStream());
    JsonNode value = jsonParser.readValueAsTree();
    EntityUtils.consume(response.getEntity());
    return value;
  }

  public <T> T mapWithJackson(Class<T> t) throws JsonParseException, IOException {
    if (jsonFactory == null) {
      jsonFactory = new MappingJsonFactory();
    }
    JsonParser jsonParser = jsonFactory.createJsonParser(asInputStream());
    T value = jsonParser.readValueAs(t);
    EntityUtils.consume(response.getEntity());
    return value;
  }

  public String declaredCharSet() {
    return EntityUtils.getContentCharSet(response.getEntity());
  }

  public void consume() throws IOException {
    EntityUtils.consume(response.getEntity());
  }

}
