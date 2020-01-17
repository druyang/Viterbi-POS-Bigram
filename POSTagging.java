import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * provides methods for training and Viterbi tagging, also provides method to get results of tagging a test file
 *
 * @author Andrw Yang
 */

public class POSTagging {
    double U = -100;

    // create class for backtracking, keeps track of current tag and score at next tag, used in map
    private class Current {
        String current; double nextScore;

        private Current(String current, double nextScore) {
        this.current = current;
        this.nextScore = nextScore;
        }
        private double getNextScore() {return nextScore;}
        private String getCurrent() {return current;}
    }

    // class used to assess the accuracy of Viterbi decoding
    private class Result {
        double correct, incorrect;
        private Result(double correct, double incorrect) {
            this.correct = correct; this.incorrect = incorrect;
        }

        @Override
        public String toString() {
            return "The given files produced " + correct + " correct tags and " + incorrect + " incorrect tags. \n" + correct/(correct + incorrect)*100 + "% Percent Correct";
        }
    }

    // builds map relating tags to the tags they transition to, as well as the probability they transition to such tags
    public HashMap<String, HashMap<String, Double>> buildTransitionMap(String tagFile) throws IOException {
        BufferedReader tagReader = new BufferedReader(new FileReader("inputs/" + tagFile));
        HashMap<String, HashMap<String, Double>> transitionMap = new HashMap<>();
        String line;
        while ((line = tagReader.readLine()) != null) {
            String[] tags = line.split(" ");
            // first count transitions out for each tag
            for (int i = -1; i < tags.length; i++) {
                if (i == -1) {
                    if (!transitionMap.containsKey("#")) {
                        transitionMap.put("#", new HashMap<>());
                        transitionMap.get("#").put(tags[i + 1], 1.0);
                    }
                    else if (!transitionMap.get("#").containsKey(tags[i + 1])) {
                        transitionMap.get("#").put(tags[i + 1], 1.0);
                    }
                    else {
                        double current = transitionMap.get("#").get(tags[i + 1]);
                        transitionMap.get("#").put(tags[i + 1], current + 1.0);
                    }
                }
                else if (i < tags.length - 1) {
                    if (!transitionMap.containsKey(tags[i])) {
                        transitionMap.put(tags[i], new HashMap<>());
                        transitionMap.get(tags[i]).put(tags[i + 1], 1.0);
                    } else if (!transitionMap.get(tags[i]).containsKey(tags[i + 1])){
                        transitionMap.get(tags[i]).put(tags[i + 1], 1.0);
                    }
                    else {
                        double current = transitionMap.get(tags[i]).get(tags[i + 1]);
                        transitionMap.get(tags[i]).put(tags[i+1], current + 1.0);
                    }
                }
                else {
                    if (!transitionMap.containsKey(tags[i])) {
                        transitionMap.put(tags[i], new HashMap<>());
                    }
                }
            }
        }
        tagReader.close();
        // now have to go through and convert to scores of transition with natural log
        for (String tag : transitionMap.keySet()) {
            double totalTransitions = 0;
            for (String transition : transitionMap.get(tag).keySet()) {
                totalTransitions += transitionMap.get(tag).get(transition);
            }
            for (String transition : transitionMap.get(tag).keySet()) {
                double transitionCount = transitionMap.get(tag).get(transition);
                transitionMap.get(tag).put(transition, Math.log(transitionCount / totalTransitions));
            }
        }
        return transitionMap;
    }

    public HashMap<String, HashMap<String, Double>> buildObservationMap(String tagFile, String obsFile) throws IOException {
        BufferedReader tagReader = new BufferedReader(new FileReader("inputs/" + tagFile));
        BufferedReader obsReader = new BufferedReader(new FileReader("inputs/" + obsFile));

        HashMap<String, HashMap<String, Double>> observationMap = new HashMap<>();

        observationMap = new HashMap<>();
        String tagLine;
        String obsLine;
        // first count how many times a tag is used for a specific word
        while ((tagLine = tagReader.readLine()) != null) {
            obsLine = obsReader.readLine().toLowerCase();
            String[] obs = obsLine.split(" ");
            String[] tags = tagLine.split(" ");


            for (int i = 0; i < tags.length; i++) {
                if (!observationMap.containsKey(tags[i])) {
                    observationMap.put(tags[i], new HashMap<>());
                    observationMap.get(tags[i]).put(obs[i], 1.0);
                }
                else if (!observationMap.get(tags[i]).containsKey(obs[i])) {
                    observationMap.get(tags[i]).put(obs[i], 1.0);
                }
                else {
                    double current = observationMap.get(tags[i]).get(obs[i]);
                    observationMap.get(tags[i]).put(obs[i], current + 1.0);
                }
            }
        }

        tagReader.close(); obsReader.close();
        // now calculate log probability that a tag is used for an observed word
        for (String tag : observationMap.keySet()) {
            double totalObs = 0;
            for (String obs : observationMap.get(tag).keySet()) {
                totalObs += observationMap.get(tag).get(obs);
            }
            for (String obs : observationMap.get(tag).keySet()) {
                double obsCount = observationMap.get(tag).get(obs);
                observationMap.get(tag).put(obs, Math.log(obsCount / totalObs));
            }
        }
        return observationMap;
    }

    // goes through and examines all possible transitions from current tags, calculating log probabilities
    public List<HashMap<String, Current>> tagPOS(String line, HashMap<String,
            HashMap<String, Double>> transitionMap, HashMap<String, HashMap<String, Double>> observationMap) {

        List<HashMap<String, Current>> POS = new ArrayList<>();

        String obs;
        String[] words = line.toLowerCase().split(" ");
        for (int i = 0; i < words.length; i++) {
            obs = words[i];
            // handle special first case, create key "#"
            if (i == 0) {
                HashMap<String, Current> initial = new HashMap<>();
                for (String nextState : transitionMap.get("#").keySet()) {
                    if (observationMap.get(nextState).containsKey(obs)) {
                        initial.put(nextState, new Current("#",
                                transitionMap.get("#").get(nextState) + observationMap.get(nextState).get(obs)));
                    }
                    else {
                        initial.put(nextState, new Current("#",
                                transitionMap.get("#").get(nextState) + U));
                    }
                }
                POS.add(initial);
            }
            // now keep going based on transition map
            else {
                HashMap<String, Current> current = new HashMap<>();
                for (String state : POS.get(i - 1).keySet()) {
                    for (String next : transitionMap.get(state).keySet()) {
                        double obsScore;
                        // handle case of unseen observation
                        if (observationMap.get(next).containsKey(obs)) {
                            obsScore = observationMap.get(next).get(obs);
                        } else {
                            obsScore = U;
                        }
                        // only keep the next state with highest score
                        if (current.containsKey(next)) {
                            double score1 = POS.get(i - 1).get(state).getNextScore() +
                                    transitionMap.get(state).get(next) + obsScore;
                            if (score1 > current.get(next).getNextScore()) {
                                current.put(next, new Current(state, score1));
                            }
                        } else {
                            current.put(next, new Current(state, POS.get(i - 1).get(state).getNextScore() +
                                    transitionMap.get(state).get(next) + obsScore));
                        }
                    }
                }
                POS.add(current);
            }
        }
        return POS;
    }

    // finds highest score in the last state, backtracks from there using the Current objects
    public List<String> backTrack(List<HashMap<String, Current>> posTags) {
        List<String> back = new LinkedList();
            HashMap<String, Current> last = posTags.get(posTags.size() - 1);
            String maxTag = null;
            for (String tag : last.keySet()) {
                if (maxTag == null) { maxTag = tag; }
                else {
                    if (last.get(maxTag).getNextScore() < last.get(tag).getNextScore()) {
                        maxTag = tag;
                    }
                }
            }
            back.add(0, maxTag);
            String currentTag = maxTag;
            for (int i = posTags.size() - 1; i > 0; i--) {
                back.add(0, posTags.get(i).get(currentTag).getCurrent());
                currentTag = posTags.get(i).get(currentTag).getCurrent();
            }

        return back;
    }

    // calculates the number of correct and incorrect tags for a given file
    public Result getResults(String sentenceFile, String tagFile, HashMap<String, HashMap<String,
            Double>> transitionMap, HashMap<String, HashMap<String, Double>> observationMap) throws IOException {

        double correct = 0;
        double incorrect = 0;

        BufferedReader sentenceReader = new BufferedReader(new FileReader("inputs/" + sentenceFile));
        BufferedReader tagReader = new BufferedReader(new FileReader("inputs/" + tagFile));
        String sentence;
        while ((sentence = sentenceReader.readLine()) != null) {
            List<String> predictedTags = backTrack(tagPOS(sentence.toLowerCase(), transitionMap, observationMap));
            String[] actualTags = tagReader.readLine().split(" ");
            for (int i = 0; i < predictedTags.size(); i++) {
                if (predictedTags.get(i).equals(actualTags[i])) {
                    correct++;
                }
                else {
                    incorrect++;
                }
            }
        }
        return new Result(correct, incorrect);
    }
    public static void main(String[] args) throws IOException {
        POSTagging tag = new POSTagging();
        HashMap<String, HashMap<String, Double>> observationMap =
                    tag.buildObservationMap("brown-train-tags.txt", "brown-train-sentences.txt");
        HashMap<String, HashMap<String, Double>> transitionMap =
                tag.buildTransitionMap("brown-train-tags.txt");
        System.out.println(tag.getResults(
                "brown-test-sentences.txt", "brown-test-tags.txt", transitionMap, observationMap));

    }

}




