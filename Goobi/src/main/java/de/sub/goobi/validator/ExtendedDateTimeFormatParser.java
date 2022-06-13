// Generated from ExtendedDateTimeFormat.g4 by ANTLR 4.10.1

package de.sub.goobi.validator;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExtendedDateTimeFormatParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T=1, Z=2, Dash=3, Plus=4, Colon=5, Slash=6, Year=7, NegativeYear=8, PositiveYear=9, 
		Digit=10, PositiveDigit=11, YearZero=12, Month=13, MonthDay=14, YearMonth=15, 
		YearMonthDay=16, Day=17, Hour=18, Minute=19, Second=20, OneThru12=21, 
		OneThru13=22, OneThru23=23, ZeroThru23=24, OneThru29=25, OneThru30=26, 
		OneThru31=27, OneThru59=28, ZeroThru59=29, Time=30, ZOffset=31, OffsetTime=32, 
		U=33, UU=34, Tilde=35, Open=36, QuestionMark=37, QuestionMarkTilde=38, 
		YearUA=39, SeasonUA=40, YearMonthUA=41, YearMonthDayUA=42, Season=43, 
		UASymbol=44, YearWithOneUnspecifedDigit=45, YearWithTwoUnspecifedDigits=46;
	public static final int
		RULE_edtf = 0, RULE_level0Expression = 1, RULE_date = 2, RULE_dateTime = 3, 
		RULE_dateTimeZ = 4, RULE_level0Interval = 5, RULE_level1Expression = 6, 
		RULE_unspecifiedDate = 7, RULE_monthUnspecified = 8, RULE_uncertainOrApproxDate = 9, 
		RULE_dayUnspecified = 10, RULE_dayAndMonthUnspecified = 11, RULE_yearUnspecified = 12, 
		RULE_open = 13, RULE_season = 14, RULE_level1Interval = 15, RULE_dateOrSeason = 16, 
		RULE_unknownBeginning = 17, RULE_unknownEnd = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"edtf", "level0Expression", "date", "dateTime", "dateTimeZ", "level0Interval", 
			"level1Expression", "unspecifiedDate", "monthUnspecified", "uncertainOrApproxDate", 
			"dayUnspecified", "dayAndMonthUnspecified", "yearUnspecified", "open", 
			"season", "level1Interval", "dateOrSeason", "unknownBeginning", "unknownEnd"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'T'", "'Z'", "'-'", "'+'", "':'", "'/'", null, null, null, null, 
			null, "'0000'", null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "'X'", 
			"'XX'", "'~'", "'..'", "'?'", "'%'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "T", "Z", "Dash", "Plus", "Colon", "Slash", "Year", "NegativeYear", 
			"PositiveYear", "Digit", "PositiveDigit", "YearZero", "Month", "MonthDay", 
			"YearMonth", "YearMonthDay", "Day", "Hour", "Minute", "Second", "OneThru12", 
			"OneThru13", "OneThru23", "ZeroThru23", "OneThru29", "OneThru30", "OneThru31", 
			"OneThru59", "ZeroThru59", "Time", "ZOffset", "OffsetTime", "U", "UU", 
			"Tilde", "Open", "QuestionMark", "QuestionMarkTilde", "YearUA", "SeasonUA", 
			"YearMonthUA", "YearMonthDayUA", "Season", "UASymbol", "YearWithOneUnspecifedDigit", 
			"YearWithTwoUnspecifedDigits"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
	public String getGrammarFileName() { return "ExtendedDateTimeFormat.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExtendedDateTimeFormatParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class EdtfContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ExtendedDateTimeFormatParser.EOF, 0); }
		public Level0ExpressionContext level0Expression() {
			return getRuleContext(Level0ExpressionContext.class,0);
		}
		public Level1ExpressionContext level1Expression() {
			return getRuleContext(Level1ExpressionContext.class,0);
		}
		public EdtfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edtf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterEdtf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitEdtf(this);
		}
	}

	public final EdtfContext edtf() throws RecognitionException {
		EdtfContext _localctx = new EdtfContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_edtf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(38);
				level0Expression();
				}
				break;
			case 2:
				{
				setState(39);
				level1Expression();
				}
				break;
			}
			setState(42);
			match(EOF);
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

	public static class Level0ExpressionContext extends ParserRuleContext {
		public DateContext date() {
			return getRuleContext(DateContext.class,0);
		}
		public DateTimeContext dateTime() {
			return getRuleContext(DateTimeContext.class,0);
		}
		public DateTimeZContext dateTimeZ() {
			return getRuleContext(DateTimeZContext.class,0);
		}
		public Level0IntervalContext level0Interval() {
			return getRuleContext(Level0IntervalContext.class,0);
		}
		public Level0ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level0Expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterLevel0Expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitLevel0Expression(this);
		}
	}

	public final Level0ExpressionContext level0Expression() throws RecognitionException {
		Level0ExpressionContext _localctx = new Level0ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_level0Expression);
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(44);
				date();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(45);
				dateTime();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(46);
				dateTimeZ();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(47);
				level0Interval();
				}
				break;
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

	public static class DateContext extends ParserRuleContext {
		public TerminalNode Year() { return getToken(ExtendedDateTimeFormatParser.Year, 0); }
		public TerminalNode YearMonth() { return getToken(ExtendedDateTimeFormatParser.YearMonth, 0); }
		public TerminalNode YearMonthDay() { return getToken(ExtendedDateTimeFormatParser.YearMonthDay, 0); }
		public DateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_date; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDate(this);
		}
	}

	public final DateContext date() throws RecognitionException {
		DateContext _localctx = new DateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_date);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Year) | (1L << YearMonth) | (1L << YearMonthDay))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class DateTimeContext extends ParserRuleContext {
		public TerminalNode YearMonthDay() { return getToken(ExtendedDateTimeFormatParser.YearMonthDay, 0); }
		public TerminalNode T() { return getToken(ExtendedDateTimeFormatParser.T, 0); }
		public TerminalNode Time() { return getToken(ExtendedDateTimeFormatParser.Time, 0); }
		public TerminalNode ZOffset() { return getToken(ExtendedDateTimeFormatParser.ZOffset, 0); }
		public DateTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDateTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDateTime(this);
		}
	}

	public final DateTimeContext dateTime() throws RecognitionException {
		DateTimeContext _localctx = new DateTimeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_dateTime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			match(YearMonthDay);
			setState(53);
			match(T);
			setState(54);
			match(Time);
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ZOffset) {
				{
				setState(55);
				match(ZOffset);
				}
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

	public static class DateTimeZContext extends ParserRuleContext {
		public TerminalNode YearMonthDay() { return getToken(ExtendedDateTimeFormatParser.YearMonthDay, 0); }
		public TerminalNode T() { return getToken(ExtendedDateTimeFormatParser.T, 0); }
		public TerminalNode Time() { return getToken(ExtendedDateTimeFormatParser.Time, 0); }
		public TerminalNode Z() { return getToken(ExtendedDateTimeFormatParser.Z, 0); }
		public DateTimeZContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeZ; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDateTimeZ(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDateTimeZ(this);
		}
	}

	public final DateTimeZContext dateTimeZ() throws RecognitionException {
		DateTimeZContext _localctx = new DateTimeZContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dateTimeZ);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			match(YearMonthDay);
			setState(59);
			match(T);
			setState(60);
			match(Time);
			setState(61);
			match(Z);
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

	public static class Level0IntervalContext extends ParserRuleContext {
		public List<DateContext> date() {
			return getRuleContexts(DateContext.class);
		}
		public DateContext date(int i) {
			return getRuleContext(DateContext.class,i);
		}
		public TerminalNode Slash() { return getToken(ExtendedDateTimeFormatParser.Slash, 0); }
		public Level0IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level0Interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterLevel0Interval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitLevel0Interval(this);
		}
	}

	public final Level0IntervalContext level0Interval() throws RecognitionException {
		Level0IntervalContext _localctx = new Level0IntervalContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_level0Interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			date();
			setState(64);
			match(Slash);
			setState(65);
			date();
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

	public static class Level1ExpressionContext extends ParserRuleContext {
		public UncertainOrApproxDateContext uncertainOrApproxDate() {
			return getRuleContext(UncertainOrApproxDateContext.class,0);
		}
		public UnspecifiedDateContext unspecifiedDate() {
			return getRuleContext(UnspecifiedDateContext.class,0);
		}
		public Level1IntervalContext level1Interval() {
			return getRuleContext(Level1IntervalContext.class,0);
		}
		public SeasonContext season() {
			return getRuleContext(SeasonContext.class,0);
		}
		public Level1ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level1Expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterLevel1Expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitLevel1Expression(this);
		}
	}

	public final Level1ExpressionContext level1Expression() throws RecognitionException {
		Level1ExpressionContext _localctx = new Level1ExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_level1Expression);
		try {
			setState(71);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				uncertainOrApproxDate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				unspecifiedDate();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(69);
				level1Interval();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(70);
				season();
				}
				break;
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

	public static class UnspecifiedDateContext extends ParserRuleContext {
		public YearUnspecifiedContext yearUnspecified() {
			return getRuleContext(YearUnspecifiedContext.class,0);
		}
		public MonthUnspecifiedContext monthUnspecified() {
			return getRuleContext(MonthUnspecifiedContext.class,0);
		}
		public DayUnspecifiedContext dayUnspecified() {
			return getRuleContext(DayUnspecifiedContext.class,0);
		}
		public DayAndMonthUnspecifiedContext dayAndMonthUnspecified() {
			return getRuleContext(DayAndMonthUnspecifiedContext.class,0);
		}
		public UnspecifiedDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unspecifiedDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterUnspecifiedDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitUnspecifiedDate(this);
		}
	}

	public final UnspecifiedDateContext unspecifiedDate() throws RecognitionException {
		UnspecifiedDateContext _localctx = new UnspecifiedDateContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_unspecifiedDate);
		try {
			setState(77);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(73);
				yearUnspecified();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(74);
				monthUnspecified();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(75);
				dayUnspecified();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(76);
				dayAndMonthUnspecified();
				}
				break;
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

	public static class MonthUnspecifiedContext extends ParserRuleContext {
		public TerminalNode Year() { return getToken(ExtendedDateTimeFormatParser.Year, 0); }
		public TerminalNode Dash() { return getToken(ExtendedDateTimeFormatParser.Dash, 0); }
		public TerminalNode UU() { return getToken(ExtendedDateTimeFormatParser.UU, 0); }
		public MonthUnspecifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_monthUnspecified; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterMonthUnspecified(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitMonthUnspecified(this);
		}
	}

	public final MonthUnspecifiedContext monthUnspecified() throws RecognitionException {
		MonthUnspecifiedContext _localctx = new MonthUnspecifiedContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_monthUnspecified);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(Year);
			setState(80);
			match(Dash);
			setState(81);
			match(UU);
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

	public static class UncertainOrApproxDateContext extends ParserRuleContext {
		public TerminalNode YearUA() { return getToken(ExtendedDateTimeFormatParser.YearUA, 0); }
		public TerminalNode YearMonthUA() { return getToken(ExtendedDateTimeFormatParser.YearMonthUA, 0); }
		public TerminalNode YearMonthDayUA() { return getToken(ExtendedDateTimeFormatParser.YearMonthDayUA, 0); }
		public UncertainOrApproxDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uncertainOrApproxDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterUncertainOrApproxDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitUncertainOrApproxDate(this);
		}
	}

	public final UncertainOrApproxDateContext uncertainOrApproxDate() throws RecognitionException {
		UncertainOrApproxDateContext _localctx = new UncertainOrApproxDateContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_uncertainOrApproxDate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << YearUA) | (1L << YearMonthUA) | (1L << YearMonthDayUA))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class DayUnspecifiedContext extends ParserRuleContext {
		public TerminalNode YearMonth() { return getToken(ExtendedDateTimeFormatParser.YearMonth, 0); }
		public TerminalNode Dash() { return getToken(ExtendedDateTimeFormatParser.Dash, 0); }
		public TerminalNode UU() { return getToken(ExtendedDateTimeFormatParser.UU, 0); }
		public DayUnspecifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dayUnspecified; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDayUnspecified(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDayUnspecified(this);
		}
	}

	public final DayUnspecifiedContext dayUnspecified() throws RecognitionException {
		DayUnspecifiedContext _localctx = new DayUnspecifiedContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_dayUnspecified);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(YearMonth);
			setState(86);
			match(Dash);
			setState(87);
			match(UU);
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

	public static class DayAndMonthUnspecifiedContext extends ParserRuleContext {
		public TerminalNode Year() { return getToken(ExtendedDateTimeFormatParser.Year, 0); }
		public List<TerminalNode> Dash() { return getTokens(ExtendedDateTimeFormatParser.Dash); }
		public TerminalNode Dash(int i) {
			return getToken(ExtendedDateTimeFormatParser.Dash, i);
		}
		public List<TerminalNode> UU() { return getTokens(ExtendedDateTimeFormatParser.UU); }
		public TerminalNode UU(int i) {
			return getToken(ExtendedDateTimeFormatParser.UU, i);
		}
		public DayAndMonthUnspecifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dayAndMonthUnspecified; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDayAndMonthUnspecified(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDayAndMonthUnspecified(this);
		}
	}

	public final DayAndMonthUnspecifiedContext dayAndMonthUnspecified() throws RecognitionException {
		DayAndMonthUnspecifiedContext _localctx = new DayAndMonthUnspecifiedContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_dayAndMonthUnspecified);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(Year);
			setState(90);
			match(Dash);
			setState(91);
			match(UU);
			setState(92);
			match(Dash);
			setState(93);
			match(UU);
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

	public static class YearUnspecifiedContext extends ParserRuleContext {
		public TerminalNode YearWithOneUnspecifedDigit() { return getToken(ExtendedDateTimeFormatParser.YearWithOneUnspecifedDigit, 0); }
		public TerminalNode YearWithTwoUnspecifedDigits() { return getToken(ExtendedDateTimeFormatParser.YearWithTwoUnspecifedDigits, 0); }
		public YearUnspecifiedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yearUnspecified; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterYearUnspecified(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitYearUnspecified(this);
		}
	}

	public final YearUnspecifiedContext yearUnspecified() throws RecognitionException {
		YearUnspecifiedContext _localctx = new YearUnspecifiedContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_yearUnspecified);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			_la = _input.LA(1);
			if ( !(_la==YearWithOneUnspecifedDigit || _la==YearWithTwoUnspecifedDigits) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class OpenContext extends ParserRuleContext {
		public TerminalNode Open() { return getToken(ExtendedDateTimeFormatParser.Open, 0); }
		public OpenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_open; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterOpen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitOpen(this);
		}
	}

	public final OpenContext open() throws RecognitionException {
		OpenContext _localctx = new OpenContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_open);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(Open);
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

	public static class SeasonContext extends ParserRuleContext {
		public TerminalNode Season() { return getToken(ExtendedDateTimeFormatParser.Season, 0); }
		public SeasonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_season; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterSeason(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitSeason(this);
		}
	}

	public final SeasonContext season() throws RecognitionException {
		SeasonContext _localctx = new SeasonContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_season);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			match(Season);
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

	public static class Level1IntervalContext extends ParserRuleContext {
		public List<DateOrSeasonContext> dateOrSeason() {
			return getRuleContexts(DateOrSeasonContext.class);
		}
		public DateOrSeasonContext dateOrSeason(int i) {
			return getRuleContext(DateOrSeasonContext.class,i);
		}
		public TerminalNode Slash() { return getToken(ExtendedDateTimeFormatParser.Slash, 0); }
		public OpenContext open() {
			return getRuleContext(OpenContext.class,0);
		}
		public UnknownBeginningContext unknownBeginning() {
			return getRuleContext(UnknownBeginningContext.class,0);
		}
		public UnknownEndContext unknownEnd() {
			return getRuleContext(UnknownEndContext.class,0);
		}
		public Level1IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_level1Interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterLevel1Interval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitLevel1Interval(this);
		}
	}

	public final Level1IntervalContext level1Interval() throws RecognitionException {
		Level1IntervalContext _localctx = new Level1IntervalContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_level1Interval);
		try {
			setState(115);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(101);
				dateOrSeason();
				setState(102);
				match(Slash);
				setState(103);
				open();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(105);
				open();
				setState(106);
				match(Slash);
				setState(107);
				dateOrSeason();
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(109);
				dateOrSeason();
				setState(110);
				match(Slash);
				setState(111);
				dateOrSeason();
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(113);
				unknownBeginning();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(114);
				unknownEnd();
				}
				break;
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

	public static class DateOrSeasonContext extends ParserRuleContext {
		public TerminalNode YearUA() { return getToken(ExtendedDateTimeFormatParser.YearUA, 0); }
		public TerminalNode Year() { return getToken(ExtendedDateTimeFormatParser.Year, 0); }
		public TerminalNode SeasonUA() { return getToken(ExtendedDateTimeFormatParser.SeasonUA, 0); }
		public TerminalNode Season() { return getToken(ExtendedDateTimeFormatParser.Season, 0); }
		public TerminalNode YearMonthUA() { return getToken(ExtendedDateTimeFormatParser.YearMonthUA, 0); }
		public TerminalNode YearMonth() { return getToken(ExtendedDateTimeFormatParser.YearMonth, 0); }
		public TerminalNode YearMonthDayUA() { return getToken(ExtendedDateTimeFormatParser.YearMonthDayUA, 0); }
		public TerminalNode YearMonthDay() { return getToken(ExtendedDateTimeFormatParser.YearMonthDay, 0); }
		public DateOrSeasonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateOrSeason; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterDateOrSeason(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitDateOrSeason(this);
		}
	}

	public final DateOrSeasonContext dateOrSeason() throws RecognitionException {
		DateOrSeasonContext _localctx = new DateOrSeasonContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_dateOrSeason);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Year) | (1L << YearMonth) | (1L << YearMonthDay) | (1L << YearUA) | (1L << SeasonUA) | (1L << YearMonthUA) | (1L << YearMonthDayUA) | (1L << Season))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class UnknownBeginningContext extends ParserRuleContext {
		public TerminalNode Slash() { return getToken(ExtendedDateTimeFormatParser.Slash, 0); }
		public DateOrSeasonContext dateOrSeason() {
			return getRuleContext(DateOrSeasonContext.class,0);
		}
		public UnknownBeginningContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownBeginning; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterUnknownBeginning(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitUnknownBeginning(this);
		}
	}

	public final UnknownBeginningContext unknownBeginning() throws RecognitionException {
		UnknownBeginningContext _localctx = new UnknownBeginningContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_unknownBeginning);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(119);
			match(Slash);
			setState(120);
			dateOrSeason();
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

	public static class UnknownEndContext extends ParserRuleContext {
		public DateOrSeasonContext dateOrSeason() {
			return getRuleContext(DateOrSeasonContext.class,0);
		}
		public TerminalNode Slash() { return getToken(ExtendedDateTimeFormatParser.Slash, 0); }
		public UnknownEndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unknownEnd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).enterUnknownEnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExtendedDateTimeFormatListener ) ((ExtendedDateTimeFormatListener)listener).exitUnknownEnd(this);
		}
	}

	public final UnknownEndContext unknownEnd() throws RecognitionException {
		UnknownEndContext _localctx = new UnknownEndContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_unknownEnd);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(122);
			dateOrSeason();
			setState(123);
			match(Slash);
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

	public static final String _serializedATN =
		"\u0004\u0001.~\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002"+
		"\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005"+
		"\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007"+
		"\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007"+
		"\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002"+
		"\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0001"+
		"\u0000\u0001\u0000\u0003\u0000)\b\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u00011\b\u0001\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u00039\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006H\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007N\b\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000ft\b\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0000\u0000\u0013"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$\u0000\u0004\u0002\u0000\u0007\u0007\u000f\u0010\u0002"+
		"\u0000\'\')*\u0001\u0000-.\u0003\u0000\u0007\u0007\u000f\u0010\'+y\u0000"+
		"(\u0001\u0000\u0000\u0000\u00020\u0001\u0000\u0000\u0000\u00042\u0001"+
		"\u0000\u0000\u0000\u00064\u0001\u0000\u0000\u0000\b:\u0001\u0000\u0000"+
		"\u0000\n?\u0001\u0000\u0000\u0000\fG\u0001\u0000\u0000\u0000\u000eM\u0001"+
		"\u0000\u0000\u0000\u0010O\u0001\u0000\u0000\u0000\u0012S\u0001\u0000\u0000"+
		"\u0000\u0014U\u0001\u0000\u0000\u0000\u0016Y\u0001\u0000\u0000\u0000\u0018"+
		"_\u0001\u0000\u0000\u0000\u001aa\u0001\u0000\u0000\u0000\u001cc\u0001"+
		"\u0000\u0000\u0000\u001es\u0001\u0000\u0000\u0000 u\u0001\u0000\u0000"+
		"\u0000\"w\u0001\u0000\u0000\u0000$z\u0001\u0000\u0000\u0000&)\u0003\u0002"+
		"\u0001\u0000\')\u0003\f\u0006\u0000(&\u0001\u0000\u0000\u0000(\'\u0001"+
		"\u0000\u0000\u0000)*\u0001\u0000\u0000\u0000*+\u0005\u0000\u0000\u0001"+
		"+\u0001\u0001\u0000\u0000\u0000,1\u0003\u0004\u0002\u0000-1\u0003\u0006"+
		"\u0003\u0000.1\u0003\b\u0004\u0000/1\u0003\n\u0005\u00000,\u0001\u0000"+
		"\u0000\u00000-\u0001\u0000\u0000\u00000.\u0001\u0000\u0000\u00000/\u0001"+
		"\u0000\u0000\u00001\u0003\u0001\u0000\u0000\u000023\u0007\u0000\u0000"+
		"\u00003\u0005\u0001\u0000\u0000\u000045\u0005\u0010\u0000\u000056\u0005"+
		"\u0001\u0000\u000068\u0005\u001e\u0000\u000079\u0005\u001f\u0000\u0000"+
		"87\u0001\u0000\u0000\u000089\u0001\u0000\u0000\u00009\u0007\u0001\u0000"+
		"\u0000\u0000:;\u0005\u0010\u0000\u0000;<\u0005\u0001\u0000\u0000<=\u0005"+
		"\u001e\u0000\u0000=>\u0005\u0002\u0000\u0000>\t\u0001\u0000\u0000\u0000"+
		"?@\u0003\u0004\u0002\u0000@A\u0005\u0006\u0000\u0000AB\u0003\u0004\u0002"+
		"\u0000B\u000b\u0001\u0000\u0000\u0000CH\u0003\u0012\t\u0000DH\u0003\u000e"+
		"\u0007\u0000EH\u0003\u001e\u000f\u0000FH\u0003\u001c\u000e\u0000GC\u0001"+
		"\u0000\u0000\u0000GD\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000"+
		"GF\u0001\u0000\u0000\u0000H\r\u0001\u0000\u0000\u0000IN\u0003\u0018\f"+
		"\u0000JN\u0003\u0010\b\u0000KN\u0003\u0014\n\u0000LN\u0003\u0016\u000b"+
		"\u0000MI\u0001\u0000\u0000\u0000MJ\u0001\u0000\u0000\u0000MK\u0001\u0000"+
		"\u0000\u0000ML\u0001\u0000\u0000\u0000N\u000f\u0001\u0000\u0000\u0000"+
		"OP\u0005\u0007\u0000\u0000PQ\u0005\u0003\u0000\u0000QR\u0005\"\u0000\u0000"+
		"R\u0011\u0001\u0000\u0000\u0000ST\u0007\u0001\u0000\u0000T\u0013\u0001"+
		"\u0000\u0000\u0000UV\u0005\u000f\u0000\u0000VW\u0005\u0003\u0000\u0000"+
		"WX\u0005\"\u0000\u0000X\u0015\u0001\u0000\u0000\u0000YZ\u0005\u0007\u0000"+
		"\u0000Z[\u0005\u0003\u0000\u0000[\\\u0005\"\u0000\u0000\\]\u0005\u0003"+
		"\u0000\u0000]^\u0005\"\u0000\u0000^\u0017\u0001\u0000\u0000\u0000_`\u0007"+
		"\u0002\u0000\u0000`\u0019\u0001\u0000\u0000\u0000ab\u0005$\u0000\u0000"+
		"b\u001b\u0001\u0000\u0000\u0000cd\u0005+\u0000\u0000d\u001d\u0001\u0000"+
		"\u0000\u0000ef\u0003 \u0010\u0000fg\u0005\u0006\u0000\u0000gh\u0003\u001a"+
		"\r\u0000ht\u0001\u0000\u0000\u0000ij\u0003\u001a\r\u0000jk\u0005\u0006"+
		"\u0000\u0000kl\u0003 \u0010\u0000lt\u0001\u0000\u0000\u0000mn\u0003 \u0010"+
		"\u0000no\u0005\u0006\u0000\u0000op\u0003 \u0010\u0000pt\u0001\u0000\u0000"+
		"\u0000qt\u0003\"\u0011\u0000rt\u0003$\u0012\u0000se\u0001\u0000\u0000"+
		"\u0000si\u0001\u0000\u0000\u0000sm\u0001\u0000\u0000\u0000sq\u0001\u0000"+
		"\u0000\u0000sr\u0001\u0000\u0000\u0000t\u001f\u0001\u0000\u0000\u0000"+
		"uv\u0007\u0003\u0000\u0000v!\u0001\u0000\u0000\u0000wx\u0005\u0006\u0000"+
		"\u0000xy\u0003 \u0010\u0000y#\u0001\u0000\u0000\u0000z{\u0003 \u0010\u0000"+
		"{|\u0005\u0006\u0000\u0000|%\u0001\u0000\u0000\u0000\u0006(08GMs";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}