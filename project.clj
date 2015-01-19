(defproject flosinulae "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/yansihong/flosinulae"
  :license {:name "-"
            :url "-"}
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.clojure/data.json "0.2.5"]
    [javax.jms/javax.jms-api "2.0"]
    [log4j/log4j "1.2.16" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
    [org.clojure/tools.logging "0.2.4"]])
