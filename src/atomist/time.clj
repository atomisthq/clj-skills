(ns atomist.time
  (:require [java-time.api :as time]))

(defn now-iso-8601 []
  (time/format :iso-local-date-time (time/local-date-time)))

(defn now-iso-8601? [s]
  (try
    (time/local-date-time (time/formatter :iso-local-date-time) s)
    (catch Throwable _ 
      false)))

(defn now []
  (.toEpochMilli (.toInstant (time/zoned-date-time))))

