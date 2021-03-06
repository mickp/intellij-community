// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.jsonpath

import com.intellij.jsonpath.lexer.JsonPathLexer
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class JsonPathLexerTest : LexerTestCase() {
  private val ROOT: String = "\$"

  override fun createLexer(): Lexer = JsonPathLexer()
  override fun getDirPath(): String = "unused"

  fun testRoot() {
    doTest("\$", "\$ ('\$')")
    doTest("@", "@ ('@')")
    doTest("name", "IDENTIFIER ('name')")

    doTest("\$[0]", """
      $ROOT ('$ROOT')
      [ ('[')
      INTEGER_NUMBER ('0')
      ] (']')
    """.trimIndent())

    doTest("@[-100]", """
      @ ('@')
      [ ('[')
      INTEGER_NUMBER ('-100')
      ] (']')
    """.trimIndent())
  }

  fun testDottedPaths() {
    doTest("\$.path", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('path')
    """.trimIndent())

    doTest("\$..path", """
      $ROOT ('$ROOT')
      .. ('..')
      IDENTIFIER ('path')
    """.trimIndent())

    doTest("@.path", """
      @ ('@')
      . ('.')
      IDENTIFIER ('path')
    """.trimIndent())

    doTest("@..path", """
      @ ('@')
      .. ('..')
      IDENTIFIER ('path')
    """.trimIndent())

    doTest("\$.path", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('path')
    """.trimIndent())

    doTest("\$.long.path.with.root", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('long')
      . ('.')
      IDENTIFIER ('path')
      . ('.')
      IDENTIFIER ('with')
      . ('.')
      IDENTIFIER ('root')
    """.trimIndent())

    doTest("@.long.path.with.eval", """
      @ ('@')
      . ('.')
      IDENTIFIER ('long')
      . ('.')
      IDENTIFIER ('path')
      . ('.')
      IDENTIFIER ('with')
      . ('.')
      IDENTIFIER ('eval')
    """.trimIndent())
  }

  fun testQuotedPaths() {
    doTest("\$['quoted']['path']", """
      $ROOT ('$ROOT')
      [ ('[')
      SINGLE_QUOTED_STRING (''quoted'')
      ] (']')
      [ ('[')
      SINGLE_QUOTED_STRING (''path'')
      ] (']')
    """.trimIndent())
    doTest("\$.['quoted'].path", """
      $ROOT ('$ROOT')
      . ('.')
      [ ('[')
      SINGLE_QUOTED_STRING (''quoted'')
      ] (']')
      . ('.')
      IDENTIFIER ('path')
    """.trimIndent())
    doTest("\$.[\"quo\\ted\"]", """
      $ROOT ('$ROOT')
      . ('.')
      [ ('[')
      DOUBLE_QUOTED_STRING ('"quo\ted"')
      ] (']')
    """.trimIndent())
  }

  fun testFilterExpression() {
    doTest("\$.demo[?(@.filter > 2)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('filter')
      WHITE_SPACE (' ')
      GT_OP ('>')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('2')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.filter == 7.2)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('filter')
      WHITE_SPACE (' ')
      EQ_OP ('==')
      WHITE_SPACE (' ')
      DOUBLE_NUMBER ('7.2')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.filter != 'value')]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('filter')
      WHITE_SPACE (' ')
      NE_OP ('!=')
      WHITE_SPACE (' ')
      SINGLE_QUOTED_STRING (''value'')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.filter == true)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('filter')
      WHITE_SPACE (' ')
      EQ_OP ('==')
      WHITE_SPACE (' ')
      true ('true')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.filter != false)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('filter')
      WHITE_SPACE (' ')
      NE_OP ('!=')
      WHITE_SPACE (' ')
      false ('false')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.null != null)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('null')
      WHITE_SPACE (' ')
      NE_OP ('!=')
      WHITE_SPACE (' ')
      null ('null')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?('a' in @.in)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      SINGLE_QUOTED_STRING (''a'')
      WHITE_SPACE (' ')
      NAMED_OP ('in')
      WHITE_SPACE (' ')
      @ ('@')
      . ('.')
      IDENTIFIER ('in')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testBooleanOperations() {
    doTest("\$.demo[?(@.a>=10 && $.b<=2)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('a')
      GE_OP ('>=')
      INTEGER_NUMBER ('10')
      WHITE_SPACE (' ')
      AND_OP ('&&')
      WHITE_SPACE (' ')
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('b')
      LE_OP ('<=')
      INTEGER_NUMBER ('2')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testIndexExpression() {
    doTest("\$.demo[(@.length - 1)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('length')
      WHITE_SPACE (' ')
      MINUS_OP ('-')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('1')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testRegexLiteral() {
    doTest("\$.demo[?(@.attr =~ /[a-z]/)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      RE_OP ('=~')
      WHITE_SPACE (' ')
      REGEX_STRING ('/[a-z]/')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.attr =~ /[0-9]/i)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      RE_OP ('=~')
      WHITE_SPACE (' ')
      REGEX_STRING ('/[0-9]/i')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@.attr =~ /[0-9]/iu)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      RE_OP ('=~')
      WHITE_SPACE (' ')
      REGEX_STRING ('/[0-9]/iu')
      ) (')')
      ] (']')
    """.trimIndent())

    doTest("\$.demo[?(@ =~ /test/U)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      WHITE_SPACE (' ')
      RE_OP ('=~')
      WHITE_SPACE (' ')
      REGEX_STRING ('/test/U')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testWildcardMultiplyOperators() {
    doTest("\$.demo[*].demo[?(@.attr * 2 == 10)]", """
      $ROOT ('$ROOT')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      * ('*')
      ] (']')
      . ('.')
      IDENTIFIER ('demo')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      MULTIPLY_OP ('*')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('2')
      WHITE_SPACE (' ')
      EQ_OP ('==')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('10')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testArrayLiteralsInCondition() {
    doTest("@[?(@.attr in [1, 2, 3])]", """
      @ ('@')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      NAMED_OP ('in')
      WHITE_SPACE (' ')
      [ ('[')
      INTEGER_NUMBER ('1')
      , (',')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('2')
      , (',')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('3')
      ] (']')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testObjectLiteralsInCondition() {
    doTest("\$[?(@.attr in {'a': 1, 'b': { }, 'c': [1, 2]})]", """
      $ROOT ('${'$'}')
      [ ('[')
      ? ('?')
      ( ('(')
      @ ('@')
      . ('.')
      IDENTIFIER ('attr')
      WHITE_SPACE (' ')
      NAMED_OP ('in')
      WHITE_SPACE (' ')
      { ('{')
      SINGLE_QUOTED_STRING (''a'')
      : (':')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('1')
      , (',')
      WHITE_SPACE (' ')
      SINGLE_QUOTED_STRING (''b'')
      : (':')
      WHITE_SPACE (' ')
      { ('{')
      WHITE_SPACE (' ')
      } ('}')
      , (',')
      WHITE_SPACE (' ')
      SINGLE_QUOTED_STRING (''c'')
      : (':')
      WHITE_SPACE (' ')
      [ ('[')
      INTEGER_NUMBER ('1')
      , (',')
      WHITE_SPACE (' ')
      INTEGER_NUMBER ('2')
      ] (']')
      } ('}')
      ) (')')
      ] (']')
    """.trimIndent())
  }

  fun testNamedOperator() {
    doTest("\$.x[?(@.a in \$.b)].in.avg()")
  }

  fun testArrayOnTheLeft() {
    doTest("@[?([1] contains 1)]")
  }
}