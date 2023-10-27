CREATE CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;
UNWIND [{_id:2, properties:{title:"Name"}}, {_id:5, properties:{title:"Name"}}, {_id:8, properties:{title:"Name"}}, {_id:11, properties:{title:"Name"}}, {_id:14, properties:{title:"Name"}}, {_id:17, properties:{title:"Name"}}, {_id:20, properties:{title:"Name"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Attribute;
UNWIND [{_id:0, properties:{interval:["0000—Now"], title:"City"}}, {_id:1, properties:{interval:["0000—Now"], title:"City"}}, {_id:4, properties:{interval:["0000—Now"], title:"Airport", id: 4}}, {_id:7, properties:{interval:["0000—Now"], title:"Airport", id: 7}}, {_id:10, properties:{interval:["0000—Now"], title:"Airport", id: 10}}, {_id:13, properties:{interval:["0000—Now"], title:"Airport", id: 13}}, {_id:16, properties:{interval:["0000—Now"], title:"Airport", id: 16}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Object;
UNWIND [{_id:3, properties:{interval:["0000—Now"], value:"City 2"}}, {_id:6, properties:{interval:["0000—Now"], value:"Airport 1"}}, {_id:9, properties:{interval:["0000—Now"], value:"Airport 2"}}, {_id:12, properties:{interval:["0000—Now"], value:"Airport 3"}}, {_id:15, properties:{interval:["0000—Now"], value:"Airport 4"}}, {_id:18, properties:{interval:["0000—Now"], value:"Airport 5"}}, {_id:21, properties:{interval:["0000—Now"], value:"City 1"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Value;
UNWIND [{start: {_id:4}, end: {_id:0}, properties:{}}, {start: {_id:7}, end: {_id:0}, properties:{}}, {start: {_id:10}, end: {_id:1}, properties:{}}, {start: {_id:13}, end: {_id:1}, properties:{}}, {start: {_id:16}, end: {_id:1}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:LocatedAt]->(end) SET r += row.properties;
UNWIND [{start: {_id:20}, end: {_id:21}, properties:{}}, {start: {_id:2}, end: {_id:3}, properties:{}}, {start: {_id:5}, end: {_id:6}, properties:{}}, {start: {_id:8}, end: {_id:9}, properties:{}}, {start: {_id:11}, end: {_id:12}, properties:{}}, {start: {_id:14}, end: {_id:15}, properties:{}}, {start: {_id:17}, end: {_id:18}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
UNWIND [{start: {_id:4}, end: {_id:13}, properties:{interval:["2000—2001"]}}, {start: {_id:4}, end: {_id:16}, properties:{interval:["2000—2001"]}}, {start: {_id:7}, end: {_id:10}, properties:{interval:["2000—2001"]}}, {start: {_id:7}, end: {_id:13}, properties:{interval:["2000—2001"]}}, {start: {_id:7}, end: {_id:16}, properties:{interval:["2000—2001"]}}, {start: {_id:4}, end: {_id:10}, properties:{interval:["2000—2001"]}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Flight]->(end) SET r += row.properties;
UNWIND [{start: {_id:0}, end: {_id:20}, properties:{}}, {start: {_id:1}, end: {_id:2}, properties:{}}, {start: {_id:4}, end: {_id:5}, properties:{}}, {start: {_id:7}, end: {_id:8}, properties:{}}, {start: {_id:10}, end: {_id:11}, properties:{}}, {start: {_id:13}, end: {_id:14}, properties:{}}, {start: {_id:16}, end: {_id:17}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
MATCH (n:`UNIQUE IMPORT LABEL`)  WITH n LIMIT 20000 REMOVE n:`UNIQUE IMPORT LABEL` REMOVE n.`UNIQUE IMPORT ID`;
DROP CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;