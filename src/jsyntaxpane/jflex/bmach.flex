/*
 * Copyright 2010 Georgios Migdos cyberpython@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class BMachLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%ignorecase
%type Token

%{
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public BMachLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PARAN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {EndOfLineComment}

EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/*instructions*/
HexInst = 0 x ([1-9]|[a-c]|[A-C]) ([0-9]|[a-f]|[A-F]) ([0-9]|[a-f]|[A-F]) ([0-9]|[a-f]|[A-F])
BinInst = ( 0001 | 0010 | 0011 | 0100 | 0101 | 0110 | 0111 | 1000 | 1001 | 1010 | 1011 | 1100 ) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1) (0|1)

%%

<YYINITIAL> {

  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { }

  /* Hex Instructions */
  {HexInst}                   { return token(TokenType.KEYWORD); }

  /* Binary Instructions */
  {BinInst}                   { return token(TokenType.KEYWORD2); }
}


  /* escape sequences */

/*  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}*/

/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }
