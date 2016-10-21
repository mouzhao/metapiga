/*    */ package monitors;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.FontMetrics;
/*    */ import java.awt.Graphics;
/*    */ import java.awt.geom.Rectangle2D;
/*    */ import javax.swing.JPanel;
/*    */ import metapiga.utilities.Tools;
/*    */ 
/*    */ public class GraphY extends JPanel
/*    */ {
/*    */   private final boolean negative;
/*    */   private final int yStep;
/*    */   private double minValue;
/*    */   private double scale;
/*    */ 
/*    */   public GraphY(boolean negative, int yStep)
/*    */   {
/* 24 */     this.negative = negative;
/* 25 */     this.yStep = yStep;
/* 26 */     this.minValue = 1.7976931348623157E+308D;
/* 27 */     this.scale = 1.0D;
/*    */   }
/*    */ 
/*    */   public void setValues(double minValue, double scale) {
/* 31 */     this.minValue = minValue;
/* 32 */     this.scale = scale;
/* 33 */     repaint();
/*    */   }
/*    */ 
/*    */   public void paintComponent(Graphics g) {
/* 37 */     g.setColor(new Color(0, 0, 0));
/* 38 */     Dimension dim = getSize();
/* 39 */     g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());
/* 40 */     g.setColor(Color.cyan);
/* 41 */     int H = getHeight();
/* 42 */     int xAxis = getWidth() - 5;
/* 43 */     int yAxis = H - 20;
/* 44 */     g.drawLine(xAxis, 0, xAxis, yAxis);
/* 45 */     for (int i = this.yStep; i < yAxis; i += this.yStep) {
/* 46 */       g.drawLine(xAxis - 1, i, xAxis + 1, i);
/* 47 */       double lik = this.negative ? this.scale * (i - H / 10) + this.minValue : this.scale * (H - H / 10 - i) + this.minValue;
/* 48 */       String s = Tools.doubletoString(lik, 0);
/* 49 */       int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth();
/* 50 */       int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight() / 2;
/* 51 */       g.drawString(s, xAxis - 2 - sl, i + sh);
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.GraphY
 * JD-Core Version:    0.6.2
 */