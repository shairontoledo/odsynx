
package net.hashcode.fsw

import java.io.File
import net.hashcode.fsw.io.Command
import org.apache.commons.io.FileUtils
import org.ini4j.Wini
import scala.collection.mutable.ListBuffer

object ConfigManager {
		var configuration:Config = _
		
		def	store:Unit = store(configuration)
		
		def	store(config:Config) = {
				val confFile = new File(config.dir, "config")
				FileUtils.touch(confFile)
				val ini = new Wini(confFile);
				if (confFile.exists){
						ini.clear
						ini.put("auth", "username", config.username);
						ini.put("auth", "password", config.password);
						ini.put("auth", "email", config.email);
						ini.put("remote", "folder", config.remoteFolder);
						ini.put("remote", "server", config.server);
						ini.put("log", "tty", config.logtty);
				} 
				ini.store();
		}
		def set(rawkey:String, value:String) = {
				val compokey = rawkey.toLowerCase.trim.split("\\.")
				def errorMsg = Command.abort("Unknown property: %s = %s".format(rawkey,value), 3)
				if (compokey.size != 2) errorMsg
				compokey(0) match {
						case "auth" => { 
										compokey(1) match{
												case "username" => configuration.username = value
												case "password" => configuration.password = value
												case "email" => configuration.email = value
												case _ => errorMsg
										}
								}
						case "remote"  => compokey(1) match{
										case "folder" => configuration.remoteFolder = value
										case "server" => configuration.server = value
										case _ => errorMsg		
								}
								
						case "log" if compokey(1) == "tty" => configuration.logtty = value.toBoolean
						case _ => errorMsg
				}
				store
		}
		
		//def	load(path:String):Config = load(new Config(path))
		
		def	load = {
				
				val ini = new Wini(configuration.configFile);
				configuration.username = ini.get("auth","username")
				configuration.password = ini.get("auth","password")
				configuration.email = ini.get("auth","email")
				configuration.server = ini.get("remote","server")
				if (configuration.server == null || configuration.server == "")
						configuration.server = "https://officedrop.com"
				configuration.remoteFolder = ini.get("remote","folder")
				if (configuration.remoteFolder == null || configuration.remoteFolder == ""){
						configuration.remoteFolder = "/"+configuration.dir.getAbsoluteFile.getParentFile.getName
				}else{
						if (!configuration.remoteFolder.startsWith("/")){
								configuration.remoteFolder = "/"+configuration.remoteFolder
						}
				
						if (ini.get("log","tty") != null && ini.get("log","tty") == "true") configuration.logtty = true
				}
				
		}
		
		
		def	initialize(path:String, force:Boolean = false):Config = {
				configuration = new Config(path, force)
				load
				store
				configuration
		}
		
}

class Config(path:String, force:Boolean = false ) {
		
		val mountPoint = path
		val dir  = new File(path,".odx")
		if (force){
				dir.mkdirs
				new File(dir,"tmp").mkdirs
				FileUtils.touch(configFile)
		} 
		if (!dir.exists) Command.abort("Not a odx directory. Try 'odx init' to initialize it", 1)
		val absolutePath = dir.getAbsolutePath
	 var username:String = _
		var password:String = _
		var email:String = _
		val ignoredFiles = ListBuffer()
		var logtty = false
		var remoteFolder:String = _ 
		var server:String = _ 
		
		def	hasAuthenticationValues = (username != null && username != "") && (password != null &&  password != "") && (email != null && email != "")
		
		def	validate = {
				if (!hasAuthenticationValues) Command.abort("Invalid email, username or password, Usage set auth.<attribute> <value> to continue", 4)
				if (server == null || server == "" ) Command.abort("Server should be set. Usage: odx set remote.server http://server:port ", 4)
		}
		
		def	hasConfigDir = dir.exists
		
		def	configFile = new File(dir,"config")
		
}
