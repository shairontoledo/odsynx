package net.hashcode.fsw;

import java.io.File;
import java.io.IOException;
import net.contentobjects.jnotify.JNotifyListener;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Hello world!
 *
 */
public class App {

  public static void main(String[] args) {

    //String path = System.getProperty("user.home");
    String path = "/Users/shairon/Projects/Learning/FSW/src/test/resources/doc";
    System.out.println("Loading");
    // watch mask, specify events you care about,
    // or JNotify.FILE_ANY for all events.
//      int mask = JNotify.FILE_CREATED
//              | JNotify.FILE_DELETED
//              | JNotify.FILE_MODIFIED
//              | JNotify.FILE_RENAMED;

    // watch subtree?
    boolean watchSubtree = true;
    for (File f : new File("/Users/shairon/Desktop").listFiles()) {
      System.out.println("f -> " + f.getAbsolutePath());
      if (!f.isHidden()) {
        App.store(f);
        //break;
      }
    }
    // add actual watch
    //int watchID = JNotify.addWatch(path, JNotify.FILE_ANY, watchSubtree, new Listener());

    // sleep a little, the application will exit if you
    // don't (watching is asynchronous), depending on your
    // application, this may not be required
    //Thread.sleep(1000000);

    try {
      int count = new File("/tmp/idx").listFiles().length;
      System.out.println("Files " + count);
      if (count > 0) {
        Searcher is = new IndexSearcher(FSDirectory.open(new File("/tmp/idx")));
        
        Query q = new QueryParser(Version.LUCENE_29, "name", new SimpleAnalyzer()).parse("root file.html");
                
        TopDocs td = is.search(q, 10);
        ScoreDoc[] sd = td.scoreDocs;
        for (int i = 0; i < sd.length; i++) {
          int docid = sd[i].doc;
          Document d = is.doc(docid);
          System.out.println("name******-> "+d.get("name"));
          
        }
       
        System.out.println("--> " + is.maxDoc());
      } else {
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

//      IndexReader reader = IndexReader.open( FSDirectory.open(new File("/tmp/idx") ));

      


    // to remove watch the watch
    //boolean res = JNotify.removeWatch(watchID);



  }
  static String INDEXDIR = "/tmp/idx";

  public static void store(File file) {
    try {
      IndexWriter writer;
      try {
       writer = new IndexWriter(
              FSDirectory.open(new File(INDEXDIR)),
              new StandardAnalyzer(Version.LUCENE_CURRENT),
              false,
              IndexWriter.MaxFieldLength.LIMITED);
      
      } catch (java.io.FileNotFoundException e) {
        writer = new IndexWriter(
              FSDirectory.open(new File(INDEXDIR)),
              new StandardAnalyzer(Version.LUCENE_CURRENT),
              true,
              IndexWriter.MaxFieldLength.LIMITED);
      
      }
      //writer.setMergeFactor(100);
      //writer.setMaxMergeDocs(50000);

      Document doc = new Document();
      //Field fid = new Field("id", , Store.YES, Field.Index.NOT_ANALYZED);
      //Field fid = new Field("id", , Store.YES, Field.Index.NOT_ANALYZED);
      //NumericField fid = new NumericField(id, Store.YES, true);

      //doc.add(fid);
      doc.add(new Field("id", file.getAbsolutePath(), Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("parent_path", file.getParent(), Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("name", file.getName(), Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("checksum", "chk", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("local_modified_time", file.lastModified() + "", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("is_directory", file.isDirectory() + "", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("fkey", "4065f5fa7175ecf30fb9cf0cdcef5405", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("size", file.length() + "", Store.YES, Field.Index.NOT_ANALYZED));

      writer.addDocument(doc);

      writer.commit();
      //writer.optimize();
      writer.close(true);
      System.out.println("done");
    } catch (IOException iOException) {
      iOException.printStackTrace();
    }

  }
}

class Listener implements JNotifyListener {

  public void fileRenamed(int wd, String rootPath, String oldName,
          String newName) {
    print("renamed " + rootPath + " : " + oldName + " -> " + newName);
  }

  public void fileModified(int wd, String rootPath, String name) {
    print("modified " + rootPath + " : " + name);
  }

  public void fileDeleted(int wd, String rootPath, String name) {
    print("deleted " + rootPath + " : " + name);
  }

  public void fileCreated(int wd, String rootPath, String name) {
    print("created " + rootPath + " : " + name);
    store(rootPath, name);
  }

  void print(String msg) {
    System.err.println(msg);
  }
  static String INDEXDIR = "/tmp/idx";

  public void store(String rootPath, String name) {
    try {
      IndexWriter writer = new IndexWriter(
              FSDirectory.open(new File(INDEXDIR)),
              new StandardAnalyzer(Version.LUCENE_CURRENT),
              true,
              IndexWriter.MaxFieldLength.LIMITED);
      //writer.setMergeFactor(100);
      //writer.setMaxMergeDocs(50000);

      Document doc = new Document();
      String id = String.format("%s/%s", rootPath, name).toLowerCase();
      File file = new File(id);
      //Field fid = new Field("id", , Store.YES, Field.Index.NOT_ANALYZED);
      //Field fid = new Field("id", , Store.YES, Field.Index.NOT_ANALYZED);
      NumericField fid = new NumericField(id, Store.YES, true);

      doc.add(fid);
      doc.add(new Field("parent_path", rootPath, Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
      doc.add(new Field("name", name, Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("checksum", "chk", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("local_modified_time", file.lastModified() + "", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("is_directory", file.isDirectory() + "", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("fkey", "4065f5fa7175ecf30fb9cf0cdcef5405", Store.YES, Field.Index.NOT_ANALYZED));
      doc.add(new Field("size", file.length() + "", Store.YES, Field.Index.NOT_ANALYZED));
      
      //writer.addDocument(doc);
      

      writer.commit();
      //writer.optimize();
      writer.close(true);
      System.out.println("done");
    } catch (IOException iOException) {
      iOException.printStackTrace();
    }

  }
}