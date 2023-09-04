package de.sub.goobi.validator;
import org.antlr.v4.runtime.CharStream;
// Generated from ExtendedDateTimeFormat.g4 by ANTLR 4.13.0
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ExtendedDateTimeFormatLexer extends Lexer {
    static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
    T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9,
    T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T=17,
    Z=18, X=19, E=20, S=21, LONGYEAR=22, DOTS=23, UNKNOWN=24, UA=25;
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };

    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    private static String[] makeRuleNames() {
        return new String[] {
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T", "Z",
                "X", "E", "S", "LONGYEAR", "DOTS", "UNKNOWN", "UA"
        };
    }
    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] {
                null, "'\\r'", "'\\n'", "':'", "'2'", "'4'", "'0'", "'-'", "'+'", "'1'",
                "'/'", "'3'", "'5'", "'6'", "'7'", "'8'", "'9'", "'T'", "'Z'", "'X'",
                "'E'", "'S'", "'Y'"
        };
    }
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static String[] makeSymbolicNames() {
        return new String[] {
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, "T", "Z", "X", "E", "S", "LONGYEAR", "DOTS",
                "UNKNOWN", "UA"
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


    public ExtendedDateTimeFormatLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
    }

    @Override
    public String getGrammarFileName() { return "ExtendedDateTimeFormat.g4"; }

    @Override
    public String[] getRuleNames() { return ruleNames; }

    @Override
    public String getSerializedATN() { return _serializedATN; }

    @Override
    public String[] getChannelNames() { return channelNames; }

    @Override
    public String[] getModeNames() { return modeNames; }

    @Override
    public ATN getATN() { return _ATN; }

    public static final String _serializedATN =
            "\u0004\u0000\u0019l\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
                    "\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
                    "\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
                    "\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
                    "\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
                    "\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
                    "\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
                    "\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
                    "\u0018\u0007\u0018\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
                    "\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
                    "\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
                    "\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
                    "\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f"+
                    "\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
                    "\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015"+
                    "\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
                    "\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018"+
                    "\u0001\u0018\u0000\u0000\u0019\u0001\u0001\u0003\u0002\u0005\u0003\u0007"+
                    "\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b"+
                    "\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013"+
                    "\'\u0014)\u0015+\u0016-\u0017/\u00181\u0019\u0001\u0000\u0001\u0003\u0000"+
                    "%%??~~k\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000"+
                    "\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000"+
                    "\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000"+
                    "\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000"+
                    "\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000"+
                    "\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000"+
                    "\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000"+
                    "\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000"+
                    "!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001"+
                    "\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000"+
                    "\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000\u0000"+
                    "\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u00013"+
                    "\u0001\u0000\u0000\u0000\u00035\u0001\u0000\u0000\u0000\u00057\u0001\u0000"+
                    "\u0000\u0000\u00079\u0001\u0000\u0000\u0000\t;\u0001\u0000\u0000\u0000"+
                    "\u000b=\u0001\u0000\u0000\u0000\r?\u0001\u0000\u0000\u0000\u000fA\u0001"+
                    "\u0000\u0000\u0000\u0011C\u0001\u0000\u0000\u0000\u0013E\u0001\u0000\u0000"+
                    "\u0000\u0015G\u0001\u0000\u0000\u0000\u0017I\u0001\u0000\u0000\u0000\u0019"+
                    "K\u0001\u0000\u0000\u0000\u001bM\u0001\u0000\u0000\u0000\u001dO\u0001"+
                    "\u0000\u0000\u0000\u001fQ\u0001\u0000\u0000\u0000!S\u0001\u0000\u0000"+
                    "\u0000#U\u0001\u0000\u0000\u0000%W\u0001\u0000\u0000\u0000\'Y\u0001\u0000"+
                    "\u0000\u0000)[\u0001\u0000\u0000\u0000+]\u0001\u0000\u0000\u0000-_\u0001"+
                    "\u0000\u0000\u0000/b\u0001\u0000\u0000\u00001j\u0001\u0000\u0000\u0000"+
                    "34\u0005\r\u0000\u00004\u0002\u0001\u0000\u0000\u000056\u0005\n\u0000"+
                    "\u00006\u0004\u0001\u0000\u0000\u000078\u0005:\u0000\u00008\u0006\u0001"+
                    "\u0000\u0000\u00009:\u00052\u0000\u0000:\b\u0001\u0000\u0000\u0000;<\u0005"+
                    "4\u0000\u0000<\n\u0001\u0000\u0000\u0000=>\u00050\u0000\u0000>\f\u0001"+
                    "\u0000\u0000\u0000?@\u0005-\u0000\u0000@\u000e\u0001\u0000\u0000\u0000"+
                    "AB\u0005+\u0000\u0000B\u0010\u0001\u0000\u0000\u0000CD\u00051\u0000\u0000"+
                    "D\u0012\u0001\u0000\u0000\u0000EF\u0005/\u0000\u0000F\u0014\u0001\u0000"+
                    "\u0000\u0000GH\u00053\u0000\u0000H\u0016\u0001\u0000\u0000\u0000IJ\u0005"+
                    "5\u0000\u0000J\u0018\u0001\u0000\u0000\u0000KL\u00056\u0000\u0000L\u001a"+
                    "\u0001\u0000\u0000\u0000MN\u00057\u0000\u0000N\u001c\u0001\u0000\u0000"+
                    "\u0000OP\u00058\u0000\u0000P\u001e\u0001\u0000\u0000\u0000QR\u00059\u0000"+
                    "\u0000R \u0001\u0000\u0000\u0000ST\u0005T\u0000\u0000T\"\u0001\u0000\u0000"+
                    "\u0000UV\u0005Z\u0000\u0000V$\u0001\u0000\u0000\u0000WX\u0005X\u0000\u0000"+
                    "X&\u0001\u0000\u0000\u0000YZ\u0005E\u0000\u0000Z(\u0001\u0000\u0000\u0000"+
                    "[\\\u0005S\u0000\u0000\\*\u0001\u0000\u0000\u0000]^\u0005Y\u0000\u0000"+
                    "^,\u0001\u0000\u0000\u0000_`\u0005.\u0000\u0000`a\u0005.\u0000\u0000a"+
                    ".\u0001\u0000\u0000\u0000bc\u0005u\u0000\u0000cd\u0005n\u0000\u0000de"+
                    "\u0005k\u0000\u0000ef\u0005n\u0000\u0000fg\u0005o\u0000\u0000gh\u0005"+
                    "w\u0000\u0000hi\u0005n\u0000\u0000i0\u0001\u0000\u0000\u0000jk\u0007\u0000"+
                    "\u0000\u0000k2\u0001\u0000\u0000\u0000\u0001\u0000\u0000";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}