package edu.cmu.lti.f14.hw3.hw3_leixiao.casconsumers;

import java.io.IOException;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_leixiao.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_leixiao.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_leixiao.utils.Utils;

/**
 * Problem: when some document sentences have the same score, it will only choose the first one
 * @author leixiao
 *
 */
public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** record the number of the sentence **/
	public ArrayList<Integer> senList;

	/** record the token and its frequency in one sentence **/
	public ArrayList<Map> al;

	/** record the cosine similarity for each sentence **/
	public ArrayList<Double> score;

	/** record the vector of the two sentences **/
	public ArrayList<Integer> s1;

	public ArrayList<Integer> s2;

	/** record the rank of each document sentence with the format <senId,rank> **/
	public Map<Integer, Integer> rankList;

	/** sort the rank **/
	public ArrayList<Double> r;
	
	/** get the rank score for each query **/
	public ArrayList<Integer> rankScore;

	public int sen = 0;

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		senList = new ArrayList<Integer>();

		al = new ArrayList<Map>();

		score = new ArrayList<Double>();

		s1 = new ArrayList<Integer>();

		s2 = new ArrayList<Integer>();

		rankList = new HashMap<Integer, Integer>();

		r = new ArrayList<Double>();
		
		rankScore = new ArrayList<Integer>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		sen++;

		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

		if (it.hasNext()) {

			// Map<String,Integer> map = new HashMap<String,Integer>();
			al.add(new HashMap<String, Integer>());

			System.out.println("sen" + sen);
			Document doc = (Document) it.next();

			// Make sure that your previous annotators have populated this in
			// CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
			Iterator<Token> iter = tokenList.iterator();

			/**
			 * read one sentence text from the document, and got each token and
			 * its frequency in each sentence and now deal with the word, to
			 * find the frequency of each word in each document with the same
			 * qid
			 */
			while (iter.hasNext()) {
				Token token = iter.next();
				// System.out.println(token.getText());
				// System.out.println(token.getFrequency());
				String text = token.getText();
				int frequency = token.getFrequency();
				al.get(sen - 1).put(text, frequency);
			}

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			senList.add(sen - 1);

			// Do something useful here

		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure

		int id = qIdList.get(0);
		int querynumber = 0;
		int i;

		for (i = 0; i < relList.size(); i++) {
			if (qIdList.get(i) == id) {
				if (relList.get(i) == 99) {
					System.out.println(i + " is query");
					querynumber = i;
					score.add(0.0);

				} else {
					System.out.println(i + " is document");
					score.add(computeCosineSimilarity((HashMap<String, Integer>) al.get(querynumber),
							(HashMap<String, Integer>) al.get(i)));

				}
			} else {
				id = qIdList.get(i);
				i--;
			}

		}

		// TODO :: compute the rank of retrieved sentences
		/**
		 * help store the rank for each document sentence, with the format of
		 * <key:sentenceID i,value: Score>
		 **/
		Map<Integer,Double> rank = new HashMap<Integer,Double>();

		id = qIdList.get(0);

		for (i = 0; i < relList.size(); i++) {

			if (qIdList.get(i) == id) {
				if (relList.get(i) == 99) {
					System.out.println(i + " is query");
					 querynumber = i;
					
					rankList.put(i, 0);

				} else {
					System.out.println(i + " is document");
					rank.put(i,score.get(i));
					System.out.println(i + " " + score.get(i));
					r.add(score.get(i));
				}

			} else {
				/** sort the rank by r **/
				Collections.sort(r);
				Collections.reverse(r);

				for (int j = 0; j < r.size(); j++) {
					System.out.println("&&&&&&&&&&&&&&&&");
					// System.out.println(r.get(j));
					/**get the score of the sentence**/
					double d = r.get(j);
					
					/**according to the score, to find the sentenceId**/
					
					//int s = rank.get(d);
					int s=0;
					
					Set<Integer>kset=rank.keySet();
					for(int ks:kset){
					    if(d==rank.get(ks)){
					         System.out.println(ks);
					         s=ks;
					         break;
					    }
					}
					
					/*****/
					rankList.put(s, j+1);
					
					System.out.println("sentence" + s + " rank: " + (j+1));
					
					if(relList.get(s)==1){
						rankScore.add(s-querynumber);
						System.out.println("rank i"+(s-querynumber));
					}
				
				}

				rank.clear();
				r.clear();
				/** continue the later calculation **/
				id = qIdList.get(i);
				i--;
			}

		}
		/** sort the rank by r **/
		Collections.sort(r);
		Collections.reverse(r);
		for (int j = 0; j < r.size(); j++) {
			System.out.println("&&&&&&&&&&&&&&&&");
			double d = r.get(j);
			//int s = rank.get(d);
			
			int s=0;
			
			Set<Integer>kset=rank.keySet();
			for(int ks:kset){
			    if(d==rank.get(ks)){
			         System.out.println(ks);
			         s=ks;
			         break;
			    }
			}
			
			rankList.put(s, j+1);
			System.out.println("sentence" + s + " rank: " + (j+1));
			// System.out.println(r.get(j));
			if(relList.get(s)==1){
				rankScore.add(s-querynumber);
				System.out.println("rank i"+(s-querynumber));
			}
			
		}
		rank.clear();
		r.clear();

		// TODO :: compute the metric:: mean reciprocal rank

		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 *
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		int querysize = queryVector.size();
		int documentsize = docVector.size();
		int k = 0;
		int i = 0;

		int vec = 0;

		Map<String, Integer> temp = new HashMap<String, Integer>();

		// TODO :: compute cosine similarity between two sentences
	
		temp.putAll(queryVector);
		temp.putAll(docVector);

		for (Map.Entry<String, Integer> entry : temp.entrySet()) {
			String a = entry.getKey();
			int b = entry.getValue();


			if (queryVector.containsKey(a)) {
				k = queryVector.get(a);
				s1.add(k);
			} else {
				s1.add(0);
			}
			if (docVector.containsKey(a)) {
				k = docVector.get(a);
				s2.add(k);
			} else {
				s2.add(0);
			}
		}
		// System.out.println("**************");

		for (i = 0; i < s1.size(); i++) {
			// System.out.println(i+" s1:" +s1.get(i)+" s2:" +s2.get(i));

			vec += s1.get(i) * s2.get(i);
		}

		cosine_similarity = (double) (vec) / (double) (querysize * documentsize);

		System.out.println("**************" + cosine_similarity);

		/** remove all the elements of s1 and s2 **/
		s1.clear();
		s2.clear();
		return cosine_similarity;
	}

	/**
	 *
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;
		
		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		int num=rankScore.size();
		double ranksum=0.0;
		for(Integer str:rankScore){
		    System.out.println(str);
		    ranksum+=(double)1/(double)str;
		   }
		metric_mrr=(double)ranksum/(double)num;
		
		/*************/
		for (Map.Entry<Integer, Integer> entry : rankList.entrySet()) {
			int a=entry.getKey();
			int b=entry.getValue();
			System.out.println("sentence "+a+" "+"rank:"+b);
			
		}
			 
		

		return metric_mrr;
	}

}