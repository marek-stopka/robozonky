grammar CommonFilters;

import Tokens;

@header {
    import java.util.Collection;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.HashMap;
    import com.github.robozonky.strategy.natural.conditions.*;
}

regionCondition returns [MarketplaceFilterCondition result]:
    { BorrowerRegionCondition c = new BorrowerRegionCondition(); }
    'kraj klienta ' IS (
        (
            r1=regionExpression OR_COMMA { c.add($r1.result); }
        )*
        r2=regionExpression OR { c.add($r2.result); }
    )?
    r3=regionExpression { c.add($r3.result); }
    { $result = c; }
;

incomeCondition returns [MarketplaceFilterCondition result]:
    { final BorrowerIncomeCondition c = new BorrowerIncomeCondition(); }
    'klient ' IS (
        (
            i1=incomeExpression OR_COMMA { c.add($i1.result); }
        )*
        i2=incomeExpression OR { c.add($i2.result); }
    )?
    i3=incomeExpression { c.add($i3.result); }
    { $result = c; }
;

purposeCondition returns [MarketplaceFilterCondition result]:
    { final LoanPurposeCondition c = new LoanPurposeCondition(); }
    'účel ' IS (
        (
            p1=purposeExpression OR_COMMA { c.add($p1.result); }
        )*
        p2=purposeExpression OR { c.add($p2.result); }
    )?
    p3=purposeExpression { c.add($p3.result); }
    { $result = c; }
;

storyCondition returns [MarketplaceFilterCondition result]:
    'příběh ' IS (
        'velmi krátký' { $result = new VeryShortStoryCondition(); }
        | 'kratší než průměrný' { $result = new ShortStoryCondition(); }
        | 'průměrně dlouhý' { $result = new AverageStoryCondition(); }
        | 'delší než průměrný' { $result = new LongStoryCondition(); }
    )
;

termCondition returns [MarketplaceFilterCondition result]:
    'délka ' (
        (c1 = termConditionRangeOpen { $result = $c1.result; })
        | (c2 = termConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = termConditionRangeClosedRight { $result = $c3.result; })
    ) ' měsíců'
;

termConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=intExpr UP_TO max=intExpr
    { $result = new LoanTermCondition($min.result, $max.result); }
;

termConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    MORE_THAN min=intExpr
    { $result = new LoanTermCondition($min.result + 1); }
;

termConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    LESS_THAN max=intExpr
    { $result = new LoanTermCondition(0, $max.result - 1); }
;

relativeTermCondition returns [MarketplaceFilterCondition result]:
    'délka ' (
        (c1 = relativeTermConditionRangeOpen { $result = $c1.result; })
        | (c2 = relativeTermConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = relativeTermConditionRangeClosedRight { $result = $c3.result; })
    ) ' % původní délky'
;

relativeTermConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=intExpr UP_TO max=intExpr
    { $result = new RelativeLoanTermCondition($min.result, $max.result); }
;

relativeTermConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    MORE_THAN min=intExpr
    { $result = new RelativeLoanTermCondition($min.result + 1); }
;

relativeTermConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    LESS_THAN max=intExpr
    { $result = new RelativeLoanTermCondition(0, $max.result - 1); }
;

elapsedTermCondition returns [MarketplaceFilterCondition result]:
    'uhrazeno ' (
        (c1 = elapsedTermConditionRangeOpen { $result = $c1.result; })
        | (c2 = elapsedTermConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = elapsedTermConditionRangeClosedRight { $result = $c3.result; })
    ) ' splátek'
;

elapsedTermConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=intExpr UP_TO max=intExpr
    { $result = new ElapsedLoanTermCondition($min.result, $max.result); }
;

elapsedTermConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    'více než' min=intExpr
    { $result = new ElapsedLoanTermCondition($min.result + 1); }
;

elapsedTermConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    'méně než' max=intExpr
    { $result = new ElapsedLoanTermCondition(0, $max.result - 1); }
;

elapsedRelativeTermCondition returns [MarketplaceFilterCondition result]:
    'uhrazeno ' (
        (c1 = elapsedRelativeTermConditionRangeOpen { $result = $c1.result; })
        | (c2 = elapsedRelativeTermConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = elapsedRelativeTermConditionRangeClosedRight { $result = $c3.result; })
    ) ' % splátek'
;

elapsedRelativeTermConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=intExpr UP_TO max=intExpr
    { $result = new RelativeElapsedLoanTermCondition($min.result, $max.result); }
;

elapsedRelativeTermConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    'více než' min=intExpr
    { $result = new RelativeElapsedLoanTermCondition($min.result + 1); }
;

elapsedRelativeTermConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    'méně než' max=intExpr
    { $result = new RelativeElapsedLoanTermCondition(0, $max.result - 1); }
;

interestCondition returns [MarketplaceFilterCondition result]:
    'úrok ' (
        (c1 = interestConditionRangeOpen { $result = $c1.result; })
        | (c2 = interestConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = interestConditionRangeClosedRight { $result = $c3.result; })
    ) ' % p.a' DOT? // last dot is optional, so that it is possible to end sentence like "p.a." and not "p.a.."
;

interestConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=floatExpr UP_TO max=floatExpr
    { $result = new LoanInterestRateCondition($min.result, $max.result); }
;

interestConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    MORE_THAN min=floatExpr
    { $result = new LoanInterestRateCondition(LoanInterestRateCondition.moreThan($min.result)); }
;

interestConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    LESS_THAN max=floatExpr
    { $result = new LoanInterestRateCondition(BigDecimal.ZERO, LoanInterestRateCondition.lessThan($max.result)); }
;

amountCondition returns [MarketplaceFilterCondition result]:
    'výše ' (
        (c1 = amountConditionRangeOpen { $result = $c1.result; })
        | (c2 = amountConditionRangeClosedLeft { $result = $c2.result; })
        | (c3 = amountConditionRangeClosedRight { $result = $c3.result; })
    ) KC
;

amountConditionRangeOpen returns [MarketplaceFilterCondition result]:
    IS min=intExpr UP_TO max=intExpr
    { $result = new LoanAmountCondition($min.result, $max.result); }
;

amountConditionRangeClosedLeft returns [MarketplaceFilterCondition result]:
    MORE_THAN min=intExpr
    { $result = new LoanAmountCondition($min.result + 1); }
;

amountConditionRangeClosedRight returns [MarketplaceFilterCondition result]:
    LESS_THAN max=intExpr
    { $result = new LoanAmountCondition(0, $max.result - 1); }
;
