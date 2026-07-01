package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

/**
 * Interface representing a variable (not its value) that is either a {@link GlobalVariable} or a
 * {@link LocalVariable}.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <QualifierT> the type of qualifier that is required to uniquely identify the variable in addition to the SCVariable
 * @param <SCVarT> the type specifying the variable (a subclass of SCVariable or SCPort)
 */
public sealed interface Variable<QualifierT, SCVarT> permits GlobalVariable, LocalVariable {

    /**
     * Returns the qualifier that is needed to uniquely identify the variable in addition to the
     * SCVariable.
     *
     * @return qualifier
     */
    QualifierT getQualifier();

    /**
     * Returns the SysCIR variable specifier.
     *
     * @return sc variable
     */
    SCVarT getSCVariable();

}
