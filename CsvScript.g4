grammar CsvScript;

prog: stat+;

stat:
	expr NEWLINE				# commonExpr
	| assignment NEWLINE		# assignStat
	| persistentDecl NEWLINE	# persistDeclStat
	| NEWLINE					# emptyStat;

persistentDecl: 'persistent' ID;

assignment: ID '=' expr;

expr:
	expr op = ('*' | '/') expr		# MulDiv
	| expr op = ('+' | '-') expr	# AddSub
	| '-' expr						# NegativeNumber
	| 'transpose' expr				# Transpose
	| ID '[' expr ']'				# Array
	| ID '[' expr '][' expr ']'		# Matrix
	| INT							# Int
	| ID							# Var
	| '(' expr ')'					# Parens;

ID: [a-zA-Z][a-zA-Z0-9_]*;
INT: [0-9]+;
NEWLINE: '\r'? '\n';
COMMENT: '#' ~[\r\n]* -> skip;
WS: [ \t]+ -> skip;