// Generated from C:/midpoint/tgit/general-test/src/pcv\PrismItems.g4 by ANTLR 4.6
package pcv;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PrismItemsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, PRV=11, PPV=12, NAMESPACE=13, ID=14, WS=15;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "PRV", "PPV", "NAMESPACE", "ID", "WS", "ESC"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'PC'", "'('", "')'", "':'", "'['", "','", "']'", "'PrismReference'", 
		"'PP'", "'PCV'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, "PRV", 
		"PPV", "NAMESPACE", "ID", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public PrismItemsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PrismItems.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\21\u0096\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3"+
		"\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13"+
		"\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\fb\n\f\3\f\5\fe\n\f\3\f\3\f\7\f"+
		"i\n\f\f\f\16\fl\13\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\7\ry"+
		"\n\r\f\r\16\r|\13\r\3\r\3\r\3\16\3\16\6\16\u0082\n\16\r\16\16\16\u0083"+
		"\3\16\3\16\3\17\6\17\u0089\n\17\r\17\16\17\u008a\3\20\6\20\u008e\n\20"+
		"\r\20\16\20\u008f\3\20\3\20\3\21\3\21\3\21\2\2\22\3\3\5\4\7\5\t\6\13\7"+
		"\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\2\3\2\7\3\2++"+
		"\6\2/<C\\aac|\7\2//\62;C\\aac|\5\2\13\f\17\17\"\"\4\2++^^\u009d\2\3\3"+
		"\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2"+
		"\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3"+
		"\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\3#\3\2\2\2\5&\3\2\2\2\7"+
		"(\3\2\2\2\t*\3\2\2\2\13,\3\2\2\2\r.\3\2\2\2\17\60\3\2\2\2\21\62\3\2\2"+
		"\2\23A\3\2\2\2\25D\3\2\2\2\27H\3\2\2\2\31o\3\2\2\2\33\177\3\2\2\2\35\u0088"+
		"\3\2\2\2\37\u008d\3\2\2\2!\u0093\3\2\2\2#$\7R\2\2$%\7E\2\2%\4\3\2\2\2"+
		"&\'\7*\2\2\'\6\3\2\2\2()\7+\2\2)\b\3\2\2\2*+\7<\2\2+\n\3\2\2\2,-\7]\2"+
		"\2-\f\3\2\2\2./\7.\2\2/\16\3\2\2\2\60\61\7_\2\2\61\20\3\2\2\2\62\63\7"+
		"R\2\2\63\64\7t\2\2\64\65\7k\2\2\65\66\7u\2\2\66\67\7o\2\2\678\7T\2\28"+
		"9\7g\2\29:\7h\2\2:;\7g\2\2;<\7t\2\2<=\7g\2\2=>\7p\2\2>?\7e\2\2?@\7g\2"+
		"\2@\22\3\2\2\2AB\7R\2\2BC\7R\2\2C\24\3\2\2\2DE\7R\2\2EF\7E\2\2FG\7X\2"+
		"\2G\26\3\2\2\2HI\7R\2\2IJ\7T\2\2JK\7X\2\2KL\3\2\2\2LM\7*\2\2MN\7q\2\2"+
		"NO\7k\2\2OP\7f\2\2PQ\3\2\2\2QR\7?\2\2Rd\5\35\17\2ST\7.\2\2TU\7v\2\2UV"+
		"\7c\2\2VW\7t\2\2WX\7i\2\2XY\7g\2\2YZ\7v\2\2Z[\7V\2\2[\\\7{\2\2\\]\7r\2"+
		"\2]^\7g\2\2^_\3\2\2\2_a\7?\2\2`b\5\33\16\2a`\3\2\2\2ab\3\2\2\2bc\3\2\2"+
		"\2ce\5\35\17\2dS\3\2\2\2de\3\2\2\2ej\3\2\2\2fi\5!\21\2gi\n\2\2\2hf\3\2"+
		"\2\2hg\3\2\2\2il\3\2\2\2jh\3\2\2\2jk\3\2\2\2km\3\2\2\2lj\3\2\2\2mn\7+"+
		"\2\2n\30\3\2\2\2op\7R\2\2pq\7R\2\2qr\7X\2\2rs\3\2\2\2st\7*\2\2tu\5\35"+
		"\17\2uz\7<\2\2vy\5!\21\2wy\n\2\2\2xv\3\2\2\2xw\3\2\2\2y|\3\2\2\2zx\3\2"+
		"\2\2z{\3\2\2\2{}\3\2\2\2|z\3\2\2\2}~\7+\2\2~\32\3\2\2\2\177\u0081\7}\2"+
		"\2\u0080\u0082\t\3\2\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0081"+
		"\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0086\7\177\2\2"+
		"\u0086\34\3\2\2\2\u0087\u0089\t\4\2\2\u0088\u0087\3\2\2\2\u0089\u008a"+
		"\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b\36\3\2\2\2\u008c"+
		"\u008e\t\5\2\2\u008d\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u008d\3\2"+
		"\2\2\u008f\u0090\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u0092\b\20\2\2\u0092"+
		" \3\2\2\2\u0093\u0094\7^\2\2\u0094\u0095\t\6\2\2\u0095\"\3\2\2\2\f\2a"+
		"dhjxz\u0083\u008a\u008f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}