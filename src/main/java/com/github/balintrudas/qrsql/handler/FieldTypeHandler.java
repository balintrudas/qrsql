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

import com.github.balintrudas.qrsql.QrsqlConfig;
import com.github.balintrudas.qrsql.FieldMetadata;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.List;

/**
 * @author Balint Rudas
 */
public interface FieldTypeHandler {

    /**
     * Whether the given {@link Class type parameter} is
     * supported by this handler.
     * @param type the type to check
     * @return true if this handler supports the supplied parameter; false otherwise
     */
    Boolean supportsType(Class type);

    /**
     * Transform {@link FieldMetadata} to Querydsl {@link Path}.
     * @param fieldMetadata Field properties
     * @param parentPath Parent path
     * @param qrsqlConfig qrsqlConfig
     * @return Querydsl {@link Path}
     */
    Path getPath(FieldMetadata fieldMetadata, Path parentPath, QrsqlConfig qrsqlConfig);

    /**
     * Transform {@code List<String> values} to field type specific value
     * @param values Values from rsql expression
     * @param fieldMetadata Field properties
     * @param qrsqlConfig qrsqlConfig
     * @return field type specific value
     */
    Object getValue(List<String> values, FieldMetadata fieldMetadata, QrsqlConfig qrsqlConfig);

    /**
     * Build a querydsl boolean expression
     * @param path Target path
     * @param fieldMetadata Field properties
     * @param value Transformed value
     * @param operator Operator from rsql expression
     * @param qrsqlConfig qrsqlConfig
     * @return {@link BooleanExpression}
     */
    BooleanExpression getExpression(Path path, FieldMetadata fieldMetadata, Object value, QrsqlOperator operator, QrsqlConfig qrsqlConfig);

}
