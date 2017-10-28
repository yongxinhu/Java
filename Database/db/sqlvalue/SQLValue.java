package db.sqlvalue;


import db.Database;

public abstract class SQLValue {

    Database.ValueType type;

    /**
     * 比较两个SQLValue
     */
    public static boolean compare(SQLValue a, String operator, SQLValue b) {
        if (a.type == Database.ValueType.NOVALUE || b.type == Database.ValueType.NOVALUE) {
            return false;
        }
        switch (operator) {
            case "<=":
                return a.compareTo(b) <= 0;
            case "==":
                return a.compareTo(b) == 0;
            case ">=":
                return a.compareTo(b) >= 0;
            case "!=":
                return a.compareTo(b) != 0;
            case "<":
                return a.compareTo(b) < 0;
            case ">":
                return a.compareTo(b) > 0;
        }
        throw new RuntimeException("Bad operator :[" + operator + "]");
    }

    /**
     * 加减乘除
     */
    public static SQLValue math(SQLValue a, String operator, SQLValue b) {
        if (a.type == Database.ValueType.NOVALUE) {
            a = a.zero();
        }
        if (b.type == Database.ValueType.NOVALUE) {
            b = b.zero();
        }
        switch (operator) {
            case "+":
                return a.add(b);
            case "-":
                return a.sub(b);
            case "*":
                return a.mul(b);
            case "/":
                return a.div(b);
        }
        throw new RuntimeException("Bad operator :[" + operator + "]");
    }

    /**
     * -------------- 这个是为了测试方便用的，除了测试以外不要用  ----------------
     */
    static SQLValue value(String literal) {
        Database.assertSqlType(literal, Database.SqlType.Literal);
        if (literal.equals("NaN") || literal.equals("NOVALUE")) {
            throw new RuntimeException("SQLValue.value(literal) doesn't accept: " + literal);
        }
        if (Database.isLiteralValueType(literal, Database.ValueType.String)) {
            return new SQLString(literal);
        } else if (Database.isLiteralValueType(literal, Database.ValueType.Int)) {
            return new SQLNumber<Integer>(Integer.parseInt(literal));
        } else {
            return new SQLNumber<Float>(Float.parseFloat(literal));
        }

    }

    /**
     * --------------- Load和Insert的时候用这个   ------------------
     * 把任意的Literal转换成内部的类型(包括 NaN 和 NOVALUE )
     * 例子参考TestSQLVTestSQLValue.java
     */
    public static SQLValue value(String literal, Database.ValueType type, boolean acceptSpecialValue) {
        Database.assertSqlType(literal, Database.SqlType.Literal);
        if (!acceptSpecialValue && (literal.equals("NaN") || literal.equals("NOVALUE"))) {
            throw new RuntimeException("SQLValue.value(literal,type,acceptSpecialDatatype) doesn't accept: " + literal);
        }
        switch (type) {
            case String:
                return new SQLString(literal);
            case Int:
                if (literal.equals("NaN")) {
                    return SQLNumber.getNaN();
                } else if (literal.equals("NOVALUE")) {
                    return SQLNumber.getNoValue(type);
                }
                Database.assertLiteralValueType(literal, Database.ValueType.Int);
                return new SQLNumber<Integer>(Integer.parseInt(literal));
            case Float:
                if (literal.equals("NaN")) {
                    return SQLNumber.getNaN();
                } else if (literal.equals("NOVALUE")) {
                    return SQLNumber.getNoValue(type);
                }
                Database.assertLiteralValueType(literal, Database.ValueType.Float);
                return new SQLNumber<Float>(Float.parseFloat(literal));
            default:
                throw new RuntimeException("Wrong using of SQLValue.value(literal,type),type should not be: " + type);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        SQLValue v = (SQLValue) o;
        return compare(this, "=", v);
    }

    abstract int compareTo(SQLValue v);

    abstract SQLValue add(SQLValue v);

    abstract SQLValue sub(SQLValue v);

    abstract SQLValue mul(SQLValue v);

    abstract SQLValue div(SQLValue v);

    abstract SQLValue zero();
}
