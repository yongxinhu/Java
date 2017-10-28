package db;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestDataBase {

    public static void main(String[] args) {

    }

    @Test
    public void Test_type() {
        assertEquals(Database.SqlType.Literal, Database.sqltype("'aa  a'"));
        assertEquals(Database.SqlType.Literal, Database.sqltype("'0.j.sc'"));
        assertEquals(Database.SqlType.Literal, Database.sqltype(".23"));
        assertEquals(Database.SqlType.Literal, Database.sqltype("23.5"));
        assertEquals(Database.SqlType.Literal, Database.sqltype("023.50"));
        assertNotEquals(Database.SqlType.Literal, Database.sqltype("2.3.5"));
        assertNotEquals(Database.SqlType.Literal, Database.sqltype("23.5 "));
        assertNotEquals(Database.SqlType.Literal, Database.sqltype("-23.5"));
        assertEquals(Database.SqlType.KeyWord, Database.sqltype("select"));
        assertNotEquals(Database.SqlType.Name, Database.sqltype("_j4sc"));
        assertNotEquals(Database.SqlType.Name, Database.sqltype("0jsc"));
        assertEquals(Database.SqlType.Operator, Database.sqltype(">="));
    }

    @Test
    public void test_assertType() {
        Database.assertSqlType("as", Database.SqlType.KeyWord);
        Database.assertSqlType("23.5", Database.SqlType.Literal);
        Database.assertSqlType("sdf'", Database.SqlType.Malformed);
        Database.assertSqlType("<=", Database.SqlType.Operator);
        Database.assertLiteralValueType("23.4", Database.ValueType.Float);
        Database.assertLiteralValueType("4", Database.ValueType.Int);
        Database.assertLiteralValueType("'sdf \"a. a,'", Database.ValueType.String);
        Database.assertLiteralValueType("NOVALUE", Database.ValueType.Int);
        Database.assertEqualsOneOf("select", "select");
        Database.assertEqualsOneOf("int", "int", "float", "string");
        Database.assertEqualsOneOf("float", "int", "float", "string");
        //Database.assertString("hhhhh","int","float","string");
    }

    @Test
    public void test_getArgs() {
        List<String> p = Database.toSentence("colA type1,colB type2,col3 type4,'234,345'");
        System.out.println(p);
        List<List<String>> result = Database.splitSentence(p, ",");
        System.out.println(result);
    }

}
