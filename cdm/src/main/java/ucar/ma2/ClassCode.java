/*
 * Copyright 1998-2015 University Corporation for Atmospheric Research/Unidata
 *  See the LICENSE file for more information.
 */


package ucar.ma2;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static ucar.ma2.Kinds.*;

abstract class Kinds
{
    static final protected int ISINT      = 1;
    static final protected int ISFLOAT    = 2;
    static final protected int ISNUMERIC  = 4;
}

/**
 * Support ability to do a switch over a limited set of classes
 */


public enum ClassCode
{
    // Use leading underscore to avoid name conflicts
    _boolean(boolean.class, false, 0),
    _char(char.class, false, 0),
    _byte(byte.class, false, ISINT | ISNUMERIC),
    _short(short.class, false, ISINT | ISNUMERIC),
    _int(int.class, false, ISINT | ISNUMERIC),
    _long(long.class, false, ISINT | ISNUMERIC),
    _float(float.class, false, ISFLOAT | ISNUMERIC),
    _double(double.class, false, ISFLOAT | ISNUMERIC),
    _Boolean(Boolean.class, true, 0),
    _Character(Character.class, true, 0),
    _Byte(Byte.class, true, ISINT | ISNUMERIC),
    _Short(Short.class, true, ISINT | ISNUMERIC),
    _Integer(Integer.class, true, ISINT | ISNUMERIC),
    _Long(Long.class, true, ISINT | ISNUMERIC),
    _Float(Float.class, true, ISFLOAT | ISNUMERIC),
    _Double(Double.class, true, ISFLOAT | ISNUMERIC),
    // Following have no primitive equivalents
    _String(String.class, true, 0),
    _ByteBuffer(ByteBuffer.class, true,0);


    private Class cl;
    private int code;
    private boolean isnonprimitive;
    private int kinds;

    ClassCode(Class c, boolean isnonprim, int kinds)
    {
        this.cl = c;
        this.code = c.hashCode();
        this.isnonprimitive = isnonprim;
        this.kinds = kinds;
    }

    public Class theClass()
    {
        return this.cl;
    }

    public int theCode()
    {
        return this.code;
    }

    public boolean isInt()
    {
        return (this.kinds & ISINT) != 0;
    }

    public boolean isFloat()
    {
        return (this.kinds & ISFLOAT) != 0;
    }

    public boolean isNumeric()
    {
        return (this.kinds & ISNUMERIC) != 0;
    }


    // Inverse access maps
    static Map<Integer, ClassCode> codemap; // code -> classcode
    static Map<Class, ClassCode> classmap;  // class -> classcode
    static Map<Class, Class> nonprim; // prim|nonprim class -> non-primitive
    static Map<Class, Class> prim; // prim|nonprim class -> primitive

    static {
        classmap = new HashMap<>();
        for(ClassCode c : ClassCode.values()) {
            classmap.put(c.theClass(), c);
        }
        codemap = new HashMap<>();
        for(ClassCode c : ClassCode.values()) {
            codemap.put(c.theCode(), c);
        }
        nonprim = new HashMap<>();
        nonprim.put(boolean.class, Boolean.class);
        nonprim.put(char.class, Character.class);
        nonprim.put(byte.class, Byte.class);
        nonprim.put(short.class, Short.class);
        nonprim.put(int.class, Integer.class);
        nonprim.put(long.class, Long.class);
        nonprim.put(float.class, Float.class);
        nonprim.put(double.class, Double.class);
        nonprim.put(Boolean.class, Boolean.class);
        nonprim.put(Character.class, Character.class);
        nonprim.put(Byte.class, Byte.class);
        nonprim.put(Short.class, Short.class);
        nonprim.put(Integer.class, Integer.class);
        nonprim.put(Long.class, Long.class);
        nonprim.put(Float.class, Float.class);
        nonprim.put(Double.class, Double.class);
        prim = new HashMap<>();
        prim.put(Boolean.class, boolean.class);
        prim.put(Character.class, char.class);
        prim.put(Byte.class, byte.class);
        prim.put(Short.class, short.class);
        prim.put(Integer.class, int.class);
        prim.put(Long.class, long.class);
        prim.put(Float.class, float.class);
        prim.put(Double.class, double.class);
        prim.put(boolean.class, boolean.class);
        prim.put(char.class, char.class);
        prim.put(byte.class, byte.class);
        prim.put(short.class, short.class);
        prim.put(int.class, int.class);
        prim.put(long.class, long.class);
        prim.put(float.class, float.class);
        prim.put(double.class, double.class);
    }

    // Map accessors
    static public ClassCode classcodeFor(int code)
    {
        return codemap.get((Integer) code);
    }

    static public ClassCode classcodeFor(Class c)
    {
        return classmap.get(c);
    }

    static public Class primitiveFor(Class c)
    {
        return prim.get(c);
    }

    static public Class nonprimitiveFor(Class c)
    {
        return nonprim.get(c);
    }

    // Provide some classifiers


}
