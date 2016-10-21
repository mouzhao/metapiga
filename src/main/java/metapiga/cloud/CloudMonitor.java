/*    */ package metapiga.cloud;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.util.concurrent.CountDownLatch;
/*    */ import metapiga.trees.Tree;
/*    */ 
/*    */ public abstract interface CloudMonitor extends Runnable
/*    */ {
/*    */   public static final int cloudReplication = 5;
/*    */ 
/*    */   public abstract void initialize(CloudConnection paramCloudConnection, CountDownLatch paramCountDownLatch);
/*    */ 
/*    */   public abstract JobStatus getMonitorStatus();
/*    */ 
/*    */   public abstract void stop();
/*    */ 
/*    */   public abstract CloudMonitorType getCloudMonitorType();
/*    */ 
/*    */   public abstract CloudStatus getCloudStatus();
/*    */ 
/*    */   public abstract boolean isTerminated();
/*    */ 
/*    */   public abstract void showStatus(CloudStatus paramCloudStatus);
/*    */ 
/*    */   public abstract void updateConsensusTree(Tree paramTree);
/*    */ 
/*    */   public static enum CloudMonitorType
/*    */   {
/* 17 */     SINGLE_SEARCH_GRAPHICAL, BATCH_SEARCH_GRAPHICAL, CONSOLE, SILENT;
/*    */   }
/* 19 */   public static enum CloudStatus { PENDING(
/* 20 */       Color.WHITE, "This replicate is waiting in the queue. The replicate at the top of the queue will be sent to the cloud as soon as a possible."), 
/* 21 */     SUBMITTED(Color.BLUE, "This replicate has been assigned to a worker and will start after worker initialization."), 
/* 22 */     RUNNING(Color.YELLOW, "This replicate is processing on the grid."), 
/* 23 */     FINISHED(Color.GREEN, "This replicate has completed successfully."), 
/* 24 */     CANCELLED(Color.BLACK, "This replicate has been killed and has not ben completed successfully."), 
/* 25 */     CANCELREQUESTED(Color.DARK_GRAY, "There is a request to kill this replicate"), 
/* 26 */     CHECKINGINPUTDATA(Color.CYAN, "Checking input data"), 
/* 27 */     UNKNOWN(Color.WHITE, "Unknown status of the replicate"), 
/* 28 */     FAILED(Color.RED, "This replicate is experiencing a temporary or fatal error or a connection problem.");
/*    */ 
/*    */     private final Color color;
/*    */     private final String tooltip;
/*    */ 
/* 32 */     private CloudStatus(Color color, String tooltip) { this.color = color;
/* 33 */       this.tooltip = tooltip; } 
/*    */     public Color getColor() {
/* 35 */       return this.color; } 
/* 36 */     public String getTooltipText() { return this.tooltip; }
/*    */ 
/*    */   }
/*    */ 
/*    */   public static enum JobStatus
/*    */   {
/* 18 */     NOT_INITIALIZED, POOLED, RUNNING, TERMINATED, CANCELLED;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.CloudMonitor
 * JD-Core Version:    0.6.2
 */