package mmj.transforms;

import java.util.*;

import mmj.lang.*;

public class ImplicationInfo extends DBInfo {
    /** The information about equivalence rules */
    private final EquivalenceInfo eqInfo;

    /**
     * The map from type to equivalence implication rule: A & A <-> B => B.
     * set.mm has only one element: (wff, <->)
     */
    private final Map<Cnst, Assrt> eqImplications;

    /** The list of implication operators : A & A -> B => B. */
    private final Map<Stmt, Assrt> implOp;

    public ImplicationInfo(final EquivalenceInfo eqInfo,
        final List<Assrt> assrtList, final TrOutput output, final boolean dbg)
    {
        super(output, dbg);
        this.eqInfo = eqInfo;
        implOp = new HashMap<Stmt, Assrt>();
        eqImplications = new HashMap<Cnst, Assrt>();
        for (final Assrt assrt : assrtList)
            findImplicationRules(assrt);
    }

    /**
     * Filters implication rules, like A & A -> B => B. Some of implications
     * could be equivalence operators (<-> in set.mm). Note: now we consider
     * only left to right direction. The rules in the form A & B <- A => B are
     * not supported! Also we don't support the reverse hypotheses order, for
     * example A -> B & A => B
     * 
     * @param assrt the candidate
     */
    protected void findImplicationRules(final Assrt assrt) {
        final ParseTree assrtTree = assrt.getExprParseTree();

        final LogHyp[] logHyps = assrt.getLogHypArray();
        if (logHyps.length != 2)
            return;

        if (assrtTree.getMaxDepth() != 1)
            return;

        final ParseNode resNode = assrtTree.getRoot();

        if (!TrUtil.isVarNode(resNode))
            return;

        final ParseNode log0Root = logHyps[0].getExprParseTree().getRoot();
        final ParseNode log1Root = logHyps[1].getExprParseTree().getRoot();

        final ParseNode preHyp;
        final ParseNode implHyp;
        if (TrUtil.isVarNode(log0Root)) {
            preHyp = log0Root;
            implHyp = log1Root;
        }
        else if (TrUtil.isVarNode(log1Root)) {
            preHyp = log1Root;
            implHyp = log0Root;
        }
        else
            return;

        if (implHyp.getChild().length != 2)
            return;

        if (implHyp.getChild()[0].getStmt() != preHyp.getStmt())
            return;

        if (implHyp.getChild()[1].getStmt() != resNode.getStmt())
            return;

        final Stmt stmt = implHyp.getStmt();

        if (implOp.containsKey(stmt))
            return;

        if (preHyp != log0Root) {
            output.dbgMessage(dbg,
                "I-TR-DBG the current implementation doesn't support A->B & A"
                    + " hypotheses order, assert %s", assrt);
            return;
        }

        output.dbgMessage(dbg, "I-TR-DBG implication assrt: %s: %s", assrt,
            assrt.getFormula());
        implOp.put(stmt, assrt);

        if (!eqInfo.isEquivalence(stmt))
            return;

        final Cnst type = resNode.getStmt().getTyp();

        if (eqImplications.containsKey(type))
            return;

        output.dbgMessage(dbg, "I-TR-DBG implication equal assrt: %s: %s", type,
            assrt);

        eqImplications.put(type, assrt);
    }

    public Assrt getEqImplication(final Cnst type) {
        return eqImplications.get(type);
    }

    public Assrt getImplOp(final Stmt op) {
        return implOp.get(op);
    }
}
