/*    */ package metapiga.trees;
/*    */ 
/*    */ import java.util.EnumMap;
/*    */ import java.util.Map;
/*    */ import metapiga.trees.exceptions.NullAncestorException;
/*    */ 
/*    */ public class ConsensusNode extends Node
/*    */ {
/* 16 */   private Map<Node.Neighbor, Double> branchStrengths = new EnumMap(Node.Neighbor.class);
/*    */ 
/*    */   public ConsensusNode()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ConsensusNode(Consensus.BiPartition p) {
/* 23 */     if (p.getCardinality() == 1) this.label = p.getTaxa(); 
/*    */   }
/*    */ 
/*    */   public void setBranchStrength(Node.Neighbor neighbor, double branchStrength)
/*    */   {
/* 27 */     this.branchStrengths.put(neighbor, Double.valueOf(branchStrength));
/* 28 */     ((ConsensusNode)this.neighbors.get(neighbor)).branchStrengths.put(getNeighborKey(neighbor), Double.valueOf(branchStrength));
/*    */   }
/*    */ 
/*    */   public void removeAllNeighbors() {
/* 32 */     this.neighbors.clear();
/* 33 */     this.branchLengths.clear();
/* 34 */     this.branchStrengths.clear();
/* 35 */     this.ancestor = null;
/*    */   }
/*    */ 
/*    */   public double getBranchStrength(Node.Neighbor neighbor) {
/* 39 */     if (this.branchStrengths.containsKey(neighbor)) {
/* 40 */       return ((Double)this.branchStrengths.get(neighbor)).doubleValue();
/*    */     }
/* 42 */     return 0.0D;
/*    */   }
/*    */ 
/*    */   public double getAncestorBranchStrength() throws NullAncestorException {
/* 46 */     if (this.ancestor == null) throw new NullAncestorException(this);
/* 47 */     if (this.branchStrengths.containsKey(this.ancestor)) return ((Double)this.branchStrengths.get(this.ancestor)).doubleValue();
/* 48 */     return 0.0D;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.ConsensusNode
 * JD-Core Version:    0.6.2
 */