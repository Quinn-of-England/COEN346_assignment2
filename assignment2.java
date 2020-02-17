package COEN346_assignment2;
/* COEN 346 Practical Assignment 1
 * Mark Zalass - 40097293
 * Quinn Hogg - 40093086
 */
import java.io.File;
import java.util.Scanner;

public class assignment2 {

    public static void main(String[] args){
        int[] process= null;
        if (args.length != 1) { // Checks if arg is present, will exit if not correct (file name/path passed from args[0])
            System.err.println("You must pass the input file name");
            System.exit(0);
        }
        try { // Reads file using provided file name
            String fileName = args[0];
            File input = new File(fileName);
            process = ReadInput(fileName, input);
        }
        catch(Exception e) { // If file cannot be opened
            System.err.println("No file found");
        }
        

    }
    public static int[] ReadInput(String fileName, File file) throws Exception {
        Scanner s = new Scanner(file); // Reads first entry for # of inputs
        int inputNum = Integer.parseInt(s.nextLine());
        int[] inputs = new int[inputNum];
        int linecount = 0;
        while (s.hasNextLine()) { // Reads all inputs and stores it into an array
            inputs[linecount] = Integer.parseInt(s.nextLine());
            linecount++;
        }
        s.close();
        return inputs;
    }
}