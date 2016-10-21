/*    */ package metapiga.modelization.data;
/*    */ 
/*    */ import metapiga.modelization.data.codons.tables.UniversalCodonTransitionTable;
/*    */ import org.junit.Assert;
/*    */ import org.junit.Test;
/*    */ 
/*    */ public class UniversalCodonTransitionTableTest
/*    */ {
/* 10 */   private UniversalCodonTransitionTable codonTable = new UniversalCodonTransitionTable();
/*    */ 
/*    */   @Test
/*    */   public void testIsStopCodonTest() {
/* 14 */     Assert.assertTrue((this.codonTable.isStopCodon(Codon.TGA)) || 
/* 15 */       (this.codonTable.isStopCodon(Codon.TAA)) || 
/* 16 */       (this.codonTable.isStopCodon(Codon.TAG)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void testIsNotStopCodonTest() {
/* 21 */     Assert.assertFalse((this.codonTable.isStopCodon(Codon.AAA)) || 
/* 22 */       (this.codonTable.isStopCodon(Codon.GAT)) || 
/* 23 */       (this.codonTable.isStopCodon(Codon.__X)) || 
/* 24 */       (this.codonTable.isStopCodon(Codon.AGA)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void testIsSynonimousTest() {
/* 29 */     Assert.assertTrue((this.codonTable.isSynonymous(Codon.CTC, Codon.CTG)) || 
/* 30 */       (this.codonTable.isSynonymous(Codon.AGT, Codon.AGC)) || 
/* 31 */       (this.codonTable.isSynonymous(Codon.GGT, Codon.GGG)) || 
/* 32 */       (this.codonTable.isSynonymous(Codon.TTG, Codon.CTT)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void testIsNotSynonimousTest() {
/* 37 */     Assert.assertFalse((this.codonTable.isSynonymous(Codon.TTC, Codon.GGT)) || 
/* 38 */       (this.codonTable.isSynonymous(Codon.CCG, Codon.ACT)) || 
/* 39 */       (this.codonTable.isSynonymous(Codon.TAA, Codon.TAG)) || 
/* 40 */       (this.codonTable.isSynonymous(Codon.__X, Codon.CGC)) || 
/* 41 */       (this.codonTable.isSynonymous(Codon.ATA, Codon.ATG)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isDifferentMoreThanOneNucleotideTest() {
/* 46 */     Assert.assertTrue((this.codonTable.isDifferentMoreThanOneNucleotide(Codon.AAA, Codon.TAC)) || 
/* 47 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.CAG, Codon.AGC)) || 
/* 48 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.TGA, Codon.ATG)) || 
/* 49 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.__X, Codon.TAC)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isNotDifferentMoreThanOneNucleotideTest() {
/* 54 */     Assert.assertFalse((this.codonTable.isDifferentMoreThanOneNucleotide(Codon.AAA, Codon.TAA)) || 
/* 55 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.TAA, Codon.TCA)) || 
/* 56 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.TCA, Codon.TCC)) || 
/* 57 */       (this.codonTable.isDifferentMoreThanOneNucleotide(Codon.TCC, Codon.ACC)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isTransitionTest() {
/* 62 */     Assert.assertTrue((this.codonTable.isTransition(Codon.AAA, Codon.GAA)) || 
/* 63 */       (this.codonTable.isTransition(Codon.GAA, Codon.GGA)) || 
/* 64 */       (this.codonTable.isTransition(Codon.GGA, Codon.AGA)) || 
/* 65 */       (this.codonTable.isTransition(Codon.ACC, Codon.ACT)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isNotTransitionTest()
/*    */   {
/* 71 */     Assert.assertFalse((this.codonTable.isTransition(Codon.AAA, Codon.CAA)) || 
/* 72 */       (this.codonTable.isTransition(Codon.GTA, Codon.GGA)) || 
/* 73 */       (this.codonTable.isTransition(Codon.GGA, Codon.CGA)) || 
/* 74 */       (this.codonTable.isTransition(Codon.ACG, Codon.ACT)) || 
/* 75 */       (this.codonTable.isTransition(Codon.ACG, Codon.__X)) || 
/* 76 */       (this.codonTable.isTransition(Codon.ACG, Codon.AAA)) || 
/* 77 */       (this.codonTable.isTransition(Codon.AAA, Codon.AAA)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isTransversionTest()
/*    */   {
/* 83 */     Assert.assertTrue((this.codonTable.isTransversion(Codon.AAA, Codon.CAA)) || 
/* 84 */       (this.codonTable.isTransversion(Codon.GAA, Codon.GTA)) || 
/* 85 */       (this.codonTable.isTransversion(Codon.GGA, Codon.CGA)) || 
/* 86 */       (this.codonTable.isTransversion(Codon.ACC, Codon.ACA)));
/*    */   }
/*    */ 
/*    */   @Test
/*    */   public void isNotTransversionTest()
/*    */   {
/* 92 */     Assert.assertFalse((this.codonTable.isTransversion(Codon.AAA, Codon.GAA)) || 
/* 93 */       (this.codonTable.isTransversion(Codon.GTA, Codon.GCA)) || 
/* 94 */       (this.codonTable.isTransversion(Codon.GGA, Codon.AGA)) || 
/* 95 */       (this.codonTable.isTransversion(Codon.ACG, Codon.ACA)) || 
/* 96 */       (this.codonTable.isTransversion(Codon.ACG, Codon.__X)) || 
/* 97 */       (this.codonTable.isTransition(Codon.ACG, Codon.AAA)) || 
/* 98 */       (this.codonTable.isTransition(Codon.AAA, Codon.AAA)));
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.UniversalCodonTransitionTableTest
 * JD-Core Version:    0.6.2
 */