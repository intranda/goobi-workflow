// Generated from ExtendedDateTimeFormat.g4 by ANTLR 4.10.1

package de.sub.goobi.validator;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExtendedDateTimeFormatParser}.
 */
public interface ExtendedDateTimeFormatListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#edtf}.
	 * @param ctx the parse tree
	 */
	void enterEdtf(ExtendedDateTimeFormatParser.EdtfContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#edtf}.
	 * @param ctx the parse tree
	 */
	void exitEdtf(ExtendedDateTimeFormatParser.EdtfContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#level0Expression}.
	 * @param ctx the parse tree
	 */
	void enterLevel0Expression(ExtendedDateTimeFormatParser.Level0ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#level0Expression}.
	 * @param ctx the parse tree
	 */
	void exitLevel0Expression(ExtendedDateTimeFormatParser.Level0ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#date}.
	 * @param ctx the parse tree
	 */
	void enterDate(ExtendedDateTimeFormatParser.DateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#date}.
	 * @param ctx the parse tree
	 */
	void exitDate(ExtendedDateTimeFormatParser.DateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void enterDateTime(ExtendedDateTimeFormatParser.DateTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void exitDateTime(ExtendedDateTimeFormatParser.DateTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#dateTimeZ}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeZ(ExtendedDateTimeFormatParser.DateTimeZContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#dateTimeZ}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeZ(ExtendedDateTimeFormatParser.DateTimeZContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#level0Interval}.
	 * @param ctx the parse tree
	 */
	void enterLevel0Interval(ExtendedDateTimeFormatParser.Level0IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#level0Interval}.
	 * @param ctx the parse tree
	 */
	void exitLevel0Interval(ExtendedDateTimeFormatParser.Level0IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#level1Expression}.
	 * @param ctx the parse tree
	 */
	void enterLevel1Expression(ExtendedDateTimeFormatParser.Level1ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#level1Expression}.
	 * @param ctx the parse tree
	 */
	void exitLevel1Expression(ExtendedDateTimeFormatParser.Level1ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#unspecifiedDate}.
	 * @param ctx the parse tree
	 */
	void enterUnspecifiedDate(ExtendedDateTimeFormatParser.UnspecifiedDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#unspecifiedDate}.
	 * @param ctx the parse tree
	 */
	void exitUnspecifiedDate(ExtendedDateTimeFormatParser.UnspecifiedDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#monthUnspecified}.
	 * @param ctx the parse tree
	 */
	void enterMonthUnspecified(ExtendedDateTimeFormatParser.MonthUnspecifiedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#monthUnspecified}.
	 * @param ctx the parse tree
	 */
	void exitMonthUnspecified(ExtendedDateTimeFormatParser.MonthUnspecifiedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#uncertainOrApproxDate}.
	 * @param ctx the parse tree
	 */
	void enterUncertainOrApproxDate(ExtendedDateTimeFormatParser.UncertainOrApproxDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#uncertainOrApproxDate}.
	 * @param ctx the parse tree
	 */
	void exitUncertainOrApproxDate(ExtendedDateTimeFormatParser.UncertainOrApproxDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#dayUnspecified}.
	 * @param ctx the parse tree
	 */
	void enterDayUnspecified(ExtendedDateTimeFormatParser.DayUnspecifiedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#dayUnspecified}.
	 * @param ctx the parse tree
	 */
	void exitDayUnspecified(ExtendedDateTimeFormatParser.DayUnspecifiedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#dayAndMonthUnspecified}.
	 * @param ctx the parse tree
	 */
	void enterDayAndMonthUnspecified(ExtendedDateTimeFormatParser.DayAndMonthUnspecifiedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#dayAndMonthUnspecified}.
	 * @param ctx the parse tree
	 */
	void exitDayAndMonthUnspecified(ExtendedDateTimeFormatParser.DayAndMonthUnspecifiedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#yearUnspecified}.
	 * @param ctx the parse tree
	 */
	void enterYearUnspecified(ExtendedDateTimeFormatParser.YearUnspecifiedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#yearUnspecified}.
	 * @param ctx the parse tree
	 */
	void exitYearUnspecified(ExtendedDateTimeFormatParser.YearUnspecifiedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#open}.
	 * @param ctx the parse tree
	 */
	void enterOpen(ExtendedDateTimeFormatParser.OpenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#open}.
	 * @param ctx the parse tree
	 */
	void exitOpen(ExtendedDateTimeFormatParser.OpenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#season}.
	 * @param ctx the parse tree
	 */
	void enterSeason(ExtendedDateTimeFormatParser.SeasonContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#season}.
	 * @param ctx the parse tree
	 */
	void exitSeason(ExtendedDateTimeFormatParser.SeasonContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#level1Interval}.
	 * @param ctx the parse tree
	 */
	void enterLevel1Interval(ExtendedDateTimeFormatParser.Level1IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#level1Interval}.
	 * @param ctx the parse tree
	 */
	void exitLevel1Interval(ExtendedDateTimeFormatParser.Level1IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#dateOrSeason}.
	 * @param ctx the parse tree
	 */
	void enterDateOrSeason(ExtendedDateTimeFormatParser.DateOrSeasonContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#dateOrSeason}.
	 * @param ctx the parse tree
	 */
	void exitDateOrSeason(ExtendedDateTimeFormatParser.DateOrSeasonContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#unknownBeginning}.
	 * @param ctx the parse tree
	 */
	void enterUnknownBeginning(ExtendedDateTimeFormatParser.UnknownBeginningContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#unknownBeginning}.
	 * @param ctx the parse tree
	 */
	void exitUnknownBeginning(ExtendedDateTimeFormatParser.UnknownBeginningContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExtendedDateTimeFormatParser#unknownEnd}.
	 * @param ctx the parse tree
	 */
	void enterUnknownEnd(ExtendedDateTimeFormatParser.UnknownEndContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExtendedDateTimeFormatParser#unknownEnd}.
	 * @param ctx the parse tree
	 */
	void exitUnknownEnd(ExtendedDateTimeFormatParser.UnknownEndContext ctx);
}