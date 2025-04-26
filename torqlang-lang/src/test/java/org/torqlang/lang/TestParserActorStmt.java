/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.junit.jupiter.api.Test;
import org.torqlang.klvm.Ident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.torqlang.lang.CommonTools.assertSourceSpan;

public class TestParserActorStmt {

    @Test
    public void test() {
        //                                      1         2         3         4         5         6         7
        //                            01234567890123456789012345678901234567890123456789012345678901234567890123456
        Parser p = new Parser("actor MyActor() in handle ask 'get' in a end handle tell 'incr' in b end end");
        StmtOrExpr sox = p.parse();
        ActorStmt actorStmt = (ActorStmt) sox;
        assertSourceSpan(actorStmt, 0, 76);
        assertEquals(Ident.create("MyActor"), actorStmt.name.ident);
        // Test format
        String expectedFormat = """
            actor MyActor() in
                handle ask 'get' in
                    a
                end
                handle tell 'incr' in
                    b
                end
            end""";
        String actualFormat = actorStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test initializer
        assertEquals(0, actorStmt.initializer().size());
        // Test handlers
        assertEquals(1, actorStmt.askHandlers().size());
        assertEquals(1, actorStmt.tellHandlers().size());
        assertSourceSpan(actorStmt.askHandlers().get(0), 19, 44);
        assertSourceSpan(actorStmt.tellHandlers().get(0), 45, 72);
    }

    @Test
    public void testWithInitializer() {
        //                                      1         2         3         4         5         6         7         8         9
        //                            0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456
        Parser p = new Parser("actor MyActor() in var x = 0 var y = 1 handle ask 'get' in a end handle tell 'incr' in b end end");
        StmtOrExpr sox = p.parse();
        ActorStmt actorStmt = (ActorStmt) sox;
        assertSourceSpan(actorStmt, 0, 96);
        assertEquals(Ident.create("MyActor"), actorStmt.name.ident);
        // Test format
        String expectedFormat = """
            actor MyActor() in
                var x = 0
                var y = 1
                handle ask 'get' in
                    a
                end
                handle tell 'incr' in
                    b
                end
            end""";
        String actualFormat = actorStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test initializer
        assertEquals(2, actorStmt.initializer().size());
        assertInstanceOf(VarStmt.class, actorStmt.initializer().get(0));
        assertInstanceOf(VarStmt.class, actorStmt.initializer().get(1));
        // Test handlers
        assertEquals(1, actorStmt.askHandlers().size());
        assertEquals(1, actorStmt.tellHandlers().size());
        assertSourceSpan(actorStmt.askHandlers().get(0), 39, 64);
        assertSourceSpan(actorStmt.tellHandlers().get(0), 65, 92);
    }

    @Test
    public void testWithRespondType() {
        //                                      1         2         3         4         5
        //                            0123456789012345678901234567890123456789012345678901234567
        Parser p = new Parser("actor MyActor() in handle ask 'get' -> Int32 in a end end");
        StmtOrExpr sox = p.parse();
        ActorStmt actorStmt = (ActorStmt) sox;
        assertSourceSpan(actorStmt, 0, 57);
        assertEquals(Ident.create("MyActor"), actorStmt.name.ident);
        // Test format
        String expectedFormat = """
            actor MyActor() in
                handle ask 'get' -> Int32 in
                    a
                end
            end""";
        String actualFormat = actorStmt.toString();
        assertEquals(expectedFormat, actualFormat);
        // Test initializer
        assertEquals(0, actorStmt.initializer().size());
        // Test handlers
        assertEquals(1, actorStmt.askHandlers().size());
        assertSourceSpan(actorStmt.askHandlers().get(0), 19, 53);
    }

}
