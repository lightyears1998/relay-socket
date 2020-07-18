package net.qfstudio.relay;

public class LogUtil {
    protected String classname;
    protected String objectID;

    public LogUtil(String classname, String objectID) {
        this.classname = classname;
        this.objectID = objectID;
    }

    public static LogUtil setupUtilFor(Object obj) {
        return new LogUtil(obj.getClass().getName(), Integer.toHexString(System.identityHashCode(obj)));
    }

    protected void log(String s) {
        System.out.println(String.format("<%s> (%s) %s", this.classname, this.objectID, s));
    }
}
