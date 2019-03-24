/*
 * MIT License
 *
 * Copyright (c) 2019 Balint Rudas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.github.balintrudas.qrsql.handler;

import com.github.balintrudas.qrsql.FieldMetadata;
import com.github.balintrudas.qrsql.QrsqlConfig;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.github.balintrudas.qrsql.operator.Operator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;

import java.util.List;

/**
 * @author Balint Rudas
 */
public class StringFieldTypeHandler extends BaseFieldTypeHandler implements FieldTypeHandler {

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
        } else {
            return super.getExpression(path, fieldMetadata, value, operator, qrsqlConfig);
        }
    }
}
