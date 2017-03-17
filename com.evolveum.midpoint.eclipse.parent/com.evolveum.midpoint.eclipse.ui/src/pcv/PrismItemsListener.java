// Generated from C:/midpoint/tgit/general-test/src/pcv\PrismItems.g4 by ANTLR 4.6
package pcv;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PrismItemsParser}.
 */
public interface PrismItemsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(PrismItemsParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(PrismItemsParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#item}.
	 * @param ctx the parse tree
	 */
	void enterItem(PrismItemsParser.ItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#item}.
	 * @param ctx the parse tree
	 */
	void exitItem(PrismItemsParser.ItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#prismContainer}.
	 * @param ctx the parse tree
	 */
	void enterPrismContainer(PrismItemsParser.PrismContainerContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#prismContainer}.
	 * @param ctx the parse tree
	 */
	void exitPrismContainer(PrismItemsParser.PrismContainerContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#prismReference}.
	 * @param ctx the parse tree
	 */
	void enterPrismReference(PrismItemsParser.PrismReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#prismReference}.
	 * @param ctx the parse tree
	 */
	void exitPrismReference(PrismItemsParser.PrismReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#prismProperty}.
	 * @param ctx the parse tree
	 */
	void enterPrismProperty(PrismItemsParser.PrismPropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#prismProperty}.
	 * @param ctx the parse tree
	 */
	void exitPrismProperty(PrismItemsParser.PrismPropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#pcv}.
	 * @param ctx the parse tree
	 */
	void enterPcv(PrismItemsParser.PcvContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#pcv}.
	 * @param ctx the parse tree
	 */
	void exitPcv(PrismItemsParser.PcvContext ctx);
	/**
	 * Enter a parse tree produced by {@link PrismItemsParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(PrismItemsParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PrismItemsParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(PrismItemsParser.NameContext ctx);
}