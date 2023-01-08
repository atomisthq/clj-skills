(ns skills.v2
  (:require [clojure.spec.alpha :as s]
            [babashka.curl :as http]
            [atomist.time]))

(defn- iso-8601? [s]
  (atomist.time/now-iso-8601? s))

(s/def ::value any?)
(s/def ::result any?)
(s/def ::name string?)
(s/def ::timestamp (s/and string? iso-8601?))
(s/def ::text string?)

(s/def ::execution-trigger (s/keys :req-un [::execution-id ::skill ::workspace-id ::type ::context ::urls ::token]))
(s/def ::execution-id string?)
(s/def ::token string?)
(s/def ::workspace-id string?)
(s/def ::skill (s/keys :req-un [::namespace ::name ::version]))
(s/def ::name string?)
(s/def ::namespace string?)
(s/def ::version string?)
(s/def ::type #{:subscription :webhook :command})
(s/def ::urls (s/keys :req-un [::execution ::logs ::transactions ::query]))
(s/def ::execution string?)
(s/def ::logs string?)
(s/def ::transactions string?)
(s/def ::query string?)
(s/def ::context (s/or :subscription :atomist.api.v2.context/subscription :webhook :atomist.api.v2.context/webhook))
(s/def :atomist.api.v2.context/subscription (s/keys :req-un [:atomist.api.v2.context.subscription/subscription]))
(s/def ::subscription-context (s/keys :req-un [::name ::configuration ::result ::metadata]))
(s/def :atomist.api.v2.context.subscription/subscription ::subscription-context)
(s/def ::configuration (s/keys :req-un [::name] :opt-un [::capabilities ::parameters]))
(s/def ::metadata (s/keys :req-un [::after-basis-t ::tx] :opt-un [::schedule-name]))
(s/def ::after-basis-t number?)
(s/def ::tx number?)
(s/def ::schedule-name string?)
(s/def ::capabilities (s/coll-of ::capability-spec))
(s/def ::capability-spec (s/keys :req-un [::name ::value]))
(s/def ::parameters (s/coll-of ::parameter))
(s/def ::parameter (s/keys :req-un [::name ::value]))
(s/def :atomist.api.v2.context/webhook (s/keys :req-un [:atomist.api.v2.context.webhook/webhook]))
(s/def ::webhook-context (s/keys :req-un [::name ::configuration :atomist.api.v2.webhook-context/request]))
(s/def :atomist.api.v2.context.webhook/webhook ::webhook-context)
(s/def :atomist.api.v2.webhook-context/request (s/keys :req-un [::url ::body ::headers ::tags]))
(s/def ::url string?)
(s/def ::body string?)
(s/def ::headers (s/map-of string? string?))
(s/def ::tags (s/coll-of ::tag))
(s/def ::tag (s/keys :req-un [::name :atomist.api.v2.tag/value]))
(s/def :atomist.api.v2.tag/value string?)

(s/def ::execution-patch (s/keys :req-un [::status]))
(s/def ::status (s/keys :req-un [::state ::reason]))
(s/def ::reason string?)
(s/def ::state #{:running :completed :failed :retryable})
(s/def ::execute-transactions (s/keys :req-un [:atomist.api.v2.execute-transactions/transactions]))
(s/def :atomist.api.v2.execute-transactions/transactions (s/coll-of ::transaction))
(s/def ::transaction (s/keys :req-un [::data] :opt-un [::ordering-key]))
(s/def ::ordering-key string?)
(s/def ::data (s/coll-of (s/map-of keyword? any?)))
(s/def ::execution-logs (s/keys :req-un [:atomist.api.v2.execution-logs/logs]))
(s/def :atomist.api.v2.execution-logs/logs (s/coll-of ::log))
(s/def ::log (s/keys :req-un [::timestamp ::level ::text]))
(s/def ::level #{:debug :info :warn :error})

(defn- validate [specs f]
  (fn [& args]
    (let [spec-validations (->>
                            (partition 2 (interleave specs args))
                            (map (fn [[spec arg]] (or (s/valid? spec arg) (s/explain-data spec arg)))))]
      (if (every? true? spec-validations)
        (apply f args)
        (throw (ex-info "invalid specs " {:specs specs :validations spec-validations}))))))

(defn- impl-execution-patch
  "args are already validated if invoked via exeuction patch
    trigger is a ::execution-patch and patch is an ::execution-patch"
  [trigger patch]
  (http/patch
   (-> trigger :urls :execution)
   {:headers {"Authorization" (str "Bearer " (-> trigger :token))
              "Content-Type" "application/edn"}
    :body (pr-str patch)}))

(defn- impl-execution-logs
  [trigger logs]
  (http/post
   (-> trigger :urls :logs)
   {:headers {"Authorization" (str "Bearer " (-> trigger :token))
              "Content-Type" "application/edn"}
    :body (pr-str logs)}))

(defn- impl-execute-transactions
  [trigger txs]
  (http/post
   (-> trigger :urls :transactions)
   {:headers {"Authorization" (str "Bearer " (-> trigger :token))
              "Content-Type" "application/edn"}
    :body (pr-str txs)}))

(def execution-patch
  (with-meta
    (validate
     [::execution-trigger ::execution-patch]
     impl-execution-patch)
    {:ns *ns*
     :name 'execution-patch
     :doc "change the status of this skill executions"}))

(def execute-transactions
  (with-meta
    (validate
     [::execution-trigger ::execute-transactions]
     impl-execute-transactions)
    {:ns *ns*
     :name 'execute-transactions
     :doc "execute data transactions"}))

(def execution-logs
  (with-meta
    (validate
     [::execution-trigger ::execution-logs]
     impl-execution-logs)
    {:ns *ns*
     :name 'execution-logs
     :doc "send skill logs"}))

