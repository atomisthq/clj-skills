(ns skills.pod
  (:require [babashka.pods.impl :as impl]))

(defn load-pod
  ([pod-spec] (load-pod pod-spec nil))
  ([pod-spec version opts] (load-pod pod-spec (assoc opts :version version)))
  ([pod-spec opts]
   (let [opts (if (string? opts)
                {:version opts}
                opts)
         pod (impl/load-pod
              pod-spec
              (merge {:remove-ns remove-ns
                      :resolve (fn [sym]
                                 (or (resolve sym)
                                     (intern
                                      (create-ns (symbol (namespace sym)))
                                      (symbol (name sym)))))}
                     opts))]
     (future (impl/processor pod))
     {:pod/id (:pod-id pod)})))

(defn init []
  (load-pod 'atomisthq/tools.docker "0.1.0"))

(defn hashes [s cb]
  (impl/invoke-public
   "pod.atomisthq.docker"
   'pod.atomisthq.docker/-generate-hashes
   [s]
   {:handlers {:success (fn [event]
                          (cb event))
               :error   (fn [{:keys [:ex-message :ex-data]}]
                          (binding [*out* *err*]
                            (println "ERROR:" ex-message)))
               :done    (fn [] (cb {:status "done"}))}}))

(comment
  (init)
  (impl/lookup-pod "pod.atomisthq.docker")
  (hashes "vonwig/malware1:latest" (fn [event] (println event)))
  )

