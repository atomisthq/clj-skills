(ns skills.v2.log
  (:require [skills.v2 :as v2]
            [atomist.time :as time]))

;; TODO this only works if the trigger can be a global binding and we're not processing concurrent requests
(def trigger)
(defn set-trigger [r]
  (alter-var-root #'trigger (constantly r)))

(defn log [level s & args]
  (let [t (time/now-iso-8601)
        message (apply str s args)]
    (println (format "%-25s %-10s %s" t level message))
    (try
      (v2/execution-logs
       trigger {:logs [{:text message
                        :timestamp t
                        :level level}]})
      (catch Throwable ex
        (println ex)))))
(defn logf [level f & args]
  (log level (apply format f args)))

(def infof (partial logf :info))
(def debugf (partial logf :debug))
(def errorf (partial logf :error))
(def warnf (partial logf :warn))
(def info (partial log :info))
(def warn (partial log :warn))
(def debug (partial log :debug))
(def error (partial log :error))
