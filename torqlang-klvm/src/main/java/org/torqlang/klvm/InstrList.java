/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InstrList implements Iterable<Instr> {

    private Entry first;
    private Entry last;
    private int size;

    public InstrList() {
    }

    public InstrList(Iterable<Instr> instrs) {
        addAll(instrs);
    }

    public final void add(Instr instr) {
        Entry e = new Entry(instr);
        e.prev = last;
        if (first == null) {
            first = e;
        }
        if (last != null) {
            last.next = e;
        }
        last = e;
        size++;
    }

    public final void addAll(Iterable<Instr> instrs) {
        for (Instr s : instrs) {
            add(s);
        }
    }

    final InstrList.Entry firstEntry() {
        return first;
    }

    @Override
    public final Iterator<Instr> iterator() {
        return new InstrIterator(first);
    }

    final InstrList.Entry lastEntry() {
        return last;
    }

    public final int size() {
        return size;
    }

    static class Entry {
        private final Instr instr;
        private Entry prev;
        private Entry next;

        private Entry(Instr instr) {
            this.instr = instr;
        }

        final Entry next() {
            return next;
        }

        final Entry prev() {
            return prev;
        }

        final Instr instr() {
            return instr;
        }
    }

    public static class InstrIterator implements Iterator<Instr> {

        private Entry next;

        InstrIterator(Entry first) {
            next = first;
        }

        @Override
        public final boolean hasNext() {
            return next != null;
        }

        @Override
        public final Instr next() {
            if (next == null) {
                throw new NoSuchElementException("Next element is not present");
            }
            Instr answer = next.instr;
            next = next.next;
            return answer;
        }
    }

}
