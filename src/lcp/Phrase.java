package lcp;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Phrase
{
  long timestamp;
  int count, wordIdx;
  String words[], url, query, sourceText;

  public Phrase()
  {
    this.wordIdx = -1;
  } // for xml-deserialization only

  public Phrase(String sourceText, int wordIdx, String[] words, String reference)
  {
    this(sourceText, wordIdx, words, reference, 1);
  }

  public Phrase(String sourceText, int wordIdx, String[] words, String reference, int count)
  {
    this.timestamp = System.currentTimeMillis();
    this.sourceText = sourceText;
    this.wordIdx = wordIdx;
    this.url = reference;
    this.words = words;
    this.count = count;
  }

  public int getWordIdx()
  {
    return wordIdx;
  }

  public void setWordIdx(int wordIdx)
  {
    this.wordIdx = wordIdx;
  }

  public String[] getWords()
  {
    return words;
  }

  public String getPhrase()
  {
    String phrase = "";
    for (int i = 0; i < words.length; i++)
      phrase += words[i] + " ";
    return phrase.trim();
  }

  public void setWords(String[] words)
  {
    this.words = words;
  }

  public String getUrl()
  {
    /*
     * if (url != null) { try { return URLDecoder.decode(url, "UTF8"); } catch (UnsupportedEncodingException e) { System.err.println(e.getMessage()); } }
     */
    return url;
  }

  public String getDecodedUrl()
  {
    if (url != null)
    {
      try
      {
        return URLDecoder.decode(url, "UTF8");
      }
      catch (UnsupportedEncodingException e)
      {
        System.err.println(e.getMessage());
      }
    }
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

  public long getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(long timestamp)
  {
    this.timestamp = timestamp;
  }

  public int getCount()
  {
    return count;
  }

  public void setCount(int count)
  {
    this.count = count;
  }

  public String toString()
  {
    return getPhrase() + "[" + getUrl() + "]";
  }

  /**
   * This method saves (serializes) the object into an XML string
   */
  public String toXml()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    XMLEncoder xmlEncoder = new XMLEncoder(baos);
    xmlEncoder.writeObject(this);
    xmlEncoder.close();
    return baos.toString();
  }

  public static String arrayToXml(Phrase[] phrases)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    XMLEncoder xmlEncoder = new XMLEncoder(baos);
    xmlEncoder.writeObject(phrases);
    xmlEncoder.close();
    return baos.toString();
  }

  public void setSourceText(String sourceText)
  {
    this.sourceText = sourceText;
  }

  public String getSourceText()
  {
    return sourceText;
  }

  public static void main(String[] args)
  {
    Phrase p = new Phrase("CommonTongues", 0, "the tongue gets clogged".split(" "), "waxinggrasshopper.blogspot.com/2009_12_01_archive.html", 43);
    Phrase p2 = new Phrase("CommonTongues", 1, "2the tongue gets clogged".split(" "), "www.callmeish.com/search%3Fquery%3DHow%2Bit%2BIs", 243);
    System.out.println(Phrase.arrayToXml(new Phrase[] { p, p2 }));
  }
}