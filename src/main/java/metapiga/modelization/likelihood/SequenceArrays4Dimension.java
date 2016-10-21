/*     */ package metapiga.modelization.likelihood;
/*     */ 
/*     */ public class SequenceArrays4Dimension
/*     */   implements SequenceArrays
/*     */ {
/*     */   private final float[][][][] sequence;
/*     */   private final int numOfCats;
/*     */   private final int numOfNodes;
/*     */   private final int numCharComponents;
/*     */   private final int numOfStates;
/*     */ 
/*     */   public SequenceArrays4Dimension(int numberOfNodes, int numRateHeterogenityCats, int numCharComponents, int numOfStates)
/*     */   {
/*  12 */     this.sequence = new float[numRateHeterogenityCats][numberOfNodes][numOfStates][numCharComponents];
/*  13 */     this.numOfCats = numRateHeterogenityCats;
/*  14 */     this.numOfNodes = numberOfNodes;
/*  15 */     this.numCharComponents = numCharComponents;
/*  16 */     this.numOfStates = numOfStates;
/*     */   }
/*     */ 
/*     */   public float getElement(int node, int cat, int character, int state)
/*     */   {
/*  21 */     return this.sequence[cat][node][state][character];
/*     */   }
/*     */ 
/*     */   public void setElement(float value, int node, int cat, int character, int state) {
/*  25 */     this.sequence[cat][node][state][character] = value;
/*     */   }
/*     */ 
/*     */   public SequenceArrays clone()
/*     */   {
/*  30 */     SequenceArrays4Dimension copySequence = new SequenceArrays4Dimension(this.numOfNodes, this.numOfCats, this.numCharComponents, this.numOfStates);
/*  31 */     for (int i = 0; i < this.numOfCats; i++) {
/*  32 */       for (int j = 0; j < this.numOfNodes; j++) {
/*  33 */         for (int k = 0; k < this.numOfStates; k++) {
/*  34 */           System.arraycopy(this.sequence[i][j][k], 0, copySequence.sequence[i][j][k], 0, this.numCharComponents);
/*     */         }
/*     */       }
/*     */     }
/*  38 */     return copySequence;
/*     */   }
/*     */ 
/*     */   public void clone(SequenceArrays seq)
/*     */   {
/*  61 */     if ((seq.getCharacterCountNoPadding() != this.numCharComponents) || 
/*  62 */       (seq.getCategoryCount() != this.numOfCats) || 
/*  63 */       (seq.getNodeCount() != this.numOfNodes) || 
/*  64 */       (seq.getStateCount() != this.numOfStates)) {
/*  65 */       throw new IndexOutOfBoundsException("SequenceArrays sizes mismatch");
/*     */     }
/*     */ 
/*  68 */     if ((seq instanceof SequenceArrays4Dimension)) {
/*  69 */       SequenceArrays4Dimension s = (SequenceArrays4Dimension)seq;
/*  70 */       for (int cat = 0; cat < this.numOfCats; cat++) {
/*  71 */         for (int nodes = 0; nodes < this.numOfNodes; nodes++)
/*  72 */           for (int state = 0; state < this.numOfStates; state++)
/*  73 */             System.arraycopy(s.sequence[cat][nodes][state], 0, this.sequence[cat][nodes][state], 0, this.numCharComponents);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  78 */       for (int cat = 0; cat < this.numOfCats; cat++)
/*  79 */         for (int node = 0; node < this.numOfNodes; node++)
/*  80 */           for (int state = 0; state < this.numOfStates; state++)
/*  81 */             for (int character = 0; character < this.numCharComponents; character++)
/*  82 */               this.sequence[cat][node][state][character] = seq.getElement(node, cat, character, state);
/*     */     }
/*     */   }
/*     */ 
/*     */   public final float[][] getSequenceAtCategoryAndNode(int cat, int node)
/*     */   {
/*  92 */     return this.sequence[cat][node];
/*     */   }
/*     */ 
/*     */   public final void setSequenceAtNodeInCategory(float[][] seq, int cat, int node) {
/*  96 */     this.sequence[cat][node] = seq;
/*     */   }
/*     */ 
/*     */   public int getCategoryCount()
/*     */   {
/* 101 */     return this.numOfCats;
/*     */   }
/*     */ 
/*     */   public int getCharacterCountNoPadding()
/*     */   {
/* 106 */     return this.numCharComponents;
/*     */   }
/*     */ 
/*     */   public int getNodeCount()
/*     */   {
/* 111 */     return this.numOfNodes;
/*     */   }
/*     */ 
/*     */   public int getStateCount()
/*     */   {
/* 116 */     return this.numOfStates;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SequenceArrays4Dimension
 * JD-Core Version:    0.6.2
 */