grammar ExtendedDateTimeFormat;

// Tokens //

T : 'T';
Z : 'Z';
X : 'X';
E : 'E';
S : 'S';
LONGYEAR : 'Y';
DOTS : '.' '.';
UNKNOWN : 'u' 'n' 'k' 'n' 'o' 'w' 'n';
UA : '~' | '?' | '%';

// Rules //

edtf : edtf_expression ('\r'? '\n')? EOF;

edtf_expression : level_0_expression
                 | level_1_expression
                 | level_2_expression
                 ;

// Level 0 / ISO 8601 //

level_0_expression : date
                   | date_time
                   ;

date : year
     | year_month
     | year_month_day
     ;

date_time : date T time;

time : base_time
     | base_time zone_offset
     ;

base_time : hour ':' minute ':' second
          | midnight
          ;

midnight : '2' '4' ':' '0' '0' ':' '0' '0';

zone_offset : Z
            | '-' zone_offset_hour_minute
            | '-' zone_offset_hour
            | '+' positive_zone_offset_hour_minute
            | '+' positive_zone_offset_hour
            ;

positive_zone_offset_hour : zone_offset_hour
                          | '0' '0'
                          ;


positive_zone_offset_hour_minute : zone_offset_hour_minute
                                 | '0' '0' ':' '0' '0'
                                 ;

zone_offset_hour : d01_13
                 | '1' '4'
                 | '0' '0'
                 ;


zone_offset_hour_minute : d01_13 ':' minute
                        | '1' '4' ':' '0' '0'
                        | '0' '0' ':' d01_59
                        ;

year : positive_year
     | '-' positive_year
     ;

positive_year : digit digit digit digit;

month : d01_12;

day : d01_31;

year_month : year '-' month;

year_month_day : year_month '-' day;

hour : d00_23;
minute : d00_59;
second : d00_59;

// Level 1 Extension //

level_1_expression : UNKNOWN
                   | unspecified
                   | level_1_interval
                   | long_year_simple
                   | season
                   ;

unspecified : unspecified_year
            | unspecified_month
            | unspecified_day
            | unspecified_day_and_month
            ;

unspecified_year : positive_unspecified_year
                 | '-' positive_unspecified_year
                 ;

positive_unspecified_year : digit digit digit X
                          | digit digit X X
                          ;

unspecified_month : year '-' X X;

unspecified_day : year_month '-' X X;

unspecified_day_and_month : year '-' X X '-' X X;

level_1_interval : level_1_element '/' level_1_element
                 | '/' level_1_element
                 | level_1_element '/'
                 ;

level_1_element : date
                | partial_uncertain_or_approximate_or_both
                | unspecified
                | partial_unspecified
                | UNKNOWN
                | DOTS
                ;

long_year_simple : LONGYEAR long_year
                 | LONGYEAR '-' long_year
                 ;

long_year : positive_digit digit digit digit digit
          | long_year digit
          ;

season : year '-' season_number UA?;

season_number : '2' '1'
              | '2' '2'
              | '2' '3'
              | '2' '4'
              | '2' '5'
              | '2' '6'
              | '2' '7'
              | '2' '8'
              | '2' '9'
              | '3' '0'
              | '3' '1'
              | '3' '2'
              | '3' '3'
              | '3' '4'
              | '3' '5'
              | '3' '6'
              | '3' '7'
              | '3' '8'
              | '3' '9'
              | '4' '0'
              | '4' '1'
              ;

// Level 2 Extension Rules //

level_2_expression : partial_uncertain_or_approximate_or_both
                   | partial_unspecified
                   | long_year_scientific
                   | set_representation
                   ;

long_year_scientific : long_year_simple E integer
                     | LONGYEAR int1_4 E integer
                     | LONGYEAR '-' int1_4 E integer
                     ;

partial_unspecified : unspecified_year '-' month '-' day
                    | unspecified_year '-' month
                    | unspecified_year '-' X X '-' day
                    | unspecified_year '-' X X '-' X X
                    | unspecified_year '-' month '-' X X
                    | year '-' X X '-' day
                    ;

partial_uncertain_or_approximate_or_both : pua_base;

set_representation : one_of_a_set
                   | all_of_a_set
                   ;

one_of_a_set : '[]'
			| '[' list_value (COMMA list_value)* ']'
			;

all_of_a_set: '{}'
			| '{' list_value (COMMA list_value)* '}'
			;

list_value : level_0_expression | level_1_expression;

pua_base : pua_year
         | pua_year_month
         | pua_year_month_day
         ;

pua_year : year UA;

pua_year_month : pua_year '-' month UA
               | year '-' month UA
               ;

pua_year_month_day : pua_year_month '-' day UA
                   | year_month '-' day UA
                   ;

// Vocabulary rules

digit : '0'
      | positive_digit
      ;

positive_digit : '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';

d01_12 : '0' positive_digit
       | '1' '0'
       | '1' '1'
       | '1' '2'
       ;

d01_13 : d01_12
       | '1' '3'
       ;

d01_23 : '0' positive_digit
       | '1' digit
       | '2' '0'
       | '2' '1'
       | '2' '2'
       | '2' '3'
       ;

d00_23 : '0' '0'
       | d01_23
       ;

d01_29 : d01_23
       | '2' '4'
       | '2' '5'
       | '2' '6'
       | '2' '7'
       | '2' '8'
       | '2' '9'
       ;

d01_30 : d01_29
       | '3' '0'
       ;

d01_31 : d01_30
       | '3' '1'
       ;

d01_59 : d01_29
       | '3' digit
       | '4' digit
       | '5' digit
       ;

d00_59 : '0' '0'
       | d01_59
       ;

int1_4 : positive_digit
       | positive_digit digit
       | positive_digit digit digit
       | positive_digit digit digit digit
       ;

integer : positive_digit
        | integer digit
        ;
        
COMMA : ',';
