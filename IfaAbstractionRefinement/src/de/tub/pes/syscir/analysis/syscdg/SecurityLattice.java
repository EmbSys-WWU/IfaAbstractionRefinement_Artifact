package de.tub.pes.syscir.analysis.syscdg;

/**
 * This class is an implementation of a System Lattice. It includes top and bottom element.
 * 
 * @param <T> The type of SecurityLevel thus the security levels the lattices use
 * 
 * @author Jan Maria Kirchner
 */
public class SecurityLattice<T extends SecurityLevel<?>> {

    private final T TOP;
    private final T BOTTOM;

    /**
     * @param top
     * @param bottom
     */
    public SecurityLattice(T top, T bottom) {
        this.TOP = top;
        this.BOTTOM = bottom;
    }

    /**
     * @return
     */
    public T top() {
        return TOP;
    }

    /**
     * @return
     */
    public T bottom() {
        return BOTTOM;
    }
}
