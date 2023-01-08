(ns atomist.async
  (:require [clojure.core.async :as a]))

(defn throw-err "Throw if is error, will be different in ClojureScript"
  [v]
  (if (instance? Throwable v) (throw v) v))

(defmacro <? "Version of <! that throw Exceptions that come out of a channel."
  [c]
  `(throw-err (a/<! ~c)))

(defmacro err-or "If body throws an exception, catch it and return it"
  [& body]
  `(try
     ~@body
     (catch Throwable e#
       e#)))

(defmacro go-safe [& body]
  `(a/go (err-or ~@body)))

