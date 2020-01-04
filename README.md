# BSBM

This is a fork of the Berlin Sparql Benchmark (BSBM) with aim to support benchmarking of virtual SPARQL endpoints based on R2RML.

http://sourceforge.net/projects/bsbmtools/

## Getting compiled version
### Docker
Using the following command, you can get the compiled version running using docker:

```
docker run -it mchaloupka/bsbm-r2rml:latest bash
```

It will start an interactive `bash` console in the folder where the BSBM is available.

### Build locally
To generate the build directory, run the following command:
```
./gradlew installDist
```
That will generate a `./build/install/bsbm` folder.

## Running

In the build folder, use:

```
./generate
```


## Additional features: 

Beware some of the paths are hardcoded, always generate files to the \<bsbm-installation-folder>.

### New serializers

**Use:**  
-s nqr  
-s nqp  
-s trig   

to generate quads with different graph associtations.


In order to generate roughly 1 million triples, using the slightly modified default quad generation schema, call:

```
./generate -pc 2785 -s trig 
```

### Auth generator

In order to create the authorization graph for a 1000 users, with 100 groups, call:
```
generateAuth -fn ./auth.trig -uc 1000 -gc 100 
```

The following files will be generated in the <bsbm-installation-folder> folder:
* auth.trig : The ontology, mapping groups to graphs
* auth.ldif : An LDAP dump of users and their groups