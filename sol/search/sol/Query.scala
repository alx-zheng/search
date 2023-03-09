package search.sol

import java.io._

import search.src.FileIO
import search.src.PorterStemmer.stem

import scala.collection.{immutable, mutable}
import scala.collection.mutable.HashMap
import scala.util.matching.Regex

/**
 * Represents a query REPL built off of a specified index
 *
 * @param titleIndex    - the filename of the title index
 * @param documentIndex - the filename of the document index
 * @param wordIndex     - the filename of the word index
 * @param usePageRank   - true if page rank is to be incorporated into scoring
 */
class Query(titleIndex: String, documentIndex: String, wordIndex: String,
            usePageRank: Boolean) {

  // Maps the document ids to the title for each document
  private val idsToTitle = new HashMap[Int, String]

  // Maps the document ids to the euclidean normalization for each document
  private val idsToMaxFreqs = new HashMap[Int, Double]

  // Maps the document ids to the page rank for each document
  private val idsToPageRank = new HashMap[Int, Double]

  // Maps each word to a map of document IDs and frequencies of documents that
  // contain that word
  private val wordsToDocumentFrequencies = new HashMap[String, HashMap[Int, Double]]

  readFiles()

  /**
   * method to clean the query through a regex
   * @param dirty - the input query word
   * @param regString - the regex
   * @return
   */
  def cleanQuery(dirty: String, regString : Regex): List[String] = {
    val matchesIterator = regString.findAllMatchIn(dirty)
    val matchesList = matchesIterator.toList.map{aMatch => aMatch.matched}
    matchesList
  }

  /**
   * method to see if wordsToDocumentFrequencies hashmap contains an input string
   * @param word - input string
   * @return
   */
  private def wordsToDocFreqContains(word: String): Boolean = {
    wordsToDocumentFrequencies.contains(word)
  }

  /**
   * Handles a single query and prints out results
   *
   * @param userQuery - the query text
   */
  private def query(userQuery: String) {
    val regex1 = new Regex("""[^\W_d]+'[^\W_d]+|[^\W_d]+""")
    var wordList = cleanQuery(userQuery, regex1).map(word => stem(word))

    wordList = wordList.filter(wordsToDocFreqContains)

    /**
     * after filtering out words that are not in the corpus/are stop words, check if list of words is empty
     * if empty, returns "Sorry, there were no results"
     */
    if (wordList.isEmpty) {
      println("Sorry, there were no results")
      return
    }

    // creates word to TF hashmap
    // val idToTF: mutable.HashMap[Int, Double] = new mutable.HashMap()
    // val wordToDocTF = mutable.HashMap[String, mutable.HashMap[Int, Double]]()
    val idToQueryTF = mutable.HashMap[Int, mutable.HashMap[String, Double]]()

    // creates page to word to tf hashmap
    for (id <- idsToTitle.keys) {
      for (queryWord: String <- wordList) {
        // checks if wordsToDocumentFrequencies of queryWord contains the page (is this word in this page)
        // if it does, calculate tf
        if (wordsToDocumentFrequencies(queryWord).contains(id)) {
          val queryWordFreq = wordsToDocumentFrequencies(queryWord)(id)
          val tf = queryWordFreq / idsToMaxFreqs(id)
          if (!idToQueryTF.contains(id)) {
            val tempHash = new mutable.HashMap[String, Double]()
            tempHash.put(queryWord, tf)
            idToQueryTF.put(id, tempHash)
          } else {
            idToQueryTF(id) += (queryWord -> tf)
          }
        } else {
          // if word isn't in page, tf = 0 because queryWordFreq = 0
          if (!idToQueryTF.contains(id)) {
            val tempHash = new mutable.HashMap[String, Double]()
            tempHash.put(queryWord, 0.0)
            idToQueryTF.put(id, tempHash)
          } else {
            idToQueryTF(id) += (queryWord -> 0.0)
          }
        }
      }
    }

    // creates word to IDF hashmap
    val wordToIDF: mutable.HashMap[String, Double] = new mutable.HashMap()

    // calculates IDF
    val numDocs = idsToTitle.size
    for (queryWord <- wordList) {
      val numDocsWithWord = wordsToDocumentFrequencies(queryWord).size
      wordToIDF.put(queryWord, Math.log(numDocs / numDocsWithWord))
    }

    // hashmap that contains the scores for each page
    var idToScore = immutable.ListMap[Int, Double]()

    // if pageRank is true, then multiply total sum of tf and idf by pagerank
    if (usePageRank) {
      for (id <- idsToTitle.keys) {
        var sum = 0.0
        for (word: String <- wordList) {
          sum += idToQueryTF(id)(word) * wordToIDF(word)
        }
        idToScore += (id -> (sum * idsToPageRank(id)))
      }
    } else {
      // else return sum of tf*idf
      for (id <- idsToTitle.keys) {
        var sum = 0.0
        for (word: String <- wordList) {
          sum += idToQueryTF(id)(word) * wordToIDF(word)
        }
        idToScore += (id -> sum)
      }
    }

    // turns the hashmap of idToScore into an array, sorts the hashmap from greatest score to lowest, and prints out
    // top 10 results
    val out = immutable.ListMap(idToScore.toSeq.sortBy(_._2):_*)
    var outArray = Array[Int](idsToTitle.size)
    for(id <- out.keys){
      outArray +:= id
    }
    printResults(outArray)
  }

  /**
   * Format and print up to 10 results from the results list
   *
   * @param results - an array of all results to be printed
   */
  private def printResults(results: Array[Int]) {
    for (i <- 0 until Math.min(10, results.size - 1)) {
      println("\t" + (i + 1) + " " + idsToTitle(results(i)))
    }
  }

  /*
   * Reads in the text files.
   */
  def readFiles(): Unit = {
    FileIO.readTitles(titleIndex, idsToTitle)
    FileIO.readDocuments(documentIndex, idsToMaxFreqs, idsToPageRank)
    FileIO.readWords(wordIndex, wordsToDocumentFrequencies)
  }

  /**
   * Starts the read and print loop for queries
   */
  def run() {
    val inputReader = new BufferedReader(new InputStreamReader(System.in))

    // Print the first query prompt and read the first line of input
    print("search> ")
    var userQuery = inputReader.readLine()

    // Loop until there are no more input lines (EOF is reached)
    while (userQuery != null) {
      // If ":quit" is reached, exit the loop
      if (userQuery == ":quit") {
        inputReader.close()
        return
      }

      // Handle the query for the single line of input
      query(userQuery)

      // Print next query prompt and read next line of input
      print("search> ")
      userQuery = inputReader.readLine()
    }

    inputReader.close()
  }
}


object Query {
  def main(args: Array[String]) {
    try {
      // Run queries with page rank
      var pageRank = false
      var titleIndex = 0
      var docIndex = 1
      var wordIndex = 2
      if (args.size == 4 && args(0) == "--pagerank") {
        pageRank = true;
        titleIndex = 1
        docIndex = 2
        wordIndex = 3
      } else if (args.size != 3) {
        println("Incorrect arguments. Please use [--pagerank] <titleIndex> "
          + "<documentIndex> <wordIndex>")
        System.exit(1)
      }
      val query: Query = new Query(args(titleIndex), args(docIndex), args(wordIndex), pageRank)
      query.readFiles()
      query.run()
    } catch {
      case _: FileNotFoundException =>
        println("One (or more) of the files were not found")
      case _: IOException => println("Error: IO Exception")
    }
  }
}
