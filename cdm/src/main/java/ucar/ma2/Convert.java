/* Copyright 2012, UCAR/Unidata.
   See the LICENSE file for more information.
*/

package ucar.ma2;


import ucar.nc2.EnumTypedef;
import ucar.units.ConversionException;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * This Class will over time, collect a number
 * of conversion routines for cdm types and values.
 * Singleton
 */

abstract public class Convert
{
    //////////////////////////////////////////////////
    // Constants

    // Big integer representation of 0xFFFFFFFFFFFFFFFF
    static final BigInteger LONGMASK = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    //////////////////////////////////////////////////
    // Static Methods

    /* Needed to implement Array.getElement() */
    static public Class
    elementClass(DataType dt)
    {
        switch (dt) {
        case BOOLEAN:
            return boolean.class;
        case ENUM1:
        case BYTE:
            return byte.class;
        case CHAR:
            return char.class;
        case ENUM2:
        case SHORT:
            return short.class;
        case ENUM4:
        case INT:
            return int.class;
        case LONG:
            return long.class;
        case FLOAT:
            return float.class;
        case DOUBLE:
            return double.class;
        case STRING:
            return String.class;
        case OPAQUE:
            return ByteBuffer.class;
        case UBYTE:
            return byte.class;
        case USHORT:
            return short.class;
        case UINT:
            return int.class;
        case ULONG:
            return long.class;
        default:
            break;
        }
        return null;
    }

    static public Object
    createVector(DataType type, long count)
    {
        int icount = (int) count;
        Object vector = null;
        switch (type) {
        case BOOLEAN:
            vector = new boolean[icount];
            break;
        case CHAR:
            vector = new char[icount];
            break;
        case ENUM1:
        case UBYTE:
        case BYTE:
            vector = new byte[icount];
            break;
        case ENUM2:
            ;
        case SHORT:
        case USHORT:
            vector = new short[icount];
            break;
        case ENUM4:
        case INT:
        case UINT:
            vector = new int[icount];
            break;
        case LONG:
        case ULONG:
            vector = new long[icount];
            break;
        case FLOAT:
            vector = new float[icount];
            break;
        case DOUBLE:
            vector = new double[icount];
            break;
        case STRING:
            vector = new String[icount];
            break;
        case OPAQUE:
            vector = new ByteBuffer[icount];
            break;
        default:
            throw new ForbiddenConversionException();
        }
        return vector;
    }


    static public void
    vectorcopy(DataType datatype, Object src, Object dst, long srcoffset, long dstoffset)
            throws ConversionException
    {
        switch (datatype) {
	case BOOLEAN:
            ((boolean[]) dst)[(int) dstoffset] = ((boolean[]) src)[(int) srcoffset];
	    break;
        case UBYTE:
        case BYTE:
            ((byte[]) dst)[(int) dstoffset] = ((byte[]) src)[(int) srcoffset];
            break;
        case CHAR:
            ((char[]) dst)[(int) dstoffset] = ((char[]) src)[(int) srcoffset];
            break;
        case USHORT:
        case SHORT:
            ((short[]) dst)[(int) dstoffset] = ((short[]) src)[(int) srcoffset];
            break;
        case UINT:
        case INT:
            ((int[]) dst)[(int) dstoffset] = ((int[]) src)[(int) srcoffset];
            break;
        case ULONG:
        case LONG:
            ((long[]) dst)[(int) dstoffset] = ((long[]) src)[(int) srcoffset];
            break;
        case FLOAT:
            ((float[]) dst)[(int) dstoffset] = ((float[]) src)[(int) srcoffset];
            break;
        case DOUBLE:
            ((double[]) dst)[(int) dstoffset] = ((double[]) src)[(int) srcoffset];
            break;
        case OPAQUE:
            // Sigh, bytebuffer hidden by CDM
            Object o = ((Object[]) src)[(int) srcoffset];
            ((ByteBuffer[]) dst)[(int) dstoffset] = (ByteBuffer) o;
            break;
        case STRING:
            // Sigh, String hidden by CDM
            o = ((Object[]) src)[(int) srcoffset];
            ((String[]) dst)[(int) dstoffset] = (String) o;
            break;
        case ENUM1:
            vectorcopy(DataType.BYTE, src, dst, srcoffset, dstoffset);
            break;
        case ENUM2:
            vectorcopy(DataType.SHORT, src, dst, srcoffset, dstoffset);
            break;
        case ENUM4:
            vectorcopy(DataType.INT, src, dst, srcoffset, dstoffset);
            break;
        default:
            throw new ForbiddenConversionException("Attempt to read non-atomic value of type: " + datatype);
        }
    }

    static public Object
    convert(DataType cdmtype, EnumTypedef en, Object o)
            throws ForbiddenConversionException
    {
        if(en != null) {
            switch (cdmtype) {
            case ENUM1:
            case ENUM2:
            case ENUM4:
                if(!(o instanceof Integer))
                    throw new ForbiddenConversionException(o.toString());
                int eval = (Integer) o;
                String econst = en.lookupEnumString(eval);
                if(econst == null)
                    throw new ForbiddenConversionException(o.toString());
                return econst;
            default:
                throw new ForbiddenConversionException(o.toString());
            }
        } else if(cdmtype == DataType.STRING) {
            return o.toString();
        } else if(o instanceof Long) {
            long lval = (Long) o;
            switch (cdmtype) {
            case BOOLEAN:
                return (lval == 0 ? Boolean.TRUE : Boolean.FALSE);
            case BYTE:
                return (byte) (lval);
            case SHORT:
                return (short) (lval);
            case INT:
                return (int) (lval);
            case LONG:
                return lval;
            case UBYTE:
                return (byte) (lval & 0xFF);
            case USHORT:
                return (short) (lval & 0xFFFF);
            case UINT:
                return (short) (lval & 0xFFFFFFFF);
            case ULONG:
                return lval;
            case STRING:
                return ((Long) o).toString();
            default:
                throw new ForbiddenConversionException(o.toString());
            }
        } else if(o instanceof Float || o instanceof Double || o instanceof Character)
            return o;
        else if(cdmtype == DataType.OPAQUE) {
            assert o instanceof ByteBuffer;
            return o;
        }
        return o;
    }

    /**
     * convert a string to a specified cdmtype
     * Note that if en is defined, then we attempt
     * to convert the string as enum const
     *
     * @param cdmtype
     * @param en
     * @param o
     * @return
     */
    static public Object
    attributeParse(DataType cdmtype, EnumTypedef en, Object o)
    {
        String so = o.toString();
        if(en != null) {
            switch (cdmtype) {
            case ENUM1:
            case ENUM2:
            case ENUM4:
                if(!(o instanceof Integer))
                    throw new ForbiddenConversionException(o.toString());
                int eval = (Integer) o;
                String econst = en.lookupEnumString(eval);
                if(econst == null)
                    throw new ForbiddenConversionException(o.toString());
                return econst;
            default:
                throw new ForbiddenConversionException(o.toString());
            }
        }
        long lval = 0;
        double dval = 0.0;
        boolean islong = true;
        boolean isdouble = true;
        // Do a quick conversion checks
        try {
            lval = Long.parseLong(so);
        } catch (NumberFormatException nfe) {
            islong = false;
        }
        try {
            dval = Double.parseDouble(so);
        } catch (NumberFormatException nfe) {
            isdouble = false;
        }
        o = null; // default is not convertible
        switch (cdmtype) {
        case BOOLEAN:
            if(so.equalsIgnoreCase("false")
                || (islong && lval == 0))
                o = Boolean.FALSE;
            else
                o = Boolean.TRUE;
            break;
        case BYTE:
            if(islong) o = Byte.valueOf((byte)lval);
            break;
        case SHORT:
            if(islong) o = Short.valueOf((short)lval);
            break;
        case INT:
            if(islong) o = Integer.valueOf((int)lval);
            break;
        case LONG:
            if(islong) o = Long.valueOf(lval);
            break;
        case UBYTE:  // Keep the proper bit pattern
            if(islong) o = Byte.valueOf((byte)(lval & 0xFFL));
            break;
        case USHORT:
            if(islong) o = Short.valueOf((short)(lval & 0xFFFFL));
            break;
        case UINT:
            if(islong) o = Integer.valueOf((int)(lval & 0xFFFFFFFFL));
            break;
        case ULONG:  //Need to resort to BigInteger
            BigInteger bi = new BigInteger(so);
            bi = bi.and(LONGMASK);
            o = (Long)bi.longValue();
            break;
        case FLOAT:
            if(islong && !isdouble) {dval = (double)lval; isdouble = true;}
            if(isdouble) o = (Float)((float)dval);
            break;
        case DOUBLE:
            if(islong && !isdouble) {dval = (double)lval; isdouble = true;}
            if(isdouble) o = (Double)(dval);
            break;
        case STRING:
            return so;
        case OPAQUE:  // Big Integer then ByteBuffer
            if(so.startsWith("0x") || so.startsWith("0X"))
                so = so.substring(2);
            bi = new BigInteger(so,16);
            // Now extract bytes
            byte[] bb = bi.toByteArray();
            o = ByteBuffer.wrap(bb);
            break;
        default:
            throw new ForbiddenConversionException(o.toString());
        }
        if(o == null)
            throw new ForbiddenConversionException(o.toString());
        return o;
    }

    static public boolean
    isPrimitiveVector(DataType type, Object o)
    {
        Class c = o.getClass();
        if(!c.isArray())
            return false;
        // cannot use isAssignableFrom, I think because primitive
        switch (type) {
        case BOOLEAN:
            return o instanceof boolean[];
        case CHAR:
            return o instanceof char[];
        case ENUM1:
        case BYTE:
        case UBYTE:
            return o instanceof byte[];
        case ENUM2:
        case SHORT:
        case USHORT:
            return o instanceof short[];
        case ENUM4:
        case INT:
        case UINT:
            return o instanceof int[];
        case LONG:
        case ULONG:
            return o instanceof long[];
        case FLOAT:
            return o instanceof float[];
        case DOUBLE:
            return o instanceof double[];
        case STRING:
            return o instanceof String[];
        case OPAQUE:
            return o instanceof ByteBuffer[];
        default:
            break;
        }
        return false;
    }

    static public Array
    arrayify(DataType datatype, Object o)
    {
        // 1. o is a constant
        if(!o.getClass().isArray()) {
            Object ovec = createVector(datatype, 1);
            java.lang.reflect.Array.set(ovec, 0, o);
            o = ovec;
        }
        int[] shape = new int[]{java.lang.reflect.Array.getLength(o)};
        return Array.factory(datatype, shape, o);
    }

}
