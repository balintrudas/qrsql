package com.github.balintrudas.qrsql.test.handler;

import com.github.balintrudas.qrsql.FieldMetadata;
import com.github.balintrudas.qrsql.QrsqlConfig;
import com.github.balintrudas.qrsql.handler.FieldTypeHandler;
import com.github.balintrudas.qrsql.operator.Operator;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;

import java.util.List;

public class CustomFieldTypeHandler implements FieldTypeHandler {
    @Override
    public Boolean supportsType(Class type) {
        return String.class.equals(type);
    }

    @Override
    public Path getPath(FieldMetadata fieldMetadata, Path parentPath, QrsqlConfig qrsqlConfig) {
        return Expressions.stringPath(parentPath, fieldMetadata.getFieldSelector());
    }

    @Override
    public Object getValue(List<String> values, FieldMetadata fieldMetadata, QrsqlConfig qrsqlConfig) {
        return values.size() > 1 ? values : values.get(0);
    }

    @Override
    public BooleanExpression getExpression(Path path, FieldMetadata fieldMetadata, Object value, QrsqlOperator operator, QrsqlConfig qrsqlConfig) {
        if (operator.equals(Operator.EQUALS_IGNORECASE)) {
            return ((StringPath) path).equalsIgnoreCase((String) value);
        } else if (operator.equals(Operator.NOTEQUALS_IGNORECASE)) {
            return ((StringPath) path).notEqualsIgnoreCase((String) value);
        } else if (operator.equals(Operator.LIKE)) {
            return ((StringPath) path).like((String) value);
        } else if (operator.equals(Operator.LIKE_IGNORECASE)) {
            return ((StringPath) path).likeIgnoreCase((String) value);
        } else if (operator.equals(Operator.STARTWITH)) {
            return ((StringPath) path).startsWith((String) value);
        } else if (operator.equals(Operator.STARTWITH_IGNORECASE)) {
            return ((StringPath) path).startsWithIgnoreCase((String) value);
        } else if (operator.equals(Operator.ENDWITH)) {
            return ((StringPath) path).endsWith((String) value);
        } else if (operator.equals(Operator.ENDWITH_IGNORECASE)) {
            return ((StringPath) path).endsWithIgnoreCase((String) value);
        } else if (operator.equals(Operator.ISEMPTY)) {
            return ((StringPath) path).isEmpty();
        } else if (operator.equals(Operator.ISNOTEMPTY)) {
            return ((StringPath) path).isNotEmpty();
        } else if (operator.equals(Operator.CONTAINS)) {
            return ((StringPath) path).contains((String) value);
        } else if (operator.equals(Operator.CONTAINS_IGNORECASE)) {
            return ((StringPath) path).containsIgnoreCase((String) value);
        } else if (operator.equals(Operator.IN)) {
            return ((SimpleExpression) path).in((List<String>) value);
        } else if (operator.equals(Operator.NOTIN)) {
            return ((SimpleExpression) path).notIn((List<String>) value);
        }else{
            return ((StringPath) path).isNotEmpty();
        }
    }
}
