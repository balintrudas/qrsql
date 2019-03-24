![banner](https://user-images.githubusercontent.com/22676978/54791812-cbf31e00-4c3b-11e9-8a4e-fb669aa38be5.jpg)

# Qrsql - RSQL to Querydsl
RESTful Service Query Language (RSQL) is a language and a library designed for searching entries in RESTful services.
Querydsl is a framework which enables the construction of type-safe SQL-like queries for multiple backends.
This library build a bridge between RSQL query language and Querydsl framework. Currently JPA model supported by the library.

RSQL:   
https://github.com/jirutka/rsql-parser  
https://kosapi.fit.cvut.cz/projects/kosapi/wiki (RSQL was originally created for KOSapi)  
Querydsl:   
https://github.com/querydsl/querydsl

# Example

```java
// e.g. http://localhost:8080/car?select=((engine.screws.size=in=(2.2,5.5) and mfgdt=before='2018.03.01') or brand!='bmw')


Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                  .selectFrom("Car")
                  .where("(engine.screws.size=in=(2.2,5.5) and mfgdt=before='2018.03.01') or brand!='bmw'")
                  .build();
List<Car> cars = qrsql.fetch();
```

# Usage
Qrsql supports multi-level fields of the entity.
#### Multi-level where clause
```java
//Engine (@OneToOne) -> List<Screw> (@OneToMany) -> String contains "1"

new Qrsql.Builder<Car>(entityManager).selectFrom("Car")
            .where("engine.screws.name=con='1'")....
```
#### Select
```java
new Qrsql.Builder<Car>(entityManager)
            .select("name,description")....
            //or
            .select(Projections.bean(Car.class, QCar.car.name, QCar.car.description))....
            //or
            .selectFrom("Car")....
            //or
            .selectFrom(Car.class)....
```
#### Where
```java
new Qrsql.Builder<Car>(entityManager).selectFrom("Car")
            .where("description=notempty=''")....
            //or
            .where(QCar.car.description.isNotEmpty())....
```
#### Sort
```java
new Qrsql.Builder<Car>(entityManager).selectFrom("Car").where(...)
            ...
            .sort("name.asc, description.desc")....
            //or
            .sort(Collections.singletonList(new OrderSpecifier(Order.ASC,QCar.car.name)))....
```
#### Page
```java
new Qrsql.Builder<Car>(entityManager).selectFrom("Car").where(...)
            ...
            .page("0,15")....
            //or
            .page(0L,15L)....
```
#### Limit
```java
new Qrsql.Builder<Car>(entityManager).selectFrom("Car").where(...)
            ...
            .limit("0,15")....
            //or
            .limit(0L,15L)....
```
#### Offset and size
```java
new Qrsql.Builder<Car>(entityManager).selectFrom("Car").where(...)
            ...
            .offset(0L)
            .size(15L)....
```
### Operators
Operator   | Syntax
------------- | -------------------------
Equals	| `=eq=` `==`
Not equals	| `=ne=` `!=`
In	| `=in=`
Not in	| `=notin=` `=notIn=` `=out=`
Is null	| `=isnull=` `=isNull=`
Is not null	| `=notnull=` `=notNull=` `=isnotnull=` `=isNotNull=`
Equals with ignore case	| `=eqic=` `=equalsignorecase=` `=equalsIgnoreCase=`
Not equals with ignore case	| `=noteqic=` `=notequalsignorecase=` `=notEqualsIgnoreCase=`
Like | `=like=`
Not like | `=notlike=` `=notLike=` 
Like with ignore case | `=likeic=` `=likeignorecase=` `=likeIgnoreCase=`
Starts with | `=startsw=` `=startswith=` `=startsWith=`
Starts with ignore case | `=startswic=` `=startswithignorecase=` `=startsWithIgnoreCase=`
Ends with | `=endsw=` `=endswith=` `=endsWith=`
Ends with ignore case | `=endswic=` `=endswithignorecase=` `=endsWithIgnoreCase=`
Is empty | `=isempty=` `=isEmpty=` `=empty=`
Is not empty | `=notempty=` `=notEmpty=` `=isnotempty=` `=isNotEmpty=`
Contains | `=con=` `=contains=`
Contains with ignore case | `=conic=` `=containsignorecase=` `=containsIgnoreCase=`
Greater | `=gt=` `>` `=greater=`
Greater or equals | `=goe=` `=ge=` `>=` `=greaterorequals=` `=greaterOrEquals=`
Less than | `=lt=` `<` `=lessthan=` `=lessThan=`
Less than or equals | `=loe=` `=le=` `<=` `=lessthanorequals=` `=lessThanOrEquals=`
Is true | `=istrue=` `=isTrue=`
Is false | `=isfalse=` `=isFalse=`
Before | `=before=`
After | `=after=`

### Custom operators
```java
new Qrsql.Builder<Car>(entityManager, Collections.singletonList(new QrsqlOperator("=custom=")), null)....
//or
new Qrsql.Builder<Car>(entityManager). .... .operator(new QrsqlOperator("=custom=")).build();
//or
QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager).operator(new QrsqlOperator("=custom=")).build();
new Qrsql.Builder<Car>(config)....
```

### Custom field type handler
By default, the library supports the following types:  
java.lang.Boolean, java.lang.Character, java.util.Collection, java.util.Date, java.sql.Timestamp, java.sql.Date, java.sql.Time,
java.lang.Enum, java.util.List, java.lang.Integer, int, java.lang.Byte, byte, java.lang.Long, long,
java.lang.Short, short, java.lang.Double, double, java.lang.Float, float, java.lang.BigInteger, java.lang.BigDecimal, java.lang.Number,
java.util.Set, java.lang.String.  

If you need, you can add a custom field type handler.
```java
public class CustomFieldTypeHandler implements FieldTypeHandler {
    @Override
    public Boolean supportsType(Class type) {...}
    @Override
    public Path getPath(FieldMetadata fieldMetadata, Path parentPath, QrsqlConfig qrsqlConfig) {...}
    @Override
    public Object getValue(List<String> values, FieldMetadata fieldMetadata, QrsqlConfig qrsqlConfig) {...}
    @Override
    public BooleanExpression getExpression(Path path, FieldMetadata fieldMetadata, Object value, QrsqlOperator operator, QrsqlConfig qrsqlConfig) {...}
}
```

```java
new Qrsql.Builder<Car>(entityManager, null, Collections.singletonList(new CustomFieldTypeHandler()))....
//or
new Qrsql.Builder<Car>(entityManager). ... .fieldTypeHandler(new CustomFieldTypeHandler()).build()
//or
QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager).fieldTypeHandler(new CustomFieldTypeHandler()).build();
new Qrsql.Builder<Car>(config)....
```

### Date format
By default, the library supports the following date formats:  
MM-yyyy, MM-yyyy, yyyy-MM, yyyy-MM, yy-MM, yy-MM-dd, yy.MM.dd, yy-MM-dd HH:mm, yyyy, dd-MM-yyyy, dd.MM.yyyy, yyyy-MM-dd, 
yyyy.MM.dd, MM/dd/yyyy, yyyy/MM/dd, dd MMM yyyy, dd MMMM yyyy, yyyyMMdd HHmm, dd-MM-yyyy HH:mm, yyyy-MM-dd HH:mm, yyyy-MM-dd HH:mm, 
MM/dd/yyyy HH:mm, yyyy/MM/dd HH:mm, dd MMM yyyy HH:mm, dd MMMM yyyy HH:mm, yyyyMMdd HHmmss, dd-MM-yyyy HH:mm:ss, yyyy-MM-dd HH:mm:ss, 
MM/dd/yyyy HH:mm:ss, yyyy/MM/dd HH:mm:ss, dd MMM yyyy HH:mm:ss, dd MMMM yyyy HH:mm:ss

If you need, you can add a custom date format.
```java
new Qrsql.Builder<Car>(entityManager). ... .dateFormat("yyyy-MM-dd").build();
//or
QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager).dateFormat("yyyy-MM-dd").build();
new Qrsql.Builder<Car>(config)....
```

### Predicate and OrderSpecifier

```java
Qrsql qrsql = new Qrsql.Builder<Car>(entityManager).selectFrom("Car")....
Predicate predicate = qrsql.buildPredicate();
OrderSpecifier[] orders = qrsql.buildOrder();
```

# License
MIT: https://opensource.org/licenses/MIT