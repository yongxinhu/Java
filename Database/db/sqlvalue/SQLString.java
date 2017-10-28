package db.sqlvalue;

import db.Database;

/**
 */
class SQLString extends SQLValue {
    private final String data;

    SQLString(String s) {
        if (s.equals("NOVALUE")) {
            type = Database.ValueType.NOVALUE;
            data = "''";
        } else {
            Database.assertLiteralValueType(s, Database.ValueType.String);
            data = s;
            type = Database.ValueType.String;
        }
    }

    SQLString(SQLString s) {
        data = s.data;
        type = s.type;
    }

    static SQLString getNoValue() {
        return new SQLString("NOVALUE");
    }

    @Override
    public String toString() {
        if (type == Database.ValueType.NaN) {
            return "NaN";
        } else if (type == Database.ValueType.NOVALUE) {
            return "NOVALUE";
        }
        return data;
    }

    @Override
    int compareTo(SQLValue v) {
        if (!(v instanceof SQLString)) {
            throw new RuntimeException("Bad compare between [" + this + "] and [" + v + "]");
        }
        return data.compareTo(((SQLString) v).data);
    }

    @Override
    SQLString add(SQLValue v) {
        if (!(v instanceof SQLString)) {
            throw new RuntimeException("Can't add [" + this.type + "] and [" + v.type + "]");
        }
        if (((SQLString) v).data.equals("NaN")) {
            return ((SQLString) v);
        }
        String t1 = data.substring(0, data.length() - 1);
        String t2 = ((SQLString) v).data.substring(1);
        return new SQLString(t1 + t2);
    }

    @Override
    SQLString sub(SQLValue v) {
        throw new RuntimeException("string cannot do 'sub'");
    }

    @Override
    SQLString mul(SQLValue v) {
        throw new RuntimeException("string cannot do 'mul'");
    }

    @Override
    SQLString div(SQLValue v) {
        throw new RuntimeException("string cannot do 'div'");
    }

    @Override
    SQLString zero() {
        return new SQLString("''");
    }


}
