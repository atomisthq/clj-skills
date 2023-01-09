(ns atomist.github
  (:require [atomist.async :refer [go-safe]]
            [clojure.spec.alpha :as spec]
            [skills.v2.log :as log]
            [babashka.curl :as curl]
            [atomist.json :as json]))

(spec/def :checkrun.action/label string?)
(spec/def :checkrun.action/description string?)
(spec/def :checkrun.action/identifier string?)
(spec/def :checkrun-output/title string?)

(spec/def :checkrun-output/summary string?)
(spec/def :checkrun-output/text string?)
(spec/def :checkrun-output/annotation any?)
(spec/def :checkrun-output/annotations (spec/coll-of :checkrun-output/annotation))
(spec/def :checkrun/output (spec/keys :req-un [:checkrun-output/title :checkrun-output/summary]
                                      :opt-un [:checkrun-output/text :checkrun-output/annotations]))
(spec/def :checkrun/conclusion #{"success" "failure" "neutral" "cancelled" "skipped" "timed_out" "action_required"})
(spec/def :checkrun/status #{"queued" "in_progress" "completed"})
(spec/def :checkrun/head_sha string?)
(spec/def :checkrun/name string?)
(spec/def :checkrun/action (spec/keys :req-un [:checkrun.action/label :checkrun.action/description :checkrun.action/identifier]))
(spec/def :checkrun/actions (spec/coll-of :checkrun/action))
(spec/def :checkrun/check-run (spec/keys :req-un [:checkrun/name :checkrun/head_sha]
                                         :opt-un [:checkrun/status :checkrun/conclusion :checkrun/output :checkrun/actions]))

(defn create-check [request owner repo parameters]
  (go-safe
    (log/info "parameters valid? " (spec/valid? :checkrun/check-run parameters))
    (let [response (curl/post (format "https://api.github.com/repos/%s/%s/check-runs" owner repo)
                              {:headers {"Authorization" (format "bearer %s" (:token request))
                                         "User-Agent" "atomist"
                                         "Accept" "application/vnd.github.antiope-preview+json"}
                               :body (json/->str parameters)})]
      response)))
