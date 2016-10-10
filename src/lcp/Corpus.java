package lcp;


interface Corpus
{
  boolean contains(String[] check, String[] queryLocalExcludes);

  String getReference(); // returns the ref for the last call to contains()
  
  int getCount(); // returns the hit count for the last call to contains()
  
  String[] excludes();
  
  Corpus excludes(String[] b);

	void useExcludes(boolean b);
}