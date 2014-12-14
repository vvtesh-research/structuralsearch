structuralsearch
================

Search source code based on its structure. For more details look at the PEPM 2015 paper on Structural Search by Venkatesh Vinayakarao et al. This code implements the ideas as mentioned in the paper. 

Overall, there are two important steps:

1. Build Lucene Index: Download latest version of Lucene (tested with Version_48). Download stackoverflow dump. Run the buildindex project. 
2. Search Lucene Index: Performs a SpanNear Query, filters structural duplicates and sorts by structural complexity. Drops blank implementations (whose complexity is zero). 

The objective of this project is to search for queries such as "factorial" in stackoverflow posts and return method definitions that are relevant. More detailes are in the paper mentioned above.
