# TODO/Questions



- handle single colon for array syntax in templated queries

- json field update


- add mechanism to handle exceptions
  perhaps add ability to push exceptions onto core.async channel?
    as described on cognicast 87 / ANTHONY MARCAR


- sections for dynamic where clauses...
    https://github.com/krisajenkins/yesql/issues/13


- sort out return from inserts that don't have generated keys (user-roles)
    need to see what jdbc driver returns
    perhaps return nil? return both?


- dynamic queries ... dynamic sections? honeysql/etc?
  perhaps just a datastructure dsl just for where clause? orderby?
  perhaps just use "sections" to generate string?


- publish to clojars
- confirm functionality on clean machine


- research reduce based system...
    http://dev.clojure.org/jira/browse/JDBC-99
    https://gist.github.com/ghadishayban/d2f31961deba98ee4595








# Pending full jsonb driver support
  * see https://github.com/impossibl/pgjdbc-ng/issues/163

- handle jsonb inserts as full parameter * waiting on driver support for jsonb

- handle new question mark operators...
  see: http://postgresql.nabble.com/Problems-with-question-marks-in-operators-JDBC-ECPG-td5849461.html
  Probably need to extract any question marks from a query, put a placeholder for it, then perform standard replacement, then replace question mark placeholders with the relevant JDBC escaped version of the question mark


- perhaps check for keyword placeholders within json on parse?












## References

https://github.com/yeller/clojure-miniprofiler
https://github.com/yeller/clojure-miniprofiler-liza


https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/clojure/core.clj#L5244
https://github.com/clojure/java.jdbc/blob/master/src/main/clojure/clojure/java/jdbc.clj#L362

http://clojure-doc.org/articles/ecosystem/java_jdbc/using_sql.html
http://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html
http://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html#prepareStatement-java.lang.String-int-




