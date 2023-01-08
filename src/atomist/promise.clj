(ns atomist.promise
  (:require [skills.v2.log :as log]
            [clojure.core.async :refer [go <!]]
            [promesa.core :as p]))

(defn chan->promise
  "convert a channel into a Promise
     - value of Promise can not be nil (Promise reject if chan emits a nil)"
  [chan]
  (p/create
   (fn [accept reject]
     (go
       (try
         (let [v (<! chan)]
           (if v
             (accept v)
             (do
               (log/warn "reject nil on chan->promise")
               (reject {:fail "nil value on channel"}))))
         (catch :default t
           (log/error t " js Promise will reject")
           (log/error chan)
           (reject {:fail "failure to process chan"
                    :error t})))))))
