grammar HopeLang;

// ============================================================
// SOURCE FILE
// ============================================================

source
    : programDecl? topLevelDecl* EOF
    ;

programDecl
    : PROGRAM ID SEMI?
    ;

topLevelDecl
    : constDecl
    | globalVarDecl
    | structDecl
    | enumDecl
    | eventDecl
    | functionDecl
    | startHandler
    | eventHandler
    | everyHandler
    | atHandler
    ;


// ============================================================
// GLOBAL DATA
// ============================================================

constDecl
    : CONST ID OF typeRef ASSIGN expression SEMI?
    ;

globalVarDecl
    : VAR ID OF typeRef (ASSIGN expression)? SEMI?
    ;


// ============================================================
// STRUCTURES
// ============================================================

structDecl
    : STRUCT ID fieldDecl* END
    ;

fieldDecl
    : ID OF typeRef (ASSIGN expression)? SEMI?
    ;


// ============================================================
// ENUMS
// ============================================================

enumDecl
    : ENUM ID enumMember+ END
    ;

enumMember
    : ID (COMMA | SEMI)?
    ;


// ============================================================
// EVENTS
// ============================================================

eventDecl
    : EVENT ID LPAREN parameterList? RPAREN SEMI?
    ;

startHandler
    : ON START statement* END
    ;

eventHandler
    : ON ID LPAREN identifierList? RPAREN statement* END
    ;

everyHandler
    : EVERY durationLiteral statement* END
    ;

atHandler
    : AT durationLiteral statement* END
    ;

identifierList
    : ID (COMMA ID)*
    ;


// ============================================================
// FUNCTIONS
// ============================================================

functionDecl
    : DEF ID LPAREN parameterList? RPAREN OF typeRef statement* END
    ;

parameterList
    : parameter (COMMA parameter)*
    ;

parameter
    : ID OF typeRef
    ;


// ============================================================
// TYPES
// ============================================================

typeRef
    : typeAtom (LBRACK arraySize RBRACK)?
    ;

typeAtom
    : primitiveType
    | LIST LT typeRef GT
    | ID
    ;

primitiveType
    : INT_TYPE
    | REAL_TYPE
    | BOOL_TYPE
    | STRING_TYPE
    | TIME_TYPE
    | VOID_TYPE
    ;

arraySize
    : INT_LITERAL
    | ID
    ;


// ============================================================
// STATEMENTS
// ============================================================

statement
    : localVarDecl
    | assignmentStatement
    | ifStatement
    | whileStatement
    | forStatement
    | breakStatement
    | continueStatement
    | returnStatement
    | emitStatement
    | expressionStatement
    ;

localVarDecl
    : VAR ID OF typeRef (ASSIGN expression)? SEMI?
    ;

assignmentStatement
    : lvalue assignmentOperator expression SEMI?
    ;

assignmentOperator
    : ASSIGN
    | PLUS_ASSIGN
    | MINUS_ASSIGN
    | MUL_ASSIGN
    | DIV_ASSIGN
    | MOD_ASSIGN
    ;

lvalue
    : ID lvalueSuffix*
    ;

lvalueSuffix
    : DOT ID
    | LBRACK expression RBRACK
    ;


// ============================================================
// CONTROL FLOW
// ============================================================

ifStatement
    : IF expression
        statement*
      (ELIF expression
        statement*)*
      (ELSE
        statement*)?
      END
    ;

whileStatement
    : WHILE expression
      statement*
      END
    ;

// `to` is inclusive.
forStatement
    : FOR ID FROM expression TO expression (STEP expression)?
      statement*
      END
    ;

breakStatement
    : BREAK SEMI?
    ;

continueStatement
    : CONTINUE SEMI?
    ;

returnStatement
    : RETURN expression? SEMI?
    ;

emitStatement
    : EMIT ID LPAREN argumentList? RPAREN SEMI?
    ;

expressionStatement
    : expression SEMI?
    ;


// ============================================================
// EXPRESSIONS
// ============================================================

expression
    : logicalOr
    ;

logicalOr
    : logicalAnd (OR logicalAnd)*
    ;

logicalAnd
    : equality (AND equality)*
    ;

equality
    : comparison ((EQ | NEQ) comparison)*
    ;

comparison
    : additive ((LT | LTE | GT | GTE) additive)*
    ;

additive
    : multiplicative ((PLUS | MINUS) multiplicative)*
    ;

multiplicative
    : unary ((MUL | DIV | MOD) unary)*
    ;

unary
    : (NOT | MINUS | PLUS) unary
    | postfix
    ;

postfix
    : primary postfixSuffix*
    ;

postfixSuffix
    : DOT ID
    | LBRACK expression RBRACK
    | LPAREN argumentList? RPAREN
    ;

primary
    : durationLiteral
    | literal
    | ID
    | LPAREN expression RPAREN
    ;

argumentList
    : expression (COMMA expression)*
    ;

literal
    : REAL_LITERAL
    | INT_LITERAL
    | STRING_LITERAL
    | TRUE
    | FALSE
    ;


// ============================================================
// TIME
// ============================================================

durationLiteral
    : (INT_LITERAL | REAL_LITERAL) timeUnit
    ;

timeUnit
    : MILLISECOND_UNIT
    | SECOND_UNIT
    | MINUTE_UNIT
    | HOUR_UNIT
    | DAY_UNIT
    ;


// ============================================================
// KEYWORDS
// ============================================================

PROGRAM     : 'program';

CONST       : 'const';
VAR         : 'var';
STRUCT      : 'struct';
ENUM        : 'enum';
EVENT       : 'event';
DEF         : 'def';

ON          : 'on';
START       : 'start';
EVERY       : 'every';
AT          : 'at';
EMIT        : 'emit';

IF          : 'if';
ELIF        : 'elif';
ELSE        : 'else';
WHILE       : 'while';
FOR         : 'for';
FROM        : 'from';
TO          : 'to';
STEP        : 'step';
BREAK       : 'break';
CONTINUE    : 'continue';
RETURN      : 'return';

OF          : 'of';
END         : 'end';
LIST        : 'list';

TRUE        : 'true';
FALSE       : 'false';
AND         : 'and';
OR          : 'or';
NOT         : 'not';


// ============================================================
// BUILT-IN TYPES
// ============================================================

INT_TYPE    : 'int';
REAL_TYPE   : 'real';
BOOL_TYPE   : 'bool';
STRING_TYPE : 'string';
TIME_TYPE   : 'time';
VOID_TYPE   : 'void';


// ============================================================
// TIME UNITS
// ============================================================

MILLISECOND_UNIT : 'ms';
SECOND_UNIT      : 'sec';
MINUTE_UNIT      : 'min';
HOUR_UNIT        : 'hour';
DAY_UNIT         : 'day';


// ============================================================
// OPERATORS
// ============================================================

PLUS_ASSIGN  : '+=';
MINUS_ASSIGN : '-=';
MUL_ASSIGN   : '*=';
DIV_ASSIGN   : '/=';
MOD_ASSIGN   : '%=';

EQ           : '==';
NEQ          : '!=';
LTE          : '<=';
GTE          : '>=';
LT           : '<';
GT           : '>';
ASSIGN       : '=';

PLUS         : '+';
MINUS        : '-';
MUL          : '*';
DIV          : '/';
MOD          : '%';

DOT          : '.';
COMMA        : ',';
SEMI         : ';';
LPAREN       : '(';
RPAREN       : ')';
LBRACK       : '[';
RBRACK       : ']';


// ============================================================
// LITERALS
// ============================================================

REAL_LITERAL
    : DIGIT+ '.' DIGIT+ ([eE] [+-]? DIGIT+)?
    | DIGIT+ [eE] [+-]? DIGIT+
    ;

INT_LITERAL
    : DIGIT+
    ;

STRING_LITERAL
    : '"' (ESCAPE_SEQUENCE | ~["\\\r\n])* '"'
    ;


// ============================================================
// IDENTIFIERS
// ============================================================

ID
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;


// ============================================================
// COMMENTS / WHITESPACE
// ============================================================

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

fragment ESCAPE_SEQUENCE
    : '\\' [btnfr"'\\]
    | '\\u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

fragment DIGIT
    : [0-9]
    ;
