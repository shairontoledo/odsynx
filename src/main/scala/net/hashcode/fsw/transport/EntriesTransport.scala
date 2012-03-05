package net.hashcode.fsw.transport

import java.io.File
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.RemoteChanges

trait EntriesTransport {
  
  //Streamed calls
  def upload(fe:FileEntry):RemoteChanges
  def update(fe:FileEntry):RemoteChanges
  
  //Only k-values
  def create(fe:FileEntry):RemoteChanges
  def delete(fe:FileEntry):RemoteChanges
  def checkRevision(revision:String):RemoteChanges
  def download(fe:FileEntry, destination:File)
}
