# FRASP: Scala-based DSL for Distribured FRP -- ACSOS 2023 Showcase

This repository contains the experiments discussed in the paper entitled: "Self-Organisation Programming: A Functional Reactive Macro Approach" submitted @ ACSOS 2023.
For more details about the FRASP library, please refer to the [library repository](https://github.com/cric96/distributed-frp).

## Description
FRASP (Field-based Reactive Aggregate Computing) is a reactive model inspired by aggregate computing, which utilizes a self-organizing top-down global-to-local approach for programming self-organizing collective behaviors. For a more detailed understanding, please refer to the documentation of the [ScaFi library](https://scafi.github.io/) or read the survey paper titled ["From field-based coordination to aggregate computing"](https://doi.org/10.1007/978-3-319-92408-3_12).

This repository focuses on performing simulations to empirically evaluate the reactive implementation of FRASP. The objectives of these simulations are as follows:

1. Ensure that the reactive implementation produces the same collective behaviors as the proactive implementation.
2. Demonstrate the improved efficiency of the reactive implementation in scenarios with limited environmental modifications.
3. Show the ability of the reactive implementation to compute only the necessary parts of the program that require recomputation.

To validate the aforementioned statements, we provide two commonly used simulations from the field coordination literature: the self-healing gradient and the channel.

- The self-healing gradient is a bio-inspired pattern where each node computes its distance from a source zone solely based on neighborhood information, without relying on GPS.
- The channel simulation creates a logical channel, represented as a Boolean field, linking a source zone to a destination zone.

Through these simulations, we aim to showcase the effectiveness and benefits of FRASP's reactive approach in achieving self-organizing collective behaviors, as well as its improved efficiency in specific scenarios. 

In both simulation cases, the nodes are deployed in a random perturbed lattice, providing a realistic environment. The color of each node represents the output of the FRASP program. For the self-healing gradient simulation, please refer to the [Gradient.scala](https://github.com/AggregateComputing/experiment-2023-acsos-distributed-frp/blob/master/src/main/scala/it/unibo/Gradient.scala) file, and for the channel simulation, please refer to [Channel.scala](https://github.com/AggregateComputing/experiment-2023-acsos-distributed-frp/blob/master/src/main/scala/it/unibo/Channel.scala).

Additionally, black dots in the simulations represent obstacles or zones in which nodes cannot communicate with each other. This feature adds an additional layer of complexity to the scenarios and showcases how FRASP handles such constraints.


|  Gradient | Channel  |
|---|---|
| ![gradient](https://github.com/AggregateComputing/experiment-2023-acsos-distributed-frp/assets/23448811/7bb3ef9c-db3b-471f-9afc-b763e9041049)  |  ![channel](https://github.com/AggregateComputing/experiment-2023-acsos-distributed-frp/assets/23448811/c2c85dc2-f7e2-4e57-aa59-d08ecee731ef)|


## Getting started

**WARNING**: re-running the whole experiment may take a very long time on a normal computer.

### Reproduce with containers (recommended)

1. Install docker and docker-compose
2. Run `docker-compose up`
3. The charts will be available in the `charts` folder.

### Reproduce natively

1. Install a Gradle-compatible version of Java.
  Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
  to learn which is the compatible version range.
  The Version of Gradle used in this experiment can be found in the `gradle-wrapper.properties` file
  located in the `gradle/wrapper` folder.
2. Install the version of Python indicated in `.python-version` (or use `pyenv`).
3. Launch either:
    - `./gradlew runAllBatch` on Linux, MacOS, or Windows if a bash-compatible shell is available;
    - `gradlew.bat runAllBatch` on Windows cmd or Powershell;
4. Once the experiment is finished, the results will be available in the `data` folder. Run:
    - `pip install --upgrade pip`
    - `pip install -r requirements.txt`
    - `python process.py`
5. The charts will be available in the `charts` folder.

## Inspect a single experiment

Follow the instructions for reproducing the entire experiment natively, but instead of running `runAllBatch`,
run `runEXPERIMENTGraphics`, replacing `EXPERIMENT` with the name of the experiment you want to run
(namely, with the name of the YAML simulation file).

If in doubt, run `./gradlew tasks` to see the list of available tasks.

To make changes to existing experiments and explore/reuse,
we recommend to use the IntelliJ Idea IDE.
Opening the project in IntelliJ Idea will automatically import the project, download the dependencies,
and allow for a smooth development experience.

When the GUI pop-up, press <kbd>P</kbd> to see the dynamics described in the articles.

## Regenerate the charts

We keep a copy of the data in this repository,
so that the charts can be regenerated without having to run the experiment again.
To regenerate the charts, run `docker compose run --no-deps charts`.
Alternatively, follow the steps or the "reproduce natively" section,
starting after the part describing how to re-launch the simulations.
