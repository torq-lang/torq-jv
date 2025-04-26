module org.torqlang.local {

    requires org.torqlang.lang;
    requires org.torqlang.klvm;
    requires org.torqlang.util;

    opens org.torqlang.local.torqsrc.system.lang to org.torqlang.util;
    opens org.torqlang.local.torqsrc.system.util to org.torqlang.util;

    exports org.torqlang.local;

}
