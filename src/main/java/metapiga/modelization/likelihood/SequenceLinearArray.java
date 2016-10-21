/*     */ package metapiga.modelization.likelihood;
/*     */ 
/*     */ public class SequenceLinearArray
/*     */   implements SequenceArrays
/*     */ {
/*     */   private final float[][] sequence;
/*     */   private final int numOfCats;
/*     */   private final int numOfNodes;
/*     */   private final int numCharComponents;
/*     */   private final int numCharComponentsWithPadding;
/*     */   private final int numOfStates;
/*     */   private final int GPUwarpSize;
/*     */ 
/*     */   public SequenceLinearArray(int numberOfNodes, int numRateHeterogenityCats, int numCharComponents, int numOfStates, int warpSize)
/*     */   {
/*  18 */     this.GPUwarpSize = warpSize;
/*  19 */     this.numOfCats = numRateHeterogenityCats;
/*  20 */     this.numOfNodes = numberOfNodes;
/*  21 */     this.numCharComponents = numCharComponents;
/*     */ 
/*  25 */     this.numCharComponentsWithPadding = ((int)(Math.ceil(numCharComponents / this.GPUwarpSize) * this.GPUwarpSize));
/*  26 */     this.numOfStates = numOfStates;
/*  27 */     this.sequence = new float[numberOfNodes][numOfStates * this.numCharComponentsWithPadding * numRateHeterogenityCats];
/*     */   }
/*     */ 
/*     */   public float getElement(int node, int cat, int character, int state)
/*     */   {
/*  33 */     int seqIndex = cat * this.numOfStates * this.numCharComponentsWithPadding + state * this.numCharComponentsWithPadding + character;
/*  34 */     return this.sequence[node][seqIndex];
/*     */   }
/*     */ 
/*     */   public void setElement(float value, int node, int cat, int character, int state)
/*     */   {
/*  40 */     this.sequence[node][(cat * this.numOfStates * this.numCharComponentsWithPadding + state * this.numCharComponentsWithPadding + character)] = value;
/*     */   }
/*     */ 
/*     */   public SequenceArrays clone()
/*     */   {
/*  46 */     SequenceLinearArray seq = new SequenceLinearArray(this.numOfNodes, this.numOfCats, this.numCharComponents, this.numOfStates, this.GPUwarpSize);
/*  47 */     for (int node = 0; node < this.numOfNodes; node++) {
/*  48 */       System.arraycopy(this.sequence[node], 0, seq.sequence[node], 0, this.numCharComponentsWithPadding * this.numOfCats * this.numOfStates);
/*     */     }
/*  50 */     return seq;
/*     */   }
/*     */ 
/*     */   public void clone(SequenceArrays seq)
/*     */   {
/*  56 */     if ((seq.getCharacterCountNoPadding() != this.numCharComponents) || 
/*  57 */       (seq.getCategoryCount() != this.numOfCats) || 
/*  58 */       (seq.getNodeCount() != this.numOfNodes) || 
/*  59 */       (seq.getStateCount() != this.numOfStates)) {
/*  60 */       throw new IndexOutOfBoundsException("SequenceArrays sizes mismatch");
/*     */     }
/*     */ 
/*  63 */     if ((seq instanceof SequenceLinearArray)) {
/*  64 */       SequenceLinearArray s = (SequenceLinearArray)seq;
/*  65 */       for (int node = 0; node < this.numOfNodes; node++)
/*  66 */         System.arraycopy(s.sequence[node], 0, this.sequence[node], 0, this.numCharComponentsWithPadding * this.numOfCats * this.numOfStates);
/*     */     }
/*     */     else {
/*  69 */       for (int cat = 0; cat < this.numOfCats; cat++)
/*  70 */         for (int node = 0; node < this.numOfNodes; node++)
/*  71 */           for (int state = 0; state < this.numOfStates; state++)
/*  72 */             for (int character = 0; character < this.numCharComponents; character++)
/*  73 */               setElement(seq.getElement(node, cat, character, state), node, cat, character, state);
/*     */     }
/*     */   }
/*     */ 
/*     */   public float[] getSequenceAtNode(int nodeIdx)
/*     */   {
/*  84 */     return this.sequence[nodeIdx];
/*     */   }
/*     */ 
/*     */   public void setSequenceAtNode(float[] s, int nodeIndex) {
/*  88 */     int a = this.sequence[nodeIndex].length;
/*  89 */     int b = s.length;
/*  90 */     if (s.length != this.sequence[nodeIndex].length) {
/*  91 */       throw new IndexOutOfBoundsException("SequenceArrays sizes mismatch");
/*     */     }
/*  93 */     this.sequence[nodeIndex] = s;
/*     */   }
/*     */ 
/*     */   public float[] getSubsequenceAtNode(int nodeIdx, int charSequenceOffset, int charSubsetSize) {
/*  97 */     float[] subsequence = new float[charSubsetSize * this.numOfCats * this.numOfStates];
/*  98 */     float[] wholeSequence = getSequenceAtNode(nodeIdx);
/*  99 */     for (int cat = 0; cat < this.numOfCats; cat++) {
/* 100 */       for (int state = 0; state < this.numOfStates; state++) {
/* 101 */         int seqOffset = cat * this.numOfStates * this.numCharComponentsWithPadding + state * this.numCharComponentsWithPadding + charSequenceOffset;
/* 102 */         int subsetOffset = cat * this.numOfStates * charSubsetSize + state * charSubsetSize;
/* 103 */         System.arraycopy(wholeSequence, seqOffset, subsequence, subsetOffset, charSubsetSize);
/*     */       }
/*     */     }
/* 106 */     return subsequence;
/*     */   }
/*     */ 
/*     */   public void setSubsequenceAtNode(int nodeIdx, int charSequenceOffset, int charSubsetSize, float[] subsequence) {
/* 110 */     if ((charSubsetSize == this.numCharComponentsWithPadding) && (charSequenceOffset == 0)) {
/* 111 */       setSequenceAtNode(subsequence, nodeIdx);
/*     */     }
/*     */ 
/* 114 */     float[] wholeSequence = getSequenceAtNode(nodeIdx);
/* 115 */     for (int cat = 0; cat < this.numOfCats; cat++)
/* 116 */       for (int state = 0; state < this.numOfStates; state++) {
/* 117 */         int seqOffset = cat * this.numOfStates * this.numCharComponentsWithPadding + state * this.numCharComponentsWithPadding + charSequenceOffset;
/* 118 */         int subsetOffset = cat * this.numOfStates * charSubsetSize + state * charSubsetSize;
/* 119 */         System.arraycopy(subsequence, subsetOffset, wholeSequence, seqOffset, charSubsetSize);
/*     */       }
/*     */   }
/*     */ 
/*     */   public int getCategoryCount()
/*     */   {
/* 126 */     return this.numOfCats;
/*     */   }
/*     */ 
/*     */   public int getCharacterCountNoPadding()
/*     */   {
/* 131 */     return this.numCharComponents;
/*     */   }
/*     */ 
/*     */   public int getCharacterCountWithPadding() {
/* 135 */     return this.numCharComponentsWithPadding;
/*     */   }
/*     */ 
/*     */   public int getNodeCount()
/*     */   {
/* 140 */     return this.numOfNodes;
/*     */   }
/*     */ 
/*     */   public int getStateCount()
/*     */   {
/* 145 */     return this.numOfStates;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SequenceLinearArray
 * JD-Core Version:    0.6.2
 */