import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * provides UI for tagging user given sentences
 *
 * @author Andrw Yang
 */

public class POSTagUI {
    public static void main(String[] args) throws IOException {
        // create a UI to type in sentence and get it tagged, based on brown training files
        POSTagging tag = new POSTagging();
        HashMap<String, HashMap<String, Double>> observationMap =
                tag.buildObservationMap("singlish-train-tags.txt", "singlish-train-sentences.txt");
        HashMap<String, HashMap<String, Double>> transitionMap =
                tag.buildTransitionMap("singlish-train-tags.txt");
        Scanner in = new Scanner(System.in);

        System.out.println("Type a sentence to get its part of speech tags. Press q to end the program");

        // keep this running until user presses 'q'
        boolean program = true;
        String line;

        while (program) {
            line = in.nextLine();
            if (line.length() == 0){
                System.out.println("Invalid input try again");
            }
            else if (line.charAt(0) == 'q' && line.length() == 1) {
                System.out.println("The program has been quit");
                program = false;
            }
            else {
                List<String> predicted = tag.backTrack(tag.tagPOS(line, transitionMap, observationMap));
                System.out.println("The predicted tags are: ");
                for (String prediction : predicted) {
                System.out.print(prediction + " ");
                }
                System.out.println("\n" + "Type a new sentence or press q to quit.");
            }

        }
    }
}
