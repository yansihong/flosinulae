; 生成邮件反馈需要的json注册数据
;
(ns flosinulae.json
  (:import (java.util.zip ZipFile ZipEntry))
  (require [clojure.data.json :as json]))

(defn show-content
  [zip-file zip-entry]
  (let[ name (.getName zip-entry)
        is (.getInputStream zip-file zip-entry)
        count (.available is)
        buffer (make-array Byte/TYPE count)
        size (.read is buffer)]
    (String. buffer "UTF-8")))

(defn read-zip-file
  [zip-file-name]
  (with-open [zip-file (ZipFile. zip-file-name)]
    (let [entries (.entries zip-file)]
      (loop [jf []]
        (let [zip-entry (.nextElement entries)]
          (if (.hasMoreElements entries)
            (recur (conj jf {:name (.getName zip-entry) :value (show-content zip-file zip-entry)}))
            jf))))))

(defn create-http-json
  [url]
  {:http {:authenticate "" :username "" :type "json" :password "" :url url :version ""}})

(defn create-mq-json
  [queue]
  {:mq {:queue queue :username "" :password "" :exchange ""}})

(defn build-cust-json-text
  "创建cust json格式数据; filename为zip文件名，其中包含js代码；custcode为客户代码；queue为队列名；url为客户接收端URL地址"
  ([] {:description "" :time (. java.lang.System currentTimeMillis)})
  ([filename] (conj (build-cust-json-text) {:funtions (read-zip-file filename)}))
  ([filename custcode]
    (conj (build-cust-json-text filename) {:custcode custcode}))
  ([filename custcode queue]
    (conj (build-cust-json-text filename custcode) (create-mq-json queue)))
  ([filename custcode queue url]
    (conj (build-cust-json-text filename custcode queue) (create-http-json url))))

(println  (json/write-str(build-cust-json-text "E:/temp/099812391723822.zip" "asdfasdf" "asdf" "http://xxxx:80/mpa/Service/y.do")))

