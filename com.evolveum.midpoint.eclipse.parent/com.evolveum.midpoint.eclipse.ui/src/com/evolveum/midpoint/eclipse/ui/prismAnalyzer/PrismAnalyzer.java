package com.evolveum.midpoint.eclipse.ui.prismAnalyzer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import pcv.PrismItemsBaseVisitor;
import pcv.PrismItemsLexer;
import pcv.PrismItemsParser;

/**
 * @author mederly
 */
public class PrismAnalyzer {
	public static void main(String[] args) throws IOException {
		System.out.print("Enter input here: ");
		StringBuilder sb = new StringBuilder();
		Reader r = new InputStreamReader(System.in);
		for (;;) {
			int c = r.read();
			if (c < 0) {
				break;
			}
			sb.append((char) c);
			System.out.print((char) c);
		}
		String input = sb.toString();
		System.out.println("Input: " + input);
		String result = processInput(input, 0);		
		System.out.println("Result: " + result);
	}

	public static String processInput(String input, int column) {
		input = preprocessInput(input);
		OutputBuffer buffer = new OutputBuffer(column);
		for (int position = 0; position < input.length(); position++) {
			String sub = input.substring(position);
			if (sub.startsWith("PRV(") || sub.startsWith("PCV(") || sub.startsWith("PPV(") || sub.startsWith("PC(") || sub.startsWith("PrismReference(") || sub.startsWith("PP(")) {
				EvalVisitor visitor = processPrismInput(sub, buffer); 
				position += visitor.endOfText;
				continue;
			}
			if (sub.startsWith("[")) {
				buffer.print("[\n");
				buffer.indent();
				buffer.printIndent();
			} else if (sub.startsWith(",")) {
				buffer.println(",");
				buffer.printIndent();
			} else if (sub.startsWith("],")) {
				buffer.println();
				buffer.dedent();
				buffer.printIndent();
				buffer.println("],");
				buffer.printIndent();
				position++;
			} else if (sub.startsWith("]")){
				buffer.println();
				buffer.dedent();
				buffer.printIndented("]");
			} else {
				buffer.print(String.valueOf(sub.charAt(0)));
			}
		}
		return buffer.result.toString();
	}
	
	private static String preprocessInput(String input) {
		input = input.replaceAll("\\(filter\\)", "filter").replaceAll("\\\\", "\\\\\\\\");
		int pos = 0;
		while (pos < input.length()) {
			int i = input.indexOf("PPV(", pos);
			if (i < 0) {
				break;
			}
			int j = input.indexOf(")]", i);
			int k = input.indexOf("), PPV(", i);
			if (j < 0 && k < 0) {
				break;		// some problem here
			} 
			int end;
			if (j < 0 || k >= 0 && k < j) {
				end = k; 
			} else {
				end = j;
			}
			String content = input.substring(i, end);
			String contentConverted = content.replaceAll("\\)", "\\\\)");
			if (!content.equals(contentConverted)) {
				System.out.println("Converted '" + content + "' to '" + contentConverted + "'");
			}
			input = input.substring(0, i) + contentConverted + input.substring(end);
			pos = end+1;
		}
		return input;
	}

	private static EvalVisitor processPrismInput(String input, OutputBuffer buffer) {
		PrismItemsLexer lexer = new PrismItemsLexer(new ANTLRInputStream(input));
		PrismItemsParser parser = new PrismItemsParser(new CommonTokenStream(lexer));
		EvalVisitor visitor = new EvalVisitor(buffer);
		parser.addErrorListener(visitor.new MyErrorListener());
		ParseTree tree = parser.start();
		visitor.visit(tree);
		return visitor;
	}
}

class OutputBuffer {
	boolean firstLine = true;
	int startingColumn;
	int indent = 0;
	StringBuilder result = new StringBuilder();
	
	public OutputBuffer(int column) {
		startingColumn = column; 
	}
	
	void print(String s) {
		result.append(s);
	}
	void println(String s) {
		result.append(s).append("\n");
	}
	void println() {
		result.append("\n");
	}
	void printIndented(String s) {
		printIndent();
		print(s);
	}

	void indent() {
		indent++;
	}
	void dedent() {
		indent--;
	}
	void printIndent() {
		if (firstLine) {
			firstLine = false;
		} else {
			for (int i = 0; i < startingColumn; i++) {
				print(" ");
			}
		}
		for (int i = 0; i < indent; i++) {
			print("  ");
		}
	}
}

class EvalVisitor extends PrismItemsBaseVisitor<Integer> {
	int endOfText;
	OutputBuffer buffer;
	
	public EvalVisitor(OutputBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public Integer visitPrismContainer(PrismItemsParser.PrismContainerContext ctx) {
		buffer.printIndented("PC ");
		buffer.indent();
		visitChildren(ctx);			// name + PCVs
		buffer.dedent();
		return 0;
	}

	@Override
	public Integer visitName(PrismItemsParser.NameContext ctx) {
		buffer.print(ctx.getText() + ":\n");
		return 0;
	}

	@Override
	public Integer visitPcv(PrismItemsParser.PcvContext ctx) {
		buffer.printIndented("PCV ");
		buffer.indent();
		visitChildren(ctx);
		buffer.dedent();
		return 1;
	}

	@Override
	public Integer visitPrismReference(PrismItemsParser.PrismReferenceContext ctx) {
		buffer.printIndented("PR ");
		buffer.indent();
		visitChildren(ctx);
		buffer.dedent();
		return 0;
	}

	@Override
	public Integer visitPrismProperty(PrismItemsParser.PrismPropertyContext ctx) {
		buffer.printIndented("PP ");
		buffer.indent();
		super.visitPrismProperty(ctx);
		buffer.dedent();
		return 0;
	}

	@Override
	public Integer visitTerminal(TerminalNode node) {
		String text = node.getText();
		if (text != null) {
			if (text.startsWith("PPV(") || text.startsWith("PRV(")) {
				buffer.printIndented(unescape(text) + "\n");
				return 1;
			}
		}
		return 0;
	}
	
	private String unescape(String text) {
		return text.replaceAll("\\\\\\)", ")").replaceAll("\\\\\\\\", "\\\\");
	}

	@Override
	public Integer visitStart(PrismItemsParser.StartContext ctx) {
		super.visitStart(ctx);
		endOfText = ctx.stop.getStopIndex();
		return 0;
	}


	@Override
	protected Integer defaultResult() {
		return 0;
	}

	@Override
	protected Integer aggregateResult(Integer aggregate, Integer nextResult) {
		return aggregate + nextResult;
	}
	
	class MyErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
			buffer.print("# error: " + msg + "\n");
		}
	}
}

