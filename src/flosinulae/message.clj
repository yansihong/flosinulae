(ns flosinulae.message
  ;(:gen-class :main true)
  (:import (java.io ByteArrayInputStream InputStream))
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.tools.logging :as log]
            [clojure-hbase.core :as hb]
            [clojure.string :as str]))

(def global-count (new java.util.concurrent.atomic.AtomicLong))

(defn- to-row-value [device-heart-map]
  (if device-heart-map
    (let [oper-time (device-heart-map :operationTime)
          phone (str (device-heart-map :simNo))
          rowkey (str (str/join (str/split oper-time #"\d{6}$"))
                      phone
                      (str/join(str/split oper-time #"^\d{8}")))]
      (assoc {} :rowkey rowkey :value device-heart-map))))

(defn- parse-message [input-byte-array]
  (with-open[is (ByteArrayInputStream. input-byte-array)]
    (let[root (xml/parse is)]
      (if (= :Heart (xml/tag root))
        (into {} (for[f (xml/content root)] [(xml/tag f) (first(xml/content f))]))
        ))))

(defn save-to-hbase [rowkey, values]
  (hb/with-table [pda (hb/table "ems_pda_log")]
    (hb/put pda rowkey :values [ :f (reduce into values)])))

(defn tibco-jms-receive [url queue username password f]
  (let[factory (new com.tibco.tibjms.TibjmsConnectionFactory url)
       connection (.createConnection factory username password)
       session (.createSession connection false javax.jms.Session/AUTO_ACKNOWLEDGE)
       destinatioin (.createQueue session queue)
       consumer (.createConsumer session destinatioin)]
      ((.start connection)
      (loop[]
        (let[message (.receive consumer)]
          (when message
            (if ( = 0 (rem (.incrementAndGet global-count) 1000))
              (log/info "Accepted messages count: " (.get global-count)))
            (let[count (.getBodyLength message)
                 buffer (make-array Byte/TYPE count)
                 read-count (.readBytes message buffer)]
              (try
                (if-let[m (to-row-value (f buffer))]
                  (save-to-hbase (m :rowkey) (m :value)))
                (catch Exception e (.printStackTrace e))
                (finally ()))
              (recur)))))
      (.close connection))))
