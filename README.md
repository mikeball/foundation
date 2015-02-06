# Foundation/PG

A clojure data access library for postgresql.


Status: Work in Progress, hardly anything functional.




## Goals
  Focus on postgresql support only
  Easy to use correctly
  Support extended postgresql datatypes (eg arrays and json)


## Features
- automatic conversion from dash to underscore and back
- upper/lower casing is ignored, rather than automatically being lower cased.
- automatic conversion of java.time.Instant to java.sql.timestamp





## TODO/Questions

- unit test with-rs
- sort out return from inserts that don't have generated keys
- handle insert of sequence of maps
- handle vector based insert format
- delete with where clause
- update with where clause








## Explicitly Not Included
Data definition sql and schema migrations. We believe that these concerns should
be handled outside of the data layer of your application and no plans to include
these into this library.





## License

Copyright © 2014 Michael Ball

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
