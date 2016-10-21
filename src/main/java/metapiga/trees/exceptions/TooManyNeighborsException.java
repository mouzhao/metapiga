/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ import metapiga.trees.Node;
/*    */ 
/*    */ public class TooManyNeighborsException extends Exception
/*    */ {
/*    */   public TooManyNeighborsException(Node node)
/*    */   {
/* 14 */     super("Cannot add a new neighbor, node (" + node + ") has already 3 neighbors.");
/*    */   }
/*    */ 
/*    */   public TooManyNeighborsException(Node node, Throwable cause) {
/* 18 */     super("Cannot add a new neighbor, node (" + node + ") has already 3 neighbors.", cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.TooManyNeighborsException
 * JD-Core Version:    0.6.2
 */