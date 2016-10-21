/*     */ package modelization.data.codons.tables;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.EnumSet;
/*     */ import java.util.Set;
/*     */ import metapiga.modelization.data.Codon;
/*     */ 
/*     */ public class MoldProtoCoelMitochCode extends CodonTransitionTable
/*     */ {
/*     */   protected Set<Codon> stopCodons;
/*     */   protected Set<Codon> phenylalanine;
/*     */   protected Set<Codon> leucine;
/*     */   protected Set<Codon> isoleucine;
/*     */   protected Set<Codon> methionine;
/*     */   protected Set<Codon> valine;
/*     */   protected Set<Codon> serine;
/*     */   protected Set<Codon> proline;
/*     */   protected Set<Codon> threonine;
/*     */   protected Set<Codon> alanine;
/*     */   protected Set<Codon> tyrosine;
/*     */   protected Set<Codon> histidine;
/*     */   protected Set<Codon> glutamine;
/*     */   protected Set<Codon> asparagine;
/*     */   protected Set<Codon> lysine;
/*     */   protected Set<Codon> asparticAcid;
/*     */   protected Set<Codon> glutamicAcid;
/*     */   protected Set<Codon> cysteine;
/*     */   protected Set<Codon> tryptophan;
/*     */   protected Set<Codon> arginine;
/*     */   protected Set<Codon> glycine;
/*     */   protected Set<Codon> opal;
/*     */   protected Set<Codon> ochre;
/*     */   protected Set<Codon> amber;
/*     */   protected ArrayList<Set<Codon>> aminoacids;
/*     */ 
/*     */   public MoldProtoCoelMitochCode()
/*     */   {
/*  17 */     this.stopCodons = EnumSet.of(Codon.TAA, Codon.TAG);
/*     */ 
/*  50 */     this.aminoacids = new ArrayList();
/*     */ 
/*  53 */     this.phenylalanine = EnumSet.of(Codon.TTT, Codon.TTC);
/*  54 */     this.leucine = EnumSet.of(Codon.CTT, Codon.CTC, Codon.CTA, Codon.CTG);
/*  55 */     this.leucine.add(Codon.TTA);
/*  56 */     this.leucine.add(Codon.TTG);
/*  57 */     this.isoleucine = EnumSet.of(Codon.ATT, Codon.ATC, Codon.ATA);
/*  58 */     this.methionine = EnumSet.of(Codon.ATG);
/*  59 */     this.valine = EnumSet.of(Codon.GTT, Codon.GTC, Codon.GTA, Codon.GTG);
/*  60 */     this.serine = EnumSet.of(Codon.TCT, Codon.TCC, Codon.TCA, Codon.TCG);
/*  61 */     this.proline = EnumSet.of(Codon.CCT, Codon.CCA, Codon.CCC, Codon.CCG);
/*  62 */     this.threonine = EnumSet.of(Codon.ACT, Codon.ACC, Codon.ACA, Codon.ACG);
/*  63 */     this.alanine = EnumSet.of(Codon.GCT, Codon.GCC, Codon.GCA, Codon.GCG);
/*  64 */     this.tyrosine = EnumSet.of(Codon.TAT, Codon.TAC);
/*  65 */     this.ochre = EnumSet.of(Codon.TAA);
/*  66 */     this.amber = EnumSet.of(Codon.TAG);
/*  67 */     this.histidine = EnumSet.of(Codon.CAT, Codon.CAC);
/*  68 */     this.glutamine = EnumSet.of(Codon.CAA, Codon.CAG);
/*  69 */     this.asparagine = EnumSet.of(Codon.AAT, Codon.AAC);
/*  70 */     this.lysine = EnumSet.of(Codon.AAA, Codon.AAG);
/*  71 */     this.asparticAcid = EnumSet.of(Codon.GAT, Codon.GAC);
/*  72 */     this.glutamicAcid = EnumSet.of(Codon.GAA, Codon.GAG);
/*  73 */     this.cysteine = EnumSet.of(Codon.TGT, Codon.TGC);
/*  74 */     this.tryptophan = EnumSet.of(Codon.TGG, Codon.TGA);
/*  75 */     this.arginine = EnumSet.of(Codon.CGT, Codon.CGC, Codon.CGA, Codon.CGG);
/*  76 */     this.serine.add(Codon.AGT);
/*  77 */     this.serine.add(Codon.AGC);
/*  78 */     this.arginine.add(Codon.AGA);
/*  79 */     this.arginine.add(Codon.AGG);
/*  80 */     this.glycine = EnumSet.of(Codon.GGT, Codon.GGC, Codon.GGA, Codon.GGG);
/*     */ 
/*  82 */     this.aminoacids.add(this.alanine);
/*  83 */     this.aminoacids.add(this.arginine);
/*  84 */     this.aminoacids.add(this.asparagine);
/*  85 */     this.aminoacids.add(this.cysteine);
/*  86 */     this.aminoacids.add(this.glutamine);
/*  87 */     this.aminoacids.add(this.glycine);
/*  88 */     this.aminoacids.add(this.histidine);
/*  89 */     this.aminoacids.add(this.isoleucine);
/*  90 */     this.aminoacids.add(this.leucine);
/*  91 */     this.aminoacids.add(this.lysine);
/*  92 */     this.aminoacids.add(this.methionine);
/*  93 */     this.aminoacids.add(this.phenylalanine);
/*  94 */     this.aminoacids.add(this.proline);
/*  95 */     this.aminoacids.add(this.serine);
/*  96 */     this.aminoacids.add(this.threonine);
/*  97 */     this.aminoacids.add(this.tyrosine);
/*  98 */     this.aminoacids.add(this.valine);
/*  99 */     this.aminoacids.add(this.asparticAcid);
/* 100 */     this.aminoacids.add(this.glutamicAcid);
/* 101 */     this.aminoacids.add(this.tryptophan);
/*     */   }
/*     */ 
/*     */   public boolean isSynonymous(Codon fromCodon, Codon toCodon)
/*     */   {
/* 106 */     for (Set aminoacid : this.aminoacids) {
/* 107 */       if ((aminoacid.contains(fromCodon)) && (aminoacid.contains(toCodon)))
/* 108 */         return true;
/*     */     }
/* 110 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isStopCodon(Codon codon)
/*     */   {
/* 116 */     if (this.stopCodons.contains(codon))
/* 117 */       return true;
/* 118 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.codons.tables.MoldProtoCoelMitochCode
 * JD-Core Version:    0.6.2
 */