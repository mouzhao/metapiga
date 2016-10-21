package metapiga;/*     */
/*     */ 
/*     */ import com.install4j.api.Util;
/*     */ import com.install4j.api.windows.SpecialFolder;
/*     */ import com.install4j.api.windows.WinFileSystem;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.Frame;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextPane;
/*     */

/*     */
/*     */ public class UpdateDialog extends JDialog
/*     */ {
/*  35 */   JPanel mainPanel = new JPanel();
/*  36 */   BorderLayout borderLayout1 = new BorderLayout();
/*  37 */   JScrollPane notesScrollPane = new JScrollPane();
/*  38 */   JTextPane notesTextArea = new JTextPane();
/*  39 */   JPanel southPanel = new JPanel();
/*  40 */   JButton closeButton = new JButton();
/*     */ 
/*     */   public UpdateDialog(Frame frame) {
/*  43 */     super(frame, "MetaPIGA 3.1", true);
/*     */     try {
/*  45 */       jbInit();
/*  46 */       URL url = getClass().getResource("metapiga/resources/update.txt");
/*  47 */       InputStream in = url.openStream();
/*  48 */       BufferedReader dis = new BufferedReader(new InputStreamReader(in));
/*     */ 
/*  50 */       StringBuffer buf = new StringBuffer();
/*     */       String line;
/*  51 */       while ((line = dis.readLine()) != null)
/*     */       {
/*     */         String line;
/*  52 */         buf.append(line + "\n");
/*     */       }
/*  54 */       dis.close();
/*  55 */       in.close();
/*  56 */       this.notesTextArea.setText(buf.toString());
/*  57 */       this.notesTextArea.setCaretPosition(0);
/*  58 */       pack();
/*  59 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  60 */       if (getWidth() > screenSize.width / 3 * 2) setSize(screenSize.width / 3 * 2, getHeight());
/*  61 */       if (getHeight() > screenSize.height / 4 * 3) setSize(getWidth(), screenSize.height / 4 * 3); 
/*     */     }
/*     */     catch (Exception ex)
/*     */     {
/*  64 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void jbInit() throws Exception {
/*  69 */     this.mainPanel.setLayout(this.borderLayout1);
/*  70 */     this.notesScrollPane.setHorizontalScrollBarPolicy(31);
/*  71 */     this.notesScrollPane.getViewport().add(this.notesTextArea, null);
/*  72 */     this.notesTextArea.setEditable(false);
/*  73 */     this.notesTextArea.setBackground(Color.BLACK);
/*  74 */     this.notesTextArea.setForeground(Color.GREEN);
/*  75 */     this.notesTextArea.setFont(new Font("Geneva", 0, 12));
/*  76 */     this.closeButton.setText("CLOSE");
/*  77 */     this.closeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/*     */         try {
/*  80 */           String appData = System.getProperty("user.home");
/*  81 */           if (Util.isWindows())
/*  82 */             appData = WinFileSystem.getSpecialFolder(SpecialFolder.APPDATA, false) + "\\MetaPIGA2\\";
/*  83 */           else if (Util.isMacOS())
/*  84 */             appData = "";
/*     */           else {
/*  86 */             appData = System.getProperty("user.home") + ".MetaPIGA2/";
/*     */           }
/*  88 */           File file = new File(appData + "updateRead");
/*  89 */           file.createNewFile();
/*  90 */           FileWriter fw = new FileWriter(file, true);
/*  91 */           fw.append("\n3.1");
/*  92 */           fw.close();
/*     */         } catch (Exception ex) {
/*  94 */           ex.printStackTrace();
/*     */         }
/*  96 */         UpdateDialog.this.dispose();
/*     */       }
/*     */     });
/*  99 */     getContentPane().add(this.mainPanel);
/* 100 */     this.mainPanel.add(this.notesScrollPane, "Center");
/* 101 */     this.mainPanel.add(this.southPanel, "South");
/* 102 */     this.southPanel.add(this.closeButton, null);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.UpdateDialog
 * JD-Core Version:    0.6.2
 */