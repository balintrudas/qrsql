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

import com.querydsl.codegen.EntityType;
import com.querydsl.codegen.TypeFactory;
import com.querydsl.core.alias.DefaultTypeSystem;
import com.querydsl.core.alias.TypeSystem;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * Holds all the information which need to determine the {@link Path} to a field
 * and generate {@link BooleanExpression}.
 *
 * @author Balint Rudas
 */
public class FieldMetadata {

    private String fieldSelector;
    private Integer fieldSelectorIndex;
    private Field field;
    private Class type;
    private Class parameterizedType;
    private Boolean isCollection;
    private EntityType entityType;
    private Class<? extends Path> pathType;
    private FieldMetadata parent;

    public FieldMetadata(Class type, FieldMetadata parent) {
        this.type = type;
        this.parent = parent;
    }

    public FieldMetadata(String fieldSelector, FieldMetadata parent) {
        this.fieldSelector = fieldSelector;
        this.fieldSelectorIndex = parseFieldSelector(fieldSelector);
        this.parent = parent;
        this.field = getField(parent, fieldSelector);
        this.type = getClass(this.field);
        this.entityType = getEntityType(parameterizedType != null ? parameterizedType : type);
        this.pathType = getPathType(this.entityType);
    }

    public FieldMetadata(String fieldSelector, Class rootClass) {
        this.fieldSelector = fieldSelector;
        this.fieldSelectorIndex = parseFieldSelector(fieldSelector);
        this.field = getField(rootClass, fieldSelector, this);
        this.type = getClass(this.field);
        this.entityType = getEntityType(parameterizedType != null ? parameterizedType : type);
        this.pathType = getPathType(this.entityType);
    }

    private Integer parseFieldSelector(String fieldSelector) {
        Integer selectorIndex = getListIndex(fieldSelector);
        if (selectorIndex != null) {
            this.fieldSelector = removeListIndex(fieldSelector);
        }
        return selectorIndex;
    }

    private Class getClass(Field field) {
        TypeSystem typeSystem = new DefaultTypeSystem();
        if (typeSystem.isListType(field.getType()) || typeSystem.isSetType(field.getType())) {
            this.isCollection = true;
            ParameterizedType listType = (ParameterizedType) field.getGenericType();
            this.parameterizedType = (Class<?>) listType.getActualTypeArguments()[0];
        }
        return field.getType();
    }


    private EntityType getEntityType(Class entityClass) {
        TypeFactory typeFactory = new TypeFactory();
        return typeFactory.getEntityType(entityClass);
    }

    private Class getPathType(EntityType entityType) {
        Class<? extends Path> pathType;
        switch (entityType.getOriginalCategory()) {
            case COMPARABLE:
                pathType = ComparablePath.class;
                break;
            case ENUM:
                pathType = EnumPath.class;
                break;
            case DATE:
                pathType = DatePath.class;
                break;
            case DATETIME:
                pathType = DateTimePath.class;
                break;
            case TIME:
                pathType = TimePath.class;
                break;
            case NUMERIC:
                pathType = NumberPath.class;
                break;
            case STRING:
                pathType = StringPath.class;
                break;
            case BOOLEAN:
                pathType = BooleanPath.class;
                break;
            default:
                pathType = EntityPathBase.class;
        }
        return pathType;
    }

    private Field getField(FieldMetadata fieldMetadata, String fieldName) {
        return this.getField(fieldMetadata.getParameterizedType() != null ? fieldMetadata.getParameterizedType() :
                fieldMetadata.getType(), fieldName, fieldMetadata);
    }

    private Field getField(Class<?> clazz, String fieldName, FieldMetadata fieldMetadata) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException nsf) {
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), fieldName, fieldMetadata);
            }
            throw new IllegalStateException("Invalid where clause: '" + fieldMetadata.getFieldSelector() + "." +
                    fieldName + "' Could not locate field '" + fieldName + "' on class " + fieldMetadata.getType());
        }
    }

    /**
     * Parse out the required index of the list.
     * @param fieldSelector
     * @return {@link Integer}
     */
    private Integer getListIndex(String fieldSelector) {
        int startIndex = fieldSelector.indexOf('[');
        int endIndex = fieldSelector.indexOf(']');
        if (startIndex != -1 && endIndex != -1) {
            return Integer.valueOf(fieldSelector.substring(startIndex + 1, endIndex));
        }
        return null;
    }

    /**
     * Remove the index expresson from the selector expression.
     * @param fieldSelector
     * @return {@link Integer}
     */
    private String removeListIndex(String fieldSelector) {
        StringBuilder stringBuilder = new StringBuilder(fieldSelector);
        int startIndex = fieldSelector.indexOf('[');
        int endIndex = fieldSelector.indexOf(']');
        if (startIndex != -1 && endIndex != -1) {
            return stringBuilder.replace(startIndex, endIndex + 1, "").toString();
        }
        return fieldSelector;
    }


    public String getFieldSelector() {
        return fieldSelector;
    }

    public Integer getFieldSelectorIndex() {
        return fieldSelectorIndex;
    }

    public Field getField() {
        return field;
    }

    public Class getType() {
        return type;
    }

    public Class getParameterizedType() {
        return parameterizedType;
    }

    public Class getCollectionType() {
        return parameterizedType != null ? parameterizedType : type;
    }

    public Boolean getCollection() {
        return isCollection;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Class<? extends Path> getPathType() {
        return pathType;
    }

    public FieldMetadata getParent() {
        return parent;
    }
}
