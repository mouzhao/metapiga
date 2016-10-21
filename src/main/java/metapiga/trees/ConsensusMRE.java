/*    */ package metapiga.trees;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.HashSet;
/*    */ import java.util.Map;
/*    */ import java.util.Set;
/*    */ import metapiga.parameters.Parameters;
/*    */ import metapiga.trees.exceptions.NullAncestorException;
/*    */ import metapiga.trees.exceptions.UnrootableTreeException;
/*    */ 
/*    */ public class ConsensusMRE
/*    */ {
/* 21 */   private final Map<String, Double> mainConsensus = new HashMap();
/* 22 */   private final Map<String, Double> consensus = new HashMap();
/*    */ 
/*    */   public void addConsensus(Tree consensusTree, Parameters parameters, boolean main)
/*    */     throws UnrootableTreeException, NullAncestorException
/*    */   {
/* 28 */     Map current = (main) || (this.mainConsensus.isEmpty()) ? this.mainConsensus : this.consensus;
/* 29 */     current.clear();
/* 30 */     consensusTree.root();
/* 31 */     Consensus consensus = new Consensus(consensusTree, parameters.dataset);
/* 32 */     for (Node n : consensusTree.getInodes())
/* 33 */       if (n != consensusTree.getRoot())
/*    */       {
/* 35 */         Branch b = new Branch(n, n.getAncestorKey());
/* 36 */         current.put(consensus.getBiPartition(b).toString(), Double.valueOf(((ConsensusNode)n).getAncestorBranchStrength()));
/*    */       }
/*    */   }
/*    */ 
/*    */   public double meanRelativeError()
/*    */   {
/* 42 */     if (this.consensus.isEmpty()) return 1.0D;
/* 43 */     double mre = 0.0D;
/* 44 */     double nBranch = this.consensus.keySet().size();
/* 45 */     Set partitions = new HashSet();
/* 46 */     partitions.addAll(this.mainConsensus.keySet());
/* 47 */     partitions.addAll(this.consensus.keySet());
/* 48 */     for (String partition : partitions) {
/* 49 */       if ((this.mainConsensus.containsKey(partition)) && (this.consensus.containsKey(partition))) {
/* 50 */         double score1 = ((Double)this.mainConsensus.get(partition)).doubleValue();
/* 51 */         double score2 = ((Double)this.consensus.get(partition)).doubleValue();
/* 52 */         if ((score1 == 0.0D) && (score2 == 0.0D)) {
/* 53 */           mre += 1.0D;
/*    */         } else {
/* 55 */           double norm = 1.0D / (score1 > score2 ? score1 : score2);
/* 56 */           score1 *= norm;
/* 57 */           score2 *= norm;
/* 58 */           mre += Math.abs(score1 - score2);
/*    */         }
/*    */       } else {
/* 61 */         mre += 1.0D;
/*    */       }
/*    */     }
/* 64 */     return mre / nBranch;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.ConsensusMRE
 * JD-Core Version:    0.6.2
 */