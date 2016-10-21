/*    */ package grid;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.util.concurrent.CountDownLatch;
/*    */ import metapiga.trees.Tree;
/*    */ 
/*    */ public abstract interface GridMonitor extends Runnable
/*    */ {
/*    */   public static final int gridReplication = 5;
/*    */ 
/*    */   public abstract void initialize(GridConnection paramGridConnection, CountDownLatch paramCountDownLatch);
/*    */ 
/*    */   public abstract JobStatus getMonitorStatus();
/*    */ 
/*    */   public abstract void stop();
/*    */ 
/*    */   public abstract GridMonitorType getGridMonitorType();
/*    */ 
/*    */   public abstract GridStatus getGridStatus();
/*    */ 
/*    */   public abstract boolean isTerminated();
/*    */ 
/*    */   public abstract void showStatus(GridStatus paramGridStatus);
/*    */ 
/*    */   public abstract void updateConsensusTree(Tree paramTree);
/*    */ 
/*    */   public static enum GridMonitorType
/*    */   {
/* 21 */     SINGLE_SEARCH_GRAPHICAL, BATCH_SEARCH_GRAPHICAL, CONSOLE, SILENT;
/*    */   }
/*    */   public static enum GridStatus {
/* 24 */     QUEUED(Color.WHITE, "This replicate is waiting in the queue. The replicate at the top of the queue will be sent to the grid as soon as a worker becomes available."), 
/* 25 */     WAITING(Color.GRAY, "This replicate has been sent to the grid and is waiting for an available worker."), 
/* 26 */     READY(Color.BLUE, "This replicate has been assigned to a worker and will start after worker initialization."), 
/* 27 */     PROCESSING(Color.YELLOW, "This replicate is processing on the grid."), 
/* 28 */     COMPLETE(Color.GREEN, "This replicate has completed successfully."), 
/* 29 */     KILLED(Color.BLACK, "This replicate has been killed and has not ben completed successfully."), 
/* 30 */     ERROR(Color.RED, "This replicate is experiencing a temporary or fatal error or a connection problem.");
/*    */ 
/*    */     private final Color color;
/*    */     private final String tooltip;
/*    */ 
/* 34 */     private GridStatus(Color color, String tooltip) { this.color = color;
/* 35 */       this.tooltip = tooltip; } 
/*    */     public Color getColor() {
/* 37 */       return this.color; } 
/* 38 */     public String getTooltipText() { return this.tooltip; }
/*    */ 
/*    */   }
/*    */ 
/*    */   public static enum JobStatus
/*    */   {
/* 22 */     NOT_INITIALIZED, POOLED, RUNNING, TERMINATED, CANCELLED;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.GridMonitor
 * JD-Core Version:    0.6.2
 */