package com.github.balintrudas.qrsql.test;

import com.github.balintrudas.qrsql.Qrsql;
import com.github.balintrudas.qrsql.QrsqlConfig;
import com.github.balintrudas.qrsql.exception.QrsqlException;
import com.github.balintrudas.qrsql.operator.QrsqlOperator;
import com.github.balintrudas.qrsql.test.handler.CustomFieldTypeHandler;
import com.github.balintrudas.qrsql.test.model.Car;
import com.github.balintrudas.qrsql.test.model.Engine;
import com.github.balintrudas.qrsql.test.model.Screw;
import com.github.balintrudas.qrsql.test.model.ScrewType;
import com.github.balintrudas.qrsql.test.repository.CarRepository;
import com.github.balintrudas.qrsql.test.repository.EngingeRepository;
import com.github.balintrudas.qrsql.test.repository.ScrewRepository;
import com.querydsl.core.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.isA;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QrsqlTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ScrewRepository screwRepository;

    @Autowired
    private EngingeRepository engingeRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static boolean dataLoaded = false;

    @Before
    public void initData() {
        if (!dataLoaded) {
            for (int i = 0; i < 50; i++) {
                Random rand = new Random();
                int randomNum = rand.nextInt((100 - 1) + 1) + 1;

                Screw screw = new Screw();
                screw.setName("Screw name " + randomNum);
                screw.setScrewType(randomEnum(ScrewType.class));
                screw.setDescription("Descreption screw " + randomNum);
                screw.setSize((long) randomNum);
                Screw savedScrew = screwRepository.save(screw);

                List<Screw> screws = new ArrayList<>();
                screws.add(savedScrew);

                Screw screw2 = new Screw();
                screw2.setName("Screw name " + randomNum);
                screw2.setScrewType(randomEnum(ScrewType.class));
                screw2.setDescription("Descreption screw " + randomNum);
                screw2.setSize((long) randomNum);
                Screw savedScrew2 = screwRepository.save(screw);

                List<Screw> screws2 = new ArrayList<>();
                screws2.add(savedScrew2);

                Engine engine = new Engine();
                engine.setName("Engine" + randomNum);
                engine.setDescription("Engine description " + randomNum);
                engine.setScrews(screws2);
                Engine savedEngine = engingeRepository.save(engine);

                Car car = new Car();
                car.setDescription("Descreption car " + randomNum);
                car.setName("Béla" + i);
                car.setActive(Math.random() < 0.5);
                car.setMfgdt(new Date());
                car.setScrews(screws);
                car.setEngine(savedEngine);
                carRepository.save(car);
            }
            dataLoaded = true;
        }
    }

    public <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        SecureRandom random = new SecureRandom();
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void shouldReadQrsqlConfig() {
        QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager).build();
        Qrsql<Car> qrsql = new Qrsql.Builder<>(config).selectFrom("Car").where("description=notempty=''").build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't read config", !cars.isEmpty());
        Assert.assertEquals("Can't read config correctly", 50, cars.size());
    }

    @Test
    public void shouldReadQrsqlConfigWithOperator() {
        QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager)
                .operator(new QrsqlOperator("=customnotempty="))
                .fieldTypeHandler(new CustomFieldTypeHandler()).build();
        Qrsql<Car> qrsql = new Qrsql.Builder<>(config)
                .selectFrom("Car")
                .where("description=customnotempty=''")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't read config with custom operator", !cars.isEmpty());
        Assert.assertEquals("Can't read config correctly with custom operator", 50, cars.size());
    }

    @Test
    public void shouldNotFindCustomOperator() {
        thrown.expectCause(isA(QrsqlException.class));
        thrown.expectMessage("Unknown operator: =customnotempty=");
        QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager)
                .fieldTypeHandler(new CustomFieldTypeHandler()).build();
        Qrsql<Car> qrsql = new Qrsql.Builder<>(config)
                .selectFrom("Car")
                .where("description=customnotempty=''")
                .build();
        List<Car> cars = qrsql.fetch();
    }

    @Test
    public void shouldReadQrsqlConfigWithFieldTypeHandler() {
        QrsqlConfig<Car> config = new QrsqlConfig.Builder<Car>(entityManager)
                .fieldTypeHandler(new CustomFieldTypeHandler()).build();
        Qrsql<Car> qrsql = new Qrsql.Builder<>(config)
                .selectFrom("Car")
                .where("description=notempty=''")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't read config with custom field type handler", !cars.isEmpty());
        Assert.assertEquals("Can't read config correctly with custom field type handler", 50, cars.size());
    }

    @Test
    public void shouldHandleMultiLevelQuery() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom("Car")
                .where("engine.screws.name=con='name'")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle multi level field query", !cars.isEmpty());
        Assert.assertEquals("Can't handle multi level field query correctly", 50, cars.size());
    }

    @Test
    public void shouldReturnEmptyListHandleMultiLevelQuery() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom("Car")
                .where("engine.screws.name=con='Eszti'")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle multi level field query", cars.isEmpty());
    }

    @Test
    public void shouldHandleSelectFromString() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom("Car")
                .where("id=notnull=''")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle select from with String", !cars.isEmpty());
        Assert.assertEquals("Can't handle select from with String correctly", 50, cars.size());
    }

    @Test
    public void shouldHandleSelectFromClass() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom(Car.class)
                .where("id=notnull=''")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle select from with Class", !cars.isEmpty());
        Assert.assertEquals("Can't handle select from with Class correctly", 50, cars.size());
    }

    @Test
    public void shouldHandleNotNullDate() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom("Car")
                .where("mfgdt=notnull=''")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle notnull operator for Date type", !cars.isEmpty());
        Assert.assertEquals("Can't handle notnull operator for Date type correctly", 50, cars.size());
    }

    @Test
    public void shouldHandleNumberIn() {
        Qrsql<Car> qrsql = new Qrsql.Builder<Car>(entityManager)
                .selectFrom("Car")
                .where("id=in=(3,6,9)")
                .build();
        List<Car> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle in operator for Number tpye", !cars.isEmpty());
        Assert.assertEquals("Can't handle in operator for Number tpy correctly", 3, cars.size());
    }

    @Test
    public void shouldReturnTupleWithSelectExpression() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle select expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle select expression correctly", 50, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
    }

    @Test
    public void shouldReturnTupleInDescOrder() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .sort("id.desc")
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle select expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle select expression correctly", 50, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
        Assert.assertTrue("Not in order", cars.get(0).toArray()[0].equals("Béla49") && cars.get(1).toArray()[0].equals("Béla48"));

    }

    @Test
    public void shouldReturnTupleWithPageString() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .sort("id.desc")
                .page("1,15")
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle page expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle page expression", 15, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
        Assert.assertTrue("Not in order", cars.get(0).toArray()[0].equals("Béla34") && cars.get(1).toArray()[0].equals("Béla33"));
    }

    @Test
    public void shouldReturnTupleWithPageNumber() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .sort("id.desc")
                .page(1L,15L)
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle page expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle page expression", 15, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
        Assert.assertTrue("Not in order", cars.get(0).toArray()[0].equals("Béla34") && cars.get(1).toArray()[0].equals("Béla33"));
    }

    @Test
    public void shouldReturnTupleWithLimitString() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .sort("id.desc")
                .limit("15,15")
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle limit expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle limit expression", 15, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
        Assert.assertTrue("Not in order", cars.get(0).toArray()[0].equals("Béla34") && cars.get(1).toArray()[0].equals("Béla33"));
    }

    @Test
    public void shouldReturnTupleWithLimitNumber() {
        Qrsql qrsql = new Qrsql.Builder<Car>(entityManager)
                .select("name,description").from("Car")
                .where("id=notnull=''")
                .sort("id.desc")
                .limit(15L,15L)
                .build();
        List<Tuple> cars = qrsql.fetch();
        Assert.assertTrue("Can't handle limit expression", !cars.isEmpty());
        Assert.assertEquals("Can't handle limit expression", 15, cars.size());
        Assert.assertEquals("More than two column", 2, cars.get(0).toArray().length);
        Assert.assertTrue("Not in order", cars.get(0).toArray()[0].equals("Béla34") && cars.get(1).toArray()[0].equals("Béla33"));
    }

}