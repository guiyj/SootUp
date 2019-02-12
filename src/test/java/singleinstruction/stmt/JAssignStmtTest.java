package singleinstruction.stmt;

import categories.Java8Test;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class JAssignStmtTest {

    @Test
    public void test(){

        Value numConst1 = IntConstant.getInstance(42);
        Value numConst2 = IntConstant.getInstance(33102);

        Local local = new Local("$i0", IntType.INSTANCE);
        Local field = new Local("i2", IntType.INSTANCE);

        IStmt lStmt = new JAssignStmt(local, numConst1);
        IStmt fStmt = new JAssignStmt(field, numConst1);
        IStmt deepStmt = new JAssignStmt(local, new JAddExpr(numConst1, numConst2));

        // equivTo : equals
        Assert.assertTrue(lStmt.equivTo(new JAssignStmt(local, numConst1)));
        Assert.assertTrue(lStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.INSTANCE) , IntConstant.getInstance(42))));

        Assert.assertTrue(deepStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.INSTANCE) ,  new JAddExpr(numConst1, numConst2) )));

        // equivTo: switched operands
        Assert.assertFalse(lStmt.equivTo(new JAssignStmt(local, numConst2)));
        Assert.assertFalse(deepStmt.equivTo(new JAssignStmt(local, new JAddExpr(numConst2, numConst1))));

        // equivTo: different operands
        Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, numConst1)));
        Assert.assertFalse(lStmt.equivTo(new JAssignStmt(new Local("$i100differentname", IntType.INSTANCE) , IntConstant.getInstance(42))));
        Assert.assertFalse(lStmt.equivTo(new JAssignStmt(new Local("$i0", LongType.INSTANCE) , LongConstant.getInstance(42))));

        // equivTo: different depth
        Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, new JAddExpr(numConst1, numConst2) )) );

        // toString
        Assert.assertEquals("$i0 = 42", lStmt.toString());
        Assert.assertEquals("i2 = 42", fStmt.toString());
        Assert.assertEquals("$i0 = 42 + 33102", deepStmt.toString());

    }


}
