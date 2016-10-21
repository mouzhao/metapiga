/*     */ package metapiga.modelization.likelihood;
/*     */ 
/*     */ import Jama.EigenvalueDecomposition;
/*     */ import Jama.Matrix;
/*     */ import cern.jet.stat.Gamma;
/*     */ import java.util.EnumMap;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import metapiga.RateParameter;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */
/*     */ import metapiga.modelization.VanDePeer;
/*     */ import metapiga.modelization.data.Codon;
/*     */ import metapiga.modelization.data.DNA;
/*     */ import metapiga.modelization.data.Data;
/*     */ import metapiga.modelization.data.EmpiricalModels;
/*     */ import metapiga.modelization.data.Protein;
/*     */ import metapiga.modelization.data.codons.tables.CodonTransitionTable;
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */
/*     */
/*     */
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public abstract class Likelihood
/*     */ {
/*     */   protected static final double LIKE_EPSILON = 1.0E-300D;
/*     */   protected static final double SIMPLE_PRECISION = 1.401298464324817E-045D;
/*     */   protected final Dataset.Partition part;
/*     */   private final Parameters.EvaluationDistribution distribution;
/*     */   private final Parameters.EvaluationRate rate;
/*     */   protected final int numStates;
/*     */   protected final int numCharComp;
/*     */   protected final Parameters.EvaluationModel model;
/*     */   protected final SequenceArrays sequences;
/*     */   protected final Set<Node> toUpdate;
/*     */   protected final Tree tree;
/*     */   protected final int numNodes;
/*  60 */   protected boolean underflow = false;
/*     */   protected final Map<Node, double[][]> underflowScaling;
/*     */   protected final Map<Node, Integer> nodeIndex;
/*     */   protected double likelihoodValue;
/*     */   protected final double[] equiFreq;
/*  68 */   protected Map<RateParameter, Double> rateParameters = new EnumMap(RateParameter.class);
/*     */   protected Matrix Q;
/*     */   protected double rateScaling;
/*     */   protected Matrix eg;
/*     */   protected Matrix ev;
/*     */   protected Matrix evi;
/*  74 */   protected Matrix temp = null;
/*     */   private double gammaShape;
/*     */   private final double[] cutpoints;
/*     */   protected final int numCat;
/*     */   protected final double[] rates;
/*     */   protected final double[] invariantSites;
/*     */   protected double pInv;
/*     */   protected double apRate;
/*     */ 
/*     */   protected Likelihood(Dataset.Partition partition, Parameters.EvaluationRate rate, Parameters.EvaluationModel model, Parameters.EvaluationDistribution distribution, double distributionShape, double pinv, double apRate, Map<RateParameter, Double> rateParameters, Parameters.EvaluationStateFrequencies stateFreq, Tree tree, int numSubsets, SequenceArrays seq)
/*     */     throws UnrootableTreeException
/*     */   {
/*  96 */     this.part = partition;
/*  97 */     this.sequences = seq;
/*  98 */     this.numStates = this.sequences.getStateCount();
/*  99 */     this.numCharComp = this.sequences.getCharacterCountNoPadding();
/* 100 */     this.numNodes = this.sequences.getNodeCount();
/* 101 */     this.numCat = this.sequences.getCategoryCount();
/*     */ 
/* 106 */     this.rate = rate;
/* 107 */     this.model = model;
/* 108 */     this.distribution = distribution;
/* 109 */     this.pInv = pinv;
/* 110 */     this.apRate = apRate;
/* 111 */     if (!tree.isRooted()) tree.root();
/* 112 */     this.tree = tree;
/* 113 */     this.nodeIndex = new HashMap();
/*     */ 
/* 115 */     this.underflowScaling = new HashMap();
/* 116 */     int nodeCounter = 0;
/* 117 */     this.toUpdate = new HashSet();
/* 118 */     this.equiFreq = new double[this.numStates];
/*     */     int k;
/*     */     Data d;
/* 121 */     for (Node node : tree.getNodes()) {
/* 122 */       this.nodeIndex.put(node, Integer.valueOf(nodeCounter));
/* 123 */       if (node.isLeaf()) {
/* 124 */         for (k = 0; k < this.numCharComp; k++) {
/* 125 */           d = this.part.getData(node.getLabel(), k);
/* 126 */           for (int cat = 0; cat < this.numCat; cat++) {
/* 127 */             for (int s = 0; s < this.numStates; s++) {
/* 128 */               float stateAppearance = d.isState(s) ? 1 : 0;
/* 129 */               this.sequences.setElement(stateAppearance, nodeCounter, cat, k, s);
/*     */             }
/*     */           }
/* 132 */           if (!model.hasEqualBaseFrequencies()) {
/* 133 */             for (int s = 0; s < this.numStates; s++)
/* 134 */               this.equiFreq[s] += (d.isState(s) ? this.part.getWeight(k) / d.numOfStates() : 0.0D);
/*     */           }
/*     */         }
/*     */       }
/*     */       else {
/* 139 */         this.toUpdate.add(node);
/*     */       }
/* 141 */       nodeCounter++;
/*     */     }
/* 143 */     if ((model.isEmpirical()) && (stateFreq == Parameters.EvaluationStateFrequencies.EMPIRICAL))
/*     */     {
/* 145 */       System.arraycopy(EmpiricalModels.getEmpiricalFrequencies(model), 0, this.equiFreq, 0, this.numStates);
/*     */     }
/* 147 */     else if (!model.hasEqualBaseFrequencies())
/*     */     {
/* 149 */       for (int i = 0; i < this.equiFreq.length; i++) {
/* 150 */         this.equiFreq[i] /= this.part.getNChar() * this.part.getNTax();
/*     */       }
/*     */     }
/*     */     else {
/* 154 */       for (int i = 0; i < this.equiFreq.length; i++) {
/* 155 */         this.equiFreq[i] = (1.0D / this.numStates);
/*     */       }
/*     */     }
/*     */ 
/* 159 */     for (RateParameter rp : RateParameter.getParametersOfModel(model)) {
/* 160 */       this.rateParameters.put(rp, (Double)rateParameters.get(rp));
/*     */     }
/* 162 */     this.cutpoints = new double[this.numCat + 1];
/* 163 */     this.cutpoints[0] = 0.0D;
/* 164 */     this.cutpoints[this.numCat] = 1000.0D;
/* 165 */     this.rates = new double[this.numCat];
/* 166 */     if (this.numCat == 1) this.rates[0] = 1.0D;
/* 167 */     this.Q = new Matrix(new double[this.numStates][this.numStates]);
/* 168 */     this.invariantSites = new double[this.numCharComp];
/* 169 */     for (int site = 0; site < this.invariantSites.length; site++) {
/* 170 */       int i = this.part.getData(0, site).getState();
/* 171 */       if ((i >= 0) && (i < this.equiFreq.length)) {
/* 172 */         this.invariantSites[site] = this.equiFreq[i];
/* 173 */         for (int taxa = 1; taxa < this.part.getNTax(); taxa++) {
/* 174 */           if (this.part.getData(0, site) != this.part.getData(taxa, site)) {
/* 175 */             this.invariantSites[site] = 0.0D;
/* 176 */             break;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 181 */     if (distribution == Parameters.EvaluationDistribution.GAMMA)
/* 182 */       updateGammaDistribution(distributionShape);
/* 183 */     if (distribution == Parameters.EvaluationDistribution.VDP) {
/* 184 */       VanDePeer vdp = new VanDePeer(this.part.getDataType(), partition, this.numCat);
/* 185 */       for (int r = 0; r < this.numCat; r++) {
/* 186 */         this.rates[r] = vdp.getRate(r);
/*     */       }
/* 188 */       updateRateMatrix();
/*     */     } else {
/* 190 */       updateRateMatrix();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Likelihood(Likelihood L, Tree tree) throws UnrootableTreeException {
/* 195 */     this.part = L.part;
/* 196 */     this.numStates = L.numStates;
/* 197 */     this.numCharComp = L.numCharComp;
/* 198 */     this.rate = L.rate;
/* 199 */     this.model = L.model;
/* 200 */     this.distribution = L.distribution;
/* 201 */     this.pInv = L.pInv;
/* 202 */     this.numCat = L.numCat;
/* 203 */     this.apRate = L.apRate;
/* 204 */     if (!tree.isRooted()) tree.root();
/* 205 */     this.tree = tree;
/* 206 */     this.equiFreq = new double[this.numStates];
/* 207 */     System.arraycopy(L.equiFreq, 0, this.equiFreq, 0, L.equiFreq.length);
/* 208 */     this.nodeIndex = new HashMap();
/* 209 */     this.numNodes = L.numNodes;
/* 210 */     this.sequences = L.sequences.clone();
/* 211 */     this.underflow = L.underflow;
/* 212 */     this.underflowScaling = new HashMap();
/*     */ 
/* 214 */     for (Entry E : L.underflowScaling.entrySet()) {
/* 215 */       double[][] ufs = new double[this.numCat][this.numCharComp];
/* 216 */       double[][] Lufs = (double[][])E.getValue();
/* 217 */       for (int i = 0; i < this.numCat; i++) {
/* 218 */         System.arraycopy(Lufs[i], 0, ufs[i], 0, this.numCharComp);
/*     */       }
/* 220 */       Node key = E.getKey() == null ? null : tree.getNode(((Node)E.getKey()).getLabel());
/* 221 */       this.underflowScaling.put(key, ufs);
/*     */     }
/* 223 */     for (Entry E : L.nodeIndex.entrySet()) {
/* 224 */       this.nodeIndex.put(tree.getNode(((Node)E.getKey()).getLabel()), (Integer)E.getValue());
/*     */     }
/*     */ 
/* 227 */     this.toUpdate = new HashSet();
/* 228 */     for (Node n : L.toUpdate) {
/* 229 */       this.toUpdate.add(tree.getNode(n.getLabel()));
/*     */     }
/* 231 */     this.rateParameters = new EnumMap(L.rateParameters);
/* 232 */     this.invariantSites = new double[this.numCharComp];
/* 233 */     System.arraycopy(L.invariantSites, 0, this.invariantSites, 0, L.invariantSites.length);
/* 234 */     this.Q = L.Q.copy();
/* 235 */     this.rateScaling = L.rateScaling;
/* 236 */     if ((this.model != Parameters.EvaluationModel.JC) && 
/* 237 */       (this.model != Parameters.EvaluationModel.K2P) && 
/* 238 */       (this.model != Parameters.EvaluationModel.HKY85) && 
/* 239 */       (this.model != Parameters.EvaluationModel.POISSON)) {
/* 240 */       this.eg = L.eg.copy();
/* 241 */       this.ev = L.ev.copy();
/* 242 */       this.evi = L.evi.copy();
/*     */     }
/* 244 */     this.likelihoodValue = L.likelihoodValue;
/*     */ 
/* 246 */     this.gammaShape = L.gammaShape;
/* 247 */     this.cutpoints = new double[this.numCat + 1];
/* 248 */     System.arraycopy(L.cutpoints, 0, this.cutpoints, 0, L.cutpoints.length);
/* 249 */     this.rates = new double[this.numCat];
/* 250 */     System.arraycopy(L.rates, 0, this.rates, 0, L.rates.length);
/*     */   }
/*     */ 
/*     */   public void clone(Likelihood L) throws UnrootableTreeException {
/* 254 */     this.pInv = L.pInv;
/* 255 */     this.apRate = L.apRate;
/* 256 */     if (!this.tree.isRooted()) this.tree.root();
/* 257 */     System.arraycopy(L.equiFreq, 0, this.equiFreq, 0, L.equiFreq.length);
/* 258 */     this.nodeIndex.clear();
/* 259 */     this.sequences.clone(L.sequences);
/* 260 */     this.underflow = L.underflow;
/*     */ 
/* 262 */     for (Entry E : L.underflowScaling.entrySet()) {
/* 263 */       double[][] ufs = new double[this.numCat][this.numCharComp];
/* 264 */       double[][] Lufs = (double[][])E.getValue();
/* 265 */       for (int i = 0; i < this.numCat; i++) {
/* 266 */         System.arraycopy(Lufs[i], 0, ufs[i], 0, this.numCharComp);
/*     */       }
/* 268 */       Node key = E.getKey() == null ? null : this.tree.getNode(((Node)E.getKey()).getLabel());
/* 269 */       this.underflowScaling.put(key, ufs);
/*     */     }
/* 271 */     for (Entry E : L.nodeIndex.entrySet()) {
/* 272 */       this.nodeIndex.put(this.tree.getNode(((Node)E.getKey()).getLabel()), (Integer)E.getValue());
/*     */     }
/*     */ 
/* 275 */     this.toUpdate.clear();
/* 276 */     for (Node n : L.toUpdate) {
/* 277 */       this.toUpdate.add(this.tree.getNode(n.getLabel()));
/*     */     }
/* 279 */     this.rateParameters = new EnumMap(L.rateParameters);
/* 280 */     System.arraycopy(L.invariantSites, 0, this.invariantSites, 0, L.invariantSites.length);
/* 281 */     this.Q = L.Q.copy();
/* 282 */     this.rateScaling = L.rateScaling;
/* 283 */     if ((this.model != Parameters.EvaluationModel.JC) && 
/* 284 */       (this.model != Parameters.EvaluationModel.K2P) && 
/* 285 */       (this.model != Parameters.EvaluationModel.HKY85) && 
/* 286 */       (this.model != Parameters.EvaluationModel.POISSON)) {
/* 287 */       this.eg = L.eg.copy();
/* 288 */       this.ev = L.ev.copy();
/* 289 */       this.evi = L.evi.copy();
/*     */     }
/* 291 */     this.likelihoodValue = L.likelihoodValue;
/*     */ 
/* 293 */     this.gammaShape = L.gammaShape;
/* 294 */     System.arraycopy(L.cutpoints, 0, this.cutpoints, 0, L.cutpoints.length);
/* 295 */     System.arraycopy(L.rates, 0, this.rates, 0, L.rates.length);
/*     */   }
/*     */ 
/*     */   private void updateRateMatrix()
/*     */   {
/* 300 */     for (int row = 0; row < this.numStates; row++) {
/* 301 */       for (int col = 0; col < this.numStates; col++) {
/* 302 */         if (row != col) {
/* 303 */           double rate = 1.0D;
/* 304 */           switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.model.ordinal()]) {
/*     */           case 18:
/*     */           case 19:
/* 307 */             if (((row == DNA.A.state) && (col == DNA.G.state)) || 
/* 308 */               ((row == DNA.G.state) && (col == DNA.A.state)) || 
/* 309 */               ((row == DNA.C.state) && (col == DNA.T.state)) || (
/* 310 */               (row == DNA.T.state) && (col == DNA.C.state))) {
/* 311 */               rate = ((Double)this.rateParameters.get(RateParameter.K)).doubleValue();
/*     */             }
/* 313 */             break;
/*     */           case 17:
/* 315 */             if (((row == DNA.A.state) && (col == DNA.G.state)) || (
/* 316 */               (row == DNA.G.state) && (col == DNA.A.state)))
/* 317 */               rate = ((Double)this.rateParameters.get(RateParameter.K1)).doubleValue();
/* 318 */             else if (((row == DNA.C.state) && (col == DNA.T.state)) || (
/* 319 */               (row == DNA.T.state) && (col == DNA.C.state))) {
/* 320 */               rate = ((Double)this.rateParameters.get(RateParameter.K2)).doubleValue();
/*     */             }
/* 322 */             break;
/*     */           case 16:
/* 324 */             if (((row == DNA.A.state) && (col == DNA.C.state)) || (
/* 325 */               (row == DNA.C.state) && (col == DNA.A.state)))
/* 326 */               rate = ((Double)this.rateParameters.get(RateParameter.A)).doubleValue();
/* 327 */             else if (((row == DNA.A.state) && (col == DNA.G.state)) || (
/* 328 */               (row == DNA.G.state) && (col == DNA.A.state)))
/* 329 */               rate = ((Double)this.rateParameters.get(RateParameter.B)).doubleValue();
/* 330 */             else if (((row == DNA.A.state) && (col == DNA.T.state)) || (
/* 331 */               (row == DNA.T.state) && (col == DNA.A.state)))
/* 332 */               rate = ((Double)this.rateParameters.get(RateParameter.C)).doubleValue();
/* 333 */             else if (((row == DNA.C.state) && (col == DNA.G.state)) || (
/* 334 */               (row == DNA.G.state) && (col == DNA.C.state)))
/* 335 */               rate = ((Double)this.rateParameters.get(RateParameter.D)).doubleValue();
/* 336 */             else if (((row == DNA.C.state) && (col == DNA.T.state)) || (
/* 337 */               (row == DNA.T.state) && (col == DNA.C.state))) {
/* 338 */               rate = ((Double)this.rateParameters.get(RateParameter.E)).doubleValue();
/*     */             }
/* 340 */             break;
/*     */           case 2:
/*     */           case 6:
/*     */           case 7:
/*     */           case 8:
/*     */           case 9:
/*     */           case 10:
/*     */           case 11:
/*     */           case 12:
/*     */           case 13:
/*     */           case 14:
/* 351 */             if (((row != Protein.Y.state) || (col != Protein.V.state)) && (
/* 352 */               (row != Protein.V.state) || (col != Protein.Y.state))) {
/*     */               try {
/* 354 */                 String cell = 
/* 356 */                   Protein.getProteinWithState(col).toString() + Protein.getProteinWithState(row).toString();
/* 357 */                 rate = ((Double)this.rateParameters.get(RateParameter.valueOf(cell))).doubleValue();
/*     */               } catch (UnknownDataException e) {
/* 359 */                 e.printStackTrace();
/*     */               }
/*     */             }
/* 362 */             else if (this.model == Parameters.EvaluationModel.MTMAM) rate = 0.0D;
/*     */ 
/* 364 */             break;
/*     */           case 3:
/* 366 */             CodonTransitionTable table = this.tree.parameters.getCodonTransitionTable();
/*     */             try {
/* 368 */               if (!table.isSynonymous(Codon.getCodonWithState(col), Codon.getCodonWithState(row))) {
/* 369 */                 rate *= ((Double)this.rateParameters.get(RateParameter.KAPPA)).doubleValue();
/*     */               }
/* 371 */               if (table.isTransition(Codon.getCodonWithState(col), Codon.getCodonWithState(row))) {
/* 372 */                 rate *= ((Double)this.rateParameters.get(RateParameter.OMEGA)).doubleValue();
/*     */               }
/* 374 */               if ((table.isDifferentMoreThanOneNucleotide(Codon.getCodonWithState(col), Codon.getCodonWithState(row))) || (table.isStopCodon(Codon.getCodonWithState(col))) || 
/* 375 */                 (table.isStopCodon(Codon.getCodonWithState(row))))
/* 376 */                 rate = 0.0D;
/*     */             }
/*     */             catch (UnknownDataException e) {
/* 379 */               e.printStackTrace();
/* 380 */               System.exit(-1);
/*     */             }
/*     */ 
/*     */           case 4:
/*     */           case 5:
/* 385 */             if (((row != Codon.GGA.state) || (col != Codon.GGG.state)) && (
/* 386 */               (row != Codon.GGG.state) || (col != Codon.GGA.state)))
/*     */               try {
/* 388 */                 String cell = 
/* 390 */                   Codon.getCodonWithState(col).toString() + Codon.getCodonWithState(row);
/* 391 */                 rate = ((Double)this.rateParameters.get(RateParameter.valueOf(cell))).doubleValue();
/*     */               } catch (UnknownDataException e) {
/* 393 */                 e.printStackTrace();
/*     */               }
/*     */             break;
/*     */           case 1:
/* 397 */             break;
/*     */           case 20:
/* 399 */             break;
/*     */           case 15:
/* 401 */             break;
/*     */           }
/*     */ 
/* 405 */           this.Q.set(row, col, rate * this.equiFreq[col]);
/*     */         }
/*     */       }
/*     */     }
/* 409 */     for (int i = 0; i < this.numStates; i++) {
/* 410 */       double sum = 0.0D;
/* 411 */       for (int j = 0; j < this.numStates; j++) {
/* 412 */         if (i != j) sum += this.Q.get(i, j);
/*     */       }
/* 414 */       this.Q.set(i, i, -sum);
/*     */     }
/*     */ 
/* 417 */     this.rateScaling = 0.0D;
/* 418 */     for (int i = 0; i < this.numStates; i++) {
/* 419 */       for (int j = 0; j < this.numStates; j++) {
/* 420 */         if (i != j) {
/* 421 */           this.rateScaling += this.equiFreq[i] * this.Q.get(i, j);
/*     */         }
/*     */       }
/*     */     }
/* 425 */     this.rateScaling = (1.0D / this.rateScaling);
/* 426 */     for (int i = 0; i < this.numStates; i++) {
/* 427 */       for (int j = 0; j < this.numStates; j++) {
/* 428 */         this.Q.set(i, j, this.Q.get(i, j) * this.rateScaling);
/*     */       }
/*     */     }
/*     */ 
/* 432 */     if ((this.model != Parameters.EvaluationModel.JC) && 
/* 433 */       (this.model != Parameters.EvaluationModel.K2P) && 
/* 434 */       (this.model != Parameters.EvaluationModel.HKY85) && 
/* 435 */       (this.model != Parameters.EvaluationModel.POISSON)) {
/* 436 */       EigenvalueDecomposition Rev = this.Q.eig();
/* 437 */       this.eg = Rev.getD();
/* 438 */       this.ev = Rev.getV();
/* 439 */       this.evi = this.ev.inverse();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateRateParameter(RateParameter rateParameter, double newValue)
/*     */   {
/* 445 */     this.rateParameters.put(rateParameter, Double.valueOf(newValue));
/* 446 */     updateRateMatrix();
/* 447 */     markAllInodesToUpdate();
/*     */   }
/*     */ 
/*     */   public void updateGammaDistribution(double newAlpha) {
/* 451 */     this.gammaShape = newAlpha;
/* 452 */     for (int i = 1; i < this.numCat; i++) {
/* 453 */       double k = i / this.numCat;
/* 454 */       this.cutpoints[i] = (Tools.percentagePointChi2(k, 2.0D * this.gammaShape) / (2.0D * this.gammaShape));
/*     */     }
/* 456 */     for (int i = 0; i < this.numCat; i++) {
/* 457 */       this.rates[i] = ((Gamma.incompleteGamma(this.gammaShape + 1.0D, this.cutpoints[(i + 1)] * this.gammaShape) - Gamma.incompleteGamma(this.gammaShape + 1.0D, this.cutpoints[i] * this.gammaShape)) / (1.0D / this.numCat));
/*     */     }
/* 459 */     updateRateMatrix();
/* 460 */     markAllInodesToUpdate();
/*     */   }
/*     */ 
/*     */   public void updateInvariant(double newPinv) {
/* 464 */     this.pInv = newPinv;
/* 465 */     if (this.pInv > 1.0D) this.pInv = 1.0D;
/* 466 */     else if (this.pInv < 0.0D) this.pInv = 0.0D;
/* 467 */     markAllInodesToUpdate();
/*     */   }
/*     */ 
/*     */   public void updateAmongPartitionRate(double newRate) {
/* 471 */     this.apRate = newRate;
/* 472 */     if (this.apRate <= 0.0D) this.apRate = 0.01D;
/* 473 */     markAllInodesToUpdate();
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationModel getModel()
/*     */   {
/* 479 */     return this.model;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationRate getRate() {
/* 483 */     return this.rate;
/*     */   }
/*     */ 
/*     */   public Map<RateParameter, Double> getRateParameters() {
/* 487 */     return new EnumMap(this.rateParameters);
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationDistribution getDistribution() {
/* 491 */     return this.distribution;
/*     */   }
/*     */ 
/*     */   public int getDistributionSubsets() {
/* 495 */     return this.numCat;
/*     */   }
/*     */ 
/*     */   public double getGammaShape() {
/* 499 */     return this.gammaShape;
/*     */   }
/*     */ 
/*     */   public double getPInv() {
/* 503 */     return this.pInv;
/*     */   }
/*     */ 
/*     */   public double getAmongPartitionRate() {
/* 507 */     return this.apRate;
/*     */   }
/*     */ 
/*     */   public boolean hasUnderflow() {
/* 511 */     return this.underflow;
/*     */   }
/*     */ 
/*     */   public void markInodeToUpdate(Node node) {
/* 515 */     if (node.isInode()) this.toUpdate.add(node); 
/*     */   }
/*     */ 
/*     */   public void markAllInodesToUpdate()
/*     */   {
/* 519 */     for (Node n : this.nodeIndex.keySet())
/* 520 */       if (n.isInode()) this.toUpdate.add(n); 
/*     */   }
/*     */ 
/*     */   public double[][] getAncestralStates(Node node)
/*     */     throws NullAncestorException
/*     */   {
/* 525 */     double[][] ancStates = new double[this.numCharComp][this.numStates];
/* 526 */     int nodeIdx = ((Integer)this.nodeIndex.get(node)).intValue();
/* 527 */     if (!this.tree.getLeaves().contains(node)) {
/* 528 */       Node previousRoot = this.tree.getRoot();
/* 529 */       this.tree.root(node);
/* 530 */       update(node);
/* 531 */       double[] siteLikelihoods = new double[this.numCharComp];
/* 532 */       for (int site = 0; site < this.numCharComp; site++) {
/* 533 */         for (int state = 0; state < this.numStates; state++) {
/* 534 */           ancStates[site][state] = 0.0D;
/* 535 */           for (int cat = 0; cat < this.numCat; cat++) {
/* 536 */             double ufscaling = 1.0D;
/* 537 */             if (this.underflow) {
/* 538 */               for (double[][] s : this.underflowScaling.values()) {
/* 539 */                 ufscaling *= s[cat][site];
/*     */               }
/*     */             }
/* 542 */             ancStates[site][state] += this.sequences.getElement(nodeIdx, cat, site, state) * 
/* 543 */               this.equiFreq[state] * ((1.0D - this.pInv) / this.numCat) * ufscaling;
/* 544 */             siteLikelihoods[site] += ancStates[site][state];
/*     */           }
/* 546 */           if (ancStates[site][state] < 1.0E-300D) {
/* 547 */             ancStates[site][state] = 0.0D;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 554 */       for (int site = 0; site < this.numCharComp; site++) {
/* 555 */         for (int state = 0; state < this.numStates; state++) {
/* 556 */           ancStates[site][state] /= siteLikelihoods[site];
/*     */         }
/*     */       }
/* 559 */       this.tree.root(previousRoot);
/*     */     } else {
/* 561 */       for (int site = 0; site < this.numCharComp; site++) {
/* 562 */         for (int state = 0; state < this.numStates; state++) {
/* 563 */           ancStates[site][state] = this.sequences.getElement(nodeIdx, 0, site, state);
/*     */         }
/*     */       }
/*     */     }
/* 567 */     return ancStates;
/*     */   }
/*     */ 
/*     */   public double getLikelihoodValue() throws NullAncestorException {
/* 571 */     if (!this.toUpdate.isEmpty()) {
/* 572 */       update(this.tree.getRoot());
/*     */     }
/* 574 */     return this.likelihoodValue;
/*     */   }
/*     */ 
/*     */   protected void calculateLikelihoodAtRoot() {
/* 578 */     Node node = this.tree.getRoot();
/* 579 */     this.likelihoodValue = 0.0D;
/* 580 */     int n = ((Integer)this.nodeIndex.get(node)).intValue();
/* 581 */     for (int site = 0; site < this.numCharComp; site++) {
/* 582 */       double siteLikelihoodVar = 0.0D;
/* 583 */       for (int state = 0; state < this.numStates; state++) {
/* 584 */         for (int cat = 0; cat < this.numCat; cat++) {
/* 585 */           double ufscaling = 1.0D;
/* 586 */           if (this.underflow) {
/* 587 */             for (double[][] s : this.underflowScaling.values()) {
/* 588 */               ufscaling *= s[cat][site];
/*     */             }
/*     */           }
/* 591 */           double sequenceValue = this.sequences.getElement(n, cat, site, state);
/* 592 */           double equilibriumF = this.equiFreq[state];
/* 593 */           siteLikelihoodVar += sequenceValue * equilibriumF * ((1.0D - this.pInv) / this.numCat) * ufscaling;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 600 */       if (siteLikelihoodVar < 1.0E-300D) {
/* 601 */         siteLikelihoodVar = 1.0E-300D;
/*     */       }
/* 603 */       double siteLikelihoodInv = this.invariantSites[site] * this.pInv;
/* 604 */       this.likelihoodValue += Math.log(siteLikelihoodVar + siteLikelihoodInv) * this.part.getWeight(site);
/*     */     }
/*     */ 
/* 608 */     this.toUpdate.remove(this.tree.getRoot());
/*     */   }
/*     */ 
/*     */   protected abstract void update(Node paramNode)
/*     */     throws NullAncestorException;
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.Likelihood
 * JD-Core Version:    0.6.2
 */