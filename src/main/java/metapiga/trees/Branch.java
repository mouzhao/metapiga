/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ 
/*     */ public class Branch
/*     */ {
/*     */   private final Node node;
/*     */   private final Node.Neighbor neighbor;
/*     */   private Node otherNode;
/*     */   private Node.Neighbor otherNeighbor;
/*     */ 
/*     */   public Branch(Node node, Node.Neighbor neighbor)
/*     */   {
/*  22 */     this.node = node;
/*  23 */     this.neighbor = neighbor;
/*  24 */     this.otherNode = node.getNeighbor(neighbor);
/*  25 */     this.otherNeighbor = node.getNeighborKey(neighbor);
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj) {
/*  29 */     Branch b = (Branch)obj;
/*  30 */     if ((this.node == b.node) && (this.neighbor == b.neighbor)) return true;
/*  31 */     return (this.otherNode == b.node) && (b.node.getNeighbor(b.neighbor) == this.node);
/*     */   }
/*     */ 
/*     */   public int hashCode() {
/*  35 */     int hash = 42;
/*  36 */     hash = 31 * hash + (this.node.hashCode() + this.otherNode.hashCode());
/*  37 */     return hash;
/*     */   }
/*     */ 
/*     */   public String toString() {
/*  41 */     return this.node.label + " -> " + this.otherNode.label;
/*     */   }
/*     */ 
/*     */   public String toMirrorString() {
/*  45 */     return this.otherNode.label + " -> " + this.node.label;
/*     */   }
/*     */ 
/*     */   public Node getNode() {
/*  49 */     return this.node;
/*     */   }
/*     */ 
/*     */   public Node getOtherNode() {
/*  53 */     return this.otherNode;
/*     */   }
/*     */ 
/*     */   public Node.Neighbor getNeighbor() {
/*  57 */     return this.neighbor;
/*     */   }
/*     */ 
/*     */   public Node.Neighbor getOtherNeighbor() {
/*  61 */     return this.otherNeighbor;
/*     */   }
/*     */ 
/*     */   public double getLength() {
/*  65 */     return this.node.getBranchLength(this.neighbor);
/*     */   }
/*     */ 
/*     */   public void setLength(double branchLength) {
/*  69 */     this.node.setBranchLength(this.neighbor, branchLength);
/*     */   }
/*     */ 
/*     */   public Branch getMirrorBranch() {
/*  73 */     return new Branch(this.otherNode, this.otherNeighbor);
/*     */   }
/*     */ 
/*     */   public Branch getNeighborBranch(Node.Neighbor neigh) {
/*  77 */     return new Branch(this.node, neigh).getMirrorBranch();
/*     */   }
/*     */ 
/*     */   public List<Branch> getAllNeighborBranches() {
/*  81 */     List branches = new ArrayList();
/*  82 */     for (Node.Neighbor n : this.node.getNeighborKeys()) {
/*  83 */       if (n != this.neighbor) branches.add(getNeighborBranch(n));
/*     */     }
/*  85 */     return branches;
/*     */   }
/*     */ 
/*     */   public boolean isTipBranch() {
/*  89 */     return (this.node.isLeaf()) || (this.otherNode.isLeaf());
/*     */   }
/*     */ 
/*     */   public Branch detach()
/*     */     throws TooManyNeighborsException
/*     */   {
/* 102 */     List neighBranch = getAllNeighborBranches();
/* 103 */     Branch neigh1 = (Branch)neighBranch.get(0);
/* 104 */     Branch neigh2 = (Branch)neighBranch.get(1);
/* 105 */     double branchLength = neigh1.getLength() + neigh2.getLength();
/* 106 */     this.node.removeNeighborButKeepBranchLength(neigh1.node);
/* 107 */     this.node.removeNeighborButKeepBranchLength(neigh2.node);
/* 108 */     Node.Neighbor key = neigh1.node.addNeighbor(neigh2.node);
/* 109 */     neigh1.node.setBranchLength(key, branchLength);
/* 110 */     return new Branch(neigh1.node, key);
/*     */   }
/*     */ 
/*     */   public void graft(Branch branch)
/*     */     throws TooManyNeighborsException
/*     */   {
/* 120 */     double branchLength = getLength();
/* 121 */     this.node.removeNeighborButKeepBranchLength(this.otherNode);
/* 122 */     Node.Neighbor key = branch.node.addNeighbor(this.node);
/* 123 */     branch.node.setBranchLength(key, branchLength / 2.0D);
/* 124 */     key = branch.node.addNeighbor(this.otherNode);
/* 125 */     branch.node.setBranchLength(key, branchLength / 2.0D);
/* 126 */     this.otherNode = branch.node;
/* 127 */     this.otherNeighbor = this.node.getNeighborKey(this.neighbor);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.Branch
 * JD-Core Version:    0.6.2
 */