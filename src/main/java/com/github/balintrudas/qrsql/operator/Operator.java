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

import java.util.HashMap;
import java.util.Map;

/**
 * Default operators
 *
 * @author Balint Rudas
 */
public enum Operator {
    //Basic operators
    EQUALS("=eq=", "=="), NOTEQUALS("=ne=", "!="), IN("=in="), NOTIN("=notin=", "=notIn=", "=out="),
    ISNULL("=isnull=", "=isNull="), ISNOTNULL("=notnull=", "=notNull=", "=isnotnull=", "=isNotNull="),
    //String operators
    EQUALS_IGNORECASE("=eqic=", "=equalsignorecase=", "=equalsIgnoreCase="), NOTEQUALS_IGNORECASE("=noteqic=", "=notequalsignorecase=", "=notEqualsIgnoreCase="),
    LIKE("=like="), NOTLIKE("=notlike=", "=notLike="), LIKE_IGNORECASE("=likeic=", "=likeignorecase=", "=likeIgnoreCase="),
    STARTWITH("=startsw=", "=startswith=", "=startsWith="), STARTWITH_IGNORECASE("=startswic=", "=startswithignorecase=", "=startsWithIgnoreCase="),
    ENDWITH("=endsw=", "=endswith=", "=endsWith="), ENDWITH_IGNORECASE("=endswic=", "=endswithignorecase=", "=endsWithIgnoreCase="),
    ISEMPTY("=isempty=", "=isEmpty=", "=empty="), ISNOTEMPTY("=notempty=", "=notEmpty=", "=isnotempty=", "=isNotEmpty="),
    CONTAINS("=con=", "=contains="), CONTAINS_IGNORECASE("=conic=", "=containsignorecase=", "=containsIgnoreCase="),
    //Number,Date
    GREATER("=gt=", ">", "=greater="), GREATER_OR_EQUALS("=goe=", "=ge=", ">=", "=greaterorequals=", "=greaterOrEquals="),
    LESS_THAN("=lt=", "<", "=lessthan=", "=lessThan="), LESS_THAN_OR_EQUALS("=loe=", "=le=", "<=", "=lessthanorequals=", "=lessThanOrEquals="),
    //Boolean
    ISTRUE("=istrue=", "=isTrue="), ISFALSE("=isfalse=", "=isFalse="),
    //Date
    BEFORE("=before="), AFTER("=after=");

    private String[] rsqlOperator;
    private static final Map<String, Operator> lookup = new HashMap<String, Operator>();

    Operator(String... rsqlOperator) {
        this.rsqlOperator = rsqlOperator;
    }

    static {
        for (Operator d : Operator.values()) {
            for (String symbol : d.getRsqlOperator()) {
                lookup.put(symbol, d);
            }
        }
    }

    public String[] getRsqlOperator() {
        return rsqlOperator;
    }

    public static Operator get(String operator) {
        return lookup.get(operator);
    }

    public static Map<String, Operator> getLookup() {
        return lookup;
    }
}
