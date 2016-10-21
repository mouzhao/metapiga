/*     */ package modelization;
/*     */ 
/*     */ import java.util.HashSet;
/*     */ import java.util.Set;
/*     */ import metapiga.modelization.data.Data;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters.DistanceModel;
/*     */ import metapiga.parameters.Parameters.StartingTreeDistribution;
/*     */ import metapiga.parameters.Parameters.StartingTreePInvPi;
/*     */ 
/*     */ public class VanDePeer
/*     */ {
/*     */   private final DataType dataType;
/*     */   private final Dataset.Partition P;
/*     */   private final int nCat;
/*     */   private boolean[] skippedSites;
/*     */   private int totalSites;
/*     */   private double[] rates;
/*     */   private DistanceMatrix distance;
/*     */   private double min;
/*     */   private double max;
/*     */   private double interval;
/*  34 */   private final int numInterval = 40;
/*     */ 
/*     */   public VanDePeer(DataType dataType, Dataset.Partition partition, int numSubsets) {
/*  37 */     this.dataType = dataType;
/*  38 */     this.P = partition;
/*  39 */     this.nCat = numSubsets;
/*  40 */     this.rates = new double[this.nCat];
/*  41 */     this.min = 100000.0D;
/*  42 */     this.max = -100000.0D;
/*  43 */     computeDistanceMatrix();
/*  44 */     double scope = this.max - this.min;
/*  45 */     this.interval = (scope / 40.0D);
/*  46 */     setSkippedSites();
/*  47 */     partitionVn(computeSortedVn(computePlot()));
/*     */   }
/*     */ 
/*     */   public double getRate(int category) {
/*  51 */     return this.rates[category];
/*     */   }
/*     */ 
/*     */   public int getNCat() {
/*  55 */     return this.nCat;
/*     */   }
/*     */ 
/*     */   private void computeDistanceMatrix() {
/*  59 */     Set set = new HashSet();
/*  60 */     set.add(this.P);
/*  61 */     Parameters.DistanceModel dm = null;
/*  62 */     switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataType.ordinal()]) {
/*     */     case 1:
/*  64 */       dm = Parameters.DistanceModel.JC;
/*  65 */       break;
/*     */     case 2:
/*  67 */       dm = Parameters.DistanceModel.POISSON;
/*  68 */       break;
/*     */     case 3:
/*  70 */       dm = Parameters.DistanceModel.GTR2;
/*     */     }
/*     */ 
/*  73 */     this.distance = new DistanceMatrix(this.dataType, set, this.P.getTaxa(), dm, Parameters.StartingTreeDistribution.NONE, 0.0D, 0.0D, Parameters.StartingTreePInvPi.EQUAL);
/*  74 */     for (int i = 0; i < this.distance.getRowDimension(); i++)
/*  75 */       for (int j = i + 1; j < this.distance.getColumnDimension(); j++) {
/*  76 */         if (this.max < this.distance.get(i, j))
/*  77 */           this.max = this.distance.get(i, j);
/*  78 */         if (this.min > this.distance.get(i, j))
/*  79 */           this.min = this.distance.get(i, j);
/*     */       }
/*     */   }
/*     */ 
/*     */   private void setSkippedSites()
/*     */   {
/*  85 */     this.skippedSites = new boolean[this.P.getCompressedNChar()];
/*     */ 
/*  87 */     for (int site = 0; site < this.skippedSites.length; site++) {
/*  88 */       this.skippedSites[site] = (this.P.getData(0, site).numOfStates() == 1 ? 1 : false);
/*  89 */       if (this.skippedSites[site] == 0) break;
/*  90 */       for (int taxa = 1; taxa < this.P.getNTax(); taxa++) {
/*  91 */         if (this.P.getData(0, site) != this.P.getData(taxa, site)) {
/*  92 */           this.skippedSites[site] = false;
/*  93 */           break;
/*     */         }
/*     */       }
/*     */     }
/*  97 */     int skipped = 0;
/*  98 */     for (int site = 0; site < this.skippedSites.length; site++) {
/*  99 */       if (this.skippedSites[site] != 0) skipped += this.P.getWeight(site);
/*     */     }
/* 101 */     this.totalSites = (this.P.getNChar() - skipped);
/*     */   }
/*     */ 
/*     */   private double[][] computePlot() {
/* 105 */     double[][] plot = new double[this.P.getCompressedNChar()][40];
/* 106 */     for (int site = 0; site < this.P.getCompressedNChar(); site++) {
/* 107 */       if (this.skippedSites[site] == 0) {
/* 108 */         double inf = this.min;
/* 109 */         for (int curInt = 0; curInt < 40; curInt++) {
/* 110 */           plot[site][curInt] = fallWithinInterval(inf, site);
/* 111 */           inf += this.interval;
/*     */         }
/*     */       }
/*     */     }
/* 115 */     return plot;
/*     */   }
/*     */ 
/*     */   private double fallWithinInterval(double inf, int site) {
/* 119 */     double nPair = 0.0D; double differences = 0.0D;
/* 120 */     for (int currentTaxa = 0; currentTaxa < this.P.getNTax(); currentTaxa++) {
/* 121 */       Data currentData = this.P.getData(currentTaxa, site);
/* 122 */       for (int taxa = currentTaxa + 1; taxa < this.P.getNTax(); taxa++) {
/* 123 */         if ((this.distance.get(currentTaxa, taxa) <= inf + this.interval) && (this.distance.get(currentTaxa, taxa) >= inf)) {
/* 124 */           Data d = this.P.getData(taxa, site);
/* 125 */           double contribution = 1.0D / (currentData.numOfStates() * d.numOfStates());
/* 126 */           for (int s = 0; s < d.getMaxStates(); s++) {
/* 127 */             for (int t = 0; t < currentData.getMaxStates(); t++) {
/* 128 */               if (s != t) differences += ((d.isState(s)) && (currentData.isState(t)) ? contribution : 0.0D);
/*     */             }
/*     */           }
/* 131 */           nPair += 1.0D;
/*     */         }
/*     */       }
/*     */     }
/* 135 */     if (nPair == 0.0D) {
/* 136 */       return 0.0D;
/*     */     }
/* 138 */     return differences / nPair;
/*     */   }
/*     */ 
/*     */   private double[] computeSortedVn(double[][] plot)
/*     */   {
/* 150 */     double acc1 = 0.0D; double acc2 = 0.0D; double acc3 = 0.0D;
/* 151 */     for (double n = this.min; n <= this.max; n += this.interval * 0.5D) {
/* 152 */       n += this.interval * 0.5D;
/* 153 */       acc3 += n;
/* 154 */       acc2 += n * Math.exp(-1.333333333333333D * n);
/*     */     }
/* 156 */     double[] temp_vn_list = new double[this.P.getCompressedNChar()];
/* 157 */     for (int site = 0; site < this.P.getCompressedNChar(); site++) {
/* 158 */       if (this.skippedSites[site] == 0) {
/* 159 */         acc1 = 0.0D;
/* 160 */         n = this.min + this.interval * 0.5D;
/* 161 */         for (int curInt = 0; curInt < 40; curInt++) {
/* 162 */           acc1 += plot[site][curInt] * n;
/* 163 */           n += this.interval;
/*     */         }
/* 165 */         temp_vn_list[site] = Math.abs(Math.log((acc3 + 1.333333333333333D * acc1) / acc2));
/*     */       } else {
/* 167 */         temp_vn_list[site] = -1.0D;
/*     */       }
/*     */     }
/* 170 */     return sortVn(temp_vn_list);
/*     */   }
/*     */ 
/*     */   private double[] sortVn(double[] list) {
/* 174 */     double[] sortedVn = new double[this.totalSites];
/* 175 */     int current = 0;
/*     */     int i;
/*     */     double min;
/* 177 */     for (; (min = getMin(list)) != -1.0D; 
/* 178 */       i < list.length)
/*     */     {
/*     */       double min;
/* 178 */       i = 0; continue;
/* 179 */       if (list[i] == min) {
/* 180 */         list[i] = -1.0D;
/* 181 */         for (int j = 0; j < this.P.getWeight(i); j++) {
/* 182 */           sortedVn[current] = min;
/* 183 */           current++;
/*     */         }
/*     */       }
/* 178 */       i++;
/*     */     }
/*     */ 
/* 188 */     return sortedVn;
/*     */   }
/*     */ 
/*     */   private double getMin(double[] list) {
/* 192 */     double min = 1.7976931348623157E+308D;
/* 193 */     for (double d : list) {
/* 194 */       if ((d != -1.0D) && (d < min)) min = d;
/*     */     }
/* 196 */     if (min == 1.7976931348623157E+308D) min = -1.0D;
/* 197 */     return min;
/*     */   }
/*     */ 
/*     */   private void partitionVn(double[] sortedVn)
/*     */   {
/* 207 */     double sum = 0.0D;
/* 208 */     int siteCount = 0;
/* 209 */     int cat = 0;
/* 210 */     double n = this.totalSites / this.nCat;
/* 211 */     for (int i = 0; i < sortedVn.length; i++) {
/* 212 */       sum += sortedVn[i];
/* 213 */       siteCount++;
/* 214 */       if (siteCount >= n) {
/* 215 */         siteCount = 1;
/* 216 */         this.rates[cat] = (sum / n);
/* 217 */         sum = 0.0D;
/* 218 */         cat++;
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.VanDePeer
 * JD-Core Version:    0.6.2
 */