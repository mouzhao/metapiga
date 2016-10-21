/*     */ package metapiga.trees;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class ConsensusTreeFrame extends JFrame
/*     */ {
/*  45 */   private final String available = "Available trees";
/*  46 */   private final String consensus = "Consensus";
/*     */ 
/*  48 */   private Map<String, Map<String, Tree>> availableTrees = new HashMap();
/*  49 */   private Map<String, Parameters> availableParameters = new TreeMap();
/*     */   private Parameters currentParameters;
/*  51 */   private DefaultListModel availableTreesListModel = new DefaultListModel();
/*  52 */   private DefaultListModel consensusListModel = new DefaultListModel();
/*  53 */   private boolean isAdjustingDatasetCombobox = false;
/*  54 */   private TitledBorder availableTitle = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Available trees");
/*  55 */   private TitledBorder consensusTitle = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Consensus");
/*  56 */   final JScrollPane scrollPane = new JScrollPane();
/*  57 */   final JScrollPane scrollPane_1 = new JScrollPane();
/*     */ 
/*  59 */   final JComboBox comboBox = new JComboBox();
/*  60 */   final JTextField consensusTreeTextField = new JTextField();
/*     */ 
/*     */   public ConsensusTreeFrame() {
/*  63 */     super("Consensus tree builder");
/*     */ 
/*  65 */     JPanel panel = new JPanel();
/*  66 */     GridBagLayout gridBagLayout = new GridBagLayout();
/*  67 */     gridBagLayout.columnWidths = new int[3];
/*  68 */     panel.setLayout(gridBagLayout);
/*  69 */     getContentPane().add(panel, "Center");
/*     */ 
/*  71 */     JPanel panel_4 = new JPanel();
/*  72 */     panel_4.setLayout(new BorderLayout());
/*  73 */     GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
/*  74 */     gridBagConstraints_5.fill = 1;
/*  75 */     gridBagConstraints_5.weighty = 1.0D;
/*  76 */     gridBagConstraints_5.weightx = 1.0D;
/*  77 */     gridBagConstraints_5.gridy = 0;
/*  78 */     gridBagConstraints_5.gridx = 0;
/*  79 */     panel_4.setPreferredSize(new Dimension(300, 300));
/*  80 */     panel_4.setMaximumSize(new Dimension(300, 300));
/*  81 */     panel_4.setMinimumSize(new Dimension(300, 300));
/*  82 */     panel.add(panel_4, gridBagConstraints_5);
/*     */ 
/*  84 */     panel_4.add(this.scrollPane);
/*  85 */     this.scrollPane.setBorder(this.availableTitle);
/*     */ 
/*  87 */     final JList availableList = new JList(this.availableTreesListModel);
/*  88 */     this.scrollPane.setViewportView(availableList);
/*     */ 
/*  90 */     JPanel panel_6 = new JPanel();
/*  91 */     panel_6.setLayout(new GridBagLayout());
/*  92 */     panel_4.add(panel_6, "North");
/*     */ 
/*  94 */     JButton addTreesFromButton_1 = new JButton();
/*  95 */     GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
/*  96 */     gridBagConstraints_1.insets = new Insets(5, 5, 5, 5);
/*  97 */     gridBagConstraints_1.weightx = 1.0D;
/*  98 */     gridBagConstraints_1.fill = 2;
/*  99 */     panel_6.add(addTreesFromButton_1, gridBagConstraints_1);
/* 100 */     addTreesFromButton_1.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 102 */         new Thread(new Runnable() {
/*     */           public void run() {
/* 104 */             ConsensusTreeFrame.this.addAvailableTrees();
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/* 109 */     addTreesFromButton_1.setText("update");
/*     */ 
/* 111 */     JPanel panel_5 = new JPanel();
/* 112 */     panel_5.setLayout(new BorderLayout());
/* 113 */     GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 114 */     gridBagConstraints.weighty = 1.0D;
/* 115 */     gridBagConstraints.weightx = 1.0D;
/* 116 */     gridBagConstraints.fill = 1;
/* 117 */     gridBagConstraints.gridy = 0;
/* 118 */     gridBagConstraints.gridx = 2;
/* 119 */     panel_5.setPreferredSize(new Dimension(300, 300));
/* 120 */     panel_5.setMaximumSize(new Dimension(300, 300));
/* 121 */     panel_5.setMinimumSize(new Dimension(300, 300));
/* 122 */     panel.add(panel_5, gridBagConstraints);
/*     */ 
/* 124 */     panel_5.add(this.scrollPane_1);
/* 125 */     this.scrollPane_1.setBorder(this.consensusTitle);
/*     */ 
/* 127 */     final JList consensusList = new JList(this.consensusListModel);
/* 128 */     this.scrollPane_1.setViewportView(consensusList);
/*     */ 
/* 130 */     JPanel panel_7 = new JPanel();
/* 131 */     GridBagLayout gridBagLayout_2 = new GridBagLayout();
/* 132 */     gridBagLayout_2.rowHeights = new int[2];
/* 133 */     gridBagLayout_2.columnWidths = new int[2];
/* 134 */     panel_7.setLayout(gridBagLayout_2);
/* 135 */     panel_5.add(panel_7, "North");
/*     */ 
/* 137 */     JLabel datasetLabel = new JLabel();
/* 138 */     datasetLabel.setText("Dataset");
/* 139 */     GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
/* 140 */     gridBagConstraints_9.fill = 2;
/* 141 */     gridBagConstraints_9.insets = new Insets(5, 5, 5, 5);
/* 142 */     panel_7.add(datasetLabel, gridBagConstraints_9);
/*     */ 
/* 144 */     this.comboBox.addItemListener(new ItemListener() {
/*     */       public void itemStateChanged(ItemEvent arg0) {
/* 146 */         if (!ConsensusTreeFrame.this.isAdjustingDatasetCombobox) {
/* 147 */           ConsensusTreeFrame.this.currentParameters = ((Parameters)ConsensusTreeFrame.this.availableParameters.get(ConsensusTreeFrame.this.comboBox.getSelectedItem()));
/* 148 */           ConsensusTreeFrame.this.consensusListModel.clear();
/* 149 */           ConsensusTreeFrame.this.availableTreesListModel.clear();
/* 150 */           for (String treeName : ((Map)ConsensusTreeFrame.this.availableTrees.get(ConsensusTreeFrame.this.currentParameters.label)).keySet()) {
/* 151 */             ConsensusTreeFrame.this.availableTreesListModel.addElement(treeName);
/*     */           }
/* 153 */           ConsensusTreeFrame.this.availableTitle.setTitle("Available trees (" + ConsensusTreeFrame.this.availableTreesListModel.getSize() + " trees)");
/* 154 */           ConsensusTreeFrame.this.consensusTitle.setTitle("Consensus (" + ConsensusTreeFrame.this.consensusListModel.getSize() + " trees)");
/* 155 */           ConsensusTreeFrame.this.scrollPane.repaint();
/*     */         }
/*     */       }
/*     */     });
/* 159 */     GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
/* 160 */     gridBagConstraints_6.weightx = 1.0D;
/* 161 */     gridBagConstraints_6.fill = 2;
/* 162 */     gridBagConstraints_6.insets = new Insets(5, 0, 5, 5);
/* 163 */     gridBagConstraints_6.gridy = 0;
/* 164 */     gridBagConstraints_6.gridx = 1;
/* 165 */     panel_7.add(this.comboBox, gridBagConstraints_6);
/*     */ 
/* 167 */     JLabel treeNameLabel = new JLabel();
/* 168 */     treeNameLabel.setText("Tree name");
/* 169 */     GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
/* 170 */     gridBagConstraints_7.fill = 2;
/* 171 */     gridBagConstraints_7.insets = new Insets(0, 5, 5, 5);
/* 172 */     gridBagConstraints_7.gridy = 1;
/* 173 */     gridBagConstraints_7.gridx = 0;
/* 174 */     panel_7.add(treeNameLabel, gridBagConstraints_7);
/*     */ 
/* 176 */     this.consensusTreeTextField.setText("consensus tree");
/* 177 */     GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
/* 178 */     gridBagConstraints_8.insets = new Insets(0, 0, 5, 5);
/* 179 */     gridBagConstraints_8.weightx = 1.0D;
/* 180 */     gridBagConstraints_8.fill = 2;
/* 181 */     gridBagConstraints_8.gridy = 1;
/* 182 */     gridBagConstraints_8.gridx = 1;
/* 183 */     panel_7.add(this.consensusTreeTextField, gridBagConstraints_8);
/*     */ 
/* 185 */     JPanel panel_1 = new JPanel();
/* 186 */     GridBagLayout gridBagLayout_1 = new GridBagLayout();
/* 187 */     gridBagLayout_1.rowHeights = new int[2];
/* 188 */     panel_1.setLayout(gridBagLayout_1);
/* 189 */     GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
/* 190 */     gridBagConstraints_2.gridy = 0;
/* 191 */     gridBagConstraints_2.gridx = 1;
/* 192 */     panel.add(panel_1, gridBagConstraints_2);
/*     */ 
/* 194 */     JButton button = new JButton();
/* 195 */     button.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/* 197 */         for (Object tree : availableList.getSelectedValues()) {
/* 198 */           ConsensusTreeFrame.this.consensusListModel.addElement(tree);
/* 199 */           ConsensusTreeFrame.this.availableTreesListModel.removeElement(tree);
/* 200 */           ConsensusTreeFrame.this.availableTitle.setTitle("Available trees (" + ConsensusTreeFrame.this.availableTreesListModel.getSize() + " trees)");
/* 201 */           ConsensusTreeFrame.this.consensusTitle.setTitle("Consensus (" + ConsensusTreeFrame.this.consensusListModel.getSize() + " trees)");
/* 202 */           ConsensusTreeFrame.this.scrollPane.repaint();
/* 203 */           ConsensusTreeFrame.this.scrollPane_1.repaint();
/*     */         }
/*     */       }
/*     */     });
/* 206 */     button.setText(">>");
/* 207 */     GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
/* 208 */     gridBagConstraints_4.gridy = 0;
/* 209 */     gridBagConstraints_4.gridx = 0;
/* 210 */     gridBagConstraints_4.insets = new Insets(5, 5, 5, 5);
/* 211 */     panel_1.add(button, gridBagConstraints_4);
/*     */ 
/* 213 */     JButton button_1 = new JButton();
/* 214 */     button_1.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 216 */         for (Object tree : consensusList.getSelectedValues()) {
/* 217 */           ConsensusTreeFrame.this.availableTreesListModel.addElement(tree);
/* 218 */           ConsensusTreeFrame.this.consensusListModel.removeElement(tree);
/* 219 */           ConsensusTreeFrame.this.availableTitle.setTitle("Available trees (" + ConsensusTreeFrame.this.availableTreesListModel.getSize() + " trees)");
/* 220 */           ConsensusTreeFrame.this.consensusTitle.setTitle("Consensus (" + ConsensusTreeFrame.this.consensusListModel.getSize() + " trees)");
/* 221 */           ConsensusTreeFrame.this.scrollPane.repaint();
/* 222 */           ConsensusTreeFrame.this.scrollPane_1.repaint();
/*     */         }
/*     */       }
/*     */     });
/* 226 */     button_1.setText("<<");
/* 227 */     GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
/* 228 */     gridBagConstraints_3.insets = new Insets(5, 5, 5, 5);
/* 229 */     gridBagConstraints_3.gridy = 1;
/* 230 */     gridBagConstraints_3.gridx = 0;
/* 231 */     panel_1.add(button_1, gridBagConstraints_3);
/*     */ 
/* 233 */     JPanel panel_2 = new JPanel();
/* 234 */     getContentPane().add(panel_2, "South");
/*     */ 
/* 236 */     JButton buildConsensusTreeButton = new JButton();
/* 237 */     buildConsensusTreeButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 239 */         new Thread(new Runnable() {
/*     */           public void run() {
/* 241 */             ConsensusTreeFrame.this.buildConsensus();
/*     */           }
/*     */         }).start();
/*     */       }
/*     */     });
/* 246 */     buildConsensusTreeButton.setText("Build consensus tree");
/* 247 */     panel_2.add(buildConsensusTreeButton);
/*     */ 
/* 249 */     JPanel panel_3 = new JPanel();
/* 250 */     FlowLayout flowLayout = new FlowLayout();
/* 251 */     flowLayout.setAlignment(3);
/* 252 */     panel_3.setLayout(flowLayout);
/* 253 */     getContentPane().add(panel_3, "North");
/*     */ 
/* 255 */     JLabel availableTreesComesLabel = new JLabel();
/* 256 */     availableTreesComesLabel.setText("Use the");
/* 257 */     panel_3.add(availableTreesComesLabel);
/*     */ 
/* 259 */     JButton addTreesFromButton = new JButton();
/* 260 */     addTreesFromButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent e) {
/* 262 */         MetaPIGA.treeViewer.setVisible(true);
/* 263 */         if (MetaPIGA.treeViewer.getState() != 0) MetaPIGA.treeViewer.setState(0);
/* 264 */         MetaPIGA.treeViewer.toFront();
/* 265 */         MetaPIGA.treeViewer.repaint();
/*     */       }
/*     */     });
/* 268 */     addTreesFromButton.setText("TreeViewer");
/* 269 */     panel_3.add(addTreesFromButton);
/*     */ 
/* 271 */     JLabel toLoadTreesLabel = new JLabel();
/* 272 */     toLoadTreesLabel.setText("to load trees and then");
/* 273 */     panel_3.add(toLoadTreesLabel);
/*     */ 
/* 275 */     if (this.comboBox.getItemCount() > 0) this.currentParameters = ((Parameters)this.availableParameters.get(this.comboBox.getSelectedItem()));
/* 276 */     addAvailableTrees();
/* 277 */     this.comboBox.setSelectedItem(MetaPIGA.treeViewer.getSelectedDataset());
/*     */   }
/*     */ 
/*     */   private void addAvailableTrees() {
/* 281 */     this.isAdjustingDatasetCombobox = true;
/* 282 */     Map parameters = MetaPIGA.treeViewer.getAvailableParameters();
/* 283 */     this.comboBox.removeAllItems();
/* 284 */     for (String name : parameters.keySet()) {
/* 285 */       this.availableParameters.put(name, (Parameters)parameters.get(name));
/* 286 */       if (!this.availableTrees.containsKey(name)) {
/* 287 */         this.availableTrees.put(name, new TreeMap());
/*     */       }
/* 289 */       Map trees = MetaPIGA.treeViewer.getLoadedTrees((Parameters)parameters.get(name));
/* 290 */       for (String treeName : trees.keySet()) {
/* 291 */         if (!((Map)this.availableTrees.get(name)).containsKey(treeName)) {
/* 292 */           ((Map)this.availableTrees.get(name)).put(treeName, (Tree)trees.get(treeName));
/*     */         }
/*     */       }
/* 295 */       this.comboBox.addItem(name);
/*     */     }
/* 297 */     this.comboBox.repaint();
/* 298 */     this.isAdjustingDatasetCombobox = false;
/* 299 */     if (this.comboBox.getItemCount() > 0) {
/* 300 */       this.currentParameters = ((Parameters)this.availableParameters.get(this.comboBox.getSelectedItem()));
/* 301 */       this.availableTreesListModel.clear();
/* 302 */       for (String treeName : ((Map)this.availableTrees.get(this.currentParameters.label)).keySet()) {
/* 303 */         this.availableTreesListModel.addElement(treeName);
/*     */       }
/*     */     }
/* 306 */     this.availableTitle.setTitle("Available trees (" + this.availableTreesListModel.getSize() + " trees)");
/* 307 */     this.scrollPane.repaint();
/*     */   }
/*     */ 
/*     */   private void buildConsensus() {
/* 311 */     if (this.consensusListModel.size() > 0) {
/* 312 */       this.currentParameters = ((Parameters)this.availableParameters.get(this.comboBox.getSelectedItem()));
/* 313 */       List trees = new ArrayList();
/* 314 */       for (int i = 0; i < this.consensusListModel.size(); i++) {
/* 315 */         Tree tree = (Tree)((Map)this.availableTrees.get(this.currentParameters.label)).get(this.consensusListModel.get(i));
/* 316 */         if (!checkTree(tree)) {
/* 317 */           JOptionPane.showMessageDialog(this, "Tree " + tree.getName() + " is uncompatible with dataset " + this.currentParameters.label, "Building consensus tree", 0);
/* 318 */           return;
/*     */         }
/* 320 */         trees.add(tree);
/*     */       }
/*     */       try {
/* 323 */         Consensus consensus = new Consensus(trees, this.currentParameters.dataset);
/* 324 */         Tree consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 325 */         consensusTree.setName(this.consensusTreeTextField.getText());
/*     */ 
/* 327 */         MetaPIGA.treeViewer.addTree(consensusTree, this.currentParameters);
/* 328 */         MetaPIGA.treeViewer.setSelectedTrees(consensusTree);
/*     */ 
/* 341 */         dispose();
/* 342 */         MetaPIGA.treeViewer.setVisible(true);
/*     */       } catch (Exception ex) {
/* 344 */         dispose();
/* 345 */         ex.printStackTrace();
/* 346 */         JOptionPane.showMessageDialog(this, Tools.getErrorPanel("Error when building consensus", ex), "Building consensus tree", 0);
/*     */       }
/*     */     } else {
/* 349 */       JOptionPane.showMessageDialog(this, "You must put at least one tree in consensus list !", "Building consensus tree", 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean checkTree(Tree tree) {
/* 354 */     List leaves = tree.getLeaves();
/* 355 */     List taxas = this.currentParameters.dataset.getTaxa();
/* 356 */     if (leaves.size() != taxas.size()) return false;
/* 357 */     for (Node node : leaves) {
/* 358 */       if (!taxas.contains(node.label)) return false;
/*     */     }
/* 360 */     return true;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.ConsensusTreeFrame
 * JD-Core Version:    0.6.2
 */