package optimization;

import javax.swing.JDialog;
import metapiga.trees.Tree;
import metapiga.trees.exceptions.NullAncestorException;
import metapiga.trees.exceptions.UnrootableTreeException;

public abstract interface Optimizer
{
  public abstract Tree getOptimizedTree()
    throws NullAncestorException, UnrootableTreeException;

  public abstract Tree getOptimizedTreeWithProgress(JDialog paramJDialog, String paramString, int paramInt1, int paramInt2)
    throws NullAncestorException, UnrootableTreeException;

  public abstract Tree getOptimizedTreeWithProgress(JDialog paramJDialog, String paramString)
    throws NullAncestorException, UnrootableTreeException;

  public abstract void stop();
}

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.optimization.Optimizer
 * JD-Core Version:    0.6.2
 */