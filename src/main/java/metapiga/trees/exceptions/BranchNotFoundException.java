/*    */ package metapiga.trees.exceptions;
/*    */ 
/*    */ import metapiga.trees.Branch;
/*    */ import metapiga.trees.Node;
/*    */ import metapiga.trees.Tree;
/*    */ 
/*    */ public class BranchNotFoundException extends Exception
/*    */ {
/*    */   public BranchNotFoundException(Node node, Node otherNode)
/*    */   {
/* 16 */     super("Node " + node + " and " + otherNode + " are not on the same branch.");
/*    */   }
/*    */ 
/*    */   public BranchNotFoundException(Node node, Node otherNode, Throwable cause) {
/* 20 */     super("Node " + node + " and " + otherNode + " are not on the same branch.", cause);
/*    */   }
/*    */ 
/*    */   public BranchNotFoundException(Branch branch, Tree T, Tree T2) {
/* 24 */     super("Branch " + branch.toString() + " of " + T + " was not found in " + T2);
/*    */   }
/*    */ 
/*    */   public BranchNotFoundException(Branch branch, Tree T, Tree T2, Throwable cause) {
/* 28 */     super("Branch " + branch.toString() + " of " + T + " was not found in " + T2, cause);
/*    */   }
/*    */ 
/*    */   public BranchNotFoundException(String message, Branch branch, Tree T, Tree T2) {
/* 32 */     super(message + " - Branch " + branch.toString() + " of " + T + " was not found in " + T2);
/*    */   }
/*    */ 
/*    */   public BranchNotFoundException(String message, Branch branch, Tree T, Tree T2, Throwable cause) {
/* 36 */     super(message + " - Branch " + branch.toString() + " of " + T + " was not found in " + T2, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.exceptions.BranchNotFoundException
 * JD-Core Version:    0.6.2
 */