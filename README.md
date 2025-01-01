# Hidden Markov Model for Named Entity Recognition

This project implements a Hidden Markov Model (HMM) and the Viterbi algorithm to solve Named Entity Recognition (NER) tasks using the MITMovie dataset. The model identifies named entities like `Actor`, `Director`, `Genre`, etc., in text by inferring the most likely sequence of labels for word tokens.

## Overview

### Hidden Markov Model (HMM)
The HMM models sequential data with:
- **Hidden States**: Represent BIO tags (Beginning-Inside-Outside tagging scheme).
- **Observations**: Represent word tokens.
- **Probabilities**:
    - Initial state distribution.
    - State transition probabilities.
    - Emission probabilities (mapping hidden states to observed tokens).

### Named Entity Recognition (NER)
NER identifies and categorizes entities in unstructured text into predefined categories. Using BIO tagging, entities are labeled to define:
- `B-`: Beginning of an entity.
- `I-`: Inside an entity.
- `O`: Outside any entity.

Example: B-Actor Steve I-Actor McQueen O provided B-Plot thrilling


### Viterbi Algorithm
The project uses the Viterbi algorithm for decoding, efficiently computing the most likely sequence of BIO tags for token observations.

## Dataset
The **MITMovie dataset** is used for training and testing. It includes:
- **Entities**: 12 predefined categories (`Actor`, `Plot`, `Opinion`, etc.).
- **Train/Test Split**:
    - Train: 7816 documents, 10,987 unique tokens.
    - Test: 1953 documents, 5786 unique tokens.

## Implementation

1. **Parameter Estimation**:
    - Transition and emission probabilities are estimated from training data.
    - Laplace smoothing prevents zero probabilities for unseen transitions and emissions.

2. **Decoding**:
    - The Viterbi algorithm labels tokens in the test set with the most likely BIO tags.

3. **Evaluation**:
    - Metrics: Accuracy, Precision, Recall, and F1 Score (micro-averaged across all entities).

---

## Enhancements

- The enhanced model (`HMMEnhanced`) applies **Laplace smoothing**, with a tuned factor of 0.08, to improve performance.
- This reduces errors for unseen data and increases F1 scores.

---

## How to Run

1. Train the model on the dataset by running the main class:
   ```java
   Main.java
   ```
2. Use `HMMEnhanced` for improved results.
3. Run the test suite to validate the implementation:
   ```java
   Tests.java
   ```
---

This project illustrates how Hidden Markov Models can be applied to natural language processing tasks, offering a foundation for understanding probabilistic modeling and sequential data handling.