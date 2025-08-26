package com.iitp.global.util.query;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.StringTemplate;

public class QueryExpressionFormatter {
    public static NumberExpression<Double> roundDoubleByFirstDecimalPlace(NumberExpression<Double> doubleExpression) {
        return Expressions.numberTemplate(Double.class, "round({0}, 1)", doubleExpression);
    }

    public static StringTemplate getImageKeyPath(StringPath path) {
        return Expressions.stringTemplate("MIN({0})", path);
    }

}
