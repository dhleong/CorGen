package net.dhleong.corgen.gran;

public class MethodLevel extends AbsGranLevel<Word> {

    public MethodLevel(String packageName, String className, String methodName) {
        super(packageName, className, methodName);
    }

    @Override
    public int getLevel() {
        return METHOD;
    }

    @Override
    protected Word generateItem(String name) {
        assert false; // this should never get called
        return null;
    }

}
