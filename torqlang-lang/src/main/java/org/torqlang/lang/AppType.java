/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.List;

/*
 * An applied type constructor. Also known as an instantiated type.
 */
public interface AppType extends MonoType {

    /*
     * The name of the type constructor, such as 'Int32' or 'Func'.
     */
    @Override
    String name();

    /*
     * The type parameters that instantiated this type.
     */
    List<MonoType> params();
}
