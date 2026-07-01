package de.tub.pes.syscir.analysis.abstraction_refinement;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import de.tomatengames.util.StringUtil;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;

/**
 * An InformationFlowPolicy describes forbidden and required information flows
 * as a list of entries. Entries are pairs of {@link PdgNodeId}s (more
 * specifically their .toString() representations) combined with an EntryType
 * (FORBIDDEN or REQUIRED).
 * 
 * This class additionally provides static utilities to parse a policy from a
 * file and a basic routine to check it.
 * 
 * @author Lukas Ernst
 */
public class InformationFlowPolicy {

    private final List<Entry> entries;

    public InformationFlowPolicy() {
        this.entries = new ArrayList<>();
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public void forbid(String source, String target) {
        entries().add(new Entry(EntryType.FORBIDDEN, source, target));
    }

    public void require(String source, String target) {
        entries().add(new Entry(EntryType.REQUIRED, source, target));
    }

    /**
     * Takes as input an array of pairs <code>p</code> (contained arrays
     * <code>p</code> must be of length 2) where <code>p[0]</code> is a source node
     * id and <code>p[1]</code> is a target node id.
     */
    public void separate(String[]... sourcesAndTargets) {
        for (int i = 0; i < sourcesAndTargets.length; i++) {
            require(sourcesAndTargets[i][0], sourcesAndTargets[i][1]);
        }
        for (int i = 0; i < sourcesAndTargets.length; i++) {
            for (int j = 0; j < sourcesAndTargets.length; j++) {
                if (i != j)
                    forbid(sourcesAndTargets[i][0], sourcesAndTargets[j][1]);
            }
        }
    }

    public static enum EntryType {
        FORBIDDEN, REQUIRED;
    }

    public static class Entry {

        private final EntryType type;
        private final String source, target;

        public Entry(EntryType type, String source, String destination) {
            this.type = type;
            this.source = source;
            this.target = destination;
        }

        public EntryType getType() {
            return type;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return type + " " + source + "->" + target;
        }
    }

    @Override
    public String toString() {
        return "{\n" + StringUtil.join(this.entries(), entry -> "\t" + entry, "\n") + "\n}";
    }

    /**
     * Checks this policy.
     * 
     * @param <P> the type of path to work with
     * @param pathfinder the function finding SDG paths if they exist for a certain
     *        policy entry
     * @param forbiddenPaths output for found forbidden paths
     * @param log the log to print results to
     * @return list of violated entries of this policy, empty if compliance is verified
     */
    public <P> List<Entry> check(Function<Entry, P> pathfinder, Consumer<P> forbiddenPaths, RefinementLog log) {
        List<Entry> violated = new ArrayList<>();
        for (InformationFlowPolicy.Entry entry : this.entries()) {
            P path = pathfinder.apply(entry);
            if (path != null) {
                if (entry.getType() == EntryType.FORBIDDEN) {
                    log.sdgPathFound(entry, path, false);
                    forbiddenPaths.accept(path);
                    violated.add(entry);
                } else {
                    log.sdgPathFound(entry, path, true);
                }
            } else {
                if (entry.getType() == EntryType.REQUIRED) {
                    log.sdgPathNotFound(entry, false);
                    violated.add(entry);
                } else {
                    log.sdgPathNotFound(entry, true);
                }
            }
        }
        return violated;
    }

    public void readPolicy(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            readPolicy(br);
        }
    }

    public void readPolicy(BufferedReader reader) throws IOException {
        for (PolicyCommand command : readPolicyCommands(reader)) {
            switch (command.name.toLowerCase(Locale.ROOT)) {
                case "require": {
                    for (String parameter : command.parameterLines) {
                        String[] ids = splitTwoIds(parameter);
                        require(ids[0], ids[1]);
                    }
                    break;
                }
                case "forbid": {
                    for (String parameter : command.parameterLines) {
                        String[] ids = splitTwoIds(parameter);
                        forbid(ids[0], ids[1]);
                    }
                    break;
                }
                case "separate": {
                    List<String[]> flows = new ArrayList<>();
                    for (String parameter : command.parameterLines)
                        flows.add(splitTwoIds(parameter));
                    separate(flows.toArray(String[][]::new));
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                            "Unknown policy command '" + command.name + "', use 'require', 'forbid' or 'separate'");
                }
            }
        }
    }

    private static String[] splitTwoIds(String param) {
        int startidx1, endidx1, startidx2, endidx2;
        if ((startidx1 = param.indexOf('(')) < 0 || (endidx1 = parenthesesEndIndex(param, startidx1)) < 0
                || (startidx2 = skipArrow(param, endidx1)) < 0 || (endidx2 = parenthesesEndIndex(param, startidx2)) < 0
                || !param.substring(endidx2).isBlank())
            throw new IllegalArgumentException("Parameter must be (NodeId) -> (NodeId)");
        return new String[] {param.substring(startidx1, endidx1), param.substring(startidx2, endidx2)};
    }

    private static int parenthesesEndIndex(String s, int fromIndex) {
        int depth = 0;
        for (int i = fromIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth <= 0)
                    return i + 1;
            }
        }
        return -1;
    }

    private static int skipArrow(String s, int fromIndex) {
        boolean minus = false;
        boolean edge = false;
        for (int i = fromIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                minus = true;
            } else if (c == '>') {
                if (!minus || edge)
                    return -1;
                edge = true;
            } else if (Character.isWhitespace(c)) {
                continue;
            } else {
                return i;
            }
        }
        return -1;
    }

    private static List<PolicyCommand> readPolicyCommands(BufferedReader reader) throws IOException {
        List<PolicyCommand> commands = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank())
                continue;
            boolean indented = Character.isWhitespace(line.charAt(0));
            line = line.trim();
            if (!indented) {
                int commandNameEndIndex = 0;
                while (true) {
                    if (commandNameEndIndex >= line.length()) {
                        commands.add(new PolicyCommand(line));
                        break;
                    }
                    if (Character.isWhitespace(line.charAt(commandNameEndIndex))) {
                        commands.add(new PolicyCommand(line.substring(0, commandNameEndIndex),
                                line.substring(commandNameEndIndex + 1)));
                        break;
                    }
                    commandNameEndIndex++;
                }
            } else {
                if (commands.isEmpty())
                    throw new IllegalArgumentException("First line cannot be indented, must initiate command");
                commands.getLast().parameterLines.add(line);
            }
        }
        return commands;
    }

    private static class PolicyCommand {

        private final String name;
        private final List<String> parameterLines;

        PolicyCommand(String name) {
            this.name = name;
            this.parameterLines = new ArrayList<>();
        }

        PolicyCommand(String name, String parameter) {
            this(name);
            this.parameterLines.add(parameter);
        }
    }

}
