/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

public interface MonoType extends PolyType {

    /*
     * Contains is inclusive. Therefore, return true if this type is equal to or contains other.
     */
    boolean contains(MonoType other);

    /*
     * Substitute exiting type variables with the given type substitutions.
     */
    @Override
    MonoType subst(TypeSubst subst);
}
