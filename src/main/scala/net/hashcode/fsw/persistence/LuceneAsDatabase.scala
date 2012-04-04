package net.hashcode.fsw.persistence
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.Volume
import org.apache.lucene.analysis.KeywordAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.store._
import org.apache.lucene.util._
import java.io._
import org.apache.lucene.document.Field._
import org.apache.lucene.queryParser.QueryParser
import org.apache.log4j.Logger

object LuceneAsDatabase extends LocalDatabase{
  var IndexDir: String = _
  val REVISION_KEY="REVISION"
  val log = Logger.getLogger(classOf[LocalDatabase])

  override def retrive(fkey: String) = findBy("fkey", fkey)
  override def findChildren(parentPath: String, op:FileEntry => Unit) = {

    log.debug("Finding children of %s".format(parentPath))	
    withSearcher( is => for (scoredoc <- is.search(luceneQuery("parent_path",parentPath), 1000000).scoreDocs){
        val fe = asFileEntry(is.doc(scoredoc.doc))
        log.debug("Found child [%s] of  [%s]".format(fe,parentPath))
        op(fe)
      } 
    )
  }

  override def find(serverPath: String) = findBy("server_path", serverPath)
  override def findById(id: String) = findBy("id", id)

  def findBy(field:String, fieldValue:String): FileEntry = {
    log.debug("Finding by %s = %s".format(field, fieldValue))

    withSearcher( is => for (scoredoc <- is.search(luceneQuery(field,fieldValue), 10).scoreDocs if is.doc(scoredoc.doc).get(field) == fieldValue){
        val fe = asFileEntry(is.doc(scoredoc.doc))
        log.debug("Found [%s] by %s = %s".format(fe, field, fieldValue))
        return fe
      } 
    )
    return null
  }

  override def saveRevision(revision:String):String = {
    if (revision == null) return null
    withIndexer( idx => idx.deleteDocuments(luceneQuery("fkey",REVISION_KEY)))
    withIndexer( idx => idx.addDocument(revisionDocument(revision)) )

    return revision
  }

  override def loadRevision:String = {
    withSearcher( is => for (scoredoc <- is.search(luceneQuery("fkey",REVISION_KEY), 10).scoreDocs ){
        val rev = is.doc(scoredoc.doc).get("filename")
        log.debug("Found revision [%s]".format(rev))
        return rev
      } 
    )
    return null
  }

  override def remove(fileEntry: FileEntry) = withIndexer(removeDoc(_,fileEntry) )
  override def save(fileEntries: List[FileEntry]) = withIndexer( indexer => fileEntries.foreach( saveOrReplace(indexer, _)))

  override def save(fileEntry: FileEntry) = {
    //It need close indexer before save other entry
    withIndexer(removeDoc(_,fileEntry))
    withIndexer(saveOrReplace(_,fileEntry) )
  }

  def luceneQuery = new QueryParser(Version.LUCENE_29, (_:String), new KeywordAnalyzer()).parse( (_:String))

  def withSearcher(idx: IndexSearcher => Unit){
    val is = new IndexSearcher(LuceneAsDatabase.directory);
    try{
      idx(is)
    }finally{
      is.close
    }
  }

  def withIndexer(idx: IndexWriter => Unit){
    val dirExists = new File(IndexDir).exists
    val writer = new IndexWriter( directory, new KeywordAnalyzer(),!dirExists, IndexWriter.MaxFieldLength.LIMITED)
    synchronized{
      try{
        idx(writer)
      }
      finally{
        writer.commit
        writer.optimize
        writer.close
      }
    }
  }

  def saveOrReplace(indexer:IndexWriter, fe: FileEntry): Unit = {
    if (fe == null) return 
    log.debug("%s Saving ".format(fe.toStringDebug))
    removeDoc(_,_)
    indexer.addDocument(luceneDocument(fe))
    log.debug("%s Saved".format(fe.toStringDebug))
  }

  def removeDoc(indexer:IndexWriter, fe: FileEntry): Unit = {
    if (fe == null) return 
    log.debug("Deleting"+ fe)
    indexer.deleteDocuments(luceneQuery("fkey",fe.fkey))
    indexer.expungeDeletes(true)
  }

  def directory = FSDirectory.open(new File(IndexDir))

  def luceneDocument(fe: FileEntry): Document = {
    val doc = new Document
    doc.add(new Field("id", fe.id+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

    doc.add(new Field("server_path", fe.serverPath, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    if (fe.parentPath != null)
      doc.add(new Field("parent_path", fe.parentPath, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("filename", fe.filename, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    if (fe.csum != null)
      doc.add(new Field("csum", fe.csum+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("local_modified_time", fe.localModifiedTime + "", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("is_directory", fe.isDirectory+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("fkey", fe.fkey, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("size", fe.size+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("absolutePath", fe.filepath+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

    doc
  }

  def	revisionDocument(revision:String) = {
    val doc = new Document

    doc.add(new Field("fkey", REVISION_KEY, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("filename", revision, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc
  }

  def	fetchAll(op:FileEntry => Unit) {

    withSearcher(
      searcher =>{
        log.debug("max docs: "+searcher.maxDoc)
        for (i<- 0 to searcher.maxDoc-1){
          val doc = searcher.doc(i)
          if ( doc != null && doc.get("fkey") != REVISION_KEY){
            val fe = asFileEntry(doc)
            log.debug(fe+" - Found")
            op(fe)
          }
        }
      }
    )
  }

  def asFileEntry(doc: Document): FileEntry ={
    val fe = new FileEntry
    fe.id = doc.get("id").toLong
    fe.serverPath = doc.get("server_path")
    fe.parentPath = doc.get("parent_path")
    fe.filename = doc.get("filename")
    fe.csum = doc.get("csum")
    fe.localModifiedTime = doc.get("local_modified_time").toLong
    fe.isDirectory = doc.get("is_directory").toBoolean
    fe.fkey = doc.get("fkey")
    fe.size = doc.get("size").toLong
    fe.file = new File(doc.get("absolutePath"))
    fe.filepath = doc.get("absolutePath")
    fe

  }

  def init = { 
    IndexDir = new File(Volume.configDir, "db").getAbsolutePath 
    withIndexer{ indexer => null}
  }



}

