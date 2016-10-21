/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ import metapiga.trees.Node;
/*    */ 
/*    */ public class NullAncestorException extends Exception
/*    */ {
/*    */   public NullAncestorException(Node node)
/*    */   {
/* 14 */     super("Node " + node + " is the root or tree is unrooted, and ancestors are not defined");
/*    */   }
/*    */ 
/*    */   public NullAncestorException(Node node, Throwable cause) {
/* 18 */     super("Node " + node + " is the root or tree is unrooted, and ancestors are not defined", cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.NullAncestorException
 * JD-Core Version:    0.6.2
 */