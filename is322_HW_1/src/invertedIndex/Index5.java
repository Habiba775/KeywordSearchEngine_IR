/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------

    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.
    public HashMap<String, DictEntry> index; // The inverted index

    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }

    //---------------------------------------------

    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();

        //enclose the posting list between a pair of square brackets []
        System.out.print("[");

        //iterate over the posting
        while (p != null) {
            /// -4- **** complete here ****
            // fix get rid of the last comma

            //print the docID of the current posting
            System.out.print("" + p.docId);

            //after moving to the next posting, check if it's null (which will mean that the previous posting was the last one). Only if the posting isn't the last one, write a comma.
            p = p.next;
            if (p != null) {
                System.out.print(",");
            }
        }
        System.out.println("]");
    }

    //---------------------------------------------

    public void printDictionary() {
        //create an iterator on the "index" map
        Iterator it = index.entrySet().iterator();
        //iterator over the index
        while (it.hasNext()) {
            //save the current map entry (key-value) in "pair"
            Map.Entry pair = (Map.Entry) it.next();
            //save the value of the current map entry in "dd"
            DictEntry dd = (DictEntry) pair.getValue();
            //this prints the entire index entry, which is, respectively; the term, the document frequency, and the posting list
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        //prints the size of the index (# of entries)
        System.out.println("*** Number of terms = " + index.size());
    }
 
    //-----------------------------------------------

    public void buildIndex(String[] files) {  // from disk not from the internet
        //give each file/document a unique id, starting with 0 and incrementing it by one for every file
        int fid = 0;
        for (String fileName : files) {
            //open the file for reading
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                //if the current file ("fileName") isn't found in the sources map, add it
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;  //will store one line in the file at a time
                int flen = 0;   //will store the file's length

                //read every line in the file
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    ///**** hint   flen +=  ________________(ln, fid);
                    //add the lenth of the current line "ln" to the flen
                    flen += indexOneLine(ln, fid);
                }
                //store the file's length in the length attribute of the source Record
                sources.get(fid).length = flen;
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        //   printDictionary();
    }

    //----------------------------------------------------------------------------

    //this method has 2 primary functionalities:
    // 1. return the length of the ln argument in the form of the variable "flen"
    // 2. add each word in the ln argument to the index by following the indexing steps
    public int indexOneLine(String ln, int fid) {
        int flen = 0;

        //split the line to its composing words. This is done by using the regex "\\W+", which stands for any number of consecutive occurrences of any non-word character (anything other than a letter, digit, or underscore).
        String[] words = ln.split("\\W+");
        //String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        //the length of the line, which will be returned at the end = # of words in the line
        flen += words.length;
        //iterate over every word in the ln
        for (String word : words) {
            //standardize all words to lowercase
            word = word.toLowerCase();
            //step 1: remove stop words, where they won't even be added to the index
            if (stopWord(word)) {
                continue;
            }
            //step 2: if not a stop word, change it to its stem word
            word = stemWord(word);

            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            //by now we are sure that there is an entry for word, whether it has a posting list or not.
            //if this document with id == fid isn't in the posting list of "word", add fid to the posting list
            if (!index.get(word).postingListContains(fid)) {
                //increase the number of documents where word appears by one, which is the document with fid
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                //if word has no posting list, create one for it and add fid to it, then equate pList with last, where both of them point to fid
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                //else if it has a posting list, just add fid to it and move the "last" pointer to point to the added fid
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            //else if fid is already in the posting list of word, just increase dtf by one, which indicates the number of occurrences of word in this document
            } else {
                index.get(word).last.dtf += 1;
            }
            //set the term_freq in the collection
            //increase the number of times this term "word" appears in the collection
            index.get(word).term_freq += 1;

            //TODO: comment this conditional ??????????????????????????
            if (word.equalsIgnoreCase("lattice")) {
                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }
        }
        return flen;
    }

    //----------------------------------------------------------------------------

    //check if word is a stop word. It's assumed in this method that any word composed on one character or belonging to any of the words in the first conditional is a stop word.
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;
    }

    //----------------------------------------------------------------------------

    //should return the stem of word
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------

    // finds the intersection of two sorted posting lists (pL1 and pL2).
    // It returns a new posting list (answer)containing only the document IDs that appear in both input lists.
    Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null;      //will hold the PL with the common documents in the two arguments
        Posting last = null;        //an auxiliary PL

        //iterate over the two posting lists
        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                Posting match = new Posting(pL1.docId); // Create a new Posting node with the matched docId
                if (answer == null) {// If this is the first match, initialize the answer list
                    answer = match;
                    last = match;
                } else {// else append to the answer list
                    last.next = match;
                    last = match; // move last pointer
                }
                //move both pointers forward as the current document is proceeded
                pL1 = pL1.next;
                pL2 = pL2.next;
                //if pl1 document id smaller than pl2 move pl1 forward
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                // if pl2 document id smaller than pl1 move pl1 forward
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    //this method prints the info of the documents where all the words in the "phrase" argument appear in each, then returns this info in the string "result"
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        //split the phrase to its words using a regex "\\W+", which stands for any number of consecutive occurrences of any non-word character (anything other than a letter, digit, or underscore)
        //then store the number of words in len
        String[] words = phrase.split("\\W+");
        int len = words.length;
        
        //fix this if word is not in the hash table will crash...
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        while (i < len) {
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        //here, "posting" will hold a posting list with all the docs such that each document of them contains all the words in the phrase
        //iterate over the posting list
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            //output the following for each doc in the PL: doc ID, doc title, and doc length
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }
    
    //---------------------------------

    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false; // keep track if the words are sorted
        String sTmp; // temp variable for swapping
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;// assume the array is sorted
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]); // compare adjacent words
                if (compare > 0) { // if compare>0 means that words[i] comes after words[i+1] so swap them
                    sTmp = words[i]; //put the current word in temporary variable
                    //swap
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    //make sorted false again to indicate that the array need another pass
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------

//  this function takes a file name and stores source records and invertedList into a file
    public void store(String storageName) {
        try {
            //  modify this path and add the appropriate one
            String pathToStorage = "is322_HW_1/"+storageName;
            // open file for reading
            Writer wr = new FileWriter(pathToStorage);
            // write the source records into the file
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                //ensuring the record is correctly parsed
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            //write section2 as a delimiter to separate source records from the inverted index
            wr.write("section2" + "\n");

            //initialize an iterator to iterate over the invertedList
            Iterator it = index.entrySet().iterator();

            //iterate until reach the end of the invertedList
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();//word
                DictEntry dd = (DictEntry) pair.getValue();//document frequency, term frequency

                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");

                // write the word, document frequency, and term frequency
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");

                //write the posting list for the current term
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    // Write document ID and term frequency in the document, separated by ":"
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            //write an end marker to indicate the end of the index
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //=========================================

    //checks if there is a storage file
    public boolean storageFileExists(String storageName){
        //open the file. the path must be set according to the computer that will run the code
        java.io.File f = new java.io.File("is322_HW_1/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------
//this function takes a storageName and creates a new storage file or overwrites an existing one with an initial marker end.
    public void createStore(String storageName) {
        try {
            //the path must be set according to the computer that will run the code
            String pathToStorage = "is322_HW_1//"+storageName;
            //write to the file then close it
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------

     //load index from hard disk into memory
   //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
//            modify this path and add the appropriate one
            String pathToStorage = "is322_HW_1/"+storageName;
            sources = new HashMap<Integer, SourceRecord>(); //stores each SourceRecord and its fileID
            index = new HashMap<String, DictEntry>(); // stores the invertedList
//            open source file for reading
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            //parsing each line
            while ((ln = file.readLine()) != null) {
                // when 'section2' is reached, it indicates that all SourceRecords have been loaded
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                // split the line to store its details
                String[] ss = ln.split(",");
                // fid stores fileID
                int fid = Integer.parseInt(ss[0]);
                try {
                    // ensure the line is correctly parsed
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));
                    // create SourceRecord to store the file details
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    //store the SourceRecord and its fileID
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            //parsing the invertedList and loading it in index
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                // when 'end' is reached, it indicates that all inverted index entries have been loaded
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                // parse dictionary entry and posting list
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");//word details
                String[] ss1b = ss1[1].split(":");//posting details
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //Individual posting entry
                // Parse the posting list for the each term
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    // if this is the first posting entry for the term initialize the posting list
                    // and make the appropriate changes
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    }
                    //  otherwise, append the new posting to the linked list
                    // and make the appropriate changes
                    else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }

    //=====================================================================
}
