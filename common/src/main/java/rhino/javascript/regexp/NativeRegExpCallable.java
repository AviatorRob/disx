package rhino.javascript.regexp;

import rhino.javascript.Context;
import rhino.javascript.Function;
import rhino.javascript.Scriptable;

/**
 * Legacy implementation of RegExp was callable, this class exists to preserve this functionality
 */
class NativeRegExpCallable extends NativeRegExp implements Function {

    NativeRegExpCallable(Scriptable scope, RECompiled compiled) {
        super(scope, compiled);
    }

    NativeRegExpCallable() {
        super();
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        return execSub(cx, scope, args, MATCH);
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        return (Scriptable) execSub(cx, scope, args, MATCH);
    }
}
