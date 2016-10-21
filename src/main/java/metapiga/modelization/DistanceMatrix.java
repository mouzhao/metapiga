/*     */ package metapiga.modelization;
/*     */ 
/*     */ import Jama.EigenvalueDecomposition;
/*     */ import Jama.Matrix;
/*     */ import java.awt.Color;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import javax.swing.text.AttributeSet;
/*     */ import javax.swing.text.BadLocationException;
/*     */ import javax.swing.text.DefaultStyledDocument;
/*     */ import javax.swing.text.SimpleAttributeSet;
/*     */ import javax.swing.text.StyleConstants;
/*     */ import metapiga.modelization.data.Codon;
/*     */ import metapiga.modelization.data.DNA;
/*     */ import metapiga.modelization.data.Data;
/*     */ import metapiga.modelization.data.DataType;
/*     */
/*     */
/*     */
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class DistanceMatrix extends Matrix
/*     */ {
/*     */   public final DataType dataType;
/*     */   public final int numOfStates;
/*     */   public final Set<Dataset.Partition> partitions;
/*     */   public final int ntax;
/*     */   public final List<String> taxas;
/*     */   public final Parameters.DistanceModel model;
/*     */   public final Parameters.StartingTreeDistribution distribution;
/*     */   public final double gammaShape;
/*     */   public final double pinv;
/*     */   public final Parameters.StartingTreePInvPi pi;
/*  47 */   private boolean saturation = false;
/*     */   public final double saturationThreshold;
/*  54 */   private int codonPosition = -1;
/*  55 */   private int numElemMat = 0;
/*  56 */   private List<DistanceMatrix> elemMatrices = new ArrayList();
/*     */ 
/*  60 */   private List<Double> lengthWeights = new ArrayList();
/*  61 */   private List<Double> evolutionWeights = new ArrayList();
/*     */ 
/*     */   public DistanceMatrix(DataType dataType, Set<Dataset.Partition> partitions, List<String> taxas, Parameters.DistanceModel model, Parameters.StartingTreeDistribution distribution, double distributionShape, double pinv, Parameters.StartingTreePInvPi pi)
/*     */   {
/*  65 */     super(taxas.size(), taxas.size());
/*  66 */     this.dataType = dataType;
/*  67 */     this.ntax = taxas.size();
/*  68 */     this.taxas = taxas;
/*  69 */     this.partitions = partitions;
/*  70 */     this.model = model;
/*  71 */     this.distribution = distribution;
/*  72 */     this.gammaShape = distributionShape;
/*  73 */     this.pinv = pinv;
/*  74 */     this.pi = pi;
/*  75 */     switch (dataType) {
/*     */     case CODON:
/*  77 */       this.saturationThreshold = 0.65D;
/*  78 */       this.numOfStates = dataType.numOfStates();
/*  79 */       break;
/*     */     case DNA:
/*  81 */       this.numOfStates = dataType.numOfStates();
/*  82 */       this.saturationThreshold = 0.85D;
/*  83 */       break;
/*     */     case PROTEIN:
/*  85 */       this.numOfStates = dataType.numOfStates();
/*  86 */       this.saturationThreshold = 0.45D;
/*  87 */       break;
/*     */     case STANDARD:
/*  90 */       this.numOfStates = DataType.DNA.numOfStates();
/*  91 */       this.saturationThreshold = 0.65D;
/*     */ 
/*  93 */       this.numElemMat = 3;
/*  94 */       break;
/*     */     default:
/*  96 */       if (!$assertionsDisabled) throw new AssertionError("Unknown datatype");
/*  97 */       this.numOfStates = dataType.numOfStates();
/*  98 */       this.saturationThreshold = 0.65D;
/*     */     }
/*     */ 
/* 101 */     if (dataType == DataType.CODON) {
/* 102 */       for (int i = 0; i < this.numElemMat; i++) {
/* 103 */         this.codonPosition = i;
/* 104 */         DistanceMatrix elemMat = new DistanceMatrix(this);
/* 105 */         this.elemMatrices.add(elemMat);
/* 106 */         this.lengthWeights.add(Double.valueOf(1.0D));
/*     */       }
/* 108 */       findEvolWeights();
/* 109 */       initWeighted2CED();
/*     */     } else {
/* 111 */       init(model);
/*     */     }
/*     */   }
/*     */ 
/*     */   private DistanceMatrix(DistanceMatrix superMatrix) {
/* 116 */     super(superMatrix.taxas.size(), superMatrix.taxas.size());
/* 117 */     this.dataType = superMatrix.dataType;
/* 118 */     this.ntax = superMatrix.taxas.size();
/* 119 */     this.taxas = superMatrix.taxas;
/* 120 */     this.partitions = superMatrix.partitions;
/* 121 */     this.model = superMatrix.model;
/* 122 */     this.distribution = superMatrix.distribution;
/* 123 */     this.gammaShape = superMatrix.gammaShape;
/* 124 */     this.pinv = superMatrix.pinv;
/* 125 */     this.pi = superMatrix.pi;
/* 126 */     this.saturationThreshold = superMatrix.saturationThreshold;
/* 127 */     this.numOfStates = superMatrix.numOfStates;
/* 128 */     if (this.dataType == DataType.CODON) {
/* 129 */       this.codonPosition = superMatrix.codonPosition;
/* 130 */       init(this.model);
/*     */     }
/* 132 */     else if (!$assertionsDisabled) { throw new AssertionError("Unknown case!"); }
/*     */ 
/*     */   }
/*     */ 
/*     */   private void init(Parameters.DistanceModel model)
/*     */   {
/* 139 */     switch (model) {
/*     */     case JC:
/* 141 */       initJC();
/* 142 */       break;
/*     */     case HKY85:
/* 144 */       initK2P();
/* 145 */       break;
/*     */     case GY:
/* 147 */       initHKY85();
/* 148 */       break;
/*     */     case GTR64:
/* 150 */       initTN93();
/* 151 */       break;
/*     */     case GTR20:
/* 153 */       initGTR();
/* 154 */       break;
/*     */     case ABSOLUTE:
/* 156 */       initGTR();
/* 157 */       break;
/*     */     case GTR:
/* 159 */       initGTR();
/* 160 */       break;
/*     */     case GTR2:
/* 162 */       initGTR();
/* 163 */       break;
/*     */     case K2P:
/* 165 */       initUncorrected();
/* 166 */       break;
/*     */     case NONE:
/* 168 */       initAbsoluteDifferences();
/* 169 */       break;
/*     */     case POISSON:
/* 171 */       initGTR();
/* 172 */       break;
/*     */     case TN93:
/* 174 */       initGTR();
/* 175 */       break;
/*     */     case UNCORRECTED:
/*     */     default:
/* 178 */       if (!$assertionsDisabled) throw new AssertionError("no model for the distance matrix");
/*     */       break;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void initWeightedCED()
/*     */   {
/* 191 */     for (int p = 0; p < this.elemMatrices.size(); p++) {
/* 192 */       double rate = ((Double)this.evolutionWeights.get(p)).doubleValue();
/* 193 */       for (int i = 0; i < this.ntax; i++)
/* 194 */         for (int j = i + 1; j < this.ntax; j++) {
/* 195 */           double pMatVal = ((DistanceMatrix)this.elemMatrices.get(p)).get(i, j);
/* 196 */           double currentVal = get(i, j);
/* 197 */           double newValue = currentVal + pMatVal * rate;
/* 198 */           set(i, j, newValue);
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void initWeighted2CED()
/*     */   {
/* 205 */     double sumRates = 0.0D;
/* 206 */     double[] oldEvolWeights = new double[this.numElemMat];
/* 207 */     for (int p = 0; p < this.numElemMat; p++) {
/* 208 */       sumRates += ((Double)this.evolutionWeights.get(p)).doubleValue();
/* 209 */       oldEvolWeights[p] = ((Double)this.evolutionWeights.get(p)).doubleValue();
/*     */     }
/* 211 */     boolean isAllZeros = true;
/* 212 */     for (int p = 0; p < this.numElemMat; p++) {
/* 213 */       double arboricity = arboricity((DistanceMatrix)this.elemMatrices.get(p));
/* 214 */       double normalizedWeight = ((Double)this.evolutionWeights.get(p)).doubleValue() * arboricity / sumRates;
/* 215 */       if (Math.abs(normalizedWeight - 0.0D) > 0.001D) {
/* 216 */         isAllZeros = false;
/*     */       }
/* 218 */       this.evolutionWeights.set(p, Double.valueOf(normalizedWeight));
/*     */     }
/* 220 */     if (isAllZeros) {
/* 221 */       for (int p = 0; p < this.numElemMat; p++) {
/* 222 */         this.evolutionWeights.set(p, Double.valueOf(1.0D));
/*     */       }
/*     */     }
/* 225 */     initWeightedCED();
/*     */   }
/*     */ 
/*     */   private double arboricity(DistanceMatrix matrix) {
/* 229 */     double arboricity = 0.0D;
/* 230 */     double count = 0.0D;
/* 231 */     for (int i = 0; i < this.ntax; i++) {
/* 232 */       for (int j = i + 1; j < this.ntax; j++) {
/* 233 */         for (int x = j + 1; x < this.ntax; x++) {
/* 234 */           for (int y = x + 1; y < this.ntax; y++)
/*     */           {
/* 236 */             double[] vals = new double[3];
/* 237 */             vals[0] = (matrix.get(i, j) + matrix.get(x, y));
/* 238 */             vals[1] = (matrix.get(i, x) + matrix.get(j, y));
/* 239 */             vals[2] = (matrix.get(i, y) + matrix.get(j, x));
/* 240 */             Arrays.sort(vals);
/* 241 */             if (vals[1] - vals[0] > vals[2] - vals[1]) {
/* 242 */               arboricity += 1.0D;
/*     */             }
/* 244 */             count += 1.0D;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 249 */     arboricity /= count;
/* 250 */     return arboricity;
/*     */   }
/*     */ 
/*     */   private void findEvolWeights()
/*     */   {
/* 259 */     double sumWeights = 0.0D;
/* 260 */     for (int p = 0; p < this.lengthWeights.size(); p++) {
/* 261 */       sumWeights += ((Double)this.lengthWeights.get(p)).doubleValue();
/*     */     }
/* 263 */     int taxaTotal = this.taxas.size();
/*     */ 
/* 267 */     for (int p = 0; p < this.numElemMat; p++) {
/* 268 */       boolean correctEntries = true;
/* 269 */       double maxIneqFact = -1.0D;
/* 270 */       Matrix dstMat = (Matrix)this.elemMatrices.get(p);
/* 271 */       for (int i = 0; i < taxaTotal; i++) {
/* 272 */         for (int j = i + 1; j < taxaTotal; j++) {
/* 273 */           if (dstMat.get(i, j) == 0.0D) {
/* 274 */             correctEntries = false;
/*     */           }
/* 276 */           for (int m = j + 1; m < taxaTotal; m++) {
/* 277 */             double ineqFact = 
/* 278 */               dstMat.get(i, j) - 
/* 279 */               dstMat.get(j, m) - 
/* 280 */               dstMat.get(i, m);
/* 281 */             if ((m != i) && (m != j) && (maxIneqFact < ineqFact)) {
/* 282 */               maxIneqFact = ineqFact;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 287 */       if ((maxIneqFact == 0.0D) && (!correctEntries)) {
/* 288 */         maxIneqFact = 1.E-009D;
/*     */       }
/* 290 */       if (maxIneqFact > 0.0D)
/*     */       {
/* 294 */         for (int i = 0; i < taxaTotal; i++) {
/* 295 */           for (int j = i + 1; j < taxaTotal; j++) {
/* 296 */             if (i != j) {
/* 297 */               ((DistanceMatrix)this.elemMatrices.get(p)).set(i, j, maxIneqFact);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 307 */     Matrix A = new Matrix(this.numElemMat + 1, this.numElemMat + 1);
/* 308 */     Matrix b = new Matrix(this.numElemMat + 1, 1);
/* 309 */     b.set(this.numElemMat, 0, this.numElemMat);
/* 310 */     for (int p = 0; p < this.numElemMat; p++) {
/* 311 */       A.set(p, this.numElemMat, 1.0D);
/* 312 */       A.set(this.numElemMat, p, 1.0D);
/* 313 */       b.set(p, 0, 0.0D);
/* 314 */       for (int i = 0; i < taxaTotal; i++) {
/* 315 */         for (int j = i + 1; j < taxaTotal; j++)
/*     */         {
/* 320 */           double temp = A.get(p, p);
/* 321 */           temp += ((DistanceMatrix)this.elemMatrices.get(p)).get(i, j);
/* 322 */           A.set(p, p, temp);
/*     */ 
/* 327 */           for (int m = 0; m < this.numElemMat; m++) {
/* 328 */             double temp2 = A.get(p, m);
/* 329 */             temp2 += ((Double)this.lengthWeights.get(p)).doubleValue() * ((DistanceMatrix)this.elemMatrices.get(m)).get(i, j) / sumWeights;
/* 330 */             A.set(p, m, temp2);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 339 */     Matrix solution = A.solve(b);
/*     */ 
/* 341 */     for (int i = 0; i < this.numElemMat; i++)
/* 342 */       this.evolutionWeights.add(i, Double.valueOf(solution.get(i, 0)));
/*     */   }
/*     */ 
/*     */   private void initAbsoluteDifferences()
/*     */   {
/* 348 */     for (int i = 0; i < this.ntax - 1; i++)
/* 349 */       for (int j = i + 1; j < this.ntax; j++) {
/* 350 */         double differences = 0.0D;
/*     */         int numChar;
/*     */         int k;
/* 351 */         for (Iterator localIterator = this.partitions.iterator(); localIterator.hasNext(); 
/* 354 */           k < numChar)
/*     */         {
/* 351 */           Dataset.Partition P = (Dataset.Partition)localIterator.next();
/* 352 */           int[] weights = P.getAllWeights();
/* 353 */           numChar = P.getCompressedNChar();
/* 354 */           k = 0; continue;
/*     */ 
/* 357 */           Data col = getData(P, i, k);
/* 358 */           Data row = getData(P, j, k);
/* 359 */           boolean same = false;
/* 360 */           for (int s = 0; s < col.getMaxStates(); s++) {
/* 361 */             if ((row.isState(s)) && (col.isState(s))) same = true;
/*     */           }
/* 363 */           differences += (same ? 0 : weights[k]);
/*     */ 
/* 354 */           k++;
/*     */         }
/*     */ 
/* 366 */         set(i, j, differences);
/*     */       }
/*     */   }
/*     */ 
/*     */   private Data getData(Dataset.Partition P, int taxon, int charIdx)
/*     */   {
/* 372 */     Data d = null;
/* 373 */     if (this.dataType == DataType.CODON) {
/* 374 */       Codon dataCodon = (Codon)P.getData(taxon, charIdx);
/* 375 */       d = dataCodon.getNucleotides()[this.codonPosition];
/*     */     } else {
/* 377 */       d = P.getData(taxon, charIdx);
/*     */     }
/* 379 */     return d;
/*     */   }
/*     */ 
/*     */   private void initUncorrected()
/*     */   {
/* 384 */     for (int i = 0; i < this.ntax - 1; i++)
/* 385 */       for (int j = i + 1; j < this.ntax; j++) {
/* 386 */         double differences = 0.0D;
/* 387 */         double nchar = 0.0D;
/* 388 */         for (Dataset.Partition P : this.partitions) {
/* 389 */           int[] weights = P.getAllWeights();
/* 390 */           int numChar = P.getCompressedNChar();
/* 391 */           for (int k = 0; k < numChar; k++)
/*     */           {
/* 394 */             Data col = getData(P, i, k);
/* 395 */             Data row = getData(P, j, k);
/* 396 */             double contribution = weights[k] / (col.numOfStates() * row.numOfStates());
/* 397 */             for (int s = 0; s < row.getMaxStates(); s++) {
/* 398 */               for (int t = 0; t < col.getMaxStates(); t++) {
/* 399 */                 if (s != t) differences += ((row.isState(s)) && (col.isState(t)) ? contribution : 0.0D);
/*     */               }
/*     */             }
/*     */           }
/* 403 */           nchar += P.getNChar();
/*     */         }
/* 405 */         differences /= nchar;
/* 406 */         if (differences > this.saturationThreshold) {
/* 407 */           this.saturation = true;
/*     */         }
/* 409 */         set(i, j, differences);
/*     */       }
/*     */   }
/*     */ 
/*     */   private void initJC()
/*     */   {
/* 415 */     if (this.pinv > 0.0D)
/* 416 */       initGTR();
/*     */     else
/* 418 */       for (int i = 0; i < this.ntax - 1; i++)
/* 419 */         for (int j = i + 1; j < this.ntax; j++) {
/* 420 */           double differences = 0.0D;
/* 421 */           double nchar = 0.0D;
/* 422 */           for (Dataset.Partition P : this.partitions) {
/* 423 */             int[] weights = P.getAllWeights();
/* 424 */             int numChar = P.getCompressedNChar();
/* 425 */             for (int k = 0; k < numChar; k++) {
/* 426 */               Data col = getData(P, i, k);
/* 427 */               Data row = getData(P, j, k);
/* 428 */               double contribution = weights[k] / (col.numOfStates() * row.numOfStates());
/* 429 */               for (int s = 0; s < row.getMaxStates(); s++) {
/* 430 */                 for (int t = 0; t < col.getMaxStates(); t++) {
/* 431 */                   if (s != t) differences += ((row.isState(s)) && (col.isState(t)) ? contribution : 0.0D);
/*     */                 }
/*     */               }
/*     */             }
/* 435 */             nchar += P.getNChar();
/*     */           }
/* 437 */           differences /= nchar;
/* 438 */           if (differences > this.saturationThreshold) {
/* 439 */             this.saturation = true;
/*     */           }
/* 441 */           double part1 = 1.0D - 1.333333333333333D * differences;
/*     */ 
/* 443 */           if (part1 <= 0.0D) {
/* 444 */             part1 = 0.01D;
/* 445 */             this.saturation = true;
/*     */           }
/* 447 */           if (this.distribution == Parameters.StartingTreeDistribution.GAMMA)
/* 448 */             set(i, j, -0.75D * (this.gammaShape * (1.0D - Math.pow(part1, -1.0D / this.gammaShape))));
/*     */           else
/* 450 */             set(i, j, -0.75D * Math.log(part1));
/*     */         }
/*     */   }
/*     */ 
/*     */   private void initK2P()
/*     */   {
/* 458 */     if (this.pinv > 0.0D) {
/* 459 */       initGTR();
/*     */     } else {
/* 461 */       int A = DNA.A.state;
/* 462 */       int C = DNA.C.state;
/* 463 */       int G = DNA.G.state;
/* 464 */       int T = DNA.T.state;
/* 465 */       for (int i = 0; i < this.ntax - 1; i++)
/* 466 */         for (int j = i + 1; j < this.ntax; j++) {
/* 467 */           double transition = 0.0D;
/* 468 */           double transversion = 0.0D;
/* 469 */           double nchar = 0.0D;
/* 470 */           for (Dataset.Partition P : this.partitions) {
/* 471 */             int[] weights = P.getAllWeights();
/* 472 */             int numChar = P.getCompressedNChar();
/* 473 */             for (int k = 0; k < numChar; k++) {
/* 474 */               Data col = getData(P, i, k);
/* 475 */               Data row = getData(P, j, k);
/*     */ 
/* 485 */               double contribution = weights[k] / (col.numOfStates() * row.numOfStates());
/* 486 */               transition += ((row.isState(A)) && (col.isState(G)) ? contribution : 0.0D);
/* 487 */               transition += ((row.isState(G)) && (col.isState(A)) ? contribution : 0.0D);
/* 488 */               transition += ((row.isState(C)) && (col.isState(T)) ? contribution : 0.0D);
/* 489 */               transition += ((row.isState(T)) && (col.isState(C)) ? contribution : 0.0D);
/* 490 */               transversion += ((row.isState(A)) && (col.isState(C)) ? contribution : 0.0D);
/* 491 */               transversion += ((row.isState(A)) && (col.isState(T)) ? contribution : 0.0D);
/* 492 */               transversion += ((row.isState(C)) && (col.isState(A)) ? contribution : 0.0D);
/* 493 */               transversion += ((row.isState(C)) && (col.isState(G)) ? contribution : 0.0D);
/* 494 */               transversion += ((row.isState(G)) && (col.isState(C)) ? contribution : 0.0D);
/* 495 */               transversion += ((row.isState(G)) && (col.isState(T)) ? contribution : 0.0D);
/* 496 */               transversion += ((row.isState(T)) && (col.isState(A)) ? contribution : 0.0D);
/* 497 */               transversion += ((row.isState(T)) && (col.isState(G)) ? contribution : 0.0D);
/*     */             }
/* 499 */             nchar += P.getNChar();
/*     */           }
/* 501 */           transition /= nchar;
/* 502 */           transversion /= nchar;
/* 503 */           double part1 = 1.0D - 2.0D * transition - transversion;
/* 504 */           double part2 = 1.0D - 2.0D * transversion;
/*     */ 
/* 506 */           if (part1 <= 0.0D) {
/* 507 */             part1 = 0.01D;
/* 508 */             this.saturation = true;
/*     */           }
/* 510 */           if (part2 <= 0.0D) {
/* 511 */             part2 = 0.01D;
/* 512 */             this.saturation = true;
/*     */           }
/* 514 */           if (this.distribution == Parameters.StartingTreeDistribution.GAMMA)
/* 515 */             set(i, j, this.gammaShape / 2.0D * (Math.pow(part1, -1.0D / this.gammaShape) + 0.5D * Math.pow(part2, -1.0D / this.gammaShape) - 1.5D));
/*     */           else
/* 517 */             set(i, j, 0.5D * Math.log(1.0D / part1) + 0.25D * Math.log(1.0D / part2));
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void initHKY85()
/*     */   {
/* 525 */     initGTR();
/*     */   }
/*     */ 
/*     */   private void initTN93() {
/* 529 */     if (this.pinv > 0.0D) {
/* 530 */       initGTR();
/*     */     } else {
/* 532 */       int A = DNA.A.state;
/* 533 */       int C = DNA.C.state;
/* 534 */       int G = DNA.G.state;
/* 535 */       int T = DNA.T.state;
/* 536 */       for (int i = 0; i < this.ntax - 1; i++)
/* 537 */         for (int j = i + 1; j < this.ntax; j++) {
/* 538 */           double transitionAG = 0.0D;
/* 539 */           double transitionCT = 0.0D;
/* 540 */           double transversion = 0.0D;
/* 541 */           double[] F = new double[this.numOfStates];
/* 542 */           double nchar = 0.0D;
/* 543 */           for (Dataset.Partition P : this.partitions) {
/* 544 */             int[] weights = P.getAllWeights();
/* 545 */             int numChar = P.getCompressedNChar();
/* 546 */             for (int k = 0; k < numChar; k++) {
/* 547 */               Data col = getData(P, i, k);
/* 548 */               Data row = getData(P, j, k);
/* 549 */               double contribution = weights[k] / (col.numOfStates() * row.numOfStates());
/* 550 */               transitionAG += ((row.isState(A)) && (col.isState(G)) ? contribution : 0.0D);
/* 551 */               transitionAG += ((row.isState(G)) && (col.isState(A)) ? contribution : 0.0D);
/* 552 */               transitionCT += ((row.isState(C)) && (col.isState(T)) ? contribution : 0.0D);
/* 553 */               transitionCT += ((row.isState(T)) && (col.isState(C)) ? contribution : 0.0D);
/* 554 */               transversion += ((row.isState(A)) && (col.isState(C)) ? contribution : 0.0D);
/* 555 */               transversion += ((row.isState(A)) && (col.isState(T)) ? contribution : 0.0D);
/* 556 */               transversion += ((row.isState(C)) && (col.isState(A)) ? contribution : 0.0D);
/* 557 */               transversion += ((row.isState(C)) && (col.isState(G)) ? contribution : 0.0D);
/* 558 */               transversion += ((row.isState(G)) && (col.isState(C)) ? contribution : 0.0D);
/* 559 */               transversion += ((row.isState(G)) && (col.isState(T)) ? contribution : 0.0D);
/* 560 */               transversion += ((row.isState(T)) && (col.isState(A)) ? contribution : 0.0D);
/* 561 */               transversion += ((row.isState(T)) && (col.isState(G)) ? contribution : 0.0D);
/* 562 */               F[A] += (col.isState(A) ? weights[k] / col.numOfStates() : 0);
/* 563 */               F[A] += (row.isState(A) ? weights[k] / row.numOfStates() : 0);
/* 564 */               F[C] += (col.isState(C) ? weights[k] / col.numOfStates() : 0);
/* 565 */               F[C] += (row.isState(C) ? weights[k] / row.numOfStates() : 0);
/* 566 */               F[G] += (col.isState(G) ? weights[k] / col.numOfStates() : 0);
/* 567 */               F[G] += (row.isState(G) ? weights[k] / row.numOfStates() : 0);
/* 568 */               F[T] += (col.isState(T) ? weights[k] / col.numOfStates() : 0);
/* 569 */               F[T] += (row.isState(T) ? weights[k] / row.numOfStates() : 0);
/*     */             }
/* 571 */             nchar += P.getNChar();
/*     */           }
/* 573 */           transitionAG /= nchar;
/* 574 */           transitionCT /= nchar;
/* 575 */           transversion /= nchar;
/* 576 */           for (int f = 0; f < F.length; f++) F[f] /= nchar * 2.0D;
/* 577 */           double Fr = F[A] + F[G];
/* 578 */           double Fy = F[C] + F[T];
/*     */ 
/* 580 */           double part1 = 1.0D - Fr / (2.0D * F[A] * F[G]) * transitionAG - 1.0D / (2.0D * Fr) * transversion;
/* 581 */           double part2 = 1.0D - Fy / (2.0D * F[T] * F[C]) * transitionCT - 1.0D / (2.0D * Fy) * transversion;
/* 582 */           double part3 = 1.0D - 1.0D / (2.0D * Fr * Fy) * transversion;
/*     */ 
/* 584 */           if (part1 <= 0.0D) {
/* 585 */             part1 = 0.01D;
/* 586 */             this.saturation = true;
/*     */           }
/* 588 */           if (part2 <= 0.0D) {
/* 589 */             part2 = 0.01D;
/* 590 */             this.saturation = true;
/*     */           }
/* 592 */           if (part3 <= 0.0D) {
/* 593 */             part3 = 0.01D;
/* 594 */             this.saturation = true;
/*     */           }
/*     */           double distance;
/* 595 */           if (this.distribution == Parameters.StartingTreeDistribution.GAMMA) {
/* 596 */             double distance = F[A] * F[G] / Fr * Math.pow(part1, -1.0D / this.gammaShape);
/* 597 */             distance += F[T] * F[C] / Fy * Math.pow(part2, -1.0D / this.gammaShape);
/* 598 */             distance += (Fr * Fy - F[A] * F[G] * Fy / Fr - F[T] * F[C] * Fr / Fy) * Math.pow(part3, -1.0D / this.gammaShape);
/* 599 */             distance += -F[A] * F[G] - F[T] * F[C] - Fr * Fy;
/* 600 */             distance *= 2.0D * this.gammaShape;
/*     */           } else {
/* 602 */             distance = 2.0D * F[A] * F[G] / Fr * Math.log(1.0D / part1);
/* 603 */             distance += 2.0D * F[T] * F[C] / Fy * Math.log(1.0D / part2);
/* 604 */             distance += 2.0D * (Fr * Fy - F[A] * F[G] * Fy / Fr - F[T] * F[C] * Fr / Fy) * Math.log(1.0D / part3);
/*     */           }
/* 606 */           set(i, j, distance);
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void initGTR()
/*     */   {
/* 614 */     double[] equiFreq = null;
/*     */     int site;
/* 615 */     if (this.pinv > 0.0D) {
/* 616 */       equiFreq = new double[this.numOfStates];
/* 617 */       double nchar = 0.0D;
/*     */       Dataset.Partition P;
/* 618 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$StartingTreePInvPi()[this.pi.ordinal()]) {
/*     */       case 1:
/* 620 */         for (int i = 0; i < this.numOfStates; i++) {
/* 621 */           equiFreq[i] = 0.25D;
/*     */         }
/* 623 */         break;
/*     */       case 2:
/* 625 */         for (int taxa = 0; taxa < this.ntax; taxa++) {
/* 626 */           for (Iterator localIterator = this.partitions.iterator(); localIterator.hasNext(); ) { P = (Dataset.Partition)localIterator.next();
/* 627 */             int[] weights = P.getAllWeights();
/* 628 */             int numChar = P.getCompressedNChar();
/* 629 */             for (int site = 0; site < numChar; site++)
/*     */             {
/* 631 */               Data d = getData(P, taxa, site);
/* 632 */               for (int s = 0; s < d.getMaxStates(); s++) {
/* 633 */                 equiFreq[s] += (d.isState(s) ? weights[site] / d.numOfStates() : 0);
/*     */               }
/*     */             }
/* 636 */             nchar += P.getNChar();
/*     */           }
/*     */         }
/* 639 */         for (int i = 0; i < equiFreq.length; i++) {
/* 640 */           equiFreq[i] /= nchar;
/*     */         }
/* 642 */         break;
/*     */       case 3:
/*     */         int numChar;
/* 644 */         for (P = this.partitions.iterator(); P.hasNext(); 
/* 647 */           site < numChar)
/*     */         {
/* 644 */           Dataset.Partition P = (Dataset.Partition)P.next();
/* 645 */           int[] weights = P.getAllWeights();
/* 646 */           numChar = P.getCompressedNChar();
/* 647 */           site = 0; continue;
/*     */ 
/* 649 */           Data d = getData(P, 0, site);
/* 650 */           boolean isConstant = d.numOfStates() == 1;
/* 651 */           if (isConstant) {
/* 652 */             for (int taxa = 1; taxa < P.getNTax(); taxa++)
/*     */             {
/* 654 */               if (d != getData(P, taxa, site)) {
/* 655 */                 isConstant = false;
/* 656 */                 break;
/*     */               }
/*     */             }
/* 659 */             if (isConstant) {
/* 660 */               for (int s = 0; s < d.getMaxStates(); s++) {
/* 661 */                 equiFreq[s] += (d.isState(s) ? weights[site] : 0);
/*     */               }
/* 663 */               nchar += weights[site];
/*     */             }
/*     */           }
/* 647 */           site++;
/*     */         }
/*     */ 
/* 668 */         for (int i = 0; i < equiFreq.length; i++) {
/* 669 */           equiFreq[i] /= nchar;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 675 */     for (int taxaA = 0; taxaA < this.ntax - 1; taxaA++)
/* 676 */       for (int taxaB = taxaA + 1; taxaB < this.ntax; taxaB++) {
/* 677 */         double[][] F = new double[this.numOfStates][this.numOfStates];
/* 678 */         double nchar = 0.0D;
/* 679 */         for (Dataset.Partition P : this.partitions) {
/* 680 */           int[] weights = P.getAllWeights();
/* 681 */           int numChar = P.getCompressedNChar();
/* 682 */           for (int k = 0; k < numChar; k++)
/*     */           {
/* 685 */             Data col = getData(P, taxaA, k);
/* 686 */             Data row = getData(P, taxaB, k);
/* 687 */             double contribution = weights[k] / (col.numOfStates() * row.numOfStates());
/* 688 */             for (int s = 0; s < row.getMaxStates(); s++) {
/* 689 */               for (int t = 0; t < col.getMaxStates(); t++) {
/* 690 */                 F[s][t] += ((row.isState(s)) && (col.isState(t)) ? contribution : 0.0D);
/*     */               }
/*     */             }
/*     */           }
/* 694 */           nchar += P.getNChar();
/*     */         }
/*     */ 
/* 697 */         if (this.pinv > 0.0D) {
/* 698 */           for (int s = 0; s < this.numOfStates; s++) {
/* 699 */             F[s][s] -= equiFreq[s] * this.pinv * nchar;
/*     */           }
/*     */ 
/* 702 */           for (int i = 0; i < this.numOfStates; i++) {
/* 703 */             for (int j = 0; j < this.numOfStates; j++) {
/* 704 */               F[i][j] /= (1.0D - this.pinv);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 709 */         if (this.model != Parameters.DistanceModel.GTR) {
/* 710 */           F = convertFtoNestedModel(F, this.model);
/*     */         }
/*     */ 
/* 713 */         for (int s = 0; s < this.numOfStates - 1; s++) {
/* 714 */           F[s][s] /= nchar;
/* 715 */           for (int t = s + 1; t <= this.numOfStates - 1; t++) {
/* 716 */             F[t][s] = ((F[t][s] + F[s][t]) / 2.0D / nchar);
/* 717 */             F[s][t] = F[t][s];
/*     */           }
/*     */         }
/* 720 */         F[(this.numOfStates - 1)][(this.numOfStates - 1)] /= nchar;
/*     */ 
/* 722 */         Matrix frequency = new Matrix(this.numOfStates, this.numOfStates);
/* 723 */         for (int i = 0; i < this.numOfStates; i++) {
/* 724 */           for (int j = 0; j < this.numOfStates; j++) {
/* 725 */             frequency.set(i, i, F[j][i] + frequency.get(i, i));
/*     */           }
/*     */         }
/*     */ 
/* 729 */         Matrix net = new Matrix(this.numOfStates, this.numOfStates);
/* 730 */         for (int i = 0; i < this.numOfStates; i++) {
/* 731 */           for (int j = 0; j < this.numOfStates; j++)
/*     */           {
/* 737 */             if (frequency.get(i, i) == 0.0D) net.set(i, j, 0.0D); else {
/* 738 */               net.set(i, j, F[i][j] / frequency.get(i, i));
/*     */             }
/*     */           }
/*     */         }
/* 742 */         EigenvalueDecomposition ev = net.eig();
/* 743 */         Matrix omega = ev.getV();
/* 744 */         Matrix psi = ev.getD();
/* 745 */         for (int i = 0; i < this.numOfStates; i++)
/*     */         {
/* 747 */           if (psi.get(i, i) < 0.0D) {
/* 748 */             psi.set(i, i, -psi.get(i, i));
/* 749 */             this.saturation = true;
/*     */           }
/* 751 */           if (this.distribution == Parameters.StartingTreeDistribution.GAMMA) {
/* 752 */             psi.set(i, i, this.gammaShape * (1.0D - Math.pow(psi.get(i, i), -1.0D / this.gammaShape)));
/*     */           } else {
/* 754 */             double logPsi = psi.get(i, i) == 0.0D ? 0.0D : Math.log(psi.get(i, i));
/* 755 */             psi.set(i, i, logPsi);
/*     */           }
/*     */         }
/*     */ 
/* 759 */         Matrix rate = omega.times(psi.times(omega.inverse()));
/*     */ 
/* 761 */         rate = frequency.times(rate);
/* 762 */         set(taxaA, taxaB, -rate.trace());
/*     */       }
/*     */   }
/*     */ 
/*     */   private double[][] convertFtoNestedModel(double[][] F, Parameters.DistanceModel model)
/*     */   {
/* 785 */     switch (model) {
/*     */     case JC:
/* 787 */       F[0][1] = ((F[0][1] + F[0][2] + F[0][3] + F[1][0] + F[1][2] + F[1][3] + F[2][0] + F[2][1] + F[2][3] + F[3][0] + F[3][1] + F[3][2]) / 12.0D);
/* 788 */       F[0][2] = F[0][1];
/* 789 */       F[0][3] = F[0][1];
/* 790 */       F[1][0] = F[0][1];
/* 791 */       F[1][2] = F[0][1];
/* 792 */       F[1][3] = F[0][1];
/* 793 */       F[2][0] = F[0][1];
/* 794 */       F[2][1] = F[0][1];
/* 795 */       F[2][3] = F[0][1];
/* 796 */       F[3][0] = F[0][1];
/* 797 */       F[3][1] = F[0][1];
/* 798 */       F[3][2] = F[0][1];
/* 799 */       F[0][0] = ((F[0][0] + F[1][1] + F[2][2] + F[3][3]) / 4.0D);
/* 800 */       F[1][1] = F[0][0];
/* 801 */       F[2][2] = F[0][0];
/* 802 */       F[3][3] = F[0][0];
/* 803 */       break;
/*     */     case HKY85:
/* 805 */       F[0][1] = ((F[0][1] + F[0][3] + F[1][0] + F[1][2] + F[2][1] + F[2][3] + F[3][0] + F[3][2]) / 8.0D);
/* 806 */       F[0][3] = F[0][1];
/* 807 */       F[1][0] = F[0][1];
/* 808 */       F[1][2] = F[0][1];
/* 809 */       F[2][1] = F[0][1];
/* 810 */       F[2][3] = F[0][1];
/* 811 */       F[3][0] = F[0][1];
/* 812 */       F[3][2] = F[0][1];
/* 813 */       F[0][2] = ((F[0][2] + F[1][3] + F[2][0] + F[3][1]) / 4.0D);
/* 814 */       F[1][3] = F[0][2];
/* 815 */       F[2][0] = F[0][2];
/* 816 */       F[3][1] = F[0][2];
/* 817 */       F[0][0] = ((F[0][0] + F[1][1] + F[2][2] + F[3][3]) / 4.0D);
/* 818 */       F[1][1] = F[0][0];
/* 819 */       F[2][2] = F[0][0];
/* 820 */       F[3][3] = F[0][0];
/* 821 */       break;
/*     */     case GY:
/* 823 */       F[0][1] = ((F[0][1] + F[0][3] + F[1][0] + F[1][2] + F[2][1] + F[2][3] + F[3][0] + F[3][2]) / 8.0D);
/* 824 */       F[0][3] = F[0][1];
/* 825 */       F[1][0] = F[0][1];
/* 826 */       F[1][2] = F[0][1];
/* 827 */       F[2][1] = F[0][1];
/* 828 */       F[2][3] = F[0][1];
/* 829 */       F[3][0] = F[0][1];
/* 830 */       F[3][2] = F[0][1];
/* 831 */       F[0][2] = ((F[0][2] + F[1][3] + F[2][0] + F[3][1]) / 4.0D);
/* 832 */       F[1][3] = F[0][2];
/* 833 */       F[2][0] = F[0][2];
/* 834 */       F[3][1] = F[0][2];
/* 835 */       break;
/*     */     case GTR64:
/* 837 */       F[0][1] = ((F[0][1] + F[0][3] + F[1][0] + F[1][2] + F[2][1] + F[2][3] + F[3][0] + F[3][2]) / 8.0D);
/* 838 */       F[0][3] = F[0][1];
/* 839 */       F[1][0] = F[0][1];
/* 840 */       F[1][2] = F[0][1];
/* 841 */       F[2][1] = F[0][1];
/* 842 */       F[2][3] = F[0][1];
/* 843 */       F[3][0] = F[0][1];
/* 844 */       F[3][2] = F[0][1];
/* 845 */       F[0][2] = ((F[0][2] + F[2][0]) / 2.0D);
/* 846 */       F[2][0] = F[0][2];
/* 847 */       F[1][3] = ((F[1][3] + F[3][1]) / 2.0D);
/* 848 */       F[3][1] = F[1][3];
/* 849 */       break;
/*     */     case GTR2:
/* 851 */       double diag = 0.0D;
/* 852 */       double other = 0.0D;
/* 853 */       for (int i = 0; i < F.length; i++) {
/* 854 */         for (int j = 0; j < F[i].length; j++) {
/* 855 */           if (i == j) diag += F[i][j]; else
/* 856 */             other += F[i][j];
/*     */         }
/*     */       }
/* 859 */       int i = 0;
/*     */       while (true) { for (int j = 0; j < F[i].length; j++)
/* 861 */           F[i][j] = (i == j ? diag / F.length : other / (F.length * F.length - F.length));
/* 859 */         i++; if (i >= F.length)
/*     */         {
/* 864 */           break;
/*     */         } }
/*     */     case GTR20:
/*     */     }
/* 868 */     return F;
/*     */   }
/*     */ 
/*     */   public DefaultStyledDocument show() throws BadLocationException {
/* 872 */     String NORMAL = "Normal";
/* 873 */     String ITALIC = "Italic";
/* 874 */     String BOLD = "Bold";
/* 875 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 876 */     Hashtable paraStyles = new Hashtable();
/* 877 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 878 */     paraStyles.put("Normal", attr);
/* 879 */     attr = new SimpleAttributeSet();
/* 880 */     StyleConstants.setItalic(attr, true);
/* 881 */     paraStyles.put("Italic", attr);
/* 882 */     attr = new SimpleAttributeSet();
/* 883 */     StyleConstants.setBold(attr, true);
/* 884 */     paraStyles.put("Bold", attr);
/* 885 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*     */ 
/* 887 */     AttributeSet boldStyle = (AttributeSet)paraStyles.get("Bold");
/* 888 */     SimpleAttributeSet redStyle = new SimpleAttributeSet();
/* 889 */     StyleConstants.setForeground(redStyle, Color.RED);
/* 890 */     int longestTaxon = 0;
/* 891 */     for (String taxa : this.taxas) {
/* 892 */       if (taxa.length() > longestTaxon) {
/* 893 */         longestTaxon = taxa.length();
/*     */       }
/*     */     }
/* 896 */     doc.insertString(doc.getLength(), "Distance matrix :\n\n", boldStyle);
/* 897 */     doc.insertString(doc.getLength(), this.model.verbose() + ".\n", defaultStyle);
/* 898 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$StartingTreeDistribution()[this.distribution.ordinal()]) {
/*     */     case 1:
/* 900 */       doc.insertString(doc.getLength(), "No rate heterogeneity among sites.\n", defaultStyle);
/* 901 */       break;
/*     */     case 2:
/* 903 */       doc.insertString(doc.getLength(), "Rate heterogeneity among sites following a discrete Gamma distribution (Yang, J. Mol. Evol. 39:306-314 (1994)), using a shape parameter (alpha) of " + this.gammaShape + ".\n", defaultStyle);
/* 904 */       break;
/*     */     case 3:
/* 906 */       doc.insertString(doc.getLength(), "Rate heterogeneity among sites following Van de Peer et al. (J. Mol. Evol. 37:221-232 (1993)), using " + this.gammaShape + " rate categories.\n", defaultStyle);
/*     */     }
/*     */ 
/* 909 */     if (this.pinv > 0.0D) {
/* 910 */       doc.insertString(doc.getLength(), "Assume that " + this.pinv * 100.0D + "% of the sites can not change (adjusting total number of site to have distances equal to the mean number of substitutions over variable sites only).\n", defaultStyle);
/* 911 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$StartingTreePInvPi()[this.pi.ordinal()]) {
/*     */       case 1:
/* 913 */         doc.insertString(doc.getLength(), "The invariant sites will have " + this.dataType.verbose().toLowerCase() + " composition equal for all.\n", defaultStyle);
/* 914 */         break;
/*     */       case 3:
/* 916 */         doc.insertString(doc.getLength(), "The invariant sites reflect as the " + this.dataType.verbose().toLowerCase() + " composition of the site which are constant.\n", defaultStyle);
/* 917 */         break;
/*     */       case 2:
/* 919 */         doc.insertString(doc.getLength(), "The invariant sites reflect the average " + this.dataType.verbose().toLowerCase() + " composition across all sequences.\n", defaultStyle);
/*     */       default:
/* 921 */         break;
/*     */       }
/*     */     } else { doc.insertString(doc.getLength(), "No invariant sites.\n", defaultStyle); }
/*     */ 
/* 925 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/* 926 */     String st = "";
/* 927 */     for (int s = 0; s < longestTaxon; s++) st = st + " ";
/* 928 */     st = st + "\t";
/* 929 */     doc.insertString(doc.getLength(), st, defaultStyle);
/* 930 */     for (String t : this.taxas) {
/* 931 */       int spaces = longestTaxon - t.length();
/* 932 */       for (int s = 0; s < spaces; s++) t = t + " ";
/* 933 */       t = t + "\t";
/* 934 */       doc.insertString(doc.getLength(), t, defaultStyle);
/*     */     }
/* 936 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/* 937 */     for (int i = 0; i < this.ntax; i++) {
/* 938 */       String t = (String)this.taxas.get(i);
/* 939 */       int spaces = longestTaxon - t.length();
/* 940 */       for (int s = 0; s < spaces; s++) t = t + " ";
/* 941 */       t = t + "\t";
/* 942 */       doc.insertString(doc.getLength(), t, defaultStyle);
/* 943 */       for (int j = 0; j < i; j++) {
/* 944 */         t = Tools.doubletoString(get(j, i), 4);
/* 945 */         spaces = longestTaxon - t.length();
/* 946 */         for (int s = 0; s < spaces; s++) t = t + " ";
/* 947 */         t = t + "\t";
/* 948 */         if ((this.model == Parameters.DistanceModel.UNCORRECTED) && (get(j, i) > this.saturationThreshold))
/* 949 */           doc.insertString(doc.getLength(), t, redStyle);
/*     */         else {
/* 951 */           doc.insertString(doc.getLength(), t, defaultStyle);
/*     */         }
/*     */       }
/* 954 */       doc.insertString(doc.getLength(), "\n", defaultStyle);
/*     */     }
/* 956 */     return doc;
/*     */   }
/*     */ 
/*     */   public boolean hasSaturation() {
/* 960 */     return this.saturation;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.DistanceMatrix
 * JD-Core Version:    0.6.2
 */