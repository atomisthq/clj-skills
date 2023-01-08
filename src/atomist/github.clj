(ns atomist.github
  (:require [clojure.core.async :refer [go]]))

(defn create-check [& args]
  (go "nothing"))
