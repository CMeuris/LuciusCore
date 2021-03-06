package com.dataintuitive.luciuscore

import com.dataintuitive.luciuscore.Model._
import com.dataintuitive.luciuscore.SignatureModel._
import com.dataintuitive.luciuscore.utilities.Statistics.median
import scala.math.{abs,max}
import com.dataintuitive.luciuscore.Implicits._

/**
  * Transformations on (collections of) statistics, usually involving both t-stats and p-stats.
  *
  * Functionality in this library includes:
  *
  * - Aggregating a collection of t-stats (and corresponding p-stats) into one single t-stats vector.
  *
  * - Converting a t-stats vector to a rank vector, taking into account zeros and multiple occurrences.
  *
  * - Converting a signature (query) to a RankVector of type ordered or unordered
  */
object TransformationFunctions {

  /**
    * From a collection of t-stats (and p-stats), calculate the intersection/aggregate in the form of a `ValueVector`.
    * This is done by calculating the median of the _significant_ t-stats per gene.
    *
    * This version takes tuples of length 2: `([t-stats], [p-stats])`.
    */
  def aggregateStats(collection: Seq[(Array[Double], Array[Double])], significanceThreshold: Double = 0.05): ValueVector = {
    collection.
      flatMap { // Join datasets and add index for genes
        x => {
          val t = x._1
          val p = x._2
          p.zip(t).zipWithIndex.map { case ((p, t), i) => (i, (t, p)) }
        }
      }
      .groupBy { // Group by gene
        case (i, (t, p)) => i
      }
      .map { // Select significant t-values, else 0.
        case (i, a) => (i, a.map { case (j, (t, p)) => if (p < significanceThreshold) t else 0.0 })
      }
      .map { // Calculate median for the set of significant expressions
        case (i, a) => (i, if (a.min == 0.0) 0.0 else median(a))
      }
      .toArray
      .sorted // Make sure the result is sorted again.
      .map(_._2)
  }

  /**
    * Convert t-stats into a rank vector. Take into account zero values of t-stats.
    *
    * Remark: Ranks start at 1.
    */
  def stats2RankVector(tp: (Array[Double], Array[Double])): RankVector = {

    // Helper function
    def avgUnsignedRank(ranks: RankVector): Double = {
      ranks.foldLeft(0.0)(_ + _) / ranks.length
    }

    val v = tp._1

    val zeros = v.count(_ == 0.0)

    v.zipWithIndex
      .sortBy {
        case (v, i) => Math.abs(v)
      }
      .zipWithIndex // add an index, this becomes the rank
      .sortBy { case ((v, i), j) => i } // sort wrt original index
      .map {
      // make sure ranks start at 1 rather than 0
      case ((v, i), j) => ((v, i), j + 1)
    }
      .map {
        // Make sure zero entries are not counted in rank
        case ((v, i), j) => if (v == 0.0) ((v, i), 0) else ((v, i), j - zeros)
      }
      .map {
        // For every feature: keep a map of the values relevant at this stage:
        // - The original value in the vector
        // - The original index in the vector
        // - The unsigned rank
        case ((v, i), j) => Map[String, Double]("value" -> v, "origIndex" -> i, "unsignedRank" -> j)
      }
      .groupBy {
        // The specifics of the average rank calculation:
        // If the absolute value is the same, we should calculate the average rank for these
        x => Math.abs(x("value"))
      }
      .values
      .map {
        // Add an annotation for the average unsigned rank
        vector => vector.map(x => x ++ Map("avgUnsignedRank" -> avgUnsignedRank(vector.map(_ ("unsignedRank")).toArray)))
      }
      .flatMap(x => x).toList
      .sortBy(_ ("origIndex")) // Recover the original ordering
      .map {
      // Add the sign to the and return only the ranks
      x => if (x("value") >= 0) x("avgUnsignedRank") else -x("avgUnsignedRank")
    }
      .toArray
  }

  /**
    * Convert an index-based signature to an ordered rank vector.
    *
    * Remark 1: We need to provide the length of the resulting RankVector.
    *
    * Remark 2: Indices are 1-based.
    */
  def signature2OrderedRankVector(s: IndexSignature, length: Int): RankVector = {
    val sLength = s.signature.length
    val ranks = (sLength to 1 by -1).map(_.toDouble)
    val unsignedRanks = s.signature zip ranks
    val signedRanks = unsignedRanks
      .map { case (signedIndex, unsignedRank) =>
        (signedIndex.abs.toInt, (signedIndex.sign + unsignedRank).toDouble)
      }.toMap
    val asSeq = for (el <- 1 to length by 1) yield signedRanks.getOrElse(el, 0.0)
    asSeq.toArray
  }

  /**
    * Convert an index-based signature to an unordered rank vector.
    *
    * Remark 1: We need to provide the length of the resulting RankVector.
    *
    * Remark 2: Indices are 1-based.
    */
  def signature2UnorderedRankVector(s: IndexSignature, length: Int): RankVector = {
    val sLength = s.signature.length
    val ranks = (sLength to 1 by -1).map(_ => 1.0)  // This is the only difference with the above, all ranks are 1
    val unsignedRanks = s.signature zip ranks
    val signedRanks = unsignedRanks
      .map { case (signedIndex, unsignedRank) =>
        (signedIndex.abs.toInt, (signedIndex.sign + unsignedRank).toDouble)
      }.toMap
    val asSeq = for (el <- 1 to length by 1) yield signedRanks.getOrElse(el, 0.0)
    asSeq.toArray
  }


  // Be careful: offset 1 for vectors for consistency!
  def nonZeroElements(v: RankVector, offset:Int = 1): Array[(Index, Rank)] = {
    v.zipWithIndex
      .map(x => (x._1, x._2 + offset))
      .map(_.swap)
      .filter(_._2 != 0.0)
  }

  // Convert rank vector to index signature
  // This is the poor man's approach, not taking into account duplicate entries and such.
  // Be careful, signature and vector indices are 1-based
  def rankVector2IndexSignature(v: RankVector): IndexSignature = {
    val nonzero = nonZeroElements(v)
    val asArrayInt = nonzero.map {
      case (unsignedIndex, signedRank) => ((signedRank.abs / signedRank) * unsignedIndex).toInt
    }
    val asArrayString = asArrayInt.map(_.toString)
    new IndexSignature(asArrayString)
  }


}
