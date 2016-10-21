/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ public class NoInclusionException extends Exception
/*    */ {
/*    */   public NoInclusionException(String big, String small)
/*    */   {
/* 12 */     super("Bipartition " + small + " is not included in " + big + ".");
/*    */   }
/*    */ 
/*    */   public NoInclusionException(String big, String small, Throwable cause) {
/* 16 */     super("Bipartition " + small + " is not included in " + big + ".", cause);
/*    */   }
/*    */ 
/*    */   public NoInclusionException(String big, String small, String message) {
/* 20 */     super("Bipartition " + small + " is not included in " + big + ". " + message);
/*    */   }
/*    */ 
/*    */   public NoInclusionException(String big, String small, String message, Throwable cause) {
/* 24 */     super("Bipartition " + small + " is not included in " + big + ". " + message, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.NoInclusionException
 * JD-Core Version:    0.6.2
 */