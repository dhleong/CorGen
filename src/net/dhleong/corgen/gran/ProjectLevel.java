package net.dhleong.corgen.gran;

/**
 * The ProjectLevel granularity is a special one that is kind
 *  of a cheat to easily contain PackageLevel granularities.
 *  
 * @author dhleong
 *
 */
public class ProjectLevel extends AbsGranLevel<PackageLevel> {

    public ProjectLevel() {
        super("PROJECT");
    }

    @Override
    public int getLevel() {
        return -1; // whatevs, highest level
    }
    
    @Override
    public String getName() {
        return "PROJECT";
    }

    @Override
    protected PackageLevel generateItem(String name) {
        return new PackageLevel(name);
    }

}
