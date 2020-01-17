# Parts of Speech  as a Hidden Markov Model  


## Design choices

`Hashmap <String, Hashmap<String, Double>>`

For the observation and transition tables, we used a Hashmap of Strings to a Hashmap of Strings to Doubles. This holds our states, observations, and probabilities.

We used two files, one containing sentences and one containing the corresponding POS, to "train" the model by calculating probabilities. 

## Performance 

![](https://i.imgur.com/Zg091yc.png)

After training with the Brown Corpus, the tagger got 96.47% correct on the Brown Corpus and 86.49% on the simple test. 

After training with a smaller set (simple-train-*.txt), the tagger got 34.24% correct on the Brown Corpus and 86.49% correct on the simple test. 

When training with a larger training set, the algorithm is more accurate. 

When using a smaller training set, the algorithm is less accurate. However, the algorithm can still be accurate but only on a test set with similar vocabulary. 

**Unseen Word Penalty**:  Increasing the magnitude of the unseen word penalty lead the algorithm to prefer more common patterns. This is because less common patterns will have a significantly lower score. This had little effect on the Brown Corpus (96.47% w/ penalty at -10000 vs 96.47% w/ penalty at -100). However, when the magnitude of the penalty was decreased (-10), the algorithm only got ~88% correct. 

**Sentence Pattern Performance:**

The following test sentences were used (trained with the Brown Corpus): 

These sentences contain words not in the training set and edge cases

- Think Different  - V ADJ 
- **Move fast, break things - V DET N N** 
- Matthew plays baseball well - NP VBZ N ADV 
- I like to sleep - PRO V TO V 
- **Richard said VIM is better than EMACS - NP VD TO V ADV CNJ DET** 
- **OK Boomer - ADV P**

**Bolded sentences are incorrect.** 

Patterns: 

The program didn't do well on cultural and memetic terms, foreign languages, words with more than one parts of speech, and technical terms (VIM, EMACS). This is expected for a program trained on the Brown Corpus. 

The algorithm seemed to default to DET (for example the foreign language, EMACS)

Surprisingly it understood slogans like "think different" which aren't commonly used in sentences

When examining the sentences the algorithm gets correctly (in the corpus) a lot of them have: 

ADJ->N, ADV->V, N/NP->V->N/NP, DET->ADJ->N->V. These are all common patterns in the English language (example: Subject-Verb-Direct Object)

## Testing

### Hard-coded Graphs

![](https://i.imgur.com/4oJBRjW.png)

We hardcoded the graph by adding each word with its correct tag and probability to the observation map: 

`observationMap.get("N").put("watch", Math.log(2.0 / 12.0));`

For the transition map, we hard coded the appropriate calculated probability with the Current/Next state 

`transitionMap.get("#").put("N", Math.log(5.0 / 7.0));`

`transitionMap.get("#").put("NP", Math.log(2.0 / 7.0));`

We checked the following phrases in the training vocabulary. This confirmed it works for similar words and for all capitalizations. 

* cat get dog
* cat and dog watch chase
* Cat chase DOG

The following new sentences were checked. It guessed 'matthew' as a noun (which is technically correct). However, the second sentence was incorrect. This is good enough for the training model, as it was only incorrect for new words. 

* matthew watch chase
* bunny chase watch with dog

### Sentence Input

### ![](https://i.imgur.com/4oJBRjW.png)

### Edge Cases

Edge cases (foreign language, pop culture, technical, new terms, multi meaning words) were tested above. 

Other edge cases: 

Invalid input:  invalid input in the GUI prints "Invalid input try again"

![https://i.imgur.com/mgjnHJy.png](https://i.imgur.com/mgjnHJy.png)

# Additional Features

![](https://i.imgur.com/s0PUabq.png)

 

With foreign languages, the algorithm is unable to even process different words let alone parts of speech. Other languages will additionally have different parts of speech and grammatical structures. In countries like Singapore where multiple languages are used interchangeably within the same sentence, this can be problematic. 

Chinese is a particularly challenging language due to the vast word base (these corpii are gigabytes if not terabytes). In addition, there are more parts of speech than English. We processed and tested Chinese/English Corpus (UN Chinese/English Corpus 1.5GB+). Grammatically speaking, English and Chinese are quite similar (some parts of speech have similar grammatical roles) so it does get a few correct. For a smaller test (submission), we built a simpler test case.

A future application of this would be context based translation. 

