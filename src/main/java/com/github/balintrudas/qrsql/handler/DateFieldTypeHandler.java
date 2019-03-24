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
import com.github.balintrudas.qrsql.util.DateUtil;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Balint Rudas
 */
public class DateFieldTypeHandler extends BaseFieldTypeHandler implements FieldTypeHandler {

    @Override
    public Boolean supportsType(Class type) {
        return java.util.Date.class.equals(type) || java.sql.Timestamp.class.equals(type) ||
                java.sql.Date.class.equals(type) || java.sql.Time.class.equals(type);
    }

    @Override
    public Path getPath(FieldMetadata fieldMetadata, Path parentPath, QrsqlConfig qrsqlConfig) {
        return Expressions.dateTimePath(fieldMetadata.getType(), parentPath, fieldMetadata.getFieldSelector());
    }

    @Override
    public Object getValue(List<String> values, FieldMetadata fieldMetadata, QrsqlConfig qrsqlConfig) {
        List<Date> converted = new ArrayList<>();
        String dateFormat = qrsqlConfig.getDateFormat();
        for (String item : values) {
            try {
                Date parsedDate = null;
                if(!item.isEmpty()) {
                    parsedDate = dateFormat != null ? DateUtil.parse(item, dateFormat) : DateUtil.parse(item);
                    if (parsedDate == null) {
                        parsedDate = new Date(Long.valueOf(item));
                    }
                }
                converted.add(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return values.size() > 1 ? converted : converted.get(0);
    }

    @Override
    public BooleanExpression getExpression(Path path, FieldMetadata fieldMetadata, Object value, QrsqlOperator operator, QrsqlConfig qrsqlConfig) {
        if (operator.equals(Operator.BEFORE)) {
            return ((DateTimePath) path).before((Date) value);
        } else if (operator.equals(Operator.AFTER)) {
            return ((DateTimePath) path).after((Date) value);
        } else if (operator.equals(Operator.GREATER)) {
            return ((DateTimePath) path).gt((Date) value);
        } else if (operator.equals(Operator.GREATER_OR_EQUALS)) {
            return ((DateTimePath) path).goe((Date) value);
        } else if (operator.equals(Operator.LESS_THAN)) {
            return ((DateTimePath) path).lt((Date) value);
        } else if (operator.equals(Operator.LESS_THAN_OR_EQUALS)) {
            return ((DateTimePath) path).loe((Date) value);
        } else if (operator.equals(Operator.IN)) {
            return ((SimpleExpression) path).in((List<Date>) value);
        } else if (operator.equals(Operator.NOTIN)) {
            return ((SimpleExpression) path).notIn((List<Date>) value);
        } else {
            return super.getExpression(path, fieldMetadata, value, operator, qrsqlConfig);
        }
    }
}
