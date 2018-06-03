package chatbot.ai_event.processor.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class RhinosNLP {
	
	protected static final String PROPERTY_KEY_PATTERNS = "patterns";
	
	protected static final String PATTERN_KEY_SUFFIX_ACTION = ".action";
	protected static final String PATTERN_KEY_SUFFIX_VERBS = ".verbs";
	protected static final String PATTERN_KEY_SUFFIX_NOUNS = ".nouns";
	protected static final String PATTERN_KEY_SUFFIX_SYSTEMS = ".systems";
	
	protected StanfordCoreNLP pipeline_;
	
	protected List<TargetPattern> targetPatterns_ = new ArrayList<>();
	
	public class TargetPattern {
		protected String key_;
		protected String action_;
		
		protected Set<String> possibleVerbs_ = new HashSet<>();
		protected Set<String> possibleNouns_ = new HashSet<>();
		protected Set<String> possibleSystems_ = new HashSet<>();
		
		public TargetPattern(String key, String action) {
			key_ = key;
			action_ = action;
		}
		
		public void addPossibleVerbs(Collection<String> possibleVerbs) {
			possibleVerbs_.addAll(possibleVerbs);
		}
		public void addPossibleNouns(Collection<String> possibleNouns) {
			possibleNouns_.addAll(possibleNouns);
		}
		public void addPossibleSystems(Collection<String> possibleSystems) {
			possibleSystems_.addAll(possibleSystems);
		}
		
		public boolean matchVerb(String verb) {
			return possibleVerbs_.contains(verb.toLowerCase());
		}
		public boolean matchNoun(String noun) {
			return possibleNouns_.contains(noun.toLowerCase());
		}
		public boolean matchSystem(String system) {
			return possibleSystems_.contains(system.toLowerCase());
		}
	}
	
	public RhinosNLP() throws IOException {
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    
	    pipeline_ = new StanfordCoreNLP(props);
	    
		  // patterns=download_log
		  //download_log.verbs=download,get
		  //download_log.nouns=logs
		  //download_log.systems=oms,ems,fixgateway,marketgateway
	    
	    URL url = getClass().getResource("nlp_pattern.properties");
	    InputStream in = url.openStream();
	    
	    Properties patternProps = new Properties();
	    patternProps.load(in);
	    
	    String patternCSV = patternProps.getProperty(PROPERTY_KEY_PATTERNS);
	    
	    List<String> patterns = Arrays.asList(patternCSV.split("\\s*,\\s*"));
	    String temp;
	    for (String pattern : patterns) {
	    	temp = patternProps.getProperty(pattern + PATTERN_KEY_SUFFIX_ACTION);
	    	
	    	TargetPattern targetPattern = new TargetPattern(pattern, temp);
	    	
	    	temp = patternProps.getProperty(pattern + PATTERN_KEY_SUFFIX_VERBS).toLowerCase();
	    	targetPattern.addPossibleVerbs(Arrays.asList(temp.split("\\s*,\\s*")));
	    	
	    	temp = patternProps.getProperty(pattern + PATTERN_KEY_SUFFIX_NOUNS).toLowerCase();
	    	targetPattern.addPossibleNouns(Arrays.asList(temp.split("\\s*,\\s*")));
	    	
	    	temp = patternProps.getProperty(pattern + PATTERN_KEY_SUFFIX_SYSTEMS).toLowerCase();
	    	targetPattern.addPossibleSystems(Arrays.asList(temp.split("\\s*,\\s*")));
	    	
	    	targetPatterns_.add(targetPattern);
	    }
	}
	
	public void parse(String streamId, String text) {
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	
	    // run all Annotators on this text
	    pipeline_.annotate(document);
	
	    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    List<String> verbs = new ArrayList<>();
	    List<String> nouns = new ArrayList<>();
	
	    for (CoreMap sentence : sentences) {
	        // traversing the words in the current sentence
	        // a CoreLabel is a CoreMap with additional token-specific methods
	        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
	            // this is the text of the token
	            String word = token.get(CoreAnnotations.TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
	            // this is the NER label of the token
	            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
	
	            System.out.println(String.format("Print: word: [%s] pos: [%s] ne: [%s]", word, pos, ne));
	            
	            if (EPartOfSpeech.VerbBaseForm.equals(EPartOfSpeech.parse(pos))) {
            		verbs.add(word);
	            } else if (EPartOfSpeech.NounSingularOrMass.equals(EPartOfSpeech.parse(pos)) ||
	            			EPartOfSpeech.NounPlural.equals(EPartOfSpeech.parse(pos)) ||
	            			EPartOfSpeech.ProperNounSingular.equals(EPartOfSpeech.parse(pos))) {
            		nouns.add(word);
	            }
	        }
	    }
	    

	    
	    // possible scenario:
	    	// download logs
	    	// pull in additional team/people
	    	// ?
	    
	    boolean performedTask = false;
	    if (!verbs.isEmpty() && !nouns.isEmpty()) {
	    	for (TargetPattern targetPattern : targetPatterns_) {
	    		boolean verbMatched = false;
	    		for (String verb : verbs) {
	    			if (targetPattern.matchVerb(verb)) {
	    				verbMatched = true;
	    				break;
	    			}
	    		}
	    		if (!verbMatched)
	    			continue;
	    		
	    		boolean nounMatched = false;
	    		for (String noun : nouns) {
	    			if (targetPattern.matchNoun(noun)) {
	    				nounMatched = true;
	    				break;
	    			}
	    		}
	    		if (!nounMatched)
	    			continue;
	    		
	    		List<String> systemMatched = new ArrayList<>();
	    		for (String noun : nouns) {
	    			if (targetPattern.matchSystem(noun)) {
	    				systemMatched.add(noun);
	    			}
	    		}
	    		
	    		if (systemMatched.isEmpty())
	    			continue;
	    		
	    		// create task
	    		new DownloadLogTask(streamId, systemMatched).perform();
	    		performedTask = true;
	    	}
	    }
		
	    if (!performedTask)
	    	System.out.println("Nothing to do....");
	}
	
	public class DownloadLogTask {
		protected String streamId_;
		
		protected List<String> targetSystems_ = new ArrayList<String>();
		
		public DownloadLogTask(String streamId, Collection<String> systems) {
			streamId_ = streamId;
			targetSystems_.addAll(systems);
		}
		
		public void perform() {
			System.out.println("I am going to download the " + targetSystems_ + " logs now...");
			System.out.println("Got it - copied to machineXX:/level2support/log/");
			System.out.println("Sending a message to tell everyone in the chatroom!");
		}
	}
}
