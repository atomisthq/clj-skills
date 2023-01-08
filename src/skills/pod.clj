(ns skills.pod
  (:require [babashka.pods :as pods]))

(defn init []
  (pods/load-pod 'atomisthq/tools.docker "0.1.0")
  (require '[pod.atomisthq.docker]))

(defn parse [s cb]
  (->> ((ns-resolve 'pod.atomisthq.docker 'parse-dockerfile) s cb)))

(defn hashes [s cb]
  (->> ((ns-resolve 'pod.atomisthq.docker 'hashes) s cb)))

(comment
  (init))


