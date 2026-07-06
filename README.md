# Automated Abstraction Refinement for Information Flow Security in Embedded Systems - Artifact and Reproduction Package

## Summary

This repository contains the implementation of our approach to abstraction refinement for information flow security in embedded systems.

All examples are included in the IfaAbstractionRefinement/testdata directory. (In the Docker image they can be found under /app/testdata.)

The program is intended to be executed inside a Docker image.
Further information regarding the creation and usage of a Docker image is provided below.

## Hardware requirements

Most of the examples (with the exception of "Transmitters" when run without exploration refinement) aren't very demanding. 4 GB of RAM and any reasonable CPU from the last 8 years should be fine.

## Setup

- Install and set up Docker (for details see https://www.docker.com/get-started/).
- Load the provided Docker image by running `docker load -i docker_image.tar` in the main directory (`IfaAbstractionRefinement_Artifact`), or build the Docker image by yourself (see Building the Docker image).
- The provided Docker image should be fully functional and can now be run.

## Test instructions

To test your setup, you can analyze the examples from the paper by running the `run_examples` script.

## Replicate experiments

All examples used in the paper can be executed via the Docker image.
If you want to simply run all examples at once and measure their performance, you can run `./eval_examples`. This produces the results listed in the Evaluation section of the paper.

To run the analysis on one example, use the command `docker run --rm --name artifact artifact -c "java -jar /app/AbstractionRefinement.jar -b /app/testdata/<test-case-directory>/<test-case> -s -ic"` (or other options for other refinement strategies, see table below, and with `<test-case-directory>` and `<test-case>` replaced appropriately).
To evaluate its performance parameters, add the option `-e` to the java call. This will lead to longer execution times (because the analysis is performed multiple times).
The results are printed to the command line. With the option `-o <path>`, you can specify an output file for the results. In non-evaluation mode, this file also contains additional information such as complete SysCDGs.

Alternatively, you can run the container and open a command line by executing `docker run --rm --name artifact -it artifact`.
Then, you can run the examples via `java -jar /app/AbstractionRefinement.jar -b <path-to-file>/<name> -s -ic` (potentially with `-e` added), which allows you to view the log files that are generated during evaluation after the evaluation terminated.

## Analyze other systems

Before you can analyze a system with our tool, you need to first parse it into an abstract syntax tree with SC2AST.
To do this, open a command line in the container by executing `docker run --rm --name artifact -it artifact` and then run `java -jar /app/sc2ast.jar -f <list-of-files> -o <path-to-output>/<name>`.
(For further information on this step, see SC2AST/docs/SC2AST.pdf.)

If you want to use the information flow refinement, you also must provide an information flow policy in a file called `<name>.policy.txt`. For this, see Specification of Policies.

You can also provide an initial abstraction in a file called `<name>.abstraction.txt`. For this, see Providing Initial Abstractions.

To perform the analysis, run (inside the container) `java -jar /app/AbstractionRefinement.jar <options>`, using the command-line options explained below.
If your abstract syntax tree, policy, and predefined abstraction files (so far as present) all have the same name (except for the suffixes `.ast.xml`, `.policy.txt`, and `.abstraction.txt`), you can use the option `-b <path>/<name>` instead of specifying all paths separately.

If you want to keep the logs from the execution or save the `.ast.xml`, you can use `docker cp artifact:/app/<path>/<file> <path-to-file-on-disk>/<file>` in a separate terminal while the container is still running. Note that containers started with `--rm` are removed after they terminate.

Please note that the implementation is a prototype, and many features of SystemC are not supported (e.g. arrays, pointers, or specified initialization).
Systems using such features will likely encounter exceptions.

### Specification of policies

A policy file describes the information flows the analysis must check. It consists of a list of entries, where each entry is a pair of SDG node ids combined with a type, either **FORBIDDEN** or **REQUIRED**.
During checking, a **FORBIDDEN** entry is violated if a path from source to target exists in the SDG, while a **REQUIRED** entry is violated if no such path exists.
See the policy files for the examples from the paper as examples.

**File structure**

The policy file is line-oriented and grouped into *commands*:

- A line that starts at the very beginning (no leading whitespace) introduces a command; its first token is the command name, and the remainder of the line (if any) is treated as a parameter.
- A line that starts with whitespace is a *parameter line* belonging to the most recent command.
- Blank lines are ignored.
- The first non-blank line must be a command - it cannot be indented.
- Command names are case-insensitive.

**Commands**

| Command | Meaning |
|---------|---------|
| `require` | Each parameter declares a flow that **must** exist. |
| `forbid`  | Each parameter declares a flow that **must not** exist. |
| `separate` | Convenience command over a list of source→target pairs: it *requires* each listed flow and *forbids* every cross flow (the source of pair *i* to the target of pair *j* for all *i ≠ j*). This expresses that the listed flows must each occur but stay isolated from one another. |

Any other command name is an error (the parser accepts only `require`, `forbid`, or `separate`).

**Parameter format**

Every parameter line has the form:

```
(NodeId) -> (NodeId)
```

Both the source and target node ids are enclosed in parentheses (they may themselves contain nested parentheses - matching is done by parenthesis depth), separated by an arrow `->`. Surrounding whitespace is allowed, but any trailing non-blank text makes the line invalid.
The node id strings correspond to the `PdgNodeId.toString()` representations emitted by the analysis (without their transition IDs). For variable in and out nodes, these have the form `(IN <variable>)` and `(OUT <variable>)`, with `<variable>` a variable specification as defined below -- however, only the `LVar` and `GVar` syntax with all otherwise optional elements present will work.

### Providing initial abstractions

A predefined abstraction file (`<name>.abstraction.txt`) supplies a set of *sources* that the analysis tracks from the start, instead of (or before) deriving them through refinement.

If an initial abstraction is specified, the source type declared in its first line is used for the rest of the analysis. For example, an initial abstraction starting with `variables` makes the analysis continue with variable sources, while one starting with `assignments` makes it continue with assignment-expression sources.

**File structure**

- The **first line** declares the abstraction *type*. It must be exactly one of `variables` or `assignments`. An unknown type is an error.
- Each subsequent non-empty line (leading/trailing whitespace is trimmed) is one entry, interpreted according to the declared type:
  - `variables` → each line names a variable.
  - `assignments` → each line names the expression location of the **right-hand side of an assignment**.

#### Variable entries

Each variable line can use either a simple, hand-written format or the full `toString()` format that the analysis itself emits. The latter is usually easiest, since it can be copied directly from a generated graph or log.

**Global variables**

- Simple: `global <instanceName>.<variableName>` - a member variable of a module instance. The instance is looked up by name, and the variable by name within that instance's class.
- Full: `GVar[<instanceType> <instanceName>;.<variableType> <variableName>;]` - the format emitted by the tool. Leading type prefixes and trailing semicolons are stripped automatically; only the instance name and variable name are used.

**Local variables**

- Simple: `local <function>/<variableName>` - exactly one function followed by the variable or parameter name. The function may be written as `<class>.<function>`, or as just `<function>` if that function name is unambiguous across all classes. Note that the simple format does not accept a multi-level `/`-separated call stack; use the `LVar[...]` format below for variables inside nested calls.
- Full: `LVar[<stackTrace>.<variableName>]` - the format emitted by the tool. The variable name comes after the final dot and may include the declared type and a trailing semicolon. The `<stackTrace>` describes the call chain leading to the variable:
  - `<class>.<function>` for a variable directly in a function that is not reached through any tracked call, or
  - `<class>.<baseFunction>:[i, j, ...].<calledFunction>` where `:[i, j, ...]` are the evaluation-location indices identifying the call site. The chain can be extended further, e.g. `<class>.<baseFunction>:[...].<function2>:[...].<finalFunction>`. Functions within the chain do not have a class prefix; they are identified by their call site.

**Examples**

```
variables
global bt.high_in
global bt.low_out
GVar[backoff_transmitter bt;.int high_in;]
GVar[backoff_transmitter bt;.int low_out;]
LVar[backoff_transmitter.monitor.int backoff;]
local high_transmitter/backoff
local backoff_transmitter.low_transmitter/backoff
LVar[backoff_transmitter.monitor:[1, 1].handle_backoff.int bo;]
LVar[backoff_transmitter.high_transmitter:[1, 1].handle_backoff.int bo;]
LVar[low_transmitter:[1, 1].handle_backoff.int bo;]
```

In the examples above:
- `global bt.high_in` and `GVar[backoff_transmitter bt;.int high_in;]` both name the member variable `high_in` of the module instance `bt`.
- `global bt.low_out` and `GVar[backoff_transmitter bt;.int low_out;]` both name the member variable `low_out` of the module instance `bt`.
- `LVar[backoff_transmitter.monitor.int backoff;]` names the local variable `backoff` of the function `monitor` in the module class `backoff_transmitter`.
- `local high_transmitter/backoff` names the local variable `backoff` of the function `high_transmitter`. This short form is accepted if the function name is unambiguous.
- `local backoff_transmitter.low_transmitter/backoff` names the local variable `backoff` of the function `low_transmitter` in the module class `backoff_transmitter`.
- The `LVar[...]handle_backoff.int bo;` entries name the local variable `bo` of the function `handle_backoff`, reached through the indicated call sites from `monitor`, `high_transmitter`, and `low_transmitter`.
- In `LVar[low_transmitter:[1, 1].handle_backoff.int bo;]`, the base function is written without the class prefix. This is accepted if the function name is unambiguous.

#### Assignment entries

For `assignments` abstractions, each line identifies the expression location of the **right-hand side expression of an assignment**.

The line format is the `ExpressionLocation.toString()` format emitted by the analysis:

```
<stackTrace>:[i, j, ...]
```

where:

- `<stackTrace>` identifies the function and call chain in which the expression occurs.
- `[i, j, ...]` is the expression-location index list.
- The location must refer to the right-hand side expression of the assignment, not to the assignment expression or statement itself.

The easiest way to create an assignment abstraction is to first run the analysis without a predefined abstraction, inspect the generated output/logs, and copy the printed expression locations for the relevant assignment right-hand sides.

**Examples**

```
assignments
backoff_transmitter.low_transmitter:[0, 1]
backoff_transmitter.high_transmitter:[0, 1]
backoff_transmitter.monitor:[0, 1]
backoff_transmitter.low_transmitter:[1, 3, 1, 1]
backoff_transmitter.high_transmitter:[1, 4, 1, 1]
backoff_transmitter.monitor:[1, 6, 1, 1]
backoff_transmitter.low_transmitter:[1, 3, 2, 1]
backoff_transmitter.high_transmitter:[1, 4, 2, 1]
backoff_transmitter.monitor:[1, 6, 2, 1]
```

### Command-line options of AbstractionRefinement.jar

The analysis is started with `java -jar /app/AbstractionRefinement.jar <options>`.
The following options are available:

| Option | Long form | Argument | Description |
|--------|-----------|----------|-------------|
| `-b`   | `--base-path`           | path prefix | Prefix used to derive the model, policy and abstraction paths. The model is read from `<prefix>.ast.xml`, the policy from `<prefix>.policy.txt`, and the predefined initial abstraction from `<prefix>.abstraction.txt`. Use this instead of `-m`/`-p`/`-a` when the files share a common base name. |
| `-m`   | `--model`               | path        | Path to the `.ast.xml` file of the system model. |
| `-p`   | `--policy`              | path        | Path to the policy file to check. If omitted, no policy is checked. |
| `-a`   | `--abstraction`         | path        | Path to a predefined initial abstraction to use. |
| `-s`   | `--splitters`           | (none)      | Enable the splitters heuristic for exploration refinement. |
| `-d`   | `--partial-descendants` | integer     | Enable the partial descendants heuristic for exploration refinement with the given threshold. |
| `-ic`  | `--cumulative-ifr`      | (none)      | Enable cumulative information flow refinement. |
| `-in`  | `--non-cumulative-ifr`  | (none)      | Enable non-cumulative information flow refinement. |
| `-o`   | `--out`                 | path        | Path to the file where the output should be written. If the file already exists, it is removed and overwritten. |
| `-e`   | `--eval`                | (none)      | Run in evaluation mode: perform several measured iterations and print the median runtime and memory usage instead of a normal run. |

**Required combinations:**

- Exactly one model source must be given: either `-m` **or** `-b`.
- If any of `-s`, `-d`, `-ic`, or `-in` is specified, automated abstraction refinement is enabled. If neither `-s` nor `-d` is specified, exploration refinement uses `SimpleConditionTracking.NONE`.
- Options `-ic` and `-in` are mutually exclusive. If either is specified, a policy file must be provided via `-p` or `-b`.
- If an initial abstraction is provided via `-a` or `-b`, the abstraction type declared in that file (`variables` or `assignments`) determines the type used throughout the subsequent analysis. Otherwise, `assignments` is used.


## Building the Docker image

- Clone the repository locally.
- If desired, add your own program files to SysCIR/testdata, so they are available in the image.
- Build the image of this container using `docker build -t artifact .` in the main directory (`IfaAbstractionRefinement_Artifact`).
This step might take a few minutes and will download the necessary dependencies for the Docker image. These dependencies are gcc, g++, lightweight Java, Maven, and the dependencies and plugins listed in `SC2AST/pom.xml` and `IfaAbstractionRefinement/pom.xml`.
