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

import com.github.balintrudas.qrsql.exception.EntityNotFoundException;
import com.github.balintrudas.qrsql.exception.QrsqlException;
import com.github.balintrudas.qrsql.exception.TypeNotSupportedException;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.github.balintrudas.qrsql.handler.FieldTypeHandler;
import com.github.balintrudas.qrsql.util.QrsqlUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for create querydsl based query from rsql expression.
 *
 * @param <E> Target type
 * @author Balint Rudas
 */
public class Qrsql<E> {

    private PredicateBuilder predicateBuilder;
    private Class entityClass;
    private String select;
    private Expression expressionSelect;
    private String where;
    private Predicate predicateWhere;
    private Long offset;
    private Long size;
    private String sort;
    private List<OrderSpecifier> orderSpecifiers;
    private QrsqlConfig<E> qrsqlConfig;

    /**
     * Validate the required parameters and create a new instance of {@link Qrsql}.
     *
     * @param builder Builder object which hold all the information to create a new {@link Qrsql}
     * @throws EntityNotFoundException If the root class is <tt>null</tt>.
     */
    private Qrsql(Builder<E> builder) throws EntityNotFoundException {
        this.qrsqlConfig = builder.qrsqlConfig;
        this.predicateBuilder = new PredicateBuilder(this.qrsqlConfig);
        if (this.qrsqlConfig.getOperators() != null) {
            QrsqlUtil.validateOperators(this.qrsqlConfig.getOperators());
        }
        if (this.qrsqlConfig.getEntityManager() == null) {
            throw new IllegalArgumentException("Entity manager cannot be null.");
        }
        this.entityClass = builder.entityClass != null ? builder.entityClass :
                QrsqlUtil.getClassForEntityString(builder.entityName, this.qrsqlConfig.getEntityManager());
        if (this.entityClass == null) {
            throw new EntityNotFoundException("Can't find entity with name: ".concat(builder.entityName));
        }
        this.select = builder.select;
        this.expressionSelect = builder.expressionSelect;
        this.where = builder.where;
        this.predicateWhere = builder.predicateWhere;
        this.offset = builder.offset;
        this.size = builder.size;
        this.sort = builder.sort;
        this.orderSpecifiers = builder.orderSpecifiers;
    }

    /**
     * Build a {@link JPAQuery}.
     *
     * @param selectFieldPath Converted selection expression. It can be null.
     * @return querydsl {@code JPAQuery} to fetch from database
     * @throws QrsqlException If some exception occurred during building {@link JPAQuery}.
     */
    private JPAQuery buildQuery(List<Path> selectFieldPath) throws QrsqlException {
        try {
            //Build predicate
            Predicate predicate = this.predicateWhere != null ? this.predicateWhere : buildPredicate();
            JPAQueryFactory query = new JPAQueryFactory(this.qrsqlConfig.getEntityManager());
            PathBuilder fromPath = new PathBuilder(this.entityClass, this.entityClass.getSimpleName().toLowerCase());

            JPAQuery jpaQuery;
            //Build select
            if ((selectFieldPath != null && selectFieldPath.size() > 0) || this.expressionSelect != null) {
                if (this.expressionSelect != null) {
                    jpaQuery = query.select(this.expressionSelect);
                } else {
                    jpaQuery = query.select(QrsqlUtil.convertPathToExpression(selectFieldPath));
                }
                jpaQuery = (JPAQuery) jpaQuery.from(fromPath).where(predicate);
            } else {
                jpaQuery = query.from(fromPath).where(predicate);
            }
            //Offset and size part
            if (this.offset != null) {
                jpaQuery.offset(this.offset);
            }

            if (this.size != null) {
                jpaQuery.limit(this.size);
            }
            //OrderBy part
            OrderSpecifier[] orderSpecifiers = buildOrder();
            if (orderSpecifiers.length > 0) {
                jpaQuery.orderBy(orderSpecifiers);
            }

            return jpaQuery;

        } catch (Exception ex) {
            throw new QrsqlException(ex);
        }
    }

    /**
     * Fetch multiple result sets.
     *
     * @return List of fetched records
     * @throws QrsqlException If some exception occurred during building {@link JPAQuery}.
     */
    public List<E> fetch() throws QrsqlException {
        List result = new ArrayList();
        List<Path> selectFieldPath = buildSelectPath();
        JPAQuery jpaQuery = buildQuery(selectFieldPath);
        if (selectFieldPath != null && selectFieldPath.size() > 0) {
            List<Tuple> tupleList = jpaQuery.fetch();
            result = tupleList;
        } else {
            result = jpaQuery.fetch();
        }
        return result;
    }

    /**
     * Fetch a single result.
     *
     * @return Fetched record
     * @throws QrsqlException If some exception occurred during building {@link JPAQuery}.
     */
    public Object fetchOne() throws QrsqlException {
        List<Path> selectFieldPath = buildSelectPath();
        JPAQuery jpaQuery = buildQuery(selectFieldPath);
        if (selectFieldPath != null && selectFieldPath.size() > 0) {
            Tuple tuple = (Tuple) jpaQuery.fetchOne();
            return tuple;
        } else {
            return jpaQuery.fetchOne();
        }
    }

    /**
     * Create a {@link Predicate} with the builder parameters.
     *
     * @return Querydsl {@link Predicate}
     * @throws QrsqlException If some exception ccourred during parse and convert rsql expression to {@link Predicate}.
     */
    public Predicate buildPredicate() throws QrsqlException {
        try {
            Node rootNode = new RSQLParser(QrsqlUtil.getOperators(this.qrsqlConfig.getOperators())).parse(this.where);
            return rootNode.accept(new PredicateBuilderVisitor(this.entityClass, this.predicateBuilder));
        } catch (Exception ex) {
            throw new QrsqlException(ex);
        }
    }

    /**
     * Create a {@link OrderSpecifier} with the builder parameters.
     *
     * @return Querydsl {@link OrderSpecifier}
     */
    public OrderSpecifier[] buildOrder() {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        if (this.sort != null) {
            Map<String, Order> sorts = QrsqlUtil.parseSortExpression(this.sort);
            for (String sortSelect : new ArrayList<>(sorts.keySet())) {
                Path sortPath = getPath(QrsqlUtil.parseFieldSelector(entityClass, sortSelect));
                orderSpecifiers.add(new OrderSpecifier(sorts.get(sortSelect), sortPath));
            }
        } else if (this.orderSpecifiers != null) {
            orderSpecifiers.addAll(this.orderSpecifiers);
        }
        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }

    /**
     * Convert select string expression to querydsl Path.
     *
     * @return
     */
    private List<Path> buildSelectPath() {
        List<Path> selectFieldPath = null;
        if (this.select != null) {
            selectFieldPath = QrsqlUtil.parseSelect(this.select, this.entityClass);
        }
        return selectFieldPath;
    }

    /**
     * @param fieldMetadataList
     * @return querydsl {@link Path}
     * @throws TypeNotSupportedException If the requested field type is not supported
     */
    private Path getPath(List<FieldMetadata> fieldMetadataList) throws TypeNotSupportedException {
        Path rootPath = Expressions.path(entityClass, entityClass.getSimpleName().toLowerCase());
        List<Path> processedPaths = new ArrayList<>();
        FieldTypeHandler fieldType = null;
        for (int i = 0; i < fieldMetadataList.size(); i++) {
            fieldType = this.qrsqlConfig.getFieldTypeHandler(fieldMetadataList.get(i).getType());
            processedPaths.add(fieldType.getPath(fieldMetadataList.get(i), i == 0 ? rootPath : processedPaths.get(i - 1), this.qrsqlConfig));
        }
        return processedPaths.get(processedPaths.size() - 1);
    }

    /**
     * Help to create complex object with all the required parameter which is used to create a {@link Qrsql}.
     *
     * @param <E> Target type
     */
    public static class Builder<E> {
        private Class entityClass;
        private String entityName;
        private String select;
        private Expression expressionSelect;
        private String where;
        private Predicate predicateWhere;
        private Long offset;
        private Long size;
        private String sort;
        private List<OrderSpecifier> orderSpecifiers;
        private QrsqlConfig<E> qrsqlConfig;

        /**
         * Create a {@link Builder} with {@link QrsqlConfig}.
         *
         * @param qrsqlConfig configuration
         */
        public Builder(QrsqlConfig<E> qrsqlConfig) {
            this.qrsqlConfig = qrsqlConfig;
        }

        /**
         * Create a {@link Builder} with {@link EntityManager}.
         *
         * @param entityManager entityManager
         */
        public Builder(EntityManager entityManager) {
            this(entityManager, null, null);
        }

        /**
         * Create a {@link Builder} with {@link EntityManager} and {@link QrsqlOperator}.
         *
         * @param entityManager     entityManager
         * @param operators         custom operators
         * @param fieldTypeHandlers custom handlers
         */
        public Builder(EntityManager entityManager, List<QrsqlOperator> operators, List<FieldTypeHandler> fieldTypeHandlers) {
            this.qrsqlConfig = new QrsqlConfig.Builder<E>(entityManager)
                    .operators(operators)
                    .fieldTypeHandlers(fieldTypeHandlers)
                    .build();
        }

        private Builder(Builder<E> builder) {
            this.entityClass = builder.entityClass;
            this.entityName = builder.entityName;
            this.select = builder.select;
            this.expressionSelect = builder.expressionSelect;
            this.where = builder.where;
            this.predicateWhere = builder.predicateWhere;
            this.offset = builder.offset;
            this.size = builder.size;
            this.sort = builder.sort;
            this.qrsqlConfig = builder.qrsqlConfig;
            this.orderSpecifiers = builder.orderSpecifiers;
        }

        /**
         * Select specific fields with string expression.
         *
         * @param select select string expression
         * @return {@link SelectBuilder}
         */
        public SelectBuilder<E> select(String select) {
            this.select = select;
            return new SelectBuilder<E>(this);
        }

        /**
         * Select specific fields with {@link Expression}.
         *
         * @param expression select expression
         * @return {@link SelectBuilder}
         */
        public SelectBuilder<E> select(Expression expression) {
            this.expressionSelect = expression;
            return new SelectBuilder<E>(this);
        }

        /**
         * Set the Qrsql target type with the given class name.
         *
         * @param entityName target type name
         * @return {@link FromBuilder}
         */
        public FromBuilder<E> selectFrom(String entityName) {
            this.entityName = entityName;
            return new FromBuilder<E>(this);
        }

        /**
         * Set the Qrsql target type with the given class.
         *
         * @param entityClass target type
         * @return {@link FromBuilder}
         */
        public FromBuilder<E> selectFrom(Class entityClass) {
            this.entityClass = entityClass;
            return new FromBuilder<E>(this);
        }

        public static class SelectBuilder<E> {

            private Builder<E> builder;

            public SelectBuilder(Qrsql.Builder<E> builder) {
                this.builder = builder;
            }

            /**
             * Set the Qrsql target type with the given class name.
             *
             * @param entityName target type name
             * @return {@link FromBuilder}
             */
            public FromBuilder<E> from(String entityName) {
                builder.entityName = entityName;
                return new FromBuilder<E>(builder);
            }

            /**
             * Set the Qrsql target type with the given class.
             *
             * @param entityClass target type
             * @return {@link FromBuilder}
             */
            public FromBuilder<E> from(Class entityClass) {
                builder.entityClass = entityClass;
                return new FromBuilder<E>(builder);
            }
        }

        public static class FromBuilder<E> {

            private Builder<E> builder;

            public FromBuilder(Qrsql.Builder<E> builder) {
                this.builder = builder;
            }

            /**
             * Add the given filter condition with {@link String} expression.
             *
             * @param where where string expression
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> where(String where) {
                builder.where = where;
                return new BuildBuilder<E>(builder);
            }

            /**
             * Add the given filter condition with {@link Predicate}.
             *
             * @param where Where predicate
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> where(Predicate where) {
                builder.predicateWhere = where;
                return new BuildBuilder<E>(builder);
            }

        }

        public static class BuildBuilder<E> extends Qrsql.Builder<E> {

            public BuildBuilder(Builder<E> builder) {
                super(builder);
            }

            /**
             * Create a new instance of {@link Qrsql}.
             *
             * @return new Qrsql
             * @throws QrsqlException If some of the given parameters is invalid.
             */
            public Qrsql<E> build() throws QrsqlException {
                try {
                    return new Qrsql<E>(this);
                } catch (Exception ex) {
                    throw new QrsqlException(ex);
                }
            }

            /**
             * Defines the limit / max results and the offset for the query results.
             *
             * @param offset row offset
             * @param size   max rows
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> limit(Long offset, Long size) {
                super.offset = offset;
                super.size = size;
                return this;
            }

            /**
             * Defines the limit / max results and the offset for the query results with {@link String} expression.
             *
             * @param limit max rows
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> limit(String limit) {
                List<Long> limitParams = null;
                try {
                    limitParams = QrsqlUtil.parseTwoParamExpression(limit);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid limit expression: '" + limit + "' . Excepted format: '(offset,size)' .");
                }
                return limit(limitParams.get(0), limitParams.get(1));
            }

            /**
             * Defines the limit / max results and the offset for the query results with pagination logic.
             *
             * @param pageNumber Zero based
             * @param pageSize   max rows
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> page(Long pageNumber, Long pageSize) {
                super.size = pageSize;
                super.offset = pageNumber * pageSize;
                return this;
            }

            /**
             * Defines the limit / max results and the offset for the query results with pagination logic.
             *
             * @param page Page expression
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> page(String page) {
                List<Long> pageParams = null;
                try {
                    pageParams = QrsqlUtil.parseTwoParamExpression(page);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid page expression: '" + page + "' . Excepted format: '(pageNumber,pageSize)' .");
                }
                return page(pageParams.get(0), pageParams.get(1));
            }

            /**
             * Defines the offset for the query results.
             *
             * @param offset row offset
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> offset(Long offset) {
                super.offset = offset;
                return this;
            }

            /**
             * Defines the limit / max results for the query results.
             *
             * @param size max rows
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> size(Long size) {
                super.size = size;
                return this;
            }

            /**
             * Set the order expressions with {@link String}.
             *
             * @param sort order
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> sort(String sort) {
                super.sort = sort;
                return this;
            }

            /**
             * Set the order expressions with {@link OrderSpecifier}.
             *
             * @param orderSpecifiers order
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> sort(List<OrderSpecifier> orderSpecifiers) {
                super.orderSpecifiers = orderSpecifiers;
                return this;
            }

            /**
             * Add additional operators to rsql parser.
             *
             * @param operators Must match <tt>=[a-zA-Z]*=|[&gt;&lt;]=?|!=</tt>
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> operators(List<QrsqlOperator> operators) {
                super.qrsqlConfig.setOperators(operators);
                return this;
            }

            /**
             * Add additional operator to rsql parser.
             *
             * @param operator Must match <tt>=[a-zA-Z]*=|[&gt;&lt;]=?|!=</tt>
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> operator(QrsqlOperator operator) {
                List<QrsqlOperator> operators = new ArrayList<>();
                operators.add(operator);
                super.qrsqlConfig.setOperators(operators);
                return this;
            }

            /**
             * Add additional field type handlers.
             *
             * @param fieldTypeHandlers Custom field types
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> fieldTypeHandlers(List<FieldTypeHandler> fieldTypeHandlers) {
                super.qrsqlConfig.setFieldTypeHandlers(fieldTypeHandlers);
                return this;
            }

            /**
             * Add additional field type handler to rsql parser.
             *
             * @param fieldTypeHandler Custom field type
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> fieldTypeHandler(FieldTypeHandler fieldTypeHandler) {
                List<FieldTypeHandler> fieldTypes = new ArrayList<>();
                fieldTypes.add(fieldTypeHandler);
                super.qrsqlConfig.setFieldTypeHandlers(fieldTypes);
                return this;
            }

            /**
             * Define the date format for Qrsql parser.
             *
             * @param dateFormat date format string
             * @return {@link BuildBuilder}
             */
            public BuildBuilder<E> dateFormat(String dateFormat) {
                super.qrsqlConfig.setDateFormat(dateFormat);
                return this;
            }

        }

    }

}
