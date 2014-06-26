package mmj.transforms;

import mmj.lang.ParseNode;

public class ConstSubst {
    /** This array contains null elements. */
    public final ParseNode[] constMap;
    public final int hash;

    public ConstSubst(final ParseNode[] constMap) {
        this.constMap = constMap;
        hash = calcHashCode();
    }

    public boolean isEmpty() {
        for (final ParseNode node : constMap)
            if (node != null)
                return false;
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ConstSubst))
            return false;
        final ConstSubst that = (ConstSubst)obj;

        assert constMap.length == that.constMap.length;
        for (int i = 0; i < constMap.length; i++)
            if (constMap[i] != that.constMap[i]) {
                if (constMap[i] == null || that.constMap[i] == null)
                    return false;
                if (!constMap[i].isDeepDup(that.constMap[i]))
                    return false;
            }
        return true;
    }

    protected int calcHashCode() {
        int hash = 0;
        for (final ParseNode node : constMap)
            if (node != null)
                hash ^= node.deepHashCode();
        return hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}