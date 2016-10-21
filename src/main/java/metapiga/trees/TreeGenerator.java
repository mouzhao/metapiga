/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusAdapter;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Set;
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
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.SpinnerModel;
/*     */ import javax.swing.SpinnerNumberModel;
/*     */ import javax.swing.SwingWorker;
/*     */ import javax.swing.UIManager;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.ProgressHandling;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.monitors.InactiveMonitor;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.DistanceModel;
/*     */ import metapiga.parameters.Parameters.StartingTreeDistribution;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.parameters.Parameters.StartingTreePInvPi;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class TreeGenerator extends JFrame
/*     */ {
/*     */   private final MainFrame mainFrame;
/*     */   private final Parameters P;
/*  58 */   private JPanel evaluationPanel = new JPanel();
/*  59 */   private GridBagLayout gridBagLayout2 = new GridBagLayout();
/*  60 */   private JPanel treeGenerationPanel = new JPanel();
/*     */   private TitledBorder titledBorder1;
/*     */   private TitledBorder titledBorder2;
/*     */   private TitledBorder titledBorder3;
/*     */   private TitledBorder titledBorder4;
/*  65 */   private JPanel modelPanel = new JPanel();
/*  66 */   JRadioButton modelTN93RadioButton = new JRadioButton();
/*  67 */   JRadioButton modelK2PRadioButton = new JRadioButton();
/*  68 */   private GridBagLayout gridBagLayout28 = new GridBagLayout();
/*  69 */   JRadioButton modelGTRRadioButton = new JRadioButton();
/*  70 */   JRadioButton modelGTR2RadioButton = new JRadioButton();
/*  71 */   JRadioButton modelGTR20RadioButton = new JRadioButton();
/*  72 */   JRadioButton modelPoissonRadioButton = new JRadioButton();
/*  73 */   JRadioButton modelHKY85RadioButton = new JRadioButton();
/*  74 */   JRadioButton modelJCRadioButton = new JRadioButton();
/*  75 */   JRadioButton distributionGammaRadioButton = new JRadioButton();
/*  76 */   private JPanel distributionPanel = new JPanel();
/*  77 */   JRadioButton distributionVDPRadioButton = new JRadioButton();
/*  78 */   JRadioButton distributionNoneRadioButton = new JRadioButton();
/*  79 */   private GridBagLayout gridBagLayout24 = new GridBagLayout();
/*  80 */   private GridBagLayout gridBagLayout3 = new GridBagLayout();
/*  81 */   private ButtonGroup treeButtonGroup = new ButtonGroup();
/*  82 */   JRadioButton treeNJTRadioButton = new JRadioButton();
/*  83 */   JRadioButton treeNJTRandomRadioButton = new JRadioButton();
/*  84 */   JRadioButton treeTrueRandomRadioButton = new JRadioButton();
/*  85 */   private ButtonGroup modelButtonGroup = new ButtonGroup();
/*  86 */   private ButtonGroup distributionButtonGroup = new ButtonGroup();
/*  87 */   private ButtonGroup pinvButtonGroup = new ButtonGroup();
/*  88 */   private ButtonGroup piButtonGroup = new ButtonGroup();
/*  89 */   JRadioButton modelNoneRadioButton = new JRadioButton();
/*  90 */   JSpinner distributionVdpSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 32, 1));
/*  91 */   private JPanel pinvPanel = null;
/*  92 */   JRadioButton pinvNoneRadioButton = null;
/*  93 */   JRadioButton pinvValueRadioButton = null;
/*  94 */   JSpinner pinvSpinner = null;
/*  95 */   private JPanel gammaValuesPanel = null;
/*  96 */   private JLabel distributionGammaShapeLabel = null;
/*  97 */   JTextField distributionGammaShapeTextField = null;
/*  98 */   private JPanel proportionPanel = null;
/*  99 */   JLabel pinvProportionLabel = null;
/* 100 */   JLabel pinvPiLabel = null;
/* 101 */   JRadioButton pinvEqualRadioButton = null;
/* 102 */   JRadioButton pinvEstimatedRadioButton = null;
/* 103 */   JRadioButton pinvConstantRadioButton = null;
/* 104 */   private JPanel treeGenerationRangePanel = null;
/* 105 */   private JLabel treeGenerationRangeLabel = null;
/* 106 */   JSpinner treeGenerationRangeSpinner = null;
/* 107 */   final JSpinner numberOfTreesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
/* 108 */   private final JPanel outgroupPanel = new JPanel();
/* 109 */   private final JTextArea txtrUseDatasetSettings = new JTextArea();
/* 110 */   private JScrollPane evaluationScroll = new JScrollPane();
/*     */ 
/*     */   public TreeGenerator(MainFrame main, Parameters parameters) {
/* 113 */     this.mainFrame = main;
/* 114 */     this.P = parameters;
/* 115 */     setTitle("Generate tree using dataset " + this.P.label);
/* 116 */     setDataType();
/* 117 */     GridBagConstraints gridBagConstraints11 = new GridBagConstraints(0, 2, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 0, 0), 0, 0);
/* 118 */     gridBagConstraints11.gridy = 3;
/* 119 */     GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
/* 120 */     gridBagConstraints9.gridx = 0;
/* 121 */     gridBagConstraints9.anchor = 17;
/* 122 */     gridBagConstraints9.insets = new Insets(0, 0, 0, 0);
/* 123 */     gridBagConstraints9.gridy = 2;
/* 124 */     GridBagConstraints gridBagConstraints12 = new GridBagConstraints(0, 3, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 0, 0), 0, 0);
/* 125 */     gridBagConstraints12.gridy = 4;
/* 126 */     GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
/* 127 */     gridBagConstraints10.gridx = 0;
/* 128 */     gridBagConstraints10.fill = 2;
/* 129 */     gridBagConstraints10.weightx = 1.0D;
/* 130 */     gridBagConstraints10.gridy = 2;
/* 131 */     GridBagConstraints gridBagConstraints1 = new GridBagConstraints(0, 4, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(0, 25, 0, 0), 0, 0);
/* 132 */     gridBagConstraints1.insets = new Insets(0, 25, 0, 20);
/* 133 */     gridBagConstraints1.gridy = 5;
/* 134 */     gridBagConstraints1.fill = 2;
/* 135 */     GridBagConstraints gridBagConstraints3 = new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 11, 2, new Insets(0, 5, 5, 5), 0, 0);
/* 136 */     gridBagConstraints3.gridheight = 1;
/* 137 */     GridBagConstraints gridBagConstraints2 = new GridBagConstraints(2, 0, 1, 1, 1.0D, 1.0D, 11, 2, new Insets(0, 0, 5, 5), 0, 0);
/* 138 */     gridBagConstraints2.weighty = 0.0D;
/* 139 */     GridBagConstraints gridBagConstraints30 = new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0);
/* 140 */     gridBagConstraints30.gridx = 3;
/* 141 */     gridBagConstraints30.anchor = 11;
/* 142 */     gridBagConstraints30.insets = new Insets(0, 0, 5, 5);
/* 143 */     gridBagConstraints30.fill = 2;
/* 144 */     gridBagConstraints30.weightx = 1.0D;
/* 145 */     gridBagConstraints30.gridy = 0;
/* 146 */     GridBagConstraints gridBagConstraints29 = new GridBagConstraints(1, 0, 1, 1, 1.0D, 1.0D, 11, 2, new Insets(0, 0, 5, 5), 0, 0);
/* 147 */     gridBagConstraints29.gridx = 1;
/* 148 */     gridBagConstraints29.weighty = 0.0D;
/* 149 */     gridBagConstraints29.fill = 2;
/* 150 */     gridBagConstraints29.anchor = 11;
/* 151 */     gridBagConstraints29.gridy = 0;
/* 152 */     this.titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Tree generation");
/* 153 */     this.titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Distance matrix");
/* 154 */     this.titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Distribution");
/* 155 */     this.titledBorder4 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Outgroup");
/* 156 */     this.gridBagLayout2.rowWeights = new double[] { 0.0D, 1.0D };
/* 157 */     this.gridBagLayout2.columnWeights = new double[] { 1.0D, 0.0D, 0.0D, 0.0D };
/* 158 */     this.evaluationPanel.setLayout(this.gridBagLayout2);
/* 159 */     this.evaluationScroll.setViewportView(this.evaluationPanel);
/* 160 */     getContentPane().add(this.evaluationScroll);
/* 161 */     this.treeGenerationPanel.setBorder(this.titledBorder1);
/* 162 */     this.treeGenerationPanel.setLayout(this.gridBagLayout3);
/* 163 */     this.modelPanel.setLayout(this.gridBagLayout28);
/* 164 */     this.modelTN93RadioButton.setText("TN93");
/* 165 */     this.modelK2PRadioButton.setToolTipText("");
/* 166 */     this.modelK2PRadioButton.setText("K2P");
/* 167 */     this.modelGTRRadioButton.setText("GTR");
/* 168 */     this.modelGTR2RadioButton.setText("GTR2");
/* 169 */     this.modelGTR20RadioButton.setText("GTR20");
/* 170 */     this.modelPoissonRadioButton.setText("Poisson");
/* 171 */     this.modelHKY85RadioButton.setText("HKY85");
/* 172 */     this.modelJCRadioButton.setText("JC");
/* 173 */     this.distributionGammaRadioButton.setText("Discrete Gamma");
/* 174 */     this.distributionGammaRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 176 */         TreeGenerator.this.distributionGammaRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 179 */     this.distributionPanel.setLayout(this.gridBagLayout24);
/* 180 */     this.distributionVDPRadioButton.setText("Van de Peer");
/* 181 */     this.distributionVDPRadioButton.setVisible(false);
/* 182 */     this.distributionVDPRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 184 */         TreeGenerator.this.distributionVDPRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 187 */     this.distributionNoneRadioButton.setSelected(true);
/* 188 */     this.distributionNoneRadioButton.setText("None");
/* 189 */     this.modelPanel.setBorder(this.titledBorder2);
/* 190 */     this.distributionPanel.setBorder(this.titledBorder3);
/* 191 */     this.treeNJTRadioButton.setSelected(true);
/* 192 */     this.treeNJTRadioButton.setText(Parameters.StartingTreeGeneration.NJ.verbose());
/* 193 */     this.treeNJTRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 195 */         TreeGenerator.this.treeNJTRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 198 */     this.treeNJTRandomRadioButton.setText(Parameters.StartingTreeGeneration.LNJ.verbose());
/* 199 */     this.treeNJTRandomRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 201 */         TreeGenerator.this.treeNJTRandomRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 204 */     this.treeTrueRandomRadioButton.setText(Parameters.StartingTreeGeneration.RANDOM.verbose());
/* 205 */     this.treeTrueRandomRadioButton.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent e) {
/* 207 */         TreeGenerator.this.treeTrueRandomRadioButton_itemStateChanged(e);
/*     */       }
/*     */     });
/* 210 */     this.modelNoneRadioButton.setEnabled(false);
/* 211 */     this.modelNoneRadioButton.setText("None");
/* 212 */     this.distributionVdpSpinner.setEnabled(false);
/* 213 */     this.distributionVdpSpinner.setPreferredSize(new Dimension(70, 18));
/* 214 */     this.distributionVdpSpinner.setVisible(false);
/* 215 */     this.modelPanel.add(this.modelGTRRadioButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 
/* 216 */       18, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 217 */     this.modelPanel.add(this.modelTN93RadioButton, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, 
/* 218 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 219 */     this.modelPanel.add(this.modelHKY85RadioButton, new GridBagConstraints(0, 2, 1, 1, 1.0D, 1.0D, 
/* 220 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 221 */     this.modelPanel.add(this.modelK2PRadioButton, new GridBagConstraints(0, 3, 1, 1, 1.0D, 1.0D, 
/* 222 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 223 */     this.modelPanel.add(this.modelJCRadioButton, new GridBagConstraints(0, 4, 1, 1, 1.0D, 1.0D, 
/* 224 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 225 */     this.modelPanel.add(this.modelGTR2RadioButton, new GridBagConstraints(0, 5, 1, 1, 1.0D, 1.0D, 
/* 226 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 227 */     this.modelPanel.add(this.modelGTR20RadioButton, new GridBagConstraints(0, 6, 1, 1, 1.0D, 1.0D, 
/* 228 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 229 */     this.modelPanel.add(this.modelPoissonRadioButton, new GridBagConstraints(0, 7, 1, 1, 1.0D, 1.0D, 
/* 230 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 231 */     this.modelPanel.add(this.modelNoneRadioButton, new GridBagConstraints(0, 8, 1, 1, 1.0D, 1.0D, 
/* 232 */       17, 1, new Insets(0, 0, 0, 0), 0, 0));
/* 233 */     this.evaluationPanel.add(this.treeGenerationPanel, gridBagConstraints3);
/* 234 */     this.evaluationPanel.add(this.modelPanel, gridBagConstraints29);
/* 235 */     this.evaluationPanel.add(this.distributionPanel, gridBagConstraints2);
/* 236 */     this.evaluationPanel.add(getPinvPanel(), gridBagConstraints30);
/* 237 */     this.distributionPanel.add(this.distributionNoneRadioButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 238 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 239 */     this.distributionPanel.add(this.distributionGammaRadioButton, new GridBagConstraints(0, 1, 1, 1, 1.0D, 0.0D, 
/* 240 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 241 */     this.distributionPanel.add(this.distributionVDPRadioButton, gridBagConstraints12);
/* 242 */     this.distributionPanel.add(this.distributionVdpSpinner, gridBagConstraints1);
/* 243 */     this.distributionPanel.add(getGammaValuesPanel(), gridBagConstraints10);
/* 244 */     this.treeGenerationPanel.add(this.treeNJTRadioButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 245 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 246 */     this.treeGenerationPanel.add(this.treeNJTRandomRadioButton, new GridBagConstraints(0, 1, 1, 1, 1.0D, 0.0D, 
/* 247 */       17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 248 */     this.treeGenerationPanel.add(this.treeTrueRandomRadioButton, gridBagConstraints11);
/* 249 */     this.treeGenerationPanel.add(getTreeGenerationRangePanel(), gridBagConstraints9);
/* 250 */     this.treeButtonGroup.add(this.treeNJTRadioButton);
/* 251 */     this.treeButtonGroup.add(this.treeNJTRandomRadioButton);
/* 252 */     this.treeButtonGroup.add(this.treeTrueRandomRadioButton);
/* 253 */     this.modelButtonGroup.add(this.modelGTRRadioButton);
/* 254 */     this.modelButtonGroup.add(this.modelTN93RadioButton);
/* 255 */     this.modelButtonGroup.add(this.modelHKY85RadioButton);
/* 256 */     this.modelButtonGroup.add(this.modelK2PRadioButton);
/* 257 */     this.modelButtonGroup.add(this.modelJCRadioButton);
/* 258 */     this.modelButtonGroup.add(this.modelGTR2RadioButton);
/* 259 */     this.modelButtonGroup.add(this.modelGTR20RadioButton);
/* 260 */     this.modelButtonGroup.add(this.modelPoissonRadioButton);
/* 261 */     this.modelButtonGroup.add(this.modelNoneRadioButton);
/* 262 */     this.distributionButtonGroup.add(this.distributionNoneRadioButton);
/* 263 */     this.distributionButtonGroup.add(this.distributionGammaRadioButton);
/* 264 */     this.distributionButtonGroup.add(this.distributionVDPRadioButton);
/*     */ 
/* 266 */     this.pinvButtonGroup.add(this.pinvNoneRadioButton);
/* 267 */     this.pinvButtonGroup.add(this.pinvValueRadioButton);
/* 268 */     this.piButtonGroup.add(this.pinvEqualRadioButton);
/* 269 */     this.piButtonGroup.add(this.pinvEstimatedRadioButton);
/* 270 */     this.piButtonGroup.add(this.pinvConstantRadioButton);
/*     */ 
/* 272 */     this.outgroupPanel.setBorder(this.titledBorder4);
/* 273 */     GridBagConstraints gbc_outgroupPanel = new GridBagConstraints();
/* 274 */     gbc_outgroupPanel.fill = 1;
/* 275 */     gbc_outgroupPanel.gridwidth = 4;
/* 276 */     gbc_outgroupPanel.anchor = 11;
/* 277 */     gbc_outgroupPanel.insets = new Insets(0, 5, 0, 5);
/* 278 */     gbc_outgroupPanel.gridx = 0;
/* 279 */     gbc_outgroupPanel.gridy = 1;
/* 280 */     this.evaluationPanel.add(this.outgroupPanel, gbc_outgroupPanel);
/* 281 */     this.outgroupPanel.setLayout(new BorderLayout(0, 0));
/* 282 */     this.txtrUseDatasetSettings.setLineWrap(true);
/* 283 */     this.txtrUseDatasetSettings.setFont(new Font("Tahoma", 0, 11));
/* 284 */     this.txtrUseDatasetSettings.setWrapStyleWord(true);
/* 285 */     String outgroup = "Use dataset settings to change outgroup and excluded taxa.";
/* 286 */     if (this.P.outgroup.size() > 0) {
/* 287 */       outgroup = outgroup + " Current outgroup: ";
/* 288 */       for (Iterator it = this.P.outgroup.iterator(); it.hasNext(); ) {
/* 289 */         outgroup = outgroup + (String)it.next();
/* 290 */         if (it.hasNext()) outgroup = outgroup + ", ";
/*     */       }
/* 292 */       outgroup = outgroup + ".";
/*     */     } else {
/* 294 */       outgroup = outgroup + " No outgroup is currently defined.";
/*     */     }
/* 296 */     this.txtrUseDatasetSettings.setText(outgroup);
/* 297 */     this.txtrUseDatasetSettings.setBackground(UIManager.getColor("Panel.background"));
/* 298 */     this.txtrUseDatasetSettings.setEditable(false);
/*     */ 
/* 300 */     this.outgroupPanel.add(this.txtrUseDatasetSettings, "North");
/*     */ 
/* 302 */     JPanel panel = new JPanel();
/* 303 */     getContentPane().add(panel, "South");
/*     */ 
/* 305 */     JButton generateTreeButton = new JButton();
/* 306 */     generateTreeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 308 */         TreeGenerator.this.dispose();
/* 309 */         TreeGenerator.this.mainFrame.setAllEnabled(null, WaitingLogo.Status.TREE_GENERATION);
/* 310 */         TreeGenerator.GenerateTrees save = new TreeGenerator.GenerateTrees(TreeGenerator.this, Integer.parseInt(TreeGenerator.this.numberOfTreesSpinner.getModel().getValue().toString()));
/* 311 */         save.execute();
/*     */       }
/*     */     });
/* 314 */     generateTreeButton.setText("Generate");
/* 315 */     panel.add(generateTreeButton);
/*     */ 
/* 317 */     this.numberOfTreesSpinner.setPreferredSize(new Dimension(70, 18));
/* 318 */     panel.add(this.numberOfTreesSpinner);
/*     */ 
/* 320 */     JLabel treesLabel = new JLabel();
/* 321 */     treesLabel.setText("trees");
/* 322 */     panel.add(treesLabel);
/*     */   }
/*     */ 
/*     */   private void setDataType()
/*     */   {
/* 327 */     switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.P.dataset.getDataType().ordinal()]) {
/*     */     case 1:
/* 329 */       this.modelTN93RadioButton.setVisible(true);
/* 330 */       this.modelK2PRadioButton.setVisible(true);
/* 331 */       this.modelGTRRadioButton.setVisible(true);
/* 332 */       this.modelHKY85RadioButton.setVisible(true);
/* 333 */       this.modelJCRadioButton.setVisible(true);
/* 334 */       this.modelGTR20RadioButton.setVisible(false);
/* 335 */       this.modelGTR2RadioButton.setVisible(false);
/* 336 */       this.modelPoissonRadioButton.setVisible(false);
/* 337 */       this.modelJCRadioButton.setSelected(true);
/* 338 */       break;
/*     */     case 2:
/* 340 */       this.modelTN93RadioButton.setVisible(false);
/* 341 */       this.modelK2PRadioButton.setVisible(false);
/* 342 */       this.modelGTRRadioButton.setVisible(false);
/* 343 */       this.modelHKY85RadioButton.setVisible(false);
/* 344 */       this.modelJCRadioButton.setVisible(false);
/* 345 */       this.modelGTR20RadioButton.setVisible(true);
/* 346 */       this.modelGTR2RadioButton.setVisible(false);
/* 347 */       this.modelPoissonRadioButton.setVisible(true);
/* 348 */       this.modelPoissonRadioButton.setSelected(true);
/* 349 */       break;
/*     */     case 3:
/* 351 */       this.modelTN93RadioButton.setVisible(false);
/* 352 */       this.modelK2PRadioButton.setVisible(false);
/* 353 */       this.modelGTRRadioButton.setVisible(false);
/* 354 */       this.modelHKY85RadioButton.setVisible(false);
/* 355 */       this.modelJCRadioButton.setVisible(false);
/* 356 */       this.modelGTR20RadioButton.setVisible(false);
/* 357 */       this.modelGTR2RadioButton.setVisible(true);
/* 358 */       this.modelPoissonRadioButton.setVisible(false);
/* 359 */       this.modelGTR2RadioButton.setSelected(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   private JPanel getPinvPanel()
/*     */   {
/* 370 */     if (this.pinvPanel == null) {
/* 371 */       GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
/* 372 */       gridBagConstraints51.gridx = 0;
/* 373 */       gridBagConstraints51.insets = new Insets(0, 25, 0, 0);
/* 374 */       gridBagConstraints51.fill = 2;
/* 375 */       gridBagConstraints51.gridy = 6;
/* 376 */       GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
/* 377 */       gridBagConstraints41.gridx = 0;
/* 378 */       gridBagConstraints41.insets = new Insets(0, 25, 0, 0);
/* 379 */       gridBagConstraints41.fill = 2;
/* 380 */       gridBagConstraints41.gridy = 5;
/* 381 */       GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
/* 382 */       gridBagConstraints31.gridx = 0;
/* 383 */       gridBagConstraints31.fill = 2;
/* 384 */       gridBagConstraints31.insets = new Insets(0, 25, 0, 0);
/* 385 */       gridBagConstraints31.gridy = 4;
/* 386 */       GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
/* 387 */       gridBagConstraints21.gridx = 0;
/* 388 */       gridBagConstraints21.insets = new Insets(5, 20, 0, 0);
/* 389 */       gridBagConstraints21.fill = 2;
/* 390 */       gridBagConstraints21.weightx = 0.0D;
/* 391 */       gridBagConstraints21.gridy = 3;
/* 392 */       this.pinvPiLabel = new JLabel();
/* 393 */       this.pinvPiLabel.setText("Base composition");
/* 394 */       this.pinvPiLabel.setEnabled(false);
/* 395 */       this.pinvPiLabel.setDisplayedMnemonic(0);
/* 396 */       GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
/* 397 */       gridBagConstraints13.gridx = 0;
/* 398 */       gridBagConstraints13.anchor = 17;
/* 399 */       gridBagConstraints13.gridy = 2;
/* 400 */       GridBagConstraints gridBagConstraints = new GridBagConstraints(0, 8, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 20, 0, 0), 0, 0);
/* 401 */       gridBagConstraints.insets = new Insets(0, 0, 0, 0);
/* 402 */       gridBagConstraints.gridy = 1;
/* 403 */       gridBagConstraints.gridx = 0;
/* 404 */       this.pinvPanel = new JPanel();
/* 405 */       this.pinvPanel.setLayout(new GridBagLayout());
/* 406 */       this.pinvPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Invariable sites"));
/* 407 */       this.pinvPanel.add(getPinvNoneRadioButton(), new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 0, 0), 0, 0));
/* 408 */       this.pinvPanel.add(getPinvValueRadioButton(), gridBagConstraints);
/* 409 */       this.pinvPanel.add(getProportionPanel(), gridBagConstraints13);
/* 410 */       this.pinvPanel.add(this.pinvPiLabel, gridBagConstraints21);
/* 411 */       this.pinvPanel.add(getPinvEqualRadioButton(), gridBagConstraints31);
/* 412 */       this.pinvPanel.add(getPinvEstimatedRadioButton(), gridBagConstraints41);
/* 413 */       this.pinvPanel.add(getPinvConstantRadioButton(), gridBagConstraints51);
/*     */     }
/* 415 */     return this.pinvPanel;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvNoneRadioButton()
/*     */   {
/* 423 */     if (this.pinvNoneRadioButton == null) {
/* 424 */       this.pinvNoneRadioButton = new JRadioButton();
/* 425 */       this.pinvNoneRadioButton.setSelected(true);
/* 426 */       this.pinvNoneRadioButton.setText("None");
/*     */     }
/* 428 */     return this.pinvNoneRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvValueRadioButton()
/*     */   {
/* 436 */     if (this.pinvValueRadioButton == null) {
/* 437 */       this.pinvValueRadioButton = new JRadioButton();
/* 438 */       this.pinvValueRadioButton.setText("P-Invariant");
/* 439 */       this.pinvValueRadioButton.addItemListener(new ItemListener() {
/*     */         public void itemStateChanged(ItemEvent e) {
/* 441 */           if (TreeGenerator.this.pinvValueRadioButton.isSelected()) {
/* 442 */             TreeGenerator.this.pinvSpinner.setEnabled(true);
/* 443 */             TreeGenerator.this.pinvProportionLabel.setEnabled(true);
/* 444 */             TreeGenerator.this.pinvPiLabel.setEnabled(true);
/* 445 */             TreeGenerator.this.pinvConstantRadioButton.setEnabled(true);
/* 446 */             TreeGenerator.this.pinvEstimatedRadioButton.setEnabled(true);
/* 447 */             TreeGenerator.this.pinvEqualRadioButton.setEnabled(true);
/*     */           } else {
/* 449 */             TreeGenerator.this.pinvSpinner.setEnabled(false);
/* 450 */             TreeGenerator.this.pinvProportionLabel.setEnabled(false);
/* 451 */             TreeGenerator.this.pinvPiLabel.setEnabled(false);
/* 452 */             TreeGenerator.this.pinvConstantRadioButton.setEnabled(false);
/* 453 */             TreeGenerator.this.pinvEstimatedRadioButton.setEnabled(false);
/* 454 */             TreeGenerator.this.pinvEqualRadioButton.setEnabled(false);
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 459 */     return this.pinvValueRadioButton;
/*     */   }
/*     */ 
/*     */   private JSpinner getPinvSpinner()
/*     */   {
/* 468 */     if (this.pinvSpinner == null) {
/* 469 */       this.pinvSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
/* 470 */       this.pinvSpinner.setEnabled(false);
/* 471 */       this.pinvSpinner.setPreferredSize(new Dimension(70, 18));
/*     */     }
/* 473 */     return this.pinvSpinner;
/*     */   }
/*     */ 
/*     */   private JPanel getGammaValuesPanel()
/*     */   {
/* 482 */     if (this.gammaValuesPanel == null) {
/* 483 */       GridBagConstraints gridBagConstraints8 = new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 5, 0, 5), 0, 0);
/* 484 */       gridBagConstraints8.fill = 2;
/* 485 */       gridBagConstraints8.gridy = 1;
/* 486 */       gridBagConstraints8.weightx = 1.0D;
/* 487 */       gridBagConstraints8.ipadx = 0;
/* 488 */       gridBagConstraints8.insets = new Insets(0, 5, 0, 20);
/* 489 */       GridBagConstraints gridBagConstraints7 = new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 5, 0, 5), 0, 0);
/* 490 */       gridBagConstraints7.anchor = 17;
/* 491 */       gridBagConstraints7.gridy = 1;
/* 492 */       gridBagConstraints7.insets = new Insets(0, 25, 0, 5);
/* 493 */       this.distributionGammaShapeLabel = new JLabel();
/* 494 */       this.distributionGammaShapeLabel.setEnabled(false);
/* 495 */       this.distributionGammaShapeLabel.setPreferredSize(new Dimension(40, 15));
/* 496 */       this.distributionGammaShapeLabel.setText("shape");
/* 497 */       this.distributionGammaShapeLabel.setMaximumSize(new Dimension(260, 15));
/* 498 */       this.gammaValuesPanel = new JPanel();
/* 499 */       this.gammaValuesPanel.setLayout(new GridBagLayout());
/* 500 */       this.gammaValuesPanel.add(this.distributionGammaShapeLabel, gridBagConstraints7);
/* 501 */       this.gammaValuesPanel.add(getDistributionGammaStartTextField(), gridBagConstraints8);
/*     */     }
/* 503 */     return this.gammaValuesPanel;
/*     */   }
/*     */ 
/*     */   private JTextField getDistributionGammaStartTextField()
/*     */   {
/* 512 */     if (this.distributionGammaShapeTextField == null) {
/* 513 */       this.distributionGammaShapeTextField = new JTextField();
/* 514 */       this.distributionGammaShapeTextField.setEnabled(false);
/* 515 */       this.distributionGammaShapeTextField.setText("0.5");
/* 516 */       this.distributionGammaShapeTextField.setPreferredSize(new Dimension(70, 20));
/* 517 */       this.distributionGammaShapeTextField
/* 518 */         .addFocusListener(new FocusAdapter() {
/*     */         public void focusLost(FocusEvent e) {
/*     */           try {
/* 521 */             Double d = Double.valueOf(Double.parseDouble(TreeGenerator.this.distributionGammaShapeTextField.getText()));
/* 522 */             if (d.doubleValue() <= 0.0D) throw new NumberFormatException(d + " is a not a non-zero positive number"); 
/*     */           }
/* 524 */           catch (NumberFormatException ex) { ex.printStackTrace();
/* 525 */             JOptionPane.showMessageDialog(null, "Error : " + TreeGenerator.this.distributionGammaShapeTextField.getText() + " is not a valid positive number. \nShape parameter is reset to 0.5.", "Gamma shape parameter", 0);
/* 526 */             TreeGenerator.this.distributionGammaShapeTextField.setText("0.5");
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 531 */     return this.distributionGammaShapeTextField;
/*     */   }
/*     */ 
/*     */   private JPanel getProportionPanel()
/*     */   {
/* 540 */     if (this.proportionPanel == null) {
/* 541 */       this.pinvProportionLabel = new JLabel();
/* 542 */       this.pinvProportionLabel.setText("Proportion");
/* 543 */       this.pinvProportionLabel.setEnabled(false);
/* 544 */       GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
/* 545 */       gridBagConstraints5.gridx = 0;
/* 546 */       gridBagConstraints5.insets = new Insets(0, 20, 0, 5);
/* 547 */       gridBagConstraints5.gridy = 0;
/* 548 */       GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
/* 549 */       gridBagConstraints6.fill = 2;
/* 550 */       gridBagConstraints6.gridx = 1;
/* 551 */       gridBagConstraints6.gridy = 0;
/* 552 */       gridBagConstraints6.insets = new Insets(0, 0, 0, 5);
/* 553 */       this.proportionPanel = new JPanel();
/* 554 */       GridBagLayout gridBagLayout = new GridBagLayout();
/* 555 */       gridBagLayout.columnWidths = new int[3];
/* 556 */       this.proportionPanel.setLayout(gridBagLayout);
/* 557 */       this.proportionPanel.add(this.pinvProportionLabel, gridBagConstraints5);
/* 558 */       this.proportionPanel.add(getPinvSpinner(), gridBagConstraints6);
/*     */ 
/* 560 */       JLabel percentPinvLabel = new JLabel();
/* 561 */       percentPinvLabel.setText("%");
/* 562 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 563 */       gridBagConstraints.gridy = 0;
/* 564 */       gridBagConstraints.gridx = 2;
/* 565 */       this.proportionPanel.add(percentPinvLabel, gridBagConstraints);
/*     */     }
/* 567 */     return this.proportionPanel;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvEqualRadioButton()
/*     */   {
/* 576 */     if (this.pinvEqualRadioButton == null) {
/* 577 */       this.pinvEqualRadioButton = new JRadioButton();
/* 578 */       this.pinvEqualRadioButton.setText("Equal");
/* 579 */       this.pinvEqualRadioButton.setEnabled(false);
/*     */     }
/* 581 */     return this.pinvEqualRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvEstimatedRadioButton()
/*     */   {
/* 590 */     if (this.pinvEstimatedRadioButton == null) {
/* 591 */       this.pinvEstimatedRadioButton = new JRadioButton();
/* 592 */       this.pinvEstimatedRadioButton.setText("Estimated");
/* 593 */       this.pinvEstimatedRadioButton.setEnabled(false);
/*     */     }
/* 595 */     return this.pinvEstimatedRadioButton;
/*     */   }
/*     */ 
/*     */   private JRadioButton getPinvConstantRadioButton()
/*     */   {
/* 604 */     if (this.pinvConstantRadioButton == null) {
/* 605 */       this.pinvConstantRadioButton = new JRadioButton();
/* 606 */       this.pinvConstantRadioButton.setText("Constant");
/* 607 */       this.pinvConstantRadioButton.setEnabled(false);
/* 608 */       this.pinvConstantRadioButton.setSelected(true);
/*     */     }
/* 610 */     return this.pinvConstantRadioButton;
/*     */   }
/*     */ 
/*     */   private JPanel getTreeGenerationRangePanel()
/*     */   {
/* 619 */     if (this.treeGenerationRangePanel == null) {
/* 620 */       GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
/* 621 */       gridBagConstraints16.gridx = 1;
/* 622 */       gridBagConstraints16.insets = new Insets(0, 5, 0, 5);
/* 623 */       gridBagConstraints16.fill = 2;
/* 624 */       gridBagConstraints16.gridy = 0;
/* 625 */       GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
/* 626 */       gridBagConstraints17.gridx = 0;
/* 627 */       gridBagConstraints17.insets = new Insets(0, 20, 0, 0);
/* 628 */       gridBagConstraints17.gridy = 0;
/* 629 */       this.treeGenerationRangeLabel = new JLabel();
/* 630 */       this.treeGenerationRangeLabel.setText("Range");
/* 631 */       this.treeGenerationRangePanel = new JPanel();
/* 632 */       GridBagLayout gridBagLayout = new GridBagLayout();
/* 633 */       gridBagLayout.columnWidths = new int[3];
/* 634 */       this.treeGenerationRangePanel.setLayout(gridBagLayout);
/* 635 */       this.treeGenerationRangePanel.add(this.treeGenerationRangeLabel, gridBagConstraints17);
/* 636 */       this.treeGenerationRangePanel.add(getTreeGenerationRangeSpinner(), gridBagConstraints16);
/*     */ 
/* 638 */       JLabel rangeLabel = new JLabel();
/* 639 */       rangeLabel.setText("%");
/* 640 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 641 */       gridBagConstraints.gridy = 0;
/* 642 */       gridBagConstraints.gridx = 2;
/* 643 */       this.treeGenerationRangePanel.add(rangeLabel, gridBagConstraints);
/*     */     }
/* 645 */     return this.treeGenerationRangePanel;
/*     */   }
/*     */ 
/*     */   private JSpinner getTreeGenerationRangeSpinner()
/*     */   {
/* 654 */     if (this.treeGenerationRangeSpinner == null) {
/* 655 */       this.treeGenerationRangeSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 5));
/* 656 */       this.treeGenerationRangeSpinner.setEnabled(false);
/* 657 */       this.treeGenerationRangeSpinner.setPreferredSize(new Dimension(70, 18));
/*     */     }
/* 659 */     return this.treeGenerationRangeSpinner;
/*     */   }
/*     */ 
/*     */   void disableModelOnCriterion() {
/* 663 */     if (!this.treeTrueRandomRadioButton.isSelected()) {
/* 664 */       this.modelGTRRadioButton.setEnabled(true);
/* 665 */       this.modelHKY85RadioButton.setEnabled(true);
/* 666 */       this.modelTN93RadioButton.setEnabled(true);
/* 667 */       this.modelJCRadioButton.setEnabled(true);
/* 668 */       this.modelK2PRadioButton.setEnabled(true);
/* 669 */       this.modelNoneRadioButton.setEnabled(false);
/* 670 */       if (this.modelNoneRadioButton.isSelected()) {
/* 671 */         this.modelJCRadioButton.setSelected(true);
/*     */       }
/* 673 */       this.distributionGammaRadioButton.setEnabled(true);
/* 674 */       this.distributionVDPRadioButton.setEnabled(true);
/* 675 */       this.pinvValueRadioButton.setEnabled(true);
/* 676 */       if (this.pinvValueRadioButton.isSelected()) this.pinvSpinner.setEnabled(true); 
/*     */     }
/* 678 */     else { this.modelGTRRadioButton.setEnabled(false);
/* 679 */       this.modelHKY85RadioButton.setEnabled(false);
/* 680 */       this.modelTN93RadioButton.setEnabled(false);
/* 681 */       this.modelJCRadioButton.setEnabled(false);
/* 682 */       this.modelK2PRadioButton.setEnabled(false);
/* 683 */       this.modelNoneRadioButton.setEnabled(true);
/* 684 */       this.modelNoneRadioButton.setSelected(true);
/* 685 */       this.distributionGammaRadioButton.setEnabled(false);
/* 686 */       this.distributionVDPRadioButton.setEnabled(false);
/* 687 */       this.pinvValueRadioButton.setEnabled(false);
/* 688 */       this.pinvSpinner.setEnabled(false);
/* 689 */       this.distributionNoneRadioButton.setSelected(true);
/* 690 */       this.pinvNoneRadioButton.setSelected(true); }
/*     */   }
/*     */ 
/*     */   void treeNJTRadioButton_itemStateChanged(ItemEvent e)
/*     */   {
/* 695 */     disableModelOnCriterion();
/*     */   }
/*     */ 
/*     */   void treeNJTRandomRadioButton_itemStateChanged(ItemEvent e) {
/* 699 */     disableModelOnCriterion();
/* 700 */     if (this.treeNJTRandomRadioButton.isSelected()) {
/* 701 */       this.treeGenerationRangeLabel.setEnabled(true);
/* 702 */       this.treeGenerationRangeSpinner.setEnabled(true);
/*     */     } else {
/* 704 */       this.treeGenerationRangeLabel.setEnabled(false);
/* 705 */       this.treeGenerationRangeSpinner.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   void treeTrueRandomRadioButton_itemStateChanged(ItemEvent e) {
/* 710 */     disableModelOnCriterion();
/*     */   }
/*     */ 
/*     */   void distributionGammaRadioButton_itemStateChanged(ItemEvent e)
/*     */   {
/* 715 */     if (this.distributionGammaRadioButton.isSelected()) {
/* 716 */       this.distributionGammaShapeLabel.setEnabled(true);
/* 717 */       this.distributionGammaShapeTextField.setEnabled(true);
/*     */     } else {
/* 719 */       this.distributionGammaShapeTextField.setEnabled(false);
/* 720 */       this.distributionGammaShapeLabel.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   void distributionVDPRadioButton_itemStateChanged(ItemEvent e) {
/* 725 */     if (this.distributionVDPRadioButton.isSelected())
/* 726 */       this.distributionVdpSpinner.setEnabled(true);
/*     */     else
/* 728 */       this.distributionVdpSpinner.setEnabled(false);
/*     */   }
/*     */ 
/*     */   private class GenerateTrees extends SwingWorker<WaitingLogo.Status, Object> {
/*     */     private final int treeNum;
/*     */ 
/*     */     public GenerateTrees(int treeNum) {
/* 735 */       this.treeNum = treeNum;
/*     */     }
/*     */     public WaitingLogo.Status doInBackground() {
/* 738 */       Parameters.StartingTreeGeneration generation = Parameters.StartingTreeGeneration.NJ;
/* 739 */       double startingTreeRange = 0.1D;
/* 740 */       Parameters.DistanceModel model = Parameters.DistanceModel.GTR;
/* 741 */       Parameters.StartingTreeDistribution distribution = Parameters.StartingTreeDistribution.NONE;
/* 742 */       double distributionShape = 1.0D;
/* 743 */       double pinv = 0.0D;
/* 744 */       Parameters.StartingTreePInvPi pi = Parameters.StartingTreePInvPi.ESTIMATED;
/* 745 */       if (TreeGenerator.this.treeNJTRadioButton.isSelected()) {
/* 746 */         generation = Parameters.StartingTreeGeneration.NJ;
/* 747 */       } else if (TreeGenerator.this.treeNJTRandomRadioButton.isSelected()) {
/* 748 */         generation = Parameters.StartingTreeGeneration.LNJ;
/* 749 */         startingTreeRange = Double.parseDouble(TreeGenerator.this.getTreeGenerationRangeSpinner().getModel().getValue().toString()) / 100.0D;
/* 750 */       } else if (TreeGenerator.this.treeTrueRandomRadioButton.isSelected()) {
/* 751 */         generation = Parameters.StartingTreeGeneration.RANDOM;
/*     */       }
/* 753 */       if (TreeGenerator.this.modelGTRRadioButton.isSelected())
/* 754 */         model = Parameters.DistanceModel.GTR;
/* 755 */       else if (TreeGenerator.this.modelGTR2RadioButton.isSelected())
/* 756 */         model = Parameters.DistanceModel.GTR2;
/* 757 */       else if (TreeGenerator.this.modelGTR20RadioButton.isSelected())
/* 758 */         model = Parameters.DistanceModel.GTR20;
/* 759 */       else if (TreeGenerator.this.modelPoissonRadioButton.isSelected())
/* 760 */         model = Parameters.DistanceModel.POISSON;
/* 761 */       else if (TreeGenerator.this.modelTN93RadioButton.isSelected())
/* 762 */         model = Parameters.DistanceModel.TN93;
/* 763 */       else if (TreeGenerator.this.modelHKY85RadioButton.isSelected())
/* 764 */         model = Parameters.DistanceModel.HKY85;
/* 765 */       else if (TreeGenerator.this.modelK2PRadioButton.isSelected())
/* 766 */         model = Parameters.DistanceModel.K2P;
/* 767 */       else if (TreeGenerator.this.modelJCRadioButton.isSelected())
/* 768 */         model = Parameters.DistanceModel.JC;
/* 769 */       else if (TreeGenerator.this.modelNoneRadioButton.isSelected()) {
/* 770 */         model = Parameters.DistanceModel.NONE;
/*     */       }
/* 772 */       if (TreeGenerator.this.distributionNoneRadioButton.isSelected()) {
/* 773 */         distribution = Parameters.StartingTreeDistribution.NONE;
/* 774 */       } else if (TreeGenerator.this.distributionGammaRadioButton.isSelected()) {
/* 775 */         distribution = Parameters.StartingTreeDistribution.GAMMA;
/* 776 */         distributionShape = new Double(TreeGenerator.this.distributionGammaShapeTextField.getText()).doubleValue();
/* 777 */       } else if (TreeGenerator.this.distributionVDPRadioButton.isSelected()) {
/* 778 */         distribution = Parameters.StartingTreeDistribution.VDP;
/* 779 */         distributionShape = new Integer(TreeGenerator.this.distributionVdpSpinner.getModel().getValue().toString()).intValue();
/*     */       }
/* 781 */       if (TreeGenerator.this.pinvValueRadioButton.isSelected())
/* 782 */         pinv = Double.parseDouble(TreeGenerator.this.pinvSpinner.getValue().toString()) / 100.0D;
/* 783 */       else if (TreeGenerator.this.pinvNoneRadioButton.isSelected()) {
/* 784 */         pinv = 0.0D;
/*     */       }
/* 786 */       if (TreeGenerator.this.pinvEqualRadioButton.isSelected())
/* 787 */         pi = Parameters.StartingTreePInvPi.EQUAL;
/* 788 */       else if (TreeGenerator.this.pinvEstimatedRadioButton.isSelected())
/* 789 */         pi = Parameters.StartingTreePInvPi.ESTIMATED;
/* 790 */       else if (TreeGenerator.this.pinvConstantRadioButton.isSelected()) {
/* 791 */         pi = Parameters.StartingTreePInvPi.CONSTANT;
/*     */       }
/*     */ 
/* 795 */       List trees = new ArrayList();
/* 796 */       ProgressHandling progress = MetaPIGA.progressHandling;
/* 797 */       progress.newSingleProgress(0, this.treeNum, "Generating tree");
/* 798 */       for (int i = 0; i < this.treeNum; i++) {
/* 799 */         progress.setValue(i + 1);
/*     */         try {
/* 801 */           Tree tree = TreeGenerator.this.P.dataset.generateTree(TreeGenerator.this.P.outgroup, generation, startingTreeRange, model, distribution, distributionShape, pinv, pi, TreeGenerator.this.P, new InactiveMonitor());
/* 802 */           if (this.treeNum > 1) tree.setName(tree.getName() + "_" + (i + 1));
/*     */ 
/* 804 */           trees.add(tree);
/* 805 */           MetaPIGA.treeViewer.addTree(tree, TreeGenerator.this.P);
/*     */         } catch (Exception e) {
/* 807 */           e.printStackTrace();
/* 808 */           JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot build tree", e), "Tree building Error", 0);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 820 */       MetaPIGA.treeViewer.setSelectedTrees(trees);
/* 821 */       MetaPIGA.treeViewer.setVisible(true);
/* 822 */       return WaitingLogo.Status.TREE_GENERATION_DONE;
/*     */     }
/*     */ 
/*     */     public void done() {
/*     */       try {
/* 827 */         TreeGenerator.this.mainFrame.setAllEnabled(TreeGenerator.this.mainFrame, (WaitingLogo.Status)get());
/*     */       } catch (ExecutionException e) {
/* 829 */         e.getCause().printStackTrace();
/*     */       } catch (InterruptedException e) {
/* 831 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.TreeGenerator
 * JD-Core Version:    0.6.2
 */