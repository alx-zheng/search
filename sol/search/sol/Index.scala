package search.sol
import java.io.FileNotFoundException
import search.src.StopWords.isStopWord
import search.src.PorterStemmer.stem
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.xml.{Node, NodeSeq}
import search.src.FileIO



/**
 * Provides an XML indexer, produces files for a querier
 *
 * @param inputFile - the filename of the XML wiki to be indexed
 */
class Index(val inputFile: String) {


  /**
   * method to clean a string using a regex
   * @param dirty - input string
   * @param regString - input regex
   * @return
   */
  private def cleanLink(dirty: String, regString : Regex): List[String] = {
    val matchesIterator = regString.findAllMatchIn(dirty)
    val matchesList = matchesIterator.toList.map{aMatch => aMatch.matched}
    matchesList
  }

  /**
   * method to add a word to the wordToDocumentFrequencies hashmap
   * @param word - input word
   * @param idSeq - the id number
   * @param wordToDocumentFrequencies - the input hashtable (wordsToDocumentFrequencies)
   */
  private def addToWordToDocumentFreqs(word: String, idSeq: NodeSeq,
                               wordToDocumentFrequencies : mutable.HashMap[String, mutable.HashMap[Int, Double]])
  : Unit = {
    if (!isStopWord(word)) {
      if (wordToDocumentFrequencies.contains(word)) { //word already in the hash
        if (wordToDocumentFrequencies(word).contains(idSeq.text.trim().toInt)) { //already contain id
          wordToDocumentFrequencies(word)(idSeq.text.trim().toInt) =
            wordToDocumentFrequencies(word)(idSeq.text.trim().toInt) + 1 //add freq
        }
        else { //doesnt contain id already
          wordToDocumentFrequencies(word).put(idSeq.text.trim().toInt, 1)
        }
      }
      else { // creates new inner hashmap with added frequency 1
        val tempHash = new mutable.HashMap[Int, Double]()
        tempHash.put(idSeq.text.trim().toInt, 1)
        wordToDocumentFrequencies.put(word, tempHash)
      }
    }
  }

  /**
   * method to calculate the euclidean distance
   * @param R - copy of rprime that gets updated
   * @param RPRIME - current ranking
   * @return
   */
  private def euDistance(R: Array[Double], RPRIME: Array[Double]): Double ={
    var sum = 0.0
    for (i <- R.indices){
      sum = sum + Math.pow((RPRIME(i)- R(i)), 2)
    }
    Math.sqrt(sum)// when greater than 0.001 be true
  }

  /**
   * method to find the minimum id (key) in idsToTitle
   * @param idToTitle
   * @return
   */
  private def findMinKey(idToTitle: mutable.HashMap[Int, String]): Int = {
    idToTitle.keysIterator.min
  }

  //CONSTANTS
  private val regex = new Regex("""\[\[[^\[]+?\]\]|[^\W_d]+'[^\W_d]+|[^\W_d]+""")
  private val mainNode: Node = xml.XML.loadFile(inputFile)


  private val wordsToDocumentFrequencies: mutable.HashMap[String, mutable.HashMap[Int, Double]] = new mutable.HashMap()
  private val idToLinks: mutable.HashMap[Int, ListBuffer[String]] = new mutable.HashMap() //for pagerank later
  private val idToTitle: mutable.HashMap[Int, String] = new mutable.HashMap()
  private val idsToMaxFreqs: mutable.HashMap[Int, Double] = new mutable.HashMap()
  private val idsToPageRank: mutable.HashMap[Int, Double] = new mutable.HashMap()
  private val idsToWeights: mutable.HashMap[Int, Double] = new mutable.HashMap()

  //going into the pages

  private val pageSeq: NodeSeq = mainNode \ "page"

  //one iteration of this loop is looking at 1 page in the pageSeq
  for(page: NodeSeq <- pageSeq) {
    val matchesIterator = regex.findAllMatchIn(page.text)
    val matchesList = matchesIterator.toList.map { aMatch => aMatch.matched } //list of strings from 1 page that is matched to regex
    val idSeq: NodeSeq = page \ "id" //should be just 1 id
    val titleSeq: NodeSeq = page \ "title" //should be just 1 title

    //adding idToTitle

    idToTitle.put(key = idSeq.text.trim().toInt, value = titleSeq.text.trim())

    //making the idToLinks Hashmap

    val listOfLinks = ListBuffer[String]()
    for (word: String <- matchesList) {
      var w = word
      //checking for links
      if (word.contains("[[") && word.contains("]]")) { //check if it ends with ]]
        //remove brackets
        w = w.substring(2, w.length() - 2)
        //if no pipe
        if (w.contains("|")) {
          val pipeindex = w.indexOf("|")
          //add the second half
          w = w.substring(pipeindex + 1, w.length())
        }

        // add to listOfLinks list
        if (!w.equals(titleSeq.text.trim())) {
          listOfLinks.append(w)
        }
      }
      //if string has multiple words
      if (w.contains(" ") || w.contains("|") || w.contains(":") || w.contains(".") || w.contains("[")) {
        val regex1 = new Regex("""[^\W_d]+'[^\W_d]+|[^\W_d]+""")
        val wordList = cleanLink(w, regex1)
        val stemmedList = wordList.map(x => stem(x))
        for (word: String <- stemmedList) {
          addToWordToDocumentFreqs(word.toLowerCase(), idSeq, wordsToDocumentFrequencies)
          }
      }
      //normal single word string
      else {
        val stemmedWord = stem(w.toLowerCase())
        addToWordToDocumentFreqs(stemmedWord.toLowerCase(), idSeq, wordsToDocumentFrequencies)
      }

      //adding to listOfLinks to idToLinks Hashmap, making listOfLinks distinct so it doesn't count multiple links to
      // same page
      idToLinks.put(idSeq.text.trim().toInt, listOfLinks.distinct)
    }

    // calculate max frequency of word for each page, add to idtoMaxFreqs hashmap
    var frequency: Double = 0
    for (word : String <- wordsToDocumentFrequencies.keys) {
      if (wordsToDocumentFrequencies(word).contains(idSeq.text.trim().toInt) &&
        wordsToDocumentFrequencies(word)(idSeq.text.trim().toInt) > frequency) {
        frequency = wordsToDocumentFrequencies(word)(idSeq.text.trim().toInt)
      }
    }

    idsToMaxFreqs.put(idSeq.text.trim().toInt, frequency)
  }

  // calculates the page weight for each page, with constant e = 0.15
  for (id <- idToTitle.keys) {
    var nk = idToLinks(id).length
    if (nk == 0) {
      nk = idToTitle.size - 1
    }

    val e = 0.15
    val totalPages = idToTitle.size
    val pageWeight = (e/totalPages) + (1-e)*(1 / nk)
    idsToWeights.put(id, pageWeight)
  }

  // calculates PageRank for each page

  private val n = idToTitle.size
  private var r: Array[Double] = Array.fill(n)(0)
  private val rprime: Array[Double] = Array.fill(n)(1.0/n)
  private val minId = findMinKey(idToTitle)
  while (euDistance(r, rprime) > 0.001) {
    r = rprime
      for (j <- idToTitle.keys){
        rprime(j - minId) = 0
        for (k <- idToTitle.keys){
          if (idToLinks(k).contains(idToTitle(j)) && k != j){ //if k links to j
            rprime(j - minId) = rprime(j - minId) + (idsToWeights(k)*(r(k - minId)))
          }
          else { //k doesnt link to j
            rprime(j - minId) = rprime(j - minId) + (0.15/n)*(r(k - minId))
          }
        }
        idsToPageRank.put(j, rprime(j - minId))
      }
  }
}

object Index {
  def main(args: Array[String]) {
  try{
    val indexObj = new Index(args(0))
    FileIO.printTitleFile(args(1), indexObj.idToTitle)
    FileIO.printDocumentFile(args(2), indexObj.idsToMaxFreqs, indexObj.idsToPageRank)
    FileIO.printWordsFile(args(3), indexObj.wordsToDocumentFrequencies)
  }
    catch{
      case e: FileNotFoundException => println("Couldn't find your file")
    }
  }
}
