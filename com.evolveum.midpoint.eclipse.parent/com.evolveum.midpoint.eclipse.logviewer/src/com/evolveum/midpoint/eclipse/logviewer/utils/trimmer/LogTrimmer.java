package com.evolveum.midpoint.eclipse.logviewer.utils.trimmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.eclipse.logviewer.parsing.ParsingUtils;

/**
 * Commands from stdin:
 *  - trim "Log header text" [lines-to-keep] (0 if remove completely)
 *  - select-test "part-of-test-name"
 *  - sort-by-threads
 * 
 * @author Pavol Mederly
 *
 */
public class LogTrimmer {
	
	// TODO clean this up
	private static String outfile;
	private static String currentThreadName;
	private static boolean sortByThreads;

	private static Map<String, PrintWriter> writers = new HashMap<>();
	
	private static void output(String line) throws IOException {
		String name;
		if (sortByThreads) {
			if (!outfile.contains("*")) {
				throw new IllegalArgumentException("When sorting by threads, the outfile must contain '*' character (to be replaced by thread name).");
			}
			name = outfile.replace("*", fixFileName(currentThreadName));
		} else {
			name = outfile;
		}
		PrintWriter out = writers.get(name);
		if (out == null) {
			System.out.println("Opening file " + name);
			out = new PrintWriter(new BufferedWriter(new FileWriter(name)));
			writers.put(name, out);
		}
		out.println(line);
	}
	
	private static String fixFileName(String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch) && ch != '-' && ch != '.' && ch != '_') {
				sb.append('_');
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	private static void closeOutput() {
		for (PrintWriter writer : writers.values()) {
			writer.close();
		}
	}

	public static void main(String[] args) throws IOException {
		
		if (args.length < 3) {
			System.out.println("Usage: LogTrimmer instructions-file output-file input-file-1 ... input-file-N");
			System.out.println();
			System.out.println("(or using interactive mode when parameters are not specified)");
			System.out.println();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String instructionsFile;
		if (args.length > 0) {
			instructionsFile = args[0];
		} else {
			System.out.print("Instructions file (ENTER to use stdin): ");
			instructionsFile = br.readLine();
		}
		
		if (args.length > 1) {
			outfile = args[1];
			System.out.println("Output: " + outfile);			
		} else {
			System.out.print("Output file (ENTER to use out.log): ");
			outfile = br.readLine();
			if (outfile.isEmpty()) {
				outfile = "out.log";
			}
		}
		
		List<String> inputFiles = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			inputFiles.add(args[i]);
		}
		if (inputFiles.isEmpty()) {
			System.out.print("Input file (ENTER to skip processing): ");
			String input = br.readLine();
			if (!input.isEmpty()) {
				inputFiles.add(input);
			} else {
				return;
			}
		}
		
		List<Command> commands = parseCommands(instructionsFile);
		List<TrimCommand> trimCommands = extractCommands(commands, TrimCommand.class);
		List<SelectTestCommand> selectTestCommands = extractCommands(commands, SelectTestCommand.class);		
		List<SortByThreadsCommand> sortByThreadsCommands = extractCommands(commands, SortByThreadsCommand.class);
		
		sortByThreads = !sortByThreadsCommands.isEmpty();

		int linesRead = 0;
		int linesWritten = 0;
		currentThreadName = "";
		
		for (String infile : inputFiles) {
			System.out.println("\nInput: " + infile);
			BufferedReader in = new BufferedReader(new FileReader(infile));

			int lineOfLogEntry = -1;
			TrimCommand currentTrimCommand = null;
			SelectTestCommand currentSelectTestCommand = null;
			for (;;) {
			
				boolean trim = false;
				String line = in.readLine();
				if (line == null) {
					break;
				}
				linesRead++;
				
				if (ParsingUtils.isLogEntryStart(line)) {
					lineOfLogEntry = 0;
					currentTrimCommand = findRelevantCommand(trimCommands, line);
					currentSelectTestCommand = updateTestCommand(currentSelectTestCommand, line, selectTestCommands);
					if (sortByThreads) {
						currentThreadName = ParsingUtils.parseThread(line, null);
					}
				} else {
					if (lineOfLogEntry >= 0) {
						lineOfLogEntry++;
					}
				}
				if (currentTrimCommand instanceof TrimCommand) {
					int linesToKeep = ((TrimCommand) currentTrimCommand).getKeepLines(); 
					if (lineOfLogEntry == linesToKeep) {
						output("  (...)");
						trim = true;
					} else if (lineOfLogEntry > linesToKeep) {
						trim = true;
					}
				}
				if (!trim && (selectTestCommands.isEmpty() || currentSelectTestCommand != null)) {
					output(line);
					linesWritten++;
				}
				
				if (linesRead%10000 == 0) {
					System.out.print("#");
				}
				if (linesRead%300000 == 0) {
					System.out.println();
				}
			}
			in.close();
		}
		closeOutput();
		
		System.out.println("\n\nLines read: " + linesRead + ", written: " + linesWritten);
	}

	private static SelectTestCommand updateTestCommand(SelectTestCommand current, String line, List<SelectTestCommand> commands) {
		final String begin = "(com.evolveum.midpoint.test.util.TestUtil): =====[ ";
		final String end = " ]=";
		int i = line.indexOf(begin);
		if (i < 0) {
			return current;
		}
		String line2 = line.substring(i + begin.length());
		int j = line2.indexOf(end);
		if (j < 0) {
			System.out.println("Malformed test start line: " + line);
			return current;
		}
		String testName = line2.substring(0, j);
		return findRelevantCommand(commands, testName);
	}

	private static <T extends Command> List<T> extractCommands(List<Command> commands, Class<T> class1) {
		List<T> rv = new ArrayList<>();
		for (Command c : commands) {
			if (class1.isAssignableFrom(c.getClass())) {
				rv.add((T) c);
			}
		}
		return rv;
	}

	private static List<Command> parseCommands(String filename) throws IOException {
		List<Command> commands = new ArrayList<>();
		BufferedReader br;
		if (filename.isEmpty()) {
			System.out.println("Enter instructions:");
			System.out.println(" - trim \"Log header text\" [lines-to-keep] (0 if remove completely)");
			System.out.println(" - select-test \"part-of-test-name\"");
			System.out.println(" - sort-by-threads");
			System.out.println(" - go");
			br = new BufferedReader(new InputStreamReader(System.in));
		} else {
			br = new BufferedReader(new FileReader(filename));
		}
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				;
			} else if (line.startsWith("trim ")) {
				String line2 = line.substring(4).trim();
				int keyEnd = findKeyEnd(line, line2);
				String key = line2.substring(1, keyEnd);
				String remainder = line2.substring(keyEnd+1).trim();
				int lines = 1;
				if (!remainder.isEmpty()) {
					lines = Integer.parseInt(remainder);
				}
				TrimCommand trimCommand = new TrimCommand(key, lines);
				commands.add(trimCommand);
			} else if (line.startsWith("select-test ")) {
				String line2 = line.substring(12).trim();
				int keyEnd = findKeyEnd(line, line2);
				String key = line2.substring(1, keyEnd);
				SelectTestCommand cmd = new SelectTestCommand(key);
				commands.add(cmd);
			} else if (line.startsWith("sort-by-threads")) {
				commands.add(new SortByThreadsCommand());
			} else if (line.equals("go")) {
				break;
			} else {
				throw new IllegalStateException("Unparseable command: " + line);
			}
		}
		br.close();
		System.out.println("Commands parsed: " + commands.size());
		return commands;
	}

	private static int findKeyEnd(String line, String line2) {
		int keyEnd;
		if (line2.startsWith("\"")) {
			keyEnd = line2.indexOf('\"', 1);
		} else if (line2.startsWith("\'")) {
			keyEnd = line2.indexOf('\'', 1);
		} else {
			throw new IllegalStateException("Unparseable command - text to find is neither in quotes nor in apostrophes: " + line);
		}
		if (keyEnd < 0) {
			throw new IllegalStateException("Unparseable command - missing ending quote/apostrophe: " + line);
		}
		return keyEnd;
	}

	private static <T extends Command> T findRelevantCommand(List<T> commands, String line) {
		for (T command : commands) {
			if (line.contains(command.getKey())) {
				return command;
			}
		}
		return null;
	}

}
