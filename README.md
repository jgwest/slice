
Slice is a super-fast, lightweight, scalable, and easy-to-setup, source code search engine for use by individuals or small teams. It is designed and optimized for use on low-end commodity hardware: it will return lightning fast results on large source code bases without requiring a large amount of RAM or an SSD. 

Of course, it will also return lightning fast results on better equipped machines with more RAM+SSD, but returning fast results on fast machines is too easy :smile:.

Additionally, unlike traditional source code engines like Github source search, Slice is optimized for substring text search. This means a search for `RestService` will not only match `RestService.java`, but also `MyRestService.java` and `MockRestServiceTest.java`. Source code search engines like Github are traditionaly only optimized for 'exact match' or 'starts with' searches (as efficient tree-based algorithms exist for these). 

Slice is a [backronym](https://en.wikipedia.org/wiki/Backronym) that stands for Searchable scaLable Index Creation Engine.

## Getting Started

1) Clone the repo, build it, and extract the newly built Open Liberty server:
```
git clone https://github.com/jgwest/slice
cd slice
mvn clean package
cd SliceEAR/target
```

2) Extract the server to install it
    * Extract the file `SliceEAR-1.0.0-SNAPSHOT.zip` to somewhere on your machine, which we will refer to as the installation directory `(install dir)`.

3) Add configuration settings to the Open Liberty configuration file, `server.xml`:
    * Open the Liberty `server.xml` file at `(install dir)/wlp/usr/servers/defaultServer/server.xml`
    * If you want Open Liberty to be accessible from other machines, you must use `host="*"`. Add that attribute to the following line to server.xml (you may also edit http/https port): `<httpEndpoint httpPort="9080" httpsPort="9443" id="defaultHttpEndpoint" host="*">`   
    * Add the following to the `server.xml`:
```
	<jndiEntry jndiName="slice/config_xml_path" value="(path to GettingStarted-FileConfiguration.xml in artifacts/ directory of git repo)"/>
	<jndiEntry jndiName="slice/SRC_DIR" value="(path to root of git repo)"/>
	<jndiEntry jndiName="slice/DB_DIR" value="(path to where you want to store the database)"/>
```

4) Run the indexer to index the source code in SRC_DIR, and store the index in DB_DIR

```

export DB_DIR=(path to where you want to store the database)
export SRC_DIR=(path to root of git repo)

java -jar (git repo root)/SliceCreation/target/SliceCreation-1.0.0-SNAPSHOT.jar (path to GettingStarted-FileConfiguration.xml in artifacts/ directory of git repo)
```

5) Start the Open Liberty server
```
(install dir)/wlp/bin/server start
```

   * Or, to start the server as a foreground process (allowing you to see the console and server log), use: 

```
(install dir)/wlp/bin/server run
```


6) Visit the Slice application at `https://(your host name):9443/SliceRS/resources/test-src`. Before you can issue search requests, you must log-in. Click `Click here to log-in here.` The username is `test-user`, the password is `test-password`. Both of these values are specified in the `GettingStarted-FileConfiguration.xml` file.

