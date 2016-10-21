/*     */ package metapiga.settings;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Frame;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.HashSet;
/*     */ import java.util.Set;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JSpinner;
/*     */ import javax.swing.JTable;
/*     */ import javax.swing.JViewport;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.SpinnerModel;
/*     */ import javax.swing.SpinnerNumberModel;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import javax.swing.table.DefaultTableCellRenderer;
/*     */ import javax.swing.table.JTableHeader;
/*     */ import javax.swing.table.TableColumn;
/*     */ import javax.swing.table.TableColumnModel;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.CodonDomainDefinition;
/*     */ import metapiga.parameters.Parameters.CodonTransitionTableType;
/*     */ 
/*     */ public class CodonMaker extends JDialog
/*     */ {
/*     */   private static final long serialVersionUID = 8922110017179861186L;
/*     */   private Parameters P;
/*  49 */   boolean save = false;
/*     */ 
/*  51 */   int firstPosition = -1;
/*  52 */   int lastPosition = -1;
/*     */   final int firstPositionOld;
/*     */   final int lastPositionOld;
/*     */   final Parameters.CodonTransitionTableType oldCodonTable;
/*  57 */   private JPanel jContentPane = null;
/*  58 */   private JPanel southPanel = null;
/*  59 */   private JPanel northPanel = null;
/*  60 */   private JPanel rangePanel = null;
/*  61 */   private JPanel pickPositionButtonPanel = null;
/*  62 */   private JPanel chooseCodeDropDownPanel = null;
/*     */ 
/*  64 */   private JButton saveButton = null;
/*  65 */   private JButton cancelButton = null;
/*  66 */   private JButton firstPositionButton = null;
/*  67 */   private JButton lastPositionButton = null;
/*  68 */   private JButton makeCodonsButton = null;
/*     */ 
/*  70 */   private JSpinner startCodonSpinner = null;
/*  71 */   private JSpinner endCodonSpinner = null;
/*     */ 
/*  73 */   private JLabel firstPositionTextLabel = null;
/*  74 */   private JLabel lastPositionTextLabel = null;
/*     */ 
/*  76 */   private JScrollPane mainScrollPane = null;
/*  77 */   private JTable characterTable = null;
/*  78 */   private CharsetEditor.TableModel charTableModel = null;
/*     */   private final SettingsPanelCodons parent;
/*  81 */   JComboBox dropDownCode = null;
/*     */   DNACodeComboModel dropDownCodeModel;
/*  84 */   final Set<String> binnedCharsets = new HashSet();
/*     */   private JScrollPane northScroll;
/*     */ 
/*     */   public CodonMaker(Frame owner, String title, boolean modal, Parameters params, SettingsPanelCodons parent)
/*     */   {
/*  88 */     super(owner, title, modal);
/*  89 */     this.P = params;
/*  90 */     this.parent = parent;
/*  91 */     if (this.P.codonDomain != null) {
/*  92 */       this.firstPosition = this.P.codonDomain.getStartCodonDomainPosition();
/*  93 */       this.lastPosition = this.P.codonDomain.getEndCodonDomainPosition();
/*  94 */       this.firstPositionOld = this.firstPosition;
/*  95 */       this.lastPositionOld = this.lastPosition;
/*  96 */       this.oldCodonTable = this.P.getCodonTaransitionTableType();
/*     */     } else {
/*  98 */       this.firstPositionOld = -1;
/*  99 */       this.lastPositionOld = -1;
/* 100 */       this.oldCodonTable = Parameters.CodonTransitionTableType.NONE;
/*     */     }
/* 102 */     initialize();
/*     */   }
/*     */ 
/*     */   private void initialize() {
/* 106 */     this.dropDownCodeModel = getDropDownCodeModel();
/* 107 */     this.dropDownCodeModel.setSelectedItem(this.P.getCurrentCodonTable());
/* 108 */     setSize(new Dimension(651, 252));
/* 109 */     setContentPane(getJContentPane());
/*     */   }
/*     */ 
/*     */   private DNACodeComboModel getDropDownCodeModel() {
/* 113 */     if (this.dropDownCodeModel == null) {
/* 114 */       this.dropDownCodeModel = new DNACodeComboModel();
/*     */     }
/* 116 */     return this.dropDownCodeModel;
/*     */   }
/*     */ 
/*     */   private JPanel getJContentPane()
/*     */   {
/* 125 */     if (this.jContentPane == null) {
/* 126 */       this.jContentPane = new JPanel();
/* 127 */       this.jContentPane.setLayout(new BorderLayout());
/* 128 */       this.jContentPane.add(getSouthPanel(), "South");
/* 129 */       this.jContentPane.add(getMainScrollPane(), "Center");
/* 130 */       this.northScroll = new JScrollPane();
/* 131 */       this.northScroll.setViewportView(getNorthPanel());
/* 132 */       this.jContentPane.add(this.northScroll, "North");
/*     */     }
/* 134 */     return this.jContentPane;
/*     */   }
/*     */ 
/*     */   private JPanel getSouthPanel() {
/* 138 */     if (this.southPanel == null) {
/* 139 */       GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
/* 140 */       gridBagConstraints1.gridx = 0;
/* 141 */       gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
/* 142 */       gridBagConstraints1.gridy = 0;
/* 143 */       GridBagConstraints gridBagConstraints = new GridBagConstraints();
/* 144 */       gridBagConstraints.gridx = 1;
/* 145 */       gridBagConstraints.insets = new Insets(5, 5, 5, 5);
/* 146 */       gridBagConstraints.gridy = 0;
/* 147 */       this.southPanel = new JPanel();
/* 148 */       this.southPanel.setLayout(new GridBagLayout());
/* 149 */       this.southPanel.add(getSaveButton(), gridBagConstraints1);
/* 150 */       this.southPanel.add(getCancelButton(), gridBagConstraints);
/*     */     }
/* 152 */     return this.southPanel;
/*     */   }
/*     */ 
/*     */   private JScrollPane getMainScrollPane()
/*     */   {
/* 161 */     if (this.mainScrollPane == null) {
/* 162 */       this.mainScrollPane = new JScrollPane();
/* 163 */       this.mainScrollPane.setViewportView(getTable());
/* 164 */       this.mainScrollPane.setHorizontalScrollBarPolicy(32);
/* 165 */       this.mainScrollPane.getViewport().setBackground(Color.BLACK);
/*     */     }
/* 167 */     return this.mainScrollPane;
/*     */   }
/*     */ 
/*     */   private JPanel getNorthPanel() {
/* 171 */     if (this.northPanel == null) {
/* 172 */       this.northPanel = new JPanel();
/* 173 */       this.northPanel.setLayout(new GridBagLayout());
/* 174 */       GridBagConstraints northPanelGBC = new GridBagConstraints();
/* 175 */       northPanelGBC.gridx = 0;
/* 176 */       northPanelGBC.gridy = 0;
/* 177 */       this.northPanel.add(getPickPositionButtonPanel(), northPanelGBC);
/* 178 */       northPanelGBC.gridx = 1;
/* 179 */       this.northPanel.add(getRangePanel(), northPanelGBC);
/* 180 */       northPanelGBC.gridx = 2;
/* 181 */       this.northPanel.add(getChooseCodeDropDownPanel(), northPanelGBC);
/*     */     }
/* 183 */     return this.northPanel;
/*     */   }
/*     */ 
/*     */   private JPanel getRangePanel() {
/* 187 */     if (this.rangePanel == null) {
/* 188 */       this.rangePanel = new JPanel();
/* 189 */       this.rangePanel.setLayout(new GridBagLayout());
/*     */ 
/* 191 */       TitledBorder titledBorder = BorderFactory.createTitledBorder("Define range for codons");
/* 192 */       this.rangePanel.setBorder(titledBorder);
/* 193 */       GridBagConstraints rangePanelGBC = new GridBagConstraints();
/* 194 */       rangePanelGBC.gridx = 0;
/* 195 */       rangePanelGBC.gridy = 0;
/* 196 */       rangePanelGBC.insets = new Insets(3, 3, 3, 3);
/* 197 */       this.firstPositionTextLabel = new JLabel("First position");
/* 198 */       this.rangePanel.add(this.firstPositionTextLabel, rangePanelGBC);
/* 199 */       rangePanelGBC.gridx = 2;
/* 200 */       this.lastPositionTextLabel = new JLabel("Last position");
/* 201 */       this.rangePanel.add(this.lastPositionTextLabel, rangePanelGBC);
/* 202 */       rangePanelGBC.gridx = 0;
/* 203 */       rangePanelGBC.gridy = 1;
/* 204 */       this.rangePanel.add(getStartCodonSpinner(), rangePanelGBC);
/* 205 */       rangePanelGBC.gridx = 1;
/* 206 */       this.rangePanel.add(new JLabel("to"), rangePanelGBC);
/* 207 */       rangePanelGBC.gridx = 2;
/* 208 */       this.rangePanel.add(getEndCodonSpinner(), rangePanelGBC);
/* 209 */       rangePanelGBC.gridx = 3;
/* 210 */       rangePanelGBC.gridy = 1;
/* 211 */       this.rangePanel.add(getMakeCodonsButton(), rangePanelGBC);
/*     */     }
/* 213 */     return this.rangePanel;
/*     */   }
/*     */ 
/*     */   private JButton getMakeCodonsButton() {
/* 217 */     if (this.makeCodonsButton == null) {
/* 218 */       this.makeCodonsButton = new JButton();
/* 219 */       this.makeCodonsButton.setText("Set range");
/* 220 */       this.makeCodonsButton.addActionListener(new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e) {
/* 223 */           Integer fromPos = (Integer)CodonMaker.this.startCodonSpinner.getValue();
/* 224 */           Integer toPos = (Integer)CodonMaker.this.endCodonSpinner.getValue();
/*     */ 
/* 226 */           if (fromPos.intValue() < toPos.intValue()) {
/* 227 */             CodonMaker.this.firstPosition = fromPos.intValue();
/* 228 */             CodonMaker.this.lastPosition = toPos.intValue();
/* 229 */             CodonMaker.this.characterTable.repaint();
/*     */           } else {
/* 231 */             JOptionPane.showMessageDialog(null, "Invalid values", "Invalid position values", 0);
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 236 */     return this.makeCodonsButton;
/*     */   }
/*     */ 
/*     */   private JSpinner getStartCodonSpinner() {
/* 240 */     if (this.startCodonSpinner == null) {
/* 241 */       int lastNuclotidePosition = this.characterTable.getColumnCount() - 1;
/* 242 */       SpinnerModel startCodonSpinnerModel = new SpinnerNumberModel(1, 1, lastNuclotidePosition, 1);
/* 243 */       this.startCodonSpinner = new JSpinner(startCodonSpinnerModel);
/*     */     }
/* 245 */     return this.startCodonSpinner;
/*     */   }
/*     */ 
/*     */   private JSpinner getEndCodonSpinner() {
/* 249 */     if (this.endCodonSpinner == null) {
/* 250 */       int lastNuclotidePosition = this.characterTable.getColumnCount() - 1;
/* 251 */       SpinnerModel endCodonSpinnerModel = new SpinnerNumberModel(lastNuclotidePosition, 0, lastNuclotidePosition, 1);
/* 252 */       this.endCodonSpinner = new JSpinner(endCodonSpinnerModel);
/*     */     }
/* 254 */     return this.endCodonSpinner;
/*     */   }
/*     */ 
/*     */   private JPanel getPickPositionButtonPanel() {
/* 258 */     if (this.pickPositionButtonPanel == null) {
/* 259 */       this.pickPositionButtonPanel = new JPanel();
/* 260 */       this.pickPositionButtonPanel.setLayout(new GridBagLayout());
/* 261 */       GridBagConstraints pickPositionGBC = new GridBagConstraints();
/* 262 */       pickPositionGBC.gridx = 0;
/* 263 */       pickPositionGBC.gridy = 0;
/* 264 */       pickPositionGBC.insets = new Insets(5, 5, 5, 5);
/* 265 */       this.pickPositionButtonPanel.add(getStartPosButton(), pickPositionGBC);
/* 266 */       pickPositionGBC.gridx = 1;
/* 267 */       this.pickPositionButtonPanel.add(getLastPositionButton(), pickPositionGBC);
/*     */ 
/* 269 */       TitledBorder titledBorder = BorderFactory.createTitledBorder("Pick position");
/* 270 */       this.pickPositionButtonPanel.setBorder(titledBorder);
/*     */     }
/* 272 */     return this.pickPositionButtonPanel;
/*     */   }
/*     */ 
/*     */   private JPanel getChooseCodeDropDownPanel() {
/* 276 */     if (this.chooseCodeDropDownPanel == null) {
/* 277 */       this.chooseCodeDropDownPanel = new JPanel();
/* 278 */       this.chooseCodeDropDownPanel.setLayout(new GridBagLayout());
/* 279 */       GridBagConstraints dropDownGBC = new GridBagConstraints();
/* 280 */       dropDownGBC.gridx = 0;
/* 281 */       dropDownGBC.gridy = 0;
/* 282 */       dropDownGBC.insets = new Insets(5, 5, 5, 5);
/* 283 */       this.chooseCodeDropDownPanel.add(getDropDownCode(), dropDownGBC);
/* 284 */       TitledBorder titledBorder = BorderFactory.createTitledBorder("Choose DNA code");
/* 285 */       this.chooseCodeDropDownPanel.setBorder(titledBorder);
/*     */     }
/* 287 */     return this.chooseCodeDropDownPanel;
/*     */   }
/*     */ 
/*     */   private JComboBox getDropDownCode() {
/* 291 */     if (this.dropDownCode == null) {
/* 292 */       this.dropDownCode = new JComboBox(this.dropDownCodeModel);
/*     */     }
/* 294 */     return this.dropDownCode;
/*     */   }
/*     */ 
/*     */   private JButton getLastPositionButton() {
/* 298 */     if (this.lastPositionButton == null) {
/* 299 */       this.lastPositionButton = new JButton();
/* 300 */       this.lastPositionButton.setText("Set as last position");
/* 301 */       this.lastPositionButton.addActionListener(new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent arg0)
/*     */         {
/* 305 */           int selectedColumn = CodonMaker.this.characterTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
/* 306 */           if (selectedColumn > CodonMaker.this.firstPosition) {
/* 307 */             CodonMaker.this.lastPosition = selectedColumn;
/* 308 */             CodonMaker.this.characterTable.repaint();
/*     */           } else {
/* 310 */             JOptionPane.showMessageDialog(null, "Last position cannot be before the start position", "Invalid last position", 0);
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/*     */ 
/* 316 */     return this.lastPositionButton;
/*     */   }
/*     */ 
/*     */   private JButton getStartPosButton() {
/* 320 */     if (this.firstPositionButton == null) {
/* 321 */       this.firstPositionButton = new JButton();
/* 322 */       this.firstPositionButton.setText("Set as first position");
/* 323 */       this.firstPositionButton.addActionListener(new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e) {
/* 326 */           int selectedColumn = CodonMaker.this.characterTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
/* 327 */           if ((selectedColumn < CodonMaker.this.lastPosition) || ((selectedColumn > CodonMaker.this.lastPosition) && (CodonMaker.this.lastPosition == -1))) {
/* 328 */             CodonMaker.this.firstPosition = selectedColumn;
/* 329 */             CodonMaker.this.characterTable.repaint();
/*     */           } else {
/* 331 */             JOptionPane.showMessageDialog(null, "Start position cannot be greater than the end position", "Invalid start position", 0);
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/* 336 */     return this.firstPositionButton;
/*     */   }
/*     */ 
/*     */   private JTable getTable() {
/* 340 */     if (this.characterTable == null) {
/* 341 */       this.charTableModel = new CharsetEditor.TableModel(this.P.charactersBlock);
/* 342 */       this.characterTable = new JTable(this.charTableModel);
/* 343 */       this.characterTable.setDefaultRenderer(String.class, new ColoredTableCellRenderer(null));
/* 344 */       this.characterTable.setAutoResizeMode(0);
/* 345 */       this.characterTable.setColumnSelectionAllowed(false);
/* 346 */       this.characterTable.setRowSelectionAllowed(false);
/* 347 */       this.characterTable.getTableHeader().setReorderingAllowed(false);
/* 348 */       this.characterTable.setGridColor(Color.black);
/* 349 */       this.characterTable.setRowHeight(0, 20);
/*     */ 
/* 351 */       TableColumn column = this.characterTable.getColumnModel().getColumn(0);
/* 352 */       column.setPreferredWidth(150);
/* 353 */       column.setHeaderRenderer(new CharsetEditor.HeaderRenderer());
/* 354 */       for (int i = 1; i < this.characterTable.getColumnCount(); i++) {
/* 355 */         column = getTable().getColumnModel().getColumn(i);
/* 356 */         column.setPreferredWidth(5);
/* 357 */         column.setHeaderRenderer(new CharsetEditor.HeaderRenderer());
/*     */       }
/*     */     }
/* 360 */     return this.characterTable;
/*     */   }
/*     */ 
/*     */   private JButton getSaveButton()
/*     */   {
/* 369 */     if (this.saveButton == null) {
/* 370 */       this.saveButton = new JButton();
/* 371 */       this.saveButton.setText("SAVE");
/* 372 */       this.saveButton.addActionListener(new ActionListener() {
/*     */         public void actionPerformed(ActionEvent e) {
/* 374 */           if ((CodonMaker.this.firstPosition < 1) || (CodonMaker.this.lastPosition < CodonMaker.this.firstPosition + 2) || (CodonMaker.this.lastPosition >= CodonMaker.this.characterTable.getColumnCount())) {
/* 375 */             JOptionPane.showMessageDialog(null, "invalid start and end positions", "Codon definition failed", 0);
/*     */           }
/* 377 */           else if (CodonMaker.this.isChanged()) {
/* 378 */             CodonMaker.this.save = true;
/* 379 */             CodonMaker.this.dispose();
/*     */           } else {
/* 381 */             CodonMaker.this.dispose();
/*     */           }
/*     */         }
/*     */       });
/*     */     }
/*     */ 
/* 387 */     return this.saveButton;
/*     */   }
/*     */ 
/*     */   private JButton getCancelButton()
/*     */   {
/* 396 */     if (this.cancelButton == null) {
/* 397 */       this.cancelButton = new JButton();
/* 398 */       this.cancelButton.setText("CANCEL");
/* 399 */       this.cancelButton.addActionListener(new ActionListener() {
/*     */         public void actionPerformed(ActionEvent e) {
/* 401 */           CodonMaker.this.dispose();
/*     */         }
/*     */       });
/*     */     }
/* 405 */     return this.cancelButton;
/*     */   }
/*     */ 
/*     */   public boolean isChanged() {
/* 409 */     if ((this.firstPositionOld != this.firstPosition) || (this.lastPositionOld != this.lastPosition) || (this.oldCodonTable != getDropDownCode().getSelectedItem())) {
/* 410 */       return true;
/*     */     }
/* 412 */     return false;
/*     */   }
/*     */   private class ColoredTableCellRenderer extends DefaultTableCellRenderer {
/*     */     private ColoredTableCellRenderer() {
/*     */     }
/*     */     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
/* 418 */       Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
/* 419 */       JLabel label = (JLabel)comp;
/* 420 */       label.setHorizontalTextPosition(0);
/* 421 */       if (value == null) value = "";
/* 422 */       label.setText(value.toString());
/*     */ 
/* 424 */       if ((column == 0) || (isNotCodonRange(CodonMaker.this.firstPosition, CodonMaker.this.lastPosition, column))) {
/* 425 */         label.setBackground(Color.BLACK);
/* 426 */         label.setForeground(Color.GREEN);
/*     */       } else {
/* 428 */         label.setBackground(new Color(135, 137, 211));
/* 429 */         label.setForeground(Color.BLACK);
/*     */       }
/*     */ 
/* 432 */       if (table.getColumnModel().getSelectionModel().getLeadSelectionIndex() == column) {
/* 433 */         if ((column == 0) || (isNotCodonRange(CodonMaker.this.firstPosition, CodonMaker.this.lastPosition, column))) {
/* 434 */           label.setBackground(new Color(104, 221, 255));
/* 435 */           label.setForeground(Color.BLACK);
/*     */         } else {
/* 437 */           label.setBackground(new Color(138, 43, 226));
/* 438 */           label.setForeground(Color.BLACK);
/*     */         }
/*     */       }
/* 441 */       if (column != 0) label.setHorizontalAlignment(0); else
/* 442 */         label.setHorizontalAlignment(10);
/* 443 */       if (row == 0) label.setVerticalAlignment(3);
/* 444 */       return label;
/*     */     }
/*     */ 
/*     */     private boolean isNotCodonRange(int first, int last, int column) {
/* 448 */       boolean ret = false;
/* 449 */       ret = ((column < first) || (column > last) || ((first == -1) && (column < last))) && ((column != first) || (last != -1));
/*     */ 
/* 451 */       return ret;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.settings.CodonMaker
 * JD-Core Version:    0.6.2
 */