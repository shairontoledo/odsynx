
package net.hashcode.fsw
import net.hashcode.fsw.persistence._
import net.hashcode.fsw.models._
import org.apache.log4j.Logger



object Main {

  def main(args: Array[String]):Unit = {
    
    val log = Logger.getLogger(Main.getClass)
    val mountPoint = "/tmp/docs"
    
    Volume.init(volName="My Volume", volPath = mountPoint)
    ///LuceneAsDatabase.init(Volume.mountPoint)
    //Volume.entries.foreach( e => log.info(""+e))
    
    // LuceneAsDatabase.save(Volume.entries)
    

    
  }
}
