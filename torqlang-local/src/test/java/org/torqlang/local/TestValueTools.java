/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.torqlang.klvm.Rec.$LABEL;
import static org.torqlang.klvm.Rec.$REC;

public class TestValueTools {

    @Test
    public void testRecords() {

        Object v;
        ValueDesc valueDesc;
        Object expected;

        // Covert list of many values to a kernel tuple
        valueDesc = TupleDesc.of(NullDesc.BASIC, Int32Desc.BASIC, NullDesc.BASIC);
        expected = Rec.completeTupleBuilder()
            .addValue(Str.of("zero"))
            .addValue(Int32.of(1))
            .addValue(Bool.TRUE)
            .build();
        v = ValueTools.toKernelValue(List.of("zero", 1L, true), valueDesc);
        assertEquals(expected, v);

        // Covert list of Long to kernel array of Int32
        valueDesc = ArrayDesc.of(Int32Desc.BASIC);
        expected = Rec.completeTupleBuilder()
            .addValue(Int32.of(0))
            .addValue(Int32.of(1))
            .addValue(Int32.of(2))
            .build();
        v = ValueTools.toKernelValue(List.of(0L, 1L, 2L), valueDesc);
        assertEquals(expected, v);

        // Covert record of many values to a kernel record
        valueDesc = RecDesc.of(
            Str.of("zero"), StrDesc.BASIC,
            Str.of("one"), Int32Desc.BASIC,
            Str.of("two"), BoolDesc.BASIC
        );
        expected = Rec.completeRecBuilder()
            .addField(Str.of("zero"), Str.of("zero"))
            .addField(Str.of("one"), Int32.of(1))
            .addField(Str.of("two"), Bool.TRUE)
            .build();
        v = ValueTools.toKernelValue(
            Map.of(
                "zero", "zero",
                "one", 1L,
                "two", true
            ),
            valueDesc
        );
        assertEquals(expected, v);

        // Covert record of many values and a nested record to a kernel record
        RecDesc nestedValueSpec = RecDesc.of(
            Str.of("zero"), StrDesc.BASIC,
            Str.of("one"), Int32Desc.BASIC,
            Str.of("two"), BoolDesc.BASIC
        );
        valueDesc = RecDesc.of(
            Str.of("zero"), StrDesc.BASIC,
            Str.of("one"), Int32Desc.BASIC,
            Str.of("two"), BoolDesc.BASIC,
            Str.of("three"), nestedValueSpec
        );
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
        v = ValueTools.toKernelValue(
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
            valueDesc
        );
        assertEquals(expected, v);

        // Covert a labeled record

        valueDesc = RecDesc.of(
            Str.of("k"), Int32Desc.BASIC
        );
        expected = Rec.completeRecBuilder()
            .setLabel(Str.of("my-label"))
            .addField(Str.of("k"), Int32.of(1))
            .build();
        v = ValueTools.toKernelValue(
            Map.of(
                "$label", "my-label",
                "$rec", Map.of(
                    "k",
                    1L
                )
            ),
            valueDesc
        );
        assertEquals(expected, v);

        // Covert a labeled record with an explicit record structure

        valueDesc = RecDesc.of(
            Str.of($REC), RecDesc.of(
                Str.of("k"), Int32Desc.BASIC
            )
        );
        expected = Rec.completeRecBuilder()
            .setLabel(Str.of("my-label"))
            .addField(Str.of("k"), Int32.of(1))
            .build();
        v = ValueTools.toKernelValue(
            Map.of(
                $LABEL, "my-label",
                $REC, Map.of(
                    "k",
                    1L
                )
            ),
            valueDesc
        );
        assertEquals(expected, v);
    }

    @Test
    public void testScalars() {

        Object v;

        // Downcast a Long to an Int32
        v = ValueTools.toKernelValue(3L, Int32Desc.BASIC);
        assertEquals(Int32.of(3), v);

        // Convert a string to a Dec128
        v = ValueTools.toKernelValue("123456789012345.12345", Dec128Desc.BASIC);
        assertEquals(Dec128.of("123456789012345.12345"), v);

        // Convert a string to a LocalDate
        v = ValueTools.toKernelValue("2024-11-03", DateDesc.BASIC);
        assertEquals(LocalDateMod.newObj(LocalDate.parse("2024-11-03")), v);

        // Convert a string to a LocalDate while ignoring the time
        v = ValueTools.toKernelValue("2024-11-03T08:10:30Z", DateDesc.BASIC);
        assertEquals(LocalDateMod.newObj(LocalDate.parse("2024-11-03")), v);
    }

    @Test
    public void testUntyped() {

        Object v;

        // Null

        v = ValueTools.toKernelValue(null);
        assertEquals(Null.SINGLETON, v);
        v = ValueTools.toNativeValue((Complete) v);
        assertNull(v);

        // Boolean

        v = ValueTools.toKernelValue(Boolean.TRUE);
        assertEquals(Bool.TRUE, v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Boolean.TRUE, v);

        v = ValueTools.toKernelValue(Boolean.FALSE);
        assertEquals(Bool.FALSE, v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Boolean.FALSE, v);

        // Character and String

        v = ValueTools.toKernelValue('a');
        assertEquals(Char.of('a'), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals('a', v);

        v = ValueTools.toKernelValue("abc");
        assertEquals(Str.of("abc"), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals("abc", v);

        // Numbers

        v = ValueTools.toKernelValue(3);
        assertEquals(Int32.of(3), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(3, v);

        v = ValueTools.toKernelValue(3L);
        assertEquals(Int64.of(3), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(3L, v);

        v = ValueTools.toKernelValue(3.0f);
        assertEquals(Flt32.of(3), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(3.0f, v);

        v = ValueTools.toKernelValue(3.0);
        assertEquals(Flt64.of(3), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(3.0, v);

        v = ValueTools.toKernelValue(new BigDecimal("3.0"));
        assertEquals(Dec128.of(3), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(new BigDecimal("3.0"), v);

        // Records

        v = ValueTools.toKernelValue(List.of());
        assertEquals(Rec.completeTupleBuilder().build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(List.of(), v);

        v = ValueTools.toKernelValue(List.of(1));
        assertEquals(Rec.completeTupleBuilder().addValue(Int32.of(1)).build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(List.of(1), v);

        v = ValueTools.toKernelValue(Map.of(Rec.$LABEL, "x", $REC, List.of(1)));
        assertEquals(Rec.completeTupleBuilder().setLabel(Str.of("x")).addValue(Int32.of(1)).build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Map.of(Rec.$LABEL, "x", $REC, List.of(1)), v);

        v = ValueTools.toKernelValue(Map.of());
        assertEquals(Rec.completeRecBuilder().build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Map.of(), v);

        v = ValueTools.toKernelValue(Map.of("k", 1));
        assertEquals(Rec.completeRecBuilder().addField(Str.of("k"), Int32.of(1)).build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Map.of("k", 1), v);

        v = ValueTools.toKernelValue(Map.of(Rec.$LABEL, "x", $REC, Map.of("k", 1)));
        assertEquals(Rec.completeRecBuilder().setLabel(Str.of("x")).addField(Str.of("k"), Int32.of(1)).build(), v);
        v = ValueTools.toNativeValue((Complete) v);
        assertEquals(Map.of(Rec.$LABEL, "x", $REC, Map.of("k", 1)), v);
    }

}
