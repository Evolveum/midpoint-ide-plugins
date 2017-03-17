// Generated from C:/midpoint/tgit/general-test/src/pcv\PrismItems.g4 by ANTLR 4.6
package pcv;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PrismItemsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PrismItemsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(PrismItemsParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(PrismItemsParser.ItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#prismContainer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrismContainer(PrismItemsParser.PrismContainerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#prismReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrismReference(PrismItemsParser.PrismReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#prismProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrismProperty(PrismItemsParser.PrismPropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#pcv}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPcv(PrismItemsParser.PcvContext ctx);
	/**
	 * Visit a parse tree produced by {@link PrismItemsParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(PrismItemsParser.NameContext ctx);
}