package de.tub.pes.syscir.analysis.abstraction_refinement;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView;
import de.tub.pes.syscir.analysis.statespace_exploration.StackTraceView.StackTrace;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utilities for parsing an abstraction (set of sources) from a file.
 * 
 * @author Lukas Ernst
 */
public class SourcesParser {

    public static Sources parse(SCSystem system, Path inputFile) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(inputFile)) {
            return parse(system, br);
        }
    }

    public static Sources parse(SCSystem system, BufferedReader input) throws IOException {
        String type = input.readLine();
        if (type.equals("variables")) {
            Sources sources = new SourceVariables();
            processInput(input, line -> sources.add(findVariable(system, line)));
            return sources;
        }
        boolean assignments;
        if ((assignments = type.equals("assignments")) /* || type.equals("expressions") */) {
            Sources sources = assignments ? new SourceAssignments() : new SourceExpressions();
            processInput(input, line -> {
                SourceExpressions.ExpressionLocation parsed = findExpressionLocation(system, line);
                sources.add(parsed.stack(), parsed.location());
            });
            return sources;
        }
        /*
         * if (type.equals("retainments")) { Sources sources = new SourceRetainments(); processInput(input,
         * line -> sources.addRetains(findExpression(system, line))); return sources; }
         */
        throw new IllegalArgumentException("Unknown sources type '" + type + "'");
    }

    private static void processInput(BufferedReader input, Consumer<String> lineConsumer) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                lineConsumer.accept(line);
            }
        }
    }

    private static Variable<?, ?> findVariable(SCSystem system, String line) {
        // Check if this is the full toString format without prefix
        if (line.startsWith("LVar[") && line.endsWith("]")) {
            String content = line.substring(5, line.length() - 1); // Remove "LVar[" and "]"
            return parseLocalVariableFromString(system, content);
        }
        if (line.startsWith("GVar[") && line.endsWith("]")) {
            String content = line.substring(5, line.length() - 1); // Remove "GVar[" and "]"
            // Parse GlobalVariable#toString format: instanceName.variableName
            int dotIdx = content.indexOf('.');
            if (dotIdx < 0) {
                throw new IllegalArgumentException("Global variable specification must be instanceName.variableName");
            }

            String instanceName = content.substring(0, dotIdx);
            if (instanceName.contains(" ")) {
                instanceName = instanceName.substring(instanceName.indexOf(' ') + 1);
            }
            if (instanceName.contains(";")) {
                instanceName = instanceName.substring(0, instanceName.indexOf(';'));
            }

            String variableName = content.substring(dotIdx + 1);
            if (variableName.contains(" ")) {
                variableName = variableName.substring(variableName.indexOf(' ') + 1);
            }
            if (variableName.contains(";")) {
                variableName = variableName.substring(0, variableName.indexOf(';'));
            }

            SCClassInstance instance = system.getInstanceByName(instanceName);
            if (instance == null) {
                throw new IllegalArgumentException("No SCClassInstance with name '" + instanceName + "' found");
            }
            SCVariable variable = instance.getSCClass().getMemberByName(variableName);
            if (variable == null) {
                throw new IllegalArgumentException(
                        "No variable with name '" + variableName + "' found in " + instance.getSCClass());
            }
            return new GlobalVariable<>(WrappedSCClassInstance.getWrapped(instance), variable);
        }

        String[] parts = line.split(" ", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Variable identifier must be global/local <spec>");
        }
        String type = parts[0];
        String spec = parts[1];
        if (type.equals("global")) {
            int splitidx = spec.indexOf('.');
            if (splitidx < 0) {
                throw new IllegalArgumentException("Global variable specification must be instanceName.variableName");
            }
            String instanceName = spec.substring(0, splitidx);
            String variableName = spec.substring(splitidx + 1);
            SCClassInstance instance = system.getInstanceByName(instanceName);
            if (instance == null) {
                throw new IllegalArgumentException("No SCClassInstance with name '" + instanceName + "' found");
            }
            SCVariable variable = instance.getSCClass().getMemberByName(variableName);
            if (variable == null) {
                throw new IllegalArgumentException(
                        "No variable with name '" + variableName + "' found in " + instance.getSCClass());
            }
            return new GlobalVariable<>(WrappedSCClassInstance.getWrapped(instance), variable);
        }
        if (type.equals("local")) {
            // Support both formats:
            // 1. Simple: "functionName/variableName"
            // 2. Full toString format: "LVar[stackTrace.variableName]" (handled below)

            // Parse simple format: functionName/variableName
            String[] stackAndVarName = spec.split("/");
            if (stackAndVarName.length != 2) {
                throw new IllegalArgumentException(
                        "Local variable specification must be functionName/variableName or LVar[...] format");
            }

            String functionName = stackAndVarName[0];
            String variableName = stackAndVarName[1];

            WrappedSCFunction function = WrappedSCFunction.getWrapped(findFunction(system, functionName));
            StackTrace stackTrace = new StackTrace(function, new ArrayList<>());

            SCVariable variable = function.getLocalVariableOrParameterAsSCVar(variableName);
            if (variable == null) {
                throw new IllegalArgumentException(
                        "No local variable with name '" + variableName + "' in function " + function);
            }
            return new LocalVariable<>(stackTrace, variable);
        }
        throw new IllegalArgumentException("Only variable types global and local are supported, not '" + type + "'");
    }

    /**
     * Parses an expression location which can be in two formats: 1. ExpressionLocation.toString format:
     * "stackTrace:location" (e.g., "ClassName.func:[0,1]") 2. Legacy position format: "in functionName
     * position#/count expressionString"
     */
    private static SourceExpressions.ExpressionLocation findExpressionLocation(SCSystem system, String line) {
        // Try to detect ExpressionLocation.toString format
        // Format: stackTrace:location where location is [n, n, ...]
        int lastBracketStart = line.lastIndexOf('[');
        if (lastBracketStart > 0) {
            int lastBracketEnd = line.lastIndexOf(']');
            if (lastBracketEnd > lastBracketStart && lastBracketEnd == line.length() - 1) {
                // This might be the ExpressionLocation format
                String beforeLocation = line.substring(0, lastBracketStart);
                String locationStr = line.substring(lastBracketStart);

                // Check if there's a colon before the location
                if (beforeLocation.endsWith(":")) {
                    String stackTraceStr = beforeLocation.substring(0, beforeLocation.length() - 1);
                    List<Integer> location = parseIndicesList(locationStr);
                    // parseStackTrace returns a StackTrace, which is a StackTraceView
                    StackTraceView stackTraceView = parseStackTrace(system, stackTraceStr);

                    return new SourceExpressions.ExpressionLocation(stackTraceView, location);
                }
            }
        }

        throw new IllegalArgumentException(
                "Legacy expression format is no longer supported. Please use ExpressionLocation.toString format.");
    }

    private static Expression findExpression(SCSystem system, String line) {
        SCFunction function = null;
        if (line.startsWith("in ")) {
            int splitidx = line.indexOf(' ', 3);
            if (splitidx < 0) {
                throw new IllegalArgumentException("Expression identifier starting with 'in ' must name a function");
            }
            function = findFunction(system, line.substring(3, splitidx));
            line = line.substring(splitidx + 1);
        }

        String[] parts = line.split(" ", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Expression identifier must contain a positioning and the expression string");
        }

        String position = parts[0];
        String expressionString = parts[1];

        int possplitidx = position.indexOf("./");
        if (possplitidx < 0) {
            throw new IllegalArgumentException("Expression location must be separated by './'");
        }
        int requestPos = Integer.parseInt(position.substring(0, possplitidx));
        int expectedCount = Integer.parseInt(position.substring(possplitidx + 2));

        Expression foundExpression = null;
        int count = 0;
        List<Expression> allExpressions;
        if (function != null) {
            allExpressions = function.getAllExpressions();
        } else {
            allExpressions = new ArrayList<>();
            for (SCClass c : system.getClasses()) {
                for (SCFunction cfunc : c.getMemberFunctions()) {
                    allExpressions.addAll(cfunc.getAllExpressions());
                }
            }
        }
        for (Expression expression : allExpressions) {
            if (!expression.toStringNoSem().equals(expressionString)) {
                continue;
            }
            count++;
            if (count == requestPos) {
                foundExpression = expression;
            }
        }
        if (count != expectedCount) {
            throw new IllegalArgumentException(
                    "Amount of '" + expressionString + "' expressions was " + count + ", not " + expectedCount);
        }
        if (foundExpression == null) {
            throw new IllegalArgumentException("Invalid expression position " + requestPos + " of " + count);
        }
        return foundExpression;
    }

    private static SCFunction findFunction(SCSystem system, String identifier) {
        int splitidx = identifier.indexOf('.');
        if (splitidx < 0) {
            SCFunction func = null;
            for (SCClass clazz : system.getClasses()) {
                SCFunction other = clazz.getMemberFunctionByName(identifier);
                if (other == null) {
                    continue;
                }
                if (func != null) {
                    throw new IllegalArgumentException("Function name " + identifier + " is ambiguous, found in both "
                            + func.getSCClass() + " and " + other.getSCClass());
                }
                func = other;
            }
            if (func == null) {
                throw new IllegalArgumentException("No function with name '" + identifier + "' found");
            }
            return func;
        }
        String className = identifier.substring(0, splitidx);
        String functionName = identifier.substring(splitidx + 1);
        SCClass c = findClass(system, className);
        SCFunction func = c.getMemberFunctionByName(functionName);
        if (func == null) {
            throw new IllegalArgumentException("No function with name '" + functionName + "' found in " + c);
        }
        return func;
    }

    private static SCClass findClass(SCSystem system, String className) {
        SCClass c = system.getClassByName(className);
        if (c != null) {
            return c;
        }
        // to also find channel classes:
        for (SCClassInstance instance : system.getInstances()) {
            c = instance.getSCClass();
            if (className.equals(c.getName())) {
                return c;
            }
        }
        throw new IllegalArgumentException("SCClass '" + className + "' not found");
    }

    /**
     * Parses a LocalVariable from its toString format (without the "LVar[" and "]" wrapper). Format:
     * stackTrace.variableName where stackTrace is: baseFunction or
     * baseFunction:indices.nextFunction:indices...
     */
    private static LocalVariable<?> parseLocalVariableFromString(SCSystem system, String content) {
        // Split by the last dot to separate stack trace from variable name
        int lastDot = content.lastIndexOf('.');
        if (lastDot < 0) {
            throw new IllegalArgumentException("LocalVariable string must contain at least one dot");
        }

        String stackTraceStr = content.substring(0, lastDot);
        String variableName = content.substring(lastDot + 1);

        if (variableName.contains(" ")) {
            variableName = variableName.substring(variableName.indexOf(' ') + 1);
        }
        if (variableName.contains(";")) {
            variableName = variableName.substring(0, variableName.indexOf(';'));
        }

        // Parse the stack trace
        StackTrace stackTrace = parseStackTrace(system, stackTraceStr);

        // Get the variable from the appropriate function (last function in the call chain)
        WrappedSCFunction targetFunction;
        if (stackTrace.calls().isEmpty()) {
            targetFunction = stackTrace.base();
        } else {
            // Get the function from the last call
            EvaluationLocation lastCall = stackTrace.calls().getLast();
            Expression expr = lastCall.getNextExpression();
            if (!(expr instanceof FunctionCallExpression fce)) {
                throw new IllegalArgumentException("Last call in stack trace must be a FunctionCallExpression");
            }
            targetFunction = WrappedSCFunction.getWrapped(fce.getFunction());
        }

        SCVariable variable = targetFunction.getLocalVariableOrParameterAsSCVar(variableName);
        if (variable == null) {
            throw new IllegalArgumentException(
                    "No local variable with name '" + variableName + "' in function " + targetFunction);
        }

        return new LocalVariable<>(stackTrace, variable);
    }

    /**
     * Parses a StackTrace from its toString format. Format: ClassName.baseFunction or
     * ClassName.baseFunction:[indices].ClassName2.function2:[indices]...finalFunctionName
     */
    private static StackTrace parseStackTrace(SCSystem system, String stackTraceStr) {
        List<EvaluationLocation> calls = new ArrayList<>();

        // Parse the base function (ClassName.functionName at the start)
        // Find the first colon - everything before it (up to the first ]) is the base function spec
        int firstColon = stackTraceStr.indexOf(':');
        String baseFunctionSpec;
        int pos;

        if (firstColon < 0) {
            // No colon means no calls, just ClassName.functionName
            baseFunctionSpec = stackTraceStr;
            pos = stackTraceStr.length();
        } else {
            // Find the className.functionName before the first colon
            baseFunctionSpec = stackTraceStr.substring(0, firstColon);
            pos = firstColon;
        }

        WrappedSCFunction baseFunction = WrappedSCFunction.getWrapped(findFunction(system, baseFunctionSpec));

        // Parse EvaluationLocations if there are any
        while (pos < stackTraceStr.length() && stackTraceStr.charAt(pos) == ':') {
            // We're at a colon, parse the indices
            pos++; // Skip the ':'
            int closeBracket = stackTraceStr.indexOf(']', pos);
            if (closeBracket < 0) {
                throw new IllegalArgumentException("Missing closing bracket for indices");
            }

            String indicesStr = stackTraceStr.substring(pos, closeBracket + 1);
            List<Integer> indices = parseIndicesList(indicesStr);

            // The function for this EvaluationLocation is determined by the baseFunctionSpec we just parsed
            // (or the previous evalLocation's function)
            WrappedSCFunction evalLocFunction;
            if (calls.isEmpty()) {
                evalLocFunction = baseFunction;
            } else {
                // Get function from previous call's expression
                EvaluationLocation prevCall = calls.getLast();
                Expression expr = prevCall.getNextExpression();
                if (!(expr instanceof FunctionCallExpression fce)) {
                    throw new IllegalArgumentException("Previous call must be a FunctionCallExpression");
                }
                evalLocFunction = WrappedSCFunction.getWrapped(fce.getFunction());
            }

            EvaluationLocation evalLoc = new EvaluationLocation(evalLocFunction, indices);
            calls.add(evalLoc);

            pos = closeBracket + 1; // Move past the ']'

            // Now check if there's a dot followed by another className.functionName
            if (pos < stackTraceStr.length() && stackTraceStr.charAt(pos) == '.') {
                pos++; // Skip the '.'

                // Find the next colon or end of string to get the next function spec
                int nextColon = stackTraceStr.indexOf(':', pos);
                if (nextColon < 0) {
                    // This is the final function name (just the name, not className.functionName)
                    pos = stackTraceStr.length();
                    // Don't create an EvaluationLocation for this - it's just the final called function name
                    break;
                } else {
                    // There's another EvaluationLocation coming
                    pos = nextColon;
                    // This nextFunctionSpec will be used in the next iteration
                }
            }
        }

        return new StackTrace(baseFunction, calls);
    }

    /**
     * Parses a list of indices from string format [1, 2, 3] to List<Integer>
     */
    private static List<Integer> parseIndicesList(String indicesStr) {
        // Remove brackets and spaces
        indicesStr = indicesStr.trim();
        if (!indicesStr.startsWith("[") || !indicesStr.endsWith("]")) {
            throw new IllegalArgumentException("Indices must be in format [1, 2, 3]");
        }

        indicesStr = indicesStr.substring(1, indicesStr.length() - 1).trim();

        if (indicesStr.isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts = indicesStr.split(",");
        List<Integer> indices = new ArrayList<>();
        for (String part : parts) {
            indices.add(Integer.parseInt(part.trim()));
        }
        return indices;
    }

}
