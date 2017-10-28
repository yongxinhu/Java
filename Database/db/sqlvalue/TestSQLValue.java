package db.sqlvalue;

import db.Database;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jscjs on 2017/2/19.
 */
public class TestSQLValue {
    @Test
    public void testsqlvalue() {
        SQLValue zero = SQLNumber.value("0");
        SQLValue two = SQLValue.value("2");
        SQLValue four = SQLValue.value("4");
        SQLValue four_f = SQLValue.value("4.0", Database.ValueType.Float, true);
        SQLValue seven = SQLValue.value("7");
        SQLValue eight = SQLValue.value("8");
        SQLValue eleven = SQLValue.value("11");
        SQLValue four_seven = SQLValue.value("4.70");
        SQLValue jsc = SQLValue.value("'jsc'");
        SQLValue hello = SQLValue.value("'hello'");
        SQLValue nan = SQLValue.value("NaN", Database.ValueType.Int, true);
        SQLValue str_no = SQLValue.value("NOVALUE", Database.ValueType.String, true);
        SQLValue num_no = SQLValue.value("NOVALUE", Database.ValueType.Float, true);


        assertTrue(SQLValue.compare(four, "<=", four));
        assertTrue(SQLValue.compare(four, "=", four_f));
        assertTrue(SQLValue.compare(four, "<", four_seven));
        assertFalse(SQLValue.compare(four_seven, "<=", four));
        assertTrue(SQLValue.compare(jsc, ">", hello));
        assertEquals("Int", SQLValue.math(four, "/", eight).type.toString());
        assertEquals("'jschello'", SQLValue.math(jsc, "+", hello).toString());
        assertTrue(SQLValue.compare(eleven, "=", SQLValue.math(seven, "+", four)));
        assertFalse(SQLValue.compare(two, "!=", SQLValue.math(eight, "/", four)));
        assertTrue(SQLValue.compare(zero, "=", SQLValue.math(seven, "/", eight)));
        assertTrue(SQLValue.compare(SQLValue.value("0.5"), "=", SQLValue.math(four_f, "/", eight)));
        assertTrue(SQLValue.compare(eight, "<", nan));
        assertTrue(SQLValue.compare(nan, "=", nan));
        assertTrue(SQLValue.compare(nan, "=", SQLValue.math(nan, "-", seven)));
        assertEquals("'jsc'", SQLValue.math(jsc, "+", str_no).toString());
        assertTrue(SQLValue.compare(four, "=", SQLValue.math(four, "+", num_no)));
        assertTrue(SQLValue.compare(zero, "=", SQLValue.math(four, "*", num_no)));
        assertTrue(SQLValue.compare(nan, "=", SQLValue.math(four, "/", zero)));
        assertFalse(SQLValue.compare(zero, "=", num_no));
        assertEquals("4.700", four_seven.toString());
        assertTrue(SQLValue.compare(zero, "=", SQLValue.math(num_no, "-", num_no)));
        //SQLValue.math(str_no,"+",num_no);
    }
}
