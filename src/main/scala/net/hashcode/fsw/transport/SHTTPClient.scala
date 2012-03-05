package net.hashcode.fsw.transport
import net.hashcode.fsw.models.RemoteChanges
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.protocol.BasicHttpContext
import org.villane.shttpc.Http
import org.villane.shttpc._

class SHTTPClient {
  val http = new Http
//  //Streamed calls
//  def upload(fe:FileEntry):RemoteChanges 
//  def update(fe:FileEntry):RemoteChanges
//  
//  //Only k-values
//  def create(fe:FileEntry):RemoteChanges
//  def delete(fe:FileEntry):RemoteChanges
//  def checkRevision(revision:String):RemoteChanges
//  def download(fe:FileEntry, destination:File)

  
  def getBody(url:String) = {
				 val server = new HttpHost("localhost", 3000);
				http.client.getCredentialsProvider().setCredentials(	
						new AuthScope(server.getHostName, server.getPort),
						new UsernamePasswordCredentials("user111", "password")
				)
				
				val authCache = new BasicAuthCache
				// Generate BASIC scheme object and add it to the local
				// auth cache
				val basicAuth = new BasicScheme
				authCache.put(server, basicAuth);

				// Add AuthCache to the execution context
				val localcontext = new BasicHttpContext
				localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)

				
    val req = http.get("http://localhost:3000/ze/api/files.json" ? Map(
        "test" -> "foo"
      ))
    printf("-> req"+req.asText)
    
    val req2 = http.get("http://localhost:3000/ze/api/revisions/checkout.json" ? Map(
        "test2" -> "foo2"
      ))
				val files = new RemoteChanges(req2.asText).files
    printf("\n\n-> total: "+files.length)
    
  }
}
