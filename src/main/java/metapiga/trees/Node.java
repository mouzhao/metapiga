/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.EnumMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ import metapiga.trees.exceptions.UnknownNeighborException;
/*     */ 
/*     */ public class Node
/*     */ {
/*     */   public static final double MINIMAL_BRANCH_LENGTH = 0.0001D;
/*     */   public static final double MAXIMAL_BRANCH_LENGTH = 5.0D;
/*  27 */   protected Map<Neighbor, Node> neighbors = new EnumMap(Neighbor.class);
/*  28 */   protected Map<Neighbor, Double> branchLengths = new EnumMap(Neighbor.class);
/*     */   protected String label;
/*     */   protected Neighbor ancestor;
/*     */ 
/*     */   public Node()
/*     */   {
/*  33 */     this.label = null;
/*     */   }
/*     */ 
/*     */   public Node(String label) {
/*  37 */     this.label = label;
/*     */   }
/*     */ 
/*     */   public Node(Node ancestor, double branchLength)
/*     */   {
/*  42 */     if (branchLength <= 0.0D) branchLength = 0.0001D;
/*  43 */     if (branchLength > 5.0D) branchLength = 5.0D;
/*  44 */     this.neighbors.put(Neighbor.A, ancestor);
/*  45 */     this.branchLengths.put(Neighbor.A, Double.valueOf(branchLength));
/*  46 */     this.label = null;
/*  47 */     this.ancestor = Neighbor.A;
/*     */   }
/*     */ 
/*     */   public String toString() {
/*  51 */     return this.label;
/*     */   }
/*     */ 
/*     */   public Neighbor addNeighbor(Node node) throws TooManyNeighborsException {
/*  55 */     if (!node.neighbors.containsKey(Neighbor.A))
/*  56 */       node.neighbors.put(Neighbor.A, this);
/*  57 */     else if (!node.neighbors.containsKey(Neighbor.B))
/*  58 */       node.neighbors.put(Neighbor.B, this);
/*  59 */     else if (!node.neighbors.containsKey(Neighbor.C))
/*  60 */       node.neighbors.put(Neighbor.C, this);
/*     */     else {
/*  62 */       throw new TooManyNeighborsException(node);
/*     */     }
/*  64 */     if (!this.neighbors.containsKey(Neighbor.A)) {
/*  65 */       this.neighbors.put(Neighbor.A, node);
/*  66 */       return Neighbor.A;
/*  67 */     }if (!this.neighbors.containsKey(Neighbor.B)) {
/*  68 */       this.neighbors.put(Neighbor.B, node);
/*  69 */       return Neighbor.B;
/*  70 */     }if (!this.neighbors.containsKey(Neighbor.C)) {
/*  71 */       this.neighbors.put(Neighbor.C, node);
/*  72 */       return Neighbor.C;
/*     */     }
/*  74 */     throw new TooManyNeighborsException(this);
/*     */   }
/*     */ 
/*     */   public Neighbor addNeighborWithBranchLength(Node node)
/*     */     throws TooManyNeighborsException
/*     */   {
/*  85 */     Neighbor keyInThis = addNeighbor(node);
/*  86 */     Neighbor keyInNeighbor = getNeighborKey(keyInThis);
/*  87 */     this.branchLengths.put(keyInThis, Double.valueOf(node.getBranchLength(keyInNeighbor)));
/*  88 */     return keyInThis;
/*     */   }
/*     */ 
/*     */   public void removeNeighborButKeepBranchLength(Node node)
/*     */   {
/*  96 */     for (Map.Entry e : this.neighbors.entrySet())
/*  97 */       if (e.getValue() == node) {
/*  98 */         Neighbor keyInNode = getNeighborKey((Neighbor)e.getKey());
/*  99 */         node.neighbors.remove(keyInNode);
/* 100 */         this.neighbors.remove(e.getKey());
/* 101 */         this.branchLengths.remove(e.getKey());
/*     */       }
/*     */   }
/*     */ 
/*     */   public void removeAllNeighbors()
/*     */   {
/* 107 */     this.neighbors.clear();
/* 108 */     this.branchLengths.clear();
/* 109 */     this.ancestor = null;
/*     */   }
/*     */ 
/*     */   public void setNeighbor(Neighbor neighbor, Node node) {
/* 113 */     this.neighbors.put(neighbor, node);
/*     */   }
/*     */ 
/*     */   public Neighbor replaceNeighbor(Node oldNeighbor, Node newNeighbor)
/*     */     throws UnknownNeighborException
/*     */   {
/* 125 */     for (Map.Entry e : this.neighbors.entrySet()) {
/* 126 */       if (e.getValue() == oldNeighbor) {
/* 127 */         this.neighbors.put((Neighbor)e.getKey(), newNeighbor);
/* 128 */         return (Neighbor)e.getKey();
/*     */       }
/*     */     }
/* 131 */     throw new UnknownNeighborException(this, oldNeighbor);
/*     */   }
/*     */ 
/*     */   public boolean hasNeighbor(Neighbor neighbor) {
/* 135 */     return this.neighbors.containsKey(neighbor);
/*     */   }
/*     */ 
/*     */   public Node getNeighbor(Neighbor neighbor) {
/* 139 */     return (Node)this.neighbors.get(neighbor);
/*     */   }
/*     */ 
/*     */   public Neighbor getNeighborKey(Neighbor neighbor)
/*     */   {
/* 148 */     for (Map.Entry e : ((Node)this.neighbors.get(neighbor)).neighbors.entrySet()) {
/* 149 */       if (e.getValue() == this) return (Neighbor)e.getKey();
/*     */     }
/* 151 */     return null;
/*     */   }
/*     */ 
/*     */   public Set<Node> getNeighborNodes() {
/* 155 */     return new HashSet(this.neighbors.values());
/*     */   }
/*     */ 
/*     */   public Set<Neighbor> getNeighborKeys() {
/* 159 */     return new HashSet(this.neighbors.keySet());
/*     */   }
/*     */ 
/*     */   public Set<Branch> getBranches() {
/* 163 */     Set branches = new HashSet();
/* 164 */     for (Neighbor n : this.neighbors.keySet()) {
/* 165 */       branches.add(new Branch(this, n));
/*     */     }
/* 167 */     return branches;
/*     */   }
/*     */ 
/*     */   public void setToRoot() {
/* 171 */     this.ancestor = null;
/*     */     Node n;
/* 172 */     for (Iterator localIterator = getNeighborNodes().iterator(); localIterator.hasNext(); n.setAncestor(this)) n = (Node)localIterator.next(); 
/*     */   }
/*     */ 
/*     */   public void setAncestor(Node node)
/*     */   {
/* 176 */     for (Map.Entry e : this.neighbors.entrySet())
/* 177 */       if (e.getValue() != node)
/* 178 */         ((Node)e.getValue()).setAncestor(this);
/*     */       else
/* 180 */         this.ancestor = ((Neighbor)e.getKey());
/*     */   }
/*     */ 
/*     */   public void removeAncestor()
/*     */   {
/* 185 */     for (Node n : this.neighbors.values())
/* 186 */       if (n != this.neighbors.get(this.ancestor)) n.removeAncestor();
/* 187 */     this.ancestor = null;
/*     */   }
/*     */ 
/*     */   public List<Node> getChildren() {
/* 191 */     if (this.ancestor == null) return new ArrayList(this.neighbors.values());
/* 192 */     List list = new ArrayList(this.neighbors.values());
/* 193 */     list.remove(this.neighbors.get(this.ancestor));
/* 194 */     return list;
/*     */   }
/*     */ 
/*     */   public Neighbor getAncestorKey() throws NullAncestorException {
/* 198 */     if (this.ancestor == null)
/* 199 */       throw new NullAncestorException(this);
/* 200 */     return this.ancestor;
/*     */   }
/*     */ 
/*     */   public Node getAncestorNode() throws NullAncestorException {
/* 204 */     if (this.ancestor == null) throw new NullAncestorException(this);
/* 205 */     return (Node)this.neighbors.get(this.ancestor);
/*     */   }
/*     */ 
/*     */   public double getAncestorBranchLength() throws NullAncestorException {
/* 209 */     if (this.ancestor == null) throw new NullAncestorException(this);
/* 210 */     if (this.branchLengths.containsKey(this.ancestor)) return ((Double)this.branchLengths.get(this.ancestor)).doubleValue();
/* 211 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public void setBranchLength(Neighbor neighbor, double branchLength) {
/* 215 */     if (branchLength < 0.0001D) branchLength = 0.0001D;
/* 216 */     if (branchLength > 5.0D) branchLength = 5.0D;
/* 217 */     this.branchLengths.put(neighbor, Double.valueOf(branchLength));
/* 218 */     ((Node)this.neighbors.get(neighbor)).branchLengths.put(getNeighborKey(neighbor), Double.valueOf(branchLength));
/*     */   }
/*     */ 
/*     */   public double getBranchLength(Neighbor neighbor) {
/* 222 */     if (this.branchLengths.containsKey(neighbor)) {
/* 223 */       return ((Double)this.branchLengths.get(neighbor)).doubleValue();
/*     */     }
/* 225 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public void setLabel(String label) {
/* 229 */     this.label = label;
/*     */   }
/*     */ 
/*     */   public String getLabel() {
/* 233 */     return this.label;
/*     */   }
/*     */ 
/*     */   public boolean isLeaf() {
/* 237 */     return this.neighbors.size() == 1;
/*     */   }
/*     */ 
/*     */   public boolean isInode() {
/* 241 */     return this.neighbors.size() > 1;
/*     */   }
/*     */ 
/*     */   public static enum Neighbor
/*     */   {
/*  25 */     A, B, C;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.Node
 * JD-Core Version:    0.6.2
 */