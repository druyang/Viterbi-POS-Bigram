import java.util.HashMap;

/**
 * provides hard-coded training, along with test sentences based on training
 *
 * @author Andrw Yang
 */
public class POSTaggingTest {
    public static void main(String[] args) {
        HashMap<String, HashMap<String, Double>> observationMap = new HashMap<>();
        HashMap<String, HashMap<String, Double>> transitionMap = new HashMap<>();

        POSTagging tag = new POSTagging();

        // hard code training model from PD
        observationMap.put("N", new HashMap<>());
        observationMap.put("V", new HashMap<>());
        observationMap.put("NP", new HashMap<>());
        observationMap.put("CNJ", new HashMap<>());

        observationMap.get("N").put("cat", Math.log(5.0 / 12.0));
        observationMap.get("N").put("dog", Math.log(5.0 / 12.0));
        observationMap.get("N").put("watch", Math.log(2.0 / 12.0));

        observationMap.get("V").put("chase", Math.log(2.0 / 9.0));
        observationMap.get("V").put("get", Math.log(1.0 / 9.0));
        observationMap.get("V").put("watch", Math.log(6.0 / 9.0));

        observationMap.get("CNJ").put("and", Math.log(3.0 / 3.0));

        observationMap.get("NP").put("chase", Math.log(5.0 / 5.0));

        transitionMap.put("#", new HashMap<>());
        transitionMap.put("N", new HashMap<>());
        transitionMap.put("V", new HashMap<>());
        transitionMap.put("NP", new HashMap<>());
        transitionMap.put("CNJ", new HashMap<>());

        transitionMap.get("#").put("N", Math.log(5.0 / 7.0));
        transitionMap.get("#").put("NP", Math.log(2.0 / 7.0));

        transitionMap.get("N").put("CNJ", Math.log(2.0 / 8.0));
        transitionMap.get("N").put("V", Math.log(6.0 / 8.0));

        transitionMap.get("V").put("N", Math.log(6.0 / 9.0));
        transitionMap.get("V").put("NP", Math.log(2.0 / 9.0));
        transitionMap.get("V").put("CNJ", Math.log(1.0 / 9.0));

        transitionMap.get("NP").put("V", Math.log(2.0 / 2.0));

        transitionMap.get("CNJ").put("N", Math.log(1.0 / 3.0));
        transitionMap.get("CNJ").put("NP", Math.log(1.0 / 3.0));
        transitionMap.get("CNJ").put("V", Math.log(1.0 / 3.0));

        // test sentence, it works
        String testSentence = "cat get dog";
        System.out.println("Testing the sentence " + "'" + testSentence + "'" + "...");
        System.out.println(tag.backTrack(tag.tagPOS(testSentence, transitionMap, observationMap)));

        // test sentence, it works
        testSentence = "cat and dog watch chase";
        System.out.println("Testing the sentence " + "'" + testSentence + "'" + "...");
        System.out.println(tag.backTrack(tag.tagPOS(testSentence, transitionMap, observationMap)));

        // test sentence, check lower casing, it works
        testSentence = "Cat chase DOG";
        System.out.println("Testing the sentence " + "'" + testSentence + "'" + "...");
        System.out.println(tag.backTrack(tag.tagPOS(testSentence, transitionMap, observationMap)));

        // guesses that matthew is noun, technically a proper noun, not surprising as matthew is new word
        testSentence = "matthew watch chase";
        System.out.println("Testing the sentence " + "'" + testSentence + "'" + "...");
        System.out.println(tag.backTrack(tag.tagPOS(testSentence, transitionMap, observationMap)));

        // doesn't work perfectly, mainly because both bunny and with are unseen words
        testSentence = "bunny chase watch with dog";
        System.out.println("Testing the sentence " + "'" + testSentence + "'" + "...");
        System.out.println(tag.backTrack(tag.tagPOS(testSentence, transitionMap, observationMap)));


    }
}
