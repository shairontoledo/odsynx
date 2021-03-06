package net.hashcode.fsw.transport

import java.io.File
import java.net.URI
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.RemoteChanges

trait EntriesTransport {

  case class Message(field:String, value:String, error:Boolean)

  //Streamed calls
  def upload(fe:FileEntry):RemoteChanges
  def update(fe:FileEntry):RemoteChanges

  def download(fe:FileEntry, destination:File)

  //Only k-values
  def create(fe:FileEntry):RemoteChanges
  def delete(fe:FileEntry):RemoteChanges
  def checkRevision(revision:String = null):RemoteChanges
  def authenticate:Tuple2[Boolean,Any]
  def share(filEntry: FileEntry):Boolean
  def signup:Tuple2[Boolean,Any]
  def setCredentials(server:URI, username:String, password:String, email:String)
  def search(keywords:String):List[String]
}
