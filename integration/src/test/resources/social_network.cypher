CREATE CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;
UNWIND [{_id:20, properties:{interval:["1960—Now"], title:"Name"}}, {_id:21, properties:{interval:["1960—Now"], title:"Name"}}, {_id:22, properties:{interval:["1960—Now"], title:"Name"}}, {_id:23, properties:{interval:["1960—Now"], title:"Name"}}, {_id:24, properties:{interval:["1937—Now"], title:"Name"}}, {_id:26, properties:{interval:["1995—Now"], title:"Name"}}, {_id:50, properties:{interval:["1977—Now"], title:"Name"}}, {_id:51, properties:{interval:["1995—Now"], title:"Name"}}, {_id:52, properties:{interval:["1960—Now"], title:"Name"}}, {_id:53, properties:{interval:["1960—Now"], title:"Name"}}, {_id:54, properties:{interval:["1960—Now"], title:"Name"}},{_id:80, properties:{interval:["1938—Now"], title:"Name"}}, {_id:81, properties:{interval:["1958—Now"], title:"Name"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Attribute;
UNWIND [{_id:40, properties:{interval:["1900—Now"], title:"City"}}, {_id:41, properties:{interval:["1900—Now"], title:"City"}}, {_id:42, properties:{interval:["1900—Now"], title:"City"}}, {_id:43, properties:{interval:["1900—Now"], title:"City"}}, {_id:44, properties:{interval:["1900—Now"], title:"City"}}, {_id:10, properties:{interval:["1967—Now"], title:"Person"}},{_id:11, properties:{interval:["1978—Now"], title:"Person"}}, {_id:12, properties:{interval:["1980—Now"], title:"Person"}}, {_id:13, properties:{interval:["1961—Now"], title:"Person"}}, {_id:14, properties:{interval:["1937—Now"], title:"Person"}},{_id:16, properties:{interval:["1995—Now"], title:"Person"}},{_id:70, properties:{interval:["1938—Now"], title:"Brand"}}, {_id:71, properties:{interval:["1958—Now"], title:"Brand"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Object;
UNWIND [{_id:60, properties:{interval:["1960—Now"], value:"New York"}}, {_id:61, properties:{interval:["1960—Now"], value:"Brussels"}}, {_id:62, properties:{interval:["1960—Now"], value:"Paris"}}, {_id:63, properties:{interval:["1960—Now"], value:"London"}}, {_id:64, properties:{interval:["1960—Now"], value:"Antwerp"}}, {_id:30, properties:{interval:["1967—Now"], value:"Sandra Carter"}}, {_id:31, properties:{interval:["1978—Now"], value:"Pauline Boutler"}}, {_id:32, properties:{interval:["1961—Now"], value:"Cathy Van Bourne"}}, {_id:33, properties:{interval:["1960—Now"], value:"Peter Burton"}}, {_id:34, properties:{interval:["1937—1959"], value:"Mary Smith"}}, {_id:35, properties:{interval:["1960—Now"], value:"Mary Smith-Taylor"}}, {_id:36, properties:{interval:["1995—Now"], value:"Daniel Yang"}},{_id:90, properties:{interval:["1938—Now"], value:"Samsung"}},{_id:91, properties:{interval:["1958—Now"], value:"LG"}}] AS row
CREATE (n:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row._id}) SET n += row.properties SET n:Value;
//Creo person-name-value
UNWIND [{start: {_id:10}, end: {_id:20}, properties:{}}, {start: {_id:11}, end: {_id:21}, properties:{}}, {start: {_id:12}, end: {_id:22}, properties:{}}, {start: {_id:13}, end: {_id:23}, properties:{}}, {start: {_id:14}, end: {_id:24}, properties:{}}, {start: {_id:16}, end: {_id:26}, properties:{}}, {start: {_id:20}, end: {_id:30}, properties:{}}, {start: {_id:21}, end: {_id:31}, properties:{}}, {start: {_id:22}, end: {_id:32}, properties:{}}, {start: {_id:23}, end: {_id:33}, properties:{}}, {start: {_id:24}, end: {_id:34}, properties:{}}, {start: {_id:24}, end: {_id:35}, properties:{}}, {start: {_id:26}, end: {_id:36}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
//Creo city-name-value
UNWIND [{start: {_id:40}, end: {_id:50}, properties:{}}, {start: {_id:41}, end: {_id:51}, properties:{}}, {start: {_id:42}, end: {_id:52}, properties:{}}, {start: {_id:43}, end: {_id:53}, properties:{}}, {start: {_id:44}, end: {_id:54}, properties:{}}, {start: {_id:50}, end: {_id:60}, properties:{}}, {start: {_id:51}, end: {_id:61}, properties:{}}, {start: {_id:52}, end: {_id:62}, properties:{}}, {start: {_id:53}, end: {_id:63}, properties:{}}, {start: {_id:54}, end: {_id:64}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
//Creo Brand-name-value
UNWIND [{start: {_id:70}, end: {_id:80}, properties:{}}, {start: {_id:71}, end: {_id:81}, properties:{}}, {start: {_id:80}, end: {_id:90}, properties:{}}, {start: {_id:81}, end: {_id:91}, properties:{}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Edge]->(end) SET r += row.properties;
//Creo Friend
UNWIND [{start: {_id:11}, end: {_id:10}, properties:{interval:["2005—Now"]}}, {start: {_id:11}, end: {_id:12}, properties:{interval:["2002—2017"]}}, {start: {_id:14}, end: {_id:11}, properties:{interval:["2010—2018"]}}, {start: {_id:14}, end: {_id:13}, properties:{interval:["1993—Now"]}}, {start: {_id:12}, end: {_id:13}, properties:{interval:["1995—Now"]}}, {start: {_id:13}, end: {_id:16}, properties:{interval:["2015—2018"]}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Friend]->(end) SET r += row.properties;
//Creo LivedIn
UNWIND [{start: {_id:10}, end: {_id:40}, properties:{interval:["1967—Now"]}}, {start: {_id:11}, end: {_id:41}, properties:{interval:["1978—2003"]}}, {start: {_id:11}, end: {_id:43}, properties:{interval:["2004—Now"]}}, {start: {_id:12}, end: {_id:41}, properties:{interval:["1980—2000"]}}, {start: {_id:12}, end: {_id:42}, properties:{interval:["2001—Now"]}}, {start: {_id:13}, end: {_id:40}, properties:{interval:["1961—Now"]}}, {start: {_id:14}, end: {_id:41}, properties:{interval:["1979—1989"]}}, {start: {_id:14}, end: {_id:44}, properties:{interval:["1990—Now"]}}, {start: {_id:16}, end: {_id:64}, properties:{interval:["1995—Now"]}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:LivedIn]->(end) SET r += row.properties;
//Creo LivedIn
UNWIND [{start: {_id:11}, end: {_id:70}, properties:{interval:["2005—2008"]}}, {start: {_id:10}, end: {_id:70}, properties:{interval:["2001—Now"]}}, {start: {_id:10}, end: {_id:71}, properties:{interval:["1995—2000"]}}, {start: {_id:14}, end: {_id:70}, properties:{interval:["1982—Now"]}}, {start: {_id:12}, end: {_id:71}, properties:{interval:["1998—Now"]}}] AS row
MATCH (start:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.start._id})
MATCH (end:`UNIQUE IMPORT LABEL`{`UNIQUE IMPORT ID`: row.end._id})
CREATE (start)-[r:Fan]->(end) SET r += row.properties;
MATCH (n:`UNIQUE IMPORT LABEL`)  WITH n LIMIT 20000 REMOVE n:`UNIQUE IMPORT LABEL` REMOVE n.`UNIQUE IMPORT ID`;
DROP CONSTRAINT ON (node:`UNIQUE IMPORT LABEL`) ASSERT (node.`UNIQUE IMPORT ID`) IS UNIQUE;
