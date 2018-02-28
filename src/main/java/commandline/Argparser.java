package commandline;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.cli.*;

public class Argparser {

    private static final HelpFormatter help = new HelpFormatter();

    private static final Options options = new Options();

    /**
     * Public method for parsing the command line arguments
     *
     * @param args The args String array from cmd
     * @return A hashmap with the parsed arguments
     */
    public static HashMap<Attribute, String> parseCmdArguments(String[] args) {

        /*
         * define the options
         */
        options.addOption("h", "help", false, "Print this help");

        /*
         * container for parsed attributes
         */
        HashMap<Attribute, String> parsedArguments = new HashMap<>();

        /*
         * set parser type, default is enough here
         */
        CommandLineParser parser = new GnuParser();

        /*
         * try to parse the command line arguments
         */
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                parsedArguments.put(Attribute.HELP, "");
            }
        } catch (ParseException exc) {
            System.err.println(exc);
            /*
             * return empty map on fail
             */
            return parsedArguments;
        }

        return parsedArguments;
    }

    public static void printHelp() {
        help.printHelp("geo-uploader", options, true);
    }

    /**
     * Retrieve the string from input stream
     *
     * @return The string
     * @throws IOException
     */
    public static String readStringFromInputStream() throws IOException {
        String string;
        Console console = System.console();
        if (console == null) {
            System.err.println("Could not get console instance!");
            return "";
        }
        string = console.readLine();
        return string;
    }

    /**
     * Retrieve the password from input stream
     *
     * @return The password
     * @throws IOException
     */
    public static String readPasswordFromInputStream() throws IOException {
        char[] password;
        Console console = System.console();
        if (console == null) {
            System.err.println("Could not get console instance!");
            return "";
        }
        password = console.readPassword();
        return new String(password);
    }


    /**
     * Definition of some useful enum types for the cmd attributes
     */
    public enum Attribute {
        HELP
    }

}

