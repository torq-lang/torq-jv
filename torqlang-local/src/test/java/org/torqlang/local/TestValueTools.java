/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;
import org.torqlang.lang.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestValueTools {

    @Test
    public void testRecords() {

        Object value;
        Type valueType;
        Object expected;

        // Covert list of many values to a kernel tuple
        valueType = TupleTypeExpr.createWithValues(List.of(StrType.SINGLETON, Int32Type.SINGLETON, BoolType.SINGLETON));
        expected = Rec.completeTupleBuilder()
            .addValue(Str.of("zero"))
            .addValue(Int32.of(1))
            .addValue(Bool.TRUE)
            .build();
        value = ValueTools.toKernelValue(List.of("zero", 1L, true), valueType);
        assertEquals(expected, value);

        // Covert list of Long to kernel array of Int32
        valueType = TypeApply.arrayOf(Int32Type.SINGLETON);
        expected = Rec.completeTupleBuilder()
            .addValue(Int32.of(0))
            .addValue(Int32.of(1))
            .addValue(Int32.of(2))
            .build();
        value = ValueTools.toKernelValue(List.of(0L, 1L, 2L), valueType);
        assertEquals(expected, value);

        // Covert record of many values to a kernel record
        valueType = RecTypeExpr.createWithFields(List.of(
            FieldType.create(StrAsType.create(Str.of("zero")), StrType.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("one")), Int32Type.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("two")), BoolType.SINGLETON)
        ));
        expected = Rec.completeRecBuilder()
            .addField(Str.of("zero"), Str.of("zero"))
            .addField(Str.of("one"), Int32.of(1))
            .addField(Str.of("two"), Bool.TRUE)
            .build();
        value = ValueTools.toKernelValue(
            Map.of(
                "zero", "zero",
                "one", 1L,
                "two", true
            ),
            valueType
        );
        assertEquals(expected, value);

        // Covert record of many values and a nested record to a kernel record
        RecType nestedRecType = RecTypeExpr.createWithFields(List.of(
            FieldType.create(StrAsType.create(Str.of("zero")), StrType.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("one")), Int32Type.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("two")), BoolType.SINGLETON)
        ));
        valueType = RecTypeExpr.createWithFields(List.of(
            FieldType.create(StrAsType.create(Str.of("zero")), StrType.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("one")), Int32Type.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("two")), BoolType.SINGLETON),
            FieldType.create(StrAsType.create(Str.of("three")), nestedRecType)
        ));

        CompleteRec nextedExpected = Rec.completeRecBuilder()
            .addField(Str.of("zero"), Str.of("zero"))
            .addField(Str.of("one"), Int32.of(1))
            .addField(Str.of("two"), Bool.TRUE)
            .build();
        expected = Rec.completeRecBuilder()
            .addField(Str.of("zero"), Str.of("zero"))
            .addField(Str.of("one"), Int32.of(1))
            .addField(Str.of("two"), Bool.TRUE)
            .addField(Str.of("three"), nextedExpected)
            .build();
        value = ValueTools.toKernelValue(
            Map.of(
                "zero", "zero",
                "one", 1L,
                "two", true,
                "three", Map.of(
                    "zero", "zero",
                    "one", 1L,
                    "two", true
                )
            ),
            valueType
        );
        assertEquals(expected, value);

        // Covert a labeled record
        valueType = RecTypeExpr.createWithLabelAndFields(
            StrAsType.create(Str.of("my-label")),
            List.of(FieldType.create(StrAsType.create(Str.of("k")), Int32Type.SINGLETON))
        );
        expected = Rec.completeRecBuilder()
            .setLabel(Str.of("my-label"))
            .addField(Str.of("k"), Int32.of(1))
            .build();
        value = ValueTools.toKernelValue(
            Map.of(
                "$label", "my-label",
                "$fields", Map.of(
                    "k",
                    1L
                )
            ),
            valueType
        );
        assertEquals(expected, value);
    }

    @Test
    public void testScalars() {
        Object value;

        // Null

        value = ValueTools.toKernelValue(null, NullType.SINGLETON);
        assertEquals(Null.SINGLETON, value);

        value = ValueTools.toKernelValue(null, NullAsType.SINGLETON);
        assertEquals(Null.SINGLETON, value);

        value = ValueTools.toKernelValue(JsonNull.SINGLETON, NullType.SINGLETON);
        assertEquals(Null.SINGLETON, value);

        value = ValueTools.toKernelValue(JsonNull.SINGLETON, NullAsType.SINGLETON);
        assertEquals(Null.SINGLETON, value);

        // Eof

        value = ValueTools.toKernelValue(Eof.NATIVE_VALUE, EofType.SINGLETON);
        assertEquals(Eof.SINGLETON, value);

        value = ValueTools.toKernelValue(Eof.NATIVE_VALUE, EofAsType.SINGLETON);
        assertEquals(Eof.SINGLETON, value);

        // Downcast a Long to an Int32

        value = ValueTools.toKernelValue(3L, Int32Type.SINGLETON);
        assertEquals(Int32.of(3), value);

        value = ValueTools.toKernelValue(3L, Int32AsType.SINGLETON);
        assertEquals(Int32.of(3), value);

        // Convert a string to a Dec128

        value = ValueTools.toKernelValue("123456789012345.12345", Dec128Type.SINGLETON);
        assertEquals(Dec128.of("123456789012345.12345"), value);

        value = ValueTools.toKernelValue("123456789012345.12345", Dec128AsType.SINGLETON);
        assertEquals(Dec128.of("123456789012345.12345"), value);

        // Convert a string to a LocalDate

        value = ValueTools.toKernelValue("2024-11-03", LocalDateMod.localDateType());
        assertEquals(LocalDateMod.createObj(LocalDate.parse("2024-11-03")), value);

        // Convert a string to a LocalDate while ignoring the time

        value = ValueTools.toKernelValue("2024-11-03T08:10:30Z", LocalDateMod.localDateType());
        assertEquals(LocalDateMod.createObj(LocalDate.parse("2024-11-03")), value);
    }

    @Test
    public void testUntyped() {

        Object value;

        // Null

        value = ValueTools.toKernelValue(null);
        assertEquals(Null.SINGLETON, value);
        value = ValueTools.toNativeValue((Complete) value);
        assertNull(value);

        value = ValueTools.toKernelValue(JsonNull.SINGLETON);
        assertEquals(Null.SINGLETON, value);
        value = ValueTools.toNativeValue((Complete) value);
        assertNull(value);

        // Eof

        value = ValueTools.toKernelValue(Eof.NATIVE_VALUE);
        assertEquals(Eof.SINGLETON, value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Eof.NATIVE_VALUE, value);

        // Boolean

        value = ValueTools.toKernelValue(Boolean.TRUE);
        assertEquals(Bool.TRUE, value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Boolean.TRUE, value);

        value = ValueTools.toKernelValue(Boolean.FALSE);
        assertEquals(Bool.FALSE, value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Boolean.FALSE, value);

        // Character and String

        value = ValueTools.toKernelValue('a');
        assertEquals(Char.of('a'), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals('a', value);

        value = ValueTools.toKernelValue("abc");
        assertEquals(Str.of("abc"), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals("abc", value.toString());

        // Numbers

        value = ValueTools.toKernelValue(3);
        assertEquals(Int32.of(3), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(3, value);

        value = ValueTools.toKernelValue(3L);
        assertEquals(Int64.of(3), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(3L, value);

        value = ValueTools.toKernelValue(3.0f);
        assertEquals(Flt32.of(3), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(3.0f, value);

        value = ValueTools.toKernelValue(3.0);
        assertEquals(Flt64.of(3), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(3.0, value);

        value = ValueTools.toKernelValue(new BigDecimal("3.0"));
        assertEquals(Dec128.of(3), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(new BigDecimal("3.0"), value);

        // Records

        value = ValueTools.toKernelValue(List.of());
        assertEquals(Rec.completeTupleBuilder().build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(List.of(), value);

        value = ValueTools.toKernelValue(List.of(1));
        assertEquals(Rec.completeTupleBuilder().addValue(Int32.of(1)).build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(List.of(1), value);

        value = ValueTools.toKernelValue(Map.of(Rec.$LABEL, "x", Rec.$FIELDS, List.of(1)));
        assertEquals(Rec.completeTupleBuilder().setLabel(Str.of("x")).addValue(Int32.of(1)).build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Map.of(Rec.$LABEL, "x", Rec.$FIELDS, List.of(1)), value);

        value = ValueTools.toKernelValue(Map.of());
        assertEquals(Rec.completeRecBuilder().build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Map.of(), value);

        value = ValueTools.toKernelValue(Map.of("k", 1));
        assertEquals(Rec.completeRecBuilder().addField(Str.of("k"), Int32.of(1)).build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Map.of("k", 1), value);

        value = ValueTools.toKernelValue(Map.of(Rec.$LABEL, "x", Rec.$FIELDS, Map.of("k", 1)));
        assertEquals(Rec.completeRecBuilder().setLabel(Str.of("x")).addField(Str.of("k"), Int32.of(1)).build(), value);
        value = ValueTools.toNativeValue((Complete) value);
        assertEquals(Map.of(Rec.$LABEL, "x", Rec.$FIELDS, Map.of("k", 1)), value);
    }

}
