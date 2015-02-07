# Foundation/PG

A clojure data access library for postgresql.


Status: Work in Progress, hardly anything functional.



## Goals
  Easy to use correctly
  Support postgresql extended datatypes (eg arrays, json, hstore, gis)


## Features
- automatic conversion from dash to underscore and back
- casing is ignored, rather than automatically being lower cased.
- automatic conversion from/to java.time.Instant




## Explicitly Not Included

Support for other databases such as MySql, MS Sql, Oracle, etc.

Data definition sql and schema migrations. We believe that these concerns should
be handled outside of the data layer of your application and no plans to include
these into this library.










## License

Copyright © 2014 Michael Ball

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
