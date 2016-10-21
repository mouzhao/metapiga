/*     */ package metapiga.modelization;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FileDialog;
/*     */ import java.awt.Font;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusAdapter;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JRadioButton;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JSpinner;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.JTextPane;
/*     */
/*     */ import javax.swing.SpinnerNumberModel;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.SwingWorker;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import javax.swing.text.DefaultEditorKit;
/*     */ import javax.swing.text.DefaultStyledDocument;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.ProgressHandling;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */
/*     */
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class ComputeDistancesFrame extends JFrame
/*     */ {
/*     */   private final MainFrame mainFrame;
/*     */   private final Parameters P;
/*     */   private DistanceMatrix D;
/*  60 */   private JPanel evaluationPanel = new JPanel();
/*  61 */   private GridBagLayout gridBagLayout2 = new GridBagLayout();
/*     */   private TitledBorder titledBorder2;
/*     */   private TitledBorder titledBorder3;
/*  64 */   private JPanel modelPanel = new JPanel();
/*  65 */   JRadioButton modelTN93RadioButton = new JRadioButton();
/*  66 */   JRadioButton modelK2PRadioButton = new JRadioButton();
/*  67 */   private GridBagLayout gridBagLayout28 = new GridBagLayout();
/*  68 */   JRadioButton modelGTRRadioButton = new JRadioButton();
/*  69 */   JRadioButton modelGTR2RadioButton = new JRadioButton();
/*  70 */   JRadioButton modelGTR20RadioButton = new JRadioButton();
/*  71 */   JRadioButton modelPoissonRadioButton = new JRadioButton();
/*  72 */   JRadioButton modelHKY85RadioButton = new JRadioButton();
/*  73 */   JRadioButton modelJCRadioButton = new JRadioButton();
/*  74 */   JRadioButton distributionGammaRadioButton = new JRadioButton();
/*  75 */   private JPanel distributionPanel = new JPanel();
/*  76 */   JRadioButton distributionVDPRadioButton = new JRadioButton();
/*  77 */   JRadioButton distributionNoneRadioButton = new JRadioButton();
/*  78 */   private GridBagLayout gridBagLayout24 = new GridBagLayout();
/*  79 */   private ButtonGroup modelButtonGroup = new ButtonGroup();
/*  80 */   private ButtonGroup distributionButtonGroup = new ButtonGroup();
/*  81 */   private ButtonGroup pinvButtonGroup = new ButtonGroup();
/*  82 */   private ButtonGroup piButtonGroup = new ButtonGroup();
/*  83 */   JRadioButton modelNoneRadioButton = new JRadioButton();
/*  84 */   JSpinner distributionVdpSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 32, 1));
/*  85 */   private JPanel pinvPanel = null;
/*  86 */   JRadioButton pinvNoneRadioButton = null;
/*  87 */   JRadioButton pinvValueRadioButton = null;
/*  88 */   JSpinner pinvSpinner = null;
/*  89 */   private JPanel gammaValuesPanel = null;
/*  90 */   private JLabel distributionGammaShapeLabel = null;
/*  91 */   JTextField distributionGammaShapeTextField = null;
/*  92 */   private JPanel proportionPanel = null;
/*  93 */   JLabel pinvProportionLabel = null;
/*  94 */   JLabel pinvPiLabel = null;
/*  95 */   JRadioButton pinvEqualRadioButton = null;
/*  96 */   JRadioButton pinvEstimatedRadioButton = null;
/*  97 */   JRadioButton pinvConstantRadioButton = null;
/*     */   private JScrollPane matrixPanel;
/*     */   private JScrollPane evaluationScroll;
/* 100 */   private JPanel matrixNoWordWrapPanel = new JPanel();
/*     */   private JTextPane matrixTextPane;
/* 102 */   private final JButton computeButton = new JButton();
/* 103 */   private final JButton btnSaveToFile = new JButton("Save to file");
/* 104 */   private final JPanel blankPanel = new JPanel();
/* 105 */   private final JRadioButton rdbtnAbsoluteNumberOf = new JRadioButton("Absolute number of differences");
/* 106 */   private final JRadioButton rdbtnDistances = new JRadioButton("Distances");
/* 107 */   private final ButtonGroup distancesButtonGroup = new ButtonGroup();
/*     */ 
/*     */   public ComputeDistancesFrame(MainFrame main, Parameters parameters) {
/* 110 */     this.mainFrame = main;
/* 111 */     this.P = parameters;
/* 112 */     setTitle("Compute distances of dataset " + this.P.label);
/* 113 */     setDataType();
/*     */ 
/* 115 */     GridBagConstraints gbc_matrixPanel = new GridBagConstraints();
/* 116 */     gbc_matrixPanel.insets = new Insets(5, 0, 0, 0);
/* 117 */     gbc_matrixPanel.fill = 1;
/* 118 */     gbc_matrixPanel.gridheight = 6;
/* 119 */     gbc_matrixPanel.gridx = 1;
/* 120 */     gbc_matrixPanel.weightx = 1.0D;
/* 121 */     gbc_matrixPanel.weighty = 1.0D;
/* 122 */     gbc_matrixPanel.gridy = 0;
/* 123 */     GridBagConstraints gridBagConstraints12 = new GridBagConstraints(0, 3, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 0, 0), 0, 0);
/* 124 */     GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
/* 125 */     gridBagConstraints10.gridx = 0;
/* 126 */     gridBagConstraints10.fill = 2;
/* 127 */     gridBagConstraints10.weightx = 1.0D;
/* 128 */     gridBagConstraints10.gridy = 2;
/* 129 */     GridBagConstraints gridBagConstraints1 = new GridBagConstraints(0, 4, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(0, 25, 0, 0), 0, 0);
/* 130 */     gridBagConstraints1.insets = new Insets(0, 25, 0, 20);
/* 131 */     gridBagConstraints1.fill = 2;
/* 132 */     GridBagConstraints gridBagConstraints2 = new GridBagConstraints(0, 3, 1, 1, 1.0D, 1.0D, 11, 2, new Insets(0, 5, 5, 5), 0, 0);
/* 133 */     gridBagConstraints2.weightx = 0.0D;
/* 134 */     gridBagConstraints2.weighty = 0.0D;
/* 135 */     GridBagConstraints gridBagConstraints30 = new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0);
/* 136 */     gridBagConstraints30.anchor = 11;
/* 137 */     gridBagConstraints30.insets = new Insets(0, 5, 5, 5);
/* 138 */     gridBagConstraints30.fill = 2;
/* 139 */     gridBagConstraints30.weightx = 0.0D;
/* 140 */     gridBagConstraints30.gridy = 4;
/* 141 */     GridBagConstraints gridBagConstraints29 = new GridBagConstraints(1, 0, 1, 1, 1.0D, 1.0D, 11, 2, new Insets(0, 5, 5, 5), 0, 0);
/* 142 */     gridBagConstraints29.gridx = 0;
/* 143 */     gridBagConstraints29.weighty = 0.0D;
/* 144 */     gridBagConstraints29.gridheight = 1;
/* 145 */     gridBagConstraints29.weightx = 0.0D;
/* 146 */     gridBagConstraints29.fill = 2;
/* 147 */     gridBagConstraints29.anchor = 11;
/* 148 */     gridBagConstraints29.gridy = 2;
/* 149 */     this.titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Distance correction");
/* 150 */     this.titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Rate heterogeneity");
/* 151 */     this.matrixNoWordWrapPanel.setLayout(new BorderLayout(0, 0));
/* 152 */     this.matrixNoWordWrapPanel.add(getMatrixTextPane());
/* 153 */     this.gridBagLayout2.rowWeights = new double[] { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D };
/* 154 */     this.gridBagLayout2.columnWeights = new double[] { 0.0D, 1.0D };
/* 155 */     this.evaluationPanel.setLayout(this.gridBagLayout2);
/* 156 */     getContentPane().add(getEvaluationScroolPane());
/* 157 */     this.modelPanel.setLayout(this.gridBagLayout28);
/* 158 */     this.modelTN93RadioButton.setText("TN93");
/* 159 */     this.modelK2PRadioButton.setText("K2P");
/* 160 */     this.modelGTRRadioButton.setText("GTR");
/* 161 */     this.modelHKY85RadioButton.setText("HKY85");
/* 162 */     this.modelJCRadioButton.setText("JC");
/* 163 */     this.modelGTR2RadioButton.setText("GTR2");
/* 164 */     this.modelGTR20RadioButton.setText("GTR20");
/* 165 */     this.modelPoissonRadioButton.setText("Poisson");
/* 166 */     this.distributionGammaRadioButton.setText("Discrete Gamma");
/* 167 */     this.distributionGammaRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 169 */         ComputeDistancesFrame.this.distributionGammaRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 172 */     this.distributionPanel.setLayout(this.gridBagLayout24);
/* 173 */     this.distributionVDPRadioButton.setText("Van de Peer");
/* 174 */     this.distributionVDPRadioButton.setVisible(false);
/* 175 */     this.distributionVDPRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 177 */         ComputeDistancesFrame.this.distributionVDPRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 180 */     this.distributionNoneRadioButton.setSelected(true);
/* 181 */     this.distributionNoneRadioButton.setText("None");
/* 182 */     this.modelPanel.setBorder(this.titledBorder2);
/* 183 */     this.distributionPanel.setBorder(this.titledBorder3);
/* 184 */     this.modelNoneRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent arg0) {
/* 186 */         if (ComputeDistancesFrame.this.modelNoneRadioButton.isSelected()) {
/* 187 */           ComputeDistancesFrame.this.pinvNoneRadioButton.setSelected(true);
/* 188 */           ComputeDistancesFrame.this.pinvValueRadioButton.setEnabled(false);
/* 189 */           ComputeDistancesFrame.this.distributionNoneRadioButton.setSelected(true);
/* 190 */           ComputeDistancesFrame.this.distributionGammaRadioButton.setEnabled(false);
/* 191 */           ComputeDistancesFrame.this.distributionVDPRadioButton.setEnabled(false);
/*     */         } else {
/* 193 */           ComputeDistancesFrame.this.pinvValueRadioButton.setEnabled(true);
/* 194 */           ComputeDistancesFrame.this.distributionGammaRadioButton.setEnabled(true);
/* 195 */           ComputeDistancesFrame.this.distributionVDPRadioButton.setEnabled(true);
/*     */         }
/*     */       }
/*     */     });
/* 199 */     this.modelNoneRadioButton.setText("None");
/* 200 */     this.distributionVdpSpinner.setEnabled(false);
/* 201 */     this.distributionVdpSpinner.setPreferredSize(new Dimension(70, 18));
/* 202 */     this.distributionVdpSpinner.setVisible(false);
/*     */ 
/* 204 */     GridBagConstraints gbc_rdbtnAbsoluteNumberOf = new GridBagConstraints();
/* 205 */     gbc_rdbtnAbsoluteNumberOf.anchor = 17;
/* 206 */     gbc_rdbtnAbsoluteNumberOf.insets = new Insets(5, 5, 0, 5);
/* 207 */     gbc_rdbtnAbsoluteNumberOf.gridx = 0;
/* 208 */     gbc_rdbtnAbsoluteNumberOf.gridy = 0;
/* 209 */     this.distancesButtonGroup.add(this.rdbtnAbsoluteNumberOf);
/* 210 */     this.rdbtnAbsoluteNumberOf.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent arg0) {
/* 212 */         ComputeDistancesFrame.this.distanceSelection();
/*     */       }
/*     */     });
/* 215 */     this.evaluationPanel.add(this.rdbtnAbsoluteNumberOf, gbc_rdbtnAbsoluteNumberOf);
/*     */ 
/* 217 */     GridBagConstraints gbc_rdbtnDistances = new GridBagConstraints();
/* 218 */     gbc_rdbtnDistances.anchor = 17;
/* 219 */     gbc_rdbtnDistances.insets = new Insets(0, 5, 5, 5);
/* 220 */     gbc_rdbtnDistances.gridx = 0;
/* 221 */     gbc_rdbtnDistances.gridy = 1;
/* 222 */     this.distancesButtonGroup.add(this.rdbtnDistances);
/* 223 */     this.rdbtnDistances.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent arg0) {
/* 225 */         ComputeDistancesFrame.this.distanceSelection();
/*     */       }
/*     */     });
/* 228 */     this.rdbtnDistances.setSelected(true);
/* 229 */     this.evaluationPanel.add(this.rdbtnDistances, gbc_rdbtnDistances);
/* 230 */     this.modelPanel.add(this.modelGTRRadioButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 
/* 231 */       18, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 232 */     this.modelPanel.add(this.modelTN93RadioButton, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, 
/* 233 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 234 */     this.modelPanel.add(this.modelHKY85RadioButton, new GridBagConstraints(0, 2, 1, 1, 1.0D, 1.0D, 
/* 235 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 236 */     this.modelPanel.add(this.modelK2PRadioButton, new GridBagConstraints(0, 3, 1, 1, 1.0D, 1.0D, 
/* 237 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 238 */     this.modelPanel.add(this.modelJCRadioButton, new GridBagConstraints(0, 4, 1, 1, 1.0D, 1.0D, 
/* 239 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 240 */     this.modelPanel.add(this.modelGTR2RadioButton, new GridBagConstraints(0, 5, 1, 1, 1.0D, 1.0D, 
/* 241 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 242 */     this.modelPanel.add(this.modelGTR20RadioButton, new GridBagConstraints(0, 6, 1, 1, 1.0D, 1.0D, 
/* 243 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 244 */     this.modelPanel.add(this.modelPoissonRadioButton, new GridBagConstraints(0, 7, 1, 1, 1.0D, 1.0D, 
/* 245 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 246 */     this.modelPanel.add(this.modelNoneRadioButton, new GridBagConstraints(0, 8, 1, 1, 1.0D, 1.0D, 
/* 247 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 248 */     this.evaluationPanel.add(this.modelPanel, gridBagConstraints29);
/* 249 */     this.evaluationPanel.add(this.distributionPanel, gridBagConstraints2);
/* 250 */     this.evaluationPanel.add(getPinvPanel(), gridBagConstraints30);
/* 251 */     this.evaluationPanel.add(getMatrixPanel(), gbc_matrixPanel);
/* 252 */     this.distributionPanel.add(this.distributionNoneRadioButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 253 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 254 */     this.distributionPanel.add(this.distributionGammaRadioButton, new GridBagConstraints(0, 1, 1, 1, 1.0D, 0.0D, 
/* 255 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 256 */     this.distributionPanel.add(this.distributionVDPRadioButton, gridBagConstraints12);
/* 257 */     this.distributionPanel.add(this.distributionVdpSpinner, gridBagConstraints1);
/* 258 */     this.distributionPanel.add(getGammaValuesPanel(), gridBagConstraints10);
/* 259 */     this.modelButtonGroup.add(this.modelGTRRadioButton);
/* 260 */     this.modelButtonGroup.add(this.modelGTR2RadioButton);
/* 261 */     this.modelButtonGroup.add(this.modelGTR20RadioButton);
/* 262 */     this.modelButtonGroup.add(this.modelPoissonRadioButton);
/* 263 */     this.modelButtonGroup.add(this.modelTN93RadioButton);
/* 264 */     this.modelButtonGroup.add(this.modelHKY85RadioButton);
/* 265 */     this.modelButtonGroup.add(this.modelK2PRadioButton);
/* 266 */     this.modelButtonGroup.add(this.modelJCRadioButton);
/* 267 */     this.modelButtonGroup.add(this.modelNoneRadioButton);
/* 268 */     this.distributionButtonGroup.add(this.distributionNoneRadioButton);
/* 269 */     this.distributionButtonGroup.add(this.distributionGammaRadioButton);
/* 270 */     this.distributionButtonGroup.add(this.distributionVDPRadioButton);
/*     */ 
/* 272 */     this.pinvButtonGroup.add(this.pinvNoneRadioButton);
/* 273 */     this.pinvButtonGroup.add(this.pinvValueRadioButton);
/* 274 */     this.piButtonGroup.add(this.pinvEqualRadioButton);
/* 275 */     this.piButtonGroup.add(this.pinvEstimatedRadioButton);
/* 276 */     this.piButtonGroup.add(this.pinvConstantRadioButton);
/*     */ 
/* 278 */     GridBagConstraints gbc_blankPanel = new GridBagConstraints();
/* 279 */     gbc_blankPanel.insets = new Insets(0, 0, 0, 5);
/* 280 */     gbc_blankPanel.weighty = 1.0D;
/* 281 */     gbc_blankPanel.fill = 1;
/* 282 */     gbc_blankPanel.gridx = 0;
/* 283 */     gbc_blankPanel.gridy = 5;
/* 284 */     this.evaluationPanel.add(this.blankPanel, gbc_blankPanel);
/* 285 */     this.blankPanel.setLayout(new BorderLayout(0, 0));
/*     */ 
/* 287 */     JPanel panel = new JPanel();
/* 288 */     getContentPane().add(panel, "North");
/*     */ 
/* 290 */     this.computeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 292 */         ComputeDistancesFrame.this.mainFrame.setAllEnabled(null, WaitingLogo.Status.COMPUTING_DISTANCES);
/* 293 */         ComputeDistances compute = new ComputeDistances(ComputeDistancesFrame.this);
/* 294 */         compute.execute();
/*     */       }
/*     */     });
/* 297 */     this.computeButton.setText("Compute");
/* 298 */     panel.add(this.computeButton);
/* 299 */     this.btnSaveToFile.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 301 */         ComputeDistancesFrame.this.writeMatrixToFile();
/*     */       }
/*     */     });
/* 305 */     panel.add(this.btnSaveToFile);
/*     */   }
/*     */ 
/*     */   private JScrollPane getEvaluationScroolPane()
/*     */   {
/* 310 */     if (this.evaluationScroll == null) {
/* 311 */       this.evaluationScroll = new JScrollPane();
/* 312 */       this.evaluationScroll.setViewportView(this.evaluationPanel);
/* 313 */       this.evaluationScroll.setHorizontalScrollBarPolicy(30);
/* 314 */       this.evaluationScroll.setVerticalScrollBarPolicy(20);
/*     */     }
/* 316 */     return this.evaluationScroll;
/*     */   }
/*     */ 
/*     */   private void setDataType() {
/* 320 */     switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.P.dataset.getDataType().ordinal()]) {
/*     */     case 4:
/* 322 */       this.modelTN93RadioButton.setVisible(true);
/* 323 */       this.modelK2PRadioButton.setVisible(true);
/* 324 */       this.modelGTRRadioButton.setVisible(true);
/* 325 */       this.modelHKY85RadioButton.setVisible(true);
/* 326 */       this.modelJCRadioButton.setVisible(true);
/* 327 */       this.modelGTR20RadioButton.setVisible(false);
/* 328 */       this.modelGTR2RadioButton.setVisible(false);
/* 329 */       this.modelPoissonRadioButton.setVisible(false);
/* 330 */       this.modelK2PRadioButton.setSelected(true);
/* 331 */       break;
/*     */     case 1:
/* 333 */       this.modelTN93RadioButton.setVisible(true);
/* 334 */       this.modelK2PRadioButton.setVisible(true);
/* 335 */       this.modelGTRRadioButton.setVisible(true);
/* 336 */       this.modelHKY85RadioButton.setVisible(true);
/* 337 */       this.modelJCRadioButton.setVisible(true);
/* 338 */       this.modelGTR20RadioButton.setVisible(false);
/* 339 */       this.modelGTR2RadioButton.setVisible(false);
/* 340 */       this.modelPoissonRadioButton.setVisible(false);
/* 341 */       this.modelJCRadioButton.setSelected(true);
/* 342 */       break;
/*     */     case 2:
/* 344 */       this.modelTN93RadioButton.setVisible(false);
/* 345 */       this.modelK2PRadioButton.setVisible(false);
/* 346 */       this.modelGTRRadioButton.setVisible(false);
/* 347 */       this.modelHKY85RadioButton.setVisible(false);
/* 348 */       this.modelJCRadioButton.setVisible(false);
/* 349 */       this.modelGTR20RadioButton.setVisible(true);
/* 350 */       this.modelGTR2RadioButton.setVisible(false);
/* 351 */       this.modelPoissonRadioButton.setVisible(true);
/* 352 */       this.modelPoissonRadioButton.setSelected(true);
/* 353 */       break;
/*     */     case 3:
/* 355 */       this.modelTN93RadioButton.setVisible(false);
/* 356 */       this.modelK2PRadioButton.setVisible(false);
/* 357 */       this.modelGTRRadioButton.setVisible(false);
/* 358 */       this.modelHKY85RadioButton.setVisible(false);
/* 359 */       this.modelJCRadioButton.setVisible(false);
/* 360 */       this.modelGTR20RadioButton.setVisible(false);
/* 361 */       this.modelGTR2RadioButton.setVisible(true);
/* 362 */       this.modelPoissonRadioButton.setVisible(false);
/* 363 */       this.modelGTR2RadioButton.setSelected(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   private JPanel getPinvPanel()
/*     */   {
/* 374 */     if (this.pinvPanel == null) {
/* 375 */       GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
/* 376 */       gridBagConstraints51.gridx = 0;
/* 377 */       gridBagConstraints51.insets = new Insets(0, 25, 0, 0);
/* 378 */       gridBagConstraints51.fill = 2;
/* 379 */       gridBagConstraints51.gridy = 6;
/* 380 */       GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
/* 381 */       gridBagConstraints41.gridx = 0;
/* 382 */       gridBagConstraints41.insets = new Insets(0, 25, 0, 0);
/* 383 */       gridBagConstraints41.fill = 2;
/* 384 */       gridBagConstraints41.gridy = 5;
/* 385 */       GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
/* 386 */       gridBagConstraints31.gridx = 0;
/* 387 */       gridBagConstraints31.fill = 2;
/* 388 */       gridBagConstraints31.insets = new Insets(0, 25, 0, 0);
/* 389 */       gridBagConstraints31.gridy = 4;
/* 390 */       GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
/* 391 */       gridBagConstraints21.gridx = 0;
/* 392 */       gridBagConstraints21.insets = new Insets(5, 20, 0, 0);
/* 393 */       gridBagConstraints21.fill = 2;
/* 394 */       gridBagConstraints21.weightx = 0.0D;
/* 395 */       gridBagConstraints21.gridy = 3;
/* 396 */       this.pinvPiLabel = new JLabel();
/* 397 */       this.pinvPiLabel.setText("Base composition");
/* 398 */       this.pinvPiLabel.setEnabled(false);
/* 399 */       this.pinvPiLabel.setDisplayedMnemonic(0);
/* 400 */       GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
/* 401 */       gridBagConstraints13.gridx = 0;
/* 402 */       gridBagConstraints13.anchor = 17;
/* 403 */       gridBagConstraints13.gridy = 2;
/* 404 */       GridBagConstraints gridBagConstraints = new GridBagConstraints(0, 8, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 20, 0, 0), 0, 0);
/* 405 */       gridBagConstraints.insets = new Insets(0, 0, 0, 0);
/* 406 */       gridBagConstraints.gridy = 1;
/* 407 */       gridBagConstraints.gridx = 0;
/* 408 */       this.pinvPanel = new JPanel();
/* 409 */       this.pinvPanel.setLayout(new GridBagLayout());
/* 410 */       this.pinvPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Invariable sites"));
/* 411 */       this.pinvPanel.add(getPinvNoneRadioButton(), new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 412 */       this.pinvPanel.add(getPinvValueRadioButton(), gridBagConstraints);
/* 413 */       this.pinvPanel.add(getProportionPanel(), gridBagConstraints13);
/* 414 */       this.pinvPanel.add(this.pinvPiLabel, gridBagConstraints21);
/* 415 */       this.pinvPanel.add(getPinvEqualRadioButton(), gridBagConstraints31);
/* 416 */       this.pinvPanel.add(getPinvEstimatedRadioButton(), gridBagConstraints41);
/* 417 */       this.pinvPanel.add(getPinvConstantRadioButton(), gridBagConstraints51);
/*     */     }
/* 419 */     return this.pinvPanel;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvNoneRadioButton()
/*     */   {
/* 427 */     if (this.pinvNoneRadioButton == null) {
/* 428 */       this.pinvNoneRadioButton = new JRadioButton();
/* 429 */       this.pinvNoneRadioButton.setSelected(true);
/* 430 */       this.pinvNoneRadioButton.setText("None");
/*     */     }
/* 432 */     return this.pinvNoneRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvValueRadioButton()
/*     */   {
/* 440 */     if (this.pinvValueRadioButton == null) {
/* 441 */       this.pinvValueRadioButton = new JRadioButton();
/* 442 */       this.pinvValueRadioButton.setText("P-Invariant");
/* 443 */       this.pinvValueRadioButton.addItemListener(new ItemListener() {
/*     */         public void itemStateChanged(ItemEvent e) {
/* 445 */           if (ComputeDistancesFrame.this.pinvValueRadioButton.isSelected()) {
/* 446 */             ComputeDistancesFrame.this.pinvSpinner.setEnabled(true);
/* 447 */             ComputeDistancesFrame.this.pinvProportionLabel.setEnabled(true);
/* 448 */             ComputeDistancesFrame.this.pinvPiLabel.setEnabled(true);
/* 449 */             ComputeDistancesFrame.this.pinvConstantRadioButton.setEnabled(true);
/* 450 */             ComputeDistancesFrame.this.pinvEstimatedRadioButton.setEnabled(true);
/* 451 */             ComputeDistancesFrame.this.pinvEqualRadioButton.setEnabled(true);
/*     */           } else {
/* 453 */             ComputeDistancesFrame.this.pinvSpinner.setEnabled(false);
/* 454 */             ComputeDistancesFrame.this.pinvProportionLabel.setEnabled(false);
/* 455 */             ComputeDistancesFrame.this.pinvPiLabel.setEnabled(false);
/* 456 */             ComputeDistancesFrame.this.pinvConstantRadioButton.setEnabled(false);
/* 457 */             ComputeDistancesFrame.this.pinvEstimatedRadioButton.setEnabled(false);
/* 458 */             ComputeDistancesFrame.this.pinvEqualRadioButton.setEnabled(false);
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 463 */     return this.pinvValueRadioButton;
/*     */   }
/*     */ 
/*     */   private JSpinner getPinvSpinner()
/*     */   {
/* 472 */     if (this.pinvSpinner == null) {
/* 473 */       this.pinvSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
/* 474 */       this.pinvSpinner.setEnabled(false);
/* 475 */       this.pinvSpinner.setPreferredSize(new Dimension(70, 18));
/*     */     }
/* 477 */     return this.pinvSpinner;
/*     */   }
/*     */ 
/*     */   private JPanel getGammaValuesPanel()
/*     */   {
/* 486 */     if (this.gammaValuesPanel == null) {
/* 487 */       GridBagConstraints gridBagConstraints8 = new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 5, 0, 5), 0, 0);
/* 488 */       gridBagConstraints8.fill = 2;
/* 489 */       gridBagConstraints8.weightx = 1.0D;
/* 490 */       gridBagConstraints8.ipadx = 0;
/* 491 */       gridBagConstraints8.insets = new Insets(0, 5, 0, 20);
/* 492 */       GridBagConstraints gridBagConstraints7 = new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 5, 0, 5), 0, 0);
/* 493 */       gridBagConstraints7.anchor = 17;
/* 494 */       gridBagConstraints7.insets = new Insets(0, 25, 0, 5);
/* 495 */       this.distributionGammaShapeLabel = new JLabel();
/* 496 */       this.distributionGammaShapeLabel.setEnabled(false);
/* 497 */       this.distributionGammaShapeLabel.setPreferredSize(new Dimension(40, 15));
/* 498 */       this.distributionGammaShapeLabel.setText("shape");
/* 499 */       this.distributionGammaShapeLabel.setMaximumSize(new Dimension(260, 15));
/* 500 */       this.gammaValuesPanel = new JPanel();
/* 501 */       this.gammaValuesPanel.setLayout(new GridBagLayout());
/* 502 */       this.gammaValuesPanel.add(this.distributionGammaShapeLabel, gridBagConstraints7);
/* 503 */       this.gammaValuesPanel.add(getDistributionGammaStartTextField(), gridBagConstraints8);
/*     */     }
/* 505 */     return this.gammaValuesPanel;
/*     */   }
/*     */ 
/*     */   private JTextField getDistributionGammaStartTextField()
/*     */   {
/* 514 */     if (this.distributionGammaShapeTextField == null) {
/* 515 */       this.distributionGammaShapeTextField = new JTextField();
/* 516 */       this.distributionGammaShapeTextField.setEnabled(false);
/* 517 */       this.distributionGammaShapeTextField.setText("0.5");
/* 518 */       this.distributionGammaShapeTextField.setPreferredSize(new Dimension(70, 20));
/* 519 */       this.distributionGammaShapeTextField
/* 520 */         .addFocusListener(new FocusAdapter() {
/*     */         public void focusLost(FocusEvent e) {
/*     */           try {
/* 523 */             Double d = Double.valueOf(Double.parseDouble(ComputeDistancesFrame.this.distributionGammaShapeTextField.getText()));
/* 524 */             if (d.doubleValue() <= 0.0D) throw new NumberFormatException(d + " is a not a non-zero positive number"); 
/*     */           }
/* 526 */           catch (NumberFormatException ex) { ex.printStackTrace();
/* 527 */             JOptionPane.showMessageDialog(null, "Error : " + ComputeDistancesFrame.this.distributionGammaShapeTextField.getText() + " is not a valid positive number. \nShape parameter is reset to 0.5.", "Gamma shape parameter", 0);
/* 528 */             ComputeDistancesFrame.this.distributionGammaShapeTextField.setText("0.5");
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 533 */     return this.distributionGammaShapeTextField;
/*     */   }
/*     */ 
/*     */   private JPanel getProportionPanel()
/*     */   {
/* 542 */     if (this.proportionPanel == null) {
/* 543 */       this.pinvProportionLabel = new JLabel();
/* 544 */       this.pinvProportionLabel.setText("Proportion");
/* 545 */       this.pinvProportionLabel.setEnabled(false);
/* 546 */       GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
/* 547 */       gridBagConstraints5.gridx = 0;
/* 548 */       gridBagConstraints5.insets = new Insets(0, 20, 0, 5);
/* 549 */       gridBagConstraints5.gridy = 0;
/* 550 */       GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
/* 551 */       gridBagConstraints6.fill = 2;
/* 552 */       gridBagConstraints6.gridx = 1;
/* 553 */       gridBagConstraints6.gridy = 0;
/* 554 */       gridBagConstraints6.insets = new Insets(0, 0, 0, 5);
/* 555 */       this.proportionPanel = new JPanel();
/* 556 */       GridBagLayout gridBagLayout = new GridBagLayout();
/* 557 */       gridBagLayout.columnWidths = new int[3];
/* 558 */       this.proportionPanel.setLayout(gridBagLayout);
/* 559 */       this.proportionPanel.add(this.pinvProportionLabel, gridBagConstraints5);
/* 560 */       this.proportionPanel.add(getPinvSpinner(), gridBagConstraints6);
/*     */ 
/* 562 */       JLabel percentPinvLabel = new JLabel();
/* 563 */       percentPinvLabel.setText("%");
/* 564 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 565 */       gridBagConstraints.gridy = 0;
/* 566 */       gridBagConstraints.gridx = 2;
/* 567 */       this.proportionPanel.add(percentPinvLabel, gridBagConstraints);
/*     */     }
/* 569 */     return this.proportionPanel;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvEqualRadioButton()
/*     */   {
/* 578 */     if (this.pinvEqualRadioButton == null) {
/* 579 */       this.pinvEqualRadioButton = new JRadioButton();
/* 580 */       this.pinvEqualRadioButton.setText("Equal");
/* 581 */       this.pinvEqualRadioButton.setEnabled(false);
/*     */     }
/* 583 */     return this.pinvEqualRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvEstimatedRadioButton()
/*     */   {
/* 592 */     if (this.pinvEstimatedRadioButton == null) {
/* 593 */       this.pinvEstimatedRadioButton = new JRadioButton();
/* 594 */       this.pinvEstimatedRadioButton.setText("Estimated");
/* 595 */       this.pinvEstimatedRadioButton.setEnabled(false);
/*     */     }
/* 597 */     return this.pinvEstimatedRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvConstantRadioButton()
/*     */   {
/* 606 */     if (this.pinvConstantRadioButton == null) {
/* 607 */       this.pinvConstantRadioButton = new JRadioButton();
/* 608 */       this.pinvConstantRadioButton.setText("Constant");
/* 609 */       this.pinvConstantRadioButton.setEnabled(false);
/* 610 */       this.pinvConstantRadioButton.setSelected(true);
/*     */     }
/* 612 */     return this.pinvConstantRadioButton;
/*     */   }
/*     */ 
/*     */   private JScrollPane getMatrixPanel()
/*     */   {
/* 621 */     if (this.matrixPanel == null) {
/* 622 */       this.matrixPanel = new JScrollPane();
/* 623 */       this.matrixPanel.setHorizontalScrollBarPolicy(30);
/* 624 */       this.matrixPanel.setVerticalScrollBarPolicy(20);
/* 625 */       this.matrixPanel.setViewportView(this.matrixNoWordWrapPanel);
/*     */     }
/* 627 */     return this.matrixPanel;
/*     */   }
/*     */ 
/*     */   private JTextPane getMatrixTextPane() {
/* 631 */     if (this.matrixTextPane == null) {
/* 632 */       this.matrixTextPane = new JTextPane();
/* 633 */       this.matrixTextPane.setBackground(Color.black);
/* 634 */       this.matrixTextPane.setFont(new Font("Courier New", 0, 12));
/* 635 */       this.matrixTextPane.setForeground(Color.green);
/* 636 */       this.matrixTextPane.setOpaque(true);
/* 637 */       this.matrixTextPane.setRequestFocusEnabled(true);
/* 638 */       this.matrixTextPane.setCaretColor(Color.black);
/* 639 */       this.matrixTextPane.setEditable(false);
/*     */     }
/* 641 */     return this.matrixTextPane;
/*     */   }
/*     */ 
/*     */   void distributionGammaRadioButton_itemStateChanged(ItemEvent e) {
/* 645 */     if (this.distributionGammaRadioButton.isSelected()) {
/* 646 */       this.distributionGammaShapeLabel.setEnabled(true);
/* 647 */       this.distributionGammaShapeTextField.setEnabled(true);
/*     */     } else {
/* 649 */       this.distributionGammaShapeTextField.setEnabled(false);
/* 650 */       this.distributionGammaShapeLabel.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   void distributionVDPRadioButton_itemStateChanged(ItemEvent e) {
/* 655 */     if (this.distributionVDPRadioButton.isSelected())
/* 656 */       this.distributionVdpSpinner.setEnabled(true);
/*     */     else
/* 658 */       this.distributionVdpSpinner.setEnabled(false);
/*     */   }
/*     */ 
/*     */   private void distanceSelection()
/*     */   {
/* 663 */     if (this.rdbtnAbsoluteNumberOf.isSelected()) {
/* 664 */       this.modelPanel.setVisible(false);
/* 665 */       this.distributionPanel.setVisible(false);
/* 666 */       getPinvPanel().setVisible(false);
/*     */     } else {
/* 668 */       this.modelPanel.setVisible(true);
/* 669 */       this.distributionPanel.setVisible(true);
/* 670 */       getPinvPanel().setVisible(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeMatrixToFile()
/*     */   {
/* 767 */     if (this.D != null) {
/* 768 */       FileDialog chooser = new FileDialog(this, "Save distance matrix to file", 1);
/* 769 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 770 */       Dimension windowSize = chooser.getSize();
/* 771 */       chooser.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 772 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 773 */       chooser.setVisible(true);
/* 774 */       if (chooser.getFile() != null) {
/* 775 */         String filename = chooser.getDirectory() + chooser.getFile();
/* 776 */         if (!filename.endsWith(".txt")) filename = filename + ".txt";
/* 777 */         File output = new File(filename);
/*     */         try {
/* 779 */           FileWriter fw = new FileWriter(output);
/* 780 */           DefaultEditorKit kit = new DefaultEditorKit();
/* 781 */           DefaultStyledDocument doc = this.D.show();
/* 782 */           kit.write(fw, doc, 0, doc.getLength());
/* 783 */           fw.close();
/*     */         } catch (Exception e) {
/* 785 */           e.printStackTrace();
/* 786 */           JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error", e), 
/* 787 */             "Error in distance file saving", 0);
/*     */         }
/*     */       }
/*     */     } else {
/* 791 */       JOptionPane.showMessageDialog(this, "You must first hit the \"compute\" button !", 
/* 792 */         "Distance matrix not computed", 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   private class ComputeDistances extends SwingWorker<WaitingLogo.Status, Object>
/*     */   {
/*     */     public ComputeDistances()
/*     */     {
/*     */     }
/*     */ 
/*     */     public WaitingLogo.Status doInBackground()
/*     */     {
/* 678 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 680 */           ComputeDistancesFrame.this.computeButton.setEnabled(false);
/* 681 */           ComputeDistancesFrame.this.btnSaveToFile.setEnabled(false);
/*     */         }
/*     */       });
/* 684 */       Parameters.DistanceModel model = Parameters.DistanceModel.JC;
/* 685 */       Parameters.StartingTreeDistribution distribution = Parameters.StartingTreeDistribution.NONE;
/* 686 */       double distributionShape = 1.0D;
/* 687 */       double pinv = 0.0D;
/* 688 */       Parameters.StartingTreePInvPi pi = Parameters.StartingTreePInvPi.ESTIMATED;
/* 689 */       if (ComputeDistancesFrame.this.rdbtnAbsoluteNumberOf.isSelected()) {
/* 690 */         model = Parameters.DistanceModel.ABSOLUTE;
/*     */       }
/* 692 */       else if (ComputeDistancesFrame.this.modelGTRRadioButton.isSelected())
/* 693 */         model = Parameters.DistanceModel.GTR;
/* 694 */       else if (ComputeDistancesFrame.this.modelGTR2RadioButton.isSelected())
/* 695 */         model = Parameters.DistanceModel.GTR2;
/* 696 */       else if (ComputeDistancesFrame.this.modelGTR20RadioButton.isSelected())
/* 697 */         model = Parameters.DistanceModel.GTR20;
/* 698 */       else if (ComputeDistancesFrame.this.modelPoissonRadioButton.isSelected())
/* 699 */         model = Parameters.DistanceModel.POISSON;
/* 700 */       else if (ComputeDistancesFrame.this.modelTN93RadioButton.isSelected())
/* 701 */         model = Parameters.DistanceModel.TN93;
/* 702 */       else if (ComputeDistancesFrame.this.modelHKY85RadioButton.isSelected())
/* 703 */         model = Parameters.DistanceModel.HKY85;
/* 704 */       else if (ComputeDistancesFrame.this.modelK2PRadioButton.isSelected())
/* 705 */         model = Parameters.DistanceModel.K2P;
/* 706 */       else if (ComputeDistancesFrame.this.modelJCRadioButton.isSelected())
/* 707 */         model = Parameters.DistanceModel.JC;
/* 708 */       else if (ComputeDistancesFrame.this.modelNoneRadioButton.isSelected()) {
/* 709 */         model = Parameters.DistanceModel.UNCORRECTED;
/*     */       }
/*     */ 
/* 712 */       if (ComputeDistancesFrame.this.distributionNoneRadioButton.isSelected()) {
/* 713 */         distribution = Parameters.StartingTreeDistribution.NONE;
/* 714 */       } else if (ComputeDistancesFrame.this.distributionGammaRadioButton.isSelected()) {
/* 715 */         distribution = Parameters.StartingTreeDistribution.GAMMA;
/* 716 */         distributionShape = new Double(ComputeDistancesFrame.this.distributionGammaShapeTextField.getText()).doubleValue();
/* 717 */       } else if (ComputeDistancesFrame.this.distributionVDPRadioButton.isSelected()) {
/* 718 */         distribution = Parameters.StartingTreeDistribution.VDP;
/* 719 */         distributionShape = new Integer(ComputeDistancesFrame.this.distributionVdpSpinner.getModel().getValue().toString()).intValue();
/*     */       }
/* 721 */       if (ComputeDistancesFrame.this.pinvValueRadioButton.isSelected())
/* 722 */         pinv = Double.parseDouble(ComputeDistancesFrame.this.pinvSpinner.getValue().toString()) / 100.0D;
/* 723 */       else if (ComputeDistancesFrame.this.pinvNoneRadioButton.isSelected()) {
/* 724 */         pinv = 0.0D;
/*     */       }
/* 726 */       if (ComputeDistancesFrame.this.pinvEqualRadioButton.isSelected())
/* 727 */         pi = Parameters.StartingTreePInvPi.EQUAL;
/* 728 */       else if (ComputeDistancesFrame.this.pinvEstimatedRadioButton.isSelected())
/* 729 */         pi = Parameters.StartingTreePInvPi.ESTIMATED;
/* 730 */       else if (ComputeDistancesFrame.this.pinvConstantRadioButton.isSelected()) {
/* 731 */         pi = Parameters.StartingTreePInvPi.CONSTANT;
/*     */       }
/* 733 */       ProgressHandling progress = MetaPIGA.progressHandling;
/* 734 */       progress.newIndeterminateProgress("Computing distances");
/*     */       try {
/* 736 */         ComputeDistancesFrame.this.D = ComputeDistancesFrame.this.P.dataset.getDistanceMatrix(model, distribution, distributionShape, pinv, pi);
/*     */       } catch (Exception e) {
/* 738 */         e.printStackTrace();
/* 739 */         JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot compute distances", e), "Distance computation Error", 0);
/*     */       }
/*     */       try {
/* 742 */         ComputeDistancesFrame.this.getMatrixTextPane().setStyledDocument(ComputeDistancesFrame.this.D.show());
/*     */       } catch (Exception e) {
/* 744 */         ComputeDistancesFrame.this.getMatrixTextPane().setText(Tools.getErrorMessage(e));
/*     */       }
/* 746 */       return WaitingLogo.Status.COMPUTING_DISTANCES_DONE;
/*     */     }
/*     */ 
/*     */     public void done() {
/*     */       try {
/* 751 */         ComputeDistancesFrame.this.mainFrame.setAllEnabled(ComputeDistancesFrame.this.mainFrame, (WaitingLogo.Status)get());
/*     */       } catch (ExecutionException e) {
/* 753 */         e.getCause().printStackTrace();
/*     */       } catch (InterruptedException e) {
/* 755 */         e.printStackTrace();
/*     */       }
/* 757 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 759 */           ComputeDistancesFrame.this.computeButton.setEnabled(true);
/* 760 */           ComputeDistancesFrame.this.btnSaveToFile.setEnabled(true);
/*     */         }
/*     */       });
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.ComputeDistancesFrame
 * JD-Core Version:    0.6.2
 */