/*    */ package modelization.data.codons.tables;
/*    */ 
/*    */ import metapiga.parameters.Parameters.CodonTransitionTableType;
/*    */ 
/*    */ public class CodonTransitionTableFactory
/*    */ {
/*    */   public static CodonTransitionTable getInstance(Parameters.CodonTransitionTableType type)
/*    */   {
/* 11 */     switch (type) {
/*    */     case CDHNNUC:
/* 13 */       return new UniversalCodonTransitionTable();
/*    */     case EFMITOCH:
/* 15 */       return new CDHNuclearCode();
/*    */     case EUPLOTIDNUC:
/* 17 */       return new EchinodermFlatwormMitochCode();
/*    */     case INVERTMITOCH:
/* 19 */       return new EuploidNuclearCode();
/*    */     case MPCMMITOCH:
/* 21 */       return new InvertebrateMitochondrialCode();
/*    */     case UNIVERSAL:
/* 23 */       return new VertebrateMitochondrialCode();
/*    */     case NONE:
/* 25 */       return new MoldProtoCoelMitochCode();
/*    */     }
/* 27 */     if (!$assertionsDisabled) throw new AssertionError("Unknown codon table");
/* 28 */     return new UniversalCodonTransitionTable();
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.codons.tables.CodonTransitionTableFactory
 * JD-Core Version:    0.6.2
 */