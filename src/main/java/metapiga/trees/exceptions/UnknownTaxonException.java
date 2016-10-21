/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ public class UnknownTaxonException extends Exception
/*    */ {
/*    */   public UnknownTaxonException(String taxa)
/*    */   {
/* 12 */     super("Unknown taxa : " + taxa);
/*    */   }
/*    */ 
/*    */   public UnknownTaxonException(String taxa, Throwable cause) {
/* 16 */     super("Unknown taxa : " + taxa, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.UnknownTaxonException
 * JD-Core Version:    0.6.2
 */