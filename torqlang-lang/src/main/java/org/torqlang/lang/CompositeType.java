/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

/*
 * Composites as either "structures" or "objects". Structures expose data and objects hide data. Structures provide
 * unification and entailment, objects provide methods and equals.
 *
 * Composite -- define label and fields (feature-value pairs) as a type with selection
 *     Obj -- no unification, requires equals and hash_code, and can have hidden state
 *     Struct -- support unification and entailment with no hidden state
 *         Array -- no label, Int32 features starting at 0, undetermined size, all values are of one type
 *         Rec -- label, mixed features, determined size, values can be of different types
 *         Tuple -- label, Int32 features starting at 0, determined size, values can be of different types
 */
public interface CompositeType extends Type {
}
