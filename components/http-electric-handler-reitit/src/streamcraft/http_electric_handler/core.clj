(ns streamcraft.http-electric-handler.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [contrib.assert :refer [check]]
            [hyperfiddle.electric-ring-adapter :as electric-ring]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.cookies :as cookies]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as res])
  (:import (clojure.lang ExceptionInfo)))


;; The following middleware setup has been copied as is from electric-starter-app.server-jetty
(defn not-found-handler [_ring-request]
  (-> (res/not-found "Not found")
      (res/content-type "text/plain")))

(defn template
  "In string template `<div>$:foo/bar$</div>`, replace all instances of $key$
with target specified by map `m`. Target values are coerced to string with `str`.
  E.g. (template \"<div>$:foo$</div>\" {:foo 1}) => \"<div>1</div>\" - 1 is coerced to string."
  [t m] (reduce-kv (fn [acc k v] (str/replace acc (str "$" k "$") (str v))) t m))

(defn get-modules [manifest-path]
  (when-let [manifest (io/resource manifest-path)]
    (let [manifest-folder (when-let [folder-name (second (rseq (str/split manifest-path #"\/")))]
                            (str "/" folder-name "/"))]
      (->> (slurp manifest)
           (edn/read-string)
           (reduce (fn [r module] (assoc r (keyword "hyperfiddle.client.module" (name (:name module)))
                                           (str manifest-folder (:output-name module)))) {})))))
(defn wrap-index-page
  "Server the `index.html` file with injected javascript modules from `manifest.edn`.
`manifest.edn` is generated by the client build and contains javascript modules
information."
  [next-handler resources-path manifest-path]
  (fn [ring-req]
    (if-let [response (res/resource-response (str (check string? resources-path) "/index.html"))]
      (if-let [bag (get-modules (check string? manifest-path))]
        (-> (res/response (template (slurp (:body response)) bag)) ; TODO cache in prod mode
            (res/content-type "text/html")                  ; ensure `index.html` is not cached
            (res/header "Cache-Control" "no-store")
            (res/header "Last-Modified" (get-in response [:headers "Last-Modified"])))
        (-> (res/not-found (pr-str ::missing-shadow-build-manifest)) ; can't inject js modules
            (res/content-type "text/plain")))
      (next-handler ring-req))))

(defn wrap-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch ExceptionInfo e
        (throw #p e))
      (catch Throwable e
        (throw #p e)))))

(defn electric-handler [entrypoint {:keys [jetty hyperfiddle]}]
  (let [{:keys [resources-path]} jetty
        {:keys [manifest-path]} hyperfiddle]
    (-> not-found-handler
        (wrap-index-page resources-path manifest-path)
        (wrap-resource resources-path)
        (wrap-content-type)
        (electric-ring/wrap-electric-websocket entrypoint)
        (cookies/wrap-cookies)
        (electric-ring/wrap-reject-stale-client hyperfiddle)
        (wrap-params))))