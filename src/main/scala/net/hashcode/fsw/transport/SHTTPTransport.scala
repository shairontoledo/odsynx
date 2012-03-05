/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hashcode.fsw.transport

import java.io.File
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.RemoteChanges
import net.hashcode.fsw.models.RemoteChanges
import org.apache.commons.io.FileUtils
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.protocol.BasicHttpContext
import org.villane.shttpc.Http
import org.villane.shttpc._

class SHTTPTransport {
  val http = new Http
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
				
  
		def	basePathFor(resource:String) = {
				"http://%s:%s/ze/api%s".format(server.getHostName, server.getPort, resource)
		}
		
  def checkRevision(revision:String = null):RemoteChanges = {
				if (revision == null){
						get("/revisions/checkout")
						
				}else{
						get("/revisions/%s/changes_since".format(revision))
				}
		}

		def	get(resouce:String):RemoteChanges = new RemoteChanges(http.get(basePathFor(resouce)).asText)
		
  def update(fe:FileEntry):RemoteChanges = new RemoteChanges(http.put(basePathFor("/files.json"),formData(fe),fe.file).asText)
  def upload(fe:FileEntry):RemoteChanges = new RemoteChanges(http.post(basePathFor("/files.json"),formData(fe),fe.file).asText)
  def create(fe:FileEntry):RemoteChanges = new RemoteChanges(http.post(basePathFor("/files.json"),formData(fe)).asText)
  def delete(fe:FileEntry):RemoteChanges = new RemoteChanges(http.delete(basePathFor("/files/%s.json".format(fe.fkey))).asText)
		
		def	formData(fe:FileEntry) = Map(
								"file_entry[filename]" -> fe.filename ,
								"file_entry[server_path]" -> fe.serverPath ,
								"file_entry[is_directory]" -> fe.isDirectory.toString ,
								"file_entry[file_updated_time]" -> (fe.localModifiedTime / 1000).toString ,
								"file_entry[parent_path]" -> fe.parentPath ,
								"file_entry[size]" -> fe.size.toString
						)

		 def download(fe:FileEntry, destination:File) = {
					val req = http.get(basePathFor("/files/%s/download.json".format(fe.fkey)))
					FileUtils.copyInputStreamToFile(req.asInputStream, destination)
			}

}
