grammar torqlang;

// This grammar was derived from the hand-written Torq lexer
// and parser for documentation purposes and may not capture
// all the nuances of the real parser.

//**************//
// PARSER RULES //
//**************//

// Next version changes:
//     'new' operator for type application
//     Module support
//     Package statement
//     Metadata support
//     Cast expressions, e.g. ident::Int32
//     Spread expressions, e.g. {customer..., person...}
//     protocol statements
//     type statements (Obj is a marker type)
//     Actor implements
//     Stream handlers
//     Type parameters on actor, func, and proc
//     Import is now 'package.{' instead of 'package['
//     Record "rest" patterns, e.g. {'name': name, rest...}
//     Tuple "rest" patterns, e.g. [value1, value2, rest...}
//     Dangling commas in record and tuple values, e.g. {'name': name,}
//     Variable input arguments, e.g. func MyFunc(params::Any...) -> Any
//     Type expressions for protocol, record, and tuple
//     Type extension (+) and union (|)
//     Array type constructors, e.g. Array[Int32] or Array[Array[Int32]]
//     Native actor declarations
//     Weak keywords: 'as' | 'ask' | 'handle' | 'implements' | 'meta' | 'native' | 'protocol' | 'stream' | 'tell'

module: package? stmt_or_expr* EOF;

package: 'package' ident ('.' ident)*;

// - - - - -//
// Language //
// - - - - -//

stmt_or_expr: meta? assign ';'*;

meta: 'meta' '#' (meta_rec | meta_tuple);

meta_rec: '{' meta_field (',' meta_field)* '}';

meta_tuple: '[' meta_value (',' meta_value)* ']';

meta_field: STR_LITERAL ':' meta_value;

meta_value: meta_rec | meta_tuple | bool |
            STR_LITERAL | INT_LITERAL;

assign: or ('=' or | ':=' or)?;

or: and ('||' and)*;

and: relational ('&&' relational)*;

relational: sum (relational_oper sum)*;

relational_oper: '<' | '>' | '==' | '!=' | '<=' | '>=';

sum: product (sum_oper product)*;

sum_oper: '+' | '-';

product: unary (product_oper unary)*;

product_oper: '*' | '/' | '%';

unary: select_or_apply | (('-' | '!') unary)+;

select_or_apply: access ('.' access | '[' stmt_or_expr ']' |
                 '(' arg_list? ')')*;

access: '@' access | cast;

cast: construct ('::' construct)*;

// The '...' is used in spread expressions
construct: keyword | ident '...'? | value;

keyword: act | actor | begin | 'break' | case | 'continue' |
         for | func | group | if | import_ | local | new | proc |
         package | protocol | return | 'self' | 'skip' | spawn |
         throw | try | type | var | while;

act: 'act' stmt_or_expr+ 'end';

actor: 'actor' ident? type_param_list? '(' pat_list? ')'
       ('implements' protocol_and)? 'in'
       (stmt_or_expr | handler)+ 'end';

handler: 'handle' (tell_handler | ask_handler | stream_handler);

tell_handler: 'tell' pat ('when' stmt_or_expr)?
              'in' stmt_or_expr+ 'end';

ask_handler: 'ask' pat ('when' stmt_or_expr)? return_type_anno?
             'in' stmt_or_expr+ 'end';

stream_handler: 'stream' pat ('when' stmt_or_expr)?
                return_type_anno?
                'in' stmt_or_expr+ 'end';

begin: 'begin' stmt_or_expr+ 'end';

case: 'case' stmt_or_expr
      ('of' pat ('when' stmt_or_expr)? 'then' stmt_or_expr+)+
      ('else' stmt_or_expr+)? 'end';

for: 'for' pat 'in' stmt_or_expr 'do' stmt_or_expr+ 'end';

func: 'func' ident? type_param_list?
      '(' pat_list? ')' return_type_anno?
      'in' stmt_or_expr+ 'end';

group: '(' stmt_or_expr+ ')';

if: 'if' stmt_or_expr 'then' stmt_or_expr+
    ('elseif' stmt_or_expr 'then' stmt_or_expr+)*
    ('else' stmt_or_expr+)? 'end';

// `import` is already an ANTLR4 keyword, therefore we create
// our keyword with a trailing underscore
import_: 'import' ident ('.' ident)*
         ('.{' import_alias (',' import_alias)* '}')?;

import_alias: ident ('as' ident)?;

local: 'local' var_decl (',' var_decl)*
       'in' stmt_or_expr+ 'end';

new: 'new' ident type_arg_list? '(' arg_list? ')';

var_decl: pat ('=' stmt_or_expr)?;

proc: 'proc' ident? type_param_list?
      '(' pat_list? ')'
      'in' stmt_or_expr+ 'end';

throw: 'throw' stmt_or_expr;

return: 'return' stmt_or_expr?;

spawn: 'spawn' '(' arg_list ')';

try: 'try' stmt_or_expr+ ('catch' pat 'then' stmt_or_expr+)*
     ('finally' stmt_or_expr+)? 'end';

var: 'var' var_decl (',' var_decl)*;

while: 'while' stmt_or_expr 'do' stmt_or_expr+ 'end';

arg_list: stmt_or_expr (',' stmt_or_expr)*;

pat_list: pat (',' pat)*;

pat: rec_pat | tuple_pat |
     (label_pat ('#' (rec_pat | tuple_pat))?) |
     INT_LITERAL | (ident var_type_anno?);

label_pat: ('~' ident) | bool | STR_LITERAL | 'eof' | 'null';

// The '...' is used to capture the "rest" of fields
rec_pat: '{' (field_pat (',' field_pat)* (',' ident? '...')?)? '}';

// The '...' is used to capture the "rest" of values
tuple_pat: '[' (pat (',' pat)* (',' ident? '...')?)? ']';

field_pat: (feat_pat ':')? pat;

feat_pat: ('~' ident) | bool | INT_LITERAL | STR_LITERAL |
          'eof' | 'null';

value: rec_value | tuple_value |
       (label_value ('#' (rec_value | tuple_value))?) |
       INT_LITERAL | CHAR_LITERAL | FLT_LITERAL | DEC_LITERAL;

label_value: ident | bool | STR_LITERAL | 'eof' | 'null';

rec_value: '{' (field_value (',' field_value)* ','?)? '}';

tuple_value: '[' (value (',' value)* ','?)? ']';

field_value: (feat_value ':')? stmt_or_expr;

feat_value: ident | bool | INT_LITERAL | STR_LITERAL |
            'eof' | 'null';

// The '...' is used to declare a variable argument
// parameter in method declarations
var_type_anno: '::' struct_or '...'? | '...';

return_type_anno: '->' struct_or;

bool: 'true' | 'false';

// Weak keywords: 'as' | 'ask' | ... | 'tell'
ident: IDENT | 'as' | 'ask' | 'handle' | 'implements' |
       'meta' | 'native' | 'protocol' | 'stream' | 'tell';

// - - - - - - //
// Type System //
// - - - - - - //

type: 'type' ident type_param_list? '=' struct_or;

type_param_list: '[' type_param (',' type_param)* ']';

type_param: ident (('<:' | '>:') struct_or)?;

type_arg_list: '[' struct_or (',' struct_or)* ']';

struct_or: struct_and ('|' struct_and)*;

struct_and: struct_expr ('&' struct_expr)*;

struct_expr: ident (type_arg_list | '#' (rec_type_body |
             tuple_type_body))? |
             (bool | STR_LITERAL | 'eof' | 'null')
             '#' (rec_type_body | tuple_type_body)? |
             rec_type_body | tuple_type_body | proc_type |
             func_type;

rec_type_body: '{' (field_type (',' field_type)* ','?)? '}';

tuple_type_body: '[' (struct_or (',' struct_or)* ','?)? ']';

field_type: (feat_value ':')? struct_or;

func_type: 'func' ('(' pat_type_list? ')' | pat) return_type_anno;

proc_type: 'proc' ('(' pat_type_list? ')' | pat);

pat_type_list: pat (',' pat)*;

protocol: 'protocol' ident type_param_list? '=' protocol_and;

protocol_and: protocol_expr ('&' protocol_expr)*;

protocol_expr: ident type_arg_list? | protocol_body;

protocol_body: '{' protocol_handler
               (',' protocol_handler)* ','? '}';

protocol_handler: 'handle' (protocol_tell_handler |
                  protocol_ask_handler |
                  protocol_stream_handler);

protocol_tell_handler: 'tell' pat;

protocol_ask_handler: 'ask' pat return_type_anno;

protocol_stream_handler: 'stream' pat return_type_anno;

//*************//
// LEXER RULES //
//*************//

IDENT: ((ALPHA | '_') (ALPHA_NUMERIC | '_')*) |
       '`' (~('`' | '\\') | ESC_SEQ)+ '`';

CHAR_LITERAL: '&\'' (~'\\' | ESC_SEQ) '\'';

STR_LITERAL: STR_SINGLE_QUOTED | STR_DOUBLE_QUOTED;
STR_SINGLE_QUOTED: '\'' (~('\'' | '\\') | ESC_SEQ)* '\'';
STR_DOUBLE_QUOTED: '"' (~('"' | '\\') | ESC_SEQ)* '"';

INT_LITERAL: DIGIT+ [lL]? |
             ('0x' | '0X') HEX_DIGIT+ [lL]?;

FLT_LITERAL: DIGIT+ '.' DIGIT+
             ([eE] ('+' | '-')? DIGIT+)? [fFdD]?;

DEC_LITERAL: DIGIT+ ('.' DIGIT+)? [mM]?;

//
// Define lexical rules for comments and whitespace
//

LINE_COMMENT : '//' .*? '\r'? '\n' -> skip;
COMMENT : '/*' .*? '*/' -> skip;
WS: [ \r\n\t\f\b]+ -> skip;

//
// Define reusable lexical patterns
//

fragment ALPHA_NUMERIC: ALPHA | DIGIT;

fragment ALPHA: UPPER_CASE_ALPHA | LOWER_CASE_ALPHA;
fragment UPPER_CASE_ALPHA: [A-Z];
fragment LOWER_CASE_ALPHA: [a-z];

fragment DIGIT: [0-9];
fragment NZ_DIGIT: [1-9];
fragment HEX_DIGIT: (DIGIT | [a-f] | [A-F]);

fragment ESC_SEQ: '\\' ([rntfb'"`\\] |
                  'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT);
