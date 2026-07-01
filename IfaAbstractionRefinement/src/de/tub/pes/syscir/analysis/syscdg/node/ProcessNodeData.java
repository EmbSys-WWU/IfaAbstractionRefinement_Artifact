package de.tub.pes.syscir.analysis.syscdg.node;

import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.CfgLikeRecord.Node;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;

/**
 * Class, that holds the data of a process in the SysCDG.
 *
 * @author twierbru
 */
public class ProcessNodeData extends SdgNodeData<AnalyzedProcess> {

    /**
     * The constructor.
     */
    public ProcessNodeData(Node node, AnalyzedProcess process) {
        super(node, process);
    }
}
