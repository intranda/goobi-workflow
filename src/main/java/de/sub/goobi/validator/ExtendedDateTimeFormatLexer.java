// Generated from ExtendedDateTimeFormat.g4 by ANTLR 4.13.2
package de.sub.goobi.validator;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape" })
public class ExtendedDateTimeFormatLexer extends Lexer {
    static {
        RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T = 23, Z = 24, X = 25, E = 26,
            S = 27, LONGYEAR = 28, DOTS = 29, UNKNOWN = 30, UA = 31, COMMA = 32;
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };

    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    private static String[] makeRuleNames() {
        return new String[] {
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16",
                "T__17", "T__18", "T__19", "T__20", "T__21", "T", "Z", "X", "E", "S",
                "LONGYEAR", "DOTS", "UNKNOWN", "UA", "COMMA"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] {
                null, "'\\r'", "'\\n'", "':'", "'2'", "'4'", "'0'", "'-'", "'+'", "'1'",
                "'/'", "'3'", "'5'", "'6'", "'7'", "'8'", "'9'", "'[]'", "'['", "']'",
                "'{}'", "'{'", "'}'", "'T'", "'Z'", "'X'", "'E'", "'S'", "'Y'", null,
                null, null, "','"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] {
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, "T",
                "Z", "X", "E", "S", "LONGYEAR", "DOTS", "UNKNOWN", "UA", "COMMA"
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
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    public String getGrammarFileName() {
        return "ExtendedDateTimeFormat.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public static final String _serializedATN =
            "\u0004\u0000 \u008a\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001" +
                    "\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004" +
                    "\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007" +
                    "\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b" +
                    "\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002" +
                    "\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002" +
                    "\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002" +
                    "\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002" +
                    "\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002" +
                    "\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002" +
                    "\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0001\u0000\u0001\u0000\u0001" +
                    "\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001" +
                    "\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001" +
                    "\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001" +
                    "\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e" +
                    "\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011" +
                    "\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013" +
                    "\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016" +
                    "\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019" +
                    "\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c" +
                    "\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d" +
                    "\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001f" +
                    "\u0001\u001f\u0000\u0000 \u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004" +
                    "\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017" +
                    "\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'" +
                    "\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a5\u001b7\u001c9\u001d" +
                    ";\u001e=\u001f? \u0001\u0000\u0001\u0003\u0000%%??~~\u0089\u0000\u0001" +
                    "\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005" +
                    "\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001" +
                    "\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000" +
                    "\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000" +
                    "\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000" +
                    "\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000" +
                    "\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000" +
                    "\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000" +
                    "\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000" +
                    "\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001" +
                    "\u0000\u0000\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000" +
                    "\u0000\u00001\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u0000" +
                    "5\u0001\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u00009\u0001" +
                    "\u0000\u0000\u0000\u0000;\u0001\u0000\u0000\u0000\u0000=\u0001\u0000\u0000" +
                    "\u0000\u0000?\u0001\u0000\u0000\u0000\u0001A\u0001\u0000\u0000\u0000\u0003" +
                    "C\u0001\u0000\u0000\u0000\u0005E\u0001\u0000\u0000\u0000\u0007G\u0001" +
                    "\u0000\u0000\u0000\tI\u0001\u0000\u0000\u0000\u000bK\u0001\u0000\u0000" +
                    "\u0000\rM\u0001\u0000\u0000\u0000\u000fO\u0001\u0000\u0000\u0000\u0011" +
                    "Q\u0001\u0000\u0000\u0000\u0013S\u0001\u0000\u0000\u0000\u0015U\u0001" +
                    "\u0000\u0000\u0000\u0017W\u0001\u0000\u0000\u0000\u0019Y\u0001\u0000\u0000" +
                    "\u0000\u001b[\u0001\u0000\u0000\u0000\u001d]\u0001\u0000\u0000\u0000\u001f" +
                    "_\u0001\u0000\u0000\u0000!a\u0001\u0000\u0000\u0000#d\u0001\u0000\u0000" +
                    "\u0000%f\u0001\u0000\u0000\u0000\'h\u0001\u0000\u0000\u0000)k\u0001\u0000" +
                    "\u0000\u0000+m\u0001\u0000\u0000\u0000-o\u0001\u0000\u0000\u0000/q\u0001" +
                    "\u0000\u0000\u00001s\u0001\u0000\u0000\u00003u\u0001\u0000\u0000\u0000" +
                    "5w\u0001\u0000\u0000\u00007y\u0001\u0000\u0000\u00009{\u0001\u0000\u0000" +
                    "\u0000;~\u0001\u0000\u0000\u0000=\u0086\u0001\u0000\u0000\u0000?\u0088" +
                    "\u0001\u0000\u0000\u0000AB\u0005\r\u0000\u0000B\u0002\u0001\u0000\u0000" +
                    "\u0000CD\u0005\n\u0000\u0000D\u0004\u0001\u0000\u0000\u0000EF\u0005:\u0000" +
                    "\u0000F\u0006\u0001\u0000\u0000\u0000GH\u00052\u0000\u0000H\b\u0001\u0000" +
                    "\u0000\u0000IJ\u00054\u0000\u0000J\n\u0001\u0000\u0000\u0000KL\u00050" +
                    "\u0000\u0000L\f\u0001\u0000\u0000\u0000MN\u0005-\u0000\u0000N\u000e\u0001" +
                    "\u0000\u0000\u0000OP\u0005+\u0000\u0000P\u0010\u0001\u0000\u0000\u0000" +
                    "QR\u00051\u0000\u0000R\u0012\u0001\u0000\u0000\u0000ST\u0005/\u0000\u0000" +
                    "T\u0014\u0001\u0000\u0000\u0000UV\u00053\u0000\u0000V\u0016\u0001\u0000" +
                    "\u0000\u0000WX\u00055\u0000\u0000X\u0018\u0001\u0000\u0000\u0000YZ\u0005" +
                    "6\u0000\u0000Z\u001a\u0001\u0000\u0000\u0000[\\\u00057\u0000\u0000\\\u001c" +
                    "\u0001\u0000\u0000\u0000]^\u00058\u0000\u0000^\u001e\u0001\u0000\u0000" +
                    "\u0000_`\u00059\u0000\u0000` \u0001\u0000\u0000\u0000ab\u0005[\u0000\u0000" +
                    "bc\u0005]\u0000\u0000c\"\u0001\u0000\u0000\u0000de\u0005[\u0000\u0000" +
                    "e$\u0001\u0000\u0000\u0000fg\u0005]\u0000\u0000g&\u0001\u0000\u0000\u0000" +
                    "hi\u0005{\u0000\u0000ij\u0005}\u0000\u0000j(\u0001\u0000\u0000\u0000k" +
                    "l\u0005{\u0000\u0000l*\u0001\u0000\u0000\u0000mn\u0005}\u0000\u0000n," +
                    "\u0001\u0000\u0000\u0000op\u0005T\u0000\u0000p.\u0001\u0000\u0000\u0000" +
                    "qr\u0005Z\u0000\u0000r0\u0001\u0000\u0000\u0000st\u0005X\u0000\u0000t" +
                    "2\u0001\u0000\u0000\u0000uv\u0005E\u0000\u0000v4\u0001\u0000\u0000\u0000" +
                    "wx\u0005S\u0000\u0000x6\u0001\u0000\u0000\u0000yz\u0005Y\u0000\u0000z" +
                    "8\u0001\u0000\u0000\u0000{|\u0005.\u0000\u0000|}\u0005.\u0000\u0000}:" +
                    "\u0001\u0000\u0000\u0000~\u007f\u0005u\u0000\u0000\u007f\u0080\u0005n" +
                    "\u0000\u0000\u0080\u0081\u0005k\u0000\u0000\u0081\u0082\u0005n\u0000\u0000" +
                    "\u0082\u0083\u0005o\u0000\u0000\u0083\u0084\u0005w\u0000\u0000\u0084\u0085" +
                    "\u0005n\u0000\u0000\u0085<\u0001\u0000\u0000\u0000\u0086\u0087\u0007\u0000" +
                    "\u0000\u0000\u0087>\u0001\u0000\u0000\u0000\u0088\u0089\u0005,\u0000\u0000" +
                    "\u0089@\u0001\u0000\u0000\u0000\u0001\u0000\u0000";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}