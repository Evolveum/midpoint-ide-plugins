// Generated from C:/midpoint/tgit/general-test/src/pcv\PrismItems.g4 by ANTLR 4.6
package pcv;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PrismItemsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, PRV=11, PPV=12, NAMESPACE=13, ID=14, WS=15;
	public static final int
		RULE_start = 0, RULE_item = 1, RULE_prismContainer = 2, RULE_prismReference = 3, 
		RULE_prismProperty = 4, RULE_pcv = 5, RULE_name = 6;
	public static final String[] ruleNames = {
		"start", "item", "prismContainer", "prismReference", "prismProperty", 
		"pcv", "name"
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

	@Override
	public String getGrammarFileName() { return "PrismItems.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PrismItemsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StartContext extends ParserRuleContext {
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public PcvContext pcv() {
			return getRuleContext(PcvContext.class,0);
		}
		public TerminalNode PRV() { return getToken(PrismItemsParser.PRV, 0); }
		public TerminalNode PPV() { return getToken(PrismItemsParser.PPV, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			setState(18);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__7:
			case T__8:
				enterOuterAlt(_localctx, 1);
				{
				setState(14);
				item();
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(15);
				pcv();
				}
				break;
			case PRV:
				enterOuterAlt(_localctx, 3);
				{
				setState(16);
				match(PRV);
				}
				break;
			case PPV:
				enterOuterAlt(_localctx, 4);
				{
				setState(17);
				match(PPV);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ItemContext extends ParserRuleContext {
		public PrismContainerContext prismContainer() {
			return getRuleContext(PrismContainerContext.class,0);
		}
		public PrismReferenceContext prismReference() {
			return getRuleContext(PrismReferenceContext.class,0);
		}
		public PrismPropertyContext prismProperty() {
			return getRuleContext(PrismPropertyContext.class,0);
		}
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_item);
		try {
			setState(23);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(20);
				prismContainer();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 2);
				{
				setState(21);
				prismReference();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 3);
				{
				setState(22);
				prismProperty();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrismContainerContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public List<PcvContext> pcv() {
			return getRuleContexts(PcvContext.class);
		}
		public PcvContext pcv(int i) {
			return getRuleContext(PcvContext.class,i);
		}
		public PrismContainerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prismContainer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterPrismContainer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitPrismContainer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitPrismContainer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrismContainerContext prismContainer() throws RecognitionException {
		PrismContainerContext _localctx = new PrismContainerContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_prismContainer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			match(T__0);
			setState(26);
			match(T__1);
			setState(27);
			name();
			setState(28);
			match(T__2);
			setState(29);
			match(T__3);
			setState(30);
			match(T__4);
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(31);
				pcv();
				setState(36);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(32);
					match(T__5);
					setState(33);
					pcv();
					}
					}
					setState(38);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(41);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrismReferenceContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public List<TerminalNode> PRV() { return getTokens(PrismItemsParser.PRV); }
		public TerminalNode PRV(int i) {
			return getToken(PrismItemsParser.PRV, i);
		}
		public PrismReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prismReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterPrismReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitPrismReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitPrismReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrismReferenceContext prismReference() throws RecognitionException {
		PrismReferenceContext _localctx = new PrismReferenceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_prismReference);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			match(T__7);
			setState(44);
			match(T__1);
			setState(45);
			name();
			setState(46);
			match(T__2);
			setState(47);
			match(T__3);
			setState(48);
			match(T__4);
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PRV) {
				{
				setState(49);
				match(PRV);
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(50);
					match(T__5);
					setState(51);
					match(PRV);
					}
					}
					setState(56);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(59);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrismPropertyContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public List<TerminalNode> PPV() { return getTokens(PrismItemsParser.PPV); }
		public TerminalNode PPV(int i) {
			return getToken(PrismItemsParser.PPV, i);
		}
		public PrismPropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prismProperty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterPrismProperty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitPrismProperty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitPrismProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrismPropertyContext prismProperty() throws RecognitionException {
		PrismPropertyContext _localctx = new PrismPropertyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_prismProperty);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			match(T__8);
			setState(62);
			match(T__1);
			setState(63);
			name();
			setState(64);
			match(T__2);
			setState(65);
			match(T__3);
			setState(66);
			match(T__4);
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PPV) {
				{
				setState(67);
				match(PPV);
				setState(72);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__5) {
					{
					{
					setState(68);
					match(T__5);
					setState(69);
					match(PPV);
					}
					}
					setState(74);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(77);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PcvContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode ID() { return getToken(PrismItemsParser.ID, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public PcvContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pcv; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterPcv(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitPcv(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitPcv(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PcvContext pcv() throws RecognitionException {
		PcvContext _localctx = new PcvContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_pcv);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(T__9);
			setState(80);
			match(T__1);
			{
			setState(81);
			name();
			}
			setState(82);
			match(T__2);
			setState(83);
			match(T__3);
			setState(97);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				{
				setState(84);
				match(T__4);
				setState(93);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8))) != 0)) {
					{
					setState(85);
					item();
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__5) {
						{
						{
						setState(86);
						match(T__5);
						setState(87);
						item();
						}
						}
						setState(92);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(95);
				match(T__6);
				}
				}
				break;
			case ID:
				{
				setState(96);
				match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PrismItemsParser.ID, 0); }
		public TerminalNode NAMESPACE() { return getToken(PrismItemsParser.NAMESPACE, 0); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PrismItemsListener ) ((PrismItemsListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PrismItemsVisitor ) return ((PrismItemsVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NAMESPACE) {
				{
				setState(99);
				match(NAMESPACE);
				}
			}

			setState(102);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\21k\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\2\5\2\25\n\2"+
		"\3\3\3\3\3\3\5\3\32\n\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4%\n\4\f"+
		"\4\16\4(\13\4\5\4*\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\7\5"+
		"\67\n\5\f\5\16\5:\13\5\5\5<\n\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\7\6I\n\6\f\6\16\6L\13\6\5\6N\n\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\7\7[\n\7\f\7\16\7^\13\7\5\7`\n\7\3\7\3\7\5\7d\n\7\3\b\5"+
		"\bg\n\b\3\b\3\b\3\b\2\2\t\2\4\6\b\n\f\16\2\2r\2\24\3\2\2\2\4\31\3\2\2"+
		"\2\6\33\3\2\2\2\b-\3\2\2\2\n?\3\2\2\2\fQ\3\2\2\2\16f\3\2\2\2\20\25\5\4"+
		"\3\2\21\25\5\f\7\2\22\25\7\r\2\2\23\25\7\16\2\2\24\20\3\2\2\2\24\21\3"+
		"\2\2\2\24\22\3\2\2\2\24\23\3\2\2\2\25\3\3\2\2\2\26\32\5\6\4\2\27\32\5"+
		"\b\5\2\30\32\5\n\6\2\31\26\3\2\2\2\31\27\3\2\2\2\31\30\3\2\2\2\32\5\3"+
		"\2\2\2\33\34\7\3\2\2\34\35\7\4\2\2\35\36\5\16\b\2\36\37\7\5\2\2\37 \7"+
		"\6\2\2 )\7\7\2\2!&\5\f\7\2\"#\7\b\2\2#%\5\f\7\2$\"\3\2\2\2%(\3\2\2\2&"+
		"$\3\2\2\2&\'\3\2\2\2\'*\3\2\2\2(&\3\2\2\2)!\3\2\2\2)*\3\2\2\2*+\3\2\2"+
		"\2+,\7\t\2\2,\7\3\2\2\2-.\7\n\2\2./\7\4\2\2/\60\5\16\b\2\60\61\7\5\2\2"+
		"\61\62\7\6\2\2\62;\7\7\2\2\638\7\r\2\2\64\65\7\b\2\2\65\67\7\r\2\2\66"+
		"\64\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3\2\2\29<\3\2\2\2:8\3\2\2\2;\63"+
		"\3\2\2\2;<\3\2\2\2<=\3\2\2\2=>\7\t\2\2>\t\3\2\2\2?@\7\13\2\2@A\7\4\2\2"+
		"AB\5\16\b\2BC\7\5\2\2CD\7\6\2\2DM\7\7\2\2EJ\7\16\2\2FG\7\b\2\2GI\7\16"+
		"\2\2HF\3\2\2\2IL\3\2\2\2JH\3\2\2\2JK\3\2\2\2KN\3\2\2\2LJ\3\2\2\2ME\3\2"+
		"\2\2MN\3\2\2\2NO\3\2\2\2OP\7\t\2\2P\13\3\2\2\2QR\7\f\2\2RS\7\4\2\2ST\5"+
		"\16\b\2TU\7\5\2\2Uc\7\6\2\2V_\7\7\2\2W\\\5\4\3\2XY\7\b\2\2Y[\5\4\3\2Z"+
		"X\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]`\3\2\2\2^\\\3\2\2\2_W\3\2\2"+
		"\2_`\3\2\2\2`a\3\2\2\2ad\7\t\2\2bd\7\20\2\2cV\3\2\2\2cb\3\2\2\2d\r\3\2"+
		"\2\2eg\7\17\2\2fe\3\2\2\2fg\3\2\2\2gh\3\2\2\2hi\7\20\2\2i\17\3\2\2\2\16"+
		"\24\31&)8;JM\\_cf";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}