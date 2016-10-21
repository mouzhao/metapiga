/*    */ package grid;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import metapiga.ProgressHandling;
/*    */ 
/*    */ public class GridStatConsole
/*    */ {
/*    */   private final GridMonitor.GridStatus[] replicateStatus;
/* 17 */   private String[] animations = { "-", "\\", "|", "/" };
/* 18 */   private int animationIndex = 0;
/*    */ 
/*    */   public GridStatConsole(int nbrOfReplicates) {
/* 21 */     this.replicateStatus = new GridMonitor.GridStatus[nbrOfReplicates];
/* 22 */     for (int i = 0; i < nbrOfReplicates; i++) this.replicateStatus[i] = GridMonitor.GridStatus.QUEUED;
/* 23 */     StringBuilder text = new StringBuilder(ProgressHandling.consoleWidth);
/* 24 */     text.append(this.animations[this.animationIndex]);
/* 25 */     for (GridMonitor.GridStatus status : GridMonitor.GridStatus.values()) {
/* 26 */       text.append("   " + status + " : " + (status == GridMonitor.GridStatus.QUEUED ? nbrOfReplicates : "0"));
/*    */     }
/* 28 */     System.out.println(text.toString());
/*    */   }
/*    */ 
/*    */   public void updateStatus(int replicateNbr, GridMonitor.GridStatus status) {
/* 32 */     this.replicateStatus[(replicateNbr - 1)] = status;
/* 33 */     StringBuilder text = new StringBuilder(ProgressHandling.consoleWidth);
/* 34 */     if (this.animationIndex == this.animations.length - 1) this.animationIndex = 0; else
/* 35 */       this.animationIndex += 1;
/* 36 */     text.append(this.animations[this.animationIndex]);
/* 37 */     for (GridMonitor.GridStatus s : GridMonitor.GridStatus.values()) {
/* 38 */       int count = 0;
/* 39 */       for (GridMonitor.GridStatus g : this.replicateStatus) {
/* 40 */         if (g == s) count++;
/*    */       }
/* 42 */       text.append(" [" + count + "]" + s);
/*    */     }
/* 44 */     System.out.print("\r" + text.toString());
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.GridStatConsole
 * JD-Core Version:    0.6.2
 */