package db;

import db.sqlvalue.SQLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jscjs on 2017/2/21.
 */
class Column {
    final String name;
    final Database.ValueType type;
    final String str_type;
    private List<SQLValue> col;

    Column(String name, String type) {
        col = new ArrayList<>();
        this.name = name;
        switch (type) {
            case "int":
                this.type = Database.ValueType.Int;
                break;
            case "float":
                this.type = Database.ValueType.Float;
                break;
            case "string":
                this.type = Database.ValueType.String;
                break;
            default:
                throw new RuntimeException("ERROR: unknown type: " + type);
        }
        str_type = type;
    }

    static Column math(Column c1, String operator, Column c2, String new_col_name) {
        Database.assertIntEqual(c1.length(), c2.length());
        String new_type;
        if (c1.str_type.equals("string") && c2.str_type.equals("string")) {
            new_type = "string";
        } else if (c1.str_type.equals("int") && c2.str_type.equals("int")) {
            new_type = "int";
        } else {
            new_type = "float";
        }
        Column new_col = new Column(new_col_name, new_type);
        for (int i = 0; i < c1.length(); i++) {
            new_col.addValue(SQLValue.math(c1.get(i), operator, c2.get(i)));
        }
        return new_col;
    }

    static Column math(Column c1, String operator, SQLValue value, String new_col_name) {
        Column new_col = new Column(new_col_name, c1.str_type);
        for (int i = 0; i < c1.length(); i++) {
            new_col.addValue(SQLValue.math(c1.get(i), operator, value));
        }
        return new_col;
    }

    SQLValue get(int index) {
        return col.get(index);
    }

    int length() {
        return col.size();
    }

    void addValue(SQLValue value) {
        col.add(value);
    }

    public Column makecopy() {
        Column c = new Column(name,str_type);
        for(SQLValue v : col) {
            c.addValue(v);
        }
        return c;
    }


}
