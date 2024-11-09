/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.JsonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Callers can provide an optional value description. A mapping to the Java `null` is an unspecified mapping.
 *
 * Currently, the following scalar types can be mapped:
 *
 * - String -> Dec128
 * - String -> Int32
 * - String -> Int64
 * - String -> LocalDate
 * - Long -> Int64
 *
 * Note that mappings are not necessary for labels and features. The label types are `Str`, `Bool`, `Eof`, `Null`,
 * and `Token`. However, a `Token` cannot be reconstructed, and therefore, cannot cross process boundaries. The feature
 * types are the same as the label types with the addition of `Int32`. Features received as a `Long` are downgraded to
 * an `Int23` automatically.
 */
public final class ValueTools {

    private static final Str $REC_STR = Str.of(Rec.$REC);

    public static Object toNativeValue(Complete value) {
        return value.toNativeValue();
    }

    public static Complete toKernelValue(Object value) {
        return toKernelValue(null, value, null);
    }

    public static Complete toKernelValue(Object value, ValueDesc valueDesc) {
        return toKernelValue(null, value, valueDesc);
    }

    private static Complete toKernelValue(Object label, Object value, ValueDesc valueDesc) {
        if (value == null || value == JsonNull.SINGLETON) {
            return Null.SINGLETON;
        }
        if (value instanceof Complete) {
            return (Complete) value;
        }
        if (value instanceof String string) {
            if (Eof.NATIVE_VALUE.equals(string)) {
                return Eof.SINGLETON;
            } else if (valueDesc instanceof DateDesc) {
                LocalDate localDate;
                if (string.length() > 10) {
                    LocalDateTime localDateTime = DateTimeFormatter.ISO_DATE_TIME.parse(string, LocalDateTime::from);
                    localDate = localDateTime.toLocalDate();
                } else {
                    localDate = DateTimeFormatter.ISO_DATE.parse(string, LocalDate::from);
                }
                return LocalDatePack.newObj(localDate);
            } else if (valueDesc instanceof Int32Desc) {
                return Int32.decode(string);
            } else if (valueDesc instanceof Int64Desc) {
                return Int64.decode(string);
            } else if (valueDesc instanceof Dec128Desc) {
                return Dec128.of(string);
            } else {
                return Str.of(string);
            }
        }
        if (value instanceof Integer integer) {
            return Int32.of(integer);
        }
        if (value instanceof Long longValue) {
            if (valueDesc instanceof Int32Desc) {
                return Int32.of(longValue.intValue());
            } else {
                return Int64.of(longValue);
            }
        }
        if (value instanceof Boolean booleanValue) {
            return Bool.of(booleanValue);
        }
        if (value instanceof Character characterValue) {
            return Char.of(characterValue);
        }
        if (value instanceof Float floatValue) {
            return Flt32.of(floatValue);
        }
        if (value instanceof Double doubleValue) {
            return Flt64.of(doubleValue);
        }
        if (value instanceof BigDecimal bigDecimal) {
            return Dec128.of(bigDecimal);
        }
        if (value instanceof Map<?, ?> m) {
            // A map of size 2 may indicate a "labeled" record as a mapping of $label->value, $rec->map/list
            if (m.size() == 2) {
                Object foundLabel = m.get(Rec.$LABEL);
                // We definitely have a labeled record
                if (foundLabel != null) {
                    // If we are already within a labeled record, a new label is nonsensical
                    if (label != null) {
                        throw new IllegalArgumentException("Label cannot follow a label");
                    }
                    Object foundValue = m.get(Rec.$REC);
                    if (foundValue != null) {
                        // We have a label, so now we must have a record or tuple
                        if (!(foundValue instanceof Map) && !(foundValue instanceof List)) {
                            throw new IllegalArgumentException("Label must precede a Map or List");
                        }
                        if (valueDesc instanceof RecDesc recSpec) {
                            ValueDesc foundSpecRec = recSpec.map.get($REC_STR);
                            if (foundSpecRec != null) {
                                valueDesc = foundSpecRec;
                            }
                        }
                        return toKernelValue(foundLabel, foundValue, valueDesc);
                    }
                }
            }
            // At this point we do not have a label, or we were called recursively with a label parameter
            List<CompleteField> fs = new ArrayList<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                // `Long` features are automatically downcast to `Integer` features
                Object key = e.getKey();
                if (key.getClass() == Long.class) {
                    key = ((Long) key).intValue();
                }
                Complete k = toKernelValue(null, key, null);
                if (!(k instanceof Feature f)) {
                    throw new IllegalArgumentException("Map key must be a Feature: " + key);
                }
                ValueDesc nestedValueSpec = valueDesc != null ?
                    ((RecDesc) valueDesc).map.get(k) : null;
                Complete v = toKernelValue(null, e.getValue(), nestedValueSpec);
                fs.add(new CompleteField(f, v));
            }
            Literal kernelLabel = null;
            if (label != null) {
                // Convert the label as a value (don't pass it as the first argument)
                kernelLabel = (Literal) toKernelValue(null, label, null);
            }
            return CompleteRec.create(kernelLabel, fs);
        }
        if (value instanceof List<?> l) {
            List<ValueDesc> tupleDescs = null;
            ValueDesc arrayDesc = null;
            if (valueDesc != null) {
                if (valueDesc instanceof TupleDesc tupleDesc) {
                    tupleDescs = tupleDesc.descs();
                } else {
                    arrayDesc = ((ArrayDesc) valueDesc).componentSpec();
                }
            }
            List<Complete> es = new ArrayList<>();
            for (int i = 0; i < l.size(); i++) {
                Object e = l.get(i);
                if (valueDesc == null) {
                    es.add(toKernelValue(null, e, null));
                } else if (tupleDescs != null) {
                    ValueDesc nestedValueSpec = tupleDescs.get(i);
                    es.add(toKernelValue(null, e, nestedValueSpec));
                } else {
                    es.add(toKernelValue(null, e, arrayDesc));
                }
            }
            Literal kernelLabel = null;
            if (label != null) {
                // Convert the label as a value (don't pass it as the first argument)
                kernelLabel = (Literal) toKernelValue(null, label, null);
            }
            return CompleteTuple.create(kernelLabel, es);
        }
        throw new IllegalArgumentException("Cannot convert to kernel value: " + value);
    }

}
