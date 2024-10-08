(ns com.lambdaseq.stack.repl.core
  (:require [taoensso.timbre :as log]
            [nrepl.server :as nrepl]))

(defn start-nrepl!
  "Start a nREPL for debugging on specified port."
  []
  (try
    (let [{:keys [port] :as server} (nrepl/start-server)]
      (log/info "Starting nREPL server on port" port)
      (spit ".nrepl-port" port)
      server)

    (catch Throwable t
      (log/error t "Failed to start nREPL")
      (throw t))))


