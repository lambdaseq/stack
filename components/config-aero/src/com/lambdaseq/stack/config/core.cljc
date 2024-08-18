(ns com.lambdaseq.stack.config.core
  (:require [aero.core :as aero]
            #?(:clj [clojure.java.io :as io])))

(defn load-config! [config-path]
  #?(:clj     (-> config-path
                  (io/resource)
                  (aero/read-config {:profile (System/getenv "STACK_PROFILE")}))
     :default (-> config-path
                  (aero/read-config {:profile (System/getenv "STACK_PROFILE")}))))
