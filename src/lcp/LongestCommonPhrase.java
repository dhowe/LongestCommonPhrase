package lcp;

import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.JFrame;

import rita.RiTa;

public class LongestCommonPhrase
{
	static String TEXT = "theimage.txt";
  static int MAX_QUERY_WORDS = 8, NUM_EXCLUDE_WORDS = 5;
  static boolean ADD_FRAME = false, PARSE_REFS = false, USE_COOKIE = false;
  static boolean WRITE_XML = false, SILENT = false, IGNORE_GOOGLE_BOOKS = true;
  static boolean SHOW_DELAYS = false, USE_DELAYS = true, USE_EXCLUDE_WORDS = false;

  public static String HOME_DIR = System.getProperty("user.home");
  static String COOKIE_PATH = HOME_DIR + "/Library/Cookies/Cookies.plist";
  static long PER_PHRASE_MIN_DELAY = 1000, PER_PHRASE_MAX_DELAY = 6000;
  static String XML_OUTPUT_DIR = HOME_DIR + "/Desktop/LCP";
  
  static String[] EXCLUDES = { 
    "beckett", "BeckettTranslated", "site:twitter.com", "site:tumblr.com", 
    "site:callmeish.com", "site:askmachine.net", "site:epinions.com", 
    "site:www.yasni.ru", "site:rudocs.exdat.com", "site:samaraaltlinguo.narod.ru" 
  };
  
  protected long perQueryMinDelay = 30, perQueryMaxDelay = 500;
  protected int startIdx, tries, cursor;
  protected List<Phrase> phrases;
  protected String[] words;
  protected Corpus corpus;

  public LongestCommonPhrase(String[] words)
  {
    this.corpus = new GoogleLookup(EXCLUDES);
    this.words = words;
    this.phrases = new ArrayList<Phrase>();
  }

  public List<Phrase> compute()
  {
    return this.compute(0);
  }

  public List<Phrase> compute(int startWordIdx)
  {
    return this.compute(startWordIdx, Integer.MAX_VALUE);
  }

  /**
   * This method does the work, calculating the phrases and storing the references
   * 
   * @param startWordIdx
   *          the word to start on
   * @param maxNumPhrases
   *          the maximum number of phrases to collect
   * @return List<Phrase> the computed LCP phrase objects
   */
  public List<Phrase> compute(int startWordIdx, int maxNumPhrases)
  {
    if (!SILENT && startWordIdx > 0)
      System.out.println("[INFO] LCP starting at wordIdx=" + startWordIdx + "/"+words.length+"\n");

    this.cursor = this.startIdx = startWordIdx;

    for (int i = 0; i < maxNumPhrases; i++)
    {
      if (i < words.length)
      {
        if (!this.leastCommonPhrase(words))
          break;
        
        if (USE_DELAYS)
          pauseBetweenRequests();
      }
      else
      {
        printResults();
        writeXmlFile();
        System.exit(0);
      }
    }

    return phrases;
  }

  public void pauseBetweenRequests()
  {
    try
    {
      int delay = (int) (PER_PHRASE_MIN_DELAY + (Math.random() * (PER_PHRASE_MAX_DELAY - PER_PHRASE_MIN_DELAY)));

      if (SHOW_DELAYS)
        System.out.println("[INFO] Sleeping for " + delay + "ms");

      Thread.sleep(delay);
    }
    catch (InterruptedException e) {}
  }

  public boolean leastCommonPhrase(String[] words)
  {
    boolean relaxExcludes = false;
    int checkIdx = MAX_QUERY_WORDS - 1;
    int excludeCursor = cursor + MAX_QUERY_WORDS;
    int excludeIdx = NUM_EXCLUDE_WORDS - 1;

    String[] check = null;
    String[] excludeWords = null;
    while (++tries < 1000)
    {
      check = subArray(words, cursor, cursor + checkIdx);

      if (check == null) return false; // we're done
      
      // max-length with no refs is not allowed
      if (!LongestCommonPhrase.PARSE_REFS && check.length == MAX_QUERY_WORDS) { 
        --checkIdx;
        continue;
      }
 
      if ((USE_EXCLUDE_WORDS && !relaxExcludes) && (check.length != MAX_QUERY_WORDS))
      {
        excludeWords = subArray(words, excludeCursor, excludeCursor + excludeIdx);
      }

      boolean gotCount = false;
      String reference = null;
      try
      { 
        // excludeWords is null if not USE_EXCLUDE_WORDS
        if (corpus.contains(check, excludeWords)) {
          
          gotCount = true;
          
          // start using excludes again after a hit
          relaxExcludes = false; 
          corpus.useExcludes(true);
          
          reference = PARSE_REFS ? corpus.getReference() : null;
        }

        int delay = (int) (perQueryMinDelay + (Math.random() * (perQueryMaxDelay - perQueryMinDelay)));

        if (SHOW_DELAYS) System.out.println("[INFO] Sleeping for " + delay + "ms");

        Thread.sleep(delay);
      }
      catch (Throwable e)
      {
      	e.printStackTrace();
        //handleError(e);
      }

      if (gotCount)
      {
        this.phrases.add(new Phrase(TEXT, cursor, check, reference, corpus.getCount()));
        //System.out.println(reference);
        this.cursor = (cursor + checkIdx + 1);
        break;
      }

      if (--checkIdx < 0) // checked everything and no hit
      {
        // Relax constraints here, ignore excludes 
        
        String[] missed = subArray(words, cursor, cursor + MAX_QUERY_WORDS);
        String msg = "No match for index="+ cursor + " -> "+ Arrays.asList(missed);
        
        if (!relaxExcludes) {
          
          System.err.println("[WARN] "+msg+"\n  Ignoring excludes and trying again...");
          checkIdx = MAX_QUERY_WORDS - 1;
          
          relaxExcludes = true;
          corpus.useExcludes(false);
          
          continue;
        }
        
        // only throw if we've already relaxed exclude-constraint
        handleError(new RuntimeException(msg));
      }
    }
    return true;
  }

  public void handleError(Throwable e)
  {
    if (phrases.size() > 0)
    {
      printResults(); // print and exit
      writeXmlFile();
    }
    else
      System.err.println("\n[WARN] No results\n");

    if (e != null) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void writeXmlFile()
  {
  	if (!WRITE_XML) return;
    File f = new File(XML_OUTPUT_DIR);
    if (!f.exists())
      f.mkdirs();
    this.writeXmlFile(new File(f, TEXT.replace(".txt", "") + "." + startIdx + 
        "-" + Math.min(words.length - 1, cursor - 1) + "." + System.currentTimeMillis() + ".xml"));
  }

  public void writeXmlFile(File f)
  {
    Phrase[] p = phrases.toArray(new Phrase[phrases.size()]);
    String xml = Phrase.arrayToXml(p);
    FileWriter fw = null;
    try
    {
      fw = new FileWriter(f);
      fw.write(xml);
      fw.flush();
      System.out.println("\nXML written to: " + f);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        fw.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void printResults()
  {
    System.out.println(phrases.size() + " phrases completed");
    //System.out.println(((GoogleCorpus) corpus).google.getCallCount() + " http requests sent");
    System.out.println("Avg-phrase-length=" + averageLength());

//    if (corpus instanceof GoogleCorpus)
//    {
//
//      Set<String> iUrls = ((GoogleCorpus) corpus).getIgnoreUrls();
//      System.out.println("Url-ignore-list(" + iUrls.size() + ")=" + iUrls + "\n");
//    }

    for (Iterator it = phrases.iterator(); it.hasNext();)
    {
      Phrase p = (Phrase) it.next();
      System.out.println(p.getPhrase() + " [" + p.getUrl() + "]");
    }
  }

  public float averageLength()
  {
    float sum = 0;
    for (Iterator it = phrases.iterator(); it.hasNext();)
    {
      Phrase p = (Phrase) it.next();
      sum += p.words.length;
    }

    return sum / (float) phrases.size();
  }

  // Util /////////////////////////////////////////////////////////

  /**
   * Returns the specified sub-array or null if end > arr.length
   */
  static String[] subArray(String[] arr, int start, int end)
  {
    int size = Math.max(0, Math.min(end, arr.length - 1) - start + 1);
    String[] newarr = new String[size];

    for (int i = 0; i < newarr.length; i++)
      newarr[i] = arr[start + i];

    return newarr.length > 0 ? newarr : null;
  }

  static String[] getWords(String file)
  {
    StringBuffer sb = new StringBuffer();
    String[] lines = RiTa.loadStrings(file);

    for (int i = 0; i < lines.length; i++)
      sb.append(lines[i] + ' ');

    return sb.toString().split(" ");
  }

  static String getDomain(String urlAddress)
  {
    try
    {
      if (!urlAddress.matches("https?://.*"))
        urlAddress = "http://" + urlAddress;
      return new URL(urlAddress).getHost();
    }
    catch (Exception e)
    {
      System.err.println("getDomain() -> " + e.getMessage());
      return urlAddress;
    }
  }

  public static void addClosableFrame(final LongestCommonPhrase lcp)
  {
    JFrame jf = new JFrame();
    jf.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        lcp.handleError(null);
        System.exit(1);
      }
    });
    jf.pack();
    jf.setVisible(true);
  }
  
  // ////////////////////////////////////////////////////////////////

  public static void main(String[] args)
  {
    ADD_FRAME = true;
    PARSE_REFS = false;
    USE_EXCLUDE_WORDS = true;
    EXCLUDES = new String[] { "beckett" };
    XML_OUTPUT_DIR = HOME_DIR + "~/Desktop/lcp_image_xml";
    
    String[] words = getWords(TEXT);
    final LongestCommonPhrase lcp = new LongestCommonPhrase(words);
    if (ADD_FRAME) addClosableFrame(lcp);
    int startIdx = 0;//(int) (Math.random() * 1000);
    if (lcp.compute(startIdx, words.length).size() > 0)
    {
      lcp.printResults();
      lcp.writeXmlFile();
    }
  }

}// end
