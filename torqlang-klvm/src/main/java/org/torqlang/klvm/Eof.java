/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public final class Eof implements Literal {

    public static final Eof SINGLETON = new Eof();

    public static final String EOF_NAME = "eof";
    public static final String NATIVE_VALUE = Eof.class.getName() + ".CAFE0E0F";

    private Eof() {
    }

    @Override
    public final <T, R> R accept(KernelVisitor<T, R> visitor, T state)
        throws Exception
    {
        return visitor.visitEof(this, state);
    }

    @Override
    public final String appendToString(String string) {
        return string + EOF_NAME;
    }

    @Override
    public final String formatValue() {
        return "eof";
    }

    @Override
    public final boolean isValidKey() {
        return true;
    }

    @Override
    public final String toNativeValue() {
        return NATIVE_VALUE;
    }

    @Override
    public final String toString() {
        return formatValue();
    }

}
