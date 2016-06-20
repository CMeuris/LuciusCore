package com.dataintuitive.luciuscore

import com.dataintuitive.test.BaseSparkContextSpec
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FlatSpec

/**
  * Created by toni on 22/04/16.
  */
class GeneTest extends FlatSpec with BaseSparkContextSpec {

  info("Test model for gene annotations")

  val gene: Gene = new Gene("probesetidString",
    "entrezidString",
    "ensemblidString",
    "symbolString",
    "nameString")

  "methods on a gene" should "return the method field" in {
    assert(gene.name === "nameString")
    assert(gene.symbol === "symbolString")
    assert(gene.ensemblid === "ensemblidString")
    assert(gene.entrezid === "entrezidString")
    assert(gene.probesetid === "probesetidString")
  }

  val genes = Genes(sc, "src/test/resources/genesfile.tsv", "\t")

  "Loading gene data from a file" should "work" in {
    assert(genes.genes(0).ensemblid === "ens1")
  }


}

