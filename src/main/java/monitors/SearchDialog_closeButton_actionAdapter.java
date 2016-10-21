/*     */ package monitors;
/*     */ 
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ 
/*     */ class SearchDialog_closeButton_actionAdapter
/*     */   implements ActionListener
/*     */ {
/*     */   SearchOnceGraphical adaptee;
/*     */ 
/*     */   SearchDialog_closeButton_actionAdapter(SearchOnceGraphical adaptee)
/*     */   {
/* 787 */     this.adaptee = adaptee;
/*     */   }
/*     */   public void actionPerformed(ActionEvent e) {
/* 790 */     this.adaptee.closeButton_actionPerformed(e);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.SearchDialog_closeButton_actionAdapter
 * JD-Core Version:    0.6.2
 */