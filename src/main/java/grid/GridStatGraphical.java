/*    */ package grid;
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
/*    */ public class GridStatGraphical extends ScrollableFlowPanel
/*    */ {
/* 28 */   private final Map<GridMonitor.GridStatus, JLabel> labels = new HashMap();
/*    */   private final GridMonitor.GridStatus[] replicateStatus;
/*    */ 
/*    */   public GridStatGraphical(int nbrOfReplicates)
/*    */   {
/* 32 */     this.replicateStatus = new GridMonitor.GridStatus[nbrOfReplicates];
/* 33 */     for (int i = 0; i < nbrOfReplicates; i++) this.replicateStatus[i] = GridMonitor.GridStatus.QUEUED;
/* 34 */     setLayout(new FlowLayout(3, 5, 5));
/* 35 */     setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.blue, Color.black), "Number of replicates for each status"));
/* 36 */     for (GridMonitor.GridStatus status : GridMonitor.GridStatus.values()) {
/* 37 */       JPanel panel = new JPanel();
/* 38 */       panel.setLayout(new FlowLayout(3));
/* 39 */       panel.setToolTipText(status.getTooltipText());
/* 40 */       JButton coloredSquare = new JButton();
/* 41 */       coloredSquare.setPreferredSize(new Dimension(15, 15));
/* 42 */       coloredSquare.setBackground(status.getColor());
/* 43 */       coloredSquare.setBorder(BorderFactory.createRaisedBevelBorder());
/* 44 */       coloredSquare.setContentAreaFilled(false);
/* 45 */       coloredSquare.setOpaque(true);
/* 46 */       coloredSquare.setToolTipText(status.getTooltipText());
/* 47 */       JLabel statusLabel = new JLabel(status.toString() + " : ");
/* 48 */       JLabel nbrOfJobsLabel = new JLabel(status == GridMonitor.GridStatus.QUEUED ? nbrOfReplicates : "0");
/* 49 */       panel.add(coloredSquare);
/* 50 */       panel.add(statusLabel);
/* 51 */       panel.add(nbrOfJobsLabel);
/* 52 */       this.labels.put(status, nbrOfJobsLabel);
/* 53 */       add(panel);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void updateStatus(int replicateNbr, GridMonitor.GridStatus status) {
/* 58 */     this.replicateStatus[(replicateNbr - 1)] = status;
/* 59 */     for (GridMonitor.GridStatus s : this.labels.keySet()) {
/* 60 */       int count = 0;
/* 61 */       for (GridMonitor.GridStatus g : this.replicateStatus) {
/* 62 */         if (g == s) count++;
/*    */       }
/* 64 */       ((JLabel)this.labels.get(s)).setText(count);
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.GridStatGraphical
 * JD-Core Version:    0.6.2
 */