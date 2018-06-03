package chatbot.ai_event.processor.nlp;

public enum EPartOfSpeech {
	CoordinatingConjunction,
	CardinalNumber,
	Determiner,
	ExistentialThere,
	ForeignWord,
	PrepositionOrSubordinatingConjunction,
	Adjective,
	AdjectiveComparative,
	AdjectiveSuperlative,
	ListItemMarker,
	Modal,
	NounSingularOrMass,
	NounPlural,
	ProperNounSingular,
	ProperNounPlural,
	Predeterminer,
	PossessiveEnding,
	PersonalPronoun,
	PossessivePronoun,
	Adverb,
	AdverbComparative,
	AdverbSuperlative,
	Particle,
	Symbol,
	To,
	Interjection,
	VerbBaseForm,
	VerbPastTense,
	VerbGerundOrPresentParticiple,
	VerbPastParticiple,
	VerbNon_3rdPersonSingularPresent,
	VerbN3rdPersonSingularPresent,
	Wh_Determiner,
	Wh_Pronoun,
	PossessiveWh_Pronoun,
	Wh_Adverb;

	public static EPartOfSpeech parse(String sPos) {
		// cf http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
	
		switch (sPos) {
		case "CC": return	EPartOfSpeech.CoordinatingConjunction;
		case "CD": return	EPartOfSpeech.CardinalNumber;
		case "DT": return	EPartOfSpeech.Determiner;
		case "EX": return	EPartOfSpeech.ExistentialThere;
		case "FW": return	EPartOfSpeech.ForeignWord;
		case "IN": return	EPartOfSpeech.PrepositionOrSubordinatingConjunction;
		case "JJ": return	EPartOfSpeech.Adjective;
		case "JJR": return	EPartOfSpeech.AdjectiveComparative;
		case "JJS": return	EPartOfSpeech.AdjectiveSuperlative;
		case "LS": return	EPartOfSpeech.ListItemMarker;
		case "MD": return	EPartOfSpeech.Modal;
		case "NN": return	EPartOfSpeech.NounSingularOrMass;
		case "NNS": return	EPartOfSpeech.NounPlural;
		case "NNP": return	EPartOfSpeech.ProperNounSingular;
		case "NNPS": return	EPartOfSpeech.ProperNounPlural;
		case "PDT": return	EPartOfSpeech.Predeterminer;
		case "POS": return	EPartOfSpeech.PossessiveEnding;
		case "PRP": return	EPartOfSpeech.PersonalPronoun;
		case "PRP$": return	EPartOfSpeech.PossessivePronoun;
		case "RB": return	EPartOfSpeech.Adverb;
		case "RBR": return	EPartOfSpeech.AdverbComparative;
		case "RBS": return	EPartOfSpeech.AdverbSuperlative;
		case "RP": return	EPartOfSpeech.Particle;
		case "SYM": return	EPartOfSpeech.Symbol;
		case "TO": return	EPartOfSpeech.To;
		case "UH": return	EPartOfSpeech.Interjection;
		case "VB": return	EPartOfSpeech.VerbBaseForm;
		case "VBD": return	EPartOfSpeech.VerbPastTense;
		case "VBG": return	EPartOfSpeech.VerbGerundOrPresentParticiple;
		case "VBN": return	EPartOfSpeech.VerbPastParticiple;
		case "VBP": return	EPartOfSpeech.VerbNon_3rdPersonSingularPresent;
		case "VBZ": return	EPartOfSpeech.VerbN3rdPersonSingularPresent;
		case "WDT": return	EPartOfSpeech.Wh_Determiner;
		case "WP": return	EPartOfSpeech.Wh_Pronoun;
		case "WP$": return	EPartOfSpeech.PossessiveWh_Pronoun;
		case "WRB": return	EPartOfSpeech.Wh_Adverb;
		default: return null;
		}
	}
}
