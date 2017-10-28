package db;

import db.sqlvalue.SQLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jscjs on 2017/2/21.
 */
class Table {
    private List<Column> table;

    Table() {
        table = new ArrayList<>();
    }

    static List<SQLValue> mergeRow(Table t1, int t1r, Table t2, int t2r, List<String> nt) {
        List<SQLValue> ret = new ArrayList<>();
        for (String t : nt) {
            if (t1.containsColumn(t)) {
                ret.add(t1.getColumn(t).get(t1r));
            } else {
                ret.add(t2.getColumn(t).get(t2r));
            }
        }
        return ret;
    }

    public boolean containsColumn(String colName) {
        for (Column c : table) {
            if (c.name.equals(colName)) {
                return true;
            }
        }
        return false;
    }

    void addColumn(Column col) {
        if (colNum() > 0) {
            Database.assertIntEqual(rowNum(), col.length());
        }
        if (!containsColumn(col.name)) {
            table.add(col);
        }
    }

    void addRow(List<SQLValue> row) {
        Database.assertIntEqual(colNum(), row.size());
        for (int i = 0; i < row.size(); i++) {
            table.get(i).addValue(row.get(i));
        }
    }

    static Table addTable(Table t1, Table t2) {
        Table result = new Table();
        for (Column c : t1.table) {
            result.addColumn(c);
        }
        for (Column c : t2.table) {
            if (!result.containsColumn(c.name)) {
                result.addColumn(c);
            }
        }
        return result;
    }

    SQLValue get(int r, int c) {
        if (c < 0 || c > colNum() || r < 0 || r > rowNum()) {
            throw new RuntimeException("get(row,col) out of boundary");
        }
        return table.get(c).get(r);
    }

    Column getColumn(int c) {
        return table.get(c);
    }

    Column getColumn(String colName) {
        for (Column c : table) {
            if (c.name.equals(colName)) {
                return c;
            }
        }
        throw new RuntimeException("No such a column: " + colName);
    }

    //返回的标题里面包含了数据类型
    List<String> getFullTitles() {
        List<String> t = new ArrayList<>();
        for (int i = 0; i < colNum(); i += 1) {
            t.add(table.get(i).name + " " + table.get(i).str_type);
        }
        return t;
    }

    //返回的标题里面不包含数据类型
    List<String> getPartialTitles() {
        List<String> t = new ArrayList<>();
        for (int i = 0; i < colNum(); i += 1) {
            t.add(table.get(i).name);
        }
        return t;
    }


    int colNum() {
        return table.size();
    }

    int rowNum() {
        if (colNum() == 0) return 0;
        return table.get(0).length();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        List<String> titles = getFullTitles();
        for (String title : titles) {
            result.append(title);
            result.append(",");
        }
        result.deleteCharAt(result.lastIndexOf(","));
        result.append("\n");
        for (int i = 0; i < rowNum(); i += 1) {
            for (int j = 0; j < colNum(); j += 1) {
                if (j > 0) {
                    result.append(",");
                }
                result.append(get(i, j).toString());
            }
            if (i != rowNum() - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    //columns looks like this:  [x] [,] [y] [+] [z]
    //args looks like this:  { [x] } { [y] [+] [z] [as] [name] }
    Table subTable(List<String> columns) {
        List<List<String>> args = Database.splitSentence(columns, ",");
        Table t = new Table();
        if (args.get(0).get(0).equals("*")) {
            for (Column c : table) {
                t.addColumn(c.makecopy());
            }
        } else for (List<String> arg : args) {
            if (arg.size() == 1) {
                t.addColumn(getColumn(arg.get(0)).makecopy());
            } else if (arg.size() == 5) {
                Database.assertEqualsOneOf(arg.get(3), "as");
                Column c1 = getColumn(arg.get(0));
                String operator = arg.get(1);
                String right = arg.get(2);
                String new_name = arg.get(4);
                if (Database.sqltype(right) == Database.SqlType.Literal) {
                    SQLValue value = SQLValue.value(right, c1.type, false);
                    Column new_col = Column.math(c1, operator, value, new_name);
                    t.addColumn(new_col);
                } else {
                    Column c2 = getColumn(arg.get(2));
                    Column new_col = Column.math(c1, operator, c2, new_name);
                    t.addColumn(new_col);
                }
            } else {
                throw new RuntimeException("Malformed select : " + arg);
            }
        }
        return t;
    }

    //columns looks like this:  [x] [y] [z]
    Table selectColumns(List<String> columns) {
        Table t = new Table();
        for (String c : columns) {
            if (containsColumn(c)) {
                t.addColumn(getColumn(c).makecopy());
            }
        }
        return t;
    }

    List<SQLValue> getRow(int num) {
        List<SQLValue> row = new ArrayList<>();
        for (Column i : table) {
            row.add(i.get(num));
        }
        return row;
    }

    public Table conditionTable(List<String> condition) {
        if (condition.size() == 0) {
            return this;
        }
        List<List<String>> scond = Database.splitSentence(condition, "and");
        Table newTable = new Table();
        for (Column i : table) {
            newTable.addColumn(new Column(i.name, i.str_type));
        }
        for (int i = 0; i < rowNum(); i++) {
            newTable.determine(this, i, scond);
        }
        return newTable;

    }

    public void determine(Table oldtable, int index, List<List<String>> scond) {
        for (List<String> i : scond) {
            if (Database.sqltype(i.get(2)) == Database.SqlType.Name) {
                if (!SQLValue.compare(oldtable.getColumn(i.get(0)).get(index), i.get(1),
                        oldtable.getColumn(i.get(2)).get(index))) {
                    return;
                }
            }
            if (Database.sqltype(i.get(2)) == Database.SqlType.Literal) {
                if (!SQLValue.compare(oldtable.getColumn(i.get(0)).get(index), i.get(1),
                        SQLValue.value(i.get(2), oldtable.getColumn(i.get(0)).type, false))) {
                    return;
                }
            }
        }
        this.addRow(oldtable.getRow(index));
    }


}
