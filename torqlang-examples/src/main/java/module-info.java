module org.torqlang.examples {

    requires org.torqlang.server;
    requires org.torqlang.local;
    requires org.torqlang.lang;
    requires org.torqlang.klvm;
    requires org.torqlang.util;

    requires org.eclipse.jetty.server;

    opens org.torqlang.examples.data.northwind to org.torqlang.util;
    opens org.torqlang.examples.torqsrc.northwind to org.torqlang.util;

    exports org.torqlang.examples;
}
