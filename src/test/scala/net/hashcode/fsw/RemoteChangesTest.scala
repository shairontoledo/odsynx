package net.hashcode.fsw.models

import java.io.File

import net.hashcode.fsw.persistence.CurrentDatabase
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.specs.SpecificationWithJUnit
import scala.util.parsing.json.JSON._
import scala.util.parsing.json.JSONObject


class RemoteChangesTest extends SpecificationWithJUnit{

  "RemoteChange " should {
    "deserialize file entry" in {
      
      val str = FileUtils.readFileToString(new File("src/test/resources/assets/newfile_response.json")) 
      val rc = new RemoteChanges(str)
      rc.lastChange must notBeNull
      rc.lastChange.filename must beEqualTo("Inbox")
      rc.lastChange.fkey must beEqualTo("6f3c28b0531655e67b231ae037b458dc")
      rc.lastChange.csum must beEqualTo("1330204698")
      
      rc.revision must beEqualTo("4072532660")
    }
    "deserialize Array[file entry]" in {
      
      val str = FileUtils.readFileToString(new File("src/test/resources/assets/checkout_response.json")) 
      val rc = new RemoteChanges(str)
      rc.lastChange must beNull
      rc.revision must beEqualTo("852c0f2ba4c7802a6e63c2c02d80c4b8")
      
      rc.files.size must beEqualTo(2)
      
      val fe = rc.files(0)
      fe.size must beEqualTo(10)
      fe.fkey must beEqualTo("6f3c28b0531655e67b231ae037b458dc")
      fe.deleted must beEqualTo(false)
      fe.parentPath must beEqualTo("/")
      fe.filename must beEqualTo("Inbox")
      fe.localModifiedTime must beEqualTo(0)
      fe.isDirectory must beEqualTo(true)
      
      val fe2 = rc.files(1)
      fe2.size must beEqualTo(40)
      fe2.fkey must beEqualTo("aasdasdasdasd")
      fe2.deleted must beEqualTo(true)
      fe2.parentPath must beEqualTo("/foobar")
      fe2.filename must beEqualTo("document.docx")
      fe2.localModifiedTime must beEqualTo(2341)
      fe2.isDirectory must beEqualTo(false)
      
    }
  }

}