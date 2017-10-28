package db;

import db.sqlvalue.SQLValue;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 */

public class Database {
    private static List<String> keywords;
    static String[] OPES = {"+", "-", "*", "/", ">", ">=", "<", "<=", "==", "!=", ",", "(", ")"};
    static String[] OPR = {"", "-", "", "/", ">", ">=", "<", "<=", "==", "!=", ",", "\\(", "\\)"};
    // Various common constructs, simplifies parsing.
    private static final String REST = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            BLANK = "\\s*";
    // Stage 1 syntax, contains the command name.
    static final Pattern CREATE_CMD = Pattern.compile(BLANK + "create" + BLANK + "table " + REST),
            LOAD_CMD = Pattern.compile(BLANK + "load " + REST),
            STORE_CMD = Pattern.compile(BLANK + "store " + REST),
            DROP_CMD = Pattern.compile(BLANK + "drop" + BLANK + "table " + REST),
            INSERT_CMD = Pattern.compile(BLANK + "insert" + BLANK + "into " + REST),
            PRINT_CMD = Pattern.compile(BLANK + "print " + REST),
            SELECT_CMD = Pattern.compile(BLANK + "select " + REST);
    // Stage 2 syntax, contains the clauses of commands.
    static final String CN = "(\\S+)\\s+\\((\\S+\\s+\\S+\\s*" + "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)";
    static final String STL1 = "([^,]+?(?:,[^,]+?)*)\\s+from\\s+";
    static final String STL2 = "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+";
    static final String STL3 = "([\\w\\s+\\-*/'<>=!]+?(?:\\s+and\\s+";
    static final String STL4 = "[\\w\\s+\\-*/'<>=!]+?)*))?";
    private static final Pattern CREATE_NEW = Pattern.compile(CN),
            SELECT_CLS = Pattern.compile(STL1 + STL2 + STL3 + STL4),
            CREATE_SEL = Pattern.compile("(\\S+)\\s+as select\\s+" + SELECT_CLS.pattern()),
            INSERT_CLS = Pattern.compile("(\\S+)\\s+values\\s+(.+?" + "\\s*(?:,\\s*.+?\\s*)*)");
    Map<String, Table> tablemap;
    List<String> queryRecorder = new ArrayList<>();

    //----------------------------------------------------------------------------
    public Database() {
        tablemap = new HashMap<String, Table>();
        keywords = new ArrayList<>();
        keywords.add("select");
        keywords.add("create");
        keywords.add("table");
        keywords.add("insert");
        keywords.add("drop");
        keywords.add("load");
        keywords.add("store");
        keywords.add("from");
        keywords.add("where");
        keywords.add("into");
        keywords.add("as");
        keywords.add("exit");
        keywords.add("values");
        OPR[0] = "\\+";
        OPR[2] = "\\*";
    }

    public static List<String> toSentence(String buf) {
        List<String> result = new ArrayList<String>();
        if (buf.length() == 0) {
            return result;
        }
        List<String> quotes = processQuote(buf);
        buf = quotes.remove(quotes.size() - 1);
        buf = buf.replaceAll("~", "_BLX_")
                .replaceAll(">=", "_DYDY_")
                .replaceAll("<=", "_XYDY_")
                .replaceAll("!=", "_BDY_");
        char[] cbuf = buf.toCharArray();
        for (int i = 0; i < cbuf.length; i += 1) {
            if (cbuf[i] == '-') {
                if (i == 0) {
                    cbuf[i] = '~';
                } else if (cbuf[i + 1] != ' '
                        && (!(cbuf[i - 1] >= '0'
                        && cbuf[i - 1] <= '9'
                        || cbuf[i - 1] >= 'a'
                        && cbuf[i - 1] <= 'z'))) {
                    cbuf[i] = '~';
                }
            }
        }
        buf = new String(cbuf);
        for (String op : OPR) {
            buf = buf.replaceAll(op, " " + op + " ");
        }
        buf = buf.replaceAll("_DYDY_", " >= ")
                .replaceAll("_XYDY_", " <= ")
                .replaceAll("_BDY_", " != ")
                .replaceAll("~", "-")
                .replaceAll("_BLX_", "~");
        String[] bufs = buf.split(" ");
        for (String s : bufs) {
            s = s.trim();
            if (s.length() > 0) {
                result.add(s);
            }
        }
        for (int i = 0; i < result.size(); i += 1) {
            String s = result.get(i);
            if (s.matches("_STR_[0-9]+")) {
                int k = Integer.parseInt(s.substring(5));
                result.set(i, quotes.get(k));
            }
        }
        for (String s : result) {
            if (sqltype(s) == SqlType.Malformed) {
                throw new RuntimeException("ERROR: malformed : [" + s + "]");
            }
        }
        return result;
    }

    public static List<List<String>> splitSentence(List<String> sentence, String splitor) {
        List<List<String>> result = new ArrayList<>();
        if (sentence.size() == 0) {
            return result;
        }
        int i = 0;
        while (i < sentence.size()) {
            i += 1;
            if (i < sentence.size() && sentence.get(i).equals(splitor)) {
                result.add(sentence.subList(0, i));
                sentence = sentence.subList(i + 1, sentence.size());
                i = 0;
            }
        }
        result.add(sentence);
        return result;
    }

    private static List<String> processQuote(String buf) {
        if (buf == null) {
            return null;
        }
        int i, j = 0, count = 0;
        List<String> record = new ArrayList<>();
        String temp;
        String doneBuf = buf;
        while (true) {
            i = buf.indexOf('\'', j);
            if (i < 0) {
                break;
            }
            j = buf.indexOf('\'', i + 1);
            if (j < 0) {
                throw new RuntimeException("ERROR: Quotes are malformed");
            }
            temp = buf.substring(i, j + 1);

            doneBuf = doneBuf.replace(temp, " _STR_" + Integer.toString(count) + ' ');
            record.add(temp);
            j += 1;
            count++;
        }
        record.add(doneBuf);
        return record;
    }
    //---------------------------------------------------------------------

    /**
     * determine the sqltype of a string
     *
     * @param s
     * @return (Literal, Keyword, Operator, Name, Malformed)
     */
    public static SqlType sqltype(String s) {
        if (s.matches("^'([^']*)'$")
                || s.matches("^-?[0-9]+$")
                || s.matches("^-?([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)$")
                || s.equals("NaN")
                || s.equals("NOVALUE")) {
            return SqlType.Literal;
        }
        for (String k : keywords) {
            if (s.equals(k)) {
                return SqlType.KeyWord;
            }
        }
        for (String k : OPES) {
            if (s.equals(k)) {
                return SqlType.Operator;
            }
        }
        if (s.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            return SqlType.Name;
        }
        return SqlType.Malformed;
    }

    public static void assertSqlType(String s, SqlType type) {
        if (sqltype(s) != type) {
            throw new RuntimeException("expect " + type + " but get [" + s + "]");
        }
    }

    public static void assertLiteralValueType(String s, ValueType type) {
        if (!isLiteralValueType(s, type)) {
            throw new RuntimeException("expect " + type + "but get :[" + s + "]");
        }
    }

    public static boolean isLiteralValueType(String s, ValueType type) {
        if (s.equals("NOVALUE") || s.equals("NaN")) {
            return true;
        }
        if (type == ValueType.Int && s.matches("^-?[0-9]+$")) {
            return true;
        } else if (type == ValueType.Float
                && (s.matches("^-?([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)$"))) {
            return true;
        } else if (type == ValueType.String && s.matches("^'([^']*)'$")) {
            return true;
        }
        return false;
    }

    //assert String "actual" equals at least one of the expects
    public static void assertEqualsOneOf(String actual, String... expects) {
        String e = "";
        for (String exp : expects) {
            if (actual.equals(exp)) {
                return;
            }
            e += "{" + exp + "}";
        }
        e = e.replaceAll("}\\{", "|");
        throw new RuntimeException("expect one of these: " + e + " but get [" + actual + "]");
    }

    public static void assertIntEqual(int expect, int actual) {
        if (expect != actual) {
            throw new RuntimeException("expect " + expect + " but get " + actual);
        }
    }


    public String transact(String query) {
        String ret;
        try {
            ret = processQuery(query);
        } catch (RuntimeException e) {
            ret = "ERROR: " + e.getMessage();
        } catch (IOException e) {
            ret = "ERROR: " + e.getMessage();
        }
        return ret;
    }

    public String processQuery(String query) throws IOException {
        String ret = "";
        List<String> sentence = toSentence(query);
        Matcher m;
        if ((m = CREATE_CMD.matcher(query)).matches()) {
            ret += createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
            ret += loadTable(sentence);
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
            ret += storeTable(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
            ret += dropTable(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
            ret += insertRow(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
            ret += printTable(sentence);
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
            Table t = select(m.group(1));
            ret += t.toString();
        } else if (sentence.get(0).equals("exit")) {
            ret += "exit";
        } else {
            throw new RuntimeException("Malformed query: " + query);
        }
        return ret;
    }

    //----------------------------------------------------------------------//
    Table getTable(String name) {
        if (tablemap.containsKey(name)) {
            return tablemap.get(name);
        }
        throw new RuntimeException("can't find table: " + name);
    }

    private String createTable(String expr) {
        Matcher m;
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            createNewTable(m.group(1), m.group(2).split(COMMA));
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            createSelectedTable(m.group(1), m.group(2), m.group(3), m.group(4));
        } else {
            throw new RuntimeException("Malformed create: " + expr);
        }
        return "";
    }

    private void createNewTable(String name, String[] cols) {
        Table newTable = new Table();
        for (String i : cols) {
            List<String> col = toSentence(i);
            newTable.addColumn(new Column(col.get(0), col.get(1)));
        }
        tablemap.put(name, newTable);

    }

    private String createSelectedTable(String name, String exprs, String tables, String conds) {
        Table t = select(exprs, tables, conds);
        tablemap.put(name, t);
        return "";
    }

    private String loadTable(List<String> sentence) throws IOException {
        assertIntEqual(2, sentence.size());
        String name = sentence.get(1);
        assertSqlType(name, SqlType.Name);
        String path = name + ".tbl";
        BufferedReader in = new BufferedReader(new FileReader(path));
        String line;
        boolean first = true;
        List<List<String>> args;
        Table t = new Table();
        while ((line = in.readLine()) != null) {
            if (first) {
                first = false;
                args = splitSentence(toSentence(line), ",");
                for (List<String> arg : args) {
                    assertIntEqual(2, arg.size());
                    assertSqlType(arg.get(0), SqlType.Name);
                    assertEqualsOneOf(arg.get(1), "int", "float", "string");
                    t.addColumn(new Column(arg.get(0), arg.get(1)));
                }
            } else {
                args = splitSentence(toSentence(line), ",");
                List<SQLValue> row = new ArrayList<>();
                for (int i = 0; i < args.size(); i += 1) {
                    assertIntEqual(1, args.get(i).size());
                    row.add(SQLValue.value(args.get(i).get(0), t.getColumn(i).type, true));
                }
                t.addRow(row);
            }
        }
        tablemap.put(name, t);
        return "";
    }

    private String storeTable(String name) throws IOException {
        String path = name + ".tbl";
        Table t = getTable(name);
        BufferedWriter out = new BufferedWriter(new FileWriter(path));
        out.write(t.toString());
        out.flush();
        return "";
    }


    //-----------Some tools for checking input and throw errors-------------//

    private String dropTable(String name) {
        if (!tablemap.containsKey(name)) {
            throw new RuntimeException("Do not have table:" + name);
        }
        tablemap.remove(name);
        return "";
    }

    private String insertRow(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        if (!m.matches()) {
            throw new RuntimeException("Malformed insert: " + expr);
        }
        String name = m.group(1);
        String values = m.group(2);
        Table t = getTable(name);
        List<String> args = toSentence(values);
        while (args.contains(",")) {
            args.remove(",");
        }
        List<SQLValue> row = new ArrayList<>();
        for (int i = 0; i < args.size(); i += 1) {
            row.add(SQLValue.value(args.get(i), t.getColumn(i).type, true));
        }
        t.addRow(row);
        return "";
    }

    private String printTable(List<String> sentence) {
        assertIntEqual(2, sentence.size());
        String name = sentence.get(1);
        assertSqlType(name, SqlType.Name);
        if (!tablemap.containsKey(name)) {
            throw new RuntimeException("No such a table: " + name);
        }
        return tablemap.get(name).toString();
    }

    private Table select(String expr) {
        Matcher m = SELECT_CLS.matcher(expr);
        Table t;
        if (!m.matches()) {
            throw new RuntimeException("Malformed select:" + expr);
        }
        t = select(m.group(1), m.group(2), m.group(3));
        return t;
    }

    private Table select(String exprs, String tables, String conds) {
        List<String> sexprs = toSentence(exprs);
        List<String> stables = toSentence(tables);
        while (stables.contains(",")) {
            stables.remove(",");
        }
        if (conds == null) {
            conds = "";
        }
        List<String> sconds = toSentence(conds);
        Table source = tablemap.get(stables.get(0)), sub;
        if (stables.size() > 1) {
            for (int i = 1; i < stables.size(); i++) {
                source = join(source, tablemap.get(stables.get(i)));
            }
        }
        List<String> wantedCols = desiredColNames(sexprs, source.getPartialTitles());
        sub = source.subTable(sexprs);
        source = Table.addTable(sub, source);
        source = source.conditionTable(sconds);
        return source.selectColumns(wantedCols);
    }

    private List<String> desiredColNames(List<String> sexprs, List<String> oldTitle) {
        if (sexprs.get(0).equals("*")) {
            return oldTitle;
        } else {
            List<String> s = new ArrayList<>();
            List<List<String>> ssexp;
            ssexp = splitSentence(sexprs, ",");
            for (List<String> exp : ssexp) {
                if (exp.size() > 0) {
                    s.add(exp.get(exp.size() - 1));
                }
            }
            return s;
        }
    }

    private List<String> commonTitles(Table t1, Table t2) {
        List<String> title1 = t1.getPartialTitles();
        List<String> title2 = t2.getPartialTitles();
        List<String> common = new ArrayList<>();
        for (String i : title1) {
            for (String j : title2) {
                if (i.equals(j)) {
                    common.add(i);
                }
            }
        }
        return common;
    }

    private static List<String> addUnique(List<String> s1, List<String> s2) {
        List<String> ret = new ArrayList<>();
        for (String s : s1) {
            ret.add(s);
        }
        for (int i = 0; i < s2.size(); i++) {
            if (!ret.contains(s2.get(i))) {
                ret.add(s2.get(i));
            }
        }
        return ret;
    }

    private List<String> newTitles(Table t1, Table t2) {
        List<String> title1 = t1.getPartialTitles();
        List<String> title2 = t2.getPartialTitles();
        List<String> common = commonTitles(t1, t2);
        List<String> newTitle = addUnique(common, title1);
        newTitle = addUnique(newTitle, title2);
        return newTitle;
    }

    private Table join(Table t1, Table t2) {
        List<String> commonTitle = commonTitles(t1, t2);
        List<String> newtitle = newTitles(t1, t2);
        Table joinTable = new Table();
        for (int i = 0; i < newtitle.size(); i += 1) {
            String tempname = newtitle.get(i);
            String type;
            if (t1.containsColumn(tempname)) {
                type = t1.getColumn(tempname).str_type;
            } else {
                type = t2.getColumn(tempname).str_type;
            }
            joinTable.addColumn(new Column(tempname, type));
        }
        for (int i = 0; i < t1.rowNum(); i++) {
            for (int j = 0; j < t2.rowNum(); j++) {
                if (canCombine(t1, i, t2, j, commonTitle)) {
                    joinTable.addRow(Table.mergeRow(t1, i, t2, j, newtitle));
                }
            }
        }
        return joinTable;
    }

    private static boolean canCombine(Table t1, int t1r, Table t2, int t2r, List<String> c) {
        SQLValue temp1, temp2;
        for (String com : c) {
            temp1 = t1.getColumn(com).get(t1r);
            temp2 = t2.getColumn(com).get(t2r);
            if (SQLValue.compare(temp1, "!=", temp2)) {
                return false;
            }
        }
        return true;
    }

    //------------------------------------------------------------------------------------------
    public enum SqlType {
        KeyWord, Operator, Literal, Name, Malformed
    }

    public enum ValueType {
        String, Int, Float, NaN, NOVALUE
    }
}
