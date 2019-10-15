# zflow

Basic helper methods to quickly build somewhat typesafe pipelines in Scala using pure functional concepts (still wip though)

At the moment, you can only build typesafe bashscripts. There are features to submit bash scripts to slurm cluster, but this is still in development.

CAUTION!! Nothing here is anyway near production ready ..

Albeight, you can run the test and see the commands that will run in the pipeline.

Instead of print out, you could write the script to disk and run it (os-lib makes this really easy)

* https://github.com/lihaoyi/os-lib

Have a look at the SlurmTemplate to generate a `slurm` script for convenience.
