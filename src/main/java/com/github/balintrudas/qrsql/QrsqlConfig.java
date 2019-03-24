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
package com.github.balintrudas.qrsql;


import com.github.balintrudas.qrsql.exception.QrsqlException;
import com.github.balintrudas.qrsql.exception.TypeNotSupportedException;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.github.balintrudas.qrsql.handler.FieldTypeHandler;
import com.github.balintrudas.qrsql.util.QrsqlUtil;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to build an initialization configuration to {@link Qrsql.Builder}
 *
 * @param <E> Target type
 * @author Balint Rudas
 */
public class QrsqlConfig<E> {

    private EntityManager entityManager;
    private List<QrsqlOperator> operators;
    private List<FieldTypeHandler> fieldTypeHandlers;
    private String dateFormat;

    private QrsqlConfig(Builder<E> builder) {
        this.entityManager = builder.entityManager;
        this.operators = builder.operators;
        this.dateFormat = builder.dateFormat;
        this.fieldTypeHandlers = QrsqlUtil.getDefaultFieldTypeHandlers();
        if (builder.fieldTypeHandlers != null) {
            this.fieldTypeHandlers.addAll(0, builder.fieldTypeHandlers);
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<QrsqlOperator> getOperators() {
        return this.operators;
    }

    public void setOperators(List<QrsqlOperator> operators) {
        this.operators = operators;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setFieldTypeHandlers(List<FieldTypeHandler> fieldTypeHandlers) {
        this.fieldTypeHandlers = QrsqlUtil.getDefaultFieldTypeHandlers();
        if (fieldTypeHandlers != null) {
            this.fieldTypeHandlers.addAll(0,fieldTypeHandlers);
        }
    }

    /**
     * Determine the FieldType of the given class.
     *
     * @param type Field class type
     * @return {@link FieldTypeHandler}
     * @throws TypeNotSupportedException If the requested field type is not supported
     */
    public FieldTypeHandler getFieldTypeHandler(Class type) {
        for (FieldTypeHandler fieldType : this.fieldTypeHandlers) {
            if (fieldType.supportsType(type)) {
                return fieldType;
            }
        }
        throw new TypeNotSupportedException("Type is not supported: " + type.toString());
    }

    /**
     * Help to create and configure {@link QrsqlConfig}
     *
     * @param <E> Target type
     */
    public static class Builder<E> {
        private EntityManager entityManager;
        private List<QrsqlOperator> operators;
        private List<FieldTypeHandler> fieldTypeHandlers;
        private String dateFormat = null;

        public Builder(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        private Builder(QrsqlConfig.Builder<E> builder) {
            this.entityManager = builder.entityManager;
            this.operators = builder.operators;
            this.fieldTypeHandlers = builder.fieldTypeHandlers;
            this.dateFormat = builder.dateFormat;
        }

        public QrsqlConfig.Builder<E> entityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
            return this;
        }


        public QrsqlConfig.Builder<E> operators(List<QrsqlOperator> operators) {
            this.operators = operators;
            return this;
        }

        public QrsqlConfig.Builder<E> operator(QrsqlOperator operator) {
            this.operators = new ArrayList<>();
            this.operators.add(operator);
            return this;
        }

        public QrsqlConfig.Builder<E> fieldTypeHandlers(List<FieldTypeHandler> fieldTypeHandlers) {
            this.fieldTypeHandlers = fieldTypeHandlers;
            return this;
        }

        public QrsqlConfig.Builder<E> fieldTypeHandler(FieldTypeHandler fieldTypeHandler) {
            this.fieldTypeHandlers = new ArrayList<>();
            this.fieldTypeHandlers.add(fieldTypeHandler);
            return this;
        }

        public QrsqlConfig.Builder<E> dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public QrsqlConfig<E> build() throws QrsqlException {
            try {
                return new QrsqlConfig<E>(this);
            } catch (Exception ex) {
                throw new QrsqlException(ex);
            }
        }


    }
}
