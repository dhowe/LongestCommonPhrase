package lcp;

import rita.RiTa;

public class GoogleLookup implements Corpus {

	private GoogleSearch search;
	private String reference;
	private int count;
	private String[] excludes;
	private boolean useExcludes = true;
	
	public GoogleLookup(String[] excludes) {

		this.excludes = excludes;
		this.search = new GoogleSearch();
	}
	
  public boolean contains(String[] check, String[] queryLocalExcludes)
  {
    //System.out.println("GoogleCorpus.contains("+RiTa.asList(check)+" && "+RiTa.asList(excludeWords)+")");
    
    String[] allExcludes = null;

    if (useExcludes) {
      
      // combine global and local excludes
      if (queryLocalExcludes == null)
      {
        allExcludes = this.excludes;
      } 
      else
      {
        String s = formatQuotedPhrase(queryLocalExcludes);
        allExcludes = new String[excludes.length + 1];
        for (int i = 0; i < excludes.length; i++)
          allExcludes[i] = excludes[i];
        allExcludes[excludes.length] = s;
      }
    }
    
    return _contains(check, allExcludes);
  }
/*
  private boolean parseForIgnores(String[] check, String html)
  {
    System.out.print("(" + check.length + ") " + RiTa.join(check) + " -> " + count + "\n");

    if (count > 0)
    {

      // NOTE: ignore these, assuming they have the full text
      List matches = this.parseRefs(html, RE1);

      if (matches.size() > 0)
      {

        warn("[WARN] Matched on " + LCP.MAX_QUERY_WORDS + " word phrase: " + RiTa.join(check) + "\n       Adding ignores: " + matches + "\n");

        ignoreAll(matches);
      }

    }
    return false; // always
  }
  */
  
  private void printInfo(String[] check, String ref)
  {
    String pref = ref != null ? "[" + ref + "]" : "";
    System.out.print("(" + check.length + ") " + RiTa.join(check) + " -> " + count + " " + pref + "\n");
    if (count > 0 && (ref != null || !LongestCommonPhrase.PARSE_REFS))
      System.out.println("---------------------------------------------");
  }
  
  private boolean _contains(String[] check, String[] exclude)
  {
    String query = this.formatQuery(check, exclude);

    //System.out.println("[QUERY] " + query);

    String ref = null;//, html = google.fetch(query);
    this.count = search.count(query);
    
    //System.out.println("[COUNT] "+count);
    
    if (false && this.count > 0) // TODO: parse ref if count > 0 && <  MAX_QUERY_WORDS
    { 
        // ok, found some 
    	if (count > LongestCommonPhrase.MAX_QUERY_WORDS) {
    		
    		// parseForIgnores
    	}
    	//if (count > LeastCommonPhrase.MAX_QUERY_WORDS) 
    		//return true; // tmp: should be false
    	
    	/*
        {   
          // for ignore list
          return parseForIgnores(check, html); 
        }
          
        if (LeastCommonPhrase.PARSE_REFS) {
          
          // try using CITE-pattern
          ref = parseCiteRefs(html);
    
          // try again using HREF-pattern
          if (ref == null)
            ref = parseHrefs(html);
         
      }*/
    }

    printInfo(check, ref);

    // this shouldn't be needed
//    if (ref != null && ignoreUrls.contains(ref)) {
//      throw new RuntimeException("Illegal state! About to add URL=" + ref  
//          + "\n       on ignore list: " + RiTa.asList(ignoreUrls));
    //}

    //return (this.reference = ref) != null;
    return this.count > 0; // assumes we will have a ref if parsing-refs
  }

  private String formatQuery(String[] phrase, String[] exclude)
  {
    // JC: method extracted for use elsewhere
    String s = formatQuotedPhrase(phrase);

    if (exclude != null)
    {
      for (int i = 0; i < exclude.length; i++)
        s += " -" + exclude[i];
    }

    return s.trim();// + "&tbs=li:1"; // TODO: JC added the param that sets Google's Verbatim
  }
  
  private String formatQuotedPhrase(String[] phrase)
  {
    String s = "\"";
    
    for (int i = 0; i < phrase.length; i++)
    {
      // strip leading paragraph tag from query
      s += (phrase[i].startsWith("<p>") ? phrase[i].substring(3) : phrase[i]) + " ";
    }
    
    return s.trim() + "\"";
  }
  
	@Override
	public String getReference() {

		return this.reference;
	}

	@Override
	public int getCount() {

		return count;
	}

	@Override
	public String[] excludes() {
		
		return excludes;
	}

	@Override
	public Corpus excludes(String[] b) {

		excludes = b;
		return this;
	}

	@Override
	public void useExcludes(boolean b) {

		this.useExcludes = b;
	}

}
