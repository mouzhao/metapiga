package metapiga;/*    */
/*    */ 
/*    */ import java.awt.Component;
/*    */ import java.awt.Container;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.FlowLayout;
/*    */ import java.awt.Rectangle;
/*    */ import javax.swing.JPanel;
/*    */ import javax.swing.Scrollable;
/*    */ 
/*    */ public class ScrollableFlowPanel extends JPanel
/*    */   implements Scrollable
/*    */ {
/*    */   public void setBounds(int x, int y, int width, int height)
/*    */   {
/* 21 */     super.setBounds(x, y, getParent().getWidth(), height);
/*    */   }
/*    */ 
/*    */   public Dimension getPreferredSize() {
/* 25 */     return new Dimension(getWidth(), getPreferredHeight());
/*    */   }
/*    */ 
/*    */   public Dimension getPreferredScrollableViewportSize() {
/* 29 */     return super.getPreferredSize();
/*    */   }
/*    */ 
/*    */   public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
/* 33 */     int hundredth = (orientation == 1 ? 
/* 34 */       getParent().getHeight() : getParent().getWidth()) / 100;
/* 35 */     return hundredth == 0 ? 1 : hundredth;
/*    */   }
/*    */ 
/*    */   public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
/* 39 */     return orientation == 1 ? getParent().getHeight() : getParent().getWidth();
/*    */   }
/*    */ 
/*    */   public boolean getScrollableTracksViewportWidth() {
/* 43 */     return true;
/*    */   }
/*    */ 
/*    */   public boolean getScrollableTracksViewportHeight() {
/* 47 */     return false;
/*    */   }
/*    */ 
/*    */   private int getPreferredHeight() {
/* 51 */     int rv = 0;
/* 52 */     int k = 0; for (int count = getComponentCount(); k < count; k++) {
/* 53 */       Component comp = getComponent(k);
/* 54 */       Rectangle r = comp.getBounds();
/* 55 */       int height = r.y + r.height;
/* 56 */       if (height > rv)
/* 57 */         rv = height;
/*    */     }
/* 59 */     rv += ((FlowLayout)getLayout()).getVgap();
/* 60 */     return rv;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.ScrollableFlowPanel
 * JD-Core Version:    0.6.2
 */