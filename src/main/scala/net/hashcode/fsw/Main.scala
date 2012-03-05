
package net.hashcode.fsw
import net.hashcode.fsw.persistence._
import java.io.File
import net.hashcode.fsw.models._
import net.hashcode.fsw.transport.SHTTPClient
import net.hashcode.fsw.transport.SHTTPTransport
import org.apache.log4j.Logger



object Main {

  def main(args: Array[String]):Unit = {
    
//    val log = Logger.getLogger(Main.getClass)
//    val mountPoint = "/tmp/docs"
    
    val sht = new SHTTPTransport
				//sht.checkRevision(null).files.foreach(println)
				//sht.create( FileEntry("/private/tmp/docs/sync api.markdown","/private/tmp" ))
//				println( sht.create( FileEntry("/private/tmp/docs/sync_api.markdown.html","/private/tmp" )).lastChange)
				println( sht.download(
								FileEntry("/private/tmp/docs/sync_api.markdown.html","/private/tmp" ), 
								new File("/tmp/docs/newfile.html")
						)
												)
    
    //Volume.init(volName="My Volume", volPath = mountPoint)
    ///LuceneAsDatabase.init(Volume.mountPoint)
    //Volume.entries.foreach( e => log.info(""+e))
    
    // LuceneAsDatabase.save(Volume.entries)
    
    //val c = new SHTTPClient
    //c.getBody("foo")
    
  }
}
