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
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Balint Rudas
 */
public class BooleanFieldTypeHandler extends BaseFieldTypeHandler implements FieldTypeHandler {


    @Override
    public Boolean supportsType(Class type) {
        return Boolean.class.equals(type) || boolean.class.equals(type);
    }

    @Override
    public Path getPath(FieldMetadata fieldMetadata, Path parentPath, QrsqlConfig qrsqlConfig) {
        return Expressions.booleanPath(parentPath, fieldMetadata.getFieldSelector());
    }

    @Override
    public Object getValue(List<String> values, FieldMetadata fieldMetadata, QrsqlConfig qrsqlConfig) {
        List<Boolean> converted = new ArrayList<>();
        for (String item : values) {
            converted.add(item.isEmpty() ? null : Boolean.valueOf(item));
        }
        return values.size() > 1 ? converted : converted.get(0);
    }

    @Override
    public BooleanExpression getExpression(Path path, FieldMetadata fieldMetadata, Object value, QrsqlOperator operator, QrsqlConfig qrsqlConfig) {
        if (operator.equals(Operator.ISTRUE)) {
            return ((BooleanPath) path).isTrue();
        } else if (operator.equals(Operator.ISFALSE)) {
            return ((BooleanPath) path).isFalse();
        } else if (operator.equals(Operator.IN)) {
            return ((SimpleExpression) path).in((List<Boolean>) value);
        } else if (operator.equals(Operator.NOTIN)) {
            return ((SimpleExpression) path).notIn((List<Boolean>) value);
        } else {
            return super.getExpression(path, fieldMetadata, value, operator, qrsqlConfig);
        }
    }
}
