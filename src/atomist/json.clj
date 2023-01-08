(ns atomist.json
  (:require [cheshire.core :as cheshire]))

(defn ->obj 
  [s & {:keys [keywordize-keys]}]
  (cheshire/parse-string s keywordize-keys))

(defn ->str 
  [obj & {:keys [keyword-fn] :or {keyword-fn name}}]
  (cheshire/generate-string obj {:key-fn keyword-fn}))

