package net.hashcode.fsw.models

import scala.util.parsing.json._
import scala.collection.mutable.HashMap
import scala.util.parsing.json.JSONObject


class RemoteChanges(responseBody:String){
  var lastChange:FileEntry = _
  var files:List[FileEntry] = List()
  var revision:String = _

  if (responseBody != null) {
    JSON.parseFull(responseBody) match{
      case Some(obj) => {
          parse(obj.asInstanceOf[Map[String,Any]])
        }
      case None => println("error")
    }
  }

  def parse(map:Map[String,Any]) = {
    if (map.contains("file_entry") ){
      lastChange = mapToFileEntry(map.getOrElse("file_entry",new HashMap[String,Any]()).asInstanceOf[Map[String,Any]])
    }
    if (map.contains("revision") ){
      revision = map.getOrElse("revision","").asInstanceOf[String]
    }
    if (map.contains("files") ){
      files = for {
        entrymap <- map.getOrElse("files",List()).asInstanceOf[List[Map[String,Any]]]
        fe = mapToFileEntry(entrymap)
      } yield fe
    }
  }

  def mapToFileEntry(map:Map[String,Any]) = {
    var fe = new FileEntry
    fe.filename = map.getOrElse("filename","").asInstanceOf[String]
    fe.fkey = map.getOrElse("fkey","").asInstanceOf[String]
    fe.csum = map.getOrElse("csum","").asInstanceOf[String]
    fe.isDirectory = map.getOrElse("is_directory","false").toString.toBoolean
    fe.deleted = map.getOrElse("deleted","false").toString.toBoolean
    fe.parentPath = map.getOrElse("parent_path","/").asInstanceOf[String]
    fe.localModifiedTime = map.getOrElse("file_updated_time","0").toString.toLong
    fe.size = map.getOrElse("size","0").toString.toLong
    val thepath = fe.parentPath+"/"+fe.filename.toLowerCase
    fe.serverPath = FileEntry.fixedPath(thepath)
    fe
  }

}

