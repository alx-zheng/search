How the user will interact with the program:

- The user will interact with the program through the Index and Query files.
- Before searching, the user needs to run Index to allow the Query file to function. How the user sets up the Index is by
providing parameters to the args[] array, such that args[] contains the file path of the wiki they want to search
through, and the files they want to be written to: title.txt, docs.txt, and words.txt. With these set up, they can run
the Index file which should create or update title.txt, doc.txt, and words.txt.
- The user can then run Query; Query also requires parameters in the args[] array, such that args[] contains title.txt,
.txt, and words.txt, that it can pull information from. Users also have the option to add “--pagerank” in the beginning
as the first argument; if they choose to do so, PageRank will be implemented when processing and returning pages for
queries. If they choose not to, search results will be returned using only information about term frequencies.
- Then the user can run the Query file, which will prompt them to search. The user can go ahead to type in a query, and
the program should output the top 10 search results.
- They will continue to be re-prompted to search with new queries until they type “:quit”; a “quit” without the colon
will be treated as a normal query.
- If the user wants to search on a new set of Wikipedia pages, they will need to change the filepath of the Index and
rerun Index and Query.

A brief overview of your design, including how the pieces of your program fit.

In our Indexer:
- We can break up the code into sections in which we attempt to create different hashmaps. These hashmaps include
wordsToDocumentFrequencies, idToLinks, itToTitle, idsToMaxFreqs, idsToPageRank, and idToWeights.
- To begin creating these we created a pageSeq from the mainNode.
- Then we looked through each page and each of its words to perform evaluations that gave us information about where and
how we wanted to add information to our mentioned data structures.
- Towards the end of the Index class, we obtained data needed for pagerank and wrote the pagerank algorithm that
generated idsToPageRank Hashmap in which the Querier uses.

In the Querier,
- The files titles.txt, docs.txt, and words.txt are read and the hashmaps that came with it are stored in the Query
file.
- The data gathered in the Index is used to generate TF and IDF scores in which queries are ultimately scored with.
- First we “cleaned” the words in the query with stem() and calculated the TF and IDF of each of these terms. This uses
the hashmaps from Index such as idsToMaxFreq, and wordsToDocumentFrequency. We use the hashmaps that best gets each
component of the TF formula
- To calculate IDF we used hashMaps such as wordsToDocumentFrequency and idToTitle. We use the hashmaps that best gets
each component of the IDF formula
- Depending on whether the user wants to incorporate pagerank or not, our code either multiples the TF IDF score with
pagerank or ignores pageRank. Pagerank was calculated in the Index so Query takes that data from the Index for its
purposes.

A description of features you failed to implement, as well as any extra features you implemented.

We implemented all the features, and we did not implement any extra features.

A description of any known bugs in your program.

- A bug that we found in our program through coincidence was through searching for “cat”/”cats” (which yield the same
results anyways). We compared our results to that of the demo, and we found that our top 10 results included 8 articles
whose titles begin with either Emperor or Empress (examples include “Emperor Suiko,” “Emperor Buretsu,” etc.). While the
other 2 results also showed up in the demo, searching for “cat” in the demo does not yield any results related to
Emperor or Empress. This confused us, and when we went through MedWiki’s corresponding pages to see why the term
frequencies for “cat” were so high; we found that there is a link, "worldcat.org", that is used in almost all of these
pages. For some reason, our indexer was storing this under "cat" in our wordsToDocumentFrequencies hashmap, although we
couldn't figure out why.

A description of how you tested your program:

- We tested our hashmaps by using print statements throughout the writing of the code to make sure that the contents of
the hashmaps are what we want.
- We tested our program by running the Demo websites and our program to compare the top 10 results.
- We tested with and without pagerank, and since the demo uses MedWiki, we were only able to do this comparison when our
program ran on MedWiki. With Small and LargeWiki we used our best judgement to determine whether the returned searches
made sense.

Some of the words we tested for MedWiki without Pagerank:

- Victory
Program top 10:1 Navy 2 Parade 3 Heraclius 4 Paavo Lipponen 5 Javier Saviola 6 Miami Marlins 7 Indianapolis Colts
8 Indira Gandhi 9 Plurality voting system 10 Heart of Oak
Demo top 10:1. Paavo Lipponen 2. Navy 3. Parade 4. History of Palau 5. Heart of Oak 6. John Ambrose Fleming
7. Javier Saviola 8. Imperialism in Asia 9. Jordanes 10. Henry the Fowler

- Memory
Program top 10:1 Memory address register 2 Intel 80286 3 Neo Geo CD 4 Motherboard 5 Empress Suiko 6 Emperor Buretsu
7 Empress Jit? 8 Emperor Y?mei 9 Intel 8080 10 Indianapolis Colts
Demo top 10: 1. Memory address register 2. Intel 80286 3. Emperor Buretsu 4. Neo Geo CD 5. Francis van Aarssens
6. Franklin J. Schaffner 7. Empress Suiko 8. Foonly 9. Emperor Y?mei 10. Empress Jit?

- Cats
Program top 10: 1 Emperor Buretsu 2 Empress Suiko 3 Emperor Sujin 4 Kattegat 5 Empress Jit? 6 Emperor Y?mei
7 Emperor Kazan 8 Kiritimati 9 Emperor Temmu 10 Emperor Montoku
Demo top 10: 1. Kattegat 2. Kiritimati 3. Politics of Lithuania 4. Nirvana (UK band)
5. Autosomal dominant polycystic kidney 6. John Ambrose Fleming7. Mercalli intensity scale 8. Men at Work
9. W. Heath Robinson 10. Morphology (linguistics)

- pizza shop
Program top 10: 1 Millenia 2 Jonathan Swift 3 Karl Amadeus Hartmann 4 Islamabad Capital Territory
5 Economy of Guadeloupe 6 Hilversum 7 Prometheus Award 8 LEO (computer) 9 Nassau, Bahamas
10 Cuisine of the Midwestern United States
Demo top 10: 1. Millenia 2. Pizza 3. Cuisine of the Midwestern United States 4. Economy of Guadeloupe
5. Karl Amadeus Hartmann 6. Oregano 7. Islamabad Capital Territory 8. Macaroni 9. Nassau, Bahamas 10. LEO (computer)

- The
Program top 10:
Sorry, there were no results
Demo top 10:
Sorry, there were no results

- The about
Program top 10:
Sorry, there were no results
Demo top 10:
Sorry, there were no results

- The great pretender
Program top 10: 1 Kepler?Poinsot polyhedron 2 Great Schism 3 Frederick William I of Prussia 4 Lake Michigan
5 Henry VII of England 6 Geography of Hungary 7 Elbridge Gerry 8 Eusebius of Nicomedia 9 Pope Sergius I
10 Emperor Buretsu
Demo top 10: 1. Great Schism 2. Elbridge Gerry 3. Pope Sergius I 4. Callicrates 5. Kepler?Poinsot polyhedron
6. Emperor Temmu 7. Frederick William I of Prussia 8. Kattegat 9. Pope Benedict III 10. Emperor Buretsu

- Animals
Program top 10: 1 Princess Mononoke 2 Mecha 3 Okapi 4 Economy of Guadeloupe 5 Echolocation 6 Mustelidae
7 Grammatical gender 8 Neolithic 9 Free-running sleep 10 Louis Agassiz
Demo top 10: 1. Echolocation 2. Princess Mononoke 3. Mustelidae 4. Lorisidae 5. Economy of Guadeloupe 6. Suzaku
7. Free-running sleep 8. Gamete 9. Mecha 10. Harappa

- Animal
Program top 10: 1 Princess Mononoke 2 Mecha 3 Okapi 4 Economy of Guadeloupe 5 Echolocation 6 Mustelidae
7 Grammatical gender 8 Neolithic 9 Free-running sleep 10 Louis Agassiz
Demo top 10: 1. Echolocation 2. Princess Mononoke 3. Mustelidae 4. Lorisidae 5. Economy of Guadeloupe 6. Suzaku
7. Free-running sleep 8. Gamete 9. Mecha 10. Harappa

Some of the words we tested for MedWiki with Pagerank:

- The
Program top 10: Sorry, there were no results
Demo top 10: Sorry, there were no results

- Memory
Program top 10:  1 Memory address register 2 Intel 80286 3 Empress Suiko 4 Emperor Buretsu 5 Emperor Y?mei
6 Empress Jit? 7 Francis van Aarssens 8 Emacs Lisp 9 Intel 8080 10 Error detection and correction
Demo top 10:  1. Memory address register 2. Empress Suiko 3. Empress Jit? 4. Intel 80286 5. Emperor Y?mei
6. Emperor Buretsu 7. Neo Geo CD8. Francis van Aarssens 9. Emperor Temmu 10. Empress K?ken

- Reverse
Program top 10: 1 Heat engine 2 Monosaccharide 3 Field hockey 4 Elbridge Gerry5 Economy of Montserrat 6 Emperor Kazan
7 Galatea 8 Mehmed II 9 Index of motion picture terminology10 Obfuscated code
Demo top 10:  1. Northern Hemisphere 2. Pope Alexander VIII 3. Monosaccharide 4. Economy of Montserrat 5. Field hockey
6. Mehmed II 7. Pyrimidine 8. Index of motion picture terminology 9. Navy 10. Elbridge Gerry

- Emperor Kazan
Program top 10: 1 Emperor Kazan 2 Emperor Y?mei 3 Emperor Temmu 4 Emperor Buretsu 5 Empress Suiko
6 Francis II, Holy Roman Emperor 7 Emperor Sujin 8 Emperor Montoku 9 Empress Jit? 10 Empress K?ken
Demo top 10: 1. Empress Suiko 2. Empress Jit? 3. Emperor Kazan 4. Emperor Y?mei 5. Emperor Temmu6. Empress K?ken
7. Emperor Buretsu 8. Pope Benedict III9. Francis II, Holy Roman Emperor 10. Emperor Sujin

- Eat food and drink water
Program top 10: 1 Food preservation 2 Gluten 3 Beijing cuisine 4 Nutrition 5 Gin and tonic 6 Enki
7 Cuisine of the Midwestern United States 8 Economy of Guadeloupe9 Earless seal 10 Matzo
Demo top 10: 1. Johannes Nicolaus Br?nsted 2. Earless seal 3. Old Fashioned 4. Neolithic 5. Preservative
6. Northern Hemisphere 7. Equivocation 8. Mustelidae 9. Cuisine of the Midwestern United States 10. Food preservation

- “ “ (blank space)
Program top 10: Sorry, there were no results
Demo top 10: Does not search

- 85
Program top 10:  1 Demographics of Kuwait 2 FUBAR 3 Malvales 4 Demographics of French Polynesia 5 Economy of Guadeloupe
6 Geography of Kenya 7 Fritz Lang 8 High-density lipoprotein 9 Islamabad Capital Territory 10 Henry the Fowler
Demo top 10:  1. Islamabad Capital Territory 2. Malvales 3. Demographics of Kuwait 4. Geography of Nicaragua 5. Nagasaki
6. Economy of Guadeloupe 7. Demographics of French Polynesia 8. Paolo Uccello 9. Pope 10. Geography of Kenya

- Elementary
Program top 10: 1 Elementary function 2 Elementary particle 3 GRE Physics Test 4 Isa (disambiguation)
5 Grand Unified Theory 6 LMS 7 FSB 8 JMS 9 Family Educational Rights and Privacy Act 10 Googolplex
Demo top 10: 1. GRE Physics Test 2. Elementary function 3. LMS 4. Elementary particle 5. JMS 6. FSB 7. Martin Lowry
8. Marquette, Michigan 9. Googolplex 10. Netherlands

Our tests without using PageRank aligned with the results of the demo; as we tested a variety of words and edge cases
(such as putting in stop words, multiple words, multiple words all consisting of stop words, capitalized and
uncapitalized words, plural words), we felt relatively confident that our implementation was accurate.

Our tests with PageRank occasionally varied from the demo’s results; although we have many of the same pages in the
top 10 results, there are some that either appear in ours but do not appear in theirs, or appear in theirs but do not
appear in ours. Often, our ranking is also different. We attributed this to our implementation and the demo’s
implementation of PageRank differing. Since we had the flexibility to choose how we wanted to incorporate PageRank,
we decided to multiply the final TF*IDF sum across all the queries by the PageRank value. This may differ from the
demo’s implementation, so it is understandable that our results may vary in comparison.

A list of the people with whom you collaborated

Alyssa Cong, Alexander Zheng
