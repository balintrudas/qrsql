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
package com.github.balintrudas.qrsql.operator;

import org.apache.commons.collections4.ListUtils;

import java.util.Arrays;

/**
 * @author Balint Rudas
 */
public class QrsqlOperator {

    private String name;
    private String[] symbols;

    public QrsqlOperator(String symbol) {
        this.symbols = new String[]{symbol};
    }

    public QrsqlOperator(String[] symbols) {
        this.symbols = symbols;
    }

    public QrsqlOperator(String name, String[] symbols) {
        this.name = name;
        this.symbols = symbols;
    }

    public String getName() {
        return name;
    }

    public String[] getSymbols() {
        return symbols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Operator){
            Operator operator = (Operator) o;
            return !ListUtils.intersection(Arrays.asList(operator.getRsqlOperator()), Arrays.asList(this.symbols)).isEmpty();
        } else if(o instanceof QrsqlOperator){
            QrsqlOperator qrsqlOperator = (QrsqlOperator) o;
            return !ListUtils.intersection(Arrays.asList(qrsqlOperator.getSymbols()), Arrays.asList(this.symbols)).isEmpty();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(symbols);
    }
}
