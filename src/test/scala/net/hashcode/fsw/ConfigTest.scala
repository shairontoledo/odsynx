package net.hashcode.fsw
import org.specs._
import org.junit._
import Assert._
import java.io._

import net.hashcode.fsw.persistence._

class ConfigTest extends SpecificationWithJUnit{
  
  "A ConfigManager " should {
    
    "load a <montpoint>/.odx/config file" in {
      
      val path = new File("src/test/resources/assets").getAbsolutePath
      ConfigManager.configuration must beNull
      
      ConfigManager.initialize(path)
      ConfigManager.configuration must notBeNull
      val g = ConfigManager.configuration
      g.username must beEqualTo("shairon")
      g.password must beEqualTo("secreto")
      g.logtty must beEqualTo(true)
      g.remoteFolder must beEqualTo("/Documents")
    }
    
    "store values" in {
      val f = File.createTempFile("odtest2", "indexing")
      val dir = new File(new File(f.getParentFile, "od"+f.hashCode), ".odx")
      println(dir)
      dir.mkdirs
      
      dir.exists must beEqualTo(true)
      ConfigManager.configuration = null
      
      val path = dir.getAbsoluteFile.getParentFile.getAbsolutePath
      ConfigManager.configuration must beNull
      val conf = new Config(path)
      conf.username = "foo"
      conf.password = "bar"
      conf.remoteFolder = "/invoices"
      conf.logtty = false
      ConfigManager.store(conf)
      
      ConfigManager.initialize(path)
      ConfigManager.configuration must notBeNull
      val g = ConfigManager.configuration
      g.username must beEqualTo("foo")
      g.password must beEqualTo("bar")
      g.logtty must beEqualTo(false)
      g.remoteFolder must beEqualTo("/invoices")
      
      
    }
    
    "store null values" in {
      val f = File.createTempFile("odtest2", "indexing")
      val dir = new File(new File(f.getParentFile, "od"+f.hashCode), ".odx")
      println(dir)
      dir.mkdirs
      
      dir.exists must beEqualTo(true)
      ConfigManager.configuration = null
      
      val path = dir.getAbsoluteFile.getParentFile.getAbsolutePath
      ConfigManager.configuration must beNull
      val conf = new Config(path)
      conf.username = null
      conf.password = null
      conf.remoteFolder = null
      conf.logtty = false
      ConfigManager.store(conf)
      
      ConfigManager.initialize(path)
      ConfigManager.configuration must notBeNull
      val g = ConfigManager.configuration
      g.username must beEqualTo("")
      g.password must beEqualTo("")
      g.logtty must beEqualTo(false)
      //TODO support to remotefolder
      //						g.remoteFolder must beEqualTo("/")
      g.hasAuthenticationValues must beEqualTo(false)
      
      
    }

				
		}
}						