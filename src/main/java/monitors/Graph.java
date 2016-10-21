/*     */ package monitors;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Point;
/*     */ import java.awt.geom.Rectangle2D;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.SortedMap;
/*     */ import java.util.TreeMap;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import javax.swing.JPanel;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class Graph extends JPanel
/*     */ {
/*     */   private static final int space = 1;
/*     */   private ConcurrentHashMap<String, List<Double>> points;
/*     */   private Map<String, Color> colors;
/*     */   private SortedMap<Integer, Integer> replicatesPositions;
/*     */   private Map<Integer, Double> replicatesMinima;
/*     */   private Map<Integer, Double> replicatesMaxima;
/*  37 */   private int currentReplicateId = -1;
/*     */   private SortedMap<Integer, Integer> restartsPositions;
/*     */   private Map<Integer, Double> restartsMinima;
/*     */   private Map<Integer, Double> restartsMaxima;
/*  41 */   private int numRestart = 2147483647;
/*  42 */   private int currentRestartId = -1;
/*     */   private String mainCurve;
/*     */   private double maxValue;
/*     */   private double maxValueCurrentRep;
/*     */   private double minValueCurrentRep;
/*     */   private double maxValueCurrentRestart;
/*     */   private double minValueCurrentRestart;
/*     */   private double minValue;
/*     */   private int numPoints;
/*     */   private final boolean maxLine;
/*     */   private final boolean minLine;
/*     */   private final boolean negative;
/*     */   private final int xStep;
/*     */   private final GraphY yAxis;
/*     */   private Point viewPosition;
/*     */ 
/*     */   public Graph(String[] curves, int mainCurveIndex, boolean maxLine, boolean minLine, boolean negative, int xStep, GraphY yAxis)
/*     */   {
/*  70 */     this.mainCurve = curves[mainCurveIndex];
/*  71 */     this.maxLine = maxLine;
/*  72 */     this.minLine = minLine;
/*  73 */     this.negative = negative;
/*  74 */     this.xStep = xStep;
/*  75 */     this.yAxis = yAxis;
/*  76 */     this.points = new ConcurrentHashMap();
/*  77 */     this.colors = new HashMap();
/*  78 */     List availableColors = new ArrayList();
/*  79 */     availableColors.add(Color.green);
/*  80 */     availableColors.add(Color.yellow);
/*  81 */     availableColors.add(Color.magenta);
/*  82 */     availableColors.add(Color.blue);
/*  83 */     availableColors.add(Color.white);
/*  84 */     availableColors.add(Color.orange);
/*  85 */     availableColors.add(Color.pink);
/*  86 */     for (String curve : curves) {
/*  87 */       this.points.put(curve, new ArrayList());
/*  88 */       Color color = availableColors.isEmpty() ? new Color(Tools.randInt(255), Tools.randInt(255), Tools.randInt(255)) : (Color)availableColors.remove(0);
/*  89 */       this.colors.put(curve, color);
/*     */     }
/*  91 */     this.replicatesPositions = Collections.synchronizedSortedMap(new TreeMap());
/*  92 */     this.replicatesMinima = new TreeMap();
/*  93 */     this.replicatesMaxima = new TreeMap();
/*  94 */     this.restartsPositions = Collections.synchronizedSortedMap(new TreeMap());
/*  95 */     this.restartsMinima = new TreeMap();
/*  96 */     this.restartsMaxima = new TreeMap();
/*  97 */     this.maxValue = 0.0D;
/*  98 */     this.minValue = 1.7976931348623157E+308D;
/*  99 */     this.maxValueCurrentRep = 0.0D;
/* 100 */     this.minValueCurrentRep = 1.7976931348623157E+308D;
/* 101 */     this.maxValueCurrentRestart = 0.0D;
/* 102 */     this.minValueCurrentRestart = 1.7976931348623157E+308D;
/* 103 */     this.numPoints = 1;
/*     */   }
/*     */ 
/*     */   public boolean addPoints(Map<String, Double> nextPoints)
/*     */   {
/* 114 */     for (Entry e : nextPoints.entrySet()) {
/* 115 */       ((List)this.points.get(e.getKey())).add((Double)e.getValue());
/*     */     }
/* 117 */     this.numPoints += 1;
/* 118 */     this.viewPosition = new Point(this.numPoints * 1, 0);
/* 119 */     double point = ((Double)nextPoints.get(this.mainCurve)).doubleValue();
/* 120 */     if (point > this.maxValue)
/* 121 */       this.maxValue = point;
/* 122 */     if (point < this.minValue)
/* 123 */       this.minValue = point;
/* 124 */     if (point > this.maxValueCurrentRep)
/* 125 */       this.maxValueCurrentRep = point;
/* 126 */     if (point < this.minValueCurrentRep)
/* 127 */       this.minValueCurrentRep = point;
/* 128 */     if (point > this.maxValueCurrentRestart)
/* 129 */       this.maxValueCurrentRestart = point;
/* 130 */     if (point < this.minValueCurrentRestart)
/* 131 */       this.minValueCurrentRestart = point;
/* 132 */     if (getWidth() <= this.numPoints * 1) return false;
/* 133 */     repaint();
/* 134 */     return true;
/*     */   }
/*     */ 
/*     */   public Color getColor(String curve) {
/* 138 */     return (Color)this.colors.get(curve);
/*     */   }
/*     */ 
/*     */   public void setColor(String curve, Color color) {
/* 142 */     this.colors.put(curve, color);
/*     */   }
/*     */ 
/*     */   public void newReplicate(int replicateId) {
/* 146 */     if (this.currentReplicateId >= 0) {
/* 147 */       this.replicatesMaxima.put(Integer.valueOf(this.currentReplicateId), Double.valueOf(this.maxValueCurrentRep));
/* 148 */       this.replicatesMinima.put(Integer.valueOf(this.currentReplicateId), Double.valueOf(this.minValueCurrentRep));
/*     */     }
/* 150 */     this.maxValueCurrentRep = 0.0D;
/* 151 */     this.minValueCurrentRep = 1.7976931348623157E+308D;
/* 152 */     this.replicatesPositions.put(Integer.valueOf(replicateId), Integer.valueOf(this.numPoints));
/* 153 */     this.currentReplicateId = replicateId;
/* 154 */     if ((this.numRestart == 2147483647) && (this.currentRestartId > -1)) this.numRestart = (this.currentRestartId + 1); 
/*     */   }
/*     */ 
/*     */   public void restart()
/*     */   {
/* 158 */     if (this.currentRestartId >= 0) {
/* 159 */       this.restartsMaxima.put(Integer.valueOf(this.currentRestartId), Double.valueOf(this.maxValueCurrentRestart));
/* 160 */       this.restartsMinima.put(Integer.valueOf(this.currentRestartId), Double.valueOf(this.minValueCurrentRestart));
/*     */     }
/* 162 */     this.maxValueCurrentRestart = 0.0D;
/* 163 */     this.minValueCurrentRestart = 1.7976931348623157E+308D;
/* 164 */     this.currentRestartId += 1;
/* 165 */     this.restartsPositions.put(Integer.valueOf(this.currentRestartId), Integer.valueOf(this.numPoints));
/*     */   }
/*     */ 
/*     */   public Point getViewPosition() {
/* 169 */     return this.viewPosition;
/*     */   }
/*     */ 
/*     */   public void paintComponent(Graphics g)
/*     */   {
/* 174 */     g.setColor(new Color(0, 0, 0));
/* 175 */     Dimension dim = getSize();
/* 176 */     g.fillRect(0, 0, (int)dim.getWidth(), (int)dim.getHeight());
/* 177 */     if (this.numPoints > 1) {
/* 178 */       g.setColor(Color.green);
/* 179 */       double scale = (this.maxValue - this.minValue) / (getHeight() * 80.0D / 100.0D);
/*     */       try {
/* 181 */         Map lastPoint = new HashMap();
/* 182 */         for (Entry e : this.points.entrySet()) {
/* 183 */           lastPoint.put((String)e.getKey(), (Double)((List)e.getValue()).get(0));
/*     */         }
/* 185 */         Iterator rIt = this.replicatesPositions.entrySet().iterator();
/* 186 */         Entry e = rIt.hasNext() ? (Entry)rIt.next() : null;
/* 187 */         int rPos = e != null ? ((Integer)e.getValue()).intValue() : -1;
/* 188 */         int rNum = e != null ? ((Integer)e.getKey()).intValue() : 1;
/* 189 */         Iterator rsIt = this.restartsPositions.entrySet().iterator();
/* 190 */         Entry ers = rsIt.hasNext() ? (Entry)rsIt.next() : null;
/* 191 */         int rsPos = ers != null ? ((Integer)ers.getValue()).intValue() : -1;
/* 192 */         int rsNum = ers != null ? ((Integer)ers.getKey()).intValue() % this.numRestart : 1;
/* 193 */         int x = 1;
/* 194 */         for (int i = 1; i < this.numPoints - 1; i++) {
/* 195 */           int x1 = 1 * (x - 1);
/* 196 */           int x2 = 1 * x;
/* 197 */           List curves = new ArrayList(this.points.keySet());
/* 198 */           if (curves.remove("Best solution")) curves.add("Best solution");
/* 199 */           for (String curve : curves) {
/* 200 */             if (((List)this.points.get(curve)).size() > i) {
/* 201 */               double thisPoint = ((Double)((List)this.points.get(curve)).get(i)).doubleValue();
/* 202 */               int y1 = getHeight() - (int)((((Double)lastPoint.get(curve)).doubleValue() - this.minValue) / scale) - getHeight() / 10;
/* 203 */               int y2 = getHeight() - (int)((thisPoint - this.minValue) / scale) - getHeight() / 10;
/* 204 */               if (this.negative) {
/* 205 */                 y1 = (int)((((Double)lastPoint.get(curve)).doubleValue() - this.minValue) / scale) + getHeight() / 10;
/* 206 */                 y2 = (int)((thisPoint - this.minValue) / scale) + getHeight() / 10;
/*     */               }
/* 208 */               g.setColor((Color)this.colors.get(curve));
/* 209 */               if ((i != rsPos - 1) && (i != rPos - 1)) g.drawLine(x1, y1, x2, y2);
/* 210 */               lastPoint.put(curve, Double.valueOf(thisPoint));
/*     */             }
/*     */           }
/* 213 */           if (i == rsPos) {
/* 214 */             if (rsNum > 0) {
/* 215 */               g.setColor(Color.magenta);
/* 216 */               g.drawLine(x2, 0, x2, getHeight());
/* 217 */               g.drawString(" restart " + rsNum, x2, 10);
/*     */             }
/* 219 */             if (rsIt.hasNext()) {
/* 220 */               ers = (Entry)rsIt.next();
/* 221 */               rsPos = ((Integer)ers.getValue()).intValue();
/* 222 */               rsNum = ((Integer)ers.getKey()).intValue() % this.numRestart;
/*     */             }
/*     */           }
/* 225 */           if (i == rPos) {
/* 226 */             g.setColor(Color.cyan);
/* 227 */             g.drawLine(x2, 0, x2, getHeight());
/* 228 */             g.drawString(" rep " + rNum, x2, 10);
/* 229 */             if (rIt.hasNext()) {
/* 230 */               e = (Entry)rIt.next();
/* 231 */               rPos = ((Integer)e.getValue()).intValue();
/* 232 */               rNum = ((Integer)e.getKey()).intValue();
/*     */             }
/*     */           }
/* 235 */           x++;
/*     */         }
/* 237 */         if ((this.maxLine) || (this.minLine)) {
/* 238 */           g.setColor(Color.red);
/* 239 */           SortedMap positions = this.restartsPositions.isEmpty() ? this.replicatesPositions : this.restartsPositions;
/* 240 */           double maxValueCurrent = this.restartsPositions.isEmpty() ? this.maxValueCurrentRep : this.maxValueCurrentRestart;
/* 241 */           Map rMaxima = this.restartsPositions.isEmpty() ? this.replicatesMaxima : this.restartsMaxima;
/* 242 */           double minValueCurrent = this.restartsPositions.isEmpty() ? this.minValueCurrentRep : this.minValueCurrentRestart;
/* 243 */           Map rMinima = this.restartsPositions.isEmpty() ? this.replicatesMinima : this.restartsMinima;
/* 244 */           Iterator rid = positions.keySet().iterator();
/* 245 */           int currentId = rid.hasNext() ? ((Integer)rid.next()).intValue() : -1;
/* 246 */           int x1 = currentId < 0 ? 0 : ((Integer)positions.get(Integer.valueOf(currentId))).intValue();
/* 247 */           for (int i = 0; i < positions.size(); i++) {
/* 248 */             int nextId = rid.hasNext() ? ((Integer)rid.next()).intValue() : -1;
/* 249 */             int x2 = nextId < 0 ? this.numPoints * 1 : ((Integer)positions.get(Integer.valueOf(nextId))).intValue();
/* 250 */             if (this.maxLine) {
/* 251 */               double thisMax = nextId < 0 ? maxValueCurrent : ((Double)rMaxima.get(Integer.valueOf(currentId))).doubleValue();
/* 252 */               int y1 = getHeight() - (int)((thisMax - this.minValue) / scale) - getHeight() / 10;
/* 253 */               if (this.negative) {
/* 254 */                 y1 = (int)((thisMax - this.minValue) / scale) + getHeight() / 10;
/*     */               }
/* 256 */               int y2 = y1;
/* 257 */               g.drawLine(x1, y1, x2, y2);
/*     */             }
/* 259 */             if (this.minLine) {
/* 260 */               double thisMin = nextId < 0 ? minValueCurrent : ((Double)rMinima.get(Integer.valueOf(currentId))).doubleValue();
/* 261 */               int y1 = getHeight() - (int)((thisMin - this.minValue) / scale) - getHeight() / 10;
/* 262 */               if (this.negative) {
/* 263 */                 y1 = (int)((thisMin - this.minValue) / scale) + getHeight() / 10;
/*     */               }
/* 265 */               int y2 = y1;
/* 266 */               g.drawLine(x1, y1, x2, y2);
/*     */             }
/* 268 */             x1 = x2;
/* 269 */             currentId = nextId;
/*     */           }
/*     */         }
/* 272 */         if (this.xStep > 0) {
/* 273 */           g.setColor(Color.cyan);
/* 274 */           SortedMap positions = this.restartsPositions.isEmpty() ? this.replicatesPositions : this.restartsPositions;
/* 275 */           Iterator rid = positions.keySet().iterator();
/* 276 */           int currentId = rid.hasNext() ? ((Integer)rid.next()).intValue() : -1;
/* 277 */           int x1 = currentId < 0 ? 0 : ((Integer)positions.get(Integer.valueOf(currentId))).intValue();
/* 278 */           int yAxis = getHeight() - 20;
/* 279 */           for (int i = 0; i < positions.size(); i++) {
/* 280 */             int nextId = rid.hasNext() ? ((Integer)rid.next()).intValue() : -1;
/* 281 */             int x2 = nextId < 0 ? this.numPoints * 1 : ((Integer)positions.get(Integer.valueOf(nextId))).intValue();
/* 282 */             for (int offset = this.xStep; x1 + offset < x2 - 1; offset += this.xStep) {
/* 283 */               g.drawLine(x1 + offset, yAxis - 1, x1 + offset, yAxis + 1);
/* 284 */               String s = offset;
/* 285 */               int sl = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
/* 286 */               int sh = (int)g.getFontMetrics().getStringBounds(s, g).getHeight();
/* 287 */               g.drawString(s, x1 + offset - sl, yAxis + 2 + sh);
/*     */             }
/* 289 */             x1 = x2;
/* 290 */             currentId = nextId;
/*     */           }
/* 292 */           g.drawLine(0, yAxis, x1, yAxis);
/*     */         }
/* 294 */         if (this.yAxis != null)
/* 295 */           this.yAxis.setValues(this.minValue, scale);
/*     */       }
/*     */       catch (Exception ex)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.Graph
 * JD-Core Version:    0.6.2
 */