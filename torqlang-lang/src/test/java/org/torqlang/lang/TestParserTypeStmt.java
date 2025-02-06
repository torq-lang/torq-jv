/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;

public class TestParserTypeStmt {

    @Test
    public void test01() {
        String source = """
            begin
                import system.schema.http.HttpPost
                type Customer = {
                    'name': Str,
                    'address': Str,
                    'age': Int32
                }
                actor CustomerApi() in
                    handle ask 'POST'#{'path': path, 'body': body, ...} :: HttpPost -> Customer | Null in
                        skip
                    end
                end
            end""";
    }

}
