(ns com.lambdaseq.stack.client-base.dev.main
  (:require [hyperfiddle.electric :as e]
            [com.lambdaseq.stack.client-base.app :refer [App]]
            [com.lambdaseq.stack.web-app.main :as app]))

(def entrypoint (e/boot-client {} App nil))

(defn ^:dev/after-load ^:export start! []
  (app/start! entrypoint))

(defn ^:dev/before-load stop! []
  (app/stop!))
