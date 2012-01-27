package net.dhleong.corgen.gran;

public class ClassLevel extends AbsGranLevel<MethodLevel> {

    public ClassLevel(String packageName, String name) {
        super(packageName, name);
    }

    @Override
    public int getLevel() {
        return CLASS;
    }

    @Override
    protected MethodLevel generateItem(String name) {
        return new MethodLevel(getLevelName(0), getName(), name);
    }

}
