/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ public class UncompatibleOutgroupException extends Exception
/*    */ {
/*    */   public UncompatibleOutgroupException(String treeName)
/*    */   {
/* 12 */     super("Defined outgroup is not compatible with topology of tree " + treeName);
/*    */   }
/*    */ 
/*    */   public UncompatibleOutgroupException(String treeName, Throwable cause) {
/* 16 */     super("Defined outgroup is not compatible with topology of tree " + treeName);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.UncompatibleOutgroupException
 * JD-Core Version:    0.6.2
 */