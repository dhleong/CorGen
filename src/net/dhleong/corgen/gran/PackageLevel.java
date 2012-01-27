package net.dhleong.corgen.gran;

public class PackageLevel extends AbsGranLevel<ClassLevel> {

    public PackageLevel(String name) {
        super(name);
    }

    @Override
    public int getLevel() {
        return PACKAGE;
    }

    @Override
    protected ClassLevel generateItem(String name) {
        return new ClassLevel(getName(), name);
    }

}
