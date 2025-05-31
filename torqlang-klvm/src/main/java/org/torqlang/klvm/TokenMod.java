/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.List;

public final class TokenMod implements KernelModule {

    public static final Str TOKEN_STR = Str.of("Token");
    public static final Ident TOKEN_IDENT = Ident.create(TOKEN_STR.value);

    private final Complete namesake;
    private final CompleteRec exports;

    private TokenMod() {
        namesake = new TokenCls();
        exports = Rec.completeRecBuilder()
            .addField(Str.of(TOKEN_IDENT.name), namesake)
            .build();
    }

    public static TokenMod singleton() {
        return LazySingleton.SINGLETON;
    }

    // Signatures:
    //     new Token -> Token
    static void clsNew(List<CompleteOrIdent> ys, Env env, Machine machine) throws WaitException {
        final int expectedArgCount = 1;
        if (ys.size() != expectedArgCount) {
            throw new InvalidArgCountError(expectedArgCount, ys, "Token.new");
        }
        Token token = new Token();
        ValueOrVar target = ys.get(0).resolveValueOrVar(env);
        target.bindToValue(token, null);
    }

    @Override
    public final CompleteRec exports() {
        return exports;
    }

    @Override
    public final Complete namesake() {
        return namesake;
    }

    private static final class LazySingleton {
        private static final TokenMod SINGLETON = new TokenMod();
    }

    static final class TokenCls implements CompleteObj {

        private static final CompleteProc TOKEN_CLS_NEW = TokenMod::clsNew;

        private TokenCls() {
        }

        @Override
        public final Value select(Feature feature) {
            if (feature.equals(CommonFeatures.$NEW)) {
                return TOKEN_CLS_NEW;
            }
            throw new FeatureNotFoundError(this, feature);
        }

        @Override
        public final String toString() {
            return toKernelString();
        }
    }

}
