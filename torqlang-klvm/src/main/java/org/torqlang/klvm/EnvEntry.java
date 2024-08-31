/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

@SuppressWarnings("ClassCanBeRecord")
public class EnvEntry {

    public final Ident ident;
    public final Var var;

    public EnvEntry(Ident ident, Var var) {
        if (ident == null) {
            throw new NullPointerException("Ident is null");
        }
        if (var == null) {
            throw new NullPointerException("Ident <" + ident + "> mapped to null Var");
        }
        this.ident = ident;
        this.var = var;
    }

}
