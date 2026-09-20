// Generated from lv426/compiler/parser/HopeLang.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class HopeLangParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PROGRAM=1, CONST=2, VAR=3, STRUCT=4, ENUM=5, EVENT=6, DEF=7, ON=8, START=9, 
		EVERY=10, AT=11, EMIT=12, IF=13, ELIF=14, ELSE=15, WHILE=16, FOR=17, FROM=18, 
		TO=19, STEP=20, BREAK=21, CONTINUE=22, RETURN=23, OF=24, END=25, LIST=26, 
		TRUE=27, FALSE=28, AND=29, OR=30, NOT=31, INT_TYPE=32, REAL_TYPE=33, BOOL_TYPE=34, 
		STRING_TYPE=35, TIME_TYPE=36, VOID_TYPE=37, MILLISECOND_UNIT=38, SECOND_UNIT=39, 
		MINUTE_UNIT=40, HOUR_UNIT=41, DAY_UNIT=42, PLUS_ASSIGN=43, MINUS_ASSIGN=44, 
		MUL_ASSIGN=45, DIV_ASSIGN=46, MOD_ASSIGN=47, EQ=48, NEQ=49, LTE=50, GTE=51, 
		LT=52, GT=53, ASSIGN=54, PLUS=55, MINUS=56, MUL=57, DIV=58, MOD=59, DOT=60, 
		COMMA=61, SEMI=62, LPAREN=63, RPAREN=64, LBRACK=65, RBRACK=66, REAL_LITERAL=67, 
		INT_LITERAL=68, STRING_LITERAL=69, ID=70, LINE_COMMENT=71, BLOCK_COMMENT=72, 
		WS=73;
	public static final int
		RULE_source = 0, RULE_programDecl = 1, RULE_topLevelDecl = 2, RULE_constDecl = 3, 
		RULE_globalVarDecl = 4, RULE_structDecl = 5, RULE_fieldDecl = 6, RULE_enumDecl = 7, 
		RULE_enumMember = 8, RULE_eventDecl = 9, RULE_startHandler = 10, RULE_eventHandler = 11, 
		RULE_everyHandler = 12, RULE_atHandler = 13, RULE_identifierList = 14, 
		RULE_functionDecl = 15, RULE_parameterList = 16, RULE_parameter = 17, 
		RULE_typeRef = 18, RULE_typeAtom = 19, RULE_primitiveType = 20, RULE_arraySize = 21, 
		RULE_statement = 22, RULE_localVarDecl = 23, RULE_assignmentStatement = 24, 
		RULE_assignmentOperator = 25, RULE_lvalue = 26, RULE_lvalueSuffix = 27, 
		RULE_ifStatement = 28, RULE_whileStatement = 29, RULE_forStatement = 30, 
		RULE_breakStatement = 31, RULE_continueStatement = 32, RULE_returnStatement = 33, 
		RULE_emitStatement = 34, RULE_expressionStatement = 35, RULE_expression = 36, 
		RULE_logicalOr = 37, RULE_logicalAnd = 38, RULE_equality = 39, RULE_comparison = 40, 
		RULE_additive = 41, RULE_multiplicative = 42, RULE_unary = 43, RULE_postfix = 44, 
		RULE_postfixSuffix = 45, RULE_primary = 46, RULE_argumentList = 47, RULE_literal = 48, 
		RULE_durationLiteral = 49, RULE_timeUnit = 50;
	private static String[] makeRuleNames() {
		return new String[] {
			"source", "programDecl", "topLevelDecl", "constDecl", "globalVarDecl", 
			"structDecl", "fieldDecl", "enumDecl", "enumMember", "eventDecl", "startHandler", 
			"eventHandler", "everyHandler", "atHandler", "identifierList", "functionDecl", 
			"parameterList", "parameter", "typeRef", "typeAtom", "primitiveType", 
			"arraySize", "statement", "localVarDecl", "assignmentStatement", "assignmentOperator", 
			"lvalue", "lvalueSuffix", "ifStatement", "whileStatement", "forStatement", 
			"breakStatement", "continueStatement", "returnStatement", "emitStatement", 
			"expressionStatement", "expression", "logicalOr", "logicalAnd", "equality", 
			"comparison", "additive", "multiplicative", "unary", "postfix", "postfixSuffix", 
			"primary", "argumentList", "literal", "durationLiteral", "timeUnit"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'program'", "'const'", "'var'", "'struct'", "'enum'", "'event'", 
			"'def'", "'on'", "'start'", "'every'", "'at'", "'emit'", "'if'", "'elif'", 
			"'else'", "'while'", "'for'", "'from'", "'to'", "'step'", "'break'", 
			"'continue'", "'return'", "'of'", "'end'", "'list'", "'true'", "'false'", 
			"'and'", "'or'", "'not'", "'int'", "'real'", "'bool'", "'string'", "'time'", 
			"'void'", "'ms'", "'sec'", "'min'", "'hour'", "'day'", "'+='", "'-='", 
			"'*='", "'/='", "'%='", "'=='", "'!='", "'<='", "'>='", "'<'", "'>'", 
			"'='", "'+'", "'-'", "'*'", "'/'", "'%'", "'.'", "','", "';'", "'('", 
			"')'", "'['", "']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PROGRAM", "CONST", "VAR", "STRUCT", "ENUM", "EVENT", "DEF", "ON", 
			"START", "EVERY", "AT", "EMIT", "IF", "ELIF", "ELSE", "WHILE", "FOR", 
			"FROM", "TO", "STEP", "BREAK", "CONTINUE", "RETURN", "OF", "END", "LIST", 
			"TRUE", "FALSE", "AND", "OR", "NOT", "INT_TYPE", "REAL_TYPE", "BOOL_TYPE", 
			"STRING_TYPE", "TIME_TYPE", "VOID_TYPE", "MILLISECOND_UNIT", "SECOND_UNIT", 
			"MINUTE_UNIT", "HOUR_UNIT", "DAY_UNIT", "PLUS_ASSIGN", "MINUS_ASSIGN", 
			"MUL_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN", "EQ", "NEQ", "LTE", "GTE", 
			"LT", "GT", "ASSIGN", "PLUS", "MINUS", "MUL", "DIV", "MOD", "DOT", "COMMA", 
			"SEMI", "LPAREN", "RPAREN", "LBRACK", "RBRACK", "REAL_LITERAL", "INT_LITERAL", 
			"STRING_LITERAL", "ID", "LINE_COMMENT", "BLOCK_COMMENT", "WS"
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
	public String getGrammarFileName() { return "HopeLang.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public HopeLangParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SourceContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(HopeLangParser.EOF, 0); }
		public ProgramDeclContext programDecl() {
			return getRuleContext(ProgramDeclContext.class,0);
		}
		public List<TopLevelDeclContext> topLevelDecl() {
			return getRuleContexts(TopLevelDeclContext.class);
		}
		public TopLevelDeclContext topLevelDecl(int i) {
			return getRuleContext(TopLevelDeclContext.class,i);
		}
		public SourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_source; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitSource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SourceContext source() throws RecognitionException {
		SourceContext _localctx = new SourceContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_source);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PROGRAM) {
				{
				setState(102);
				programDecl();
				}
			}

			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3580L) != 0)) {
				{
				{
				setState(105);
				topLevelDecl();
				}
				}
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(111);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramDeclContext extends ParserRuleContext {
		public TerminalNode PROGRAM() { return getToken(HopeLangParser.PROGRAM, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public ProgramDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_programDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitProgramDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramDeclContext programDecl() throws RecognitionException {
		ProgramDeclContext _localctx = new ProgramDeclContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_programDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			match(PROGRAM);
			setState(114);
			match(ID);
			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(115);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TopLevelDeclContext extends ParserRuleContext {
		public ConstDeclContext constDecl() {
			return getRuleContext(ConstDeclContext.class,0);
		}
		public GlobalVarDeclContext globalVarDecl() {
			return getRuleContext(GlobalVarDeclContext.class,0);
		}
		public StructDeclContext structDecl() {
			return getRuleContext(StructDeclContext.class,0);
		}
		public EnumDeclContext enumDecl() {
			return getRuleContext(EnumDeclContext.class,0);
		}
		public EventDeclContext eventDecl() {
			return getRuleContext(EventDeclContext.class,0);
		}
		public FunctionDeclContext functionDecl() {
			return getRuleContext(FunctionDeclContext.class,0);
		}
		public StartHandlerContext startHandler() {
			return getRuleContext(StartHandlerContext.class,0);
		}
		public EventHandlerContext eventHandler() {
			return getRuleContext(EventHandlerContext.class,0);
		}
		public EveryHandlerContext everyHandler() {
			return getRuleContext(EveryHandlerContext.class,0);
		}
		public AtHandlerContext atHandler() {
			return getRuleContext(AtHandlerContext.class,0);
		}
		public TopLevelDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topLevelDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitTopLevelDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopLevelDeclContext topLevelDecl() throws RecognitionException {
		TopLevelDeclContext _localctx = new TopLevelDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_topLevelDecl);
		try {
			setState(128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(118);
				constDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(119);
				globalVarDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(120);
				structDecl();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(121);
				enumDecl();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(122);
				eventDecl();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(123);
				functionDecl();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(124);
				startHandler();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(125);
				eventHandler();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(126);
				everyHandler();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(127);
				atHandler();
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

	@SuppressWarnings("CheckReturnValue")
	public static class ConstDeclContext extends ParserRuleContext {
		public TerminalNode CONST() { return getToken(HopeLangParser.CONST, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(HopeLangParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public ConstDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitConstDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstDeclContext constDecl() throws RecognitionException {
		ConstDeclContext _localctx = new ConstDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_constDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(CONST);
			setState(131);
			match(ID);
			setState(132);
			match(OF);
			setState(133);
			typeRef();
			setState(134);
			match(ASSIGN);
			setState(135);
			expression();
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(136);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class GlobalVarDeclContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(HopeLangParser.VAR, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(HopeLangParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public GlobalVarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globalVarDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitGlobalVarDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalVarDeclContext globalVarDecl() throws RecognitionException {
		GlobalVarDeclContext _localctx = new GlobalVarDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_globalVarDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(VAR);
			setState(140);
			match(ID);
			setState(141);
			match(OF);
			setState(142);
			typeRef();
			setState(145);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(143);
				match(ASSIGN);
				setState(144);
				expression();
				}
			}

			setState(148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(147);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class StructDeclContext extends ParserRuleContext {
		public TerminalNode STRUCT() { return getToken(HopeLangParser.STRUCT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<FieldDeclContext> fieldDecl() {
			return getRuleContexts(FieldDeclContext.class);
		}
		public FieldDeclContext fieldDecl(int i) {
			return getRuleContext(FieldDeclContext.class,i);
		}
		public StructDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitStructDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructDeclContext structDecl() throws RecognitionException {
		StructDeclContext _localctx = new StructDeclContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_structDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			match(STRUCT);
			setState(151);
			match(ID);
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(152);
				fieldDecl();
				}
				}
				setState(157);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(158);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FieldDeclContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(HopeLangParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public FieldDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitFieldDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDeclContext fieldDecl() throws RecognitionException {
		FieldDeclContext _localctx = new FieldDeclContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fieldDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(ID);
			setState(161);
			match(OF);
			setState(162);
			typeRef();
			setState(165);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(163);
				match(ASSIGN);
				setState(164);
				expression();
				}
			}

			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(167);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EnumDeclContext extends ParserRuleContext {
		public TerminalNode ENUM() { return getToken(HopeLangParser.ENUM, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<EnumMemberContext> enumMember() {
			return getRuleContexts(EnumMemberContext.class);
		}
		public EnumMemberContext enumMember(int i) {
			return getRuleContext(EnumMemberContext.class,i);
		}
		public EnumDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEnumDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumDeclContext enumDecl() throws RecognitionException {
		EnumDeclContext _localctx = new EnumDeclContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_enumDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			match(ENUM);
			setState(171);
			match(ID);
			setState(173); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(172);
				enumMember();
				}
				}
				setState(175); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ID );
			setState(177);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EnumMemberContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode COMMA() { return getToken(HopeLangParser.COMMA, 0); }
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public EnumMemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumMember; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEnumMember(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumMemberContext enumMember() throws RecognitionException {
		EnumMemberContext _localctx = new EnumMemberContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_enumMember);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			match(ID);
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA || _la==SEMI) {
				{
				setState(180);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
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

	@SuppressWarnings("CheckReturnValue")
	public static class EventDeclContext extends ParserRuleContext {
		public TerminalNode EVENT() { return getToken(HopeLangParser.EVENT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public EventDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eventDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEventDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EventDeclContext eventDecl() throws RecognitionException {
		EventDeclContext _localctx = new EventDeclContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_eventDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(EVENT);
			setState(184);
			match(ID);
			setState(185);
			match(LPAREN);
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(186);
				parameterList();
				}
			}

			setState(189);
			match(RPAREN);
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(190);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class StartHandlerContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(HopeLangParser.ON, 0); }
		public TerminalNode START() { return getToken(HopeLangParser.START, 0); }
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StartHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_startHandler; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitStartHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartHandlerContext startHandler() throws RecognitionException {
		StartHandlerContext _localctx = new StartHandlerContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_startHandler);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(ON);
			setState(194);
			match(START);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(195);
				statement();
				}
				}
				setState(200);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(201);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EventHandlerContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(HopeLangParser.ON, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public IdentifierListContext identifierList() {
			return getRuleContext(IdentifierListContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public EventHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eventHandler; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEventHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EventHandlerContext eventHandler() throws RecognitionException {
		EventHandlerContext _localctx = new EventHandlerContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_eventHandler);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			match(ON);
			setState(204);
			match(ID);
			setState(205);
			match(LPAREN);
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(206);
				identifierList();
				}
			}

			setState(209);
			match(RPAREN);
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(210);
				statement();
				}
				}
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(216);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EveryHandlerContext extends ParserRuleContext {
		public TerminalNode EVERY() { return getToken(HopeLangParser.EVERY, 0); }
		public DurationLiteralContext durationLiteral() {
			return getRuleContext(DurationLiteralContext.class,0);
		}
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public EveryHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_everyHandler; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEveryHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EveryHandlerContext everyHandler() throws RecognitionException {
		EveryHandlerContext _localctx = new EveryHandlerContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_everyHandler);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(EVERY);
			setState(219);
			durationLiteral();
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(220);
				statement();
				}
				}
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(226);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class AtHandlerContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(HopeLangParser.AT, 0); }
		public DurationLiteralContext durationLiteral() {
			return getRuleContext(DurationLiteralContext.class,0);
		}
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public AtHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atHandler; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitAtHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtHandlerContext atHandler() throws RecognitionException {
		AtHandlerContext _localctx = new AtHandlerContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_atHandler);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			match(AT);
			setState(229);
			durationLiteral();
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(230);
				statement();
				}
				}
				setState(235);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(236);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierListContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(HopeLangParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(HopeLangParser.ID, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HopeLangParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HopeLangParser.COMMA, i);
		}
		public IdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierListContext identifierList() throws RecognitionException {
		IdentifierListContext _localctx = new IdentifierListContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_identifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			match(ID);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(239);
				match(COMMA);
				setState(240);
				match(ID);
				}
				}
				setState(245);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDeclContext extends ParserRuleContext {
		public TerminalNode DEF() { return getToken(HopeLangParser.DEF, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public FunctionDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitFunctionDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDeclContext functionDecl() throws RecognitionException {
		FunctionDeclContext _localctx = new FunctionDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_functionDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(246);
			match(DEF);
			setState(247);
			match(ID);
			setState(248);
			match(LPAREN);
			setState(250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(249);
				parameterList();
				}
			}

			setState(252);
			match(RPAREN);
			setState(253);
			match(OF);
			setState(254);
			typeRef();
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(255);
				statement();
				}
				}
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(261);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterListContext extends ParserRuleContext {
		public List<ParameterContext> parameter() {
			return getRuleContexts(ParameterContext.class);
		}
		public ParameterContext parameter(int i) {
			return getRuleContext(ParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HopeLangParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HopeLangParser.COMMA, i);
		}
		public ParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterListContext parameterList() throws RecognitionException {
		ParameterListContext _localctx = new ParameterListContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_parameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			parameter();
			setState(268);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(264);
				match(COMMA);
				setState(265);
				parameter();
				}
				}
				setState(270);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			match(ID);
			setState(272);
			match(OF);
			setState(273);
			typeRef();
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

	@SuppressWarnings("CheckReturnValue")
	public static class TypeRefContext extends ParserRuleContext {
		public TypeAtomContext typeAtom() {
			return getRuleContext(TypeAtomContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(HopeLangParser.LBRACK, 0); }
		public ArraySizeContext arraySize() {
			return getRuleContext(ArraySizeContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(HopeLangParser.RBRACK, 0); }
		public TypeRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeRef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitTypeRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeRefContext typeRef() throws RecognitionException {
		TypeRefContext _localctx = new TypeRefContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_typeRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			typeAtom();
			setState(280);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACK) {
				{
				setState(276);
				match(LBRACK);
				setState(277);
				arraySize();
				setState(278);
				match(RBRACK);
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

	@SuppressWarnings("CheckReturnValue")
	public static class TypeAtomContext extends ParserRuleContext {
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public TerminalNode LIST() { return getToken(HopeLangParser.LIST, 0); }
		public TerminalNode LT() { return getToken(HopeLangParser.LT, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode GT() { return getToken(HopeLangParser.GT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TypeAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeAtom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitTypeAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeAtomContext typeAtom() throws RecognitionException {
		TypeAtomContext _localctx = new TypeAtomContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_typeAtom);
		try {
			setState(289);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INT_TYPE:
			case REAL_TYPE:
			case BOOL_TYPE:
			case STRING_TYPE:
			case TIME_TYPE:
			case VOID_TYPE:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				primitiveType();
				}
				break;
			case LIST:
				enterOuterAlt(_localctx, 2);
				{
				setState(283);
				match(LIST);
				setState(284);
				match(LT);
				setState(285);
				typeRef();
				setState(286);
				match(GT);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(288);
				match(ID);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveTypeContext extends ParserRuleContext {
		public TerminalNode INT_TYPE() { return getToken(HopeLangParser.INT_TYPE, 0); }
		public TerminalNode REAL_TYPE() { return getToken(HopeLangParser.REAL_TYPE, 0); }
		public TerminalNode BOOL_TYPE() { return getToken(HopeLangParser.BOOL_TYPE, 0); }
		public TerminalNode STRING_TYPE() { return getToken(HopeLangParser.STRING_TYPE, 0); }
		public TerminalNode TIME_TYPE() { return getToken(HopeLangParser.TIME_TYPE, 0); }
		public TerminalNode VOID_TYPE() { return getToken(HopeLangParser.VOID_TYPE, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 270582939648L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class ArraySizeContext extends ParserRuleContext {
		public TerminalNode INT_LITERAL() { return getToken(HopeLangParser.INT_LITERAL, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public ArraySizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arraySize; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitArraySize(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArraySizeContext arraySize() throws RecognitionException {
		ArraySizeContext _localctx = new ArraySizeContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_arraySize);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			_la = _input.LA(1);
			if ( !(_la==INT_LITERAL || _la==ID) ) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public LocalVarDeclContext localVarDecl() {
			return getRuleContext(LocalVarDeclContext.class,0);
		}
		public AssignmentStatementContext assignmentStatement() {
			return getRuleContext(AssignmentStatementContext.class,0);
		}
		public IfStatementContext ifStatement() {
			return getRuleContext(IfStatementContext.class,0);
		}
		public WhileStatementContext whileStatement() {
			return getRuleContext(WhileStatementContext.class,0);
		}
		public ForStatementContext forStatement() {
			return getRuleContext(ForStatementContext.class,0);
		}
		public BreakStatementContext breakStatement() {
			return getRuleContext(BreakStatementContext.class,0);
		}
		public ContinueStatementContext continueStatement() {
			return getRuleContext(ContinueStatementContext.class,0);
		}
		public ReturnStatementContext returnStatement() {
			return getRuleContext(ReturnStatementContext.class,0);
		}
		public EmitStatementContext emitStatement() {
			return getRuleContext(EmitStatementContext.class,0);
		}
		public ExpressionStatementContext expressionStatement() {
			return getRuleContext(ExpressionStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_statement);
		try {
			setState(305);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(295);
				localVarDecl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(296);
				assignmentStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(297);
				ifStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(298);
				whileStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(299);
				forStatement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(300);
				breakStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(301);
				continueStatement();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(302);
				returnStatement();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(303);
				emitStatement();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(304);
				expressionStatement();
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

	@SuppressWarnings("CheckReturnValue")
	public static class LocalVarDeclContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(HopeLangParser.VAR, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode OF() { return getToken(HopeLangParser.OF, 0); }
		public TypeRefContext typeRef() {
			return getRuleContext(TypeRefContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(HopeLangParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public LocalVarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localVarDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLocalVarDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocalVarDeclContext localVarDecl() throws RecognitionException {
		LocalVarDeclContext _localctx = new LocalVarDeclContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_localVarDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			match(VAR);
			setState(308);
			match(ID);
			setState(309);
			match(OF);
			setState(310);
			typeRef();
			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(311);
				match(ASSIGN);
				setState(312);
				expression();
				}
			}

			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(315);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentStatementContext extends ParserRuleContext {
		public LvalueContext lvalue() {
			return getRuleContext(LvalueContext.class,0);
		}
		public AssignmentOperatorContext assignmentOperator() {
			return getRuleContext(AssignmentOperatorContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public AssignmentStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitAssignmentStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentStatementContext assignmentStatement() throws RecognitionException {
		AssignmentStatementContext _localctx = new AssignmentStatementContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_assignmentStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			lvalue();
			setState(319);
			assignmentOperator();
			setState(320);
			expression();
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(321);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentOperatorContext extends ParserRuleContext {
		public TerminalNode ASSIGN() { return getToken(HopeLangParser.ASSIGN, 0); }
		public TerminalNode PLUS_ASSIGN() { return getToken(HopeLangParser.PLUS_ASSIGN, 0); }
		public TerminalNode MINUS_ASSIGN() { return getToken(HopeLangParser.MINUS_ASSIGN, 0); }
		public TerminalNode MUL_ASSIGN() { return getToken(HopeLangParser.MUL_ASSIGN, 0); }
		public TerminalNode DIV_ASSIGN() { return getToken(HopeLangParser.DIV_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(HopeLangParser.MOD_ASSIGN, 0); }
		public AssignmentOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitAssignmentOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentOperatorContext assignmentOperator() throws RecognitionException {
		AssignmentOperatorContext _localctx = new AssignmentOperatorContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_assignmentOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 18287077393170432L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class LvalueContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public List<LvalueSuffixContext> lvalueSuffix() {
			return getRuleContexts(LvalueSuffixContext.class);
		}
		public LvalueSuffixContext lvalueSuffix(int i) {
			return getRuleContext(LvalueSuffixContext.class,i);
		}
		public LvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLvalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalueContext lvalue() throws RecognitionException {
		LvalueContext _localctx = new LvalueContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_lvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			match(ID);
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT || _la==LBRACK) {
				{
				{
				setState(327);
				lvalueSuffix();
				}
				}
				setState(332);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LvalueSuffixContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(HopeLangParser.DOT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LBRACK() { return getToken(HopeLangParser.LBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(HopeLangParser.RBRACK, 0); }
		public LvalueSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalueSuffix; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLvalueSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalueSuffixContext lvalueSuffix() throws RecognitionException {
		LvalueSuffixContext _localctx = new LvalueSuffixContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_lvalueSuffix);
		try {
			setState(339);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				enterOuterAlt(_localctx, 1);
				{
				setState(333);
				match(DOT);
				setState(334);
				match(ID);
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
				match(LBRACK);
				setState(336);
				expression();
				setState(337);
				match(RBRACK);
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

	@SuppressWarnings("CheckReturnValue")
	public static class IfStatementContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(HopeLangParser.IF, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> ELIF() { return getTokens(HopeLangParser.ELIF); }
		public TerminalNode ELIF(int i) {
			return getToken(HopeLangParser.ELIF, i);
		}
		public TerminalNode ELSE() { return getToken(HopeLangParser.ELSE, 0); }
		public IfStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitIfStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfStatementContext ifStatement() throws RecognitionException {
		IfStatementContext _localctx = new IfStatementContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_ifStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(341);
			match(IF);
			setState(342);
			expression();
			setState(346);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(343);
				statement();
				}
				}
				setState(348);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ELIF) {
				{
				{
				setState(349);
				match(ELIF);
				setState(350);
				expression();
				setState(354);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
					{
					{
					setState(351);
					statement();
					}
					}
					setState(356);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(361);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(369);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(362);
				match(ELSE);
				setState(366);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
					{
					{
					setState(363);
					statement();
					}
					}
					setState(368);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(371);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class WhileStatementContext extends ParserRuleContext {
		public TerminalNode WHILE() { return getToken(HopeLangParser.WHILE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public WhileStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whileStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitWhileStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhileStatementContext whileStatement() throws RecognitionException {
		WhileStatementContext _localctx = new WhileStatementContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_whileStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(373);
			match(WHILE);
			setState(374);
			expression();
			setState(378);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(375);
				statement();
				}
				}
				setState(380);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(381);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ForStatementContext extends ParserRuleContext {
		public TerminalNode FOR() { return getToken(HopeLangParser.FOR, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode FROM() { return getToken(HopeLangParser.FROM, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode TO() { return getToken(HopeLangParser.TO, 0); }
		public TerminalNode END() { return getToken(HopeLangParser.END, 0); }
		public TerminalNode STEP() { return getToken(HopeLangParser.STEP, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public ForStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitForStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForStatementContext forStatement() throws RecognitionException {
		ForStatementContext _localctx = new ForStatementContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_forStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(383);
			match(FOR);
			setState(384);
			match(ID);
			setState(385);
			match(FROM);
			setState(386);
			expression();
			setState(387);
			match(TO);
			setState(388);
			expression();
			setState(391);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STEP) {
				{
				setState(389);
				match(STEP);
				setState(390);
				expression();
				}
			}

			setState(396);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9115285643232858104L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) {
				{
				{
				setState(393);
				statement();
				}
				}
				setState(398);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(399);
			match(END);
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

	@SuppressWarnings("CheckReturnValue")
	public static class BreakStatementContext extends ParserRuleContext {
		public TerminalNode BREAK() { return getToken(HopeLangParser.BREAK, 0); }
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public BreakStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_breakStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitBreakStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BreakStatementContext breakStatement() throws RecognitionException {
		BreakStatementContext _localctx = new BreakStatementContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_breakStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(401);
			match(BREAK);
			setState(403);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(402);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ContinueStatementContext extends ParserRuleContext {
		public TerminalNode CONTINUE() { return getToken(HopeLangParser.CONTINUE, 0); }
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public ContinueStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_continueStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitContinueStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContinueStatementContext continueStatement() throws RecognitionException {
		ContinueStatementContext _localctx = new ContinueStatementContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_continueStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(CONTINUE);
			setState(407);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(406);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnStatementContext extends ParserRuleContext {
		public TerminalNode RETURN() { return getToken(HopeLangParser.RETURN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public ReturnStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitReturnStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReturnStatementContext returnStatement() throws RecognitionException {
		ReturnStatementContext _localctx = new ReturnStatementContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_returnStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			match(RETURN);
			setState(411);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(410);
				expression();
				}
				break;
			}
			setState(414);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(413);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EmitStatementContext extends ParserRuleContext {
		public TerminalNode EMIT() { return getToken(HopeLangParser.EMIT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public ArgumentListContext argumentList() {
			return getRuleContext(ArgumentListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public EmitStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emitStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEmitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EmitStatementContext emitStatement() throws RecognitionException {
		EmitStatementContext _localctx = new EmitStatementContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_emitStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			match(EMIT);
			setState(417);
			match(ID);
			setState(418);
			match(LPAREN);
			setState(420);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 27)) & ~0x3f) == 0 && ((1L << (_la - 27)) & 16562199199763L) != 0)) {
				{
				setState(419);
				argumentList();
				}
			}

			setState(422);
			match(RPAREN);
			setState(424);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(423);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionStatementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(HopeLangParser.SEMI, 0); }
		public ExpressionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitExpressionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionStatementContext expressionStatement() throws RecognitionException {
		ExpressionStatementContext _localctx = new ExpressionStatementContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_expressionStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(426);
			expression();
			setState(428);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(427);
				match(SEMI);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public LogicalOrContext logicalOr() {
			return getRuleContext(LogicalOrContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			logicalOr();
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

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrContext extends ParserRuleContext {
		public List<LogicalAndContext> logicalAnd() {
			return getRuleContexts(LogicalAndContext.class);
		}
		public LogicalAndContext logicalAnd(int i) {
			return getRuleContext(LogicalAndContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(HopeLangParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(HopeLangParser.OR, i);
		}
		public LogicalOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLogicalOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOrContext logicalOr() throws RecognitionException {
		LogicalOrContext _localctx = new LogicalOrContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_logicalOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(432);
			logicalAnd();
			setState(437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(433);
				match(OR);
				setState(434);
				logicalAnd();
				}
				}
				setState(439);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndContext extends ParserRuleContext {
		public List<EqualityContext> equality() {
			return getRuleContexts(EqualityContext.class);
		}
		public EqualityContext equality(int i) {
			return getRuleContext(EqualityContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(HopeLangParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(HopeLangParser.AND, i);
		}
		public LogicalAndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAnd; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLogicalAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalAndContext logicalAnd() throws RecognitionException {
		LogicalAndContext _localctx = new LogicalAndContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_logicalAnd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			equality();
			setState(445);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(441);
				match(AND);
				setState(442);
				equality();
				}
				}
				setState(447);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityContext extends ParserRuleContext {
		public List<ComparisonContext> comparison() {
			return getRuleContexts(ComparisonContext.class);
		}
		public ComparisonContext comparison(int i) {
			return getRuleContext(ComparisonContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(HopeLangParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(HopeLangParser.EQ, i);
		}
		public List<TerminalNode> NEQ() { return getTokens(HopeLangParser.NEQ); }
		public TerminalNode NEQ(int i) {
			return getToken(HopeLangParser.NEQ, i);
		}
		public EqualityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equality; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitEquality(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityContext equality() throws RecognitionException {
		EqualityContext _localctx = new EqualityContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_equality);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			comparison();
			setState(453);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQ || _la==NEQ) {
				{
				{
				setState(449);
				_la = _input.LA(1);
				if ( !(_la==EQ || _la==NEQ) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(450);
				comparison();
				}
				}
				setState(455);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public List<AdditiveContext> additive() {
			return getRuleContexts(AdditiveContext.class);
		}
		public AdditiveContext additive(int i) {
			return getRuleContext(AdditiveContext.class,i);
		}
		public List<TerminalNode> LT() { return getTokens(HopeLangParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(HopeLangParser.LT, i);
		}
		public List<TerminalNode> LTE() { return getTokens(HopeLangParser.LTE); }
		public TerminalNode LTE(int i) {
			return getToken(HopeLangParser.LTE, i);
		}
		public List<TerminalNode> GT() { return getTokens(HopeLangParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(HopeLangParser.GT, i);
		}
		public List<TerminalNode> GTE() { return getTokens(HopeLangParser.GTE); }
		public TerminalNode GTE(int i) {
			return getToken(HopeLangParser.GTE, i);
		}
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_comparison);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
			additive();
			setState(461);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16888498602639360L) != 0)) {
				{
				{
				setState(457);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 16888498602639360L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(458);
				additive();
				}
				}
				setState(463);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveContext extends ParserRuleContext {
		public List<MultiplicativeContext> multiplicative() {
			return getRuleContexts(MultiplicativeContext.class);
		}
		public MultiplicativeContext multiplicative(int i) {
			return getRuleContext(MultiplicativeContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(HopeLangParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(HopeLangParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(HopeLangParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(HopeLangParser.MINUS, i);
		}
		public AdditiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitAdditive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveContext additive() throws RecognitionException {
		AdditiveContext _localctx = new AdditiveContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_additive);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			multiplicative();
			setState(469);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(465);
					_la = _input.LA(1);
					if ( !(_la==PLUS || _la==MINUS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(466);
					multiplicative();
					}
					} 
				}
				setState(471);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeContext extends ParserRuleContext {
		public List<UnaryContext> unary() {
			return getRuleContexts(UnaryContext.class);
		}
		public UnaryContext unary(int i) {
			return getRuleContext(UnaryContext.class,i);
		}
		public List<TerminalNode> MUL() { return getTokens(HopeLangParser.MUL); }
		public TerminalNode MUL(int i) {
			return getToken(HopeLangParser.MUL, i);
		}
		public List<TerminalNode> DIV() { return getTokens(HopeLangParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(HopeLangParser.DIV, i);
		}
		public List<TerminalNode> MOD() { return getTokens(HopeLangParser.MOD); }
		public TerminalNode MOD(int i) {
			return getToken(HopeLangParser.MOD, i);
		}
		public MultiplicativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicative; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitMultiplicative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeContext multiplicative() throws RecognitionException {
		MultiplicativeContext _localctx = new MultiplicativeContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_multiplicative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(472);
			unary();
			setState(477);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1008806316530991104L) != 0)) {
				{
				{
				setState(473);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1008806316530991104L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(474);
				unary();
				}
				}
				setState(479);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryContext extends ParserRuleContext {
		public UnaryContext unary() {
			return getRuleContext(UnaryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(HopeLangParser.NOT, 0); }
		public TerminalNode MINUS() { return getToken(HopeLangParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(HopeLangParser.PLUS, 0); }
		public PostfixContext postfix() {
			return getRuleContext(PostfixContext.class,0);
		}
		public UnaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitUnary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryContext unary() throws RecognitionException {
		UnaryContext _localctx = new UnaryContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_unary);
		int _la;
		try {
			setState(483);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(480);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 108086393204375552L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(481);
				unary();
				}
				break;
			case TRUE:
			case FALSE:
			case LPAREN:
			case REAL_LITERAL:
			case INT_LITERAL:
			case STRING_LITERAL:
			case ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(482);
				postfix();
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

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixContext extends ParserRuleContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public List<PostfixSuffixContext> postfixSuffix() {
			return getRuleContexts(PostfixSuffixContext.class);
		}
		public PostfixSuffixContext postfixSuffix(int i) {
			return getRuleContext(PostfixSuffixContext.class,i);
		}
		public PostfixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfix; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitPostfix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixContext postfix() throws RecognitionException {
		PostfixContext _localctx = new PostfixContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_postfix);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
			primary();
			setState(489);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(486);
					postfixSuffix();
					}
					} 
				}
				setState(491);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixSuffixContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(HopeLangParser.DOT, 0); }
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LBRACK() { return getToken(HopeLangParser.LBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(HopeLangParser.RBRACK, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public ArgumentListContext argumentList() {
			return getRuleContext(ArgumentListContext.class,0);
		}
		public PostfixSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixSuffix; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitPostfixSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixSuffixContext postfixSuffix() throws RecognitionException {
		PostfixSuffixContext _localctx = new PostfixSuffixContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_postfixSuffix);
		int _la;
		try {
			setState(503);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
				enterOuterAlt(_localctx, 1);
				{
				setState(492);
				match(DOT);
				setState(493);
				match(ID);
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 2);
				{
				setState(494);
				match(LBRACK);
				setState(495);
				expression();
				setState(496);
				match(RBRACK);
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(498);
				match(LPAREN);
				setState(500);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 27)) & ~0x3f) == 0 && ((1L << (_la - 27)) & 16562199199763L) != 0)) {
					{
					setState(499);
					argumentList();
					}
				}

				setState(502);
				match(RPAREN);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public DurationLiteralContext durationLiteral() {
			return getRuleContext(DurationLiteralContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode ID() { return getToken(HopeLangParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(HopeLangParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(HopeLangParser.RPAREN, 0); }
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_primary);
		try {
			setState(512);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(505);
				durationLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(506);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(507);
				match(ID);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(508);
				match(LPAREN);
				setState(509);
				expression();
				setState(510);
				match(RPAREN);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(HopeLangParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(HopeLangParser.COMMA, i);
		}
		public ArgumentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitArgumentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentListContext argumentList() throws RecognitionException {
		ArgumentListContext _localctx = new ArgumentListContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_argumentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(514);
			expression();
			setState(519);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(515);
				match(COMMA);
				setState(516);
				expression();
				}
				}
				setState(521);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode REAL_LITERAL() { return getToken(HopeLangParser.REAL_LITERAL, 0); }
		public TerminalNode INT_LITERAL() { return getToken(HopeLangParser.INT_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(HopeLangParser.STRING_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(HopeLangParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(HopeLangParser.FALSE, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			_la = _input.LA(1);
			if ( !(((((_la - 27)) & ~0x3f) == 0 && ((1L << (_la - 27)) & 7696581394435L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class DurationLiteralContext extends ParserRuleContext {
		public TimeUnitContext timeUnit() {
			return getRuleContext(TimeUnitContext.class,0);
		}
		public TerminalNode INT_LITERAL() { return getToken(HopeLangParser.INT_LITERAL, 0); }
		public TerminalNode REAL_LITERAL() { return getToken(HopeLangParser.REAL_LITERAL, 0); }
		public DurationLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_durationLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitDurationLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DurationLiteralContext durationLiteral() throws RecognitionException {
		DurationLiteralContext _localctx = new DurationLiteralContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_durationLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			_la = _input.LA(1);
			if ( !(_la==REAL_LITERAL || _la==INT_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(525);
			timeUnit();
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

	@SuppressWarnings("CheckReturnValue")
	public static class TimeUnitContext extends ParserRuleContext {
		public TerminalNode MILLISECOND_UNIT() { return getToken(HopeLangParser.MILLISECOND_UNIT, 0); }
		public TerminalNode SECOND_UNIT() { return getToken(HopeLangParser.SECOND_UNIT, 0); }
		public TerminalNode MINUTE_UNIT() { return getToken(HopeLangParser.MINUTE_UNIT, 0); }
		public TerminalNode HOUR_UNIT() { return getToken(HopeLangParser.HOUR_UNIT, 0); }
		public TerminalNode DAY_UNIT() { return getToken(HopeLangParser.DAY_UNIT, 0); }
		public TimeUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof HopeLangVisitor ) return ((HopeLangVisitor<? extends T>)visitor).visitTimeUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeUnitContext timeUnit() throws RecognitionException {
		TimeUnitContext _localctx = new TimeUnitContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_timeUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8521215115264L) != 0)) ) {
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

	public static final String _serializedATN =
		"\u0004\u0001I\u0212\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u0001\u0000\u0003\u0000h\b\u0000\u0001\u0000\u0005\u0000k\b\u0000"+
		"\n\u0000\f\u0000n\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001u\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0003\u0002\u0081\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u008a\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004\u0092\b\u0004\u0001\u0004\u0003\u0004\u0095\b\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u009a\b\u0005\n\u0005\f\u0005"+
		"\u009d\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0003\u0006\u00a6\b\u0006\u0001\u0006\u0003\u0006"+
		"\u00a9\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0004\u0007\u00ae\b"+
		"\u0007\u000b\u0007\f\u0007\u00af\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0003\b\u00b6\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u00bc\b\t"+
		"\u0001\t\u0001\t\u0003\t\u00c0\b\t\u0001\n\u0001\n\u0001\n\u0005\n\u00c5"+
		"\b\n\n\n\f\n\u00c8\t\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u00d0\b\u000b\u0001\u000b\u0001\u000b\u0005\u000b"+
		"\u00d4\b\u000b\n\u000b\f\u000b\u00d7\t\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0005\f\u00de\b\f\n\f\f\f\u00e1\t\f\u0001\f\u0001\f"+
		"\u0001\r\u0001\r\u0001\r\u0005\r\u00e8\b\r\n\r\f\r\u00eb\t\r\u0001\r\u0001"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u00f2\b\u000e\n\u000e"+
		"\f\u000e\u00f5\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u00fb\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0005\u000f\u0101\b\u000f\n\u000f\f\u000f\u0104\t\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u010b\b\u0010\n"+
		"\u0010\f\u0010\u010e\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u0119\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0122\b\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0003\u0016\u0132\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u013a\b\u0017\u0001\u0017\u0003"+
		"\u0017\u013d\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003"+
		"\u0018\u0143\b\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0005"+
		"\u001a\u0149\b\u001a\n\u001a\f\u001a\u014c\t\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0154\b\u001b"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u0159\b\u001c\n\u001c"+
		"\f\u001c\u015c\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c"+
		"\u0161\b\u001c\n\u001c\f\u001c\u0164\t\u001c\u0005\u001c\u0166\b\u001c"+
		"\n\u001c\f\u001c\u0169\t\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u016d"+
		"\b\u001c\n\u001c\f\u001c\u0170\t\u001c\u0003\u001c\u0172\b\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u0179"+
		"\b\u001d\n\u001d\f\u001d\u017c\t\u001d\u0001\u001d\u0001\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0003\u001e\u0188\b\u001e\u0001\u001e\u0005\u001e\u018b\b"+
		"\u001e\n\u001e\f\u001e\u018e\t\u001e\u0001\u001e\u0001\u001e\u0001\u001f"+
		"\u0001\u001f\u0003\u001f\u0194\b\u001f\u0001 \u0001 \u0003 \u0198\b \u0001"+
		"!\u0001!\u0003!\u019c\b!\u0001!\u0003!\u019f\b!\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0003\"\u01a5\b\"\u0001\"\u0001\"\u0003\"\u01a9\b\"\u0001#"+
		"\u0001#\u0003#\u01ad\b#\u0001$\u0001$\u0001%\u0001%\u0001%\u0005%\u01b4"+
		"\b%\n%\f%\u01b7\t%\u0001&\u0001&\u0001&\u0005&\u01bc\b&\n&\f&\u01bf\t"+
		"&\u0001\'\u0001\'\u0001\'\u0005\'\u01c4\b\'\n\'\f\'\u01c7\t\'\u0001(\u0001"+
		"(\u0001(\u0005(\u01cc\b(\n(\f(\u01cf\t(\u0001)\u0001)\u0001)\u0005)\u01d4"+
		"\b)\n)\f)\u01d7\t)\u0001*\u0001*\u0001*\u0005*\u01dc\b*\n*\f*\u01df\t"+
		"*\u0001+\u0001+\u0001+\u0003+\u01e4\b+\u0001,\u0001,\u0005,\u01e8\b,\n"+
		",\f,\u01eb\t,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-"+
		"\u0003-\u01f5\b-\u0001-\u0003-\u01f8\b-\u0001.\u0001.\u0001.\u0001.\u0001"+
		".\u0001.\u0001.\u0003.\u0201\b.\u0001/\u0001/\u0001/\u0005/\u0206\b/\n"+
		"/\f/\u0209\t/\u00010\u00010\u00011\u00011\u00011\u00012\u00012\u00012"+
		"\u0000\u00003\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bd\u0000\f"+
		"\u0001\u0000=>\u0001\u0000 %\u0002\u0000DDFF\u0002\u0000+/66\u0001\u0000"+
		"01\u0001\u000025\u0001\u000078\u0001\u00009;\u0002\u0000\u001f\u001f7"+
		"8\u0002\u0000\u001b\u001cCE\u0001\u0000CD\u0001\u0000&*\u022c\u0000g\u0001"+
		"\u0000\u0000\u0000\u0002q\u0001\u0000\u0000\u0000\u0004\u0080\u0001\u0000"+
		"\u0000\u0000\u0006\u0082\u0001\u0000\u0000\u0000\b\u008b\u0001\u0000\u0000"+
		"\u0000\n\u0096\u0001\u0000\u0000\u0000\f\u00a0\u0001\u0000\u0000\u0000"+
		"\u000e\u00aa\u0001\u0000\u0000\u0000\u0010\u00b3\u0001\u0000\u0000\u0000"+
		"\u0012\u00b7\u0001\u0000\u0000\u0000\u0014\u00c1\u0001\u0000\u0000\u0000"+
		"\u0016\u00cb\u0001\u0000\u0000\u0000\u0018\u00da\u0001\u0000\u0000\u0000"+
		"\u001a\u00e4\u0001\u0000\u0000\u0000\u001c\u00ee\u0001\u0000\u0000\u0000"+
		"\u001e\u00f6\u0001\u0000\u0000\u0000 \u0107\u0001\u0000\u0000\u0000\""+
		"\u010f\u0001\u0000\u0000\u0000$\u0113\u0001\u0000\u0000\u0000&\u0121\u0001"+
		"\u0000\u0000\u0000(\u0123\u0001\u0000\u0000\u0000*\u0125\u0001\u0000\u0000"+
		"\u0000,\u0131\u0001\u0000\u0000\u0000.\u0133\u0001\u0000\u0000\u00000"+
		"\u013e\u0001\u0000\u0000\u00002\u0144\u0001\u0000\u0000\u00004\u0146\u0001"+
		"\u0000\u0000\u00006\u0153\u0001\u0000\u0000\u00008\u0155\u0001\u0000\u0000"+
		"\u0000:\u0175\u0001\u0000\u0000\u0000<\u017f\u0001\u0000\u0000\u0000>"+
		"\u0191\u0001\u0000\u0000\u0000@\u0195\u0001\u0000\u0000\u0000B\u0199\u0001"+
		"\u0000\u0000\u0000D\u01a0\u0001\u0000\u0000\u0000F\u01aa\u0001\u0000\u0000"+
		"\u0000H\u01ae\u0001\u0000\u0000\u0000J\u01b0\u0001\u0000\u0000\u0000L"+
		"\u01b8\u0001\u0000\u0000\u0000N\u01c0\u0001\u0000\u0000\u0000P\u01c8\u0001"+
		"\u0000\u0000\u0000R\u01d0\u0001\u0000\u0000\u0000T\u01d8\u0001\u0000\u0000"+
		"\u0000V\u01e3\u0001\u0000\u0000\u0000X\u01e5\u0001\u0000\u0000\u0000Z"+
		"\u01f7\u0001\u0000\u0000\u0000\\\u0200\u0001\u0000\u0000\u0000^\u0202"+
		"\u0001\u0000\u0000\u0000`\u020a\u0001\u0000\u0000\u0000b\u020c\u0001\u0000"+
		"\u0000\u0000d\u020f\u0001\u0000\u0000\u0000fh\u0003\u0002\u0001\u0000"+
		"gf\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000hl\u0001\u0000\u0000"+
		"\u0000ik\u0003\u0004\u0002\u0000ji\u0001\u0000\u0000\u0000kn\u0001\u0000"+
		"\u0000\u0000lj\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000\u0000mo\u0001"+
		"\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000op\u0005\u0000\u0000\u0001"+
		"p\u0001\u0001\u0000\u0000\u0000qr\u0005\u0001\u0000\u0000rt\u0005F\u0000"+
		"\u0000su\u0005>\u0000\u0000ts\u0001\u0000\u0000\u0000tu\u0001\u0000\u0000"+
		"\u0000u\u0003\u0001\u0000\u0000\u0000v\u0081\u0003\u0006\u0003\u0000w"+
		"\u0081\u0003\b\u0004\u0000x\u0081\u0003\n\u0005\u0000y\u0081\u0003\u000e"+
		"\u0007\u0000z\u0081\u0003\u0012\t\u0000{\u0081\u0003\u001e\u000f\u0000"+
		"|\u0081\u0003\u0014\n\u0000}\u0081\u0003\u0016\u000b\u0000~\u0081\u0003"+
		"\u0018\f\u0000\u007f\u0081\u0003\u001a\r\u0000\u0080v\u0001\u0000\u0000"+
		"\u0000\u0080w\u0001\u0000\u0000\u0000\u0080x\u0001\u0000\u0000\u0000\u0080"+
		"y\u0001\u0000\u0000\u0000\u0080z\u0001\u0000\u0000\u0000\u0080{\u0001"+
		"\u0000\u0000\u0000\u0080|\u0001\u0000\u0000\u0000\u0080}\u0001\u0000\u0000"+
		"\u0000\u0080~\u0001\u0000\u0000\u0000\u0080\u007f\u0001\u0000\u0000\u0000"+
		"\u0081\u0005\u0001\u0000\u0000\u0000\u0082\u0083\u0005\u0002\u0000\u0000"+
		"\u0083\u0084\u0005F\u0000\u0000\u0084\u0085\u0005\u0018\u0000\u0000\u0085"+
		"\u0086\u0003$\u0012\u0000\u0086\u0087\u00056\u0000\u0000\u0087\u0089\u0003"+
		"H$\u0000\u0088\u008a\u0005>\u0000\u0000\u0089\u0088\u0001\u0000\u0000"+
		"\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u0007\u0001\u0000\u0000"+
		"\u0000\u008b\u008c\u0005\u0003\u0000\u0000\u008c\u008d\u0005F\u0000\u0000"+
		"\u008d\u008e\u0005\u0018\u0000\u0000\u008e\u0091\u0003$\u0012\u0000\u008f"+
		"\u0090\u00056\u0000\u0000\u0090\u0092\u0003H$\u0000\u0091\u008f\u0001"+
		"\u0000\u0000\u0000\u0091\u0092\u0001\u0000\u0000\u0000\u0092\u0094\u0001"+
		"\u0000\u0000\u0000\u0093\u0095\u0005>\u0000\u0000\u0094\u0093\u0001\u0000"+
		"\u0000\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\t\u0001\u0000\u0000"+
		"\u0000\u0096\u0097\u0005\u0004\u0000\u0000\u0097\u009b\u0005F\u0000\u0000"+
		"\u0098\u009a\u0003\f\u0006\u0000\u0099\u0098\u0001\u0000\u0000\u0000\u009a"+
		"\u009d\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009b"+
		"\u009c\u0001\u0000\u0000\u0000\u009c\u009e\u0001\u0000\u0000\u0000\u009d"+
		"\u009b\u0001\u0000\u0000\u0000\u009e\u009f\u0005\u0019\u0000\u0000\u009f"+
		"\u000b\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005F\u0000\u0000\u00a1\u00a2"+
		"\u0005\u0018\u0000\u0000\u00a2\u00a5\u0003$\u0012\u0000\u00a3\u00a4\u0005"+
		"6\u0000\u0000\u00a4\u00a6\u0003H$\u0000\u00a5\u00a3\u0001\u0000\u0000"+
		"\u0000\u00a5\u00a6\u0001\u0000\u0000\u0000\u00a6\u00a8\u0001\u0000\u0000"+
		"\u0000\u00a7\u00a9\u0005>\u0000\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000"+
		"\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\r\u0001\u0000\u0000\u0000\u00aa"+
		"\u00ab\u0005\u0005\u0000\u0000\u00ab\u00ad\u0005F\u0000\u0000\u00ac\u00ae"+
		"\u0003\u0010\b\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ae\u00af\u0001"+
		"\u0000\u0000\u0000\u00af\u00ad\u0001\u0000\u0000\u0000\u00af\u00b0\u0001"+
		"\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1\u00b2\u0005"+
		"\u0019\u0000\u0000\u00b2\u000f\u0001\u0000\u0000\u0000\u00b3\u00b5\u0005"+
		"F\u0000\u0000\u00b4\u00b6\u0007\u0000\u0000\u0000\u00b5\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u0011\u0001\u0000"+
		"\u0000\u0000\u00b7\u00b8\u0005\u0006\u0000\u0000\u00b8\u00b9\u0005F\u0000"+
		"\u0000\u00b9\u00bb\u0005?\u0000\u0000\u00ba\u00bc\u0003 \u0010\u0000\u00bb"+
		"\u00ba\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc"+
		"\u00bd\u0001\u0000\u0000\u0000\u00bd\u00bf\u0005@\u0000\u0000\u00be\u00c0"+
		"\u0005>\u0000\u0000\u00bf\u00be\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001"+
		"\u0000\u0000\u0000\u00c0\u0013\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005"+
		"\b\u0000\u0000\u00c2\u00c6\u0005\t\u0000\u0000\u00c3\u00c5\u0003,\u0016"+
		"\u0000\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000"+
		"\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000"+
		"\u0000\u00c7\u00c9\u0001\u0000\u0000\u0000\u00c8\u00c6\u0001\u0000\u0000"+
		"\u0000\u00c9\u00ca\u0005\u0019\u0000\u0000\u00ca\u0015\u0001\u0000\u0000"+
		"\u0000\u00cb\u00cc\u0005\b\u0000\u0000\u00cc\u00cd\u0005F\u0000\u0000"+
		"\u00cd\u00cf\u0005?\u0000\u0000\u00ce\u00d0\u0003\u001c\u000e\u0000\u00cf"+
		"\u00ce\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0"+
		"\u00d1\u0001\u0000\u0000\u0000\u00d1\u00d5\u0005@\u0000\u0000\u00d2\u00d4"+
		"\u0003,\u0016\u0000\u00d3\u00d2\u0001\u0000\u0000\u0000\u00d4\u00d7\u0001"+
		"\u0000\u0000\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001"+
		"\u0000\u0000\u0000\u00d6\u00d8\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001"+
		"\u0000\u0000\u0000\u00d8\u00d9\u0005\u0019\u0000\u0000\u00d9\u0017\u0001"+
		"\u0000\u0000\u0000\u00da\u00db\u0005\n\u0000\u0000\u00db\u00df\u0003b"+
		"1\u0000\u00dc\u00de\u0003,\u0016\u0000\u00dd\u00dc\u0001\u0000\u0000\u0000"+
		"\u00de\u00e1\u0001\u0000\u0000\u0000\u00df\u00dd\u0001\u0000\u0000\u0000"+
		"\u00df\u00e0\u0001\u0000\u0000\u0000\u00e0\u00e2\u0001\u0000\u0000\u0000"+
		"\u00e1\u00df\u0001\u0000\u0000\u0000\u00e2\u00e3\u0005\u0019\u0000\u0000"+
		"\u00e3\u0019\u0001\u0000\u0000\u0000\u00e4\u00e5\u0005\u000b\u0000\u0000"+
		"\u00e5\u00e9\u0003b1\u0000\u00e6\u00e8\u0003,\u0016\u0000\u00e7\u00e6"+
		"\u0001\u0000\u0000\u0000\u00e8\u00eb\u0001\u0000\u0000\u0000\u00e9\u00e7"+
		"\u0001\u0000\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec"+
		"\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001\u0000\u0000\u0000\u00ec\u00ed"+
		"\u0005\u0019\u0000\u0000\u00ed\u001b\u0001\u0000\u0000\u0000\u00ee\u00f3"+
		"\u0005F\u0000\u0000\u00ef\u00f0\u0005=\u0000\u0000\u00f0\u00f2\u0005F"+
		"\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000"+
		"\u0000\u0000\u00f4\u001d\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f7\u0005\u0007\u0000\u0000\u00f7\u00f8\u0005F\u0000"+
		"\u0000\u00f8\u00fa\u0005?\u0000\u0000\u00f9\u00fb\u0003 \u0010\u0000\u00fa"+
		"\u00f9\u0001\u0000\u0000\u0000\u00fa\u00fb\u0001\u0000\u0000\u0000\u00fb"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fc\u00fd\u0005@\u0000\u0000\u00fd\u00fe"+
		"\u0005\u0018\u0000\u0000\u00fe\u0102\u0003$\u0012\u0000\u00ff\u0101\u0003"+
		",\u0016\u0000\u0100\u00ff\u0001\u0000\u0000\u0000\u0101\u0104\u0001\u0000"+
		"\u0000\u0000\u0102\u0100\u0001\u0000\u0000\u0000\u0102\u0103\u0001\u0000"+
		"\u0000\u0000\u0103\u0105\u0001\u0000\u0000\u0000\u0104\u0102\u0001\u0000"+
		"\u0000\u0000\u0105\u0106\u0005\u0019\u0000\u0000\u0106\u001f\u0001\u0000"+
		"\u0000\u0000\u0107\u010c\u0003\"\u0011\u0000\u0108\u0109\u0005=\u0000"+
		"\u0000\u0109\u010b\u0003\"\u0011\u0000\u010a\u0108\u0001\u0000\u0000\u0000"+
		"\u010b\u010e\u0001\u0000\u0000\u0000\u010c\u010a\u0001\u0000\u0000\u0000"+
		"\u010c\u010d\u0001\u0000\u0000\u0000\u010d!\u0001\u0000\u0000\u0000\u010e"+
		"\u010c\u0001\u0000\u0000\u0000\u010f\u0110\u0005F\u0000\u0000\u0110\u0111"+
		"\u0005\u0018\u0000\u0000\u0111\u0112\u0003$\u0012\u0000\u0112#\u0001\u0000"+
		"\u0000\u0000\u0113\u0118\u0003&\u0013\u0000\u0114\u0115\u0005A\u0000\u0000"+
		"\u0115\u0116\u0003*\u0015\u0000\u0116\u0117\u0005B\u0000\u0000\u0117\u0119"+
		"\u0001\u0000\u0000\u0000\u0118\u0114\u0001\u0000\u0000\u0000\u0118\u0119"+
		"\u0001\u0000\u0000\u0000\u0119%\u0001\u0000\u0000\u0000\u011a\u0122\u0003"+
		"(\u0014\u0000\u011b\u011c\u0005\u001a\u0000\u0000\u011c\u011d\u00054\u0000"+
		"\u0000\u011d\u011e\u0003$\u0012\u0000\u011e\u011f\u00055\u0000\u0000\u011f"+
		"\u0122\u0001\u0000\u0000\u0000\u0120\u0122\u0005F\u0000\u0000\u0121\u011a"+
		"\u0001\u0000\u0000\u0000\u0121\u011b\u0001\u0000\u0000\u0000\u0121\u0120"+
		"\u0001\u0000\u0000\u0000\u0122\'\u0001\u0000\u0000\u0000\u0123\u0124\u0007"+
		"\u0001\u0000\u0000\u0124)\u0001\u0000\u0000\u0000\u0125\u0126\u0007\u0002"+
		"\u0000\u0000\u0126+\u0001\u0000\u0000\u0000\u0127\u0132\u0003.\u0017\u0000"+
		"\u0128\u0132\u00030\u0018\u0000\u0129\u0132\u00038\u001c\u0000\u012a\u0132"+
		"\u0003:\u001d\u0000\u012b\u0132\u0003<\u001e\u0000\u012c\u0132\u0003>"+
		"\u001f\u0000\u012d\u0132\u0003@ \u0000\u012e\u0132\u0003B!\u0000\u012f"+
		"\u0132\u0003D\"\u0000\u0130\u0132\u0003F#\u0000\u0131\u0127\u0001\u0000"+
		"\u0000\u0000\u0131\u0128\u0001\u0000\u0000\u0000\u0131\u0129\u0001\u0000"+
		"\u0000\u0000\u0131\u012a\u0001\u0000\u0000\u0000\u0131\u012b\u0001\u0000"+
		"\u0000\u0000\u0131\u012c\u0001\u0000\u0000\u0000\u0131\u012d\u0001\u0000"+
		"\u0000\u0000\u0131\u012e\u0001\u0000\u0000\u0000\u0131\u012f\u0001\u0000"+
		"\u0000\u0000\u0131\u0130\u0001\u0000\u0000\u0000\u0132-\u0001\u0000\u0000"+
		"\u0000\u0133\u0134\u0005\u0003\u0000\u0000\u0134\u0135\u0005F\u0000\u0000"+
		"\u0135\u0136\u0005\u0018\u0000\u0000\u0136\u0139\u0003$\u0012\u0000\u0137"+
		"\u0138\u00056\u0000\u0000\u0138\u013a\u0003H$\u0000\u0139\u0137\u0001"+
		"\u0000\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u013a\u013c\u0001"+
		"\u0000\u0000\u0000\u013b\u013d\u0005>\u0000\u0000\u013c\u013b\u0001\u0000"+
		"\u0000\u0000\u013c\u013d\u0001\u0000\u0000\u0000\u013d/\u0001\u0000\u0000"+
		"\u0000\u013e\u013f\u00034\u001a\u0000\u013f\u0140\u00032\u0019\u0000\u0140"+
		"\u0142\u0003H$\u0000\u0141\u0143\u0005>\u0000\u0000\u0142\u0141\u0001"+
		"\u0000\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u01431\u0001\u0000"+
		"\u0000\u0000\u0144\u0145\u0007\u0003\u0000\u0000\u01453\u0001\u0000\u0000"+
		"\u0000\u0146\u014a\u0005F\u0000\u0000\u0147\u0149\u00036\u001b\u0000\u0148"+
		"\u0147\u0001\u0000\u0000\u0000\u0149\u014c\u0001\u0000\u0000\u0000\u014a"+
		"\u0148\u0001\u0000\u0000\u0000\u014a\u014b\u0001\u0000\u0000\u0000\u014b"+
		"5\u0001\u0000\u0000\u0000\u014c\u014a\u0001\u0000\u0000\u0000\u014d\u014e"+
		"\u0005<\u0000\u0000\u014e\u0154\u0005F\u0000\u0000\u014f\u0150\u0005A"+
		"\u0000\u0000\u0150\u0151\u0003H$\u0000\u0151\u0152\u0005B\u0000\u0000"+
		"\u0152\u0154\u0001\u0000\u0000\u0000\u0153\u014d\u0001\u0000\u0000\u0000"+
		"\u0153\u014f\u0001\u0000\u0000\u0000\u01547\u0001\u0000\u0000\u0000\u0155"+
		"\u0156\u0005\r\u0000\u0000\u0156\u015a\u0003H$\u0000\u0157\u0159\u0003"+
		",\u0016\u0000\u0158\u0157\u0001\u0000\u0000\u0000\u0159\u015c\u0001\u0000"+
		"\u0000\u0000\u015a\u0158\u0001\u0000\u0000\u0000\u015a\u015b\u0001\u0000"+
		"\u0000\u0000\u015b\u0167\u0001\u0000\u0000\u0000\u015c\u015a\u0001\u0000"+
		"\u0000\u0000\u015d\u015e\u0005\u000e\u0000\u0000\u015e\u0162\u0003H$\u0000"+
		"\u015f\u0161\u0003,\u0016\u0000\u0160\u015f\u0001\u0000\u0000\u0000\u0161"+
		"\u0164\u0001\u0000\u0000\u0000\u0162\u0160\u0001\u0000\u0000\u0000\u0162"+
		"\u0163\u0001\u0000\u0000\u0000\u0163\u0166\u0001\u0000\u0000\u0000\u0164"+
		"\u0162\u0001\u0000\u0000\u0000\u0165\u015d\u0001\u0000\u0000\u0000\u0166"+
		"\u0169\u0001\u0000\u0000\u0000\u0167\u0165\u0001\u0000\u0000\u0000\u0167"+
		"\u0168\u0001\u0000\u0000\u0000\u0168\u0171\u0001\u0000\u0000\u0000\u0169"+
		"\u0167\u0001\u0000\u0000\u0000\u016a\u016e\u0005\u000f\u0000\u0000\u016b"+
		"\u016d\u0003,\u0016\u0000\u016c\u016b\u0001\u0000\u0000\u0000\u016d\u0170"+
		"\u0001\u0000\u0000\u0000\u016e\u016c\u0001\u0000\u0000\u0000\u016e\u016f"+
		"\u0001\u0000\u0000\u0000\u016f\u0172\u0001\u0000\u0000\u0000\u0170\u016e"+
		"\u0001\u0000\u0000\u0000\u0171\u016a\u0001\u0000\u0000\u0000\u0171\u0172"+
		"\u0001\u0000\u0000\u0000\u0172\u0173\u0001\u0000\u0000\u0000\u0173\u0174"+
		"\u0005\u0019\u0000\u0000\u01749\u0001\u0000\u0000\u0000\u0175\u0176\u0005"+
		"\u0010\u0000\u0000\u0176\u017a\u0003H$\u0000\u0177\u0179\u0003,\u0016"+
		"\u0000\u0178\u0177\u0001\u0000\u0000\u0000\u0179\u017c\u0001\u0000\u0000"+
		"\u0000\u017a\u0178\u0001\u0000\u0000\u0000\u017a\u017b\u0001\u0000\u0000"+
		"\u0000\u017b\u017d\u0001\u0000\u0000\u0000\u017c\u017a\u0001\u0000\u0000"+
		"\u0000\u017d\u017e\u0005\u0019\u0000\u0000\u017e;\u0001\u0000\u0000\u0000"+
		"\u017f\u0180\u0005\u0011\u0000\u0000\u0180\u0181\u0005F\u0000\u0000\u0181"+
		"\u0182\u0005\u0012\u0000\u0000\u0182\u0183\u0003H$\u0000\u0183\u0184\u0005"+
		"\u0013\u0000\u0000\u0184\u0187\u0003H$\u0000\u0185\u0186\u0005\u0014\u0000"+
		"\u0000\u0186\u0188\u0003H$\u0000\u0187\u0185\u0001\u0000\u0000\u0000\u0187"+
		"\u0188\u0001\u0000\u0000\u0000\u0188\u018c\u0001\u0000\u0000\u0000\u0189"+
		"\u018b\u0003,\u0016\u0000\u018a\u0189\u0001\u0000\u0000\u0000\u018b\u018e"+
		"\u0001\u0000\u0000\u0000\u018c\u018a\u0001\u0000\u0000\u0000\u018c\u018d"+
		"\u0001\u0000\u0000\u0000\u018d\u018f\u0001\u0000\u0000\u0000\u018e\u018c"+
		"\u0001\u0000\u0000\u0000\u018f\u0190\u0005\u0019\u0000\u0000\u0190=\u0001"+
		"\u0000\u0000\u0000\u0191\u0193\u0005\u0015\u0000\u0000\u0192\u0194\u0005"+
		">\u0000\u0000\u0193\u0192\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000"+
		"\u0000\u0000\u0194?\u0001\u0000\u0000\u0000\u0195\u0197\u0005\u0016\u0000"+
		"\u0000\u0196\u0198\u0005>\u0000\u0000\u0197\u0196\u0001\u0000\u0000\u0000"+
		"\u0197\u0198\u0001\u0000\u0000\u0000\u0198A\u0001\u0000\u0000\u0000\u0199"+
		"\u019b\u0005\u0017\u0000\u0000\u019a\u019c\u0003H$\u0000\u019b\u019a\u0001"+
		"\u0000\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000\u019c\u019e\u0001"+
		"\u0000\u0000\u0000\u019d\u019f\u0005>\u0000\u0000\u019e\u019d\u0001\u0000"+
		"\u0000\u0000\u019e\u019f\u0001\u0000\u0000\u0000\u019fC\u0001\u0000\u0000"+
		"\u0000\u01a0\u01a1\u0005\f\u0000\u0000\u01a1\u01a2\u0005F\u0000\u0000"+
		"\u01a2\u01a4\u0005?\u0000\u0000\u01a3\u01a5\u0003^/\u0000\u01a4\u01a3"+
		"\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000\u01a5\u01a6"+
		"\u0001\u0000\u0000\u0000\u01a6\u01a8\u0005@\u0000\u0000\u01a7\u01a9\u0005"+
		">\u0000\u0000\u01a8\u01a7\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001\u0000"+
		"\u0000\u0000\u01a9E\u0001\u0000\u0000\u0000\u01aa\u01ac\u0003H$\u0000"+
		"\u01ab\u01ad\u0005>\u0000\u0000\u01ac\u01ab\u0001\u0000\u0000\u0000\u01ac"+
		"\u01ad\u0001\u0000\u0000\u0000\u01adG\u0001\u0000\u0000\u0000\u01ae\u01af"+
		"\u0003J%\u0000\u01afI\u0001\u0000\u0000\u0000\u01b0\u01b5\u0003L&\u0000"+
		"\u01b1\u01b2\u0005\u001e\u0000\u0000\u01b2\u01b4\u0003L&\u0000\u01b3\u01b1"+
		"\u0001\u0000\u0000\u0000\u01b4\u01b7\u0001\u0000\u0000\u0000\u01b5\u01b3"+
		"\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6K\u0001"+
		"\u0000\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b8\u01bd\u0003"+
		"N\'\u0000\u01b9\u01ba\u0005\u001d\u0000\u0000\u01ba\u01bc\u0003N\'\u0000"+
		"\u01bb\u01b9\u0001\u0000\u0000\u0000\u01bc\u01bf\u0001\u0000\u0000\u0000"+
		"\u01bd\u01bb\u0001\u0000\u0000\u0000\u01bd\u01be\u0001\u0000\u0000\u0000"+
		"\u01beM\u0001\u0000\u0000\u0000\u01bf\u01bd\u0001\u0000\u0000\u0000\u01c0"+
		"\u01c5\u0003P(\u0000\u01c1\u01c2\u0007\u0004\u0000\u0000\u01c2\u01c4\u0003"+
		"P(\u0000\u01c3\u01c1\u0001\u0000\u0000\u0000\u01c4\u01c7\u0001\u0000\u0000"+
		"\u0000\u01c5\u01c3\u0001\u0000\u0000\u0000\u01c5\u01c6\u0001\u0000\u0000"+
		"\u0000\u01c6O\u0001\u0000\u0000\u0000\u01c7\u01c5\u0001\u0000\u0000\u0000"+
		"\u01c8\u01cd\u0003R)\u0000\u01c9\u01ca\u0007\u0005\u0000\u0000\u01ca\u01cc"+
		"\u0003R)\u0000\u01cb\u01c9\u0001\u0000\u0000\u0000\u01cc\u01cf\u0001\u0000"+
		"\u0000\u0000\u01cd\u01cb\u0001\u0000\u0000\u0000\u01cd\u01ce\u0001\u0000"+
		"\u0000\u0000\u01ceQ\u0001\u0000\u0000\u0000\u01cf\u01cd\u0001\u0000\u0000"+
		"\u0000\u01d0\u01d5\u0003T*\u0000\u01d1\u01d2\u0007\u0006\u0000\u0000\u01d2"+
		"\u01d4\u0003T*\u0000\u01d3\u01d1\u0001\u0000\u0000\u0000\u01d4\u01d7\u0001"+
		"\u0000\u0000\u0000\u01d5\u01d3\u0001\u0000\u0000\u0000\u01d5\u01d6\u0001"+
		"\u0000\u0000\u0000\u01d6S\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000"+
		"\u0000\u0000\u01d8\u01dd\u0003V+\u0000\u01d9\u01da\u0007\u0007\u0000\u0000"+
		"\u01da\u01dc\u0003V+\u0000\u01db\u01d9\u0001\u0000\u0000\u0000\u01dc\u01df"+
		"\u0001\u0000\u0000\u0000\u01dd\u01db\u0001\u0000\u0000\u0000\u01dd\u01de"+
		"\u0001\u0000\u0000\u0000\u01deU\u0001\u0000\u0000\u0000\u01df\u01dd\u0001"+
		"\u0000\u0000\u0000\u01e0\u01e1\u0007\b\u0000\u0000\u01e1\u01e4\u0003V"+
		"+\u0000\u01e2\u01e4\u0003X,\u0000\u01e3\u01e0\u0001\u0000\u0000\u0000"+
		"\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e4W\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e9\u0003\\.\u0000\u01e6\u01e8\u0003Z-\u0000\u01e7\u01e6\u0001\u0000"+
		"\u0000\u0000\u01e8\u01eb\u0001\u0000\u0000\u0000\u01e9\u01e7\u0001\u0000"+
		"\u0000\u0000\u01e9\u01ea\u0001\u0000\u0000\u0000\u01eaY\u0001\u0000\u0000"+
		"\u0000\u01eb\u01e9\u0001\u0000\u0000\u0000\u01ec\u01ed\u0005<\u0000\u0000"+
		"\u01ed\u01f8\u0005F\u0000\u0000\u01ee\u01ef\u0005A\u0000\u0000\u01ef\u01f0"+
		"\u0003H$\u0000\u01f0\u01f1\u0005B\u0000\u0000\u01f1\u01f8\u0001\u0000"+
		"\u0000\u0000\u01f2\u01f4\u0005?\u0000\u0000\u01f3\u01f5\u0003^/\u0000"+
		"\u01f4\u01f3\u0001\u0000\u0000\u0000\u01f4\u01f5\u0001\u0000\u0000\u0000"+
		"\u01f5\u01f6\u0001\u0000\u0000\u0000\u01f6\u01f8\u0005@\u0000\u0000\u01f7"+
		"\u01ec\u0001\u0000\u0000\u0000\u01f7\u01ee\u0001\u0000\u0000\u0000\u01f7"+
		"\u01f2\u0001\u0000\u0000\u0000\u01f8[\u0001\u0000\u0000\u0000\u01f9\u0201"+
		"\u0003b1\u0000\u01fa\u0201\u0003`0\u0000\u01fb\u0201\u0005F\u0000\u0000"+
		"\u01fc\u01fd\u0005?\u0000\u0000\u01fd\u01fe\u0003H$\u0000\u01fe\u01ff"+
		"\u0005@\u0000\u0000\u01ff\u0201\u0001\u0000\u0000\u0000\u0200\u01f9\u0001"+
		"\u0000\u0000\u0000\u0200\u01fa\u0001\u0000\u0000\u0000\u0200\u01fb\u0001"+
		"\u0000\u0000\u0000\u0200\u01fc\u0001\u0000\u0000\u0000\u0201]\u0001\u0000"+
		"\u0000\u0000\u0202\u0207\u0003H$\u0000\u0203\u0204\u0005=\u0000\u0000"+
		"\u0204\u0206\u0003H$\u0000\u0205\u0203\u0001\u0000\u0000\u0000\u0206\u0209"+
		"\u0001\u0000\u0000\u0000\u0207\u0205\u0001\u0000\u0000\u0000\u0207\u0208"+
		"\u0001\u0000\u0000\u0000\u0208_\u0001\u0000\u0000\u0000\u0209\u0207\u0001"+
		"\u0000\u0000\u0000\u020a\u020b\u0007\t\u0000\u0000\u020ba\u0001\u0000"+
		"\u0000\u0000\u020c\u020d\u0007\n\u0000\u0000\u020d\u020e\u0003d2\u0000"+
		"\u020ec\u0001\u0000\u0000\u0000\u020f\u0210\u0007\u000b\u0000\u0000\u0210"+
		"e\u0001\u0000\u0000\u0000:glt\u0080\u0089\u0091\u0094\u009b\u00a5\u00a8"+
		"\u00af\u00b5\u00bb\u00bf\u00c6\u00cf\u00d5\u00df\u00e9\u00f3\u00fa\u0102"+
		"\u010c\u0118\u0121\u0131\u0139\u013c\u0142\u014a\u0153\u015a\u0162\u0167"+
		"\u016e\u0171\u017a\u0187\u018c\u0193\u0197\u019b\u019e\u01a4\u01a8\u01ac"+
		"\u01b5\u01bd\u01c5\u01cd\u01d5\u01dd\u01e3\u01e9\u01f4\u01f7\u0200\u0207";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}