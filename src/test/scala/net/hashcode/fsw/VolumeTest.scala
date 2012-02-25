package net.hashcode.fsw.models
import org.specs._
import org.junit._
import Assert._
import java.io._
import org.apache.commons.io.FileUtils

class VolumeTest extends SpecificationWithJUnit{

  "Volume" should {
    doBefore{
      val f = File.createTempFile("odtest", "xxx")
      val dir = new File(f.getParentFile, "od"+f.hashCode) 
      dir.mkdir
      (dir.exists && dir.isDirectory) must beEqualTo(true)
      FileUtils.copyDirectory(new File("src/test/resources/doc"), new File(dir, "doc"))
      new File(dir,"doc").exists must beEqualTo(true)
      Volume.init("Test volume", dir.getAbsolutePath)
    }
    "create config dir" in {
      Volume.configDir must notBe(null)
      new File(Volume.configDir).exists must beEqualTo(true)
    }
    "create mount a existing directory" in {
      Volume.mountPoint must notBe(null)
      new File(Volume.mountPoint).exists must beEqualTo(true)
    }
    
    
  }
}
