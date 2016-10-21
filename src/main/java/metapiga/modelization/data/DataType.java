/*     */ package metapiga.modelization.data;
/*     */ 
/*     */ import java.util.BitSet;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ 
/*     */ public enum DataType
/*     */ {
/*  16 */   DNA(4, "Nucleotide"), 
/*  17 */   PROTEIN(20, "Amino acid"), 
/*  18 */   STANDARD(2, "Standard binary data"), 
/*  19 */   CODON(64, "Nucleotide codons");
/*     */ 
/*     */   private final int numOfStates;
/*     */   private final String verbose;
/*     */ 
/*  25 */   private DataType(int numOfStates, String verbose) { this.numOfStates = numOfStates;
/*  26 */     this.verbose = verbose;
/*     */   }
/*     */ 
/*     */   public final int numOfStates()
/*     */   {
/*  34 */     return this.numOfStates;
/*     */   }
/*     */ 
/*     */   public final String verbose()
/*     */   {
/*  40 */     return this.verbose;
/*     */   }
/*     */ 
/*     */   public final Data getDataWithState(int state)
/*     */     throws UnknownDataException
/*     */   {
/*  50 */     switch (this) {
/*     */     case CODON:
/*  52 */       return DNA.getDNAWithState(state);
/*     */     case STANDARD:
/*  54 */       return Codon.getCodonWithState(state);
/*     */     case DNA:
/*  56 */       return Protein.getProteinWithState(state);
/*     */     case PROTEIN:
/*  58 */       return Standard.getStandardWithState(state);
/*     */     }
/*  60 */     throw new UnknownDataException(toString());
/*     */   }
/*     */ 
/*     */   public final Data getData(String data)
/*     */     throws UnknownDataException
/*     */   {
/*  71 */     switch (this) {
/*     */     case CODON:
/*  73 */       return DNA.valueOf(data);
/*     */     case STANDARD:
/*  75 */       return Codon.valueOf(data);
/*     */     case DNA:
/*  77 */       return Protein.valueOf(data);
/*     */     case PROTEIN:
/*     */       try {
/*  80 */         return Standard.getStandardWithState(Integer.parseInt(data));
/*     */       } catch (NumberFormatException e) {
/*  82 */         return Standard.X;
/*     */       }
/*     */     }
/*  85 */     throw new UnknownDataException(toString());
/*     */   }
/*     */ 
/*     */   public final Data getData(BitSet bitSet)
/*     */     throws UnknownDataException
/*     */   {
/*  97 */     switch (this) {
/*     */     case CODON:
/*  99 */       return DNA.getDNA(bitSet);
/*     */     case DNA:
/* 101 */       return Protein.getProtein(bitSet);
/*     */     case PROTEIN:
/* 103 */       return Standard.getStandard(bitSet);
/*     */     case STANDARD:
/* 105 */       return Codon.getCodon(bitSet);
/*     */     }
/* 107 */     throw new UnknownDataException(toString());
/*     */   }
/*     */ 
/*     */   public final int getStateOf(String data)
/*     */     throws UnknownDataException
/*     */   {
/* 119 */     switch (this) {
/*     */     case CODON:
/* 121 */       return DNA.getStateOf(data);
/*     */     case DNA:
/* 123 */       return Protein.getStateOf(data);
/*     */     case PROTEIN:
/* 125 */       return Standard.getStateOf(data);
/*     */     case STANDARD:
/* 127 */       return Codon.getStateOf(data);
/*     */     }
/* 129 */     throw new UnknownDataException(toString());
/*     */   }
/*     */ 
/*     */   public final Data getUndeterminateData()
/*     */     throws UnknownDataException
/*     */   {
/* 140 */     switch (this) {
/*     */     case CODON:
/* 142 */       return DNA.N;
/*     */     case STANDARD:
/* 144 */       return Codon.__X;
/*     */     case DNA:
/* 146 */       return Protein.X;
/*     */     case PROTEIN:
/* 148 */       return Standard.X;
/*     */     }
/* 150 */     throw new UnknownDataException(toString());
/*     */   }
/*     */ 
/*     */   public final Data getMostProbableData(double[] probabilities)
/*     */     throws Exception
/*     */   {
/* 161 */     if (probabilities.length != this.numOfStates) throw new Exception("Array probabilities must have " + this.numOfStates + " components, it has only " + probabilities.length);
/* 162 */     BitSet b = new BitSet(this.numOfStates);
/* 163 */     double best = probabilities[0];
/* 164 */     b.set(0);
/* 165 */     for (int state = 1; state < this.numOfStates; state++) {
/* 166 */       if (probabilities[state] > best) {
/* 167 */         b.clear();
/* 168 */         best = probabilities[state];
/* 169 */         b.set(state, true);
/* 170 */       } else if (probabilities[state] == best) {
/* 171 */         b.set(state, true);
/*     */       }
/*     */     }
/* 174 */     if (best == 0.0D) return null; try
/*     */     {
/* 176 */       return getData(b); } catch (UnknownDataException e) {
/*     */     }
/* 178 */     return getUndeterminateData();
/*     */   }
/*     */ 
/*     */   public final int getRenderingSize()
/*     */   {
/* 188 */     switch (this) {
/*     */     case CODON:
/* 190 */       return 1;
/*     */     case PROTEIN:
/* 193 */       return 1;
/*     */     case DNA:
/* 196 */       return 1;
/*     */     case STANDARD:
/* 199 */       return 3;
/*     */     }
/*     */ 
/* 202 */     return 1;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.DataType
 * JD-Core Version:    0.6.2
 */