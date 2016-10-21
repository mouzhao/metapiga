/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ import metapiga.trees.Node;
/*    */ 
/*    */ public class UnknownNeighborException extends Exception
/*    */ {
/*    */   public UnknownNeighborException(Node node, Node neighbor)
/*    */   {
/* 14 */     super("Node " + neighbor + " is not a neighbor of node " + node);
/*    */   }
/*    */ 
/*    */   public UnknownNeighborException(Node node, Node neighbor, Throwable cause) {
/* 18 */     super("Node " + neighbor + " is not a neighbor of node " + node, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.UnknownNeighborException
 * JD-Core Version:    0.6.2
 */