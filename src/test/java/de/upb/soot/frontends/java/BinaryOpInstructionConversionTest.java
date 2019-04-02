package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.EquivTo;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class BinaryOpInstructionConversionTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("BinaryOperations");
  }

  private static void assertCorrectPos(
      PositionInfo info,
      int firstLine,
      int firstCol,
      int firstOffset,
      int lastLine,
      int lastCol,
      int lastOffset) {
    Position pos = info.getStmtPosition();
    assertEquals(firstLine, pos.getFirstLine());
    assertEquals(firstCol, pos.getFirstCol());
    assertEquals(firstOffset, pos.getFirstOffset());
    assertEquals(lastLine, pos.getLastLine());
    assertEquals(lastCol, pos.getLastCol());
    assertEquals(lastOffset, pos.getLastOffset());
  }

  private static void assertEquiv(EquivTo expected, EquivTo actual) {
    if (!expected.equivTo(actual)) {
      throw new AssertionError("Expected '" + expected + "', actual is " + actual);
    }
  }

  private static <T> void assertInstanceOfSatisfying(
      Object actual, Class<T> tClass, Consumer<T> checker) {
    try {
      checker.accept(tClass.cast(actual));
    } catch (ClassCastException e) {
      throw new AssertionError(
          "Expected value of type "
              + tClass
              + (actual != null ? ", got type " + actual.getClass() + " with value " : ", got ")
              + actual);
    }
  }

  @Test
  public void testAddByte() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "addByte", declareClassSig, "byte", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<IStmt> stmts = new ArrayList<>(body.getStmts());
    assertEquals(8, stmts.size());

    assertInstanceOfSatisfying(
        stmts.get(0),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("r0", RefType.getInstance("BinaryOperations")));
          assertEquiv(
              stmt.getRightOp(), Jimple.newThisRef(RefType.getInstance("BinaryOperations")));
          assertCorrectPos(stmt.getPositionInfo(), 3, 19, 93, 3, 20, 94);
        });

    assertInstanceOfSatisfying(
        stmts.get(1),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$b0", ByteType.getInstance()));
          assertEquiv(stmt.getRightOp(), Jimple.newParameterRef(ByteType.getInstance(), 0));
          assertCorrectPos(stmt.getPositionInfo(), 3, 19, 93, 3, 20, 94);
        });

    assertInstanceOfSatisfying(
        stmts.get(2),
        JIdentityStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$b1", ByteType.getInstance()));
          assertEquiv(stmt.getRightOp(), Jimple.newParameterRef(ByteType.getInstance(), 1));
          assertCorrectPos(stmt.getPositionInfo(), 3, 19, 93, 3, 20, 94);
        });

    assertInstanceOfSatisfying(
        stmts.get(3),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$i0", IntType.getInstance()));
          assertEquiv(
              stmt.getRightOp(),
              new JCastExpr(new Local("$b0", ByteType.getInstance()), IntType.getInstance()));
          assertCorrectPos(stmt.getPositionInfo(), 3, 19, 93, 3, 20, 94);
        });

    assertInstanceOfSatisfying(
        stmts.get(4),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$i1", IntType.getInstance()));
          assertEquiv(
              stmt.getRightOp(),
              new JCastExpr(new Local("$b1", ByteType.getInstance()), IntType.getInstance()));
          assertCorrectPos(stmt.getPositionInfo(), 3, 23, 97, 3, 24, 98);
        });

    assertInstanceOfSatisfying(
        stmts.get(5),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$i2", IntType.getInstance()));
          assertEquiv(
              stmt.getRightOp(),
              new JAddExpr(
                  new Local("$i0", IntType.getInstance()),
                  new Local("$i1", IntType.getInstance())));
          assertCorrectPos(stmt.getPositionInfo(), 3, 19, 93, 3, 24, 98);
        });

    assertInstanceOfSatisfying(
        stmts.get(6),
        JAssignStmt.class,
        stmt -> {
          assertEquiv(stmt.getLeftOp(), new Local("$b2", ByteType.getInstance()));
          assertEquiv(
              stmt.getRightOp(),
              new JCastExpr(new Local("$i2", IntType.getInstance()), ByteType.getInstance()));
          assertCorrectPos(stmt.getPositionInfo(), 3, 11, 85, 3, 25, 99);
        });

    assertInstanceOfSatisfying(
        stmts.get(7),
        JReturnStmt.class,
        stmt -> {
          assertEquiv(stmt.getOp(), new Local("$b2", ByteType.getInstance()));
          assertCorrectPos(stmt.getPositionInfo(), 3, 4, 78, 3, 26, 100);
        });
  }

  @Test
  public void testAddDouble() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "addDouble", declareClassSig, "double", Arrays.asList("double", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testMulDouble() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "mulDouble", declareClassSig, "double", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSubChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "subChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testMulShort() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "mulShort", declareClassSig, "short", Arrays.asList("short", "short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testDivInt() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "divInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testModChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "modChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testIncShort() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "incShort", declareClassSig, "short", Collections.singletonList("short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testDecInt() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "decInt", declareClassSig, "int", Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testOrLong() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "orLong", declareClassSig, "long", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testXorInt() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "xorInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAndChar() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "andChar", declareClassSig, "char", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testLShiftByte() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "lshiftByte", declareClassSig, "byte", Collections.singletonList("byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testRShiftShort() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "rshiftShort", declareClassSig, "short", Arrays.asList("short", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testNegLong() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "negLong", declareClassSig, "long", Collections.singletonList("long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testZeroFillRshiftInt() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "zeroFillRshiftInt", declareClassSig, "int", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testLogicalAnd() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "logicalAnd", declareClassSig, "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testLogicalOr() {
    // TODO: failed test
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "logicalOr", declareClassSig, "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testNot() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "not", declareClassSig, "boolean", Collections.singletonList("boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "equal", declareClassSig, "boolean", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testNotEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "notEqual", declareClassSig, "boolean", Arrays.asList("float", "float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testGreater() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greater", declareClassSig, "boolean", Arrays.asList("double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSmaller() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smaller", declareClassSig, "boolean", Arrays.asList("long", "long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testGreaterEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "greaterEqual", declareClassSig, "boolean", Arrays.asList("char", "char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testSmallerEqual() {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "smallerEqual", declareClassSig, "boolean", Arrays.asList("byte", "byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }
}
