# TODO/Questions


- change placeholder character to $
    https://github.com/krisajenkins/yesql/issues/79



- polish sections for dynamic where clauses...
    https://github.com/krisajenkins/yesql/issues/13



- publish to clojars
- confirm functionality on clean machine
- locate some testers




- solidify batch insert/update syntax
    https://github.com/krisajenkins/yesql/issues/51


- research reduce based system...
    http://dev.clojure.org/jira/browse/JDBC-99
    https://gist.github.com/ghadishayban/d2f31961deba98ee4595


- ability to easily include foreign key related records.


- reorganize/combine reading + writing namespaces into one?
- sort out return from inserts that don't have generated keys (user-roles)


- dynamic queries ... dynamic sections? honeysql/etc?
  perhaps just a datastructure dsl just for where clause? orderby?


- deal with the new question mark operators...
  see: http://postgresql.nabble.com/Problems-with-question-marks-in-operators-JDBC-ECPG-td5849461.html
  Probably need to extract any question marks from a query, put a placeholder for it, then perform standard replacement, then replace question mark placeholders with the relevant JDBC escaped version of the question mark




## References

https://github.com/yeller/clojure-miniprofiler
https://github.com/yeller/clojure-miniprofiler-liza


https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/clojure/core.clj#L5244
https://github.com/clojure/java.jdbc/blob/master/src/main/clojure/clojure/java/jdbc.clj#L362

http://clojure-doc.org/articles/ecosystem/java_jdbc/using_sql.html
http://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html
http://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html#prepareStatement-java.lang.String-int-




