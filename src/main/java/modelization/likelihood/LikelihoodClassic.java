/*     */ package modelization.likelihood;
/*     */ 
/*     */ import Jama.Matrix;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import metapiga.RateParameter;
/*     */ import metapiga.modelization.Dataset.Partition;
/*     */ import metapiga.modelization.data.DNA;
/*     */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*     */ import metapiga.parameters.Parameters.EvaluationModel;
/*     */ import metapiga.parameters.Parameters.EvaluationRate;
/*     */ import metapiga.parameters.Parameters.EvaluationStateFrequencies;
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class LikelihoodClassic extends Likelihood
/*     */ {
/*     */   private final SequenceArrays4Dimension localSequence;
/*     */ 
/*     */   protected LikelihoodClassic(Dataset.Partition partition, Parameters.EvaluationRate rate, Parameters.EvaluationModel model, Parameters.EvaluationDistribution distribution, double distributionShape, double pinv, double apRate, Map<RateParameter, Double> rateParameters, Parameters.EvaluationStateFrequencies stateFreq, Tree tree, int numSubsets, SequenceArrays4Dimension seq)
/*     */     throws UnrootableTreeException
/*     */   {
/*  29 */     super(partition, rate, model, distribution, distributionShape, pinv, apRate, rateParameters, stateFreq, tree, numSubsets, seq);
/*     */ 
/*  31 */     this.localSequence = seq;
/*     */   }
/*     */ 
/*     */   protected LikelihoodClassic(LikelihoodClassic L, Tree tree) throws UnrootableTreeException {
/*  35 */     super(L, tree);
/*  36 */     this.localSequence = ((SequenceArrays4Dimension)this.sequences);
/*     */   }
/*     */ 
/*     */   protected void update(Node node) throws NullAncestorException
/*     */   {
/*  41 */     for (Node child : node.getChildren()) {
/*  42 */       if (this.toUpdate.contains(child)) {
/*  43 */         update(child);
/*  44 */         this.toUpdate.remove(child);
/*     */       }
/*     */     }
/*  47 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.model.ordinal()]) {
/*     */     case 20:
/*  49 */       computeJC(node);
/*  50 */       break;
/*     */     case 19:
/*  52 */       computeK2P(node);
/*  53 */       break;
/*     */     case 18:
/*  55 */       computeHKY85(node);
/*  56 */       break;
/*     */     case 17:
/*  58 */       computeTN93(node);
/*  59 */       break;
/*     */     case 15:
/*  61 */       computePoisson(node);
/*  62 */       break;
/*     */     case 1:
/*     */     case 2:
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/*     */     case 6:
/*     */     case 7:
/*     */     case 8:
/*     */     case 9:
/*     */     case 10:
/*     */     case 11:
/*     */     case 12:
/*     */     case 13:
/*     */     case 14:
/*     */     case 16:
/*  78 */       computeGTR(node);
/*     */     }
/*     */ 
/*  81 */     if (this.tree.getRoot() == node)
/*  82 */       calculateLikelihoodAtRoot();
/*     */   }
/*     */ 
/*     */   protected void computeJC(Node node)
/*     */     throws NullAncestorException
/*     */   {
/*  93 */     List children = node.getChildren();
/*  94 */     Node node1 = (Node)children.get(0);
/*  95 */     Node node2 = (Node)children.get(1);
/*     */ 
/*  97 */     for (int count = 1; count < children.size(); count++)
/*     */     {
/*     */       float[][][] tempRootSeq;
/*     */       double bl1;
/*     */       double bl2;
/*     */       float[][][] tempRootSeq;
/* 100 */       if (count > 1) {
/* 101 */         node1 = (Node)children.get(count);
/* 102 */         node2 = node;
/* 103 */         double bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 104 */         double bl2 = 0.0D;
/* 105 */         tempRootSeq = new float[this.numCat][this.numStates][this.numCharComp];
/*     */       } else {
/* 107 */         bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 108 */         bl2 = node2.getAncestorBranchLength() / (1.0D - this.pInv);
/* 109 */         tempRootSeq = null;
/*     */       }
/* 111 */       Node key = count > 1 ? null : node;
/* 112 */       if (this.underflowScaling.containsKey(key)) {
/* 113 */         this.underflowScaling.remove(key);
/* 114 */         this.underflow = (!this.underflowScaling.isEmpty());
/*     */       }
/* 116 */       int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 117 */       int n1 = ((Integer)this.nodeIndex.get(node1)).intValue();
/* 118 */       int n2 = ((Integer)this.nodeIndex.get(node2)).intValue();
/* 119 */       double[][] ufScaling = new double[this.numCat][this.numCharComp];
/* 120 */       boolean underflowEncountered = false;
/* 121 */       boolean rescalingNeeded = false;
/*     */       do {
/* 123 */         for (int cat = 0; cat < this.numCat; cat++) {
/* 124 */           double exp1 = Math.exp(-bl1 * this.rateScaling * this.rates[cat] * this.apRate);
/* 125 */           double exp2 = Math.exp(-bl2 * this.rateScaling * this.rates[cat] * this.apRate);
/* 126 */           double diag1 = 0.25D + 0.75D * exp1;
/* 127 */           double offdiag1 = 0.25D - 0.25D * exp1;
/* 128 */           double diag2 = 0.25D + 0.75D * exp2;
/* 129 */           double offdiag2 = 0.25D - 0.25D * exp2;
/*     */ 
/* 131 */           float[][] seq1 = this.localSequence.getSequenceAtCategoryAndNode(cat, n1);
/* 132 */           float[][] seq2 = this.localSequence.getSequenceAtCategoryAndNode(cat, n2);
/* 133 */           float[][] seq = count > 1 ? tempRootSeq[cat] : this.localSequence.getSequenceAtCategoryAndNode(cat, n);
/* 134 */           double[] scalingFactor = ufScaling[cat];
/* 135 */           for (int site = 0; site < this.numCharComp; site++) {
/* 136 */             double lMax = 0.0D;
/* 137 */             for (int state = 0; state < this.numStates; state++) {
/* 138 */               double sum1 = 0.0D; double sum2 = 0.0D;
/* 139 */               for (int s = 0; s < this.numStates; s++) {
/* 140 */                 sum1 += seq1[s][site] * (state == s ? diag1 : offdiag1);
/* 141 */                 sum2 += seq2[s][site] * (state == s ? diag2 : offdiag2);
/*     */               }
/* 143 */               double prod = sum1 * sum2;
/* 144 */               if (rescalingNeeded) {
/* 145 */                 seq[state][site] = ((float)(prod / scalingFactor[site]));
/*     */               } else {
/* 147 */                 seq[state][site] = ((float)prod);
/* 148 */                 if (prod > lMax) lMax = prod;
/* 149 */                 if (prod < 1.401298464324817E-045D) underflowEncountered = true;
/*     */               }
/*     */             }
/* 152 */             if (!rescalingNeeded) scalingFactor[site] = lMax;
/* 153 */             assert (!Double.isInfinite(lMax)) : site;
/* 154 */             assert (!Double.isNaN(lMax)) : site;
/*     */           }
/*     */         }
/* 157 */         if (underflowEncountered)
/*     */         {
/* 159 */           this.underflowScaling.put(key, ufScaling);
/* 160 */           this.underflow = true;
/* 161 */           rescalingNeeded = true;
/* 162 */           underflowEncountered = false;
/*     */         } else {
/* 164 */           rescalingNeeded = false;
/*     */         }
/*     */       }
/* 122 */       while (
/* 166 */         rescalingNeeded);
/* 167 */       if (count > 1)
/* 168 */         for (int cat = 0; cat < this.numCat; cat++)
/* 169 */           this.localSequence.setSequenceAtNodeInCategory(tempRootSeq[cat], cat, n);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeK2P(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 183 */     double kappa = ((Double)this.rateParameters.get(RateParameter.K)).doubleValue();
/* 184 */     int A = DNA.A.state;
/* 185 */     int C = DNA.C.state;
/* 186 */     int G = DNA.G.state;
/* 187 */     int T = DNA.T.state;
/* 188 */     List children = node.getChildren();
/* 189 */     Node node1 = (Node)children.get(0);
/* 190 */     Node node2 = (Node)children.get(1);
/* 191 */     for (int count = 1; count < children.size(); count++)
/*     */     {
/*     */       float[][][] tempRootSeq;
/*     */       double bl1;
/*     */       double bl2;
/*     */       float[][][] tempRootSeq;
/* 194 */       if (count > 1) {
/* 195 */         node1 = (Node)children.get(count);
/* 196 */         node2 = node;
/* 197 */         double bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 198 */         double bl2 = 0.0D;
/* 199 */         tempRootSeq = new float[this.numCat][this.numStates][this.numCharComp];
/*     */       } else {
/* 201 */         bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 202 */         bl2 = node2.getAncestorBranchLength() / (1.0D - this.pInv);
/* 203 */         tempRootSeq = null;
/*     */       }
/* 205 */       Node key = count > 1 ? null : node;
/* 206 */       if (this.underflowScaling.containsKey(key)) {
/* 207 */         this.underflowScaling.remove(key);
/* 208 */         this.underflow = (!this.underflowScaling.isEmpty());
/*     */       }
/* 210 */       int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 211 */       int n1 = ((Integer)this.nodeIndex.get(node1)).intValue();
/* 212 */       int n2 = ((Integer)this.nodeIndex.get(node2)).intValue();
/* 213 */       double[][] ufScaling = new double[this.numCat][this.numCharComp];
/* 214 */       boolean underflowEncountered = false;
/* 215 */       boolean rescalingNeeded = false;
/*     */       do {
/* 217 */         for (int cat = 0; cat < this.numCat; cat++) {
/* 218 */           double exp1a = Math.exp(-bl1 * this.rateScaling * this.rates[cat] * this.apRate);
/* 219 */           double exp2a = Math.exp(-bl2 * this.rateScaling * this.rates[cat] * this.apRate);
/* 220 */           double exp1b = Math.exp(-bl1 * this.rateScaling * this.rates[cat] * this.apRate * ((kappa + 1.0D) / 2.0D));
/* 221 */           double exp2b = Math.exp(-bl2 * this.rateScaling * this.rates[cat] * this.apRate * ((kappa + 1.0D) / 2.0D));
/* 222 */           double diag1 = 0.25D + 0.25D * exp1a + 0.5D * exp1b;
/* 223 */           double diag2 = 0.25D + 0.25D * exp2a + 0.5D * exp2b;
/* 224 */           double ti1 = 0.25D + 0.25D * exp1a - 0.5D * exp1b;
/* 225 */           double ti2 = 0.25D + 0.25D * exp2a - 0.5D * exp2b;
/* 226 */           double tv1 = 0.25D - 0.25D * exp1a;
/* 227 */           double tv2 = 0.25D - 0.25D * exp2a;
/*     */ 
/* 229 */           float[][] seq1 = this.localSequence.getSequenceAtCategoryAndNode(cat, n1);
/* 230 */           float[][] seq2 = this.localSequence.getSequenceAtCategoryAndNode(cat, n2);
/* 231 */           float[][] seq = count > 1 ? tempRootSeq[cat] : this.localSequence.getSequenceAtCategoryAndNode(cat, n);
/* 232 */           double[] scalingFactor = ufScaling[cat];
/* 233 */           for (int site = 0; site < this.numCharComp; site++) {
/* 234 */             double lMax = 0.0D;
/* 235 */             for (int base = 0; base < this.numStates; base++) {
/* 236 */               double sum1 = 0.0D; double sum2 = 0.0D;
/* 237 */               if (base == A) {
/* 238 */                 sum1 += seq1[A][site] * diag1;
/* 239 */                 sum2 += seq2[A][site] * diag2;
/* 240 */                 sum1 += seq1[C][site] * tv1;
/* 241 */                 sum2 += seq2[C][site] * tv2;
/* 242 */                 sum1 += seq1[G][site] * ti1;
/* 243 */                 sum2 += seq2[G][site] * ti2;
/* 244 */                 sum1 += seq1[T][site] * tv1;
/* 245 */                 sum2 += seq2[T][site] * tv2;
/* 246 */               } else if (base == C) {
/* 247 */                 sum1 += seq1[A][site] * tv1;
/* 248 */                 sum2 += seq2[A][site] * tv2;
/* 249 */                 sum1 += seq1[C][site] * diag1;
/* 250 */                 sum2 += seq2[C][site] * diag2;
/* 251 */                 sum1 += seq1[G][site] * tv1;
/* 252 */                 sum2 += seq2[G][site] * tv2;
/* 253 */                 sum1 += seq1[T][site] * ti1;
/* 254 */                 sum2 += seq2[T][site] * ti2;
/* 255 */               } else if (base == G) {
/* 256 */                 sum1 += seq1[A][site] * ti1;
/* 257 */                 sum2 += seq2[A][site] * ti2;
/* 258 */                 sum1 += seq1[C][site] * tv1;
/* 259 */                 sum2 += seq2[C][site] * tv2;
/* 260 */                 sum1 += seq1[G][site] * diag1;
/* 261 */                 sum2 += seq2[G][site] * diag2;
/* 262 */                 sum1 += seq1[T][site] * tv1;
/* 263 */                 sum2 += seq2[T][site] * tv2;
/* 264 */               } else if (base == T) {
/* 265 */                 sum1 += seq1[A][site] * tv1;
/* 266 */                 sum2 += seq2[A][site] * tv2;
/* 267 */                 sum1 += seq1[C][site] * ti1;
/* 268 */                 sum2 += seq2[C][site] * ti2;
/* 269 */                 sum1 += seq1[G][site] * tv1;
/* 270 */                 sum2 += seq2[G][site] * tv2;
/* 271 */                 sum1 += seq1[T][site] * diag1;
/* 272 */                 sum2 += seq2[T][site] * diag2;
/*     */               }
/* 274 */               double prod = sum1 * sum2;
/* 275 */               if (rescalingNeeded) {
/* 276 */                 seq[base][site] = ((float)(prod / scalingFactor[site]));
/*     */               } else {
/* 278 */                 seq[base][site] = ((float)prod);
/* 279 */                 if (prod > lMax) lMax = prod;
/* 280 */                 if (prod < 1.401298464324817E-045D) underflowEncountered = true;
/*     */               }
/*     */             }
/* 283 */             if (!rescalingNeeded) scalingFactor[site] = lMax;
/* 284 */             assert (!Double.isInfinite(lMax)) : site;
/* 285 */             assert (!Double.isNaN(lMax)) : site;
/*     */           }
/*     */         }
/* 288 */         if (underflowEncountered)
/*     */         {
/* 290 */           this.underflowScaling.put(key, ufScaling);
/* 291 */           this.underflow = true;
/* 292 */           rescalingNeeded = true;
/* 293 */           underflowEncountered = false;
/*     */         } else {
/* 295 */           rescalingNeeded = false;
/*     */         }
/*     */       }
/* 216 */       while (
/* 297 */         rescalingNeeded);
/* 298 */       if (count > 1)
/* 299 */         for (int cat = 0; cat < this.numCat; cat++)
/* 300 */           this.localSequence.setSequenceAtNodeInCategory(tempRootSeq[cat], cat, n);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeHKY85(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 315 */     double kappa = ((Double)this.rateParameters.get(RateParameter.K)).doubleValue();
/* 316 */     int A = DNA.A.state;
/* 317 */     int C = DNA.C.state;
/* 318 */     int G = DNA.G.state;
/* 319 */     int T = DNA.T.state;
/* 320 */     List children = node.getChildren();
/* 321 */     Node node1 = (Node)children.get(0);
/* 322 */     Node node2 = (Node)children.get(1);
/* 323 */     for (int count = 1; count < children.size(); count++) {
/* 324 */       double[] bl = new double[2];
/* 325 */       double[] PIj = new double[this.numStates];
/* 326 */       double[][] expA = new double[2][this.numStates];
/* 327 */       double[][] expB = new double[2][this.numStates];
/* 328 */       double[][] diag = new double[2][this.numStates];
/* 329 */       double[][] ti = new double[2][this.numStates];
/* 330 */       double[][] tv = new double[2][this.numStates];
/*     */       float[][][] tempRootSeq;
/*     */       float[][][] tempRootSeq;
/* 333 */       if (count > 1) {
/* 334 */         node1 = (Node)children.get(count);
/* 335 */         node2 = node;
/* 336 */         bl[0] = (node1.getAncestorBranchLength() / (1.0D - this.pInv));
/* 337 */         bl[1] = 0.0D;
/* 338 */         tempRootSeq = new float[this.numCat][this.numStates][this.numCharComp];
/*     */       } else {
/* 340 */         bl[0] = (node1.getAncestorBranchLength() / (1.0D - this.pInv));
/* 341 */         bl[1] = (node2.getAncestorBranchLength() / (1.0D - this.pInv));
/* 342 */         tempRootSeq = null;
/*     */       }
/* 344 */       Node key = count > 1 ? null : node;
/* 345 */       if (this.underflowScaling.containsKey(key)) {
/* 346 */         this.underflowScaling.remove(key);
/* 347 */         this.underflow = (!this.underflowScaling.isEmpty());
/*     */       }
/*     */       double tmp340_339 = (this.equiFreq[A] + this.equiFreq[G]); PIj[G] = tmp340_339; PIj[A] = tmp340_339;
/*     */       double tmp366_365 = (this.equiFreq[C] + this.equiFreq[T]); PIj[T] = tmp366_365; PIj[C] = tmp366_365;
/* 352 */       int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 353 */       int n1 = ((Integer)this.nodeIndex.get(node1)).intValue();
/* 354 */       int n2 = ((Integer)this.nodeIndex.get(node2)).intValue();
/* 355 */       double[][] ufScaling = new double[this.numCat][this.numCharComp];
/* 356 */       boolean underflowEncountered = false;
/* 357 */       boolean rescalingNeeded = false;
/*     */       do {
/* 359 */         for (int cat = 0; cat < this.numCat; cat++)
/*     */         {
/* 361 */           for (int seq = 0; seq < 2; seq++) {
/* 362 */             for (int base = 0; base < this.numStates; base++) {
/* 363 */               expA[seq][base] = Math.exp(-bl[seq] * this.rateScaling * this.rates[cat] * this.apRate);
/* 364 */               expB[seq][base] = Math.exp(-bl[seq] * this.rateScaling * this.rates[cat] * this.apRate * (1.0D + PIj[base] * (kappa - 1.0D)));
/*     */             }
/*     */           }
/*     */ 
/* 368 */           for (int seq = 0; seq < 2; seq++) {
/* 369 */             for (int base = 0; base < this.numStates; base++) {
/* 370 */               diag[seq][base] = (this.equiFreq[base] + this.equiFreq[base] * (1.0D / PIj[base] - 1.0D) * expA[seq][base] + (PIj[base] - this.equiFreq[base]) / PIj[base] * expB[seq][base]);
/* 371 */               ti[seq][base] = (this.equiFreq[base] + this.equiFreq[base] * (1.0D / PIj[base] - 1.0D) * expA[seq][base] - this.equiFreq[base] / PIj[base] * expB[seq][base]);
/* 372 */               tv[seq][base] = (this.equiFreq[base] * (1.0D - expA[seq][base]));
/*     */             }
/*     */           }
/*     */ 
/* 376 */           float[][] seq1 = this.localSequence.getSequenceAtCategoryAndNode(cat, n1);
/* 377 */           float[][] seq2 = this.localSequence.getSequenceAtCategoryAndNode(cat, n2);
/* 378 */           float[][] seq = count > 1 ? tempRootSeq[cat] : this.localSequence.getSequenceAtCategoryAndNode(cat, n);
/* 379 */           double[] scalingFactor = ufScaling[cat];
/* 380 */           for (int site = 0; site < this.numCharComp; site++) {
/* 381 */             double lMax = 0.0D;
/* 382 */             for (int base = 0; base < this.numStates; base++) {
/* 383 */               double sum1 = 0.0D; double sum2 = 0.0D;
/* 384 */               if (base == A) {
/* 385 */                 sum1 += seq1[A][site] * diag[0][A];
/* 386 */                 sum2 += seq2[A][site] * diag[1][A];
/* 387 */                 sum1 += seq1[C][site] * tv[0][C];
/* 388 */                 sum2 += seq2[C][site] * tv[1][C];
/* 389 */                 sum1 += seq1[G][site] * ti[0][G];
/* 390 */                 sum2 += seq2[G][site] * ti[1][G];
/* 391 */                 sum1 += seq1[T][site] * tv[0][T];
/* 392 */                 sum2 += seq2[T][site] * tv[1][T];
/* 393 */               } else if (base == C) {
/* 394 */                 sum1 += seq1[A][site] * tv[0][A];
/* 395 */                 sum2 += seq2[A][site] * tv[1][A];
/* 396 */                 sum1 += seq1[C][site] * diag[0][C];
/* 397 */                 sum2 += seq2[C][site] * diag[1][C];
/* 398 */                 sum1 += seq1[G][site] * tv[0][G];
/* 399 */                 sum2 += seq2[G][site] * tv[1][G];
/* 400 */                 sum1 += seq1[T][site] * ti[0][T];
/* 401 */                 sum2 += seq2[T][site] * ti[1][T];
/* 402 */               } else if (base == G) {
/* 403 */                 sum1 += seq1[A][site] * ti[0][A];
/* 404 */                 sum2 += seq2[A][site] * ti[1][A];
/* 405 */                 sum1 += seq1[C][site] * tv[0][C];
/* 406 */                 sum2 += seq2[C][site] * tv[1][C];
/* 407 */                 sum1 += seq1[G][site] * diag[0][G];
/* 408 */                 sum2 += seq2[G][site] * diag[1][G];
/* 409 */                 sum1 += seq1[T][site] * tv[0][T];
/* 410 */                 sum2 += seq2[T][site] * tv[1][T];
/* 411 */               } else if (base == T) {
/* 412 */                 sum1 += seq1[A][site] * tv[0][A];
/* 413 */                 sum2 += seq2[A][site] * tv[1][A];
/* 414 */                 sum1 += seq1[C][site] * ti[0][C];
/* 415 */                 sum2 += seq2[C][site] * ti[1][C];
/* 416 */                 sum1 += seq1[G][site] * tv[0][G];
/* 417 */                 sum2 += seq2[G][site] * tv[1][G];
/* 418 */                 sum1 += seq1[T][site] * diag[0][T];
/* 419 */                 sum2 += seq2[T][site] * diag[1][T];
/*     */               }
/* 421 */               double prod = sum1 * sum2;
/* 422 */               if (rescalingNeeded) {
/* 423 */                 seq[base][site] = ((float)(prod / scalingFactor[site]));
/*     */               } else {
/* 425 */                 seq[base][site] = ((float)prod);
/* 426 */                 if (prod > lMax) lMax = prod;
/* 427 */                 if (prod < 1.401298464324817E-045D) underflowEncountered = true;
/*     */               }
/*     */             }
/* 430 */             if (!rescalingNeeded) scalingFactor[site] = lMax;
/* 431 */             assert (!Double.isInfinite(lMax)) : site;
/* 432 */             assert (!Double.isNaN(lMax)) : site;
/*     */           }
/*     */         }
/* 435 */         if (underflowEncountered)
/*     */         {
/* 437 */           this.underflowScaling.put(key, ufScaling);
/* 438 */           this.underflow = true;
/* 439 */           rescalingNeeded = true;
/* 440 */           underflowEncountered = false;
/*     */         } else {
/* 442 */           rescalingNeeded = false;
/*     */         }
/*     */       }
/* 358 */       while (
/* 444 */         rescalingNeeded);
/* 445 */       if (count > 1)
/* 446 */         for (int cat = 0; cat < this.numCat; cat++)
/* 447 */           this.localSequence.setSequenceAtNodeInCategory(tempRootSeq[cat], cat, n);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeTN93(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 461 */     computeGTR(node);
/*     */   }
/*     */ 
/*     */   protected void computePoisson(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 472 */     List children = node.getChildren();
/* 473 */     Node node1 = (Node)children.get(0);
/* 474 */     Node node2 = (Node)children.get(1);
/* 475 */     for (int count = 1; count < children.size(); count++)
/*     */     {
/*     */       float[][][] tempRootSeq;
/*     */       double bl1;
/*     */       double bl2;
/*     */       float[][][] tempRootSeq;
/* 478 */       if (count > 1) {
/* 479 */         node1 = (Node)children.get(count);
/* 480 */         node2 = node;
/* 481 */         double bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 482 */         double bl2 = 0.0D;
/* 483 */         tempRootSeq = new float[this.numCat][this.numStates][this.numCharComp];
/*     */       } else {
/* 485 */         bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 486 */         bl2 = node2.getAncestorBranchLength() / (1.0D - this.pInv);
/* 487 */         tempRootSeq = null;
/*     */       }
/* 489 */       Node key = count > 1 ? null : node;
/* 490 */       if (this.underflowScaling.containsKey(key)) {
/* 491 */         this.underflowScaling.remove(key);
/* 492 */         this.underflow = (!this.underflowScaling.isEmpty());
/*     */       }
/* 494 */       int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 495 */       int n1 = ((Integer)this.nodeIndex.get(node1)).intValue();
/* 496 */       int n2 = ((Integer)this.nodeIndex.get(node2)).intValue();
/* 497 */       double[][] ufScaling = new double[this.numCat][this.numCharComp];
/* 498 */       boolean underflowEncountered = false;
/* 499 */       boolean rescalingNeeded = false;
/*     */       do {
/* 501 */         for (int cat = 0; cat < this.numCat; cat++) {
/* 502 */           double exp1 = Math.exp(-bl1 * this.rateScaling * this.rates[cat] * this.apRate);
/* 503 */           double exp2 = Math.exp(-bl2 * this.rateScaling * this.rates[cat] * this.apRate);
/* 504 */           double diag1 = 0.05D + 0.95D * exp1;
/* 505 */           double offdiag1 = 0.05D - 0.05D * exp1;
/* 506 */           double diag2 = 0.05D + 0.95D * exp2;
/* 507 */           double offdiag2 = 0.05D - 0.05D * exp2;
/*     */ 
/* 509 */           float[][] seq1 = this.localSequence.getSequenceAtCategoryAndNode(cat, n1);
/* 510 */           float[][] seq2 = this.localSequence.getSequenceAtCategoryAndNode(cat, n2);
/* 511 */           float[][] seq = count > 1 ? tempRootSeq[cat] : this.localSequence.getSequenceAtCategoryAndNode(cat, n);
/* 512 */           double[] scalingFactor = ufScaling[cat];
/* 513 */           for (int site = 0; site < this.numCharComp; site++) {
/* 514 */             double lMax = 0.0D;
/* 515 */             for (int state = 0; state < this.numStates; state++) {
/* 516 */               double sum1 = 0.0D; double sum2 = 0.0D;
/* 517 */               for (int s = 0; s < this.numStates; s++) {
/* 518 */                 sum1 += seq1[s][site] * (state == s ? diag1 : offdiag1);
/* 519 */                 sum2 += seq2[s][site] * (state == s ? diag2 : offdiag2);
/*     */               }
/* 521 */               double prod = sum1 * sum2;
/* 522 */               if (rescalingNeeded) {
/* 523 */                 seq[state][site] = ((float)(prod / scalingFactor[site]));
/*     */               } else {
/* 525 */                 seq[state][site] = ((float)prod);
/* 526 */                 if (prod > lMax) lMax = prod;
/* 527 */                 if (prod < 1.401298464324817E-045D) underflowEncountered = true;
/*     */               }
/*     */             }
/* 530 */             if (!rescalingNeeded) scalingFactor[site] = lMax;
/* 531 */             assert (!Double.isInfinite(lMax)) : site;
/* 532 */             assert (!Double.isNaN(lMax)) : site;
/*     */           }
/*     */         }
/* 535 */         if (underflowEncountered)
/*     */         {
/* 537 */           this.underflowScaling.put(key, ufScaling);
/* 538 */           this.underflow = true;
/* 539 */           rescalingNeeded = true;
/* 540 */           underflowEncountered = false;
/*     */         } else {
/* 542 */           rescalingNeeded = false;
/*     */         }
/*     */       }
/* 500 */       while (
/* 544 */         rescalingNeeded);
/* 545 */       if (count > 1)
/* 546 */         for (int cat = 0; cat < this.numCat; cat++)
/* 547 */           this.localSequence.setSequenceAtNodeInCategory(tempRootSeq[cat], cat, n);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeGTR(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 560 */     List children = node.getChildren();
/* 561 */     Node node1 = (Node)children.get(0);
/* 562 */     Node node2 = (Node)children.get(1);
/* 563 */     for (int count = 1; count < children.size(); count++)
/*     */     {
/*     */       float[][][] tempRootSeq;
/*     */       double bl1;
/*     */       double bl2;
/*     */       float[][][] tempRootSeq;
/* 566 */       if (count > 1) {
/* 567 */         node1 = (Node)children.get(count);
/* 568 */         node2 = node;
/* 569 */         double bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 570 */         double bl2 = 0.0D;
/* 571 */         tempRootSeq = new float[this.numCat][this.numStates][this.numCharComp];
/*     */       } else {
/* 573 */         bl1 = node1.getAncestorBranchLength() / (1.0D - this.pInv);
/* 574 */         bl2 = node2.getAncestorBranchLength() / (1.0D - this.pInv);
/* 575 */         tempRootSeq = null;
/*     */       }
/* 577 */       Node key = count > 1 ? null : node;
/* 578 */       if (this.underflowScaling.containsKey(key)) {
/* 579 */         this.underflowScaling.remove(key);
/* 580 */         this.underflow = (!this.underflowScaling.isEmpty());
/*     */       }
/* 582 */       int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 583 */       int n1 = ((Integer)this.nodeIndex.get(node1)).intValue();
/* 584 */       int n2 = ((Integer)this.nodeIndex.get(node2)).intValue();
/* 585 */       double[][] ufScaling = new double[this.numCat][this.numCharComp];
/* 586 */       boolean underflowEncountered = false;
/* 587 */       boolean rescalingNeeded = false;
/*     */       do {
/* 589 */         for (int cat = 0; cat < this.numCat; cat++)
/*     */         {
/*     */           Matrix TPM2;
/*     */           Matrix TPM1;
/*     */           Matrix TPM2;
/* 592 */           if (!Tools.isIdentity(this.Q)) {
/* 593 */             if (this.temp == null) this.temp = new Matrix(this.numStates, this.numStates);
/* 594 */             for (int i = 0; i < this.numStates; i++) {
/* 595 */               this.temp.set(i, i, Math.exp(bl1 * this.rates[cat] * this.apRate * this.eg.get(i, i)));
/*     */             }
/* 597 */             Matrix TPM1 = this.ev.times(this.temp).times(this.evi);
/* 598 */             for (int i = 0; i < this.numStates; i++) {
/* 599 */               this.temp.set(i, i, Math.exp(bl2 * this.rates[cat] * this.apRate * this.eg.get(i, i)));
/*     */             }
/* 601 */             TPM2 = this.ev.times(this.temp).times(this.evi);
/*     */           } else {
/* 603 */             TPM1 = this.Q;
/* 604 */             TPM2 = this.Q;
/*     */           }
/*     */ 
/* 607 */           float[][] seq1 = this.localSequence.getSequenceAtCategoryAndNode(cat, n1);
/* 608 */           float[][] seq2 = this.localSequence.getSequenceAtCategoryAndNode(cat, n2);
/* 609 */           float[][] seq = count > 1 ? tempRootSeq[cat] : this.localSequence.getSequenceAtCategoryAndNode(cat, n);
/* 610 */           double[] scalingFactor = ufScaling[cat];
/* 611 */           double[][] tpm1 = TPM1.getArray();
/* 612 */           double[][] tpm2 = TPM2.getArray();
/* 613 */           for (int site = 0; site < this.numCharComp; site++) {
/* 614 */             double lMax = 0.0D;
/* 615 */             for (int state = 0; state < this.numStates; state++) {
/* 616 */               double sum1 = 0.0D; double sum2 = 0.0D;
/* 617 */               for (int s = 0; s < this.numStates; s++) {
/* 618 */                 sum1 += seq1[s][site] * tpm1[state][s];
/* 619 */                 sum2 += seq2[s][site] * tpm2[state][s];
/*     */               }
/* 621 */               double prod = sum1 * sum2;
/* 622 */               if (rescalingNeeded) {
/* 623 */                 double scalFact = scalingFactor[site];
/* 624 */                 seq[state][site] = ((float)(prod / scalFact));
/*     */               } else {
/* 626 */                 seq[state][site] = ((float)prod);
/* 627 */                 if (prod > lMax) lMax = prod;
/* 628 */                 underflowEncountered = true;
/*     */               }
/*     */             }
/* 631 */             if (!rescalingNeeded) scalingFactor[site] = lMax;
/*     */           }
/*     */         }
/* 634 */         if (underflowEncountered)
/*     */         {
/* 636 */           this.underflowScaling.put(key, ufScaling);
/* 637 */           this.underflow = true;
/* 638 */           rescalingNeeded = true;
/* 639 */           underflowEncountered = false;
/*     */         } else {
/* 641 */           rescalingNeeded = false;
/*     */         }
/*     */       }
/* 588 */       while (
/* 643 */         rescalingNeeded);
/* 644 */       if (count > 1)
/* 645 */         for (int cat = 0; cat < this.numCat; cat++)
/* 646 */           this.localSequence.setSequenceAtNodeInCategory(tempRootSeq[cat], cat, n);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.likelihood.LikelihoodClassic
 * JD-Core Version:    0.6.2
 */