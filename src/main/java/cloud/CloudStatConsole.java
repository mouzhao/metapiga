/*    */ package cloud;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import metapiga.ProgressHandling;
/*    */ 
/*    */ public class CloudStatConsole
/*    */ {
/*    */   private final CloudMonitor.CloudStatus[] replicateStatus;
/*  9 */   private String[] animations = { "-", "\\", "|", "/" };
/* 10 */   private int animationIndex = 0;
/*    */ 
/*    */   public CloudStatConsole(int numberOfReplicates)
/*    */   {
/* 14 */     this.replicateStatus = new CloudMonitor.CloudStatus[numberOfReplicates];
/* 15 */     for (int i = 0; i < numberOfReplicates; i++) {
/* 16 */       this.replicateStatus[i] = CloudMonitor.CloudStatus.PENDING;
/*    */     }
/* 18 */     StringBuilder text = new StringBuilder(ProgressHandling.consoleWidth);
/* 19 */     text.append(this.animations[this.animationIndex]);
/* 20 */     for (CloudMonitor.CloudStatus status : CloudMonitor.CloudStatus.values()) {
/* 21 */       text.append("   " + status + " : " + (status == CloudMonitor.CloudStatus.PENDING ? numberOfReplicates : "0"));
/*    */     }
/* 23 */     System.out.println(text.toString());
/*    */   }
/*    */ 
/*    */   public void updateStatus(int replicateNum, CloudMonitor.CloudStatus status) {
/* 27 */     this.replicateStatus[(replicateNum - 1)] = status;
/* 28 */     StringBuilder text = new StringBuilder(ProgressHandling.consoleWidth);
/* 29 */     if (this.animationIndex == this.animations.length - 1)
/* 30 */       this.animationIndex = 0;
/*    */     else {
/* 32 */       this.animationIndex += 1;
/*    */     }
/* 34 */     text.append(this.animations[this.animationIndex]);
/* 35 */     for (CloudMonitor.CloudStatus s : CloudMonitor.CloudStatus.values()) {
/* 36 */       int count = 0;
/* 37 */       for (CloudMonitor.CloudStatus g : this.replicateStatus) {
/* 38 */         if (g == s) count++;
/*    */       }
/* 40 */       text.append(" [" + count + "]" + s);
/*    */     }
/* 42 */     System.out.print("\r" + text.toString());
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.cloud.CloudStatConsole
 * JD-Core Version:    0.6.2
 */