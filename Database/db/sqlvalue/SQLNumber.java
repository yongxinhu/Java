package db.sqlvalue;

import db.Database;

/**
 * Created by jscjs on 2017/2/19
 */
class SQLNumber<T extends Number> extends SQLValue {
    private final T data;

    SQLNumber(T number) {
        data = number;
        type = (number instanceof Integer) ? Database.ValueType.Int : Database.ValueType.Float;
    }

    SQLNumber(SQLNumber<T> number) {
        data = number.data;
        type = number.type;
    }

    private SQLNumber(T number, Database.ValueType datatype) {
        data = number;
        type = datatype;
    }

    static SQLNumber getNaN() {
        return new SQLNumber<Integer>(0, Database.ValueType.NaN);
    }

    static SQLNumber getNoValue(Database.ValueType type) {
        if (type == Database.ValueType.Int) {
            return new SQLNumber<Integer>(0, Database.ValueType.NOVALUE);
        } else if (type == Database.ValueType.Float) {
            return new SQLNumber<Float>(new Float(0), Database.ValueType.NOVALUE);
        }
        throw new RuntimeException("SQLNumber do not accept type: " + type);
    }

    @Override
    int compareTo(SQLValue v) {
        if (v instanceof SQLString) {
            throw new RuntimeException("Bad compare between [" + this + "] and [" + v + "]");
        }
        if (this.type == Database.ValueType.NaN) {
            if (v.type == Database.ValueType.NaN) {
                return 0;
            }
            return 1;
        }
        if (v.type == Database.ValueType.NaN) {
            return -1;
        }
        float p = this.data.floatValue(), q = ((SQLNumber) v).data.floatValue();
        return Float.compare(p, q);
    }

    @Override
    public String toString() {
        if (type == Database.ValueType.NaN) {
            return "NaN";
        } else if (type == Database.ValueType.NOVALUE) {
            return "NOVALUE";
        } else if (type == Database.ValueType.Int) {
            return data.toString();
        } else {
            return String.format("%.3f", data.floatValue());
        }
    }

    @Override
    SQLNumber add(SQLValue v) {
        if (!(v instanceof SQLNumber)) {
            throw new RuntimeException("can't add between [" + this.type + "] and [" + v.type + "]");
        }
        if (this.type == Database.ValueType.NaN || v.type == Database.ValueType.NaN) {
            return SQLNumber.getNaN();
        }
        if (this.type == Database.ValueType.Int && v.type == Database.ValueType.Int) {
            return new SQLNumber<Integer>(data.intValue() + ((SQLNumber) v).data.intValue());
        }
        return new SQLNumber<Float>(data.floatValue() + ((SQLNumber) v).data.floatValue());
    }

    @Override
    SQLNumber sub(SQLValue v) {
        if (!(v instanceof SQLNumber)) {
            throw new RuntimeException("can't sub between [" + this.type + "] and [" + v.type + "]");
        }
        if (this.type == Database.ValueType.NaN || v.type == Database.ValueType.NaN) {
            return SQLNumber.getNaN();
        }
        if (this.type == Database.ValueType.Int && v.type == Database.ValueType.Int) {
            return new SQLNumber<Integer>(data.intValue() - ((SQLNumber) v).data.intValue());
        }
        return new SQLNumber<Float>(data.floatValue() - ((SQLNumber) v).data.floatValue());
    }

    @Override
    SQLNumber mul(SQLValue v) {
        if (!(v instanceof SQLNumber)) {
            throw new RuntimeException("can't mul between [" + this.type + "] and [" + v.type + "]");
        }
        if (this.type == Database.ValueType.NaN || v.type == Database.ValueType.NaN) {
            return SQLNumber.getNaN();
        }
        if (this.type == Database.ValueType.Int && v.type == Database.ValueType.Int) {
            return new SQLNumber<Integer>(data.intValue() * ((SQLNumber) v).data.intValue());
        }
        return new SQLNumber<Float>(data.floatValue() * ((SQLNumber) v).data.floatValue());
    }

    @Override
    SQLNumber div(SQLValue v) {
        if (!(v instanceof SQLNumber)) {
            throw new RuntimeException("can't div between [" + this.type + "] and [" + v.type + "]");
        }
        if (this.type == Database.ValueType.NaN || v.type == Database.ValueType.NaN) {
            return SQLNumber.getNaN();
        }
        if (this.type == Database.ValueType.Int && v.type == Database.ValueType.Int) {
            int t = ((SQLNumber) v).data.intValue();
            if (t == 0) {
                return SQLNumber.getNaN();
            }
            return new SQLNumber<Integer>(data.intValue() / t);
        }
        float f = ((SQLNumber) v).data.floatValue();
        if (f == 0) {
            return SQLNumber.getNaN();
        }
        return new SQLNumber<Float>(data.floatValue() / f);
    }

    @Override
    SQLNumber zero() {
        if (data instanceof Integer) {
            return new SQLNumber<Integer>(0);
        }
        return new SQLNumber<Float>(new Float(0));
    }
}
