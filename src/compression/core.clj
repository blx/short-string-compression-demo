(ns compression.core
  (:gen-class)
  (:import
    [java.awt Color]
    [java.io ByteArrayOutputStream]
    [java.util Base64]
    [java.util.zip GZIPOutputStream DeflaterOutputStream]
    [org.jfree.chart.plot ValueMarker])
  (:require
    [clojure.edn :as edn]
    [incanter.core :as i]
    [incanter.charts :as c]))

(def comments
  (edn/read-string (slurp "resources/comments.edn")))

(def encoding "UTF-8")

(defn gzip [s]
  (with-open [baos (ByteArrayOutputStream.)
              os (GZIPOutputStream. baos)]
    (.write os (.getBytes s encoding))
    (.finish os)
    (.toByteArray baos)))

(defn deflate [s]
  (with-open [baos (ByteArrayOutputStream.)
              os (DeflaterOutputStream. baos)]
    (.write os (.getBytes s encoding))
    (.finish os)
    (.toByteArray baos)))

(defn factor [compressor s]
  (float
    (/ (count (compressor s))
       (count (.getBytes s)))))

(defn scale [n strings]
  (->> strings
       (partition n)
       (map (partial interpose " "))
       (map (partial apply str))))

(defn juxtmap
  "Maps the fs in a stripe across coll.
  Equivalent to [(map f1 coll) (map f2 coll) ...]"
  [coll & fs]
  ((apply juxt (map #(partial map %) fs)) coll))

(defn lens-facs [strings compressor]
  (->> (range 3 25)
       (mapcat #(scale % strings))
       (#(juxtmap % count (partial factor compressor)))))

(defn scatter
  ([strings] (scatter strings gzip))
  ([strings compressor]
   (let [[lengths factors] (lens-facs strings compressor)
         chart (c/line-chart lengths factors
                               :title "String size, compressed vs uncompressed"
                               :x-label "bytes(original)"
                               :y-label "bytes(compressed) / bytes(original)")]
     (.addRangeMarker (.getPlot chart) (doto (ValueMarker. 1.0)
                                         (.setPaint Color/black)))
     (i/view chart :width 900 :height 600))))

(defn scatter'
  [strings compressors labels]
  (let [strings (->> (range 3 25)
                     (mapcat #(scale % strings)))
        lengths (map count strings)
        chart (c/scatter-plot lengths (map (partial factor (first compressors)) strings)
                              :title "String size, compressed vs uncompressed"
                              :x-label "bytes(original)"
                              :y-label "bytes(compressed) / bytes(original)"
                              :legend true
                              :series-label (first labels))
        point-size 3]
    (doseq [[i c] (map-indexed vector (rest compressors))]
      (doto chart
        (c/add-points lengths (map (partial factor c) strings)
                      :series-label (nth (rest labels) i))))
    (doseq [i (range (count compressors))]
      (c/set-point-size chart point-size :dataset i))
    (.addRangeMarker (.getPlot chart) (doto (ValueMarker. 1.0)
                                        (.setPaint Color/black)))
    (i/view chart :width 1100 :height 900)))

;(defn -main
;  [& args]
;  )
