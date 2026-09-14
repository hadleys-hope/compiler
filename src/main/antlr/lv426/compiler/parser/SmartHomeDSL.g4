grammar SmartHomeDSL;

@header {
package lv426.compiler.parser;
}

// ==========================================
// PARSER RULES
// ==========================================

program
    : declaration* handler* EOF
    ;

declaration
    : SENSOR name=IDENT COLON type SEMI           # SensorDecl
    | ACTUATOR name=IDENT COLON type SEMI         # ActuatorDecl
    | VAR name=IDENT COLON type ASSIGN expr SEMI  # VarDecl
    | CONST name=IDENT COLON type ASSIGN expr SEMI # ConstDecl
    ;

type
    : TYPE_INT     # IntType
    | TYPE_FLOAT   # FloatType
    | TYPE_BOOL    # BoolType
    | TYPE_STRING  # StringType
    ;

handler
    : ON INIT block                                      # OnInitHandler
    | ON TICK block                                      # OnTickHandler
    | ON CHANGE LPAREN name=IDENT RPAREN block           # OnChangeHandler
    | ON TIME LPAREN time=TIME_LITERAL RPAREN block      # OnTimeHandler
    ;

block
    : LBRACE statement* RBRACE
    ;

statement
    : assignStmt
    | ifStmt
    | whileStmt
    | callStmt
    ;

assignStmt
    : name=IDENT ASSIGN expr SEMI
    ;

ifStmt
    : IF condition=expr thenBlock=block (ELSE (elseIf=ifStmt | elseBlock=block))?
    ;

whileStmt
    : WHILE condition=expr block
    ;

callStmt
    : call SEMI
    ;

call
    : name=IDENT LPAREN (expr (COMMA expr)*)? RPAREN
    ;

// Выражения упорядочены по приоритету операторов (от наивысшего к низшему)
expr
    : MINUS expr                                                           # UnaryMinusExpr
    | left=expr op=(STAR | SLASH | PERCENT) right=expr                     # MulDivExpr
    | left=expr op=(PLUS | MINUS) right=expr                               # AddSubExpr
    | left=expr op=(EQ | NEQ | LT | LE | GT | GE) right=expr               # ComparisonExpr
    | NOT expr                                                             # NotExpr
    | left=expr AND right=expr                                             # AndExpr
    | left=expr OR right=expr                                              # OrExpr
    | call                                                                 # CallExpr
    | name=IDENT                                                           # VarExpr
    | INT_LITERAL                                                          # IntLiteralExpr
    | FLOAT_LITERAL                                                        # FloatLiteralExpr
    | STRING_LITERAL                                                       # StringLiteralExpr
    | boolLiteral                                                          # BoolLiteralExpr
    | LPAREN expr RPAREN                                                   # ParenExpr
    ;

boolLiteral
    : TRUE
    | FALSE
    | ON
    | OFF
    ;

// ==========================================
// LEXER RULES
// ==========================================

// Ключевые слова
SENSOR      : 'sensor';
ACTUATOR    : 'actuator';
VAR         : 'var';
CONST       : 'const';
ON          : 'on';
OFF         : 'off';
INIT        : 'init';
TICK        : 'tick';
CHANGE      : 'change';
TIME        : 'time';
IF          : 'if';
ELSE        : 'else';
WHILE       : 'while';
AND         : 'and';
OR          : 'or';
NOT         : 'not';
TRUE        : 'true';
FALSE       : 'false';
TYPE_INT    : 'int';
TYPE_FLOAT  : 'float';
TYPE_BOOL   : 'bool';
TYPE_STRING : 'string';

// Операторы и разделители
ASSIGN   : '=';
COLON    : ':';
SEMI     : ';';
COMMA    : ',';
LPAREN   : '(';
RPAREN   : ')';
LBRACE   : '{';
RBRACE   : '}';

PLUS     : '+';
MINUS    : '-';
STAR     : '*';
SLASH    : '/';
PERCENT  : '%';

EQ       : '==';
NEQ      : '!=';
LE       : '<=';
GE       : '>=';
LT       : '<';
GT       : '>';

// Литералы
TIME_LITERAL   : [0-9] [0-9] ':' [0-9] [0-9];
FLOAT_LITERAL  : [0-9]+ '.' [0-9]+;
INT_LITERAL    : [0-9]+;
STRING_LITERAL : '"' ( '\\"' | '\\n' | '\\\\' | ~["\\\r\n] )* '"';

IDENT          : [a-zA-Z] [a-zA-Z0-9_]*;

// Пропуск комментариев и пробельных символов
LINE_COMMENT   : '//' ~[\r\n]* -> skip;
WS             : [ \t\r\n]+    -> skip;