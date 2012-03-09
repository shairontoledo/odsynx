package net.hashcode.fsw.transport

import java.io.File
import java.net.URI
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
import org.apache.log4j.Logger
import org.codehaus.jackson.node.ArrayNode
import org.villane.shttpc.Http
import org.villane.shttpc._
import scala.util.parsing.json._

object SHTTPTransport extends EntriesTransport{
  val log = Logger.getLogger(classOf[EntriesTransport])
  val http = new Http
  var server:HttpHost = _

  var serverScheme:String = _
  var serverPort:Int = _
  var serverName:String = _
  var username:String = _
  var password:String = _
  var email:String = _
  var autenticated = false 

  override def setCredentials(server:URI, username:String, password:String, email:String) {
    log.info("[%s] Set credentials username: %s email: %s".format(server,username, email))
    this.serverName = server.getHost
    this.serverPort =  server.getPort
    this.serverScheme =  server.getScheme
    this.username = username
    this.password = password
    this.email = email
    this.server = new HttpHost(serverName, serverPort,serverScheme)

  }

  override def	signup = {

    log.info("[%s] Signing up username: %s email: %s".format(server,username, email))
    val resp = http.post(basePathFor("/users.json"), Map(
        "user[username]"-> this.username,
        "user[customer_name]" -> ("Customer "+this.username),
        "user[password]" -> this.password,
        "user[email]" -> this.email
      ))

    if (Range(200,201).contains(resp.statusCode) ){
      log.info("[%s] Signed up with successfully username: %s email: %s".format(server,username, email))
      resp.asInputStream.close
      setHttpClient
      (true, null)
    }else{
      if (resp.asInputStream != null)
        resp.asInputStream.close
      log.error("[%s](status code: %s) Signing up username: %s email: %s".format(server, resp.statusCode, username, email))
      (false, resp.asJackson.asInstanceOf[ArrayNode])
    }

  }

  override def	authenticate = {

    log.info("[%s] Authenticating username: %s ".format(server,username))
    val resp = http.post(basePathFor("/session.json"), Map(
        "username"-> this.username,
        "password" -> this.password
      ))

    if (Range(200,201).contains(resp.statusCode) ){
      log.info("[%s] Authenticated username: %s".format(server,username))
      resp.asInputStream.close
      setHttpClient
      (true, null)
    }else{
      resp.asInputStream.close
      log.error("[%s](status code: %s) Authenticating username: %s".format(server, resp.statusCode, username))
      (false, "Incorrect username or password")
    }

  }

  //TODO thread-safe
  private def setHttpClient = {
    log.debug("[%s] setHttpClient username: %s".format(server,username))
    http.client.getCredentialsProvider.setCredentials(	
      AuthScope.ANY,
      new UsernamePasswordCredentials(username, password)
    )				


    val authCache = new BasicAuthCache
    val basicAuth = new BasicScheme
    authCache.put(server, basicAuth)

    val localcontext = new BasicHttpContext
    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
  }

  def	basePathFor(resource:String) = {
    "%s://%s:%s/ze/api%s".format(serverScheme,server.getHostName, server.getPort, resource)
  }

  override def checkRevision(revision:String = null):RemoteChanges = {
    setHttpClient
    log.info("[%s] Checking revision %s".format(server,revision))
    val resp = if (revision == null){
      http.get(basePathFor("/revisions/checkout"))

    }else{
      http.get(basePathFor("/revisions/%s/changes_since".format(revision)))
    }
    if (resp.statusCode == 304){
      //resp.asInputStream.close
      val rc = new RemoteChanges(null)
      rc.revision = revision
      return rc
    }
    return new RemoteChanges(resp.asText)
  }

  def	get(resouce:String):RemoteChanges = new RemoteChanges(http.get(basePathFor(resouce)).asText)

  //TODO refactory these methods 
  override def update(fe:FileEntry):RemoteChanges ={
    log.info("[%s] Updating %s".format(server,fe))
    new RemoteChanges(http.put(basePathFor("/files.json"),fileEntryFormData(fe),fe.file).asText)
  } 

  override def upload(fe:FileEntry):RemoteChanges ={
    log.info("[%s] Uploading %s".format(server,fe))
    new RemoteChanges(http.post(basePathFor("/files.json"),fileEntryFormData(fe),fe.file).asText)
  } 

  override def create(fe:FileEntry):RemoteChanges = {
    log.info("[%s] Creating %s".format(server,fe))
    new RemoteChanges(http.post(basePathFor("/files.json"),fileEntryFormData(fe)).asText)
  } 

  override def delete(fe:FileEntry):RemoteChanges = {
    log.info("[%s] Deleting %s".format(server,fe))
    new RemoteChanges(http.delete(basePathFor("/files/%s.json".format(fe.fkey))).asText)
  } 

  def	fileEntryFormData(fe:FileEntry) = Map(
    "file_entry[filename]" -> fe.filename ,
    "file_entry[server_path]" -> fe.serverPath ,
    "file_entry[is_directory]" -> fe.isDirectory.toString ,
    "file_entry[file_updated_time]" -> fe.localModifiedTime.toString ,
    "file_entry[parent_path]" -> fe.parentPath ,
    "file_entry[size]" -> fe.size.toString
  )

  override def download(fe:FileEntry, destination:File) = {
    log.info("[%s] downloading %s to %s".format(server,fe, destination))
    val req = http.get(basePathFor("/files/%s/download.json".format(fe.fkey)))
    FileUtils.copyInputStreamToFile(req.asInputStream, destination)
  }

}
