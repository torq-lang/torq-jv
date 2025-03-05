/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import java.util.Set;

/*
 *  The PolyType hierarchy below corresponds to the Hindley-Milner type grammar for monotypes and polytypes.
 *
 *  - PolyType
 *      - MonoType
 *          - AppType
 *          - VarType
 *      - QuantType
 *
 *  Hindley-Milner monotypes and polytypes
 *
 *  τ = α                           [var]
 *    | C τ1 ... τn                 [app]
 *
 *  σ = τ                           [monotype]
 *    | ∀α. σ                       [quantifier]
 */
public interface PolyType {

    /*
     TODO: The following contains some additional subtypes of AppType:
           ScalarType - with a scalar name, such as 'Int32', 'Bool', etc.
               0 parameters
           FuncType - with 'Func' name
               0-n parameters
           CompType - with 'Rec' or 'Obj' name
               1-n parameters, first is label type, rest are field types
               Field types are in cardinality order
               With structural type compatibility
           FieldType - with 'Field' name
               2 parameters, a DiscreteType feature parameter and value parameter
           DiscreteType - with 'Discrete' name
               1 parameter that is an Int32 or Lit value
           UnionType - with 'Union' name
               Can contain 1 or more of any other type
               1-n parameters for each sum type
     */

    String FOR_ALL_QUANT = "∀";
    String LOWER_ALPHA = "α";
    String LOWER_BETA = "β";
    String LOWER_GAMMA = "γ";
    String LOWER_DELTA = "δ";
    String LOWER_EPSILON = "ε";
    String LOWER_LAMBDA = "λ";
    String LOWER_SIGMA = "σ";
    String LOWER_TAU = "τ";

    PolyType addQuantifiers(Set<VarType> freeVars);

    void captureFreeVars(Set<VarType> freeVars);

    Set<VarType> freeVars();

    MonoType instantiate(SuffixFactory suffixFactory);

    String name();

    PolyType subst(TypeSubst subst);
}