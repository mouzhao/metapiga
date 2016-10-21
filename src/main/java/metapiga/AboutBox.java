package metapiga;/*     */
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Font;
/*     */ import java.awt.Frame;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.WindowEvent;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class AboutBox extends JDialog
/*     */   implements ActionListener
/*     */ {
/*     */   private JPanel panel;
/*  38 */   JPanel panel1 = new JPanel();
/*  39 */   JPanel insetsPanel1 = new JPanel();
/*  40 */   JPanel insetsPanel3 = new JPanel();
/*  41 */   JButton button1 = new JButton();
/*  42 */   JLabel imageLabel = new JLabel();
/*  43 */   JLabel label1 = new JLabel();
/*  44 */   JLabel label3 = new JLabel();
/*  45 */   JLabel label4 = new JLabel();
/*  46 */   JLabel label5 = new JLabel();
/*  47 */   JLabel label6 = new JLabel();
/*  48 */   JLabel label7 = new JLabel();
/*  49 */   BorderLayout borderLayout1 = new BorderLayout();
/*  50 */   String product = "MetaPIGA 3.1";
/*  51 */   String comments1 = "Djordje Grbic, Raphael Helaers, & Michel C. Milinkovitch";
/*  52 */   String comments2 = "Laboratory of Artificial and Natural Evolution";
/*  53 */   String comments3 = "Dpt of Genetics & Evolution";
/*  54 */   String comments4 = "University of Geneva (Switzerland)";
/*  55 */   String librairies = "MetaPIGA includes the following third party libraries";
/*  56 */   private final JLabel wwwLabel = new JLabel("http://www.metapiga.org");
/*  57 */   private final JLabel lblCernColt = new JLabel("CERN Colt Scientific 1.2.0");
/*  58 */   private final JLabel lblBioJava = new JLabel("BioJava");
/*  59 */   private final JLabel lblJAMA = new JLabel("JAMA : A Java Matrix Package");
/*  60 */   private final JLabel lblGoogleCollectionClasses = new JLabel("Google Collection classes");
/*  61 */   private final JLabel lbljCuda = new JLabel("jcuda : Java bindings for CUDA");
/*  62 */   private final JPanel panel_1 = new JPanel();
/*  63 */   private final JLabel label = new JLabel("&");
/*  64 */   private final JLabel lblJgrapht = new JLabel("jgrapht");
/*  65 */   private final JPanel panel_2 = new JPanel();
/*  66 */   private final JLabel lblMetapigaAlsoImplements = new JLabel("MetaPIGA also implements the");
/*  67 */   private final JLabel lblTrimal = new JLabel("trimAl");
/*  68 */   private final JLabel lblAlgorithms = new JLabel("algorithms (Capella-Gutierrez et al, 2009)");
/*  69 */   private final JLabel trimalIconLabel = new JLabel(Tools.getScaledIcon(MainFrame.imageTrimal, 16));
/*     */ 
/*     */   public AboutBox(Frame parent) {
/*  72 */     super(parent);
/*  73 */     enableEvents(64L);
/*     */     try {
/*  75 */       jbInit();
/*  76 */       pack();
/*     */     }
/*     */     catch (Exception e) {
/*  79 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   AboutBox() {
/*  84 */     this(null);
/*     */   }
/*     */ 
/*     */   private void jbInit() throws Exception
/*     */   {
/*  89 */     GridBagConstraints gridBagConstraints8 = new GridBagConstraints(0, 0, 1, 5, 0.0D, 0.0D, 10, 0, new Insets(5, 0, 5, 30), 0, 0);
/*  90 */     gridBagConstraints8.gridx = 0;
/*  91 */     gridBagConstraints8.weighty = 0.0D;
/*  92 */     gridBagConstraints8.weightx = 0.0D;
/*  93 */     gridBagConstraints8.anchor = 11;
/*  94 */     gridBagConstraints8.gridy = 0;
/*  95 */     GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
/*  96 */     gridBagConstraints71.anchor = 17;
/*  97 */     gridBagConstraints71.insets = new Insets(5, 5, 0, 2);
/*  98 */     gridBagConstraints71.gridx = 2;
/*  99 */     gridBagConstraints71.gridy = 0;
/* 100 */     gridBagConstraints71.weightx = 0.0D;
/* 101 */     GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
/* 102 */     gridBagConstraints61.insets = new Insets(2, 5, 5, 5);
/* 103 */     gridBagConstraints61.gridx = 1;
/* 104 */     gridBagConstraints61.gridy = 4;
/* 105 */     gridBagConstraints61.weightx = 0.0D;
/* 106 */     gridBagConstraints61.anchor = 18;
/* 107 */     gridBagConstraints61.gridheight = 1;
/* 108 */     GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
/* 109 */     gridBagConstraints51.insets = new Insets(2, 5, 5, 5);
/* 110 */     gridBagConstraints51.gridx = 1;
/* 111 */     gridBagConstraints51.gridy = 3;
/* 112 */     gridBagConstraints51.weightx = 0.0D;
/* 113 */     gridBagConstraints51.anchor = 18;
/* 114 */     gridBagConstraints51.gridheight = 1;
/* 115 */     GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
/* 116 */     gridBagConstraints41.insets = new Insets(5, 5, 5, 5);
/* 117 */     gridBagConstraints41.gridx = 1;
/* 118 */     gridBagConstraints41.gridy = 2;
/* 119 */     gridBagConstraints41.weightx = 0.0D;
/* 120 */     gridBagConstraints41.anchor = 18;
/* 121 */     gridBagConstraints41.gridheight = 1;
/* 122 */     GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
/* 123 */     gridBagConstraints31.insets = new Insets(10, 5, 5, 5);
/* 124 */     gridBagConstraints31.gridx = 1;
/* 125 */     gridBagConstraints31.gridy = 1;
/* 126 */     gridBagConstraints31.weightx = 0.0D;
/* 127 */     gridBagConstraints31.anchor = 18;
/* 128 */     gridBagConstraints31.gridheight = 1;
/* 129 */     GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
/* 130 */     gridBagConstraints21.gridwidth = 2;
/* 131 */     gridBagConstraints21.insets = new Insets(30, 5, 5, 5);
/* 132 */     gridBagConstraints21.gridx = 0;
/* 133 */     gridBagConstraints21.gridy = 5;
/* 134 */     gridBagConstraints21.weightx = 0.0D;
/* 135 */     gridBagConstraints21.anchor = 18;
/* 136 */     gridBagConstraints21.gridheight = 1;
/* 137 */     GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
/* 138 */     gridBagConstraints11.insets = new Insets(25, 5, 10, 5);
/* 139 */     gridBagConstraints11.gridx = 1;
/* 140 */     gridBagConstraints11.gridy = 0;
/* 141 */     gridBagConstraints11.fill = 0;
/* 142 */     gridBagConstraints11.weightx = 0.0D;
/* 143 */     gridBagConstraints11.anchor = 18;
/* 144 */     gridBagConstraints11.gridheight = 1;
/* 145 */     GridBagConstraints gridBagConstraints7 = new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0);
/* 146 */     gridBagConstraints7.weightx = 1.0D;
/* 147 */     GridBagConstraints gridBagConstraints4 = new GridBagConstraints(1, 0, 1, 1, 1.0D, 1.0D, 17, 1, new Insets(5, 5, 5, 5), 0, 0);
/* 148 */     gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
/* 149 */     gridBagConstraints4.ipadx = 0;
/* 150 */     gridBagConstraints4.anchor = 10;
/* 151 */     gridBagConstraints4.weightx = 1.0D;
/* 152 */     gridBagConstraints4.weighty = 1.0D;
/* 153 */     gridBagConstraints4.fill = 1;
/* 154 */     this.imageLabel.setIcon(MainFrame.imageMetapiga);
/* 155 */     setTitle("About");
/* 156 */     this.label1.setText(this.product);
/* 157 */     this.label3.setText(this.librairies);
/* 158 */     this.label4.setText(this.comments1);
/* 159 */     this.label5.setText(this.comments2);
/* 160 */     this.label6.setText(this.comments3);
/* 161 */     this.label7.setText(this.comments4);
/* 162 */     this.button1.setText("Ok");
/* 163 */     this.button1.addActionListener(this);
/* 164 */     this.panel1.setLayout(this.borderLayout1);
/* 165 */     GridBagLayout gridBagLayout = new GridBagLayout();
/* 166 */     gridBagLayout.rowWeights = new double[] { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D };
/* 167 */     gridBagLayout.columnWeights = new double[] { 1.0D, 0.0D, 0.0D, 0.0D };
/* 168 */     gridBagLayout.columnWidths = new int[4];
/* 169 */     this.insetsPanel3.setLayout(gridBagLayout);
/* 170 */     this.insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
/* 171 */     this.insetsPanel3.add(this.label1, gridBagConstraints11);
/* 172 */     GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 173 */     gridBagConstraints.weighty = 0.0D;
/* 174 */     gridBagConstraints.weightx = 1.0D;
/* 175 */     gridBagConstraints.fill = 1;
/* 176 */     gridBagConstraints.gridheight = 11;
/* 177 */     gridBagConstraints.gridy = 0;
/* 178 */     gridBagConstraints.gridx = 3;
/* 179 */     this.insetsPanel3.add(getPanel(), gridBagConstraints);
/* 180 */     this.insetsPanel3.add(this.label3, gridBagConstraints21);
/* 181 */     this.insetsPanel3.add(this.label4, gridBagConstraints31);
/* 182 */     this.insetsPanel3.add(this.label5, gridBagConstraints41);
/* 183 */     this.insetsPanel3.add(this.label6, gridBagConstraints51);
/* 184 */     this.insetsPanel3.add(this.label7, gridBagConstraints61);
/* 185 */     this.insetsPanel3.add(this.imageLabel, gridBagConstraints8);
/* 186 */     this.insetsPanel1.add(this.button1, null);
/* 187 */     this.panel1.add(this.insetsPanel1, "South");
/* 188 */     this.panel1.add(this.insetsPanel3, "North");
/*     */ 
/* 190 */     GridBagConstraints gbc = new GridBagConstraints();
/* 191 */     gbc.anchor = 16;
/* 192 */     gbc.insets = new Insets(2, 0, 5, 5);
/* 193 */     gbc.gridx = 0;
/* 194 */     gbc.gridy = 4;
/* 195 */     this.wwwLabel.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 198 */         Tools.openURL("http://www.metapiga.org");
/*     */       }
/*     */     });
/* 201 */     this.wwwLabel.setForeground(Color.BLUE);
/* 202 */     this.wwwLabel.setFont(new Font("Segoe UI", 2, 12));
/* 203 */     this.insetsPanel3.add(this.wwwLabel, gbc);
/*     */ 
/* 205 */     this.lblCernColt.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 208 */         Tools.openURL("http://acs.lbl.gov/software/colt/");
/*     */       }
/*     */     });
/* 211 */     this.lblCernColt.setForeground(Color.BLUE);
/*     */ 
/* 213 */     this.lblGoogleCollectionClasses.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 216 */         Tools.openURL("http://code.google.com/p/google-collections/");
/*     */       }
/*     */     });
/* 219 */     this.lblGoogleCollectionClasses.setForeground(Color.BLUE);
/*     */ 
/* 221 */     this.lblJAMA.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 224 */         Tools.openURL("http://math.nist.gov/javanumerics/jama/");
/*     */       }
/*     */     });
/* 227 */     this.lblJAMA.setForeground(Color.BLUE);
/*     */ 
/* 229 */     this.lbljCuda.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 232 */         Tools.openURL("http://www.jcuda.org/");
/*     */       }
/*     */     });
/* 235 */     this.lbljCuda.setForeground(Color.BLUE);
/*     */ 
/* 237 */     GridBagConstraints gbc_1 = new GridBagConstraints();
/* 238 */     gbc_1.anchor = 18;
/* 239 */     gbc_1.insets = new Insets(5, 5, 5, 5);
/* 240 */     gbc_1.gridx = 0;
/* 241 */     gbc_1.gridy = 6;
/* 242 */     this.insetsPanel3.add(this.lblCernColt, gbc_1);
/*     */ 
/* 244 */     GridBagConstraints gbc_4 = new GridBagConstraints();
/* 245 */     gbc_4.anchor = 18;
/* 246 */     gbc_4.insets = new Insets(5, 5, 5, 5);
/* 247 */     gbc_4.gridx = 1;
/* 248 */     gbc_4.gridy = 6;
/* 249 */     this.insetsPanel3.add(this.lblJAMA, gbc_4);
/*     */ 
/* 251 */     GridBagConstraints gbc_5 = new GridBagConstraints();
/* 252 */     gbc_5.anchor = 18;
/* 253 */     gbc_5.insets = new Insets(5, 5, 5, 5);
/* 254 */     gbc_5.gridx = 1;
/* 255 */     gbc_5.gridy = 7;
/* 256 */     this.insetsPanel3.add(this.lblGoogleCollectionClasses, gbc_5);
/*     */ 
/* 258 */     GridBagConstraints gbc_6 = new GridBagConstraints();
/* 259 */     gbc_6.anchor = 18;
/* 260 */     gbc_6.insets = new Insets(5, 5, 5, 5);
/* 261 */     gbc_6.gridx = 0;
/* 262 */     gbc_6.gridy = 8;
/* 263 */     this.insetsPanel3.add(this.lbljCuda, gbc_6);
/*     */ 
/* 265 */     GridBagConstraints gbc_3 = new GridBagConstraints();
/* 266 */     gbc_3.insets = new Insets(0, 0, 5, 5);
/* 267 */     gbc_3.fill = 1;
/* 268 */     gbc_3.gridx = 0;
/* 269 */     gbc_3.gridy = 7;
/* 270 */     FlowLayout flowLayout = (FlowLayout)this.panel_1.getLayout();
/* 271 */     flowLayout.setAlignment(3);
/* 272 */     this.insetsPanel3.add(this.panel_1, gbc_3);
/* 273 */     this.panel_1.add(this.lblBioJava);
/*     */ 
/* 275 */     this.lblBioJava.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 278 */         Tools.openURL("http://www.biojava.org/");
/*     */       }
/*     */     });
/* 281 */     this.lblBioJava.setForeground(Color.BLUE);
/*     */ 
/* 283 */     this.lblJgrapht.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 286 */         Tools.openURL("http://www.jgrapht.org/");
/*     */       }
/*     */     });
/* 289 */     this.lblJgrapht.setForeground(Color.BLUE);
/*     */ 
/* 291 */     this.panel_1.add(this.label);
/*     */ 
/* 293 */     this.panel_1.add(this.lblJgrapht);
/*     */ 
/* 295 */     GridBagConstraints gbc_panel_2 = new GridBagConstraints();
/* 296 */     gbc_panel_2.gridwidth = 2;
/* 297 */     gbc_panel_2.insets = new Insets(15, 0, 5, 5);
/* 298 */     gbc_panel_2.fill = 1;
/* 299 */     gbc_panel_2.gridx = 0;
/* 300 */     gbc_panel_2.gridy = 9;
/* 301 */     FlowLayout flowLayout_1 = (FlowLayout)this.panel_2.getLayout();
/* 302 */     flowLayout_1.setAlignment(3);
/* 303 */     this.insetsPanel3.add(this.panel_2, gbc_panel_2);
/*     */ 
/* 305 */     this.panel_2.add(this.lblMetapigaAlsoImplements);
/* 306 */     this.lblTrimal.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseClicked(MouseEvent arg0) {
/* 309 */         Tools.openURL("http://trimal.cgenomics.org");
/*     */       }
/*     */     });
/* 312 */     this.lblTrimal.setForeground(Color.BLUE);
/*     */ 
/* 314 */     this.panel_2.add(this.lblTrimal);
/*     */ 
/* 316 */     this.panel_2.add(this.lblAlgorithms);
/*     */ 
/* 318 */     this.panel_2.add(this.trimalIconLabel);
/* 319 */     getContentPane().add(this.panel1, null);
/* 320 */     setResizable(true);
/*     */   }
/*     */ 
/*     */   protected void processWindowEvent(WindowEvent e)
/*     */   {
/* 325 */     if (e.getID() == 201) {
/* 326 */       cancel();
/*     */     }
/* 328 */     super.processWindowEvent(e);
/*     */   }
/*     */ 
/*     */   void cancel()
/*     */   {
/* 333 */     dispose();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 338 */     if (e.getSource() == this.button1)
/* 339 */       cancel();
/*     */   }
/*     */ 
/*     */   protected JPanel getPanel()
/*     */   {
/* 346 */     if (this.panel == null) {
/* 347 */       this.panel = new JPanel();
/*     */     }
/* 349 */     return this.panel;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.AboutBox
 * JD-Core Version:    0.6.2
 */