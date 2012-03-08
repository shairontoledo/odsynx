
package net.hashcode.fsw
import net.hashcode.fsw.persistence._
import java.io.File
import java.net.URI
import net.hashcode.fsw.io.Command
import net.hashcode.fsw.models.Volume
import net.hashcode.fsw.models._
import org.codehaus.jackson.node.ArrayNode
import net.hashcode.fsw.transport.SHTTPTransport
import net.liftweb.json.JsonAST.JArray
import org.apache.log4j.Logger
import org.codehaus.jackson.node.ObjectNode
import net.hashcode.fsw.transport._


object Main {
		
		
		
		def	currentDir = FileEntry.fixedPath(new File("").getAbsoluteFile.getAbsolutePath)
  def main(args: Array[String]):Unit = {
    
//    val log = Logger.getLogger(Main.getClass)
//    val mountPoint = "/tmp/docs"    
//    val sht = new SHTTPTransport
//				println( sht.download(
//								FileEntry("/private/tmp/docs/sync_api.markdown.html","/private/tmp" ), 
//								new File("/tmp/docs/newfile.html")
//						)
						
				val cmd = Command(args) 
						
				cmd match{
						case Command.Authenticate() => authenticate
						case Command.SignUp() => signup
						case Command.Pull() => pull
						case Command.Push(file) => push(file)
						case Command.Status() => status
						case Command.Sync() => sync
						case Command.Mount() => println("it is: "+cmd)
						case Command.Init() => init
						case Command.Set(key,value) => set(key,value)
				}
  }
		
		def	init = {
				ConfigManager.initialize(currentDir, true)
		}
		def boot = ConfigManager.initialize(currentDir).validate
		def	initUserEnvironment = {
				boot
				authenticate
				Volume.initWithConfig(ConfigManager.configuration, false)
		}
		def pull = {
				initUserEnvironment
				Volume.pull
		}
		
		def	set(key:String,value:String) = {
				ConfigManager.initialize(currentDir)
				ConfigManager.set(key,value)
		}
		
		def	push(file:String) = {
				initUserEnvironment
				val fileToSync = if (file == null) new File("").getAbsoluteFile else new File(file)
				Volume.push(fileToSync)
		}
		
		def	status = {
				initUserEnvironment
				//val fileToSync = if (file == null) new File(".").getAbsoluteFile.getParentFile else new File(file)
				Volume.printStatus(null)
		}
		
		
//		def	status = {
//				boot
//				authenticate
//				val volume = Volume.initWithConfig(ConfigManager.configuration, false)
//				//val fileToSync = if (file == null) new File(".").getAbsoluteFile.getParentFile else new File(file)
//				Volume.printStatus
//		}
		
		def	sync = {
				initUserEnvironment
				
				Volume.pull
				Volume.push(new File("").getAbsoluteFile)
		}
		
		def	authenticate = {
				boot
				CurrentTransport.setCredentials(new URI(ConfigManager.configuration.server), 
																																				ConfigManager.configuration.username, 
																																				ConfigManager.configuration.password, 
																																				ConfigManager.configuration.email)
				val res = CurrentTransport.authenticate
				if (!res._1){
						println(res._2)
						Command.abort("Use command 'set' to fix issues or use 'signup' for new user", 4)
				}
		}
		
		def	signup = {
				boot
				CurrentTransport.setCredentials(new URI(ConfigManager.configuration.server), 
																																				ConfigManager.configuration.username, 
																																				ConfigManager.configuration.password, 
																																				ConfigManager.configuration.email)
				val res = CurrentTransport.signup
				if (!res._1){
						val errors = res._2.asInstanceOf[ArrayNode]
					
						for (i <- 0 to errors.size-1){
								println("Error for auth.%s : %s".format(errors.get(i).get(0), errors.get(i).get(1)))
						}
						Command.abort("Use command 'set' to fix issues", 4)
				}
				
				
		}
		
}
