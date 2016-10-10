package lcp;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class GoogleSearch {

	private static final Pattern COUNT_PAT = Pattern.compile(" ?([0-9,]+) ");
	
	static int timeout = 10 * 1000;
	static String divId = "resultStats";
	static String gs = "https://www.google.com.hk/search?q=%QUERY%&tbs=li:1";
	static String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	public static void main(String[] args) {

		GoogleSearch googleSearch = new GoogleSearch();
		int count = googleSearch.count("\"how it is XYZeda1234\"");
		System.out.println("FOUND "+count);
		count = googleSearch.count("\"how it is\"");
		System.out.println("FOUND "+count);
	}
	
  private static int regexCheck(Matcher m)
  {
    int result = 0;
    if (m.find()) {
      
      String countOld = m.group(1);
      StringBuilder countNew = new StringBuilder();
      for (int i = 0; i < countOld.length(); i++) {
        char c = countOld.charAt(i);
        if (c >= '0' && c <= '9')
          countNew.append(c);
      }
      
      long l = Long.parseLong(countNew.toString());
      result = (l > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)l;
    }
    
    return result;
  }

  static String[] NO_RESULTS = {
  	"did not match any documents",
  	"No results found for ",
  	"沒有任何文件符合您的搜尋"
  };
 
	public int count(String query) {

		int result = -1;
		Document doc = fetch(query);
		//System.out.println(doc.text());
		String page = doc.text();
		for (int i = 0; i < NO_RESULTS.length; i++) {
			if (page.contains(NO_RESULTS[i])) {
				//System.out.println("NOTHING: "+NO_RESULTS[i]);
				return 0;
			}
		}

		Element ele = doc.getElementById(divId);		
		if (ele == null) {
			throw new RuntimeException("select failed1");
		}
		
		String text = ele.text();
		if (text == null) 
			throw new RuntimeException("select failed2");

		result = regexCheck(COUNT_PAT.matcher(text));
		
		if (result > 100000)
			System.out.println("[WARN] result= "+result+" query="+query);
		 //System.out.println(page);
		
		return result;
	}

	private static Document fetch(String q) {

		try {
			return Jsoup.connect(makeQuery(q)).userAgent(ua).timeout(timeout).get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String makeQuery(String q) {

		String query = gs.replaceAll("%QUERY%", q);
    query = query.replaceAll("\"", "%22").replaceAll(" ", "+");
    query = query.replaceAll("%2B", "+");
    //System.out.println("Q: " + query);
    return query;
	}

	public String getReference() {

		return "Implement me";
	}

}
