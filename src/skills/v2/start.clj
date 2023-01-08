(ns skills.v2.start
  (:require [clojure.edn :as edn]
            [atomist.time :as time]
            [promesa.core :as p]
            [skills.v2 :as v2]
            [skills.v2.log :as log]
            [org.httpkit.server :as srv]))

(defn app [handler {:keys [request-method uri body]}]
  (case [request-method uri]
    [:post "/"] (let [start (time/now)
                      payload (edn/read-string (slurp body))]
                  (log/set-trigger payload)
                  (deref
                   (p/catch
                    (p/then
                     (handler payload)
                     (fn [r]
                       (v2/execution-patch
                        payload
                        {:status (or (:atomist/status r) {:state :failed :reason "missing status"})})
                       (println (format "execution took %d ms, finished with status: 'ok'" (- (time/now) start)))
                       {:status 201}))
                    (fn [err]
                      (v2/execution-patch
                       payload
                       {:status {:state :failed :reason (str "handler failure " err)}})
                      {:status 201}))))))

(defn start-http-listener
  "start a cloud-run handler
     params
       handler - (param) => Promise"
  [handler]
  (srv/run-server (partial #'app handler) {:port 8080}))
