#odx - Officedrop Sync for Unix
##Install 

Require Oracle/Sun JVM 5 or 6

    curl -O http://www.hashcode.eti.br/downloads/odx/odx-base-1.1.jar
    alias odx="java -cp $PWD/odx-base-1.1.jar net.hashcode.fsw.Main"


##Getting started
After install odx go to a directory and so init current directory to be synced 

    odx init
  
Set your username, password and email 

    odx set auth.username username333
    odx set auth.password mypassword33
    odx set auth.email myemail33@1email.com
    
By default odx uses `https://www.officedropdev.com` if you want use other server set it by

    odx set remote.server https://yourserver.com
    
For existing user, you can just test authentication by 

    odx authenticate
    
For new ones 

    odx signup

#Synchronous mode

Just use command mount

    odx mount
    
It will listen by local and remote changes. You can see in `STDOUT` odx working

    # Pulling
    # [https://www.paperporttest.com] Checking revision 3604726230
    # Remote changes (3) revision 83ac03b1a324968df1c3773c9acd292d
    # [Removed] [8debb9154d21e0c5084487f7e1f96657] D /config
    # [Removed] [9391a263bf2832b69ea00baf3aa9ff91] F /config/logging.yml
    # [Removed] [b5ddcede67e134472d8f49d3352e340d] F /config/elasticsearch.yml
    # Current revision 83ac03b1a324968df1c3773c9acd292d
    # [Synced ] [6666cd76f96956469e7be39d750cc7d9] D /
    # [Synced ] [0e62afc91abe157ef67895cca0a7f912] D /logs
    # [Synced ] [2504fb4332d6678b6822121e81f42b65] F /logs/officedrop.log.2012-03-06
    # [Synced ] [62d21a7b6cc038c090a366c8db665369] F /logs/od.log.2012-02-01
    # [Synced ] [75577d72dcc5dcf35dd4a9125b26ec96] F /logs/officedrop_index_search_slowlog.log
    # [Synced ] [7ec5c9b823dcc729e53fbdcfeb8e400c] F /logs/officedrop.log
    # [Synced ] [80daff07321249a46b8899782767852c] F /logs/od.log
    # [Synced ] [a1db20233c10b95dfbdfdeca2bafd556] F /logs/od_index_search_slowlog.log
    # [Synced ] [af437ae5d2349610683d5102e82bd382] F /logs/officedrop.log.2012-03-02
    # [Synced ] [ce5e5a09469eb83d161461dac3946e4a] F /logs/od.log.2012-02-29
    # [https://www.paperporttest.com] Deleting [8debb9154d21e0c5084487f7e1f96657] D /config
    # Current revision 6f3c28b0531655e67b231ae037b458dc
 

Where
 
    # [Sync status or servername] [fkey or action] [D=Directory, F=File] <relative/server path>


#Asynchronous mode

###Getting new changes from remote server

    odx pull

###Pushing all changes to remote server

    odx push

###Push only changes from a directory

    odx push mydirectory

###Sync 

    odx sync
    
###Getting current status

    odx status

###Public Share

    odx share mydir/myfile.pdf

It will show the shared url, like this

     Shared link -> "https://www.paperport.com/ze/guest/public/1aee067f5508b51d2a98d00befc2c8c914ef6185"

###Searching (online) 

    odx search "invoice 2012"
    
It will list synced files matched with "invoice 2012"

     Searching for 'invoice 2012'
     [4141532071a4c6939488d6d5a60d7684] F /documents/2012/invoice 22 - 2012.pdf
     [8737e439620f57f64f3eed4684569992] F /documents/2012/invoice 23 - 2012.pdf
     

##help

    odx help
    
Print

    usage: odx <command>

        init                Create base configuration to sync current directory.
        set  <key> <value>  Set configuration. It need two arguments.
        signup              Sign up new user. I need auth.* values be set before signup
        authenticate        Authenticate.
        status              Get status of current directory.
        pull                Get current revision from server and apply it locally.
        push                Push all local changes to remote server.
        push  <dir>         Push changes from a specific directory to remote server.
        sync                Sync, pull and push command, it also cache current revision.
        mount               Start sync as daemon. It will listen remote and local change and sync the system when needed.


##Advanced user

You can edit setting on `.odx/config` 

    [auth]
    username = shairon
    password = toledo
    email = shairon@toledo.com

    [remote]
    folder = /foo
    server = https://www.officedropdev.com

    [log]
    tty = false

