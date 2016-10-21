package metapiga;/*     */
/*     */ 
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Cursor;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.SwingUtilities;
/*     */ 
/*     */ public class WaitingLogo
/*     */   implements Runnable
/*     */ {
/*  50 */   public static final ImageIcon imageRest = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/rest.png"));
/*  51 */   public static final ImageIcon imageAnimation0 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_0.png"));
/*  52 */   public static final ImageIcon imageAnimation1 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_1.png"));
/*  53 */   public static final ImageIcon imageAnimation2 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_2.png"));
/*  54 */   public static final ImageIcon imageAnimation3 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_3.png"));
/*  55 */   public static final ImageIcon imageAnimation4 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_4.png"));
/*  56 */   public static final ImageIcon imageAnimation5 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_5.png"));
/*  57 */   public static final ImageIcon imageAnimation6 = new ImageIcon(MainFrame.class.getResource("metapiga/resources/splash/animation_6.png"));
/*     */   CardLayout logoCardLayout;
/*     */   JPanel logoPanel;
/*     */   Component component;
/*     */   JLabel statusBar;
/*     */   ProgressHandling progressBar;
/*     */   boolean waiting;
/*  65 */   int numImages = 7;
/*     */   String text;
/*     */   boolean indeterminate;
/*     */ 
/*     */   public WaitingLogo(Component parentComponent, Status status)
/*     */   {
/*  71 */     this.logoPanel = MainFrame.splashPanel;
/*  72 */     this.statusBar = MainFrame.statusBar;
/*  73 */     this.progressBar = MetaPIGA.progressHandling;
/*  74 */     this.component = parentComponent;
/*  75 */     this.text = status.text;
/*  76 */     this.indeterminate = status.indeterminate;
/*  77 */     this.logoPanel.removeAll();
/*  78 */     this.logoCardLayout = new CardLayout();
/*  79 */     this.logoPanel.setLayout(this.logoCardLayout);
/*  80 */     this.logoPanel.add(new JLabel(imageRest), "rest");
/*  81 */     this.logoPanel.add(new JLabel(imageAnimation0), "0");
/*  82 */     this.logoPanel.add(new JLabel(imageAnimation1), "1");
/*  83 */     this.logoPanel.add(new JLabel(imageAnimation2), "2");
/*  84 */     this.logoPanel.add(new JLabel(imageAnimation3), "3");
/*  85 */     this.logoPanel.add(new JLabel(imageAnimation4), "4");
/*  86 */     this.logoPanel.add(new JLabel(imageAnimation5), "5");
/*  87 */     this.logoPanel.add(new JLabel(imageAnimation6), "6");
/*  88 */     this.waiting = false;
/*     */   }
/*     */ 
/*     */   public void stop(Status status) {
/*  92 */     this.text = status.text;
/*  93 */     this.indeterminate = status.indeterminate;
/*  94 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/*  96 */         WaitingLogo.this.component.setCursor(Cursor.getPredefinedCursor(0));
/*  97 */         WaitingLogo.this.statusBar.setVisible(true);
/*  98 */         WaitingLogo.this.progressBar.setVisible(false);
/*  99 */         WaitingLogo.this.statusBar.setText(WaitingLogo.this.text);
/* 100 */         WaitingLogo.this.progressBar.printText(WaitingLogo.this.text);
/*     */       }
/*     */     });
/* 103 */     this.waiting = false;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 108 */     this.waiting = true;
/* 109 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 111 */         WaitingLogo.this.component.setCursor(Cursor.getPredefinedCursor(3));
/* 112 */         WaitingLogo.this.statusBar.setVisible(false);
/* 113 */         WaitingLogo.this.progressBar.newIndeterminateProgress(WaitingLogo.this.text);
/* 114 */         WaitingLogo.this.progressBar.setVisible(true);
/*     */       }
/*     */     });
/* 117 */     int current = 0;
/* 118 */     while (this.waiting) {
/* 119 */       current++;
/* 120 */       if (current == this.numImages) current = 0;
/* 121 */       final int icurrent = current;
/* 122 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 124 */           WaitingLogo.this.logoCardLayout.show(WaitingLogo.this.logoPanel, icurrent);
/*     */         }
/*     */       });
/*     */       try {
/* 128 */         Thread.sleep(200L);
/*     */       } catch (InterruptedException e) {
/* 130 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 133 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 135 */         WaitingLogo.this.logoCardLayout.show(WaitingLogo.this.logoPanel, "rest");
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public static enum Status
/*     */   {
/*  20 */     LOAD_DATA_FILE("Loading a data file", false, true), 
/*  21 */     DATA_FILE_LOADED("Data file successfully loaded", true, false), 
/*  22 */     DATA_BATCH_LOADED("Nexus batch successfully loaded", true, false), 
/*  23 */     DATA_FILE_NOT_LOADED("Data file NOT loaded", true, false), 
/*  24 */     SAVE_NEXUS_FILE("Saving parameters to a Nexus file", false, true), 
/*  25 */     NEXUS_FILE_SAVED("Nexus file successfully saved", true, false), 
/*  26 */     NEXUS_FILE_NOT_SAVED("Nexus file NOT saved", true, false), 
/*  27 */     SAVING_PARAMETERS("Saving changes", false, true), 
/*  28 */     PARAMETERS_NOT_SAVED("Cannot rebuild the dataset with new parameters", true, false), 
/*  29 */     PARAMETERS_SAVED("Settings changes saved", true, false), 
/*  30 */     DUPLICATION("Duplicating Nexus file", false, true), 
/*  31 */     DUPLICATION_DONE("Nexus file duplicated", true, false), 
/*  32 */     DUPLICATION_NOT_DONE("Nexus file NOT duplicated", true, false), 
/*  33 */     CHECK_DATASET("Testing dataset", false, true), 
/*  34 */     CHECK_DATASET_DONE("Dataset fully tested", true, true), 
/*  35 */     CHECK_DATASET_NOT_DONE("The dataset was NOT fully tested", true, false), 
/*  36 */     TREE_GENERATION("Generating tree(s)", false, false), 
/*  37 */     TREE_GENERATION_DONE("Tree(s) generated", true, false), 
/*  38 */     COMPUTING_DISTANCES("Computing distances", false, true), 
/*  39 */     COMPUTING_DISTANCES_DONE("Distances computed", true, false);
/*     */ 
/*     */     public String text;
/*     */     public final boolean enable;
/*     */     public final boolean indeterminate;
/*     */ 
/*  44 */     private Status(String text, boolean enable, boolean indeterminate) { this.text = text;
/*  45 */       this.enable = enable;
/*  46 */       this.indeterminate = indeterminate;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.WaitingLogo
 * JD-Core Version:    0.6.2
 */