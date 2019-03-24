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

import com.google.common.collect.Lists;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cz.jirutka.rsql.parser.ast.*;
import com.github.balintrudas.qrsql.exception.QrsqlException;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;

import java.util.List;

/**
 * Rsql visitor class to build a {@link BooleanExpression}
 * @author Balint Rudas
 */
public class PredicateBuilderVisitor implements RSQLVisitor<Predicate, Predicate> {

    private Class rootClass;
    private PredicateBuilder predicateBuilder;

    public PredicateBuilderVisitor(Class rootClass, PredicateBuilder predicateBuilder) {
        this.rootClass = rootClass;
        this.predicateBuilder = predicateBuilder;
    }

    @Override
    public Predicate visit(AndNode node, Predicate param) {
            return getLogicalExpression(node, param, Ops.AND);
    }

    @Override
    public Predicate visit(OrNode node, Predicate param) {
            return getLogicalExpression(node, param, Ops.OR);
    }

    @Override
    public Predicate visit(ComparisonNode node, Predicate param) {
            Predicate expression = buildExpression(node.getSelector(), node.getOperator().getSymbol(), node.getArguments());
            if(expression==null){
                throw new QrsqlException("Can't build predicate with selector: " + node.getSelector() + " operator: " +
                        node.getOperator().getSymbol() + " value: " + node.getArguments().toString());
            }
            return expression;
    }

    private BooleanExpression getLogicalExpression(LogicalNode node, Predicate param, Ops logicalOperator) {
        List<Node> children = Lists.newArrayList(node.getChildren());
        Node firstNode = children.remove(0);
        BooleanExpression predicate = (BooleanExpression) firstNode.accept(this, param);
        for (Node subNode : children) {
            BooleanExpression subPredicate = (BooleanExpression) subNode.accept(this, param);
            predicate = combineLogicalExpression(logicalOperator, predicate, subPredicate);
        }
        return predicate;
    }

    private BooleanExpression combineLogicalExpression(Ops logicalOperator, BooleanExpression predicate, Predicate subPredicate) {
        BooleanExpression combinedPredicate = predicate;
        if (Ops.AND.equals(logicalOperator)) {
            combinedPredicate = predicate.and(subPredicate);
        } else if (Ops.OR.equals(logicalOperator)) {
            combinedPredicate = predicate.or(subPredicate);
        }
        return combinedPredicate;
    }

    public BooleanExpression buildExpression(String selector, String operator, List<String> values) {
        return predicateBuilder.getExpression(this.rootClass, selector, values, new QrsqlOperator(operator));
    }
}
