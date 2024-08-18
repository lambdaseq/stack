(ns com.lambdaseq.stack.client-base.app.router
  (:require [hyperfiddle.electric :as e]
            [com.lambdaseq.stack.frontend-router.api :as frontend-router]
            [missionary.core :as ms]
            [com.lambdaseq.stack.client-base.app.layout :as layout]
            [com.lambdaseq.stack.client-base.app.home.routes :as home.routes]
            [com.lambdaseq.stack.client-base.app.project.routes :as projects.routes]
            #?@(:cljs [[reitit.frontend :as rf]
                       [reitit.frontend.easy :as rfe]
                       [reitit.coercion.malli :as rcm]])))

(e/def routes
  [["/app" {:layout layout/MainViewLayout}
    ["" {:name        :root
         :redirect-to [:home]}]
    ["not-found" {:name  :not-found
                  :view  layout/NotFoundView
                  :title "Not Found"}]
    home.routes/routes
    projects.routes/routes]])

(e/def rf-router
  (e/client
    (rf/router routes {:data      {:coercion rcm/coercion}
                       :conflicts false})))

(e/def app-router
  (e/client
    (->> (ms/observe
           (fn [!]
             (rfe/start!
               rf-router
               !
               {:use-fragment false})))
         (ms/relieve {})
         new)))

(e/defn RouteSwitch []
  (e/client
    (let [{:keys [layout view title breadcrumbs redirect-to]} frontend-router/data]
      (if redirect-to
        (let [[name path query] redirect-to]
          (rfe/navigate name {:path  path
                              :query query}))
        (layout. {:view        view
                  :title       (layout/resolve-title. title)
                  :breadcrumbs breadcrumbs})))))
