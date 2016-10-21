/*     */ package modelization.data.codons.tables;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.EnumSet;
/*     */ import java.util.Set;
/*     */ import metapiga.modelization.data.Codon;
/*     */ 
/*     */ public class InvertebrateMitochondrialCode extends CodonTransitionTable
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
/*     */   public InvertebrateMitochondrialCode()
/*     */   {
/*  12 */     this.stopCodons = EnumSet.of(Codon.TAA, Codon.TAG);
/*     */ 
/*  45 */     this.aminoacids = new ArrayList();
/*     */ 
/*  48 */     this.phenylalanine = EnumSet.of(Codon.TTT, Codon.TTC);
/*  49 */     this.leucine = EnumSet.of(Codon.CTT, Codon.CTC, Codon.CTA, Codon.CTG);
/*  50 */     this.leucine.add(Codon.TTA);
/*  51 */     this.leucine.add(Codon.TTG);
/*  52 */     this.isoleucine = EnumSet.of(Codon.ATT, Codon.ATC);
/*  53 */     this.methionine = EnumSet.of(Codon.ATG, Codon.ATA);
/*  54 */     this.valine = EnumSet.of(Codon.GTT, Codon.GTC, Codon.GTA, Codon.GTG);
/*  55 */     this.serine = EnumSet.of(Codon.TCT, Codon.TCC, Codon.TCA, Codon.TCG, Codon.AGA);
/*  56 */     this.proline = EnumSet.of(Codon.CCT, Codon.CCA, Codon.CCC, Codon.CCG);
/*  57 */     this.threonine = EnumSet.of(Codon.ACT, Codon.ACC, Codon.ACA, Codon.ACG);
/*  58 */     this.alanine = EnumSet.of(Codon.GCT, Codon.GCC, Codon.GCA, Codon.GCG);
/*  59 */     this.tyrosine = EnumSet.of(Codon.TAT, Codon.TAC);
/*  60 */     this.ochre = EnumSet.of(Codon.TAA);
/*  61 */     this.amber = EnumSet.of(Codon.TAG);
/*  62 */     this.histidine = EnumSet.of(Codon.CAT, Codon.CAC);
/*  63 */     this.glutamine = EnumSet.of(Codon.CAA, Codon.CAG);
/*  64 */     this.asparagine = EnumSet.of(Codon.AAT, Codon.AAC);
/*  65 */     this.lysine = EnumSet.of(Codon.AAA, Codon.AAG);
/*  66 */     this.asparticAcid = EnumSet.of(Codon.GAT, Codon.GAC);
/*  67 */     this.glutamicAcid = EnumSet.of(Codon.GAA, Codon.GAG);
/*  68 */     this.cysteine = EnumSet.of(Codon.TGT, Codon.TGC);
/*  69 */     this.tryptophan = EnumSet.of(Codon.TGG, Codon.TGA);
/*  70 */     this.arginine = EnumSet.of(Codon.CGT, Codon.CGC, Codon.CGA, Codon.CGG);
/*  71 */     this.serine.add(Codon.AGT);
/*  72 */     this.serine.add(Codon.AGC);
/*  73 */     this.serine.add(Codon.AGG);
/*  74 */     this.glycine = EnumSet.of(Codon.GGT, Codon.GGC, Codon.GGA, Codon.GGG);
/*     */ 
/*  76 */     this.aminoacids.add(this.alanine);
/*  77 */     this.aminoacids.add(this.arginine);
/*  78 */     this.aminoacids.add(this.asparagine);
/*  79 */     this.aminoacids.add(this.cysteine);
/*  80 */     this.aminoacids.add(this.glutamine);
/*  81 */     this.aminoacids.add(this.glycine);
/*  82 */     this.aminoacids.add(this.histidine);
/*  83 */     this.aminoacids.add(this.isoleucine);
/*  84 */     this.aminoacids.add(this.leucine);
/*  85 */     this.aminoacids.add(this.lysine);
/*  86 */     this.aminoacids.add(this.methionine);
/*  87 */     this.aminoacids.add(this.phenylalanine);
/*  88 */     this.aminoacids.add(this.proline);
/*  89 */     this.aminoacids.add(this.serine);
/*  90 */     this.aminoacids.add(this.threonine);
/*  91 */     this.aminoacids.add(this.tyrosine);
/*  92 */     this.aminoacids.add(this.valine);
/*  93 */     this.aminoacids.add(this.asparticAcid);
/*  94 */     this.aminoacids.add(this.glutamicAcid);
/*  95 */     this.aminoacids.add(this.tryptophan);
/*     */   }
/*     */ 
/*     */   public boolean isSynonymous(Codon fromCodon, Codon toCodon)
/*     */   {
/* 100 */     for (Set aminoacid : this.aminoacids) {
/* 101 */       if ((aminoacid.contains(fromCodon)) && (aminoacid.contains(toCodon)))
/* 102 */         return true;
/*     */     }
/* 104 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isStopCodon(Codon codon)
/*     */   {
/* 110 */     if (this.stopCodons.contains(codon))
/* 111 */       return true;
/* 112 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.codons.tables.InvertebrateMitochondrialCode
 * JD-Core Version:    0.6.2
 */