package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.graph.*;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  // JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType booleanType = factory.getClassType("boolean");
  JavaClassType classType = factory.getClassType("Test");
  MethodSignature methodSignature = new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l4 = JavaJimple.newLocal("l4", booleanType);
  Local l0hash1 = JavaJimple.newLocal("l0#1", intType);
  Local l0hash2 = JavaJimple.newLocal("l0#2", intType);
  Local l0hash3 = JavaJimple.newLocal("l0#3", intType);
  Local l1hash2 = JavaJimple.newLocal("l1#2", intType);
  Local l1hash4 = JavaJimple.newLocal("l1#4", intType);

  /**
   * int a = 0; int b = 0; a = a + 1; b = b + 1;
   *
   * <pre>
   * 1. l0 = 0
   * 2. l1 = 1
   * 3. l0 = l0 + 1
   * 4. l1 = l1 + 1
   * 5. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l1#2 = 1
   * 3. l0#3 = l0#1 + 1
   * 4. l1#4 = l1#2 + 1
   * 5. return
   * </pre>
   */
  @Test
  public void testLocalSplitterForMultilocals() {

    Body body = createMultilocalsBody();
    System.out.println(body.getStmtGraph().getEntryPoint());
    //Fixme: entryPoint in ImmutableStmtGraph is not copied.
    //printGraph(body);
    /**
    Body.BodyBuilder builder = Body.builder(body);
    Body newBody = builder.build();
    System.out.println(newBody.getStmtGraph().getEntryPoint());
    printGraph(newBody);*/

    //LocalSplitter localSplitter = new LocalSplitter();
    //Body newBody = localSplitter.interceptBody(body);
    //Body expectedBody = createExpectedMuiltilocalsBody();

    // check newBody's locals
    //assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    //assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    //assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /** bodycreater for multilocals */
  private Body createMultilocalsBody() {

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1);
    builder.setLocals(locals);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();
    builder.setTraps(traps);

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 =
            JavaJimple.newAssignStmt(
                    l1, JavaJimple.newAddExpr(l1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    //set graph-nodes
    builder.addStmt(stmt1);
    builder.addStmt(stmt2);
    builder.addStmt(stmt3);
    builder.addStmt(stmt4);
    builder.addStmt(ret);

    // set graph-edges
    builder.addFlow(stmt1, stmt2);
    builder.addFlow(stmt2, stmt3);
    builder.addFlow(stmt3, stmt4);
    builder.addFlow(stmt4, ret);

    //set first stmt
    builder.setFirstStmt(stmt1);

    // build position
    Position position = NoPositionInformation.getInstance();
    builder.setPosition(position);

    Body body = builder.build();
    return body;
  }

  /**
   * int a = 0; if(a<0) a = a + 1; else {a = a+ 1; a = a +1;} return a
   *
   * <pre>
   * 1. l0 = 0
   * 2. if l0 >= 0 goto l0 = l0 -1
   * 3. l0 = l0 + 1
   * 4. goto  [?= return l0#2]
   * 5. l0 = l0 - 1
   * 6. l0 = l0 + 2
   * 7. return l0
   * </pre>
   *
   * to:
   * alternation1
   * <pre>
   * 1. l0#1 = 0
   * 2. if l0#1 >= 0 goto l0#3 = l0#1 -1
   * 3. l0#2 = l0#1 + 1
   * 4. goto  [?= return l0]
   * 5. l0#3 = l0#1 - 1
   * 6. l0#2 = l0#3 + 2
   * 7. return l0#2
   * </pre>
   *
   */
/**
  @Test

  public void testLocalSplitterForBinaryBranches() {

    Body body = createBBBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);

    Body expectedBody1 = createExpectedBBBody1();

    System.out.println(newBody.getStmtGraph());
    System.out.println(newBody.branches);

    System.out.println(expectedBody1.getStmtGraph());
    System.out.println(expectedBody1.branches);


     // check newBody's locals
    assertLocalsEquiv(expectedBody1.getLocals(), newBody.getLocals());

     // check newBody's first stmt
    assertTrue(expectedBody1.getFirstStmt().equivTo(newBody.getFirstStmt()));

     // check newBody's stmtGraph
    //assertStmtGraphEquiv(expectedBody1.getStmtGraph(), newBody.getStmtGraph());

  }*/

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   * 1. l0 = 0
   * 2. l4 = l0 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0 + 1
   * 5. l0 = l1
   * 6. l2 = l0
   * 7. l3 = l0 + 1
   * 8. l0 = l3
   * 9. goto [?= l4 = l0 < 10]
   * 10. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l4 = l0#1 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0#1 + 1
   * 5. l0#2 = l1
   * 6. l2 = l0#2
   * 7. l3 = l0#2 + 1
   * 8. l0#1 = l3
   * 9. goto [?= l4 = l0#1 < 10]
   * 10. return
   * </pre>
   */
  /**
  @Test
  public void testLocalSplitterForLoop() {

    Body body = createLoopBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedLoopBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }*/



  /**
  private Body createExpectedMuiltilocalsBody() {
    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(l0);
    locals.add(l1);
    locals.add(l0hash1);
    locals.add(l1hash2);
    locals.add(l0hash3);
    locals.add(l1hash4);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 = JavaJimple.newAssignStmt(l1hash2, IntConstant.getInstance(1), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newAssignStmt(
            l0hash3,
            JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l1hash4,
            JavaJimple.newAddExpr(l1hash2, IntConstant.getInstance(1)),
            noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // build stmtGraph
    MutableGraph<Stmt> stmtGraph =
        GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(ret);

    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, ret);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(ms, locals, traps, stmtGraph, branches, startingStmt, position);
  }**/

  /** bodycreater for BinaryBranches */
  /**private Body createBBBody() {

    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(l0);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
            JavaJimple.newIfStmt(
                    JavaJimple.newGeExpr(l0, IntConstant.getInstance(0)),
                    noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newSubExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
            JavaJimple.newAssignStmt(
                    l0, JavaJimple.newAddExpr(l0, IntConstant.getInstance(2)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l0, noStmtPositionInfo);

    // build stmtGraph
    StmtGraph stmtGraph = new StmtGraph();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(stmt5);
    stmtGraph.addNode(stmt6);
    stmtGraph.addNode(ret);

    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, ret);
    stmtGraph.putEdge(stmt2, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, ret);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(stmt3);
    branches1.add(stmt5);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(ret);
    branches.put(stmt2, branches1);
    branches.put(stmt4, branches2);

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(ms, locals, traps, stmtGraph, branches, startingStmt, position);
  }

  private Body createExpectedBBBody1() {
    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(l0);
    locals.add(l0hash1);
    locals.add(l0hash2);
    locals.add(l0hash3);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
            JavaJimple.newIfStmt(
                    JavaJimple.newGeExpr(l0hash1, IntConstant.getInstance(0)),
                    noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0hash2, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
            JavaJimple.newAssignStmt(
                    l0hash3, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
            JavaJimple.newAssignStmt(
                    l0hash2, JavaJimple.newAddExpr(l0hash3, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l0hash2, noStmtPositionInfo);

    // build stmtGraph
    MutableGraph<Stmt> stmtGraph =
            GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(stmt5);
    stmtGraph.addNode(stmt6);
    stmtGraph.addNode(ret);

    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, ret);
    stmtGraph.putEdge(stmt2, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, ret);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(stmt3);
    branches1.add(stmt5);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(ret);
    branches.put(stmt2, branches1);
    branches.put(stmt4, branches2);

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(ms, locals, traps, stmtGraph, branches, startingStmt, position);
  }

  private Body createExpectedBBBody2() {
    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(l0);
    locals.add(l0hash1);
    locals.add(l0hash2);
    locals.add(l0hash3);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
            JavaJimple.newIfStmt(
                    JavaJimple.newGeExpr(l0hash1, IntConstant.getInstance(0)),
                    noStmtPositionInfo);
    Stmt stmt3 =
            JavaJimple.newAssignStmt(
                    l0hash3, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt4 = JavaJimple.newGotoStmt(noStmtPositionInfo);
    Stmt stmt5 =
            JavaJimple.newAssignStmt(
                    l0hash2, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt6 =
            JavaJimple.newAssignStmt(
                    l0hash3, JavaJimple.newAddExpr(l0hash2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(l0hash3, noStmtPositionInfo);

    // build stmtGraph
    MutableGraph<Stmt> stmtGraph =
            GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(stmt5);
    stmtGraph.addNode(stmt6);
    stmtGraph.addNode(ret);

    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt4, ret);
    stmtGraph.putEdge(stmt2, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, ret);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(stmt3);
    branches1.add(stmt5);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(ret);
    branches.put(stmt2, branches1);
    branches.put(stmt4, branches2);

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(ms, locals, traps, stmtGraph, branches, startingStmt, position);
  }*/

  /** bodycreater for Loop */
  /**private Body createLoopBody() {

    Body.BodyBuilder builder = Body.builder();

    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(l0);
    locals.add(l1);
    locals.add(l2);
    locals.add(l3);
    locals.add(l4);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    Stmt stmt1 = JavaJimple.newAssignStmt(l0, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newAssignStmt(
            l4, JavaJimple.newLtExpr(l0, IntConstant.getInstance(10)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newIfStmt(
            JavaJimple.newEqExpr(l4, IntConstant.getInstance(0)),
            noStmtPositionInfo); // goto stmt10_loop
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l0, l1, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newAssignStmt(l2, l0, noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l3, JavaJimple.newAddExpr(l0, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newAssignStmt(l0, l3, noStmtPositionInfo);
    Stmt stmt9 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // build stmtGraph
    MutableGraph<Stmt> stmtGraph =
        GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(stmt5);
    stmtGraph.addNode(stmt6);
    stmtGraph.addNode(stmt7);
    stmtGraph.addNode(stmt8);
    stmtGraph.addNode(stmt9);
    stmtGraph.addNode(ret);
    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt3, ret);
    stmtGraph.putEdge(stmt4, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, stmt7);
    stmtGraph.putEdge(stmt7, stmt8);
    stmtGraph.putEdge(stmt8, stmt9);
    stmtGraph.putEdge(stmt9, stmt2);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(ret);
    branches1.add(stmt4);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(stmt2);
    branches.put(stmt3, branches1);
    branches.put(stmt9, branches2);

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(ms, locals, traps, stmtGraph, branches, startingStmt, position);
  }

  private Body createExpectedLoopBody() {
    // build expected set locals
    Set<Local> expectedLocals = new HashSet<>();
    expectedLocals.add(l0);
    expectedLocals.add(l1);
    expectedLocals.add(l2);
    expectedLocals.add(l3);
    expectedLocals.add(l4);
    expectedLocals.add(l0hash1);
    expectedLocals.add(l0hash2);

    // build expected traps (an empty list)
    List<Trap> expectedTraps = Collections.emptyList();

    // build Stmts
    Stmt stmt1 = JavaJimple.newAssignStmt(l0hash1, IntConstant.getInstance(0), noStmtPositionInfo);
    Stmt stmt2 =
        JavaJimple.newAssignStmt(
            l4, JavaJimple.newLtExpr(l0hash1, IntConstant.getInstance(10)), noStmtPositionInfo);
    Stmt stmt3 =
        JavaJimple.newIfStmt(
            JavaJimple.newEqExpr(l4, IntConstant.getInstance(0)),
            noStmtPositionInfo); // goto stmt4 and ret
    Stmt stmt4 =
        JavaJimple.newAssignStmt(
            l1, JavaJimple.newAddExpr(l0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt5 = JavaJimple.newAssignStmt(l0hash2, l1, noStmtPositionInfo);
    Stmt stmt6 = JavaJimple.newAssignStmt(l2, l0hash2, noStmtPositionInfo);
    Stmt stmt7 =
        JavaJimple.newAssignStmt(
            l3, JavaJimple.newAddExpr(l0hash2, IntConstant.getInstance(1)), noStmtPositionInfo);
    Stmt stmt8 = JavaJimple.newAssignStmt(l0hash1, l3, noStmtPositionInfo);
    Stmt stmt9 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
    Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

    // build expected stmtGraph
    MutableGraph<Stmt> expectedStmtGraph =
        GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    expectedStmtGraph.addNode(stmt1);
    expectedStmtGraph.addNode(stmt2);
    expectedStmtGraph.addNode(stmt3);
    expectedStmtGraph.addNode(stmt4);
    expectedStmtGraph.addNode(stmt5);
    expectedStmtGraph.addNode(stmt6);
    expectedStmtGraph.addNode(stmt7);
    expectedStmtGraph.addNode(stmt8);
    expectedStmtGraph.addNode(stmt9);
    expectedStmtGraph.addNode(ret);
    expectedStmtGraph.putEdge(stmt1, stmt2);
    expectedStmtGraph.putEdge(stmt2, stmt3);
    expectedStmtGraph.putEdge(stmt3, stmt4);
    expectedStmtGraph.putEdge(stmt3, ret);
    expectedStmtGraph.putEdge(stmt4, stmt5);
    expectedStmtGraph.putEdge(stmt5, stmt6);
    expectedStmtGraph.putEdge(stmt6, stmt7);
    expectedStmtGraph.putEdge(stmt7, stmt8);
    expectedStmtGraph.putEdge(stmt8, stmt9);
    expectedStmtGraph.putEdge(stmt9, stmt2);

    // build the expected map branches
    Map<Stmt, List<Stmt>> expectedBranches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(ret);
    branches1.add(stmt4);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(stmt2);
    expectedBranches.put(stmt3, branches1);
    expectedBranches.put(stmt9, branches2);

    // build the expected firstStmt
    Stmt expectedFirstStmt = stmt1;
    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(
            ms,
        expectedLocals,
        expectedTraps,
        expectedStmtGraph,
        expectedBranches,
        expectedFirstStmt,
        position);
  }

  private static void assertLocalsEquiv(Set<Local> expected, Set<Local> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    boolean isEqual = true;
    for (Local local : actual) {
      if (!expected.contains(local)) {
        isEqual = false;
        break;
      }
    }
    assertTrue(isEqual);
  }

  private static void assertStmtGraphEquiv(Graph<Stmt> expected, Graph<Stmt> actual) {
    assertNotNull(expected);
    assertNotNull(actual);

    Set<Stmt> actualStmts = actual.nodes();
    Set<Stmt> expectedStmts = expected.nodes();
    assertEquals(expectedStmts.size(), actualStmts.size());

    Set<EndpointPair<Stmt>> actualEdges = actual.edges();
    Set<EndpointPair<Stmt>> expectedEdges = expected.edges();
    assertEquals(expectedEdges.size(), actualEdges.size());

    boolean isEqual = true;
    boolean isContains = false;
    for (Stmt stmt : actualStmts) {
      for (Stmt expectedStmt : expectedStmts) {
        if (stmt.equivTo(expectedStmt)) {
          isContains = true;
        }
      }
      if (!isContains) {
        isEqual = false;
        break;
      }
      isContains = false;
    }

    if (isEqual) {
      isContains = false;
      for (EndpointPair<Stmt> edge : actualEdges) {
        Stmt souce = edge.source();
        Stmt target = edge.target();
        for (EndpointPair<Stmt> expectedEdge : expectedEdges) {
          Stmt expectedSouce = edge.source();
          Stmt expectedTarget = edge.target();
          if (souce.equivTo(expectedSouce) && target.equivTo(expectedTarget)) {
            isContains = true;
          }
        }
        if (!isContains) {
          isEqual = false;
          break;
        }
        isContains = false;
      }
    }
    assertTrue(isEqual);
  }

  private static void assertStmtGraphEquiv(Graph<Stmt> expected1, Graph<Stmt> expected2, Graph<Stmt> actual) {
    assertNotNull(expected1);
    assertNotNull(expected2);
    assertNotNull(actual);

    Set<Stmt> actualStmts = actual.nodes();
    Set<EndpointPair<Stmt>> actualEdges = actual.edges();

    Set<Stmt> expectedStmts1 = expected1.nodes();
    assertEquals(expectedStmts1.size(), actualStmts.size());
    Set<Stmt> expectedStmts2 = expected2.nodes();
    assertEquals(expectedStmts2.size(), actualStmts.size());

    Set<EndpointPair<Stmt>> expectedEdges1 = expected1.edges();
    assertEquals(expectedEdges1.size(), actualEdges.size());
    Set<EndpointPair<Stmt>> expectedEdges2 = expected1.edges();
    assertEquals(expectedEdges2.size(), actualEdges.size());

    boolean isEqual = true;
    boolean isContains = false;
    for (Stmt stmt : actualStmts) {
      for (Stmt expectedStmt : expectedStmts1) {
        if (stmt.equivTo(expectedStmt)) {
          isContains = true;
        }
      }
      if (!isContains) {
        isEqual = false;
        break;
      }
      isContains = false;
    }

    if (isEqual) {
      isContains = false;
      for (EndpointPair<Stmt> edge : actualEdges) {
        Stmt souce = edge.source();
        Stmt target = edge.target();
        for (EndpointPair<Stmt> expectedEdge : expectedEdges1) {
          Stmt expectedSouce = edge.source();
          Stmt expectedTarget = edge.target();
          if (souce.equivTo(expectedSouce) && target.equivTo(expectedTarget)) {
            isContains = true;
          }
        }
        if (!isContains) {
          isEqual = false;
          break;
        }
        isContains = false;
      }
    }

    if(!isEqual){
      isEqual = true;
      isContains = false;
      for (Stmt stmt : actualStmts) {
        for (Stmt expectedStmt : expectedStmts2) {
          if (stmt.equivTo(expectedStmt)) {
            isContains = true;
          }
        }
        if (!isContains) {
          isEqual = false;
          break;
        }
        isContains = false;
      }

      if (isEqual) {
        isContains = false;
        for (EndpointPair<Stmt> edge : actualEdges) {
          Stmt souce = edge.source();
          Stmt target = edge.target();
          for (EndpointPair<Stmt> expectedEdge : expectedEdges2) {
            Stmt expectedSouce = edge.source();
            Stmt expectedTarget = edge.target();
            if (souce.equivTo(expectedSouce) && target.equivTo(expectedTarget)) {
              isContains = true;
            }
          }
          if (!isContains) {
            isEqual = false;
            break;
          }
          isContains = false;
        }
      }
    }
    assertTrue(isEqual);
  }*/
  private void printGraph(Body body){
    for(Stmt node: body.getStmtGraph().nodes()){
      System.out.println("predecessor: " + body.getStmtGraph().predecessors(node));
      System.out.println(node);
      System.out.println("successor: " + body.getStmtGraph().successors(node));
      System.out.println("_______________________________________________________");
    }
  }
}
