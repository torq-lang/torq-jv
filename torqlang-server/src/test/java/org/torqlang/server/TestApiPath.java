/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestApiPath {

    @Test
    public void test01() {

        ApiPath path;

        path = ApiPath.parse("/");
        assertEquals(0, path.extractParams().size());
        assertEquals(0, path.compareSegs(List.of()));
        assertEquals(0, path.compareTo(ApiPath.parse("/")));

        path = ApiPath.parse("/x");
        assertEquals(0, path.extractParams().size());
        assertEquals(0, path.compareSegs(List.of("x")));
        assertEquals(0, path.compareSegs(List.of("{}")));
        assertEquals(0, path.compareSegs(List.of("{id}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{id}")));

        path = ApiPath.parse("/{}");
        assertEquals(1, path.extractParams().size());
        assertEquals(0, path.extractParams().get(0).pos());
        assertEquals("", path.extractParams().get(0).name());
        assertEquals(0, path.compareSegs(List.of("x")));
        assertEquals(0, path.compareSegs(List.of("{}")));
        assertEquals(0, path.compareSegs(List.of("{id}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{id}")));

        path = ApiPath.parse("/x/y");
        assertEquals(0, path.extractParams().size());
        assertEquals(0, path.compareSegs(List.of("x", "y")));
        assertEquals(0, path.compareSegs(List.of("{}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{}")));
        assertEquals(0, path.compareSegs(List.of("{id}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{id}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{id}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{id}")));

        path = ApiPath.parse("/{foo}/y");
        assertEquals(1, path.extractParams().size());
        assertEquals(0, path.extractParams().get(0).pos());
        assertEquals("foo", path.extractParams().get(0).name());
        assertEquals(0, path.compareSegs(List.of("x", "y")));
        assertEquals(0, path.compareSegs(List.of("{}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{}")));
        assertEquals(0, path.compareSegs(List.of("{id}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{id}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{id}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{id}")));

        path = ApiPath.parse("/x/{bar}");
        assertEquals(1, path.extractParams().size());
        assertEquals(1, path.extractParams().get(0).pos());
        assertEquals("bar", path.extractParams().get(0).name());
        assertEquals(0, path.compareSegs(List.of("x", "y")));
        assertEquals(0, path.compareSegs(List.of("{}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{}")));
        assertEquals(0, path.compareSegs(List.of("{id}", "y")));
        assertEquals(0, path.compareSegs(List.of("x", "{id}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{}")));
        assertEquals(0, path.compareTo(ApiPath.parse("/{id}/y")));
        assertEquals(0, path.compareTo(ApiPath.parse("/x/{id}")));
    }

}
