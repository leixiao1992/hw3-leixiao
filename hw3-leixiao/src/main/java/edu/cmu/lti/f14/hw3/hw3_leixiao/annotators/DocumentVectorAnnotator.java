package edu.cmu.lti.f14.hw3.hw3_leixiao.annotators;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_leixiao.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_leixiao.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_leixiao.utils.Utils;


public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
	
	public ArrayList<String> wordList;
	
	 public static int calculate(String input,String target) {
		  int count = 0;
		  StringTokenizer tokenizer = new StringTokenizer(input);
		  while (tokenizer.hasMoreElements()) {
		   String element = (String) tokenizer.nextElement();
		   
		   /**ignore the case to compare*/
		   
		   if (target.equalsIgnoreCase(element))
		    count++;
		  }
		  return count;
		 }

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		//TO DO: construct a vector of tokens and update the tokenList in CAS
		
		
		ArrayList<Token>tokenList=new ArrayList<Token>();
		
		wordList = new ArrayList<String>();
				
		String doc1=docText.replace(",","");
		String doc2=doc1.replace(".","");
		
		/**change all the word to lower case to store in the disk*/
		
		String doc3=doc2.toLowerCase();
		String ss[]=new String[20];
		ss=doc3.split(" ");
		int i;
						
		for(i=0;i<ss.length;i++){
			
			/**delete the same token*/
			
			if(wordList.indexOf(ss[i])!=-1){
								
				continue;
			}
			
			wordList.add(ss[i]);
			Token name = new Token(jcas);
			name.setText(ss[i]);
			int count=calculate(doc3,ss[i]);
			name.setFrequency(count);
			
			tokenList.add(name);			
			name.addToIndexes();
			//System.out.println(ss[i]+count);
			
		}
		FSList fsTokenList=Utils.fromCollectionToFSList(jcas, tokenList);
		doc.setTokenList(fsTokenList);		
		//System.out.println(docText);
		
	}

}
