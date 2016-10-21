package metapiga;/*    */
/*    */ 
/*    */ import java.awt.BorderLayout;
/*    */ import java.awt.Color;
/*    */
/*    */ import java.awt.Dimension;
/*    */ import java.awt.Font;
/*    */ import java.awt.Frame;
/*    */ import java.awt.Toolkit;
/*    */ import java.awt.event.ActionEvent;
/*    */ import java.awt.event.ActionListener;
/*    */ import java.io.BufferedReader;
/*    */ import java.io.InputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.net.URL;
/*    */ import javax.swing.JButton;
/*    */ import javax.swing.JDialog;
/*    */ import javax.swing.JPanel;
/*    */ import javax.swing.JScrollPane;
/*    */ import javax.swing.JTextPane;
/*    */

/*    */
/*    */ public class ChangelogDialog extends JDialog
/*    */ {
/* 29 */   JPanel mainPanel = new JPanel();
/* 30 */   BorderLayout borderLayout1 = new BorderLayout();
/* 31 */   JScrollPane notesScrollPane = new JScrollPane();
/* 32 */   JTextPane notesTextArea = new JTextPane();
/* 33 */   JPanel southPanel = new JPanel();
/* 34 */   JButton closeButton = new JButton();
/*    */ 
/*    */   public ChangelogDialog(Frame frame) {
/* 37 */     super(frame, "MetaPIGA changelog", true);
/*    */     try {
/* 39 */       jbInit();
/* 40 */       URL url = getClass().getResource("metapiga/resources/changelog.txt");
/* 41 */       InputStream in = url.openStream();
/* 42 */       BufferedReader dis = new BufferedReader(new InputStreamReader(in));
/*    */ 
/* 44 */       StringBuffer buf = new StringBuffer();
/*    */       String line;
/* 45 */       while ((line = dis.readLine()) != null)
/*    */       {
/*    */         String line;
/* 46 */         buf.append(line + "\n");
/*    */       }
/* 48 */       dis.close();
/* 49 */       in.close();
/* 50 */       this.notesTextArea.setText(buf.toString());
/* 51 */       this.notesTextArea.setCaretPosition(0);
/* 52 */       pack();
/* 53 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 54 */       if (getWidth() > screenSize.width / 3 * 2) setSize(screenSize.width / 3 * 2, getHeight());
/* 55 */       if (getHeight() > screenSize.height / 3 * 2) setSize(getWidth(), screenSize.height / 3 * 2); 
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
/* 58 */       ex.printStackTrace();
/*    */     }
/*    */   }
/*    */ 
/*    */   private void jbInit() throws Exception {
/* 63 */     this.mainPanel.setLayout(this.borderLayout1);
/* 64 */     this.notesScrollPane.setHorizontalScrollBarPolicy(31);
/* 65 */     this.notesScrollPane.getViewport().add(this.notesTextArea, null);
/* 66 */     this.notesTextArea.setEditable(false);
/* 67 */     this.notesTextArea.setBackground(Color.BLACK);
/* 68 */     this.notesTextArea.setForeground(Color.GREEN);
/* 69 */     this.notesTextArea.setFont(new Font("Geneva", 0, 12));
/* 70 */     this.closeButton.setText("CLOSE");
/* 71 */     this.closeButton.addActionListener(new ActionListener() {
/*    */       public void actionPerformed(ActionEvent e) {
/* 73 */         ChangelogDialog.this.dispose();
/*    */       }
/*    */     });
/* 76 */     getContentPane().add(this.mainPanel);
/* 77 */     this.mainPanel.add(this.notesScrollPane, "Center");
/* 78 */     this.mainPanel.add(this.southPanel, "South");
/* 79 */     this.southPanel.add(this.closeButton, null);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.ChangelogDialog
 * JD-Core Version:    0.6.2
 */