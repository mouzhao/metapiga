/*    */ package modelization.data.codons.tables;
/*    */ 
/*    */ import metapiga.modelization.data.Codon;
/*    */ 
/*    */ public abstract class CodonTransitionTable
/*    */ {
/*    */   public abstract boolean isSynonymous(Codon paramCodon1, Codon paramCodon2);
/*    */ 
/*    */   public abstract boolean isStopCodon(Codon paramCodon);
/*    */ 
/*    */   public boolean isTransition(Codon fromCodon, Codon toCodon)
/*    */   {
/* 26 */     if (isDifferentMoreThanOneNucleotide(fromCodon, toCodon)) {
/* 27 */       return false;
/*    */     }
/*    */ 
/* 30 */     char[] fnstr = fromCodon.toString().toCharArray();
/* 31 */     char[] tnstr = toCodon.toString().toCharArray();
/*    */ 
/* 33 */     int diffIdx = -1;
/* 34 */     for (int i = 0; i < tnstr.length; i++) {
/* 35 */       if (fnstr[i] != tnstr[i]) diffIdx = i;
/*    */     }
/*    */ 
/* 38 */     if (fromCodon == toCodon) return false;
/* 39 */     if ((fnstr[diffIdx] == 'C') && (tnstr[diffIdx] == 'T')) return true;
/* 40 */     if ((fnstr[diffIdx] == 'T') && (tnstr[diffIdx] == 'C')) return true;
/* 41 */     if ((fnstr[diffIdx] == 'A') && (tnstr[diffIdx] == 'G')) return true;
/* 42 */     if ((fnstr[diffIdx] == 'G') && (tnstr[diffIdx] == 'A')) return true;
/*    */ 
/* 44 */     return false;
/*    */   }
/*    */ 
/*    */   public boolean isTransversion(Codon fromCodon, Codon toCodon) {
/* 48 */     if (fromCodon == toCodon) return false;
/* 49 */     if (isDifferentMoreThanOneNucleotide(fromCodon, toCodon)) {
/* 50 */       return false;
/*    */     }
/*    */ 
/* 53 */     return !isTransition(fromCodon, toCodon);
/*    */   }
/*    */ 
/*    */   public boolean isDifferentMoreThanOneNucleotide(Codon fromCodon, Codon toCodon) {
/* 57 */     char[] fnstr = fromCodon.toString().toCharArray();
/* 58 */     char[] tnstr = toCodon.toString().toCharArray();
/*    */ 
/* 60 */     int diff = 0;
/* 61 */     if ((fromCodon == Codon.__X) || (toCodon == Codon.__X)) {
/* 62 */       return true;
/*    */     }
/* 64 */     for (int i = 0; i < tnstr.length; i++) {
/* 65 */       if (fnstr[i] != tnstr[i]) diff++;
/*    */     }
/*    */ 
/* 68 */     return diff > 1;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.codons.tables.CodonTransitionTable
 * JD-Core Version:    0.6.2
 */