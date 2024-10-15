# Models and Query Languages for Temporal Graph Databases
## Test T-GQL Queries

A "ready to run" set up can be found in the following Google drive [link](https://drive.google.com/drive/folders/10EcvD5sn74iurgztyBipxZ3jr-Dt-vSY?usp=sharing). It is necessary to run Java commands from a terminal. Find the instructions in this [tutorial](https://youtu.be/q0kJco8DYvs) or you can follow the steps below to proceed with the installation.

1. [Clone or Download and unzipp this project](project)
2. [Prepare and start Neo4j database](#neo4j)
3. [Prepare and execute the T-GQL webapp](webapp)
4. [Run queries](#run)



#### <h4 id=project>1. Clone or Download and unzip this project </h4>

#### <h4 id=neo4j>2. Prepare and start Neo4j database</h4>
   ##### a. Copy the database
   > In this work, we used Neo4j Community version 3.5.14. Once downloaded and extracted, you will find the folder
   <code>neo4j-community-3.5.14/data/databases</code>. 
   
   > Copy one or all of the databases provided with this work (```utils``` folder in this repository): _soc_net.db_, _Flighst.db_, _schelde_L60.db_, _schelde_G60.db_ and _schelde_G10.db_, and paste them in the Neo4j database folder.
  
  > Edit the  **conf/neo4j.conf** file and include the line:
   ```dbms.active_database=schelde_G60.db``` (or any of the above mentioned databases). 
   This command is available en Neo4j version 3.5.14. It is possible that for other versions, the line is slightly different.

  ##### b. Include the T-GQL plugin
> You can copy the _procedure-1.0.jar_ file from the __utils__ folder or compile it and produce it. Paste it in the Neo4j **plugins** folder.
> We are ready to start the database. For our Neo4j version, run into a bash terminal, in the neo4j folder, the following command line:

```bin/neo4j.bat console```


#### <h4 id=webapp>3. Prepare and execute the T-GQL webapp</h4>
##### a. Client configuration
> You can find the _client-1.0.jar_ file into the __utils__ folder. Alternatively, you can compile and produce it (tdbg-client). 

> The client jar reads a configuration file, ```client.properties``` which can also be found in the __utils__ folder. The Neo4j password you are using must be included in this file.

__Example of client.properties__
```properties
database.bolt.host=localhost
database.bolt.port=7687
database.username=neo4j
database.password=neo4j
webapp.port = 7000
socialNetwork.persons=50
socialNetwork.maxFriendships=5
socialNetwork.maxFriendshipIntervals=2
socialNetwork.cityCount=5
socialNetwork.brandCount=2
socialNetwork.maxFans=2
socialNetwork.maxFansIntervals=1
socialNetwork.cPath.numberOfPaths=2,1
socialNetwork.cPath.minLength=5,10
flights.cityCount=10
flights.outgoingFlightsPerAirport=3
flights.flightsPerDestination=3
```
##### b. Execute webapp

From terminal:

```bash
java -jar client-1.0.jar --config ./client.properties
```
Then open a web browser:  <localhost:7000>.

From IDE:
Use  `Main.main()` as access point. The app will connect to <localhost:7000>.

### <h4 id=run>4. Run queries</h4>

The following are query examples that you can copy & paste into the webapp and test. Please, make sure to use the appropiate database for each case.

#####  Queries for Social Network Example
```dbms.active_database=soc_net.db```

<code>SELECT p2.Name
MATCH (p1:Person) -[:Friend]-> (p2:Person)
WHERE p1.Name = 'Wei Xu'
SNAPSHOT '2020' </code>

<code>SELECT d, p2.Name, c.Name
MATCH (p1:Person) -[:Friend]-> (p2:Person),
      (p2)-[d:LivedIn]->(c:City)
WHERE p1.Name = 'Kea Sanders-Upcott'
BETWEEN '2015' AND '2017'</code>

<code>SELECT p2.Name as friend_name
MATCH (p1:Person) -[:Friend]-> (p2:Person)
WHERE p1.Name = 'Kea Sanders-Upcott'
WHEN
MATCH (p1)-[d:LivedIn]->(c:City)                                
WHERE c.Name = 'Buenos Aires' </code>

<code>SELECT paths.path,paths.intervals, paths.alphas
MATCH (p1:Person), (p2:Person),  
      paths=alphaPath((p1) - [f:Friend*4] -> (p2), '2015', '2021')
WHERE p1.Name = 'Kea Sanders-Upcott'
AND p2.Name = 'Luca Mori'</code>

<code>SELECT paths,paths.interval
MATCH (p1:Person), (p2:Person),  paths=cPath((p1) - [f:Friend*3..4] -> (p2))
WHERE p1.Name = 'Kea Sanders-Upcott'
AND p2.Name = 'Luca Mori'</code>

<code>SELECT p1.Name
MATCH (p1:Person), (p2:Person)
WHERE p2.Name = 'Peter Norton'
AND cPath((p1) -[:Friend*2..3]-> (p2))</code>

##### Queries for the Flights Example

```dbms.active_database=Flights.db```

<code>SELECT path
MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),
    (c2:City)<-[:LocatedAt]-(a2:Airport),
  path = fastestPath((a1)-[:Flight*]->(a2))                                 
WHERE c1.Name = 'London'
AND c2.Name = 'Bariloche'</code>

<code>SELECT path
MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),
    (c2:City)<-[:LocatedAt]-(a2:Airport),
  path = latestDeparturePath((a1)-[:Flight*]->(a2),'2024-07-08 20:00')
WHERE c1.Name = 'London'
AND c2.Name = 'Bariloche'</code>


##### Queries for the Sensor Network 

```dbms.active_database=schelde_G10.db```

<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) <- [f:flowsTo*4..4] - (p2),'2022-04-01 23:00','2022-04-02 11:00')
WHERE p1.Name = 'zes01a-SF-1066'
  AND ALL p.attribute.ec=2</code>

Figure 6.12: April 10th, 2022 from 03:00 to 16:00

<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) <- [f:flowsTo*4..4] - (p2),'2022-04-10 03:00','2022-04-10 16:00')
WHERE p1.Name = 'zes01a-SF-1066'
  AND ALL p.attribute.ec=2 </code>


A similar query but starting from station _zes09x-SF-1066_

<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) - [f:flowsTo*4..4] -> (p2),'2022-04-10 03:00','2022-04-10 16:00')
WHERE p1.Name = 'zes09x-SF-1066'
  AND ALL p.attribute.ec=2</code>

Please note the edge direction


```dbms.active_database=schelde_G60.db```

<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) <- [f:flowsTo*3] - (p2),'2022-04-01 23:00','2022-04-02 11:00')
WHERE p1.Name = 'zes07g-SF-O-1066'
AND ALL p.attribute.ec=2
EXCLUDE 2</code>


<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) <- [f:flowsTo*4..8] - (p2),'2022-04-01 23:00','2022-04-02 11:00')
WHERE p1.Name = 'zes01a-SF-1066'
AND ALL p.attribute.ec=2</code>

Sensor Network continuous path in G60
<code>SELECT p.path, p.interval
MATCH (p1:Sensor), (p2:Sensor),  p=SNcPath((p1) <- [f:flowsTo*4..6] - (p2),'2022-04-01 23:00','2022-04-02 11:00')
WHERE p1.Name = 'zes01a-SF-1066'
  AND ALL p.attribute.ec=2</code>

```dbms.active_database=schelde_L60.db```

Figure 6.11 
<code>SELECT p.path, p.intervals, p.alphas
MATCH (p1:Sensor), (p2:Sensor),  p=SNalphaPath((p1) <- [f:flowsTo*8..8] - (p2),'2022-04-01 23:00','2022-04-02 11:00')
WHERE p1.Name = 'zes01a-SF-1066'
AND ALL p.attribute.ec=2</code>
