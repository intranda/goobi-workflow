/**
 * This is an Antlr4 grammar for the Extended Date Time Format (EDTF).
 * EDTF specification website: http://www.loc.gov/standards/datetime/
 * Version of the spec supported: Draft Submission, updated September 10, 2012
 *
 * Author: Kevin S. Clarke (ksclarke@gmail.com)
 * Created: 2013/02/06
 * Updated: 2013/02/23
 *
 * License: BSD 2-Clause http://github.com/ksclarke/freelib-edtf/LICENSE
 */

grammar ExtendedDateTimeFormat;

@header {
package de.sub.goobi.validator;
}

edtf : (level0Expression | level1Expression) EOF;

// **************************   Level 0: Tokens   *************************** //

T : 'T';
Z : 'Z';
Dash : '-';
Plus : '+';
Colon : ':';
Slash : '/';

Year : PositiveYear | NegativeYear | YearZero;
NegativeYear : Dash PositiveYear;
PositiveYear
    : PositiveDigit Digit Digit Digit
    | Digit PositiveDigit Digit Digit
    | Digit Digit PositiveDigit Digit
    | Digit Digit Digit PositiveDigit
    ;
Digit : PositiveDigit | '0';
PositiveDigit : '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';
YearZero : '0000';
Month : OneThru12;
MonthDay
    : ( '01' | '03' | '05' | '07' | '08' | '10' | '12' ) Dash OneThru31
    | ( '04' | '06' | '09' | '11' ) Dash OneThru30
    | '02' Dash OneThru29
    ;
YearMonth : Year Dash Month;
YearMonthDay : Year Dash MonthDay;
Day : OneThru31;
Hour : ZeroThru23;
Minute : ZeroThru59;
Second : ZeroThru59;

OneThru12
    : '01' | '02' | '03' | '04' | '05' | '06'  | '07' | '08' | '09' | '10'
    | '11' | '12'
    ;
OneThru13 : OneThru12 | '13';
OneThru23
    : OneThru13 | '14' | '15' | '16' | '17'  | '18' | '19' | '20' | '21'
    | '22' | '23'
    ;
ZeroThru23 : '00' | OneThru23;
OneThru29 : OneThru23 | '24' | '25' | '26' | '27' | '28' | '29';
OneThru30 : OneThru29 | '30';
OneThru31 : OneThru30 | '31';
OneThru59 : OneThru31
    | '32' | '33' | '34' | '35' | '36' | '37' | '38' | '39' | '40' | '41'
    | '42' | '43' | '44' | '45' | '46' | '47' | '48' | '49' | '50' | '51'
    | '52' | '53' | '54' | '55' | '56' | '57' | '58' | '59'
    ;
ZeroThru59 : '00' | OneThru59;
Time : Hour Colon Minute Colon Second | '24' Colon '00' Colon '00';
ZOffset : (Plus | Dash) OffsetTime;
OffsetTime : OneThru13 (Colon Minute)?
    | '14' Colon '00'
    | '00' Colon OneThru59
    ;

// ***********************   Level 0: Parser Rules   ************************ //

level0Expression : date | dateTime | dateTimeZ | level0Interval;
date : Year | YearMonth | YearMonthDay;
dateTime : YearMonthDay T Time ZOffset?;
dateTimeZ : YearMonthDay T Time Z;
// (Z | ((Plus | Dash) ZOffset))?;

// *******************  Level 0: Interval Parser Rules  ********************* //

level0Interval : date Slash date;

// ***************************   Level 1: Tokens   ************************** //

U : 'X';
UU : 'XX';
Tilde : '~';
Open : '..';
QuestionMark : '?';
QuestionMarkTilde : '%';

YearUA : Year UASymbol;
SeasonUA : Season UASymbol;
YearMonthUA : YearMonth UASymbol;
YearMonthDayUA : YearMonthDay UASymbol;

Season : Year Dash ('21' | '22' | '23' | '24');
UASymbol : QuestionMark | Tilde | QuestionMarkTilde;
YearWithOneUnspecifedDigit : Digit Digit Digit U;
YearWithTwoUnspecifedDigits : Digit Digit U U;

// *************************  Level 1: Parser Rules  ************************ //

level1Expression
    : uncertainOrApproxDate
    | unspecifiedDate
    | level1Interval
    | season
    ;
unspecifiedDate
    : yearUnspecified
    | monthUnspecified
    | dayUnspecified
    | dayAndMonthUnspecified
    ;
monthUnspecified : Year Dash UU;
uncertainOrApproxDate : YearUA | YearMonthUA | YearMonthDayUA;
dayUnspecified : YearMonth Dash UU;
dayAndMonthUnspecified : Year Dash UU Dash UU;
yearUnspecified : YearWithOneUnspecifedDigit | YearWithTwoUnspecifedDigits;

// Keeps our parse levels consistent with the least amount of added structure
open : Open;
season : Season;

// *******************  Level 1: Interval Parser Rules  ********************* //

level1Interval : (dateOrSeason Slash open) | (open Slash dateOrSeason) | (dateOrSeason Slash dateOrSeason) | unknownBeginning | unknownEnd;

dateOrSeason
	: YearUA | Year
	| SeasonUA | Season
	| YearMonthUA | YearMonth
	| YearMonthDayUA | YearMonthDay
	;

unknownBeginning
	: (Slash dateOrSeason)
	;
	
unknownEnd
	: (dateOrSeason Slash)
	;
