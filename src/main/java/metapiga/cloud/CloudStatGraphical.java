/*    */ package metapiga.cloud;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.FlowLayout;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import javax.swing.BorderFactory;
/*    */ import javax.swing.JButton;
/*    */ import javax.swing.JLabel;
/*    */ import javax.swing.JPanel;
/*    */ import javax.swing.border.TitledBorder;
/*    */ import metapiga.ScrollableFlowPanel;
/*    */ 
/*    */ public class CloudStatGraphical extends ScrollableFlowPanel
/*    */ {
/* 20 */   private final Map<CloudMonitor.CloudStatus, JLabel> labels = new HashMap();
/*    */   private final CloudMonitor.CloudStatus[] replicateStatus;
/*    */ 
/*    */   public CloudStatGraphical(int numberOfReplicates)
/*    */   {
/* 23 */     this.replicateStatus = new CloudMonitor.CloudStatus[numberOfReplicates];
/* 24 */     for (int i = 0; i < numberOfReplicates; i++) {
/* 25 */       this.replicateStatus[i] = CloudMonitor.CloudStatus.PENDING;
/*    */     }
/* 27 */     setLayout(new FlowLayout(3, 5, 5));
/* 28 */     setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.blue, Color.black), "Number of replicates for each status"));
/* 29 */     for (CloudMonitor.CloudStatus status : CloudMonitor.CloudStatus.values()) {
/* 30 */       JPanel panel = new JPanel();
/* 31 */       panel.setLayout(new FlowLayout(3));
/* 32 */       panel.setToolTipText(status.getTooltipText());
/* 33 */       JButton coloredSquare = new JButton();
/* 34 */       coloredSquare.setSize(new Dimension(15, 15));
/* 35 */       coloredSquare.setBackground(status.getColor());
/* 36 */       coloredSquare.setBorder(BorderFactory.createRaisedBevelBorder());
/* 37 */       coloredSquare.setContentAreaFilled(false);
/* 38 */       coloredSquare.setOpaque(true);
/* 39 */       coloredSquare.setToolTipText(status.getTooltipText());
/* 40 */       JLabel statusLabel = new JLabel(status.toString() + " : ");
/* 41 */       JLabel numberOfJobsLabel = new JLabel(status == CloudMonitor.CloudStatus.PENDING ? numberOfReplicates : "0");
/* 42 */       panel.add(coloredSquare);
/* 43 */       panel.add(statusLabel);
/* 44 */       panel.add(numberOfJobsLabel);
/* 45 */       this.labels.put(status, numberOfJobsLabel);
/* 46 */       add(panel);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void updateStatus(int replicateNmbr, CloudMonitor.CloudStatus status) {
/* 51 */     this.replicateStatus[(replicateNmbr - 1)] = status;
/* 52 */     for (CloudMonitor.CloudStatus s : this.labels.keySet()) {
/* 53 */       int count = 0;
/* 54 */       for (CloudMonitor.CloudStatus g : this.replicateStatus) {
/* 55 */         if (g == s) count++;
/*    */       }
/* 57 */       ((JLabel)this.labels.get(s)).setText(count);
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.CloudStatGraphical
 * JD-Core Version:    0.6.2
 */