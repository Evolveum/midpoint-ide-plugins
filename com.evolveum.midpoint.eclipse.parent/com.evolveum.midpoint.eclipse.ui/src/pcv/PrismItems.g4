// corresponding Java files have to be generated from this .g4 file manually 
grammar PrismItems;
start : item | pcv | PRV | PPV ;
item : prismContainer | prismReference | prismProperty ;
prismContainer : 'PC' '(' name ')' ':' '[' (pcv (',' pcv)*)? ']';
prismReference : 'PrismReference' '(' name ')' ':' '[' (PRV (',' PRV)*)? ']';
prismProperty : 'PP' '(' name ')' ':' '[' (PPV (',' PPV)*)? ']';
pcv : 'PCV' '(' ( name ) ')' ':' (('[' (item (',' item)*)? ']')|ID) ;
PRV : 'PRV' '(' 'oid' '=' ID (',' 'targetType' '=' NAMESPACE? ID)?  (ESC | ~(')'))* ')' ;
PPV : 'PPV' '(' ID ':' (ESC | ~(')'))* ')' ;

name : (NAMESPACE)? ID ;
NAMESPACE : '{' [a-zA-Z0-9_:./\-]+ '}' ;
ID : [a-zA-Z0-9\-_]+ ;
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

fragment
ESC
  :  '\\' ('\\' | ')')
  ;