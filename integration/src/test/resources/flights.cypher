CREATE CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;
UNWIND [
{_id:20, properties:{interval:["1580—Now"], title:"Name"}},
{_id:21, properties:{interval:["1902—Now"], title:"Name"}},
{_id:22, properties:{interval:["1535—Now"], title:"Name"}},
{_id:23, properties:{interval:["1554—Now"], title:"Name"}},
{_id:24, properties:{interval:["43—Now"], title:"Name"}},
{_id:50, properties:{interval:["1949—Now"], title:"Name"}},
{_id:51, properties:{interval:["1947—Now"], title:"Name"}},
{_id:52, properties:{interval:["1954—Now"], title:"Name"}},
{_id:53, properties:{interval:["1965—Now"], title:"Name"}},
{_id:54, properties:{interval:["1985—Now"], title:"Name"}},
{_id:55, properties:{interval:["1930—Now"], title:"Name"}},
{_id:56, properties:{interval:["1946—Now"], title:"Name"}},
{_id:57, properties:{interval:["1966—Now"], title:"Name"}}
] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Attribute;
UNWIND [
{_id:10, properties:{interval:["1580—Now"], title:"City"}},
{_id:11, properties:{interval:["1902—Now"], title:"City"}},
{_id:12, properties:{interval:["1535—Now"], title:"City"}},
{_id:13, properties:{interval:["1554—Now"], title:"City"}},
{_id:14, properties:{interval:["43—Now"], title:"City"}},
{_id:40, properties:{interval:["1949—Now"], title:"Airport", id: 40}},
{_id:41, properties:{interval:["1947—Now"], title:"Airport", id: 41}},
{_id:42, properties:{interval:["1954—Now"], title:"Airport", id: 42}},
{_id:43, properties:{interval:["1965—Now"], title:"Airport", id: 43}},
{_id:44, properties:{interval:["1985—Now"], title:"Airport", id: 44}},
{_id:45, properties:{interval:["1930—Now"], title:"Airport", id: 45}},
{_id:46, properties:{interval:["1946—Now"], title:"Airport", id: 46}},
{_id:47, properties:{interval:["1966—Now"], title:"Airport", id: 47}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Object;
UNWIND [
{_id:30, properties:{interval:["1580—Now"], value:"Buenos Aires"}},
{_id:31, properties:{interval:["1902—Now"], value:"Bariloche"}},
{_id:32, properties:{interval:["1535—Now"], value:"Lima"}},
{_id:33, properties:{interval:["1554—Now"], value:"Sao Paulo"}},
{_id:34, properties:{interval:["43—Now"], value:"London"}},
{_id:60, properties:{interval:["1949—Now"], value:"EZE"}},
{_id:61, properties:{interval:["1947—Now"], value:"AEP"}},
{_id:62, properties:{interval:["1954—Now"], value:"BRC"}},
{_id:63, properties:{interval:["1965—Now"], value:"LIM"}},
{_id:64, properties:{interval:["1985—Now"], value:"GRU"}},
{_id:65, properties:{interval:["1930—Now"], value:"LGW"}},
{_id:66, properties:{interval:["1946—Now"], value:"LHR"}},
{_id:67, properties:{interval:["1966—Now"], value:"STN"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Value;
UNWIND [
{start: {_id:40}, end: {_id:10}, properties:{interval:["1949—Now"]}},
{start: {_id:41}, end: {_id:10}, properties:{interval:["1947—Now"]}},
{start: {_id:42}, end: {_id:11}, properties:{interval:["1954—Now"]}},
{start: {_id:43}, end: {_id:12}, properties:{interval:["1965—Now"]}},
{start: {_id:44}, end: {_id:13}, properties:{interval:["1985—Now"]}},
{start: {_id:45}, end: {_id:14}, properties:{interval:["1930—Now"]}},
{start: {_id:46}, end: {_id:14}, properties:{interval:["1946—Now"]}},
{start: {_id:47}, end: {_id:14}, properties:{interval:["1966—Now"]}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:LocatedAt]->(end) SET r += row.properties;
//Object a Attribute
UNWIND [
{start: {_id:10}, end: {_id:20}, properties:{}},
{start: {_id:11}, end: {_id:21}, properties:{}},
{start: {_id:12}, end: {_id:22}, properties:{}},
{start: {_id:13}, end: {_id:23}, properties:{}},
{start: {_id:14}, end: {_id:24}, properties:{}},
{start: {_id:40}, end: {_id:50}, properties:{}},
{start: {_id:41}, end: {_id:51}, properties:{}},
{start: {_id:42}, end: {_id:52}, properties:{}},
{start: {_id:43}, end: {_id:53}, properties:{}},
{start: {_id:44}, end: {_id:54}, properties:{}},
{start: {_id:45}, end: {_id:55}, properties:{}},
{start: {_id:46}, end: {_id:56}, properties:{}},
{start: {_id:47}, end: {_id:57}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
UNWIND [
{start: {_id:40}, end: {_id:46}, properties:{interval:["2020-03-07 14:25—2020-03-08 06:35"], flightNumber:["BA244"]}},
{start: {_id:46}, end: {_id:40}, properties:{interval:["2020-03-07 22:30—2020-03-08 09:10"], flightNumber:["BA245"]}},
{start: {_id:40}, end: {_id:45}, properties:{interval:["2020-03-07 11:30—2020-03-08 03:30"], flightNumber:["DI7506"]}},
{start: {_id:45}, end: {_id:40}, properties:{interval:["2020-03-07 23:15—2020-03-08 08:05"], flightNumber:["DI7505"]}},
{start: {_id:40}, end: {_id:43}, properties:{interval:["2020-03-07 20:12—2020-03-07 22:59"], flightNumber:["LP2468"]}},
{start: {_id:43}, end: {_id:40}, properties:{interval:["2020-03-07 12:44—2020-03-07 19:03"], flightNumber:["LP2429"]}},
{start: {_id:40}, end: {_id:44}, properties:{interval:["2020-03-07 16:05—2020-03-07 18:25"], flightNumber:["AR1286"]}},
{start: {_id:44}, end: {_id:40}, properties:{interval:["2020-03-07 11:10—2020-03-07 14:15"], flightNumber:["JJ8003"]}},
{start: {_id:43}, end: {_id:45}, properties:{interval:["2020-03-07 21:35—2020-03-08 15:55"], flightNumber:["BA2238"]}},
{start: {_id:45}, end: {_id:43}, properties:{interval:["2020-03-07 23:35—2020-03-08 10:30"], flightNumber:["BA2239"]}},
{start: {_id:44}, end: {_id:46}, properties:{interval:["2020-03-07 15:30—2020-03-08 06:55"], flightNumber:["BA246"]}},
{start: {_id:46}, end: {_id:44}, properties:{interval:["2020-03-07 21:00—2020-03-08 05:00"], flightNumber:["BA247"]}},
{start: {_id:40}, end: {_id:42}, properties:{interval:["2020-03-08 13:00—2020-03-08 15:25"], flightNumber:["AR1678"]}},
{start: {_id:42}, end: {_id:40}, properties:{interval:["2020-03-07 09:30—2020-03-07 11:35"], flightNumber:["AR1873"]}},
{start: {_id:42}, end: {_id:44}, properties:{interval:["2020-03-07 17:00—2020-03-07 21:35"], flightNumber:["AR2020"]}},
{start: {_id:44}, end: {_id:42}, properties:{interval:["2020-03-08 10:00—2020-03-08 14:35"], flightNumber:["AR2021"]}}
] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Flight]->(end) SET r += row.properties;
UNWIND [
{start: {_id:20}, end: {_id:30}, properties:{}},
{start: {_id:21}, end: {_id:31}, properties:{}},
{start: {_id:22}, end: {_id:32}, properties:{}},
{start: {_id:23}, end: {_id:33}, properties:{}},
{start: {_id:24}, end: {_id:34}, properties:{}},
{start: {_id:50}, end: {_id:60}, properties:{}},
{start: {_id:51}, end: {_id:61}, properties:{}},
{start: {_id:52}, end: {_id:62}, properties:{}},
{start: {_id:53}, end: {_id:63}, properties:{}},
{start: {_id:54}, end: {_id:64}, properties:{}},
{start: {_id:55}, end: {_id:65}, properties:{}},
{start: {_id:56}, end: {_id:66}, properties:{}},
{start: {_id:57}, end: {_id:67}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
MATCH (n:`UNIQUE IMPORT LABEL`)  WITH n LIMIT 20000 REMOVE n:`UNIQUE IMPORT LABEL` REMOVE n.`UNIQUE IMPORT ID`;
DROP CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;
