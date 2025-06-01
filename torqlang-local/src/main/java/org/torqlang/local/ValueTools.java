/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ValueTools {

    public static Object toNativeValue(Complete value) {
        return value.toNativeValue();
    }

    public static Complete toKernelValue(Object value) {
        return toKernelValue(value, null);
    }

    public static Complete toKernelValue(Object value, Type type) {
        if (value == null || value == JsonNull.SINGLETON) {
            return Null.SINGLETON;
        }
        if (value instanceof Complete) {
            return (Complete) value;
        }
        if (value instanceof String string) {
            if (type == null) {
                if (Eof.NATIVE_VALUE.equals(string)) {
                    return Eof.SINGLETON;
                } else {
                    return Str.of(string);
                }
            } else if (type instanceof StrType) {
                return Str.of(string);
            } else if (type instanceof Int32Type) {
                return Int32.decode(string);
            } else if (type instanceof Int64Type) {
                return Int64.decode(string);
            } else if (type instanceof Dec128Type) {
                return Dec128.decode(string);
            } else if (type instanceof EofType && string.equals(Eof.NATIVE_VALUE)) {
                return Eof.SINGLETON;
            } else if (type instanceof ValueTool valueTool) {
                return valueTool.toKernelValue(value);
            }
            throw new IllegalArgumentException("Cannot convert String to " + type);
        }
        if (value instanceof Boolean booleanValue) {
            if (type != null && type != BoolType.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert Boolean to " + type);
            }
            return Bool.of(booleanValue);
        }
        if (value instanceof Character characterValue) {
            if (type != null && type != CharType.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert Character to " + type);
            }
            return Char.of(characterValue);
        }
        if (value instanceof Integer integer) {
            if (type != null && type != Int32Type.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert Integer to " + type);
            }
            return Int32.of(integer);
        }
        if (value instanceof Long longValue) {
            if (type instanceof Int32Type) {
                return Int32.of(longValue.intValue());
            } else if (type == null || type instanceof Int64Type) {
                return Int64.of(longValue.intValue());
            } else {
                throw new IllegalArgumentException("Cannot convert Long to " + type);
            }
        }
        if (value instanceof Float floatValue) {
            if (type != null && type != Flt32Type.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert Float to " + type);
            }
            return Flt32.of(floatValue);
        }
        if (value instanceof Double doubleValue) {
            if (type != null && type != Flt64Type.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert Double to " + type);
            }
            return Flt64.of(doubleValue);
        }
        if (value instanceof BigDecimal bigDecimal) {
            if (type != null && type != Dec128Type.SINGLETON) {
                throw new IllegalArgumentException("Cannot convert BigDecimal to " + type);
            }
            return Dec128.of(bigDecimal);
        }
        if (value instanceof Map<?, ?> map) {
            return toKernelStruct(map, type);
        }
        if (value instanceof List<?> list) {
            return toKernelTuple(null, list, type);
        }
        throw new IllegalArgumentException("Cannot convert to kernel value: " + value);
    }

    /*
     * Map the following to an Array, Rec, or Tuple
     *     {
     *         "$LABEL": <<label>>,
     *         "$FIELDS": <<fields>>
     *     }
     */
    private static Complete toKernelStruct(Map<?, ?> map, Type type) {
        if (map.size() == 2) {
            Object labelFound = map.get(Rec.$LABEL);
            if (labelFound != null) {
                Object valueFound = map.get(Rec.$FIELDS);
                if (valueFound != null) {
                    if (valueFound instanceof Map<?, ?> mapContent) {
                        return toKernelRec(labelFound, mapContent, type);
                    } else if (valueFound instanceof List<?> listContent) {
                        return toKernelTuple(labelFound, listContent, type);
                    } else {
                        throw new IllegalArgumentException("A label must precede a structure");
                    }
                }
            }
        }
        return toKernelRec(null, map, type);
    }

    private static CompleteRec toKernelRec(Object label, Map<?, ?> fields, Type type) {
        RecTypeExpr recTypeExpr;
        if (type == null) {
            recTypeExpr = null;
        } else if (type instanceof RecTypeExpr recTypeExprFound) {
            recTypeExpr = recTypeExprFound;
        } else if (type instanceof RecType) {
            recTypeExpr = null;
        } else {
            throw new IllegalArgumentException("Cannot convert to kernel value: " + fields);
        }
        Literal kernelLabel;
        if (label != null && recTypeExpr != null) {
            kernelLabel = (Literal) toKernelValue(label, recTypeExpr.label);
        } else {
            kernelLabel = (Literal) toKernelValue(label, null);
        }
        List<CompleteField> kernelFields = new ArrayList<>();
        for (Map.Entry<?, ?> e : fields.entrySet()) {
            // `Long` features are automatically downcast to `Integer` features
            Object key = e.getKey();
            if (key.getClass() == Long.class) {
                key = ((Long) key).intValue();
            }
            Complete completeKey = toKernelValue(key, null);
            if (!(completeKey instanceof Feature kernelFeature)) {
                throw new IllegalArgumentException("Map key must be a Feature: " + key);
            }
            FeatureAsType kernelFeatureAsType = FeatureAsType.create(kernelFeature);
            Type valueType = recTypeExpr != null ? recTypeExpr.findValue(kernelFeatureAsType) : null;
            Complete kernelValue = toKernelValue(e.getValue(), valueType);
            kernelFields.add(new CompleteField(kernelFeature, kernelValue));
        }
        return CompleteRec.create(kernelLabel, kernelFields);
    }

    private static CompleteTuple toKernelTuple(Object label, List<?> values, Type type) {
        Type arrayElementType = null;
        TupleTypeExpr tupleTypeExpr = null;
        if (type != null) {
            if (type instanceof TypeApply typeApply) {
                if (typeApply.name.ident().equals(ArrayType.IDENT)) {
                    arrayElementType = typeApply.typeArgs.get(0);
                } else {
                    throw new IllegalArgumentException("Cannot convert to kernel value: " + values);
                }
            } else if (type instanceof TupleTypeExpr tupleTypeExprFound) {
                tupleTypeExpr = tupleTypeExprFound;
            } else if (!(type instanceof ArrayType)) {
                throw new IllegalArgumentException("Cannot convert to kernel value: " + values);
            }
        }
        Literal kernelLabel;
        if (label != null && tupleTypeExpr != null) {
            kernelLabel = (Literal) toKernelValue(label, tupleTypeExpr.label);
        } else {
            kernelLabel = (Literal) toKernelValue(label, null);
        }
        List<Complete> kernelValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Object e = values.get(i);
            if (type == null) {
                kernelValues.add(toKernelValue(e, null));
            } else if (tupleTypeExpr != null) {
                Type valueType = tupleTypeExpr.values.get(i);
                kernelValues.add(toKernelValue(e, valueType));
            } else {
                kernelValues.add(toKernelValue(e, arrayElementType));
            }
        }
        return CompleteTuple.create(kernelLabel, kernelValues);
    }

}
