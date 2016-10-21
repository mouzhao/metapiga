/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ public class UnrootableTreeException extends Exception
/*    */ {
/*    */   public UnrootableTreeException()
/*    */   {
/* 12 */     super("No outgroup and no access node, cannot root the tree.");
/*    */   }
/*    */ 
/*    */   public UnrootableTreeException(Throwable cause) {
/* 16 */     super("No outgroup and no access node, cannot root the tree.", cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.UnrootableTreeException
 * JD-Core Version:    0.6.2
 */